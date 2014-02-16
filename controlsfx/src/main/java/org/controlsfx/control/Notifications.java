/**
 * Copyright (c) 2014, ControlsFX
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

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.util.Duration;

import org.controlsfx.control.action.Action;

public class Notifications {

    private static final String STYLE_CLASS_DARK = "dark";

    private String title;
    private String text;
    private Node graphic;
    private ObservableList<Action> actions;
    private Pos position;
    private Duration hideAfterDuration;
    private boolean hideCloseButton;
    private EventHandler<ActionEvent> onAction;

    private List<String> styleClass = new ArrayList<>();

    private Notifications() {
        // no-op
    }

    public static Notifications create() {
        return new Notifications();
    }

    public Notifications text(String text) {
        this.text = text;
        return this;
    }

    public Notifications title(String title) {
        this.title = title;
        return this;
    }

    public Notifications graphic(Node graphic) {
        this.graphic = graphic;
        return this;
    }

    public Notifications position(Pos position) {
        this.position = position;
        return this;
    }

    public Notifications hideAfter(Duration duration) {
        this.hideAfterDuration = duration;
        return this;
    }

    public Notifications onAction(EventHandler<ActionEvent> onAction) {
        this.onAction = onAction;
        return this;
    }

    public Notifications darkStyle() {
        styleClass.add(STYLE_CLASS_DARK);
        return this;
    }

    public Notifications hideCloseButton() {
        this.hideCloseButton = true;
        return this;
    }

    public Notifications action(Action... actions) {
        this.actions = actions == null ? FXCollections.<Action>observableArrayList() :
            FXCollections.observableArrayList(actions);
        return this;
    }

    private Notification build() {
        return new Notification(title, text, graphic, position, 
                hideAfterDuration, hideCloseButton, onAction, 
                actions, styleClass);
    }
    
    public void show() {
        build().show();
    }
    
    
    
    static final class Notification {
        private final String title;
        private final String text;
        private final Node graphic;
        private final ObservableList<Action> actions;
        private final Pos position;
        private final Duration hideAfterDuration;
        private final boolean hideCloseButton;
        private final List<String> styleClass;
        private final EventHandler<ActionEvent> onAction;
        
        private Notification(String title, String text, Node graphic, Pos position,
                Duration hideAfterDuration, boolean hideCloseButton, EventHandler<ActionEvent> onAction,
                ObservableList<Action> actions, List<String> styleClass) {
            this.title = title;
            this.text = text;
            this.graphic = graphic;
            this.position = position == null ? Pos.BOTTOM_RIGHT : position;
            this.hideAfterDuration = hideAfterDuration == null ? Duration.seconds(5) : hideAfterDuration;
            this.hideCloseButton = hideCloseButton;
            this.onAction = onAction;
            this.actions = actions == null ? FXCollections.observableArrayList() : actions;
            this.styleClass = styleClass;
        }
        
        public void show() {
            NotificationPopupHandler.getInstance().show(this);
        }
        
        public final String getTitle() {
            return title;
        }
        
        public final String getText() {
            return text;
        }
        
        public final Node getGraphic() {
            return graphic;
        }
        
        public final ObservableList<Action> getActions() {
            return actions;
        }
        
        public final Pos getPosition() {
            return position;
        }
        
        public final Duration getHideAfterDuration() {
            return hideAfterDuration;
        }
        
        public final boolean isHideCloseButton() {
            return hideCloseButton;
        }
        
        public final List<String> getStyleClass() {
            return styleClass;
        }
        
        public final EventHandler<ActionEvent> getOnAction() {
            return onAction;
        }
    }
}