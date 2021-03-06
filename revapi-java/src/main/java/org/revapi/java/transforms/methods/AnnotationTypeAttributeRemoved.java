package org.revapi.java.transforms.methods;

import java.io.Reader;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import org.revapi.AnalysisContext;
import org.revapi.Difference;
import org.revapi.DifferenceTransform;
import org.revapi.java.spi.Code;
import org.revapi.java.spi.JavaModelElement;

/**
 * @author Lukas Krejci
 * @since 0.1
 */
public class AnnotationTypeAttributeRemoved implements DifferenceTransform<JavaModelElement> {
    private Locale locale;
    private final Pattern[] codes;

    public AnnotationTypeAttributeRemoved() {
        codes = new Pattern[]{Pattern.compile("^" + Pattern.quote(Code.METHOD_REMOVED.code()) + "$")};
    }

    @Nonnull
    @Override
    public Pattern[] getDifferenceCodePatterns() {
        return codes;
    }

    @Nullable
    @Override
    public String[] getConfigurationRootPaths() {
        return null;
    }

    @Nullable
    @Override
    public Reader getJSONSchema(@Nonnull String configurationRootPath) {
        return null;
    }

    @Override
    public void initialize(@Nonnull AnalysisContext analysisContext) {
        locale = analysisContext.getLocale();
    }

    @Nullable
    @Override
    public Difference transform(@Nullable JavaModelElement oldElement, @Nullable JavaModelElement newElement,
        @Nonnull Difference difference) {

        @SuppressWarnings("ConstantConditions")
        ExecutableElement method = (ExecutableElement) oldElement.getModelElement();

        if (method.getEnclosingElement().getKind() == ElementKind.ANNOTATION_TYPE) {
            return Code.METHOD_ATTRIBUTE_REMOVED_FROM_ANNOTATION_TYPE.createDifference(locale);
        }

        return difference;
    }

    @Override
    public void close() {
    }
}
