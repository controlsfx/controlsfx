package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ValidationResult {

	private List<ValidationMessage> errors   = new ArrayList<>();
	private List<ValidationMessage> warnings = new ArrayList<>();
	
	public ValidationResult() {
	}
	
	public ValidationResult add( ValidationMessage message ) {
		
		if ( Severity.ERROR == message.getSeverity() ) {
			errors.add( message);
		} else if ( Severity.WARNING == message.getSeverity() ) {
			warnings.add(message);
		}
		return this;
	}
	
	public ValidationResult addMessages( Collection<ValidationMessage> messages ) {
		messages.stream().forEach( message-> add(message));
		return this;
	}
	
	public ValidationResult add( ValidationResult validationResult ) {
		errors.addAll(validationResult.getErrors());
		warnings.addAll(validationResult.getWarnings());
		return this;
	}
	
	public ValidationResult addValidationResults( Collection<ValidationResult> validationResults ) {
		validationResults.stream().forEach( result-> add(result));
		return this;
	}
	
	
	public List<? extends ValidationMessage> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	public List<? extends ValidationMessage> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
	
	public List<? extends ValidationMessage> getMessages() {
		List<ValidationMessage> messages = new ArrayList<>();
		messages.addAll(errors);
		messages.addAll(warnings);
		return Collections.unmodifiableList(messages);
	}
	
}
