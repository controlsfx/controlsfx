package org.controlsfx.validation;

import javafx.scene.control.Control;

public class SimpleValidationMessage implements ValidationMessage {
	
	private final String text;
	private final Severity severity;
	private final Control target;
	
	
	public static SimpleValidationMessage error( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.ERROR);
	}
	
	public static SimpleValidationMessage warning( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.WARNING);
	}
	
	public SimpleValidationMessage( Control target, String text, Severity severity ) {
		this.text = text;
		this.severity = severity;
		this.target = target;
	}

	@Override
	public Control getTarget() {
		return target;
	}
	
	
	@Override
	public String getText() {
		return text;
	}

	@Override
	public Severity getSeverity() {
		return severity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((severity == null) ? 0 : severity.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleValidationMessage other = (SimpleValidationMessage) obj;
		if (severity != other.severity)
			return false;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!target.equals(other.target))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%s)", severity, text);
	}

}
