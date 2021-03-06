/*
 * Copyright 2014 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.revapi.java;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.revapi.API;
import org.revapi.AnalysisContext;
import org.revapi.ApiAnalyzer;
import org.revapi.ArchiveAnalyzer;
import org.revapi.DifferenceAnalyzer;
import org.revapi.java.compilation.CompilationValve;
import org.revapi.java.compilation.ProbingEnvironment;
import org.revapi.java.spi.Check;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public final class JavaApiAnalyzer implements ApiAnalyzer {

    private final ExecutorService compilationExecutor = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private volatile int cnt;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "Java API Compilation Thread #" + (++cnt));
        }
    });

    private AnalysisContext analysisContext;
    private AnalysisConfiguration configuration;
    private final Iterable<Check> checks;

    public JavaApiAnalyzer() {
        this(ServiceLoader.load(Check.class, JavaApiAnalyzer.class.getClassLoader()));
    }

    public JavaApiAnalyzer(Iterable<Check> checks) {
        this.checks = checks;
    }

    @Nullable
    @Override
    public String[] getConfigurationRootPaths() {
        ArrayList<String> checkConfigPaths = new ArrayList<>();
        checkConfigPaths.add("revapi.java");

        for (Check c : checks) {
            String[] cp = c.getConfigurationRootPaths();
            if (cp != null) {
                checkConfigPaths.addAll(Arrays.asList(cp));
            }
        }

        String[] configs = new String[checkConfigPaths.size()];
        configs = checkConfigPaths.toArray(configs);

        return configs;
    }

    @Nullable
    @Override
    public Reader getJSONSchema(@Nonnull String configurationRootPath) {
        if ("revapi.java".equals(configurationRootPath)) {
            return new InputStreamReader(getClass().getResourceAsStream("/META-INF/config-schema.json"),
                Charset.forName("UTF-8"));
        }

        for (Check check : checks) {
            String[] roots = check.getConfigurationRootPaths();
            if (roots == null) {
                continue;
            }

            for (String root : check.getConfigurationRootPaths()) {
                if (configurationRootPath.equals(root)) {
                    return check.getJSONSchema(root);
                }
            }
        }

        return null;
    }

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
        this.analysisContext = analysisContext;
        this.configuration = AnalysisConfiguration.fromModel(analysisContext.getConfiguration());
    }

    @Nonnull
    @Override
    public ArchiveAnalyzer getArchiveAnalyzer(@Nonnull API api) {
        Set<File> bootstrapClasspath =
            api == analysisContext.getOldApi() ? configuration.getOldApiBootstrapClasspath() :
                configuration.getNewApiBootstrapClasspath();
        boolean ignoreMissingAnnotations = configuration.isIgnoreMissingAnnotations();
        boolean ignoreAdditionalClasspathContributions = configuration.isIgnoreAdditionalClasspathContributions();

        return new JavaArchiveAnalyzer(api, compilationExecutor, configuration.getMissingClassReporting(),
            ignoreMissingAnnotations, bootstrapClasspath, ignoreAdditionalClasspathContributions);
    }

    @Nonnull
    @Override
    public DifferenceAnalyzer getDifferenceAnalyzer(@Nonnull ArchiveAnalyzer oldArchive,
        @Nonnull ArchiveAnalyzer newArchive) {
        JavaArchiveAnalyzer oldA = (JavaArchiveAnalyzer) oldArchive;
        JavaArchiveAnalyzer newA = (JavaArchiveAnalyzer) newArchive;

        ProbingEnvironment oldEnvironment = oldA.getProbingEnvironment();
        ProbingEnvironment newEnvironment = newA.getProbingEnvironment();
        CompilationValve oldValve = oldA.getCompilationValve();
        CompilationValve newValve = newA.getCompilationValve();

        return new JavaElementDifferenceAnalyzer(analysisContext, oldEnvironment, oldValve, newEnvironment, newValve,
            checks, configuration);
    }

    @Override
    public void close() {
        compilationExecutor.shutdown();
    }
}
