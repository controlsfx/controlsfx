package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.controlsfx.control.decoration.Decoration;


@FunctionalInterface
public interface ValidationDecorator {
	
     Collection<Decoration> createDecorations( ValidationMessage message );
     
}
