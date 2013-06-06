package org.controlsfx.property.editor;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import org.controlsfx.property.PropertyDescriptor;

public class ColorEditor extends AbstractPropertyEditor< ColorPicker> {

    public ColorEditor( PropertyDescriptor property ) {
        super(property, new ColorPicker());
    }
    
    @Override protected ObservableValue<?> getObservableValue() {
        return control.valueProperty();
    }

    @Override public void setValue(Object value) {
        if ( value instanceof Color ) {
           control.setValue((Color) value);
        }
    }

}
