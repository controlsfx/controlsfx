/**
 * Copyright (c) 2013, 2018 ControlsFX
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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import org.controlsfx.control.HiddenSidesPane;

public class HiddenSidesPaneSkin extends SkinBase<HiddenSidesPane> {

    private static final String SHOW = "showPane";
    
    private final StackPane stackPane;
    private final EventHandler<MouseEvent> exitedHandler;
    private boolean mousePressed;

    public HiddenSidesPaneSkin(HiddenSidesPane pane) {
        super(pane);

        exitedHandler = event -> {
            if (isMouseEnabled() && getSkinnable().getPinnedSide() == null
                    && !mousePressed) {
                hide();
            }
        };

        stackPane = new StackPane();
        getChildren().add(stackPane);
        updateStackPane();

        InvalidationListener rebuildListener = observable -> updateStackPane();
        pane.contentProperty().addListener(rebuildListener);
        pane.topProperty().addListener(rebuildListener);
        pane.rightProperty().addListener(rebuildListener);
        pane.bottomProperty().addListener(rebuildListener);
        pane.leftProperty().addListener(rebuildListener);

        pane.addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (isMouseEnabled() && getSkinnable().getPinnedSide() == null) {
                Side side = getSide(event);
                if (side != null) {
                    show(side);
                } else if (isMouseMovedOutsideSides(event)) {
                    hide();
                }
            }
        });

        pane.addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);

        pane.addEventFilter(MouseEvent.MOUSE_PRESSED,
                event -> mousePressed = true);

        pane.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            mousePressed = false;

            if (isMouseEnabled() && getSkinnable().getPinnedSide() == null) {
                Side side = getSide(event);
                if (side != null) {
                    show(side);
                } else {
                    hide();
                }
            }
        });

        for (Side side : Side.values()) {
            visibility[side.ordinal()] = new SimpleDoubleProperty(0);
            visibility[side.ordinal()].addListener(observable -> getSkinnable()
                    .requestLayout());
        }

        Side pinnedSide = getSkinnable().getPinnedSide();
        if (pinnedSide != null) {
        	show(pinnedSide);
        }

        pane.pinnedSideProperty().addListener(
                observable -> show(getSkinnable().getPinnedSide()));
        
        final ObservableMap<Object, Object> properties = pane.getProperties();
        properties.remove(SHOW);
        properties.addListener(propertiesMapListener);

        Rectangle clip = new Rectangle();
        clip.setX(0);
        clip.setY(0);
        clip.widthProperty().bind(getSkinnable().widthProperty());
        clip.heightProperty().bind(getSkinnable().heightProperty());

        getSkinnable().setClip(clip);
    }
    
    private final MapChangeListener<Object, Object> propertiesMapListener = c -> {
        if (c.wasAdded() && SHOW.equals(c.getKey())) {
            Object value = c.getValueAdded();
            if (value == null) {
                hide();
            }
            else if (value instanceof Side) {
                show((Side)value);
            }
            getSkinnable().getProperties().remove(SHOW);
        }
    };

    private boolean isMouseMovedOutsideSides(MouseEvent event) {
        if (getSkinnable().getLeft() != null
                && getSkinnable().getLeft().getBoundsInParent()
                        .contains(event.getX(), event.getY())) {
            return false;
        }

        if (getSkinnable().getTop() != null
                && getSkinnable().getTop().getBoundsInParent()
                        .contains(event.getX(), event.getY())) {
            return false;
        }

        if (getSkinnable().getRight() != null
                && getSkinnable().getRight().getBoundsInParent()
                        .contains(event.getX(), event.getY())) {
            return false;
        }

        if (getSkinnable().getBottom() != null
                && getSkinnable().getBottom().getBoundsInParent()
                        .contains(event.getX(), event.getY())) {
            return false;
        }

        return true;
    }

    private boolean isMouseEnabled() {
        return getSkinnable().getTriggerDistance() > 0;
    }

    private Side getSide(MouseEvent evt) {
        if (stackPane.getBoundsInLocal().contains(evt.getX(), evt.getY())) {
            double trigger = getSkinnable().getTriggerDistance();
            if (evt.getX() <= trigger) {
                return Side.LEFT;
            } else if (evt.getX() > getSkinnable().getWidth() - trigger) {
                return Side.RIGHT;
            } else if (evt.getY() <= trigger) {
                return Side.TOP;
            } else if (evt.getY() > getSkinnable().getHeight() - trigger) {
                return Side.BOTTOM;
            }
        }

        return null;
    }

    private DoubleProperty[] visibility = new SimpleDoubleProperty[Side
            .values().length];

    private Timeline showTimeline;

    private void show(Side side) {
        if (hideTimeline != null) {
            hideTimeline.stop();
        }

        if (showTimeline != null && showTimeline.getStatus() == Status.RUNNING) {
            return;
        }

        KeyValue[] keyValues = new KeyValue[Side.values().length];
        for (Side s : Side.values()) {
            keyValues[s.ordinal()] = new KeyValue(visibility[s.ordinal()],
                    s.equals(side) ? 1 : 0);
        }

        Duration delay = getSkinnable().getAnimationDelay() != null ? getSkinnable()
                .getAnimationDelay() : Duration.millis(300);
        Duration duration = getSkinnable().getAnimationDuration() != null ? getSkinnable()
                .getAnimationDuration() : Duration.millis(200);

        KeyFrame keyFrame = new KeyFrame(duration, keyValues);
        showTimeline = new Timeline(keyFrame);
        showTimeline.setDelay(delay);
        showTimeline.play();
    }

    private Timeline hideTimeline;

    private void hide() {
        if (showTimeline != null) {
            showTimeline.stop();
        }

        if (hideTimeline != null && hideTimeline.getStatus() == Status.RUNNING) {
            return;
        }

        boolean sideVisible = false;
        for (Side side : Side.values()) {
            if (visibility[side.ordinal()].get() > 0) {
                sideVisible = true;
                break;
            }
        }

        // nothing to do here
        if (!sideVisible) {
            return;
        }

        KeyValue[] keyValues = new KeyValue[Side.values().length];
        for (Side side : Side.values()) {
            keyValues[side.ordinal()] = new KeyValue(
                    visibility[side.ordinal()], 0);
        }

        Duration delay = getSkinnable().getAnimationDelay() != null ? getSkinnable()
                .getAnimationDelay() : Duration.millis(300);
        Duration duration = getSkinnable().getAnimationDuration() != null ? getSkinnable()
                .getAnimationDuration() : Duration.millis(200);

        KeyFrame keyFrame = new KeyFrame(duration, keyValues);
        hideTimeline = new Timeline(keyFrame);
        hideTimeline.setDelay(delay);
        hideTimeline.play();
    }

    private void updateStackPane() {
        stackPane.getChildren().clear();

        if (getSkinnable().getContent() != null) {
            stackPane.getChildren().add(getSkinnable().getContent());
        }
        if (getSkinnable().getTop() != null) {
            stackPane.getChildren().add(getSkinnable().getTop());
            getSkinnable().getTop().setManaged(false);
            getSkinnable().getTop().removeEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
            getSkinnable().getTop().addEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
        }
        if (getSkinnable().getRight() != null) {
            stackPane.getChildren().add(getSkinnable().getRight());
            getSkinnable().getRight().setManaged(false);
            getSkinnable().getRight().removeEventFilter(
                    MouseEvent.MOUSE_EXITED, exitedHandler);
            getSkinnable().getRight().addEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
        }
        if (getSkinnable().getBottom() != null) {
            stackPane.getChildren().add(getSkinnable().getBottom());
            getSkinnable().getBottom().setManaged(false);
            getSkinnable().getBottom().removeEventFilter(
                    MouseEvent.MOUSE_EXITED, exitedHandler);
            getSkinnable().getBottom().addEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
        }
        if (getSkinnable().getLeft() != null) {
            stackPane.getChildren().add(getSkinnable().getLeft());
            getSkinnable().getLeft().setManaged(false);
            getSkinnable().getLeft().removeEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
            getSkinnable().getLeft().addEventFilter(MouseEvent.MOUSE_EXITED,
                    exitedHandler);
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY,
            double contentWidth, double contentHeight) {

        /*
         * Layout the stackpane in a normal way (equals
         * "lay out the content node", the only managed node)
         */
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        // layout the unmanaged side nodes

        Node bottom = getSkinnable().getBottom();
        if (bottom != null) {
            double prefHeight = bottom.prefHeight(-1);
            double offset = prefHeight
                    * visibility[Side.BOTTOM.ordinal()].get();
            bottom.resizeRelocate(contentX, contentY + contentHeight - offset,
                    contentWidth, prefHeight);
            bottom.setVisible(visibility[Side.BOTTOM.ordinal()].get() > 0);
        }

        Node left = getSkinnable().getLeft();
        if (left != null) {
            double prefWidth = left.prefWidth(-1);
            double offset = prefWidth * visibility[Side.LEFT.ordinal()].get();
            left.resizeRelocate(contentX - (prefWidth - offset), contentY,
                    prefWidth, contentHeight);
            left.setVisible(visibility[Side.LEFT.ordinal()].get() > 0);
        }

        Node right = getSkinnable().getRight();
        if (right != null) {
            double prefWidth = right.prefWidth(-1);
            double offset = prefWidth * visibility[Side.RIGHT.ordinal()].get();
            right.resizeRelocate(contentX + contentWidth - offset, contentY,
                    prefWidth, contentHeight);
            right.setVisible(visibility[Side.RIGHT.ordinal()].get() > 0);
        }

        Node top = getSkinnable().getTop();
        if (top != null) {
            double prefHeight = top.prefHeight(-1);
            double offset = prefHeight * visibility[Side.TOP.ordinal()].get();
            top.resizeRelocate(contentX, contentY - (prefHeight - offset),
                    contentWidth, prefHeight);
            top.setVisible(visibility[Side.TOP.ordinal()].get() > 0);
        }
    }
}
