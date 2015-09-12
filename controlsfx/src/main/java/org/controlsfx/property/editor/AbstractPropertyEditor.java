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

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import org.controlsfx.control.PropertySheet.Item;

/**
 * An abstract implementation of the {@link PropertyEditor} interface.
 *
 * @param <T> The type of the property being edited.
 * @param <C> The type of Node that is used to edit this property.
 */
public abstract class AbstractPropertyEditor<T, C extends Node> implements PropertyEditor<T> {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/

    private final Item property;
    private final C control;
    private boolean suspendUpdate;
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates an editable AbstractPropertyEditor instance for the given property
     * using the given editing control.
     * 
     * @param property The property that the instance is responsible for editing.
     * @param control The control that is responsible for editing the property.
     */
    public AbstractPropertyEditor(Item property, C control) {
        this(property, control, ! property.isEditable());
    }
    
    /**
     * Creates an AbstractPropertyEditor instance for the given property
     * using the given editing control. It may be read-only or editable, based
     * on the readonly boolean parameter being true or false.
     * 
     * @param property The property that the instance is responsible for editing.
     * @param control The control that is responsible for editing the property.
     * @param readonly Specifies whether the editor should allow input or not.
     */
    public AbstractPropertyEditor(Item property, C control, boolean readonly) {
        this.control = control;
        this.property = property;
        if (! readonly) {
            getObservableValue().addListener((ObservableValue<? extends Object> o, Object oldValue, Object newValue) -> {
                if (! suspendUpdate) {
                    suspendUpdate = true;
                    AbstractPropertyEditor.this.property.setValue(getValue());
                    suspendUpdate = false;
                }
            });
            
            if (property.getObservableValue().isPresent()) {
                property.getObservableValue().get().addListener((ObservableValue<? extends Object> o, Object oldValue, Object newValue) -> {
                    if (! suspendUpdate) {
                        suspendUpdate = true;
                        AbstractPropertyEditor.this.setValue((T) property.getValue());
                        suspendUpdate = false;
                    }
                });
            }
            
        }
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * Returns an {@link ObservableValue} of the property that this property
     * editor is responsible for editing. This is the editor's value, e.g. a 
     * TextField's textProperty().
     */
    protected abstract ObservableValue<T> getObservableValue();
    
    /**
     * Returns the property that this property editor is responsible for editing.
     */
    public final Item getProperty() {
        return property;
    }
        
    /**
     * {@inheritDoc}
     */
    @Override public C getEditor() {
        return control;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public T getValue() {
        return getObservableValue().getValue();
    }
}
