package org.controlsfx.validation;

import javafx.scene.control.Control;

public interface Validator<T> {

	ValidationResult validate( Control c, T newValue );
	
}
