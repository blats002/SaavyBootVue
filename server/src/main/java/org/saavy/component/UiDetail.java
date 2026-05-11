package org.saavy.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiDetail {
    String key() default "";
    String title() default "";
    String dialogHeader() default "";
    String parentField() default "";
    String parentValue() default "";
    String messages()  default "";
    String detailEndpoint()  default "";
}

