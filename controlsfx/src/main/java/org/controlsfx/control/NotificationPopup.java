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

import impl.org.controlsfx.skin.NotificationBar;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import org.controlsfx.control.action.Action;

public class NotificationPopup {
    
    private static final List<NotificationPopup> popupsList = new ArrayList<>();
    private static final double padding = 15;
    private static final Duration FADE_OUT_DURATION = Duration.seconds(5);
    
    private Scene ownerScene;
    
    private boolean isShowing = false;

    public void show(Window owner, Notification notification) {
        // need to install our CSS
        if (owner instanceof Stage) {
            ownerScene = ((Stage)owner).getScene();
//        } else if (owner instanceof Scene) {
//            ownerScene = (Scene) owner;
//        } else if (owner instanceof Node) {
//            ownerScene = ((Node)owner).getScene();
        }
        
        ownerScene.getStylesheets().add(getClass().getResource("notificationpopup.css").toExternalForm());
        
        
        final Popup popup = new Popup();
        
        final NotificationBar notificationBar = new NotificationBar() {
            @Override public String getText() {
                return notification.getText();
            }

            @Override public Node getGraphic() {
                return notification.getGraphic();
            }

            @Override public ObservableList<Action> getActions() {
                return notification.getActions();
            }

            @Override public boolean isShowing() {
                return isShowing;
            }
            
            @Override protected double computeMinWidth(double height) {
                return 400;
            }
            
            @Override protected double computeMinHeight(double width) {
                return 100;
            }

            @Override public boolean isShowFromTop() {
                Pos p = notification.getPosition();
                switch (p) {
                    case TOP_LEFT:
                    case TOP_CENTER:
                    case TOP_RIGHT: 
                        return true;
                    default: 
                        return false;
                }
            }
            
            @Override public void hide() {
                isShowing = false;
                doHide();
            }
        };
        
        // determine location for the popup
        double anchorX = 0, anchorY = 0;
        final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        final double screenWidth = screenBounds.getWidth();
        final double screenHeight = screenBounds.getHeight();
        final double barWidth = notificationBar.getWidth();
        final double barHeight = notificationBar.getHeight();
        
        Pos p = notification.getPosition();
        switch (p) {
            default:
            case BOTTOM_RIGHT:
                anchorX = screenWidth - barWidth - padding;
                anchorY = screenHeight - barHeight - padding;
                break;
            case TOP_RIGHT:
                anchorX = screenWidth - barWidth - padding;
                anchorY = padding;
                break;
            case TOP_LEFT:
                anchorX = padding;
                anchorY = padding;
                break;
            case BOTTOM_LEFT:
                anchorX = padding;
                anchorY = screenHeight - barHeight - padding;
                break;
        }
        
        popup.getContent().add(notificationBar);
        popup.show(owner, anchorX, anchorY);
        isShowing = true;
        notificationBar.doShow();
        
        popupsList.add(this);
        
        
        // begin a timeline to get rid of the popup
        KeyValue fadeOutBegin = new KeyValue(notificationBar.opacityProperty(), 1.0);
        KeyValue fadeOutEnd = new KeyValue(notificationBar.opacityProperty(), 0.0);

        KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame kfEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

        Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(FADE_OUT_DURATION);
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                popup.hide();
                popupsList.remove(NotificationPopup.this);
            }
        });
        timeline.play();
    }
    
    public static class Notification {
        private final String text;
        private final Node graphic;
        private final ObservableList<Action> actions;
        private final Pos position;
        
        private Notification(String text, Node graphic, Pos position, ObservableList<Action> actions) {
            this.text = text;
            this.graphic = graphic;
            this.position = position == null ? Pos.BOTTOM_RIGHT : position;
            this.actions = actions == null ? FXCollections.observableArrayList() : actions;
        }
        
        public String getText() {
            return text;
        }
        
        public Node getGraphic() {
            return graphic;
        }
        
        public ObservableList<Action> getActions() {
            return actions;
        }
        
        public Pos getPosition() {
            return position;
        }
    }
    
    public static class Notifications {
        private String text;
        private Node graphic;
        private ObservableList<Action> actions;
        private Pos position;
        
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
        
        public Notifications graphic(Node graphic) {
            this.graphic = graphic;
            return this;
        }
        
        public Notifications position(Pos position) {
            this.position = position;
            return this;
        }
        
        public Notifications action(Action... actions) {
            this.actions = actions == null ? FXCollections.<Action>observableArrayList() :
                                             FXCollections.observableArrayList(actions);
            return this;
        }
        
        public Notification build() {
            return new Notification(text, graphic, position, actions);
        }
    }
}
