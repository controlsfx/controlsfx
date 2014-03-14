package org.controlsfx.validation;

import javafx.scene.control.Control;

public class Validators {
	
	private Validators(){}
	
	public static <T> Validator<T> getEmptyValidator(final String message, Severity severity ) {
		return new Validator<T>() {

			@Override
			public ValidationResult validate( Control c, T value) {
				return new ValidationResultBuilder(c)
				  .addMessageIf(
				     message, 
				     severity == null? Severity.ERROR: severity,
				     () -> value instanceof String? value.toString().trim().isEmpty(): value == null)
				  .build();
			}
			
		};
	}
	
	public static <T> Validator<T> getEmptyValidator(final String message ) {
		return getEmptyValidator(message, Severity.ERROR);
	}
	
}
