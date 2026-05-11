package org.saavy.component;

import java.lang.annotation.*;
import java.util.HashMap;
import java.util.Map;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiField {
    String label() default "";
    String type() default "";
    boolean sortable() default true;
    boolean hidden() default false;
    boolean editable() default true;
    boolean required() default false;
    int order() default 0;
    String optionLabel() default "label";
    String optionValue() default "value";
    String optionsEndpoint() default "";
    String enumOptions() default "";
}
