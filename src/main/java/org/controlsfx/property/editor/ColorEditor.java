package org.controlsfx.property.editor;

import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

import org.controlsfx.property.Property;

public class ColorEditor extends AbstractPropertyEditor< ColorPicker> {

    public ColorEditor( Property property ) {
        super(property, new ColorPicker());
        control.valueProperty().addListener(getPropertyChangeListener());
    }

    @Override public Color getValue() {
        return control.getValue();
    }

    @Override public void setValue(Object value) {
        if ( value instanceof Color ) {
           control.setValue((Color) value);
        }
    }

}
