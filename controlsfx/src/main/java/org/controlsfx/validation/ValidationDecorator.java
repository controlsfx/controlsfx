package org.controlsfx.validation;

import java.util.Collection;

import org.controlsfx.control.decoration.Decoration;


@FunctionalInterface
public interface ValidationDecorator {
	
     Collection<Decoration> createDecorations( ValidationMessage message );
     
}
