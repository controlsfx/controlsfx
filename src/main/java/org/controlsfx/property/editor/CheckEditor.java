package org.controlsfx.property.editor;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;

import org.controlsfx.property.PropertyDescriptor;

public class CheckEditor extends AbstractPropertyEditor<CheckBox> {

    public CheckEditor( PropertyDescriptor property ) {
        super(property, new CheckBox());
    }
    
    @Override protected ObservableValue<?> getObservableValue() {
        return control.selectedProperty();
    }
    
    @Override public void setValue(Object value) {
        if (value instanceof Boolean ) {
           control.setSelected((Boolean)value);
        }
    }

}
