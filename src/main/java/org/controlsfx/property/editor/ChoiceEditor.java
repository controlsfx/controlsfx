package org.controlsfx.property.editor;

import java.util.Collection;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import org.controlsfx.property.PropertyDescriptor;

public class ChoiceEditor extends AbstractPropertyEditor<ComboBox<Object>> {

    public ChoiceEditor(  PropertyDescriptor property, Collection<Object> choices ) {
        super(property, new ComboBox<Object>());
        control.setItems(FXCollections.observableArrayList(choices));
    }
    
    @Override protected ObservableValue<?> getObservableValue() {
        return control.selectionModelProperty();
    }

    @Override public void setValue(Object value) {
          control.getSelectionModel().select(value);
    }

}
