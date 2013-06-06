package org.controlsfx.property.editor;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;

import org.controlsfx.property.PropertyDescriptor;

public class TextEditor extends AbstractPropertyEditor<TextField> {

    public TextEditor( PropertyDescriptor property ) {
        super(property, new TextField());
    }
    
    @Override protected StringProperty getObservableValue() {
        return control.textProperty();
    }

    @Override public void setValue(Object value) {
        if ( value instanceof String ) {
           control.setText((String)value);
        }
    }

}
