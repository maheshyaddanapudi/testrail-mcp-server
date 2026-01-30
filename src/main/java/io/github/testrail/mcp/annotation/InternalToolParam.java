package io.github.testrail.mcp.annotation;

import java.lang.annotation.*;

/**
 * Provides metadata for internal tool parameters.
 * Used for documentation and validation.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalToolParam {
    
    /**
     * Description of what this parameter is for.
     */
    String description();
    
    /**
     * Whether this parameter is required. Defaults to true.
     */
    boolean required() default true;
    
    /**
     * Default value if not provided. Empty string means no default.
     */
    String defaultValue() default "";
}
