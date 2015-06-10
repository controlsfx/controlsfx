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
package impl.org.controlsfx.skin;

import impl.org.controlsfx.behavior.SnapshotViewBehavior;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import org.controlsfx.control.SnapshotView;
import org.controlsfx.control.SnapshotView.Boundary;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * View for the {@link SnapshotView}. It displays the node and the selection and manages their positioning. Mouse events
 * are handed over to the {@link SnapshotViewBehavior} which uses them to change the selection.
 */
public class SnapshotViewSkin extends BehaviorSkinBase<SnapshotView, SnapshotViewBehavior> {

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

        super(snapshotView, new SnapshotViewBehavior(snapshotView));

        this.gridPane = createGridPane();
        this.selectedArea = new Rectangle();
        this.unselectedArea = new Rectangle();
        this.mouseNode = createMouseNode();

        buildSceneGraph();
        initializeAreas();

        registerChangeListener(snapshotView.nodeProperty(), "NODE"); //$NON-NLS-1$
        registerChangeListener(snapshotView.selectionProperty(), "SELECTION"); //$NON-NLS-1$
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);

        if ("NODE".equals(p)) { //$NON-NLS-1$
            updateNode();
        } else if ("SELECTION".equals(p)) { //$NON-NLS-1$
            updateSelection();
        }
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
        Cursor newCursor = getBehavior().handleMouseEvent(event);
        mouseNode.setCursor(newCursor);
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

}
