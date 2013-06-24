package org.controlsfx.property.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public abstract class AbstractObjectField<T> extends HBox {

    private final TextField textField = new TextField();
    private final Button editButton = new Button("...");

    private ObjectProperty<T> objectProperty = new SimpleObjectProperty<T>();
    
    public AbstractObjectField() {
        super(1);
        textField.setEditable(false);
        textField.setFocusTraversable(false);
        getChildren().add( textField );
        getChildren().add( editButton );
        HBox.setHgrow(textField, Priority.ALWAYS);
        
        editButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                final T result = edit( objectProperty.get());
                if ( result != null ) {
                    objectProperty.set(result);
                }
            }
        });
        
        objectProperty.addListener( new ChangeListener<T>() {
            @Override public void changed(ObservableValue<? extends T> o, T oldValue, T newValue ) {
                textProperty().set( objectToString(newValue));
            }
        });
    }
    
    protected StringProperty textProperty() {
        return textField.textProperty();
    }
    
    public ObjectProperty<T> getObjectProperty() {
        return objectProperty;
    }
    
    protected String objectToString( T object ) {
        return object == null? "": object.toString();
    }
    
    protected abstract Class<T> getType();
    
    protected abstract T edit( T object );
    
}
