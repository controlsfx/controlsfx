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

import impl.org.controlsfx.skin.HyperlinkLabelSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import com.sun.javafx.event.EventHandlerManager;

public class HyperlinkLabel extends Control implements EventTarget {
    
    private final EventHandlerManager eventHandlerManager =
            new EventHandlerManager(this);
    
    
    public HyperlinkLabel() {
        this(null);
    }
    
    public HyperlinkLabel(String text) {
        setText(text);
    }
    
    @Override protected Skin<?> createDefaultSkin() {
        return new HyperlinkLabelSkin(this);
    }
    
    
    // --- text
    private final StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() { 
        return text; 
    }
    public final String getText() {
        return text.get();
    }
    public final void setText(String value) {
        text.set(value);
    }
    
    
    // --- onAction
    /**
     * The action, which is invoked whenever a hyperlink is fired. This
     * may be due to the user clicking on the hyperlink with the mouse, or by
     * a touch event, or by a key press.
     */
    private ObjectProperty<EventHandler<ActionEvent>> onAction;

    public final void setOnAction(EventHandler<ActionEvent> value) {
        onActionProperty().set( value);
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return onAction == null ? null : onAction.get();
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (onAction == null) {
            onAction = new SimpleObjectProperty<EventHandler<ActionEvent>>(this, "onAction") {
                @Override protected void invalidated() {
                    eventHandlerManager.setEventHandler(ActionEvent.ACTION, get());
                }
            };
        }
        return onAction;
    }
}
