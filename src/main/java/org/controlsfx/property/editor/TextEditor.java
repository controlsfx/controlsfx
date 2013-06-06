package org.controlsfx.property.editor;

import javafx.scene.control.TextField;

import org.controlsfx.property.Property;

public class TextEditor extends AbstractPropertyEditor<TextField> {

    public TextEditor( Property property ) {
        super(property, new TextField());
        control.textProperty().addListener( getPropertyChangeListener() );
    }

    @Override public Object getValue() {
        return control.getText();
    }

    @Override public void setValue(Object value) {
        if ( value instanceof String ) {
           control.setText((String)value);
        }
    }

}
