package org.controlsfx.validation;

import org.controlsfx.control.decoration.Decoration;

public interface ValidationDecorator {
     Decoration createDecoration( ValidationMessage message ); 
}
