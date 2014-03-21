package org.controlsfx.validation;

import java.util.Collection;
import java.util.Objects;

import javafx.scene.control.Control;

public class Validators {
	
	private Validators(){}
	
	public static <T> Validator<T> createEmptyValidator(final String message, final Severity severity ) {
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
	
	public static <T> Validator<T> createEmptyValidator(final String message ) {
		return createEmptyValidator(message, Severity.ERROR);
	}
	
	public static <T> Validator<T> createEqualsValidator(final String message, final Severity severity, final Collection<T> values ) {
		
		return new Validator<T>() {

			@Override
			public ValidationResult validate( Control c, T value) {
				return new ValidationResultBuilder(c)
				  .addMessageIf(
				     message, 
				     severity == null? Severity.ERROR: severity,
				     () -> {
				    	for( T v: values ) {
				    		if (Objects.equals(value, v)) return false;
				    	}
				    	return true;
				     }
				  ).build();
			}
			
		};
	}
	
	
	public static <T> Validator<T> createEqualsValidator(final String message, final Collection<T> values ) {
		return createEqualsValidator(message, Severity.ERROR, values);
	}	
	
}
