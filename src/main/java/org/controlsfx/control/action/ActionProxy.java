package org.controlsfx.control.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to allow conversion of class methods to {@link Action}  
 * @param id action id
 * @param text action text, required
 * @param image action image 
 * @param longText action long text
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionProxy {
	String id() default "";
    String text();
    String image() default "";
    String longText() default "";
}
