package org.controlsfx.validation;

import org.controlsfx.control.decoration.Decoration;

@FunctionalInterface
public interface ValidationDecorator {
     Decoration createDecoration( ValidationMessage message ); 
}
