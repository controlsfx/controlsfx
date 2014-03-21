package org.controlsfx.validation;

import javafx.scene.control.Control;

public interface ValidationMessage {
	
	String getText();
	Severity getSeverity();
	Control getTarget();
	
	static ValidationMessage error( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.ERROR);
	}
	
	static ValidationMessage warning( Control target, String text ) {
		return new SimpleValidationMessage(target, text, Severity.WARNING);
	}
}
