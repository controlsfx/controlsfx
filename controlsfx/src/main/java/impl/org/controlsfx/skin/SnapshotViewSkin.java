/**
 * Copyright (c) 2014, 2021, ControlsFX
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

import impl.org.controlsfx.tools.rectangle.CoordinatePosition;
import impl.org.controlsfx.tools.rectangle.CoordinatePositions;
import impl.org.controlsfx.tools.rectangle.Rectangles2D;
import impl.org.controlsfx.tools.rectangle.change.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import org.controlsfx.control.SnapshotView;
import org.controlsfx.control.SnapshotView.Boundary;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * View for the {@link SnapshotView}. It displays the node and the selection and manages their positioning.
 * Mouse events are used to change the selection.
 */
public class SnapshotViewSkin extends SkinBase<SnapshotView> {

    /**
     * The percentage of the control's node's width/height used as a tolerance for determining whether the cursor is on
     * an edge of the selection.
     */
    private static final double RELATIVE_EDGE_TOLERANCE = 0.015;

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * The currently displayed node; when the {@link SnapshotView#nodeProperty() node} property changes
     * {@link #updateNode() updateNode} will set the new one.
     */
    private Node node;

    /**
     * The pane displaying the {@link #node}.
     */
    private final GridPane gridPane;

    /**
     * The (mutable) rectangle which represents the selected area.
     */
    private final Rectangle selectedArea;

    /**
     * The rectangle whose stroke represents the unselected area. Binding is used to ensure that the rectangle itself
     * always has the same size and position as the {@link #selectedArea}.
     */
    private final Rectangle unselectedArea;

    /**
     * The node capturing mouse events.
     */
    private final Node mouseNode;

    /**
     * The current selection change; might be {@code null}.
     */
    private SelectionChange selectionChange;

    /**
     * A function which sets the {@link SnapshotView#selectionChangingProperty() selectionChanging} property to the
     * given value.
     */
    private final Consumer<Boolean> setSelectionChanging;

    /* ************************************************************************
     *                                                                         *
     * Constructor & Initialization                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new skin for the specified {@link SnapshotView}.
     * 
     * @param snapshotView
     *            the {@link SnapshotView} this skin will display
     */
    public SnapshotViewSkin(SnapshotView snapshotView) {

        super(snapshotView);

        this.gridPane = createGridPane();
        this.selectedArea = new Rectangle();
        this.unselectedArea = new Rectangle();
        this.mouseNode = createMouseNode();
        this.setSelectionChanging = createSetSelectionChanging();

        buildSceneGraph();
        initializeAreas();

        registerChangeListener(snapshotView.nodeProperty(), e -> updateNode());
        registerChangeListener(snapshotView.selectionProperty(), e -> updateSelection());
    }

    /**
     * Creates the grid pane which will contain the node.
     * 
     * @return a {@link GridPane}
     */
    private static GridPane createGridPane() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    /**
     * Creates the node which will be used to capture mouse events. Events are handed over to
     * {@link #handleMouseEvent(MouseEvent) handleMouseEvent}.
     * 
     * @return a {@link Node}
     */
    private Node createMouseNode() {
        Rectangle mouseNode = new Rectangle();

        // make the node transparent and make sure its size does not affect the control's size
        mouseNode.setFill(Color.TRANSPARENT);
        mouseNode.setManaged(false);

        // bind width and height to the control
        mouseNode.widthProperty().bind(getSkinnable().widthProperty());
        mouseNode.heightProperty().bind(getSkinnable().heightProperty());

        // let it handle the mouse events if allowed by the user
        mouseNode.addEventHandler(MouseEvent.ANY, this::handleMouseEvent);
        mouseNode.mouseTransparentProperty().bind(getSkinnable().selectionMouseTransparentProperty());

        return mouseNode;
    }

    /**
     * Creates a function which sets the applied boolean to {@link SnapshotView#selectionChangingProperty()}.
     *
     * @return a Boolean {@link Consumer}
     */
    private Consumer<Boolean> createSetSelectionChanging() {
        return changing -> getNode().getProperties().put(SnapshotView.SELECTION_CHANGING_PROPERTY_KEY, changing);
    }

    /**
     * Builds this skin's scene graph.
     */
    private void buildSceneGraph() {
        getChildren().addAll(gridPane, unselectedArea, selectedArea, mouseNode);
        updateNode();
    }

    /**
     * Initializes the {@link #selectedArea} and the {@link #unselectedArea}. This includes their style and their
     * bindings to the {@link SnapshotView#selectionProperty() selection} property.
     */
    private void initializeAreas() {
        styleAreas();
        bindAreaCoordinatesTogether();
        bindAreaVisibilityToSelection();
    }

    /**
     * Styles the selected and unselected area.
     */
    private void styleAreas() {
        selectedArea.fillProperty().bind(getSkinnable().selectionAreaFillProperty());
        selectedArea.strokeProperty().bind(getSkinnable().selectionBorderPaintProperty());
        selectedArea.strokeWidthProperty().bind(getSkinnable().selectionBorderWidthProperty());
        selectedArea.setStrokeType(StrokeType.OUTSIDE);
        // if the control's layout depends on this rectangle,
        // the stroke's width messes up the layout if the selection is on the pane's edge
        selectedArea.setManaged(false);
        selectedArea.setMouseTransparent(true);

        unselectedArea.setFill(Color.TRANSPARENT);
        unselectedArea.strokeProperty().bind(getSkinnable().unselectedAreaFillProperty());
        unselectedArea.strokeWidthProperty().bind(
                Bindings.max(getSkinnable().widthProperty(), getSkinnable().heightProperty()));
        unselectedArea.setStrokeType(StrokeType.OUTSIDE);
        // this call is crucial! it prevents the enormous unselected area from messing up the layout
        unselectedArea.setManaged(false);
        unselectedArea.setMouseTransparent(true);
    }

    /**
     * Binds the position and size of {@link #unselectedArea} to {@link #selectedArea}.
     */
    private void bindAreaCoordinatesTogether() {
        unselectedArea.xProperty().bind(selectedArea.xProperty());
        unselectedArea.yProperty().bind(selectedArea.yProperty());
        unselectedArea.widthProperty().bind(selectedArea.widthProperty());
        unselectedArea.heightProperty().bind(selectedArea.heightProperty());
    }

    /**
     * Binds the visibility of {@link #selectedArea} and {@link #unselectedArea} to the {@code SnapshotView} 's
     * {@link SnapshotView#selectionActiveProperty() selectionActive} and {@link SnapshotView#hasSelectionProperty()
     * selectionValid} properties.
     */
    @SuppressWarnings("unused")
    private void bindAreaVisibilityToSelection() {
        ReadOnlyBooleanProperty selectionExists = getSkinnable().hasSelectionProperty();
        ReadOnlyBooleanProperty selectionActive = getSkinnable().selectionActiveProperty();
        BooleanBinding existsAndActive = Bindings.and(selectionExists, selectionActive);

        selectedArea.visibleProperty().bind(existsAndActive);
        unselectedArea.visibleProperty().bind(existsAndActive);

        // UGLY WORKAROUND AHEAD!
        // The clipper should be created in 'styleAreas' but due to the problem explained in 'Clipper.setClip(Node)'
        // it has to be created here where the visibility is determined.

        // clip the unselected area according to the view's property - this is done by a designated inner class
        new Clipper(getSkinnable(), unselectedArea, () -> unselectedArea.visibleProperty().bind(existsAndActive));
    }

    /* ************************************************************************
     *                                                                         *
     * Node                                                                    *
     *                                                                         *
     **************************************************************************/

    /**
     * Displays the current {@link SnapshotView#nodeProperty() node}.
     */
    private void updateNode() {
        if (node != null) {
            gridPane.getChildren().remove(node);
        }

        node = getSkinnable().getNode();
        if (node != null) {
            gridPane.getChildren().add(0, node);
        }
    }

    /* ************************************************************************
     *                                                                         *
     * Selection                                                               *
     *                                                                         *
     **************************************************************************/

    /**
     * Updates the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}) to a
     * changed selection.
     */
    private void updateSelection() {
        boolean showSelection = getSkinnable().hasSelection() && getSkinnable().isSelectionActive();

        if (showSelection) {
            // the selection can be properly displayed
            Rectangle2D selection = getSkinnable().getSelection();
            setSelection(selection.getMinX(), selection.getMinY(), selection.getWidth(), selection.getHeight());
        } else {
            // in this case the selection areas are invisible,
            // so the only thing left to do is to make sure their coordinates are not all over the place
            // (this is not strictly necessary but makes the skin's state cleaner)
            setSelection(0, 0, 0, 0);
        }
    }

    /**
     * Updates the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}) to the
     * specified arguments.
     * 
     * @param x
     *            the new x coordinate of the upper left corner
     * @param y
     *            the new y coordinate of the upper left corner
     * @param width
     *            the new width
     * @param height
     *            the new height
     */
    private void setSelection(double x, double y, double width, double height) {
        selectedArea.setX(x);
        selectedArea.setY(y);
        selectedArea.setWidth(width);
        selectedArea.setHeight(height);
    }

    /* ************************************************************************
     *                                                                         *
     * Mouse Events                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Handles mouse events.
     * 
     * @param event
     *            the {@link MouseEvent} to handle
     */
    private void handleMouseEvent(MouseEvent event) {
        Cursor newCursor = handleMouseEventImpl(event);
        mouseNode.setCursor(newCursor);
    }

    /* ************************************************************************
     *                                                                         *
     * Events                                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Handles the specified mouse event (possibly by creating/changing/removing a selection) and returns the matching
     * cursor.
     *
     * @param mouseEvent
     *            the handled {@link MouseEvent}; must not be {@code null}
     * @return the cursor which will be used for this event
     */
    private Cursor handleMouseEventImpl(MouseEvent mouseEvent) {
        Objects.requireNonNull(mouseEvent, "The argument 'mouseEvent' must not be null."); //$NON-NLS-1$

        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        SnapshotViewSkin.SelectionEvent selectionEvent = createSelectionEvent(mouseEvent);

        if (eventType == MouseEvent.MOUSE_MOVED) {
            return getCursor(selectionEvent);
        }
        if (eventType == MouseEvent.MOUSE_PRESSED) {
            return handleMousePressedEvent(selectionEvent);
        }
        if (eventType == MouseEvent.MOUSE_DRAGGED) {
            return handleMouseDraggedEvent(selectionEvent);
        }
        if (eventType == MouseEvent.MOUSE_RELEASED) {
            return handleMouseReleasedEvent(selectionEvent);
        }

        return Cursor.DEFAULT;
    }

    // TRANSFORM MOUSE EVENT TO SELECTION EVENT

    /**
     * Creates a selection event for the specified mouse event
     *
     * @param mouseEvent
     *            the {@link MouseEvent} for which the selection event will be created
     * @return the {@link SnapshotViewSkin.SelectionEvent} for the specified mouse event
     */
    private SnapshotViewSkin.SelectionEvent createSelectionEvent(MouseEvent mouseEvent) {
        Point2D point = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        Rectangle2D selectionBounds = createBoundsForCurrentBoundary();
        CoordinatePosition position = computePosition(point);
        return new SnapshotViewSkin.SelectionEvent(mouseEvent, point, selectionBounds, position);
    }

    /**
     * Returns the bounds according to the current {@link SnapshotView#selectionAreaBoundaryProperty()
     * selectionAreaBoundary}.
     *
     * @return the bounds as a {@link Rectangle2D}
     */
    private Rectangle2D createBoundsForCurrentBoundary() {
        Boundary boundary = getSkinnable().getSelectionAreaBoundary();
        switch (boundary) {
            case CONTROL:
                return new Rectangle2D(0, 0, getControlWidth(), getControlHeight());
            case NODE:
                boolean nodeExists = getSnapshotNode() != null;
                if (nodeExists) {
                    Bounds nodeBounds = getSnapshotNode().getBoundsInParent();
                    return Rectangles2D.fromBounds(nodeBounds);
                } else {
                    return Rectangle2D.EMPTY;
                }
            default:
                throw new IllegalArgumentException("The boundary " + boundary + " is not fully implemented."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Returns the position of the specified point relative to a possible selection.
     *
     * @param point
     *            the point (in the node's preferred coordinates) whose position will be computed
     *
     * @return the {@link CoordinatePosition} the event's point has relative to the control's current selection; if the
     *         selection is inactive this always returns {@link CoordinatePosition#OUT_OF_RECTANGLE}.
     */
    private CoordinatePosition computePosition(Point2D point) {
        boolean noSelection = !getSkinnable().hasSelection() || !getSkinnable().isSelectionActive();
        boolean controlHasNoSpace = getControlWidth() == 0 || getControlHeight() == 0;
        if (noSelection || controlHasNoSpace) {
            return CoordinatePosition.OUT_OF_RECTANGLE;
        }

        double tolerance = computeTolerance();
        return computePosition(getSelection(), point, tolerance);
    }

    /**
     * Computes the tolerance which is used to determine whether the cursor is on an edge.
     *
     * @return the absolute tolerance
     */
    private double computeTolerance() {
        double controlMeanLength = Math.sqrt(getControlWidth() * getControlHeight());
        return RELATIVE_EDGE_TOLERANCE * controlMeanLength;
    }

    /**
     * Returns the position of the specified point relative to the specified selection with the specified tolerance.
     *
     * @param selection
     *            the selection relative to which the point's position will be computed; as a {@link Rectangle2D}
     * @param point
     *            the {@link Point2D} whose position will be computed
     * @param tolerance
     *            the absolute tolerance used to determine whether the point is on an edge
     *
     * @return the {@link CoordinatePosition} the event's point has relative to the control's current selection; if the
     *         selection is inactive this always returns {@link CoordinatePosition#OUT_OF_RECTANGLE}.
     */
    private static CoordinatePosition computePosition(Rectangle2D selection, Point2D point, double tolerance) {
        CoordinatePosition onEdge = CoordinatePositions.onEdges(selection, point, tolerance);
        if (onEdge != null) {
            return onEdge;
        } else {
            return CoordinatePositions.inRectangle(selection, point);
        }
    }

    // HANDLE SELECTION EVENTS

    /**
     * Handles {@link MouseEvent#MOUSE_PRESSED} events by creating a new {@link #selectionChange} and beginning the
     * change.
     *
     * @param selectionEvent
     *            the handled {@link SnapshotViewSkin.SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMousePressedEvent(SnapshotViewSkin.SelectionEvent selectionEvent) {
        if (selectionEvent.isPointInSelectionBounds()) {
            // get all necessary information to create a selection change
            Cursor cursor = getCursor(selectionEvent);
            Rectangle2DChangeStrategy selectionChangeStrategy = getChangeStrategy(selectionEvent);
            boolean deactivateSelectionIfClick = willDeactivateSelectionIfClick(selectionEvent);

            // create and begin the selection change
            selectionChange = new SnapshotViewSkin.SelectionChangeByStrategy(
                    getSkinnable(), setSelectionChanging, selectionChangeStrategy, cursor, deactivateSelectionIfClick);
            selectionChange.beginSelectionChange(selectionEvent.getPoint());
        } else {
            // if the mouse is outside the legal bounds, the selection will not actually change
            selectionChange = SnapshotViewSkin.NoSelectionChange.INSTANCE;
        }

        return selectionChange.getCursor();
    }
    /**
     * Handles {@link MouseEvent#MOUSE_DRAGGED} events by continuing the current {@link #selectionChange}.
     *
     * @param selectionEvent
     *            the handled {@link SnapshotViewSkin.SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMouseDraggedEvent(SnapshotViewSkin.SelectionEvent selectionEvent) {
        selectionChange.continueSelectionChange(selectionEvent.getPoint());
        return selectionChange.getCursor();
    }

    /**
     * Handles {@link MouseEvent#MOUSE_RELEASED} events by ending the current {@link #selectionChange} and setting it to
     * {@code null}.
     *
     * @param selectionEvent
     *            the handled {@link SnapshotViewSkin.SelectionEvent}
     * @return the cursor which will be used after the selection change ends
     */
    private Cursor handleMouseReleasedEvent(SnapshotViewSkin.SelectionEvent selectionEvent) {
        // end and deactivate the selection change
        selectionChange.endSelectionChange(selectionEvent.getPoint());
        selectionChange = null;

        return getCursor(selectionEvent);
    }

    // CURSOR AND SELECTION CHANGE

    /**
     * Returns the cursor which will be used for the specified selection event.
     *
     * @param selectionEvent
     *            the {@link SnapshotViewSkin.SelectionEvent} to check
     * @return the {@link Cursor} which will be used for the event
     */
    private Cursor getCursor(SnapshotViewSkin.SelectionEvent selectionEvent) {
        // show the default cursor if the mouse is out of the selection bounds
        if (!selectionEvent.isPointInSelectionBounds()) {
            return getRegularCursor();
        }

        // otherwise pick a cursor from the relative position
        switch (selectionEvent.getPosition()) {
            case IN_RECTANGLE:
                return Cursor.MOVE;
            case OUT_OF_RECTANGLE:
                return getRegularCursor();
            case NORTH_EDGE:
                return Cursor.N_RESIZE;
            case NORTHEAST_EDGE:
                return Cursor.NE_RESIZE;
            case EAST_EDGE:
                return Cursor.E_RESIZE;
            case SOUTHEAST_EDGE:
                return Cursor.SE_RESIZE;
            case SOUTH_EDGE:
                return Cursor.S_RESIZE;
            case SOUTHWEST_EDGE:
                return Cursor.SW_RESIZE;
            case WEST_EDGE:
                return Cursor.W_RESIZE;
            case NORTHWEST_EDGE:
                return Cursor.NW_RESIZE;
            default:
                throw new IllegalArgumentException("The position " + selectionEvent.getPosition() //$NON-NLS-1$
                        + " is not fully implemented."); //$NON-NLS-1$
        }
    }

    /**
     * @return the cursor from the {@link #getSkinnable() control's} current {@link SnapshotView#cursorProperty() cursor}
     */
    private Cursor getRegularCursor() {
        return getSkinnable().getCursor();
    }

    /**
     * Returns the selection change strategy based on the specified selection event, which must be a
     * {@link MouseEvent#MOUSE_PRESSED MOUSE_PRESSED} event.
     *
     * @param selectionEvent
     *            the {@link SnapshotViewSkin.SelectionEvent} which will be checked
     * @return the {@link Rectangle2DChangeStrategy} which will be executed based on the selection event
     * @throws IllegalArgumentException
     *             if {@link SnapshotViewSkin.SelectionEvent#getMouseEvent()} is not of type {@link MouseEvent#MOUSE_PRESSED}.
     */
    private Rectangle2DChangeStrategy getChangeStrategy(SnapshotViewSkin.SelectionEvent selectionEvent) {
        boolean mousePressed = selectionEvent.getMouseEvent().getEventType() == MouseEvent.MOUSE_PRESSED;
        if (!mousePressed) {
            throw new IllegalArgumentException();
        }

        Rectangle2D selectionBounds = selectionEvent.getSelectionBounds();

        switch (selectionEvent.getPosition()) {
            case IN_RECTANGLE:
                return new MoveChangeStrategy(getSelection(), selectionBounds);
            case OUT_OF_RECTANGLE:
                return new NewChangeStrategy(
                        isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case NORTH_EDGE:
                return new ToNorthChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case NORTHEAST_EDGE:
                return new ToNortheastChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case EAST_EDGE:
                return new ToEastChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case SOUTHEAST_EDGE:
                return new ToSoutheastChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case SOUTH_EDGE:
                return new ToSouthChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case SOUTHWEST_EDGE:
                return new ToSouthwestChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case WEST_EDGE:
                return new ToWestChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            case NORTHWEST_EDGE:
                return new ToNorthwestChangeStrategy(
                        getSelection(), isSelectionRatioFixed(), getSelectionRatio(), selectionBounds);
            default:
                throw new IllegalArgumentException("The position " + selectionEvent.getPosition() //$NON-NLS-1$
                        + " is not fully implemented."); //$NON-NLS-1$
        }
    }

    /**
     * Checks whether the selection will be deactivated if the mouse is clicked at the {@link SnapshotViewSkin.SelectionEvent}.
     *
     * @param selectionEvent
     *            the selection event which will be checked
     * @return {@code true} if the selection event is such that the selection will be deactivated if the mouse is only
     *         clicked
     */
    private static boolean willDeactivateSelectionIfClick(SnapshotViewSkin.SelectionEvent selectionEvent) {
        boolean rightClick = selectionEvent.getMouseEvent().getButton() == MouseButton.SECONDARY;
        boolean outOfAreaClick = selectionEvent.getPosition() == CoordinatePosition.OUT_OF_RECTANGLE;

        return rightClick || outOfAreaClick;
    }

    /* ************************************************************************
     *                                                                         *
     * Usability Access Functions to SnapshotView Properties                   *
     *                                                                         *
     **************************************************************************/

    /**
     * The control's width.
     *
     * @return {@link SnapshotView#getWidth()}
     */
    private double getControlWidth() {
        return getSkinnable().getWidth();
    }

    /**
     * The control's height.
     *
     * @return {@link SnapshotView#getHeight()}
     */
    private double getControlHeight() {
        return getSkinnable().getHeight();
    }

    /**
     * The currently displayed node.
     *
     * @return {@link SnapshotView#getNode()}
     */
    private Node getSnapshotNode() {
        return getSkinnable().getNode();
    }

    /**
     * The current selection.
     *
     * @return {@link SnapshotView#getSelection()}
     */
    private Rectangle2D getSelection() {
        return getSkinnable().getSelection();
    }
    /**
     * Indicates whether the current selection has a fixed ratio.
     *
     * @return {@link SnapshotView#isSelectionRatioFixed()}
     */
    private boolean isSelectionRatioFixed() {
        return getSkinnable().isSelectionRatioFixed();
    }

    /**
     * The current selection's fixed ratio.
     *
     * @return {@link SnapshotView#getFixedSelectionRatio()}
     */
    private double getSelectionRatio() {
        return getSkinnable().getFixedSelectionRatio();
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * Clips the unselected area to the {@link SnapshotView#unselectedAreaBoundaryProperty() unselectedAreaBoundary}.
     *
     */
    private static class Clipper {

        /**
         * The snapshot view to whose {@link Node#boundsInLocalProperty() boundsInLocal} the {@link #clippedNode} will
         * be clipped.
         */
        private final SnapshotView snapshotView;

        /**
         * The node to which the clips will be added.
         */
        private final Node clippedNode;

        /**
         * A function which rebinds the clip's visibility after it was unbound. Only necessary because of the workaround
         * explained in {@link #setClip(Node) setClip}.
         */
        private final Runnable rebindClippedNodeVisibility;

        /**
         * The {@link Rectangle} used to clip the {@link #clippedNode} to {@link Boundary#CONTROL}.
         */
        private final Rectangle controlClip;

        /**
         * The {@link Rectangle} used to clip the {@link #clippedNode} to {@link Boundary#NODE}.
         */
        private final Rectangle nodeClip;

        /**
         * A listener which updates the {@link #controlClip} when the {@link #snapshotView}'s
         * {@link Node#boundsInLocalProperty() boundsInLocal} change.
         */
        private final ChangeListener<Bounds> updateControlClipToNewBoundsListener;

        /**
         * A listener which updates the {@link #nodeClip} when the {@link SnapshotView#nodeProperty() node}'s
         * {@link Node#boundsInParentProperty() boundsInParent} change.
         */
        private final ChangeListener<Bounds> updateNodeClipToNewBoundsListener;

        /**
         * Creates a new clipper with the specified arguments.
         * 
         * @param snapshotView
         *            the {@link SnapshotView} to whose bounds the {@code clippedNode} will be clipped
         * @param clippedNode
         *            the {@link Node} whose bounds will be clipped
         * @param rebindClippedNodeVisibility
         *            a function which rebinds the {@code clippedNode}'s visibility
         */
        public Clipper(SnapshotView snapshotView, Node clippedNode, Runnable rebindClippedNodeVisibility) {
            this.snapshotView = snapshotView;
            this.clippedNode = clippedNode;
            this.rebindClippedNodeVisibility = rebindClippedNodeVisibility;

            // for 'CONTROL', clip to the control's bounds
            controlClip = new Rectangle();
            updateControlClipToNewBoundsListener =
                    (o, oldBounds, newBounds) -> resizeRectangleToBounds(controlClip, newBounds);

            // for 'NODE', clip to the node's bounds
            nodeClip = new Rectangle();
            // create the listener which will resize the rectangle
            updateNodeClipToNewBoundsListener =
                    (o, oldBounds, newBounds) -> resizeRectangleToBounds(nodeClip, newBounds);

            // set the clipping and keep updating it
            setClipping();
            snapshotView.unselectedAreaBoundaryProperty().addListener((o, oldBoundary, newBoundary) -> setClipping());
        }

        /**
         * Sets clipping to the current {@link SnapshotView#unselectedAreaBoundaryProperty() unselectedAreaBoundary}.
         */
        private void setClipping() {
            Boundary boundary = snapshotView.getUnselectedAreaBoundary();
            switch (boundary) {
            case CONTROL:
                clipToControl();
                break;
            case NODE:
                clipToNode();
                break;
            default:
                throw new IllegalArgumentException("The boundary " + boundary + " is not fully implemented."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * Clips the {@link #clippedNode} to {@link #controlClip} and keeps updating the latter when the control changes
         * its bounds.
         */
        private void clipToControl() {
            // stop resizing the node clip
            updateNodeClipToChangingNode(snapshotView.nodeProperty(), snapshotView.getNode(), null);

            // resize the control clip and keep doing so
            resizeRectangleToBounds(controlClip, snapshotView.getBoundsInLocal());
            snapshotView.boundsInLocalProperty().addListener(updateControlClipToNewBoundsListener);

            // set the clip
            setClip(controlClip);
        }

        /**
         * Clips the {@link #clippedNode} to {@link #nodeClip} and keeps updating the latter when the control changes
         * its bounds.
         */
        private void clipToNode() {
            // update the node clip to the new bounds and whenever the node changes its bounds
            updateNodeClipToChangingNode(snapshotView.nodeProperty(), null, snapshotView.getNode());
            // move that listener from old to new nodes
            snapshotView.nodeProperty().addListener(this::updateNodeClipToChangingNode);

            // set the clip
            setClip(nodeClip);
        }

        /**
         * Resizes the {@link #nodeClip} to the specified new node's {@link Node#boundsInParentProperty()
         * boundsInParent} (or to an empty rectangle if it is {@code null}) and moves the
         * {@link #updateNodeClipToNewBoundsListener} from the old to the new node's {@code boundInParents} property.
         * <p>
         * Designed to be used as a lambda method reference.
         * 
         * @param o
         *            the {@link ObservableValue} which changed its value
         * @param oldNode
         *            the old node
         * @param newNode
         *            the new node
         */
        private void updateNodeClipToChangingNode(
                @SuppressWarnings("unused") ObservableValue<? extends Node> o, Node oldNode, Node newNode) {

            // resize the rectangle to match the new node
            resizeRectangleToNodeBounds(nodeClip, newNode);

            // move the listener from one node to the next
            if (oldNode != null) {
                oldNode.boundsInParentProperty().removeListener(updateNodeClipToNewBoundsListener);
            }
            if (newNode != null) {
                newNode.boundsInParentProperty().addListener(updateNodeClipToNewBoundsListener);
            }
        }

        /**
         * Resizes the specified rectangle to the specified node's {@link Node#boundsInParentProperty() boundsInParent}.
         * 
         * @param rectangle
         *            the {@link Rectangle} which will be resized
         * @param node
         *            the {@link Node} to whose bounds the {@code rectangle} will be resized
         */
        private static void resizeRectangleToNodeBounds(Rectangle rectangle, Node node) {
            if (node == null) {
                resizeRectangleToZero(rectangle);
            } else {
                resizeRectangleToBounds(rectangle, node.getBoundsInParent());
            }
        }

        /**
         * Resized the specified rectangle so that its upper left point is {@code (0, 0)} and its width and height are
         * both 0.
         * 
         * @param rectangle
         *            the {@link Rectangle} which will be resized
         */
        private static void resizeRectangleToZero(Rectangle rectangle) {
            rectangle.setX(0);
            rectangle.setY(0);
            rectangle.setWidth(0);
            rectangle.setHeight(0);
        }

        /**
         * Resized the specified rectangle so that it matches the specified bounds, i.e. it will have the same upper
         * left point and width and height.
         * 
         * @param rectangle
         *            the {@link Rectangle} which will be resized
         * @param bounds
         *            the {@link Bounds} to which the rectangle will be resized
         */
        private static void resizeRectangleToBounds(Rectangle rectangle, Bounds bounds) {
            rectangle.setX(bounds.getMinX());
            rectangle.setY(bounds.getMinY());
            rectangle.setWidth(bounds.getWidth());
            rectangle.setHeight(bounds.getHeight());
        }

        /**
         * Sets the specified clip on the {@link #clippedNode}.
         * 
         * @param clip
         *            the {@link Node} which is used as a clip
         */
        private void setClip(Node clip) {

            /*
             * UGLY WORKAROUND
             * 
             * Setting the clip on the unselected area while it is invisible leads to either the clip having no effect
             * or no area being displayed at all. Obviously I'm doing something wrong but I couldn't determine the root
             * cause so I fixed the symptom. Now the area is turned visible, the clip is set and then it is made
             * invisible again.
             * 
             * Everything below but 'clippedNode.setClip(clip);' is part of that workaround. To reproduce the bug
             * comment all those lines out. Then, after 'HelloSnapshotView' started, select 'NODE' for the unselected
             * area boundary and draw a selection on the node. The area above the node which is not selected should be
             * painted in a semi-opaque black but due to the bug it is not. Instead the area outside of the selection
             * has no paint at all and is simply transparent.
             * Note that if the boundary is turned back to CONTROL, a selection is made and then NODE is set again, the
             * clips works properly and the preexisting selection's outer area is clipped to the node.  
             * 
             * If someone finds out what the *$#&? I've been doing wrong, please fix and be so kind to mail to
             * nipa@codefx.org! :)
             */

            boolean workAroundVisibilityProblem = !clippedNode.isVisible();
            if (workAroundVisibilityProblem) {
                clippedNode.visibleProperty().unbind();
                clippedNode.setVisible(true);
            }

            clippedNode.setClip(clip);

            if (workAroundVisibilityProblem) {
                rebindClippedNodeVisibility.run();
            }
        }
    }

    /**
     * A selection event encapsulates a {@link MouseEvent} and adds some additional information like the coordinates
     * relative to the node's preferred size and its position relative to a selection.
     */
    private static class SelectionEvent {

        /**
         * The {@link MouseEvent} for which this selection event was created.
         */
        private final MouseEvent mouseEvent;

        /**
         * The coordinates of the mouse event as a {@link Point2D}.
         */
        private final Point2D point;

        /**
         * The {@link Rectangle2D} within which any new selection must be contained.
         */
        private final Rectangle2D selectionBounds;

        /**
         * The {@link #point}'s position relative to a possible selection.
         */
        private final CoordinatePosition position;

        /**
         * Creates a new selection event with the specified arguments.
         *
         * @param mouseEvent
         *            the {@link MouseEvent} for which this selection event is created
         * @param point
         *            the coordinates of the mouse event as a {@link Point2D}
         * @param selectionBounds
         *            the {@link Rectangle2D} within which any new selection must be contained
         * @param position
         *            the point's position relative to a possible selection
         */
        public SelectionEvent(
                MouseEvent mouseEvent, Point2D point, Rectangle2D selectionBounds, CoordinatePosition position) {

            this.mouseEvent = mouseEvent;
            this.point = point;
            this.selectionBounds = selectionBounds;
            this.position = position;
        }

        /**
         * @return the mouse event for which this selection event was created
         */
        public MouseEvent getMouseEvent() {
            return mouseEvent;
        }

        /**
         * @return the coordinates of the mouse event in the nodes' preferred coordinates
         */
        public Point2D getPoint() {
            return point;
        }

        /**
         * @return the {@link Rectangle2D} within which any new selection must be contained
         */
        public Rectangle2D getSelectionBounds() {
            return selectionBounds;
        }

        /**
         * @return {@code true} if the {@link #getSelectionBounds() selectionBounds} contains the {@link #getPoint()
         *         point}; otherwise {@code false}
         */
        public boolean isPointInSelectionBounds() {
            return selectionBounds.contains(point);
        }

        /**
         * @return the {@link #getPoint() point}'s position relative to a possible selection.
         */
        public CoordinatePosition getPosition() {
            return position;
        }

    }

    /**
     * Handles the actual change of a selection when the mouse is pressed, dragged and released.
     */
    private interface SelectionChange {

        /**
         * Begins the selection change at the specified point.
         *
         * @param point
         *            the starting point of the selection change
         */
        void beginSelectionChange(Point2D point);

        /**
         * Continues the selection change to the specified point.
         *
         * @param point
         *            the next point of this selection change
         */
        void continueSelectionChange(Point2D point);

        /**
         * Ends the selection change at the specified point.
         *
         * @param point
         *            the final point of this selection change
         */
        void endSelectionChange(Point2D point);

        /**
         * The cursor for this selection change.
         *
         * @return the cursor for this selection change
         */
        Cursor getCursor();

    }

    /**
     * Implementation of {@link SnapshotViewSkin.SelectionChange} which does not actually change anything.
     */
    private static class NoSelectionChange implements SnapshotViewSkin.SelectionChange {

        /**
         * The singleton instance.
         */
        public static final SnapshotViewSkin.NoSelectionChange INSTANCE = new SnapshotViewSkin.NoSelectionChange();

        /**
         * Private constructor for singleton.
         */
        private NoSelectionChange() {
            // nothing to do
        }

        @Override
        public void beginSelectionChange(Point2D point) {
            // nothing to do
        }

        @Override
        public void continueSelectionChange(Point2D point) {
            // nothing to do
        }

        @Override
        public void endSelectionChange(Point2D point) {
            // nothing to do
        }

        @Override
        public Cursor getCursor() {
            return Cursor.DEFAULT;
        }

    }

    /**
     * Executes the changes from a {@link Rectangle2DChangeStrategy} on a {@link SnapshotView}'s
     * {@link SnapshotView#selectionProperty() selection} property. This includes to check whether the mouse moved from
     * the change's start to end and to possibly deactivate the selection if not.
     */
    private static class SelectionChangeByStrategy implements SnapshotViewSkin.SelectionChange {

        // Attributes

        /**
         * The snapshot view whose selection will be changed.
         */
        private final SnapshotView snapshotView;

        /**
         * A function which sets the {@link SnapshotView#selectionChangingProperty() selectionChanging} property to the
         * given value.
         */
        private final Consumer<Boolean> setSelectionChanging;

        /**
         * The executed change strategy.
         */
        private final Rectangle2DChangeStrategy selectionChangeStrategy;

        /**
         * The cursor during the selection change.
         */
        private final Cursor cursor;

        /**
         * Indicates if the selection will be deactivated if the mouse is only clicked (e.g. does not move between start
         * and end).
         */
        private final boolean deactivateSelectionIfClick;

        /**
         * The change's starting point. Used to check whether the mouse moved.
         */
        private Point2D startingPoint;

        /**
         * Set to true as soon as the mouse moved away from the starting point.
         */
        private boolean mouseMoved;

        // Constructor

        /**
         * Creates a new selection change for the specified {@link SnapshotView} using the specified
         * {@link Rectangle2DChangeStrategy}.
         *
         * @param snapshotView
         *            the {@link SnapshotView} whose selection will be changed
         * @param setSelectionChanging
         *            a function which sets the {@link SnapshotView#selectionChangingProperty() selectionChanging}
         *            property to the given value
         * @param selectionChangeStrategy
         *            the {@link Rectangle2DChangeStrategy} used to change the selection
         * @param cursor
         *            the {@link Cursor} used during the selection change
         * @param deactivateSelectionIfClick
         *            indicates whether the selection will be deactivated if the change is only a click
         */
        public SelectionChangeByStrategy(
                SnapshotView snapshotView, Consumer<Boolean> setSelectionChanging,
                Rectangle2DChangeStrategy selectionChangeStrategy, Cursor cursor, boolean deactivateSelectionIfClick) {

            this.snapshotView = snapshotView;
            this.setSelectionChanging = setSelectionChanging;
            this.selectionChangeStrategy = selectionChangeStrategy;
            this.cursor = cursor;
            this.deactivateSelectionIfClick = deactivateSelectionIfClick;
        }

        // Selection Change

        @Override
        public void beginSelectionChange(Point2D point) {
            startingPoint = point;
            setSelectionChanging.accept(true);

            Rectangle2D newSelection = selectionChangeStrategy.beginChange(point);
            snapshotView.setSelection(newSelection);
        }

        @Override
        public void continueSelectionChange(Point2D point) {
            updateMouseMoved(point);

            Rectangle2D newSelection = selectionChangeStrategy.continueChange(point);
            snapshotView.setSelection(newSelection);
        }

        @Override
        public void endSelectionChange(Point2D point) {
            updateMouseMoved(point);

            Rectangle2D newSelection = selectionChangeStrategy.endChange(point);
            snapshotView.setSelection(newSelection);

            boolean deactivateSelection = deactivateSelectionIfClick && !mouseMoved;
            if (deactivateSelection) {
                snapshotView.setSelection(null);
            }
            setSelectionChanging.accept(false);
        }

        /**
         * Updates {@link #mouseMoved} by checking whether the specified point is different from the
         * {@link #startingPoint}.
         *
         * @param point
         *            the point which will be compared to the {@link #startingPoint}
         */
        private void updateMouseMoved(Point2D point) {
            // if the mouse already moved, do nothing
            if (mouseMoved) {
                return;
            }

            // if the mouse did not move yet, check whether it did now
            boolean mouseMovedNow = !startingPoint.equals(point);
            mouseMoved = mouseMovedNow;
        }

        // Attribute Access
        @Override
        public Cursor getCursor() {
            return cursor;
        }
    }
}
