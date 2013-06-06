package org.controlsfx.property.editor;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.Region;

import org.controlsfx.property.PropertyDescriptor;

public abstract class AbstractPropertyEditor<T extends Region> implements PropertyEditor {

    protected final PropertyDescriptor property;
    protected final T control;
    
    public AbstractPropertyEditor( PropertyDescriptor property, T control ) {
        this.control = control;
        this.property = property;
        getObservableValue().addListener(new ChangeListener<Object>() {
            @Override public void changed(ObservableValue<? extends Object> o, Object oldValue, Object newValue) {
                AbstractPropertyEditor.this.property.setValue(getValue());
            }
        });
    }
    
    @Override
    public T asNode() {
        return control;
    }
    
    protected abstract ObservableValue<?> getObservableValue();

    @Override public Object getValue() {
        return getObservableValue().getValue();
    }
    
}
