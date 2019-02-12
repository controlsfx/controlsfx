/**
 * Copyright (c) 2014, 2015, 2018 ControlsFX
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

import impl.org.controlsfx.skin.HiddenSidesPaneSkin;
import java.util.Objects;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.util.Duration;

/**
 * A pane used to display a full-size content node and four initially hidden
 * nodes on the four sides. The hidden nodes can be made visible by moving the
 * mouse cursor to the edges (see {@link #setTriggerDistance(double)}) of the
 * pane. The hidden node will appear (at its preferred width or height) with a
 * short slide-in animation. The node will disappear again as soon as the mouse
 * cursor exits it. A hidden node / side can also be pinned by calling
 * {@link #setPinnedSide(Side)}. It will remain visible as long as it stays
 * pinned.
 * 
 * <h3>Screenshot</h3> The following screenshots shows the right side node
 * hovering over a table after it was made visible:
 * 
 * <center><img src="hiddenSidesPane.png" alt="Screenshot of HiddenSidesPane">
 * 
 * </center> <h3>Code Sample</h3>
 * 
 * <pre>
 * HiddenSidesPane pane = new HiddenSidesPane();
 * pane.setContent(new TableView());
 * pane.setRight(new ListView());
 * </pre>
 */
public class HiddenSidesPane extends ControlsFXControl {

    /**
     * Constructs a new pane with the given content node and the four side
     * nodes. Each one of the side nodes may be null.
     * 
     * @param content
     *            the primary node that will fill the entire width and height of
     *            the pane
     * @param top
     *            the hidden node on the top side
     * @param right
     *            the hidden node on the right side
     * @param bottom
     *            the hidden node on the bottom side
     * @param left
     *            the hidden node on the left side
     */
    public HiddenSidesPane(Node content, Node top, Node right, Node bottom,
            Node left) {
        setContent(content);
        setTop(top);
        setRight(right);
        setBottom(bottom);
        setLeft(left);
    }

    /**
     * Constructs a new pane with no content and no side nodes.
     */
    public HiddenSidesPane() {
        this(null, null, null, null, null);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new HiddenSidesPaneSkin(this);
    }

    private DoubleProperty triggerDistance = new SimpleDoubleProperty(this,
            "triggerDistance", 16); //$NON-NLS-1$

    /**
     * The property that stores the distance to the pane's edges that will
     * trigger the appearance of the hidden side nodes.<br>
     * Setting the property to zero or a negative value will disable this
     * functionality, so a hidden side can only be made visible with
     * {@link #setPinnedSide(Side)}.
     * 
     * @return the trigger distance property
     */
    public final DoubleProperty triggerDistanceProperty() {
        return triggerDistance;
    }

    /**
     * Returns the value of the trigger distance property.
     * 
     * @return the trigger distance property value
     */
    public final double getTriggerDistance() {
        return triggerDistance.get();
    }

    /**
     * Set the value of the trigger distance property. <br>
     * Setting the property to zero or a negative value will disable this
     * functionality, so a hidden side can only be made visible with
     * {@link #setPinnedSide(Side)}.
     * 
     * @param distance
     *            the new value for the trigger distance property
     */
    public final void setTriggerDistance(double distance) {
        triggerDistance.set(distance);
    }

    // Content node support.

    private ObjectProperty<Node> content = new SimpleObjectProperty<>(this,
            "content"); //$NON-NLS-1$

    /**
     * The property that is used to store a reference to the content node. The
     * content node will fill the entire width and height of the pane.
     * 
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    /**
     * Returns the value of the content node property.
     * 
     * @return the content node property value
     */
    public final Node getContent() {
        return contentProperty().get();
    }

    /**
     * Sets the value of the content node property.
     * 
     * @param content
     *            the new content node
     */
    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    // Top node support.

    private ObjectProperty<Node> top = new SimpleObjectProperty<>(this,
            "top"); //$NON-NLS-1$

    /**
     * The property used to store a reference to the node shown at the top side
     * of the pane.
     * 
     * @return the hidden node at the top side of the pane
     */
    public final ObjectProperty<Node> topProperty() {
        return top;
    }

    /**
     * Returns the value of the top node property.
     * 
     * @return the top node property value
     */
    public final Node getTop() {
        return topProperty().get();
    }

    /**
     * Sets the value of the top node property.
     * 
     * @param top
     *            the top node value
     */
    public final void setTop(Node top) {
        topProperty().set(top);
    }

    // Right node support.

    /**
     * The property used to store a reference to the node shown at the right
     * side of the pane.
     * 
     * @return the hidden node at the right side of the pane
     */
    private ObjectProperty<Node> right = new SimpleObjectProperty<>(this,
            "right"); //$NON-NLS-1$

    /**
     * Returns the value of the right node property.
     * 
     * @return the right node property value
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    /**
     * Returns the value of the right node property.
     * 
     * @return the right node property value
     */
    public final Node getRight() {
        return rightProperty().get();
    }

    /**
     * Sets the value of the right node property.
     * 
     * @param right
     *            the right node value
     */
    public final void setRight(Node right) {
        rightProperty().set(right);
    }

    // Bottom node support.

    /**
     * The property used to store a reference to the node shown at the bottom
     * side of the pane.
     * 
     * @return the hidden node at the bottom side of the pane
     */
    private ObjectProperty<Node> bottom = new SimpleObjectProperty<>(this,
            "bottom"); //$NON-NLS-1$

    /**
     * Returns the value of the bottom node property.
     * 
     * @return the bottom node property value
     */
    public final ObjectProperty<Node> bottomProperty() {
        return bottom;
    }

    /**
     * Returns the value of the bottom node property.
     * 
     * @return the bottom node property value
     */
    public final Node getBottom() {
        return bottomProperty().get();
    }

    /**
     * Sets the value of the bottom node property.
     * 
     * @param bottom
     *            the bottom node value
     */
    public final void setBottom(Node bottom) {
        bottomProperty().set(bottom);
    }

    // Left node support.

    /**
     * The property used to store a reference to the node shown at the left side
     * of the pane.
     * 
     * @return the hidden node at the left side of the pane
     */
    private ObjectProperty<Node> left = new SimpleObjectProperty<>(this,
            "left"); //$NON-NLS-1$

    /**
     * Returns the value of the left node property.
     * 
     * @return the left node property value
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    /**
     * Returns the value of the left node property.
     * 
     * @return the left node property value
     */
    public final Node getLeft() {
        return leftProperty().get();
    }

    /**
     * Sets the value of the left node property.
     * 
     * @param left
     *            the left node value
     */
    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    // Pinned side support.

    private ObjectProperty<Side> pinnedSide = new SimpleObjectProperty<>(
            this, "pinnedSide"); //$NON-NLS-1$

    /**
     * Returns the pinned side property. The value of this property determines
     * if one of the four hidden sides stays visible all the time.
     * 
     * @return the pinned side property
     */
    public final ObjectProperty<Side> pinnedSideProperty() {
        return pinnedSide;
    }

    /**
     * Returns the value of the pinned side property.
     * 
     * @return the pinned side property value
     */
    public final Side getPinnedSide() {
        return pinnedSideProperty().get();
    }

    /**
     * Sets the value of the pinned side property.
     * 
     * @param side
     *            the new pinned side value
     */
    public final void setPinnedSide(Side side) {
        pinnedSideProperty().set(side);
    }

    // slide in animation delay

    private final ObjectProperty<Duration> animationDelay = new SimpleObjectProperty<>(
            this, "animationDelay", Duration.millis(300)); //$NON-NLS-1$

    /**
     * Returns the animation delay property. The value of this property
     * determines the delay before the hidden side slide in / slide out
     * animation starts to play.
     * 
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDelayProperty() {
        return animationDelay;
    }

    /**
     * Returns the animation delay
     * 
     * @return animation delay
     */
    public final Duration getAnimationDelay() {
        return animationDelay.get();
    }

    /**
     * Set the animation delay
     * 
     * @param duration
     *            slide in animation delay
     */
    public final void setAnimationDelay(Duration duration) {
        animationDelay.set(duration);
    }

    // slide in / slide out duration

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(
            this, "animationDuration", Duration.millis(200)); //$NON-NLS-1$

    /**
     * Returns the animation duration property. The value of this property
     * determines the fade in time for a hidden side to become visible.
     * 
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    /**
     * Returns the animation delay
     * 
     * @return animation delay
     */
    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    /**
     * Set the animation delay
     * 
     * @param duration
     *            animation duration
     */
    public final void setAnimationDuration(Duration duration) {
        animationDuration.set(duration);
    }
    
    /**
     * Shows a specific side
     * 
     * @param side 
     *          the side to show
     */
    public void show(Side side) {
        Objects.requireNonNull(side, "side cannot be null");
        getProperties().put("showPane", side);
    }
    
    /**
     * Hides the currently showing side
     */
    public void hide() {
        getProperties().put("showPane", null);
    }
}
