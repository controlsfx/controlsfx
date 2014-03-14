package org.controlsfx.validation;

import static org.controlsfx.validation.SimpleValidationMessage.error;
import static org.controlsfx.validation.SimpleValidationMessage.warning;

import java.util.function.Supplier;

import javafx.scene.control.Control;

public class ValidationResultBuilder {

	private final Control target;
	private final ValidationResult validationResult = new ValidationResult();
	
	public ValidationResultBuilder( Control target ) { 
		this.target = target; 
    }
	
	public ValidationResult build() {
		return validationResult;
	}
	
	public ValidationResultBuilder addError( String text ) {
		validationResult.add ( error( target, text ));
		return this;
	}
	
	public ValidationResultBuilder addErrorIf( String text,  Supplier<Boolean> condition ) {
		if ( condition.get()) {
			validationResult.add ( error( target, text ));
		}
		return this;
	}
	
	public ValidationResultBuilder addWarning( String text ) {
		validationResult.add ( warning( target, text ));
		return this;
	}
	
	public ValidationResultBuilder addWarningIf( String text,  Supplier<Boolean> condition ) {
		if ( condition.get()) {
			validationResult.add ( warning( target, text ));
		}
		return this;
	}
	
	
	
}
