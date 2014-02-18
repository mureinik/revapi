/*
 * Copyright 2014 Lukas Krejci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.revapi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public interface ProblemTransform {

    void initialize(@Nonnull Configuration configuration);

    /**
     * Returns a transformed version of the problem. If this method returns null, the problem is
     * discarded and not reported. Therefore, if you don't want to transform a problem, just return it.
     *
     * @param oldElement the old element that triggered the problem
     * @param newElement the new element that triggered the problem
     * @param problem    the problem description
     *
     * @return the transformed problem or the passed in problem if no transformation necessary or null if the problem
     * should be discarded
     */
    @Nullable
    MatchReport.Problem transform(@Nullable Element oldElement, @Nullable Element newElement,
        @Nonnull MatchReport.Problem problem);
}
