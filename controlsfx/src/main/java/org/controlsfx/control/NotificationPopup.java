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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.Timeline;
import javafx.animation.Transition;
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
                switch (notification.getPosition()) {
                    case BOTTOM_LEFT:
                    case BOTTOM_CENTER:
                    case BOTTOM_RIGHT:
                        popup.setAnchorY(y - padding);
                        break;
                    default: 
                            //no-op
                            break;
                }
            }
        };
        
        notificationBar.getStyleClass().addAll(notification.getStyleClass());
        
        popup.getContent().add(notificationBar);
        popup.show(owner, 0, 0);
        
        // determine location for the popup
        double anchorX = 0, anchorY = 0;
        final double barWidth = notificationBar.getWidth();
        final double barHeight = notificationBar.getHeight();
        
        Pos p = notification.getPosition();
        
        // get anchorX
        switch (p) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                anchorX = padding;
                break;
                
            case TOP_CENTER:
            case CENTER:
            case BOTTOM_CENTER:
                anchorX = screenWidth / 2.0 - barWidth / 2.0 - padding / 2.0;
                break;
                
            default:
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                anchorX = screenWidth - barWidth - padding;
                break;
        }
        
        // get anchorY
        switch (p) {
            case TOP_LEFT:
            case TOP_CENTER:
            case TOP_RIGHT:
                anchorY = padding;
                break;
                
            case CENTER_LEFT:
            case CENTER:
            case CENTER_RIGHT:
                anchorY = screenHeight / 2.0 - barHeight / 2.0 - padding / 2.0;
                break;

            default:
            case BOTTOM_LEFT:
            case BOTTOM_CENTER:
            case BOTTOM_RIGHT:
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
        timeline.setDelay(notification.getHideAfterDuration());
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
    
    
    
    
    public static final class Notification {
        private final String title;
        private final String text;
        private final Node graphic;
        private final ObservableList<Action> actions;
        private final Pos position;
        private final Duration hideAfterDuration;
        private final boolean hideCloseButton;
        private final List<String> styleClass;
        
        private Notification(String title, String text, Node graphic, Pos position,
                Duration hideAfterDuration, boolean hideCloseButton, 
                ObservableList<Action> actions, List<String> styleClass) {
            this.title = title;
            this.text = text;
            this.graphic = graphic;
            this.position = position == null ? Pos.BOTTOM_RIGHT : position;
            this.hideAfterDuration = hideAfterDuration == null ? Duration.seconds(5) : hideAfterDuration;
            this.hideCloseButton = hideCloseButton;
            this.actions = actions == null ? FXCollections.observableArrayList() : actions;
            this.styleClass = styleClass;
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
    }
    
    public static class Notifications {
        
        private static final String STYLE_CLASS_DARK = "dark";
        
        private String title;
        private String text;
        private Node graphic;
        private ObservableList<Action> actions;
        private Pos position;
        private Duration hideAfterDuration;
        private boolean hideCloseButton;
        
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
        
        public Notification build() {
            return new Notification(title, text, graphic, position, hideAfterDuration, hideCloseButton, actions, styleClass);
        }
    }
}
