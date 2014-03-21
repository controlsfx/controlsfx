package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import javafx.scene.control.Control;

public class ValidationResultBuilder {

	private final Control target;
	private final Collection<ValidationMessage> messages = new ArrayList<>();
	
	public ValidationResultBuilder( Control target ) { 
		this.target = target; 
    }
	
	public ValidationResult build() {
		return ValidationResult.fromMessages(messages);
	}
	
	public ValidationResultBuilder addMessageIf( String text, Severity severity, Supplier<Boolean> condition ) {
		if ( condition == null || condition.get()) {
			messages.add( new SimpleValidationMessage(target, text, severity));
		}
		return this;
	}
	
	public ValidationResultBuilder addMessage( String text, Severity severity ) {
		return addMessageIf(text,severity,null);
	}
	
	
	public ValidationResultBuilder addError( String text ) {
		return addMessage( text, Severity.ERROR );
	}
	
	public ValidationResultBuilder addErrorIf( String text, Supplier<Boolean> condition ) {
		return addMessageIf( text, Severity.ERROR, condition );
	}
	
	public ValidationResultBuilder addWarning( String text ) {
		return addMessage( text, Severity.WARNING );
	}
	
	public ValidationResultBuilder addWarningIf( String text,  Supplier<Boolean> condition ) {
		return addMessageIf( text, Severity.WARNING, condition );
	}
	
	
	
}
