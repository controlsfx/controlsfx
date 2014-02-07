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
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

/**
 * A master / detail pane is used to display two nodes with a strong
 * relationship to each other. Most of the time the user works with the
 * information displayed in the master node but every once in a while additional
 * information is required and can be made visible via the detail node. By
 * default the detail appear with a short slide in animation and disappear with
 * a slide out. This control allows the detail to be positioned in four
 * different locations (top, bottom, left, or right).
 */
public class MasterDetailPane extends Control {

    /**
     * Constructs a new pane.
     * 
     * @param side
     *            the position where the detail will be shown (top, bottom,
     *            left, right)
     * @param masterNode
     *            the master node (always visible)
     * @param detailNode
     *            the detail node (slides in and out)
     * @param expanded
     *            the initial state (expanded / collapsed)
     */
    public MasterDetailPane(Side side, Node masterNode, Node detailNode,
            boolean expanded) {

        super();

        Objects.requireNonNull(side);
        Objects.requireNonNull(masterNode);
        Objects.requireNonNull(detailNode);

        setDetailPos(side);
        setMasterNode(masterNode);
        setDetailNode(detailNode);
        setShowDetailNode(expanded);

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
     * @param pos
     *            the position where the details will be shown (top, bottom,
     *            left, right)
     * @param expanded
     *            the initial state (expanded / collapsed)
     */
    public MasterDetailPane(Side pos, boolean expanded) {
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
    public MasterDetailPane(Side pos) {
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
    public MasterDetailPane() {
        this(Side.RIGHT, new Placeholder(true), new Placeholder(false), true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MasterDetailPaneSkin(this);
    }

    // Detail postion support

    private final ObjectProperty<Side> detailPos = new SimpleObjectProperty<>(
            this, "detailsPos", Side.RIGHT);

    /**
     * The property used to store the position of the detail node.
     * 
     * @return the details node property
     */
    public final ObjectProperty<Side> detailPosProperty() {
        return detailPos;
    }

    /**
     * Returns the value of the detail node property.
     * 
     * @return the position of the details node (left, right, top, bottom)
     */
    public final Side getDetailPos() {
        return detailPosProperty().get();
    }

    /**
     * Sets the value of the detail node property.
     * 
     * @param side
     *            the position of the detail node (left, right, top, bottom)
     */
    public final void setDetailPos(Side side) {
        Objects.requireNonNull(side);
        detailPosProperty().set(side);
    }

    // Expanded state support

    private final BooleanProperty showDetailNode = new SimpleBooleanProperty(
            this, "showDetailNode", true);

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
     * @param show
     *            if true the pane will show the detail node
     */
    public final void setShowDetailNode(boolean show) {
        showDetailNodeProperty().set(show);
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

    // Detail node support.

    private final ObjectProperty<Node> detailNode = new SimpleObjectProperty<>(
            this, "detailNode");

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
     * @param node
     *            the new master node
     */
    public final void setDetailNode(Node node) {
        Objects.requireNonNull(node);
        detailNodeProperty().set(node);
    }

    // Animation support.

    private final BooleanProperty animated = new SimpleBooleanProperty(this,
            "animated", true);

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
     *         (slide in)
     */
    public final boolean isAnimated() {
        return animatedProperty().get();
    }

    /**
     * Sets the value of the "animated" property.
     * 
     * @param animated
     *            if true the detail node will be shown with a short animation
     *            (slide in)
     */
    public final void setAnimated(boolean animated) {
        animatedProperty().set(animated);
    }

    private DoubleProperty dividerPosition = new SimpleDoubleProperty(this,
            "dividerPosition", .33);

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
     * detail node.
     */
    private static final class Placeholder extends Label {

        public Placeholder(boolean master) {
            super(master ? "Master" : "Detail");

            setAlignment(Pos.CENTER);

            if (master) {
                setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                setStyle("-fx-background-color: red; ; -fx-text-fill: white;");
            }
        }
    }
}
