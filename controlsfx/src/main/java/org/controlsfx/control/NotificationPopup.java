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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Duration;

import org.controlsfx.control.action.Action;


public class NotificationPopup {
    
    private static final Map<Pos, List<Popup>> popupsMap = new HashMap<>();
    private static final double padding = 15;
    
    private static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    private static final double screenWidth = screenBounds.getWidth();
    private static final double screenHeight = screenBounds.getHeight();
    
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
        popup.setAutoFix(false);
//        popup.getScene().getRoot().setStyle("-fx-background-color: yellow");
        
//        final Pane pane = new Pane();
//        pane.setStyle("-fx-background-color: yellow");
        
        final NotificationBar notificationBar = new NotificationBar() {
            @Override public String getTitle() {
                return notification.getTitle();
            }
            
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
                return NotificationPopup.this.isShowFromTop(notification.getPosition());
            }
            
            @Override public void hide() {
                isShowing = false;
                doHide();
            }
            
            @Override public boolean isHideCloseButton() {
                return notification.isHideCloseButton();
            }
            
            @Override public double getContainerHeight() {
                return screenHeight;
            }
            
            @Override public void relocateInParent(double x, double y) {
                // this allows for us to slide the notification upwards
                if (! isShowFromTop()) {
                    popup.setAnchorY(y - padding);
                }
            }
        };
        
        popup.getContent().add(notificationBar);
        popup.show(owner, 0, 0);
        
        // determine location for the popup
        double anchorX = 0, anchorY = 0;
        final double barWidth = notificationBar.getWidth();
        final double barHeight = notificationBar.prefHeight(-1);
        
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
        
        popup.setAnchorX(anchorX);
        popup.setAnchorY(anchorY);
        
        isShowing = true;
        notificationBar.doShow();
        
        addPopupToMap(p, popup);
        
        // begin a timeline to get rid of the popup
        KeyValue fadeOutBegin = new KeyValue(notificationBar.opacityProperty(), 1.0);
        KeyValue fadeOutEnd = new KeyValue(notificationBar.opacityProperty(), 0.0);

        KeyFrame kfBegin = new KeyFrame(Duration.ZERO, fadeOutBegin);
        KeyFrame kfEnd = new KeyFrame(Duration.millis(500), fadeOutEnd);

        Timeline timeline = new Timeline(kfBegin, kfEnd);
        timeline.setDelay(notification.getFadeOutDuration());
        timeline.setOnFinished(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                popup.hide();
                removePopupFromMap(p, NotificationPopup.this);
            }
        });
        timeline.play();
    }
    
    private void addPopupToMap(Pos p, Popup popup) {
        List<Popup> popups;
        if (! popupsMap.containsKey(p)) {
            popups = new LinkedList<>();
            popupsMap.put(p, popups);
        } else {
            popups = popupsMap.get(p);
        }
        
        final double newPopupHeight = popup.getContent().get(0).getBoundsInParent().getHeight();
        
        // animate all other popups in the list upwards so that the new one
        // is in the 'new' area
        ParallelTransition parallelTransition = new ParallelTransition();
        for (Popup oldPopup : popups) {
            final double oldAnchorY = oldPopup.getAnchorY();
            Transition t = new Transition() {
                {
                    setCycleDuration(Duration.millis(350));
                }
                
                @Override protected void interpolate(double frac) {
                    final boolean isShowFromTop = isShowFromTop(p);
                    
                    double newAnchorY = oldAnchorY + (isShowFromTop ? 1 : -1) * newPopupHeight * frac;
                    oldPopup.setAnchorY(newAnchorY);
                }
            };
            t.setCycleCount(1);
            parallelTransition.getChildren().add(t);
        }
        parallelTransition.play();
        
        // add the popup to the list so it is kept in memory and can be
        // accessed later on
        popups.add(popup);
    }
    
    private void removePopupFromMap(Pos p, NotificationPopup popup) {
        if (popupsMap.containsKey(p)) {
            List<Popup> popups = popupsMap.get(p);
            popups.remove(popup);
        }
    }
    
    private boolean isShowFromTop(Pos p) {
        switch (p) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT: 
                return true;
            default: 
                return false;
        }
    }
    
    
    
    
    public static class Notification {
        private final String title;
        private final String text;
        private final Node graphic;
        private final ObservableList<Action> actions;
        private final Pos position;
        private final Duration fadeOutDuration;
        private final boolean hideCloseButton;
        
        private Notification(String title, String text, Node graphic, Pos position,
                Duration fadeOutDuration, boolean hideCloseButton, ObservableList<Action> actions) {
            this.title = title;
            this.text = text;
            this.graphic = graphic;
            this.position = position == null ? Pos.BOTTOM_RIGHT : position;
            this.fadeOutDuration = fadeOutDuration == null ? Duration.seconds(5) : fadeOutDuration;
            this.hideCloseButton = hideCloseButton;
            this.actions = actions == null ? FXCollections.observableArrayList() : actions;
        }
        
        public String getTitle() {
            return title;
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
        
        public Duration getFadeOutDuration() {
            return fadeOutDuration;
        }
        
        public boolean isHideCloseButton() {
            return hideCloseButton;
        }
    }
    
    public static class Notifications {
        private String title;
        private String text;
        private Node graphic;
        private ObservableList<Action> actions;
        private Pos position;
        private Duration fadeOutDuration;
        private boolean hideCloseButton;
        
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
        
        public Notifications fadeAfter(Duration duration) {
            this.fadeOutDuration = duration;
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
        
        public Notification build() {
            return new Notification(title, text, graphic, position, fadeOutDuration, hideCloseButton, actions);
        }
    }
}
