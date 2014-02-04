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

import impl.org.controlsfx.skin.MasterDetailsPaneSkin;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.util.Duration;

/**
 * A master / details pane is used to display two nodes with a strong
 * relationship to each other. Most of the time the user works with the
 * information displayed in the master node but every once in a while additional
 * information is required and can be made visible via the details node. By
 * default the details appear with a short slide in animation and disappear with
 * a slide out. This control allows the details to be positioned in four
 * different locations (top, bottom, left, or right).
 */
public class MasterDetailsPane extends Control {

    public enum DetailsPos {
        LEFT, TOP, RIGHT, BOTTOM;
    }

    /**
     * Constructs a new pane.
     * 
     * @param pos
     *            the position where the details will be shown (top, bottom,
     *            left, right)
     * @param masterNode
     *            the master node (always visible)
     * @param detailsNode
     *            the details node (slides in and out)
     * @param expanded
     *            the initial state (expanded / collapsed)
     */
    public MasterDetailsPane(DetailsPos pos, Node masterNode, Node detailsNode,
            boolean expanded) {

        super();

        Objects.requireNonNull(pos);
        Objects.requireNonNull(masterNode);
        Objects.requireNonNull(detailsNode);

        setDetailsPos(pos);
        setMasterNode(masterNode);
        setDetailsNode(detailsNode);
        setExpanded(expanded);
    }

    /**
     * Constructs a new pane with two placeholder nodes.
     * 
     * @param pos
     *            the position where the details will be shown (top, bottom,
     *            left, right)
     * @param expanded
     *            the initial state (expanded / collapsed)
     */
    public MasterDetailsPane(DetailsPos pos, boolean expanded) {
        this(pos, new Placeholder(true), new Placeholder(false), expanded);
    }

    /**
     * Constructs a new pane with two placeholder nodes. The initial state is
     * expanded.
     * 
     * @param pos
     *            the position where the details will be shown (top, bottom,
     *            left, right)
     */
    public MasterDetailsPane(DetailsPos pos) {
        this(pos, new Placeholder(true), new Placeholder(false), true);
    }

    /**
     * Constructs a new pane with two placeholder nodes. The initial state is
     * expanded and the details will be shown to the right of the master node.
     * 
     * @param pos
     *            the position where the details will be shown (top, bottom,
     *            left, right)
     */
    public MasterDetailsPane() {
        this(DetailsPos.RIGHT, new Placeholder(true), new Placeholder(false),
                true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MasterDetailsPaneSkin(this);
    }

    // Details postion support

    private final ObjectProperty<DetailsPos> detailsPos = new SimpleObjectProperty<>(
            this, "detailsPos", DetailsPos.RIGHT);

    /**
     * The property used to store the position of the details node.
     * 
     * @return the details node property
     */
    public final ObjectProperty<DetailsPos> detailsPosProperty() {
        return detailsPos;
    }

    /**
     * Returns the value of the details node property.
     * 
     * @return the position of the details node (left, right, top, bottom)
     */
    public final DetailsPos getDetailsPos() {
        return detailsPosProperty().get();
    }

    /**
     * Sets the value of the details node property.
     * 
     * @param pos
     *            the position of the details node (left, right, top, bottom)
     */
    public final void setDetailsPos(DetailsPos pos) {
        Objects.requireNonNull(pos);
        detailsPosProperty().set(pos);
    }

    // Expanded state support

    private final BooleanProperty expanded = new SimpleBooleanProperty(this,
            "expanded", true);

    /**
     * The property used to store the expansion state of the pane.
     * 
     * @return true if the pane is currently expanded (shows the details node)
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    /**
     * Returns the value of the expanded property.
     * 
     * @return true if the pane is currently expanded (shows the details node)
     */
    public final boolean isExpanded() {
        return expandedProperty().get();
    }

    /**
     * Sets the value of the expanded property.
     * 
     * @param expanded
     *            if true the pane will show the details node
     */
    public final void setExpanded(boolean expanded) {
        expandedProperty().set(expanded);
    }

    // Master node support.

    private final ObjectProperty<Node> masterNode = new SimpleObjectProperty<>(
            this, "masterNode");

    /**
     * The property used to store the master node.
     * 
     * @return the master node property
     */
    public final ObjectProperty<Node> masterNodeProperty() {
        return masterNode;
    }

    /**
     * Returns the value of the master node property.
     * 
     * @return the master node
     */
    public final Node getMasterNode() {
        return masterNodeProperty().get();
    }

    /**
     * Sets the value of the master node property.
     * 
     * @param node
     *            the new master node
     */
    public final void setMasterNode(Node node) {
        Objects.requireNonNull(node);
        masterNodeProperty().set(node);
    }

    // Details node support.

    private final ObjectProperty<Node> detailsNode = new SimpleObjectProperty<>(
            this, "detailsNode");

    /**
     * The property used to store the details node.
     * 
     * @return the details node property
     */
    public final ObjectProperty<Node> detailsNodeProperty() {
        return detailsNode;
    }

    /**
     * Returns the value of the details node property.
     * 
     * @return the details node
     */
    public final Node getDetailsNode() {
        return detailsNodeProperty().get();
    }

    /**
     * Sets the value of the details node property.
     * 
     * @param node
     *            the new master node
     */
    public final void setDetailsNode(Node node) {
        Objects.requireNonNull(node);
        detailsNodeProperty().set(node);
    }

    // Animation support.

    private final BooleanProperty animated = new SimpleBooleanProperty(this,
            "animated", true);

    /**
     * The property used to store the "animated" flag. If true then the details
     * node will be shown / hidden with a short slide in / out animation.
     * 
     * @return the "animated" property
     */
    public final BooleanProperty animatedProperty() {
        return animated;
    }

    /**
     * Returns the value of the "animated" property.
     * 
     * @return true if the details node will be shown with a short animation
     *         (slide in)
     */
    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    /**
     * Sets the value of the "animated" property.
     * 
     * @param animated
     *            if true the details node will be shown with a short animation
     *            (slide in)
     */
    public final void setAnimated(boolean animated) {
        animatedProperty().set(animated);
    }

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(
            this, "animationDuration", Duration.seconds(.1));

    /**
     * The property used to store the duration of the slide in / out animation
     * when the details node becomes visible / hidden.
     * 
     * @return the animation duration property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    /**
     * Sets the duration of the animation used to show / hide the details node.
     * 
     * @param duration
     *            the animation duration
     */
    public final void setAnimationDuration(Duration duration) {
        Objects.requireNonNull(duration);
        animationDurationProperty().set(duration);
    }

    /**
     * Returns the duration of the animation used to show / hide the details
     * node.
     * 
     * @return the animation duration
     */
    public final Duration getAnimationDuration() {
        return animationDurationProperty().get();
    }

    private DoubleProperty dividerPosition = new SimpleDoubleProperty(this,
            "dividerPosition", -1);

    /**
     * Stores the location of the divider.
     * 
     * @return the divider location
     */
    public final DoubleProperty dividerPositionProperty() {
        return dividerPosition;
    }

    /**
     * Returns the value of the divider position property.
     * 
     * @return the position of the divider
     */
    public final double getDividerPosition() {
        return dividerPosition.get();
    }

    /**
     * Sets the value of the divider position property.
     * 
     * @param position
     *            the new divider position.
     */
    public final void setDividerPosition(double position) {
        dividerPosition.set(position);
    }

    /*
     * A placeholder for the constructors that do not accept a master or a
     * details node.
     */
    private static final class Placeholder extends Label {

        public Placeholder(boolean master) {
            super(master ? "Master" : "Details");

            setAlignment(Pos.CENTER);

            if (master) {
                setStyle("-fx-background-color: green;");
                setPrefSize(400, 400);
            } else {
                setStyle("-fx-background-color: red;");
                setPrefSize(100, 100);
            }
        }
    }
}
