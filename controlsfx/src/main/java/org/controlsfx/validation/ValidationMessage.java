package org.controlsfx.validation;

import javafx.scene.control.Control;

public interface ValidationMessage {
	String getText();
	Severity getSeverity();
	Control getTarget();
}
