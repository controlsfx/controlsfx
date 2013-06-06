package org.controlsfx.property.editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;

import org.controlsfx.property.Property;

public abstract class AbstractPropertyEditor<T extends Control> implements PropertyEditor {

    protected final Property property;
    protected final T control;
    
    public AbstractPropertyEditor( Property property, T control ) {
        this.control = control;
        this.property = property;
    }
    
    @Override
    public T asControl() {
        return control;
    }

    protected ChangeListener<Object> getPropertyChangeListener() {
        return new ChangeListener<Object>() {
            @Override public void changed(ObservableValue<? extends Object> o, Object oldValue, Object newValue) {
                property.setValue(getValue());
            }
        };
    }
    
}
