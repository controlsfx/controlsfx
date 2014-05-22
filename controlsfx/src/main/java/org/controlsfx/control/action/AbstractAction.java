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
package org.controlsfx.control.action;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.i18n.SimpleLocalizedStringProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCombination;

/**
 * A convenience class that implements the {@link Action} interface and provides
 * a simpler API. It is highly recommended to use this class rather than 
 * implement the {@link Action} interface directly.
 * 
 * <p>To better understand how to use actions, and where they fit within the
 * JavaFX ecosystem, refer to the {@link Action} class documentation.
 * 
 * @see Action
 */
public abstract class AbstractAction implements Action {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private ObservableMap<Object, Object> properties;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a new AbstractAction instance with the given String set as the 
     * {@link #textProperty() text} value.
     *  
     * @param text The string to display in the text property of controls such
     *      as {@link Button#textProperty() Button}.
     */
    public AbstractAction(String text) {
        setText(text);
    }
    
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- text
    private final StringProperty textProperty = new SimpleLocalizedStringProperty(this, "text"); //$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override public StringProperty textProperty() {
        return textProperty;
    }
    
    /**
     * 
     * @return the text of the Action.
     */
    public final String getText() {
        return textProperty.get();
    }

    /**
     * Sets the text of the Action.
     * @param value 
     */
    public final void setText(String value) {
        textProperty.set(value);
    }
    
    
    // --- disabled
    private final BooleanProperty disabledProperty = new SimpleBooleanProperty(this, "disabled"); //$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override public BooleanProperty disabledProperty() {
        return disabledProperty;
    }
    
    /**
     * 
     * @return whether the action is available to the end user,
     * or whether it should appeared 'grayed out'.
     */
    public final boolean isDisabled() {
        return disabledProperty.get();
    }
    
    /**
     * Sets whether the action should be available to the end user,
     * or whether it should appeared 'grayed out'.
     * @param value 
     */
    public final void setDisabled(boolean value) {
        disabledProperty.set(value);
    }

    
    // --- longText
    private final StringProperty longTextProperty = new SimpleLocalizedStringProperty(this, "longText"); //$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override public StringProperty longTextProperty() {
        return longTextProperty;
    }
    
    /**
     * @see #longTextProperty() 
     * @return The longer form of the text to show to the user
     */
    public final String getLongText() {
        return Localization.localize(longTextProperty.get());
    }
    
    /**
     * Sets the longer form of the text to show to the user
     * @param value 
     * @see #longTextProperty() 
     */
    public final void setLongText(String value) {
        longTextProperty.set(value);
    }
    
    
    // --- graphic
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>(this, "graphic"); //$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override public ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }
    
    /**
     * 
     * @return The graphic that should be shown to the user in relation to this action.
     */
    public final Node getGraphic() {
        return graphicProperty.get();
    }
    
    /**
     * Sets the graphic that should be shown to the user in relation to this action.
     * @param value 
     */
    public final void setGraphic(Node value) {
        graphicProperty.set(value);
    }
    
    
    // --- accelerator
    private final ObjectProperty<KeyCombination> acceleratorProperty = new SimpleObjectProperty<KeyCombination>(this, "accelerator"); //$NON-NLS-1$
    
    /** {@inheritDoc} */
    @Override public ObjectProperty<KeyCombination> acceleratorProperty() {
        return acceleratorProperty;
    }
    
    /**
     * 
     * @return The accelerator {@link KeyCombination} that should be used for this action,
     * if it is used in an applicable UI control
     */
    public final KeyCombination getAccelerator() {
        return acceleratorProperty.get();
    }
    
    /**
     * Sets the accelerator {@link KeyCombination} that should be used for this action,
     * if it is used in an applicable UI control
     * @param value 
     */
    public final void setAccelerator(KeyCombination value) {
        acceleratorProperty.set(value);
    }
    
    
    // --- properties
    /** {@inheritDoc} */
    @Override public ObservableMap<Object, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableHashMap();
        }
        return properties;
    }

    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    // --- execute
    /** {@inheritDoc} */
    @Override public abstract void execute(ActionEvent ae);
}