/**
 * Copyright (c) 2014 - 2016, ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import impl.org.controlsfx.skin.MasterDetailPaneSkin;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

/**
 * A master / detail pane is used to display two nodes with a strong
 * relationship to each other. Most of the time the user works with the
 * information displayed in the master node but every once in a while additional
 * information is required and can be made visible via the detail node. By
 * default the detail appears with a short slide-in animation and disappears
 * with a slide-out. This control allows the detail node to be positioned in
 * four different locations (top, bottom, left, or right).
 * <h3>Screenshot</h3>
 * To better describe what a master / detail pane is, please refer to the picture
 * below:
 * <center><img src="masterDetailPane.png" alt="Screenshot of MasterDetailPane"></center>
 * <h3>Code Sample</h3>
 * <pre>
 * {@code
 * MasterDetailPane pane = new MasterDetailPane();
 * pane.setMasterNode(new TableView());
 * pane.setDetailNode(new PropertySheet());
 * pane.setDetailSide(Side.RIGHT);
 * pane.setShowDetailNode(true);
 * }</pre>
 */
public class MasterDetailPane extends ControlsFXControl {

    /**
     * Constructs a new pane.
     *
     * @param side       the position where the detail will be shown (top, bottom,
     *                   left, right)
     * @param masterNode the master node (always visible)
     * @param detailNode the detail node (slides in and out)
     * @param showDetail the initial state of the detail node (shown or hidden)
     */
    public MasterDetailPane(Side side, Node masterNode, Node detailNode,
                            boolean showDetail) {

        super();

        Objects.requireNonNull(side);
        Objects.requireNonNull(masterNode);
        Objects.requireNonNull(detailNode);

        getStyleClass().add("master-detail-pane"); //$NON-NLS-1$

        setDetailSide(side);
        setMasterNode(masterNode);
        setDetailNode(detailNode);
        setShowDetailNode(showDetail);

        switch (side) {
            case BOTTOM:
            case RIGHT:
                setDividerPosition(.8);
                break;
            case TOP:
            case LEFT:
                setDividerPosition(.2);
                break;
            default:
                break;

        }
    }

    /**
     * Constructs a new pane with two placeholder nodes.
     *
     * @param pos        the position where the details will be shown (top, bottom,
     *                   left, right)
     * @param showDetail the initial state of the detail node (shown or hidden)
     */
    public MasterDetailPane(Side pos, boolean showDetail) {
        this(pos, new Placeholder(true), new Placeholder(false), showDetail);
    }

    /**
     * Constructs a new pane with two placeholder nodes. The detail node will be
     * shown.
     *
     * @param pos the position where the details will be shown (top, bottom,
     *            left, right)
     */
    public MasterDetailPane(Side pos) {
        this(pos, new Placeholder(true), new Placeholder(false), true);
    }

    /**
     * Constructs a new pane with two placeholder nodes. The detail node will be
     * shown and to the right of the master node.
     */
    public MasterDetailPane() {
        this(Side.RIGHT, new Placeholder(true), new Placeholder(false), true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new MasterDetailPaneSkin(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(MasterDetailPane.class, "masterdetailpane.css");
    }

    /**
     * Resets the divider position to a value that ensures that the detail node will be fully
     * visible at its preferred width or height.
     */
    public final void resetDividerPosition() {

        Node node = getDetailNode();

        if (node == null) {
            return;
        }

        /*
         * Store the current state in order to recreate it once the
         * divider position has been updated.
         */
        boolean wasShowing = isShowDetailNode();
        boolean wasAnimated = isAnimated();

        if (!wasShowing) {
            /*
             * We have to disable animation, otherwise the updates to the scene
             * graph will happen too late.
             */
            setAnimated(false);
            setShowDetailNode(true);

            /*
             * Force CSS pass to ensure that calls to prefWidth/Height will
             * return proper values.
             */
            node.applyCss();
        }

        double dividerSize = getDividerSizeHint();

        double ps;

        switch (getDetailSide()) {
            case LEFT:
            case RIGHT:
                ps = node.prefWidth(-1) + dividerSize;
                break;
            case TOP:
            case BOTTOM:
            default:
                ps = node.prefHeight(-1) + dividerSize;
                break;
        }

        double position = 0;

        switch (getDetailSide()) {
            case LEFT:
                position = ps / getWidth();
                break;
            case RIGHT:
                position = 1 - (ps / getWidth());
                break;
            case TOP:
                position = ps / getHeight();
                break;
            case BOTTOM:
                position = 1 - (ps / getHeight());
                break;
        }

        setDividerPosition(Math.min(1, Math.max(0, position)));

        if (!wasShowing) {
            setShowDetailNode(wasShowing);
            setAnimated(wasAnimated);
        }
    }

    // Divider size support

    private final DoubleProperty dividerSizeHint = new SimpleDoubleProperty(this, "dividerSizeHint", 10) {
        @Override
        public void set(double newValue) {
            super.set(Math.max(0, newValue));
        }
    };

    /**
     * Returns a property that is used to let the master detail pane know how big the divider
     * handles are. This value is needed by the {@link #resetDividerPosition()} method in order to properly
     * calculate the divider location that is needed to fully show the detail node.
     *
     * @return the divider size hint property
     */
    public final DoubleProperty dividerSizeHintProperty() {
        return dividerSizeHint;
    }

    /**
     * Sets the value of {@link #dividerSizeHintProperty()}.
     *
     * @param size the expected divider size (width or height depending on detail's side)
     */
    public final void setDividerSizeHint(double size) {
        dividerSizeHint.set(size);
    }

    /**
     * Returns the value of {@link #dividerSizeHintProperty()}.
     *
     * @return the expected divider size (width or height depending on detail's side)
     */
    public final double getDividerSizeHint() {
        return dividerSizeHint.get();
    }

    // Detail position support

    private final ObjectProperty<Side> detailSide = new SimpleObjectProperty<>(
            this, "detailSide", Side.RIGHT); //$NON-NLS-1$

    /**
     * The property used to store the side where the detail node will be shown.
     *
     * @return the details side property
     */
    public final ObjectProperty<Side> detailSideProperty() {
        return detailSide;
    }

    /**
     * Returns the value of the detail side property.
     *
     * @return the side where the detail node will be shown (left, right, top,
     * bottom)
     */
    public final Side getDetailSide() {
        return detailSideProperty().get();
    }

    /**
     * Sets the value of the detail side property.
     *
     * @param side the side where the detail node will be shown (left, right,
     *             top, bottom)
     */
    public final void setDetailSide(Side side) {
        Objects.requireNonNull(side);
        detailSideProperty().set(side);
    }

    // Show / hide detail node support.

    private final BooleanProperty showDetailNode = new SimpleBooleanProperty(
            this, "showDetailNode", true); //$NON-NLS-1$

    /**
     * The property used to store the visibility of the detail node.
     *
     * @return true if the pane is currently expanded (shows the detail node)
     */
    public final BooleanProperty showDetailNodeProperty() {
        return showDetailNode;
    }

    /**
     * Returns the value of the "show detail node" property.
     *
     * @return true if the pane is currently expanded (shows the detail node)
     */
    public final boolean isShowDetailNode() {
        return showDetailNodeProperty().get();
    }

    /**
     * Sets the value of the "show detail node" property.
     *
     * @param show if true the pane will show the detail node
     */
    public final void setShowDetailNode(boolean show) {
        showDetailNodeProperty().set(show);
    }

    // Master node support.

    private final ObjectProperty<Node> masterNode = new SimpleObjectProperty<>(
            this, "masterNode"); //$NON-NLS-1$

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
     * @param node the new master node
     */
    public final void setMasterNode(Node node) {
        Objects.requireNonNull(node);
        masterNodeProperty().set(node);
    }

    // Detail node support.

    private final ObjectProperty<Node> detailNode = new SimpleObjectProperty<>(
            this, "detailNode"); //$NON-NLS-1$

    /**
     * The property used to store the detail node.
     *
     * @return the detail node property
     */
    public final ObjectProperty<Node> detailNodeProperty() {
        return detailNode;
    }

    /**
     * Returns the value of the detail node property.
     *
     * @return the detail node
     */
    public final Node getDetailNode() {
        return detailNodeProperty().get();
    }

    /**
     * Sets the value of the detail node property.
     *
     * @param node the new master node
     */
    public final void setDetailNode(Node node) {
        detailNodeProperty().set(node);
    }

    // Animation support.

    private final BooleanProperty animated = new SimpleBooleanProperty(this,
            "animated", true); //$NON-NLS-1$

    /**
     * The property used to store the "animated" flag. If true then the detail
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
     * @return true if the detail node will be shown with a short animation
     * (slide in)
     */
    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    /**
     * Sets the value of the "animated" property.
     *
     * @param animated if true the detail node will be shown with a short animation
     *                 (slide in)
     */
    public final void setAnimated(boolean animated) {
        animatedProperty().set(animated);
    }

    private DoubleProperty dividerPosition = new SimpleDoubleProperty(this,
            "dividerPosition", .33); //$NON-NLS-1$

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
     * @param position the new divider position.
     */
    public final void setDividerPosition(double position) {
        /**
         * See https://bitbucket.org/controlsfx/controlsfx/issue/145/divider-position-in-masterdetailpane-is
         *
         * This work-around is not the best ever found but at least it works.
         */
        if (getDividerPosition() == position) {
            dividerPosition.set(-1);
        }
        dividerPosition.set(position);
    }

    /*
     * A placeholder for the constructors that do not accept a master or a
     * detail node.
     */
    private static final class Placeholder extends Label {

        public Placeholder(boolean master) {
            super(master ? "Master" : "Detail"); //$NON-NLS-1$ //$NON-NLS-2$

            setAlignment(Pos.CENTER);

            if (master) {
                setStyle("-fx-background-color: -fx-background;"); //$NON-NLS-1$
            } else {
                setStyle("-fx-background-color: derive(-fx-background, -10%);"); //$NON-NLS-1$
            }
        }
    }
}
