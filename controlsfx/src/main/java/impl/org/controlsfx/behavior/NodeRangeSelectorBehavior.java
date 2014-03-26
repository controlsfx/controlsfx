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

import impl.org.controlsfx.tools.MathTools;
import impl.org.controlsfx.tools.rectangle.CoordinatePosition;
import impl.org.controlsfx.tools.rectangle.CoordinatePositions;
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

import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.NodeRangeSelector;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

/**
 * The behavior for the {@link NodeRangeSelector}.
 */
public class NodeRangeSelectorBehavior extends BehaviorBase<NodeRangeSelector> {

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
     * Creates a new behavior for the specified {@link NodeRangeSelector}.
     * 
     * @param selectableImageView
     *            the control which this behavior will control
     */
    public NodeRangeSelectorBehavior(NodeRangeSelector selectableImageView) {
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
     * @return {@link NodeRangeSelector#getSelection()}
     */
    private Rectangle2D getSelection() {
        return getControl().getSelection();
    }

    /**
     * The image's width.
     * 
     * @return {@link Image#getWidth() width} of {@link NodeRangeSelector#getImage()}
     */
    private double getImageWidth() {
        return getControl().getNode().prefWidth(-1);
    }

    /**
     * The image's height.
     * 
     * @return {@link Image#getHeight() height} of {@link NodeRangeSelector#getImage()}
     */
    private double getImageHeight() {
        return getControl().getNode().prefHeight(-1);
    }

    /**
     * Indicates whether the current selection has a fixed ratio.
     * 
     * @return {@link NodeRangeSelector#isSelectionRatioFixed()}
     */
    private boolean isSelectionRatioFixed() {
        return getControl().isSelectionRatioFixed();
    }

    /**
     * The current selection's fixed ratio.
     * 
     * @return {@link NodeRangeSelector#getFixedSelectionRatio()}
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
    public Cursor handleSelectionEvent(MouseEvent selectionEvent) {
        EventType<? extends MouseEvent> eventType = selectionEvent.getEventType();

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
    private Cursor handleMousePressedEvent(MouseEvent selectionEvent) {
        // get all necessary information to create a selection change
        Cursor cursor = getCursor(selectionEvent);
        Rectangle2DChangeStrategy selectionChangeStrategy = getChangeStrategy(selectionEvent);
        boolean deactivateSelectionIfClick = willDeactivateSelectionIfClick(selectionEvent);

        // create and begin the selection change
        Point2D pointInImage = transformToImageCoordiantes(selectionEvent);
        selectionChange = new SelectionChange(getControl(), selectionChangeStrategy, cursor, deactivateSelectionIfClick);
        selectionChange.beginSelectionChange(pointInImage);

        return selectionChange.getCursor();
    }

    /**
     * Handles {@link MouseEvent#MOUSE_DRAGGED} events by continuing the current {@link #selectionChange}.
     * 
     * @param selectionEvent
     *            the handled {@link SelectionEvent}
     * @return the cursor which will be used while the selection changes
     */
    private Cursor handleMouseDraggedEvent(MouseEvent selectionEvent) {
        Point2D pointInImage = transformToImageCoordiantes(selectionEvent);
        selectionChange.continueSelectionChange(pointInImage);
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
    private Cursor handleMouseReleasedEvent(MouseEvent selectionEvent) {
        // end and deactivate the selection change
        Point2D pointInImage = transformToImageCoordiantes(selectionEvent);
        selectionChange.endSelectionChange(pointInImage);
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
    private CoordinatePosition getPosition(MouseEvent selectionEvent) {
        boolean noSelection = !getControl().isSelectionValid() || !getControl().isSelectionActive();
        if (noSelection) {
            return CoordinatePosition.OUT_OF_RECTANGLE;
        }
        
        Point2D pointInImage = transformToImageCoordiantes(selectionEvent);

        CoordinatePosition onEdge = CoordinatePositions
                .onEdges(getSelection(), pointInImage, getTolerance());
        if (onEdge != null) {
            return onEdge;
        } else {
            return CoordinatePositions.inRectangle(getSelection(), pointInImage);
        }
    }

    /**
     * Returns the cursor which will be used for the specified selection event.
     * 
     * @param selectionEvent
     *            the {@link SelectionEvent} to check
     * @return the {@link Cursor} which will be used for the event
     */
    private Cursor getCursor(MouseEvent selectionEvent) {
        CoordinatePosition position = getPosition(selectionEvent);
        switch (position) {
            case IN_RECTANGLE:     return Cursor.MOVE;
            case OUT_OF_RECTANGLE: return Cursor.DEFAULT;
            case NORTH_EDGE:       return Cursor.N_RESIZE;
            case NORTHEAST_EDGE:   return Cursor.NE_RESIZE;
            case EAST_EDGE:        return Cursor.E_RESIZE;
            case SOUTHEAST_EDGE:   return Cursor.SE_RESIZE;
            case SOUTH_EDGE:       return Cursor.S_RESIZE;
            case SOUTHWEST_EDGE:   return Cursor.SW_RESIZE;
            case WEST_EDGE:        return Cursor.W_RESIZE;
            case NORTHWEST_EDGE:   return Cursor.NW_RESIZE;
            default: throw new IllegalArgumentException("The position " + position + " is not fully implemented.");
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
    private Rectangle2DChangeStrategy getChangeStrategy(MouseEvent selectionEvent) {
        boolean mousePressed = selectionEvent.getEventType() == MouseEvent.MOUSE_PRESSED;
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
    private boolean willDeactivateSelectionIfClick(MouseEvent selectionEvent) {
        CoordinatePosition position = getPosition(selectionEvent);
        return position == CoordinatePosition.OUT_OF_RECTANGLE;
    }
    
    /**
     * Transforms the specified x and y coordinates from the mouse node to a point which has the coordinates of the
     * corresponding position in the displayed image (which must not be null).
     * 
     * @param x
     *            the x coordinate within the mouse node
     * @param y
     *            the y coordinate within the mouse node
     * @return a point which represents the specified coordinates in the image
     */
    private Point2D transformToImageCoordiantes(MouseEvent me) {
        final Node n = getControl().getNode();
        
        double nodeWidth = n == null ? 0 : n.prefWidth(-1);
        double nodeHeight = n == null ? 0 : n.prefHeight(-1);
        
        double xRatio = 1;//getControl().getWidth() / nodeWidth;
        double yRatio = 1;//getControl().getHeight() / nodeHeight;

        double xInPicture = MathTools.inInterval(0, me.getX() / xRatio, nodeWidth);
        double yInPicture = MathTools.inInterval(0, me.getY() / yRatio, nodeHeight);

        return new Point2D(xInPicture, yInPicture);
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * Executes the changes from a {@link Rectangle2DChangeStrategy} on a {@link NodeRangeSelector}'s
     * {@link NodeRangeSelector#selectionProperty() selection} property. This includes to check whether the mouse
     * moved from the change's start to end and to possibly deactivate the selection if not.
     */
    private static class SelectionChange {

        // Attributes

        /**
         * The image view whose selection will be changed.
         */
        private final NodeRangeSelector selectableImageView;

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
         * Creates a new selection change for the specified {@link NodeRangeSelector} using the specified
         * {@link Rectangle2DChangeStrategy}.
         * 
         * @param selectableImageView
         *            the {@link NodeRangeSelector} whose selection will be changed
         * @param selectionChangeStrategy
         *            the {@link Rectangle2DChangeStrategy} used to change the selection
         * @param cursor
         *            the {@link Cursor} used during the selection change
         * @param deactivateSelectionIfClick
         *            indicates whether the selection will be deactivated if the change is only a click
         */
        public SelectionChange(NodeRangeSelector selectableImageView, Rectangle2DChangeStrategy selectionChangeStrategy,
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
                selectableImageView.setSelection(null);
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
