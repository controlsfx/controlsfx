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
package impl.org.controlsfx.behavior;

import impl.org.controlsfx.tools.rectangle.CoordinatePosition;
import impl.org.controlsfx.tools.rectangle.CoordinatePositions;
import impl.org.controlsfx.tools.rectangle.Rectangles2D;
import impl.org.controlsfx.tools.rectangle.change.MoveChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.NewChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.Rectangle2DChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToEastChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToNorthChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToNortheastChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToNorthwestChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToSouthChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToSoutheastChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToSouthwestChangeStrategy;
import impl.org.controlsfx.tools.rectangle.change.ToWestChangeStrategy;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.SnapshotView;
import org.controlsfx.control.SnapshotView.Boundary;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

/**
 * The behavior for the {@link SnapshotView}. It is concerned with creating and changing selections according to mouse
 * events handed to {@link #handleMouseEvent(MouseEvent) handleMouseEvents}.
 */
public class SnapshotViewBehavior extends BehaviorBase<SnapshotView> {

    /**
     * The percentage of the control's node's width/height used as a tolerance for determining whether the cursor is on
     * an edge of the selection.
     */
    private static final double RELATIVE_EDGE_TOLERANCE = 0.015;

    /* ************************************************************************
     *                                                                         *
     * Attributes                                                              *
     *                                                                         *
     **************************************************************************/

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
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new behavior for the specified {@link SnapshotView}.
     * 
     * @param snapshotView
     *            the control which this behavior will control
     */
    public SnapshotViewBehavior(SnapshotView snapshotView) {
        super(snapshotView, new ArrayList<KeyBinding>());
        this.setSelectionChanging = createSetSelectionChanging();
    }

    /**
     * Creates a function which sets the applied boolean to {@link SnapshotView#selectionChangingProperty()}.
     * 
     * @return a Boolean {@link Consumer}
     */
    private Consumer<Boolean> createSetSelectionChanging() {
        return changing -> getControl().getProperties().put(SnapshotView.SELECTION_CHANGING_PROPERTY_KEY, changing);
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
    public Cursor handleMouseEvent(MouseEvent mouseEvent) {
        Objects.requireNonNull(mouseEvent, "The argument 'mouseEvent' must not be null."); //$NON-NLS-1$

        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        SelectionEvent selectionEvent = createSelectionEvent(mouseEvent);

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
     * @return the {@link SelectionEvent} for the specified mouse event
     */
    private SelectionEvent createSelectionEvent(MouseEvent mouseEvent) {
        Point2D point = new Point2D(mouseEvent.getX(), mouseEvent.getY());
        Rectangle2D selectionBounds = createBoundsForCurrentBoundary();
        CoordinatePosition position = computePosition(point);
        return new SelectionEvent(mouseEvent, point, selectionBounds, position);
    }

    /**
     * Returns the bounds according to the current {@link SnapshotView#selectionAreaBoundaryProperty()
     * selectionAreaBoundary}.
     * 
     * @return the bounds as a {@link Rectangle2D}
     */
    private Rectangle2D createBoundsForCurrentBoundary() {
        Boundary boundary = getControl().getSelectionAreaBoundary();
        switch (boundary) {
        case CONTROL:
            return new Rectangle2D(0, 0, getControlWidth(), getControlHeight());
        case NODE:
            boolean nodeExists = getNode() != null;
            if (nodeExists) {
                Bounds nodeBounds = getNode().getBoundsInParent();
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
        boolean noSelection = !getControl().hasSelection() || !getControl().isSelectionActive();
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
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMousePressedEvent(SelectionEvent selectionEvent) {
        if (selectionEvent.isPointInSelectionBounds()) {
            // get all necessary information to create a selection change
            Cursor cursor = getCursor(selectionEvent);
            Rectangle2DChangeStrategy selectionChangeStrategy = getChangeStrategy(selectionEvent);
            boolean deactivateSelectionIfClick = willDeactivateSelectionIfClick(selectionEvent);

            // create and begin the selection change
            selectionChange = new SelectionChangeByStrategy(
                    getControl(), setSelectionChanging, selectionChangeStrategy, cursor, deactivateSelectionIfClick);
            selectionChange.beginSelectionChange(selectionEvent.getPoint());
        } else {
            // if the mouse is outside the legal bounds, the selection will not actually change
            selectionChange = NoSelectionChange.INSTANCE;
        }

        return selectionChange.getCursor();
    }
    /**
     * Handles {@link MouseEvent#MOUSE_DRAGGED} events by continuing the current {@link #selectionChange}.
     * 
     * @param selectionEvent
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMouseDraggedEvent(SelectionEvent selectionEvent) {
        selectionChange.continueSelectionChange(selectionEvent.getPoint());
        return selectionChange.getCursor();
    }

    /**
     * Handles {@link MouseEvent#MOUSE_RELEASED} events by ending the current {@link #selectionChange} and setting it to
     * {@code null}.
     * 
     * @param selectionEvent
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used after the selection change ends
     */
    private Cursor handleMouseReleasedEvent(SelectionEvent selectionEvent) {
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
     *            the {@link SelectionEvent} to check
     * @return the {@link Cursor} which will be used for the event
     */
    private Cursor getCursor(SelectionEvent selectionEvent) {
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
     * @return the cursor from the {@link #getControl() control's} current {@link SnapshotView#cursorProperty() cursor}
     */
    private Cursor getRegularCursor() {
        return getControl().getCursor();
    }

    /**
     * Returns the selection change strategy based on the specified selection event, which must be a
     * {@link MouseEvent#MOUSE_PRESSED MOUSE_PRESSED} event.
     * 
     * @param selectionEvent
     *            the {@link SelectionEvent} which will be checked
     * @return the {@link Rectangle2DChangeStrategy} which will be executed based on the selection event
     * @throws IllegalArgumentException
     *             if {@link SelectionEvent#getMouseEvent()} is not of type {@link MouseEvent#MOUSE_PRESSED}.
     */
    private Rectangle2DChangeStrategy getChangeStrategy(SelectionEvent selectionEvent) {
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
     * Checks whether the selection will be deactivated if the mouse is clicked at the {@link SelectionEvent}.
     * 
     * @param selectionEvent
     *            the selection event which will be checked
     * @return {@code true} if the selection event is such that the selection will be deactivated if the mouse is only
     *         clicked
     */
    private static boolean willDeactivateSelectionIfClick(SelectionEvent selectionEvent) {
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
        return getControl().getWidth();
    }

    /**
     * The control's height.
     * 
     * @return {@link SnapshotView#getHeight()}
     */
    private double getControlHeight() {
        return getControl().getHeight();
    }

    /**
     * The currently displayed node.
     * 
     * @return {@link SnapshotView#getNode()}
     */
    private Node getNode() {
        return getControl().getNode();
    }

    /**
     * The current selection.
     * 
     * @return {@link SnapshotView#getSelection()}
     */
    private Rectangle2D getSelection() {
        return getControl().getSelection();
    }
    /**
     * Indicates whether the current selection has a fixed ratio.
     * 
     * @return {@link SnapshotView#isSelectionRatioFixed()}
     */
    private boolean isSelectionRatioFixed() {
        return getControl().isSelectionRatioFixed();
    }

    /**
     * The current selection's fixed ratio.
     * 
     * @return {@link SnapshotView#getFixedSelectionRatio()}
     */
    private double getSelectionRatio() {
        return getControl().getFixedSelectionRatio();
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

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
    private static interface SelectionChange {

        /**
         * Begins the selection change at the specified point.
         * 
         * @param point
         *            the starting point of the selection change
         */
        public abstract void beginSelectionChange(Point2D point);

        /**
         * Continues the selection change to the specified point.
         * 
         * @param point
         *            the next point of this selection change
         */
        public abstract void continueSelectionChange(Point2D point);

        /**
         * Ends the selection change at the specified point.
         * 
         * @param point
         *            the final point of this selection change
         */
        public abstract void endSelectionChange(Point2D point);

        /**
         * The cursor for this selection change.
         * 
         * @return the cursor for this selection change
         */
        public abstract Cursor getCursor();

    }

    /**
     * Implementation of {@link SelectionChange} which does not actually change anything.
     */
    private static class NoSelectionChange implements SelectionChange {

        /**
         * The singleton instance.
         */
        public static final NoSelectionChange INSTANCE = new NoSelectionChange();

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
    private static class SelectionChangeByStrategy implements SelectionChange {

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
