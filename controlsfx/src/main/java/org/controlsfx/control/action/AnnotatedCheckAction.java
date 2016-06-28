package org.controlsfx.control.action;

import java.lang.reflect.Method;

@ActionCheck
public class AnnotatedCheckAction extends AnnotatedAction {

    /**
     * Instantiates an action that will call the specified method on the specified target.
     * This action is marked  with @ActionCheck
     *
     * @param text
     * @param method
     * @param target
     */
    public AnnotatedCheckAction(String text, Method method, Object target) {
        super(text, method, target);
    }
}
