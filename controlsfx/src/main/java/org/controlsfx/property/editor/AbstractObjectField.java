package org.controlsfx.property.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.CustomTextField;

// package-private for now...
abstract class AbstractObjectField<T> extends HBox {

    //TODO: Replace with CSS
    private static final Image image = new Image("/org/controlsfx/control/open-editor.png");

    private final CustomTextField textField = new CustomTextField();

    private ObjectProperty<T> objectProperty = new SimpleObjectProperty<T>();

    public AbstractObjectField() {
        super(1);
        textField.setEditable(false);
        textField.setFocusTraversable(false);

        StackPane button = new StackPane(new ImageView(image));
        button.setCursor(Cursor.DEFAULT);

        button.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent e) {
                if ( MouseButton.PRIMARY == e.getButton() ) {
                    final T result = edit(objectProperty.get());
                    if (result != null) {
                        objectProperty.set(result);
                    }
                }
            }
        });

        textField.setRight(button);
        getChildren().add(textField);
        HBox.setHgrow(textField, Priority.ALWAYS);

        objectProperty.addListener(new ChangeListener<T>() {
            @Override public void changed(ObservableValue<? extends T> o, T oldValue, T newValue) {
                textProperty().set(objectToString(newValue));
            }
        });
    }

    protected StringProperty textProperty() {
        return textField.textProperty();
    }

    public ObjectProperty<T> getObjectProperty() {
        return objectProperty;
    }

    protected String objectToString(T object) {
        return object == null ? "" : object.toString();
    }

    protected abstract Class<T> getType();

    protected abstract T edit(T object);
}
