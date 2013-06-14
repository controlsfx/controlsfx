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

import impl.org.controlsfx.skin.NotificationPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import org.controlsfx.control.action.Action;

/**
 * 
 */
public class NotificationPane extends Control {
    
    public static final String STYLE_CLASS_DARK = "dark";
    
    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * 
     */
    public NotificationPane() {
        getStyleClass().add("notification-pane");
    }
    
    
    
    /***************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new NotificationPaneSkin(this);
    }
    
    /** {@inheritDoc} */
    @Override protected String getUserAgentStylesheet() {
        return NotificationPane.class.getResource("notificationpane.css").toExternalForm();
    }
    
    
    
    /***************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- content
    private ObjectProperty<Node> content = new SimpleObjectProperty<Node>(this, "content");
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }
    public final void setContent(Node value) {
        this.content.set(value); 
    }
    public final Node getContent() {
        return content.get();
    }
    
    
    // --- text
    private StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
        return text;
    }
    public final void setText(String value) {
        this.text.set(value); 
    }
    public final String getText() {
        return text.get();
    }
    
    
    // --- graphic
    private ObjectProperty<Node> graphic = new SimpleObjectProperty<Node>(this, "graphic");
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }
    public final void setGraphic(Node value) {
        this.graphic.set(value); 
    }
    public final Node getGraphic() {
        return graphic.get();
    }
    
    
    // --- showing
    private BooleanProperty showing = new SimpleBooleanProperty(this, "showing");
    public final BooleanProperty showingProperty() {
        return showing;
    }
    private final void setShowing(boolean value) {
        this.showing.set(value); 
    }
    public final boolean isShowing() {
        return showing.get();
    }
    
    
    // --- show from top
    private BooleanProperty showFromTop = new SimpleBooleanProperty(this, "showFromTop", true);
    public final BooleanProperty showFromTopProperty() {
        return showFromTop;
    }
    public final void setShowFromTop(boolean value) {
        this.showFromTop.set(value); 
    }
    public final boolean isShowFromTop() {
        return showFromTop.get();
    }
    
    
    
    /***************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    /**
     * Observable list of actions used for the actions area of the notification 
     * bar. Modifying the contents of this list will change the actions available to
     * the user.
     * @return The {@link ObservableList} of actions available to the user.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    public void show() {
        setShowing(true);
    }
    
    public void hide() {
        setShowing(false);
    }
}
