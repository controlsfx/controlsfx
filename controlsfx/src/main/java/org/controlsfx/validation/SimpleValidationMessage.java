package org.controlsfx.validation;

public class SimpleValidationMessage implements ValidationMessage {
	
	private final String text;
	private final Severity severity;
	
	public SimpleValidationMessage( String text, Severity severity ) {
		this.text = text;
		this.severity = severity;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

}
