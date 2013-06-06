package org.controlsfx.property.editor;

import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import org.controlsfx.property.Property;

public class ChoiceEditor extends AbstractPropertyEditor<ComboBox<Object>> {

    public ChoiceEditor(  Property property, Collection<Object> choices ) {
        super(property, new ComboBox<Object>());
        control.setItems(FXCollections.observableArrayList(choices));
        control.selectionModelProperty().addListener(getPropertyChangeListener());
        
    }

    @Override public Object getValue() {
        return control.getSelectionModel().getSelectedItem();
    }

    @Override public void setValue(Object value) {
          control.getSelectionModel().select(value);
    }

}
