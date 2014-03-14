package org.controlsfx.control.imageview;

import java.util.ArrayList;

import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import org.controlsfx.tools.rectangle.CoordinatePosition;
import org.controlsfx.tools.rectangle.CoordinatePositions;
import org.controlsfx.tools.rectangle.change.Rectangle2DChangeStrategy;
import org.controlsfx.tools.rectangle.change.MoveChangeStrategy;
import org.controlsfx.tools.rectangle.change.NewChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToEastChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToNorthChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToNortheastChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToNorthwestChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToSouthChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToSoutheastChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToSouthwestChangeStrategy;
import org.controlsfx.tools.rectangle.change.ToWestChangeStrategy;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

/**
 * The behavior for the {@link SelectableImageView}.
 */
public class SelectableImageViewBehavior extends BehaviorBase<SelectableImageView> {

    /**
     * The percentage of the image's width/height used as a tolerance for determining whether the cursor is on an edge.
     */
    private static final double relativeEdgeTolerance = 0.01;

    /* ************************************************************************
     *                                                                         *
     * Attributes                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * The current selection change; might be null.
     */
    private SelectionChange selectionChange;

    /* ************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new behavior for the specified {@link SelectableImageView}.
     * 
     * @param selectableImageView
     *            the control which this behavior will control
     */
    public SelectableImageViewBehavior(SelectableImageView selectableImageView) {
        super(selectableImageView, new ArrayList<KeyBinding>());
    }

    /* ************************************************************************
     *                                                                         *
     * Usability Access Functions to SelectableImageView Properties            *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns the tolerance used to compute whether the cursor is on an edge.
     * 
     * @return the tolerance based on the currently shown image's width and height.
     */
    private double getTolerance() {
        // TODO better way to compute this?
        double meanLength = Math.sqrt(getImageWidth() * getImageHeight());
        return meanLength * relativeEdgeTolerance;
    }

    /**
     * The current selection.
     * 
     * @return {@link SelectableImageView#getSelection()}
     */
    private Rectangle2D getSelection() {
        return getControl().getSelection();
    }

    /**
     * The image's width.
     * 
     * @return {@link Image#getWidth() width} of {@link SelectableImageView#getImage()}
     */
    private double getImageWidth() {
        return getControl().getImage().getWidth();
    }

    /**
     * The image's height.
     * 
     * @return {@link Image#getHeight() height} of {@link SelectableImageView#getImage()}
     */
    private double getImageHeight() {
        return getControl().getImage().getHeight();
    }

    /**
     * Indicates whether the current selection has a fixed ratio.
     * 
     * @return {@link SelectableImageView#isSelectionRatioFixed()}
     */
    private boolean isSelectionRatioFixed() {
        return getControl().isSelectionRatioFixed();
    }

    /**
     * The current selection's fixed ratio.
     * 
     * @return {@link SelectableImageView#getFixedSelectionRatio()}
     */
    private double getSelectionRatio() {
        return getControl().getFixedSelectionRatio();
    }

    /* ************************************************************************
     *                                                                         *
     * Events                                                                  *
     *                                                                         *
     **************************************************************************/

    /**
     * Handles the specified selection event (possibly by creating/changing/removing a selection) and returns the
     * matching cursor.
     * 
     * @param selectionEvent
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used for this event
     */
    public Cursor handleSelectionEvent(SelectionEvent selectionEvent) {
        EventType<? extends MouseEvent> eventType = selectionEvent.getMouseEvent().getEventType();

        if (eventType == MouseEvent.MOUSE_MOVED)
            return getCursor(selectionEvent);
        if (eventType == MouseEvent.MOUSE_PRESSED)
            return handleMousePressedEvent(selectionEvent);
        if (eventType == MouseEvent.MOUSE_DRAGGED)
            return handleMouseDraggedEvent(selectionEvent);
        if (eventType == MouseEvent.MOUSE_RELEASED)
            return handleMouseReleasedEvent(selectionEvent);

        return Cursor.DEFAULT;
    }

    /**
     * Handles {@link MouseEvent#MOUSE_PRESSED} events by creating a new {@link #selectionChange} and beginning the
     * change.
     * 
     * @param selectionEvent
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMousePressedEvent(SelectionEvent selectionEvent) {
        // get all necessary information to create a selection change
        Cursor cursor = getCursor(selectionEvent);
        Rectangle2DChangeStrategy selectionChangeStrategy = getChangeStrategy(selectionEvent);
        boolean deactivateSelectionIfClick = willDeactivateSelectionIfClick(selectionEvent);

        // create and begin the selection change
        selectionChange = new SelectionChange(getControl(), selectionChangeStrategy, cursor, deactivateSelectionIfClick);
        selectionChange.beginSelectionChange(selectionEvent.getPointInImage());

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
        selectionChange.continueSelectionChange(selectionEvent.getPointInImage());
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
        selectionChange.endSelectionChange(selectionEvent.getPointInImage());
        selectionChange = null;

        return getCursor(selectionEvent);
    }

    /* ************************************************************************
     *                                                                         *
     * Selection Change                                                        *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns the position the selection event's coordinates have relative to a possible selection.
     * 
     * @param selectionEvent
     *            the {@link SelectionEvent} whose {@link SelectionEvent#getPointInImage() pointInImage} will be checked
     * @return the {@link CoordinatePosition} the selection's point has relative to the control's current selection; if
     *         the selection is inactive this always returns {@link CoordinatePosition#OUT_OF_RECTANGLE}.
     */
    private CoordinatePosition getPosition(SelectionEvent selectionEvent) {
        boolean noSelection = !getControl().isSelectionActive();
        if (noSelection)
            return CoordinatePosition.OUT_OF_RECTANGLE;

        CoordinatePosition onEdge = CoordinatePositions
                .onEdges(getSelection(), selectionEvent.getPointInImage(), getTolerance());
        if (onEdge != null)
            return onEdge;
        else
            return CoordinatePositions.inRectangle(getSelection(), selectionEvent.getPointInImage());
    }

    /**
     * Returns the cursor which will be used for the specified selection event.
     * 
     * @param selectionEvent
     *            the {@link SelectionEvent} to check
     * @return the {@link Cursor} which will be used for the event
     */
    private Cursor getCursor(SelectionEvent selectionEvent) {
        CoordinatePosition position = getPosition(selectionEvent);
        switch (position) {
        case IN_RECTANGLE:
            return Cursor.MOVE;
        case OUT_OF_RECTANGLE:
            return Cursor.DEFAULT;
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
            throw new IllegalArgumentException("The position " + position + " is not fully implemented.");
        }
    }

    /**
     * Returns the selection change strategy based on the specified selection event, which must be based on a
     * {@link MouseEvent#MOUSE_PRESSED} event.
     * 
     * @param selectionEvent
     *            the {@link SelectionEvent} which will be checked
     * @return the {@link Rectangle2DChangeStrategy} which will be executed based on the selection event
     * @throws IllegalArgumentException
     *             if {@link SelectionEvent#getMouseEvent()} is not of type {@link MouseEvent#MOUSE_PRESSED}.
     */
    private Rectangle2DChangeStrategy getChangeStrategy(SelectionEvent selectionEvent) {
        boolean mousePressed = selectionEvent.getMouseEvent().getEventType() == MouseEvent.MOUSE_PRESSED;
        if (!mousePressed)
            throw new IllegalArgumentException();

        CoordinatePosition position = getPosition(selectionEvent);
        switch (position) {
        case IN_RECTANGLE:
            return new MoveChangeStrategy(getSelection(), getImageWidth(), getImageHeight());
        case OUT_OF_RECTANGLE:
            return new NewChangeStrategy(isSelectionRatioFixed(), getSelectionRatio());
        case NORTH_EDGE:
            return new ToNorthChangeStrategy(
                    getSelection(), isSelectionRatioFixed(), getSelectionRatio(), getImageWidth(), getImageHeight());
        case NORTHEAST_EDGE:
            return new ToNortheastChangeStrategy(getSelection(), isSelectionRatioFixed(), getSelectionRatio());
        case EAST_EDGE:
            return new ToEastChangeStrategy(
                    getSelection(), isSelectionRatioFixed(), getSelectionRatio(), getImageWidth(), getImageHeight());
        case SOUTHEAST_EDGE:
            return new ToSoutheastChangeStrategy(getSelection(), isSelectionRatioFixed(), getSelectionRatio());
        case SOUTH_EDGE:
            return new ToSouthChangeStrategy(
                    getSelection(), isSelectionRatioFixed(), getSelectionRatio(), getImageWidth(), getImageHeight());
        case SOUTHWEST_EDGE:
            return new ToSouthwestChangeStrategy(getSelection(), isSelectionRatioFixed(), getSelectionRatio());
        case WEST_EDGE:
            return new ToWestChangeStrategy(
                    getSelection(), isSelectionRatioFixed(), getSelectionRatio(), getImageWidth(), getImageHeight());
        case NORTHWEST_EDGE:
            return new ToNorthwestChangeStrategy(getSelection(), isSelectionRatioFixed(), getSelectionRatio());
        default:
            throw new IllegalArgumentException("The position " + position + " is not fully implemented.");
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
    private boolean willDeactivateSelectionIfClick(SelectionEvent selectionEvent) {
        CoordinatePosition position = getPosition(selectionEvent);
        return position == CoordinatePosition.OUT_OF_RECTANGLE;
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * An event for which this class provides a cursor and possibly executes a {@link Rectangle2DChangeStrategy selection change
     * strategy}.
     */
    public interface SelectionEvent {

        /**
         * The mouse event which caused this event
         * 
         * @return the mouse event
         */
        MouseEvent getMouseEvent();

        /**
         * The event's x/y-coordinates translated to the image's coordinates.
         * 
         * @return a point in the image
         */
        Point2D getPointInImage();
    }

    /**
     * Executes the changes from a {@link Rectangle2DChangeStrategy} on a {@link SelectableImageView}'s
     * {@link SelectableImageView#selectionProperty() selection} property. This includes to check whether the mouse
     * moved from the change's start to end and to possibly deactivate the selection if not.
     */
    private static class SelectionChange {

        // Attributes

        /**
         * The image view whose selection will be changed.
         */
        private final SelectableImageView selectableImageView;

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
         * Creates a new selection change for the specified {@link SelectableImageView} using the specified
         * {@link Rectangle2DChangeStrategy}.
         * 
         * @param selectableImageView
         *            the {@link SelectableImageView} whose selection will be changed
         * @param selectionChangeStrategy
         *            the {@link Rectangle2DChangeStrategy} used to change the selection
         * @param cursor
         *            the {@link Cursor} used during the selection change
         * @param deactivateSelectionIfClick
         *            indicates whether the selection will be deactivated if the change is only a click
         */
        public SelectionChange(SelectableImageView selectableImageView, Rectangle2DChangeStrategy selectionChangeStrategy,
                Cursor cursor, boolean deactivateSelectionIfClick) {
            super();
            this.selectableImageView = selectableImageView;
            this.selectionChangeStrategy = selectionChangeStrategy;
            this.cursor = cursor;
            this.deactivateSelectionIfClick = deactivateSelectionIfClick;
        }

        // Selection Change

        /**
         * Begins the selection change at the specified point.
         * 
         * @param point
         *            the starting point of the selection change
         */
        public void beginSelectionChange(Point2D point) {
            startingPoint = point;
            selectableImageView.selectionChangingProperty().set(true);

            Rectangle2D newSelection = selectionChangeStrategy.beginChange(point);
            selectableImageView.setSelection(newSelection);
        }

        /**
         * Continues the selection change to the specified point.
         * 
         * @param point
         *            the next point of this selection change
         */
        public void continueSelectionChange(Point2D point) {
            updateMouseMoved(point);

            Rectangle2D newSelection = selectionChangeStrategy.continueChange(point);
            selectableImageView.setSelection(newSelection);
        }

        /**
         * Ends the selection change at the specified point.
         * 
         * @param point
         *            the final point of this selection change
         */
        public void endSelectionChange(Point2D point) {
            updateMouseMoved(point);

            Rectangle2D newSelection = selectionChangeStrategy.endChange(point);
            selectableImageView.setSelection(newSelection);

            boolean deactivateSelection = deactivateSelectionIfClick && !mouseMoved;
            if (deactivateSelection)
                selectableImageView.setSelectionActive(false);
            selectableImageView.selectionChangingProperty().set(false);
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
            if (mouseMoved)
                return;

            // of the mouse did not move yet, check whether it did now
            boolean mouseMovedNow = !startingPoint.equals(point);
            mouseMoved = mouseMovedNow;
        }

        // Attribute Access

        /**
         * The cursor for this selection change.
         * 
         * @return the cursor for this selection change
         */
        public Cursor getCursor() {
            return cursor;
        }

    }

}
