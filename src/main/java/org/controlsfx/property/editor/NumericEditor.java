package org.controlsfx.property.editor;

import java.lang.reflect.InvocationTargetException;

import javafx.scene.control.TextField;

import org.controlsfx.property.Property;

public class NumericEditor extends AbstractPropertyEditor<TextField> {

    private Class<? extends Number> sourceClass = Double.class;

    
    public NumericEditor( Property property ) {
        super(property, new NumericField());
        control.textProperty().addListener( getPropertyChangeListener() );
    }

    @Override public Object getValue() {
        try {
            return sourceClass.getConstructor( String.class).newInstance(control.getText());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    
    @SuppressWarnings("unchecked")
    @Override public void setValue(Object value) {
        if ( value instanceof Number ) {
           sourceClass = (Class<? extends Number>) value.getClass(); 
           control.setText(value.toString());
        }
    }

}

//TODO: Expose as new control?
class NumericField extends TextField {
    
    private static String regex = "^(([1-9]*)|(([1-9]*).([0-9]*)))$";
    
    @Override public void replaceText(int start, int end, String text) {
        String txt = getText().substring(0,start) + text + getText().substring(end);
        if (txt.matches(regex)) {
            super.replaceText(start, end, text);
        }
    }
    
}
