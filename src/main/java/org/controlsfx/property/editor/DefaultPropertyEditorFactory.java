package org.controlsfx.property.editor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 * A default implementation of the {@link Callback} type required by the
 * {@link PropertySheet} 
 * {@link PropertySheet#propertyEditorFactory() property editor factory}. By 
 * default this is the implementation used by PropertySheet, but developers may
 * choose to provide their own, or more likely, extend this implementation
 * and override the {@link #call(Item)} method to add in support for additional
 * editor types.
 *
 * @see PropertySheet
 */
public class DefaultPropertyEditorFactory implements Callback<Item, PropertyEditor<?>> {
    
    @Override public PropertyEditor<?> call(Item item) {
        Class<?> type = item.getType();
        
        //TODO: add support for char and collection editors
        if (type != null && type == String.class) {
            return new TextEditor(item);
        }

        if (type != null && isNumber(type)) {
            return new NumericEditor(item);
        }
        
        if (type != null && (type == boolean.class || type == Boolean.class)) {
            return new CheckEditor(item);
        }

        if (type != null && type.isAssignableFrom(Color.class)) {
            return new ColorEditor(item);
        }

        if (type != null && type.isEnum()) {
            return new ChoiceEditor<Object>(item, Arrays.<Object>asList(type.getEnumConstants()));
        }
        
        if (type != null && type == Font.class) {
            return new FontEditor(item);
        }
        
        return null; 
    }
    
    private static Class<?>[] numericTypes = new Class[]{
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        BigInteger.class, BigDecimal.class
    };    
    
    // there should be better ways to do this
    private static boolean isNumber(Class<?> type)  {
        if ( type == null ) return false;
        for (Class<?> cls : numericTypes) {
            if ( type == cls ) return true;
        }
        return false;
    }
}
