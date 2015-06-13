/**
 * Copyright (c) 2015 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.property.editor;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.textfield.CustomTextField;

// package-private for now...
abstract class AbstractObjectField<T> extends HBox {

    //TODO: Replace with CSS
    private static final Image image = new Image(AbstractObjectField.class.getResource("/org/controlsfx/control/open-editor.png").toExternalForm()); //$NON-NLS-1$

    private final CustomTextField textField = new CustomTextField();

    private ObjectProperty<T> objectProperty = new SimpleObjectProperty<>();

    public AbstractObjectField() {
        super(1);
        textField.setEditable(false);
        textField.setFocusTraversable(false);

        StackPane button = new StackPane(new ImageView(image));
        button.setCursor(Cursor.DEFAULT);

        button.setOnMouseReleased(e -> {
            if ( MouseButton.PRIMARY == e.getButton() ) {
                final T result = edit(objectProperty.get());
                if (result != null) {
                    objectProperty.set(result);
                }
            }
        });

        textField.setRight(button);
        getChildren().add(textField);
        HBox.setHgrow(textField, Priority.ALWAYS);

        objectProperty.addListener((o, oldValue, newValue) -> textProperty().set(objectToString(newValue)));
    }

    protected StringProperty textProperty() {
        return textField.textProperty();
    }

    public ObjectProperty<T> getObjectProperty() {
        return objectProperty;
    }

    protected String objectToString(T object) {
        return object == null ? "" : object.toString(); //$NON-NLS-1$
    }

    protected abstract Class<T> getType();

    protected abstract T edit(T object);
}
