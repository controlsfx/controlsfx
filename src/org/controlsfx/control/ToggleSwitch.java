package org.controlsfx.control;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends Region {
    Rectangle clipRect;
    private double preDragPos; 
    private double preDragLayoutX;
    private double preDragClipX;

    final Label offLabel = new Label("OFF");
    final Label onLabel = new Label("ON");
    final StackPane slider = new StackPane();

    Timeline timeline = new Timeline();
    final BooleanProperty toggle = new SimpleBooleanProperty(false);

    public ToggleSwitch() {
        getStyleClass().add("toggle-switch");
        slider.getStyleClass().add("on-off-slider");
        onLabel.getStyleClass().add("on-label");
        offLabel.getStyleClass().add("off-label");

        timeline.setCycleCount(1);

        slider.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                preDragPos = me.getX();
                preDragLayoutX = getLayoutX();
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
                double dragWidth = snapSize(getLayoutX()-preDragLayoutX);
                if (dragWidth == 0) { // mouse click on slider and no drag
                    animate();
                    return;
                }
                double fullWidth = snapSize(offLabel.prefWidth(-1)-(prefWidth(-1)/4)/2);
                if (Math.abs(dragWidth) < fullWidth-fullWidth/2) {
                    // restore position before partial drag
                    clipRect.setX(preDragClipX);
                    setLayoutX(preDragLayoutX);
                    return;
                } 
                // complete drag process to take care if full drag did not happen.
                if (dragWidth < 0) {
                    clipRect.setX(preDragClipX+fullWidth);
                    setLayoutX(preDragLayoutX-fullWidth);
                } else {
                    clipRect.setX(preDragClipX-fullWidth);
                    setLayoutX(preDragLayoutX+fullWidth);
                }
                toggle.set(!toggle.get());
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
        setClip(clipRect);

        getChildren().addAll(offLabel, onLabel, slider);
    }
        
    private void moveDelta(double delta) {
        double min = -snapSize((offLabel.prefWidth(-1)-((prefWidth(-1)/4)/2)));
        double max = 0;
        if (getLayoutX()+delta < min || getLayoutX()+delta > max) return;
        clipRect.setX(clipRect.getX()-delta);
        setLayoutX(getLayoutX()+delta);
    }
        
    private void animate() {
        double movingWidth = snapSize(offLabel.prefWidth(-1)-(prefWidth(-1)/4)/2);
        timeline.getKeyFrames().addAll (
                 new KeyFrame(Duration.ZERO, new KeyValue(clipRect.xProperty(), 0)),
                 new KeyFrame(Duration.ZERO, new KeyValue(layoutXProperty(), 0)),
                 new KeyFrame(new Duration(400), 
                        new KeyValue(clipRect.xProperty(), movingWidth),
                        new KeyValue(layoutXProperty(), -movingWidth))
        );
        timeline.setRate((!toggle.get()) ? 1 : -1);
        timeline.play();
        toggle.set(!toggle.get());
    }
        
    @Override protected double computePrefWidth(double height) {
        return snapSpace(getInsets().getLeft())+2*snapSize(offLabel.prefWidth(height))+
                snapSpace(getInsets().getRight());
    }

    @Override protected double computePrefHeight(double width) {
        return snapSpace(getInsets().getTop())+snapSize(offLabel.prefHeight(width))+
                snapSpace(getInsets().getBottom());
    }
            
    @Override protected void layoutChildren() {
        double x = snapSpace(getInsets().getLeft());
        double y = snapSpace(getInsets().getTop());
        double sliderWidth = snapSize(prefWidth(-1)/4);

        offLabel.resizeRelocate(x, y, snapSize(offLabel.prefWidth(-1)), 
                snapSize(offLabel.prefHeight(-1)));
        onLabel.resizeRelocate(x+snapSize(offLabel.prefWidth(-1)), y, 
                snapSize(offLabel.prefWidth(-1)), snapSize(onLabel.prefHeight(-1)));

        clipRect.setWidth(snapSize(offLabel.prefWidth(-1)+sliderWidth/2));
        clipRect.setHeight(snapSize(this.prefHeight(-1)));
        clipRect.setArcWidth(45);
        clipRect.setArcHeight(45);

        slider.resize(sliderWidth, snapSize(prefHeight(-1)));
        slider.relocate(snapSize(prefWidth(-1))/2-sliderWidth/2, 0);
    }

}