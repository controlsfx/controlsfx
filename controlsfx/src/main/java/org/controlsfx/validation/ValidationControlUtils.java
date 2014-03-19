package org.controlsfx.validation;

import javafx.scene.control.Control;

public class ValidationControlUtils {

	private ValidationControlUtils() {}
	
	
    private static String CTRL_REQUIRED_FLAG = "controlsfx.required.control";
	
	public static void setRequired( Control c, boolean required ) {
		c.getProperties().put(CTRL_REQUIRED_FLAG, required );
	}
	
	public static boolean isRequired( Control c ) {
		Object value = c.getProperties().get(CTRL_REQUIRED_FLAG);
		return value instanceof Boolean? (Boolean)value: false;
	}
	
}
