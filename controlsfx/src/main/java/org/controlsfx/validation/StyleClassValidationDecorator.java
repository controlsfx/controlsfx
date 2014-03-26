package org.controlsfx.validation;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.StyleClassDecoration;

public class StyleClassValidationDecorator implements ValidationDecorator {

	@Override
	public Collection<Decoration> createDecorations(ValidationMessage message) {
		return Arrays.asList(new StyleClassDecoration( Severity.ERROR == message.getSeverity()? "error":"warning"));
	}
	
	@Override
	public String toString() {
		return "Style Class Validation Decorator";
	}

}
