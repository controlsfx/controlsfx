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
package impl.org.controlsfx.skin;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.ActionUtils;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class NotificationPaneSkin extends BehaviorSkinBase<NotificationPane, BehaviorBase<NotificationPane>> {
    
    private NotificationBar notificationBar;
    private Node content;
    
    public NotificationPaneSkin(final NotificationPane control) {
        super(control, new BehaviorBase<>(control));
        
        notificationBar = new NotificationBar(control);
        
        updateContent();
        
        registerChangeListener(control.contentProperty(), "CONTENT");
        registerChangeListener(control.textProperty(), "TEXT");
        registerChangeListener(control.graphicProperty(), "GRAPHIC");
        registerChangeListener(control.showingProperty(), "SHOWING");
        registerChangeListener(control.showFromTopProperty(), "SHOW_FROM_TOP");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("CONTENT".equals(p)) {
            updateContent();
        } else if ("TEXT".equals(p)) {
            notificationBar.label.setText(getSkinnable().getText());
        } else if ("GRAPHIC".equals(p)) {
            notificationBar.label.setGraphic(getSkinnable().getGraphic());
        } else if ("SHOWING".equals(p)) {
            if (getSkinnable().isShowing()) {
                notificationBar.show();
            } else {
                notificationBar.hide();
            }
        } else if ("SHOW_FROM_TOP".equals(p)) {
            if (getSkinnable().isShowing()) {
                getSkinnable().requestLayout();
            }
        }
    }
    
    private void updateContent() {
        if (content != null) {
            getChildren().remove(content);
        }
        
        content = getSkinnable().getContent();
        getChildren().setAll(content, notificationBar);
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        final double notificationBarHeight = notificationBar.prefHeight(w);
        
        if (getSkinnable().isShowFromTop()) {
            // place at top of area
            notificationBar.resizeRelocate(x, y - (1 - notificationBar.transition.get()) * notificationBarHeight, w, notificationBarHeight);
        } else {
            // place at bottom of area
            notificationBar.resizeRelocate(x, h - notificationBarHeight, w, notificationBarHeight);
        }
        
        // layout the content
        if (content != null) {
            content.resizeRelocate(x, y, w, h);
        }
    }
    
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.minWidth(height);
    };
    
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.minHeight(width);
    };
    
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.prefWidth(height);
    };
    
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.prefHeight(width);
    };
    
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.maxWidth(height);
    };
    
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.maxHeight(width);
    };
    
    
    
    
    static class NotificationBar extends Region {
        
        private static final double MIN_HEIGHT = 40;
        
        private final NotificationPane notificationPane;
        
        private final Label label;
        private ButtonBar actionsBar;
        private final Button closeBtn;
        
        private final GridPane pane;

        public NotificationBar(final NotificationPane notificationPane) {
            this.notificationPane = notificationPane;
            
            pane = new GridPane();
            pane.getStyleClass().add("notification-bar");
            pane.setAlignment(Pos.BASELINE_LEFT);
            pane.setVisible(notificationPane.isShowing());
            getChildren().setAll(pane);
            
            // initialise label area
            label = new Label();
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setVgrow(label, Priority.ALWAYS);
            GridPane.setHgrow(label, Priority.ALWAYS);
            
            label.setText(notificationPane.getText());
            label.setGraphic(notificationPane.getGraphic());
            label.opacityProperty().bind(transition);
            
            // initialise actions area
            notificationPane.getActions().addListener(new InvalidationListener() {
                @Override public void invalidated(Observable arg0) {
                    updatePane();
                }
            });
            
            // initialise close button area
            closeBtn = new Button();
            closeBtn.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent arg0) {
                    notificationPane.hide();
                }
            });
            closeBtn.getStyleClass().setAll("close-button");
            StackPane graphic = new StackPane();
            graphic.getStyleClass().setAll("graphic");
            closeBtn.setGraphic(graphic);
            closeBtn.setMinSize(17, 17);
            closeBtn.setPrefSize(17, 17);
            closeBtn.opacityProperty().bind(transition);
            GridPane.setMargin(closeBtn, new Insets(0, 0, 0, 8));
            
            // put it all together
            updatePane();
        }
        
        private void updatePane() {
            actionsBar = ActionUtils.createButtonBar(notificationPane.getActions());
            actionsBar.opacityProperty().bind(transition);
            GridPane.setHgrow(actionsBar, Priority.SOMETIMES);
            
            pane.add(label, 0, 0);
            pane.add(actionsBar, 1, 0);
            pane.add(closeBtn, 2, 0);
        }
        
        @Override protected void layoutChildren() {
            final double w = getWidth();
            final double h = computePrefHeight(-1);
            pane.resize(w, h);
        }
        
        @Override protected double computeMinHeight(double width) {
            return super.computePrefHeight(width);
        }
        
        @Override protected double computePrefHeight(double width) {
            if (notificationPane.isShowFromTop()) {
                return MIN_HEIGHT; 
            } else {
                return Math.max(pane.prefHeight(width), MIN_HEIGHT) * transition.get();
            }
        }
        
        private void show() {
            transitionStartValue = 0;
            doAnimationTransition();
        }
        
        private void hide() {
            transitionStartValue = 1;
            doAnimationTransition();
        }
        
        
        
        // --- animation timeline code
        private final Duration TRANSITION_DURATION = new Duration(350.0);
        private Timeline timeline;
        private double transitionStartValue;
        private void doAnimationTransition() {
            Duration duration;

            if (timeline != null && (timeline.getStatus() != Status.STOPPED)) {
                duration = timeline.getCurrentTime();
                transitionStartValue = transition.get();
                timeline.stop();
            } else {
                duration = TRANSITION_DURATION;
            }

            timeline = new Timeline();
            timeline.setCycleCount(1);

            KeyFrame k1, k2;

            if (notificationPane.isShowing()) {
                k1 = new KeyFrame(
                    Duration.ZERO,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // start expand
                            pane.setCache(true);
                            pane.setVisible(true);
                        }
                    },
                    new KeyValue(transition, transitionStartValue)
                );

                k2 = new KeyFrame(
                    duration,
                        new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // end expand
                            pane.setCache(false);
                        }
                    },
                    new KeyValue(transition, 1, Interpolator.EASE_OUT)

                );
            } else {
                k1 = new KeyFrame(
                    Duration.ZERO,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // Start collapse
                            pane.setCache(true);
                        }
                    },
                    new KeyValue(transition, transitionStartValue)
                );

                k2 = new KeyFrame(
                    duration,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // end collapse
                            pane.setCache(false);
                            pane.setVisible(false);
                        }
                    },
                    new KeyValue(transition, 0, Interpolator.EASE_IN)
                );
            }

            timeline.getKeyFrames().setAll(k1, k2);
            timeline.play();
        }
        
        private DoubleProperty transition = new SimpleDoubleProperty() {
            @Override protected void invalidated() {
                notificationPane.requestLayout();
            }
        };
    }
}
