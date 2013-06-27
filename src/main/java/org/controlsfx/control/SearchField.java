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
package org.controlsfx.control;

import impl.org.controlsfx.skin.SearchFieldSkin;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A simple control that adds a clear button to a TextField control. Instantiate
 * as you would a {@link TextField}.
 * 
 * @see CustomTextField
 */
public class SearchField extends Control {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final CustomTextField customTextField = new CustomTextField();

    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Instantiates a default SearchField.
     */
    public SearchField() {
        getStyleClass().add("search-field");
    }

    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new SearchFieldSkin(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return SearchField.class.getResource("searchfield.css").toExternalForm();
    }
    
    public CustomTextField getCustomTextField() {
        return customTextField;
    }

    /**
     * Returns the wrapped TextField, if the API on the SearchField control
     * itself is not sufficient.
     */
    public TextField getTextField() {
        return customTextField.getTextField();
    }

    /**
     * Delegate method that forwards on to {@link TextField#getPromptText()}.
     */
    public final String getPromptText() {
        return customTextField.getPromptText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#getText()}.
     */
    public final String getText() {
        return customTextField.getText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#promptTextProperty()}.
     */
    public final StringProperty promptTextProperty() {
        return customTextField.promptTextProperty();
    }

    /**
     * Delegate method that forwards on to {@link TextField#setPromptText(String)}.
     */
    public final void setPromptText(String value) {
        customTextField.setPromptText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#setText(String)}.
     */
    public final void setText(String value) {
        customTextField.setText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#textProperty()}.
     */
    public final StringProperty textProperty() {
        return customTextField.textProperty();
    }
}
