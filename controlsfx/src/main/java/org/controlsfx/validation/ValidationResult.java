package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ValidationResult {

	private List<ValidationMessage> errors   = new ArrayList<>();
	private List<ValidationMessage> warnings = new ArrayList<>();
	
	public ValidationResult() {}
	
	public static final ValidationResult fromMessages( ValidationMessage... messages ) {
		return new ValidationResult().addAll(messages);
	}
	
	public static final ValidationResult fromMessages( Collection<? extends ValidationMessage> messages ) {
		return new ValidationResult().addAll(messages);
	}
	
	public static final ValidationResult fromResults( ValidationResult... results ) {
		return new ValidationResult().combineAll(results);
	}

	public static final ValidationResult fromResults( Collection<ValidationResult> results ) {
		return new ValidationResult().combineAll(results);
	}
	
	public ValidationResult copy() {
		return ValidationResult.fromMessages(getMessages());
	}
	
	public ValidationResult add( ValidationMessage message ) {
		
		if ( message != null ) {
			switch( message.getSeverity() ) {
				case ERROR  : errors.add( message); break;
				case WARNING: warnings.add(message); break;
			}
		}
		
		return this;
	}
	
	public ValidationResult addAll( Collection<? extends ValidationMessage> messages ) {
		messages.stream().forEach( msg-> add(msg));
		return this;
	}
	
	public ValidationResult addAll( ValidationMessage... messages ) {
		return addAll(Arrays.asList(messages));
	}
	
	public ValidationResult combine( ValidationResult validationResult ) {
		return validationResult == null? copy(): copy().addAll(validationResult.getMessages());
	}
	
	public ValidationResult combineAll( Collection<ValidationResult> validationResults ) {
		return validationResults.stream().reduce(copy(), (x,r) -> {
			return r == null? x: x.addAll(r.getMessages());
		});
	}
	
	public ValidationResult combineAll( ValidationResult... validationResults ) {
		return combineAll( Arrays.asList(validationResults));
	}
	
	
	public Collection<? extends ValidationMessage> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	public Collection<? extends ValidationMessage> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
	
	public Collection<? extends ValidationMessage> getMessages() {
		List<ValidationMessage> messages = new ArrayList<>();
		messages.addAll(errors);
		messages.addAll(warnings);
		return Collections.unmodifiableList(messages);
	}
	
}
