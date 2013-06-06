package org.controlsfx.property.editor;

import javafx.scene.control.CheckBox;

import org.controlsfx.property.Property;

public class CheckEditor extends AbstractPropertyEditor<CheckBox> {

    public CheckEditor( Property property ) {
        super(property, new CheckBox());
        control.selectedProperty().addListener(getPropertyChangeListener());
    }

    @Override public Boolean getValue() {
        return control.isSelected();
    }

    @Override public void setValue(Object value) {
        if (value instanceof Boolean ) {
           control.setSelected((Boolean)value);
        }
    }

}
