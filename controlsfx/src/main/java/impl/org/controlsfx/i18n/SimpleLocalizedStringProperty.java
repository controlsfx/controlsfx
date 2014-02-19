package impl.org.controlsfx.i18n;

import javafx.beans.property.SimpleStringProperty;

public class SimpleLocalizedStringProperty extends SimpleStringProperty {

	public SimpleLocalizedStringProperty() {
	}

	public SimpleLocalizedStringProperty(String initialValue) {
		super(initialValue);
	}

	public SimpleLocalizedStringProperty(Object bean, String name) {
		super(bean, name);
	}

	public SimpleLocalizedStringProperty(Object bean, String name, String initialValue) {
		super(bean, name, initialValue);
	}
	
	public String getValue() {
		return Localization.localize(super.getValue());
	}

}
