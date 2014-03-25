package org.controlsfx.validation;

import java.util.Collection;
import java.util.function.BiFunction;

import javafx.scene.control.Control;

public interface Validator<T> extends BiFunction<Control, T, ValidationResult> {
	
	static <T> Validator<T> createEmptyValidator(final String message, final Severity severity ) {
		return new Validator<T>() {

			@Override
			public ValidationResult apply( Control c, T value) {
				return ValidationResult
				  .fromMessageIf(c, message, severity,
				     value instanceof String? value.toString().trim().isEmpty(): value == null);
			}
			
		};
	}
	
	static <T> Validator<T> createEmptyValidator(final String message ) {
		return createEmptyValidator(message, Severity.ERROR);
	}
	
	static <T> Validator<T> createEqualsValidator(final String message, final Severity severity, final Collection<T> values ) {
		
		return new Validator<T>() {

			@Override
			public ValidationResult apply( Control c, T value) {
				return ValidationResult.fromMessageIf(c,message,severity, !values.contains(value)); 
			}
			
		};
	}
	
	
    static <T> Validator<T> createEqualsValidator(final String message, final Collection<T> values ) {
		return createEqualsValidator(message, Severity.ERROR, values);
	}
	
}
