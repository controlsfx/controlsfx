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

import impl.org.controlsfx.behavior.ToggleSwitchBehavior;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.controlsfx.control.ToggleSwitch;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class ToggleSwitchSkin extends BehaviorSkinBase<ToggleSwitch, ToggleSwitchBehavior> {
    private Rectangle clipRect;
    private double preDragPos; 
    private double preDragLayoutX;
    private double preDragClipX;

    private final Label offLabel = new Label("Off");
    private final Label onLabel = new Label("On");
    private final StackPane slider = new StackPane();

    private Timeline timeline = new Timeline();

    public ToggleSwitchSkin(final ToggleSwitch control) {
        super(control, new ToggleSwitchBehavior(control));
        
        slider.getStyleClass().add("on-off-slider");
        onLabel.getStyleClass().add("on-label");
        offLabel.getStyleClass().add("off-label");

        timeline.setCycleCount(1);

        slider.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                preDragPos = me.getX();
                preDragLayoutX = control.getLayoutX();
                preDragClipX = clipRect.getX();
            }
        });

        slider.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                double delta =  me.getX() - preDragPos;
                moveDelta(delta);
            }
        });
            
        slider.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent t) {
                double dragWidth = snapSize(control.getLayoutX()-preDragLayoutX);
                if (dragWidth == 0) { // mouse click on slider and no drag
                    animate();
                    return;
                }
                
////                double fullWidth = snapSize(offLabel.prefWidth(-1)-(control.prefWidth(-1)/4)/2);
                double fullWidth = getSkinnable().getWidth();
                
                
                if (Math.abs(dragWidth) < fullWidth-fullWidth/2) {
                    // restore position before partial drag
                    clipRect.setX(preDragClipX);
                    control.setLayoutX(preDragLayoutX);
                    return;
                } 
                // complete drag process to take care if full drag did not happen.
                if (dragWidth < 0) {
                    clipRect.setX(preDragClipX+fullWidth);
                    control.setLayoutX(preDragLayoutX-fullWidth);
                } else {
                    clipRect.setX(preDragClipX-fullWidth);
                    control.setLayoutX(preDragLayoutX+fullWidth);
                }
                control.setSelected(!control.isSelected());
            }
        });
            
        EventHandler<MouseEvent> actionHander = new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent t) {
                animate(); // animate on / off 
            }
        };

        offLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, actionHander);
        onLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, actionHander);
        clipRect = new Rectangle();
        control.setClip(clipRect);

        getChildren().addAll(offLabel, onLabel, slider);
    }
        
    private void moveDelta(double delta) {
//        double min = -snapSize((offLabel.prefWidth(-1)-((getSkinnable().prefWidth(-1)/4)/2)));
        final double buttonWidth = getButtonWidth(getSkinnable().getHeight()) - offLabel.getPadding().getLeft() - 2;
        double min = -buttonWidth;
        double max = 0;
        final double layoutX = getSkinnable().getLayoutX();
        final double newLayoutX = layoutX + delta;
        if (newLayoutX < min || newLayoutX > max) return;
        clipRect.setX(clipRect.getX() - delta);
        getSkinnable().setLayoutX(newLayoutX);
    }
        
    private void animate() {
//        double movingWidth = snapSize(offLabel.prefWidth(-1) + offLabel.getPadding().getLeft() -(getSkinnable().prefWidth(-1)/4)/2);
        final double movingWidth = getButtonWidth(getSkinnable().getHeight()) - offLabel.getPadding().getLeft() - 2;
        timeline.getKeyFrames().addAll (
                 new KeyFrame(Duration.ZERO, new KeyValue(clipRect.xProperty(), 0)),
                 new KeyFrame(Duration.ZERO, new KeyValue(getSkinnable().layoutXProperty(), 0)),
                 new KeyFrame(new Duration(400), 
                        new KeyValue(clipRect.xProperty(), movingWidth),
                        new KeyValue(getSkinnable().layoutXProperty(), -movingWidth))
        );
        timeline.setRate((!getSkinnable().isSelected()) ? 1 : -1);
        timeline.play();
        getSkinnable().setSelected(! getSkinnable().isSelected());
    }
    
    private double getButtonWidth(double h) {
        final double buttonWidth = Math.max(snapSize(offLabel.prefWidth(h)), snapSize(onLabel.prefWidth(h)));
        return buttonWidth;
    }
    
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final double buttonWidth = getButtonWidth(height);
        return leftInset + snapSize(buttonWidth) + rightInset + 1;
    }
    
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + snapSize(offLabel.prefHeight(width)) + bottomInset;
    }
    
    @Override protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
//        double sliderWidth = snapSize(getSkinnable().prefWidth(-1)/4);
        final double thumbSize = Math.max(contentWidth / 4, contentHeight);
        final Insets offLabelPadding = offLabel.getPadding();
        
        final double buttonWidth = getButtonWidth(contentHeight);

        offLabel.resizeRelocate(contentX + 1, contentY, buttonWidth, contentHeight);
        onLabel.resizeRelocate(contentX + 1 + buttonWidth, contentY, buttonWidth, contentHeight);

        clipRect.setWidth(contentWidth/2.0 + thumbSize/2.0 + offLabelPadding.getLeft());
        clipRect.setHeight(contentHeight);
        clipRect.setArcWidth(20);
        clipRect.setArcHeight(20);

        slider.resize(thumbSize, thumbSize);
        slider.relocate(contentWidth/2.0-thumbSize/2.0 + offLabelPadding.getLeft(), contentY);
    }

}