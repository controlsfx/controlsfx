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

import impl.org.controlsfx.skin.CustomTextFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A base class for people wanting to customize a {@link TextField} to contain nodes
 * inside the text field itself, without being on top of the users typed-in text.
 * 
 * @see SearchField
 */
public class CustomTextField extends TextField {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    

    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Instantiates a default CustomTextField.
     */
    public CustomTextField() {
        getStyleClass().add("custom-text-field");
    }

    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- left
    private ObjectProperty<Node> left = new SimpleObjectProperty<Node>(this, "left");
    
    /**
     * Property representing the {@link Node} that is placed on the left of
     * the text field.
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }
    
    public final Node getLeft() {
        return left.get();
    }
    
    public final void setLeft(Node value) {
        left.set(value);
    }
    
    
    // --- right
    private ObjectProperty<Node> right = new SimpleObjectProperty<Node>(this, "right");
    
    /**
     * Property representing the {@link Node} that is placed on the right of
     * the text field.
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }
    
    public final Node getRight() {
        return right.get();
    }
    
    public final void setRight(Node value) {
        right.set(value);
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
        return new CustomTextFieldSkin(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return CustomTextField.class.getResource("customtextfield.css").toExternalForm();
    }
}
