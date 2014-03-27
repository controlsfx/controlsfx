package org.controlsfx.validation;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.StyleClassDecoration;

public class StyleClassValidationDecorator implements ValidationDecorator {
	
	private final String errorClass;
	private final String warningClass;
	
	public StyleClassValidationDecorator() {
		this(null,null);
	}
	
	public StyleClassValidationDecorator( String errorClass, String warningClass ) {
		this.errorClass = errorClass != null? errorClass: "error";
		this.warningClass = warningClass != null? warningClass: "warning";	
	}
	
	@Override
	public Collection<Decoration> createDecorations(ValidationMessage message) {
		return Arrays.asList(new StyleClassDecoration( Severity.ERROR == message.getSeverity()? errorClass:warningClass));
	}
	
	@Override
	public String toString() {
		return "Style Class Validation Decorator";
	}

}
