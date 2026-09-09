package org.saavy.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UiMaster {
    String title() default "";
    String dialogHeader() default "";
    String optionLabel() default "";
    String messages() default "";
    String masterEndPoint() default "";
    boolean deletable() default true;
    String deletableField() default "";
}
