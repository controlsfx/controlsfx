package org.controlsfx.property.editor;

import javafx.beans.value.ObservableValue;

import org.controlsfx.control.PropertySheet.Item;

public class AbstractObjectPropertyEditor<T, C extends AbstractObjectField<T> > extends AbstractPropertyEditor<C> {

    private final Class<T> type; 
    
    public AbstractObjectPropertyEditor(Item property, C control, boolean readonly) {
        super(property, control, readonly);
        type = control.getType();
    }

    public AbstractObjectPropertyEditor(Item property, C control) {
        this(property, control, false);
    }
    
    protected ObservableValue<T> getObservableValue() {
        return control.getObjectProperty();
    }

    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if ( type.isAssignableFrom(value.getClass())) {
           control.getObjectProperty().set((T)value);
        }
    }
    

}
