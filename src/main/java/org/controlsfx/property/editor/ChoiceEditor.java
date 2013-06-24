/**
 * Copyright (c) 2013, ControlsFX
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

import java.util.Collection;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

import org.controlsfx.control.PropertySheet.Item;

/**
 * A {@link PropertyEditor} that is suitable for use for selecting from a
 * limited range of choices.
 */
public class ChoiceEditor<T> extends AbstractPropertyEditor<T, ComboBox<T>> {

    /**
     * Creates a default ChoiceEditor instance that will edit the given 
     * {@link item}.
     * 
     * @param item The item that this editor instance should be responsible for 
     *      editing.
     */
    public ChoiceEditor( Item property, Collection<T> choices ) {
        super(property, new ComboBox<T>());
        getEditor().setItems(FXCollections.observableArrayList(choices));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected ObservableValue<T> getObservableValue() {
        return getEditor().getSelectionModel().selectedItemProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override public void setValue(T value) {
        getEditor().getSelectionModel().select(value);
    }
}
