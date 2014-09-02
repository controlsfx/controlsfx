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

import java.util.function.Consumer;

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
 * View for the {@link SnapshotView}. It displays the node and the selection and manages their positioning. MouseEvents
 * are handed over to the {@link SnapshotViewBehavior} which uses them to change the selection.
 */
public class SnapshotViewSkin extends BehaviorSkinBase<SnapshotView, SnapshotViewBehavior> {

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * The currently displayed node; when it changes {@link #updateNode() updateNode} will set the new one.
     */
    private Node node;

    /**
     * The pane displaying the {@link #node}.
     */
    private final GridPane gridPane;

    /**
     * The rectangle which represents the selected area.
     */
    private final Rectangle selectedArea;

    /**
     * The rectangle whose stroke represents the unselected area. Note that binding is used to ensure that the rectangle
     * itself always has the same size and position as the {@link #selectedArea}.
     */
    private final Rectangle unselectedArea;

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
    public SnapshotViewSkin(SnapshotView snapshotView, Consumer<Boolean> setSelectionChanging) {

        super(snapshotView, new SnapshotViewBehavior(snapshotView, setSelectionChanging));

        this.gridPane = createGridPane();
        this.selectedArea = new Rectangle();
        this.unselectedArea = new Rectangle();
        this.mouseNode = createMouseNode();

        buildSceneGraph();
        initializeAreas();

        registerChangeListener(snapshotView.nodeProperty(), "NODE");
        registerChangeListener(snapshotView.selectionProperty(), "SELECTION");
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);

        if ("NODE".equals(p)) {
            updateNode();
        } else if ("SELECTION".equals(p)) {
            updateSelection();
        }
    }

    private static GridPane createGridPane() {
        GridPane pane = new GridPane();
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

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
    @SuppressWarnings("unused")
    private void styleAreas() {
        selectedArea.fillProperty().bind(getSkinnable().selectionFillProperty());
        selectedArea.strokeProperty().bind(getSkinnable().selectionStrokeProperty());
        selectedArea.strokeWidthProperty().bind(getSkinnable().selectionStrokeWidthProperty());
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

        // clip the unselected area according to the view's property - this is done by a designated inner class
        new Clipper(getSkinnable(), unselectedArea);
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
    private void bindAreaVisibilityToSelection() {
        ReadOnlyBooleanProperty selectionExists = getSkinnable().hasSelectionProperty();
        ReadOnlyBooleanProperty selectionActive = getSkinnable().selectionActiveProperty();
        BooleanBinding existsAndActive = Bindings.and(selectionExists, selectionActive);

        selectedArea.visibleProperty().bind(existsAndActive);
        unselectedArea.visibleProperty().bind(existsAndActive);
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
     * Updates the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}). This
     * needs to be done whenever either the node, selection or control size changes.
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
     * Handles {@link MouseEvent}s.
     * 
     * @param event
     *            the event to handle
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

    private static class Clipper {

        private final SnapshotView snapshotView;

        private final Node clippedNode;

        private final Rectangle controlClip;

        private final Rectangle nodeClip;

        private final ChangeListener<Bounds> updateControlClipToNodeBounds;

        private final ChangeListener<Bounds> updateNodeClipToNodeBounds;

        public Clipper(SnapshotView snapshotView, Node clippedNode) {
            this.snapshotView = snapshotView;
            this.clippedNode = clippedNode;

            // for 'CONTROL', clip to the control's bounds
            controlClip = new Rectangle();
            updateControlClipToNodeBounds = (o, oldBounds, newBounds) -> resizeRectangleToBounds(controlClip, newBounds);

            // for 'NODE', clip to the node's bounds
            nodeClip = new Rectangle();
            // create the listener which will resize the rectangle
            updateNodeClipToNodeBounds = (o, oldBounds, newBounds) -> resizeRectangleToBounds(nodeClip, newBounds);

            // set the clipping and keep updating it
            setClipping();
            snapshotView.unselectedAreaBoundaryProperty().addListener((o, oldBoundary, newBoundary) -> setClipping());
        }

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
                throw new IllegalArgumentException("The boundary " + boundary + " is not fully implemented.");
            }
        }

        private void clipToControl() {
            // stop resizing the node clip
            updateNodeClipToChangingNode(snapshotView.nodeProperty(), snapshotView.getNode(), null);

            // resize the control clip and keep doing so
            resizeRectangleToBounds(controlClip, snapshotView.getBoundsInLocal());
            snapshotView.boundsInLocalProperty().addListener(updateControlClipToNodeBounds);

            // set the control
            clippedNode.setClip(controlClip);
        }

        private void clipToNode() {
            // update the node clip to the new bounds and whenever the node changes its bounds
            updateNodeClipToChangingNode(snapshotView.nodeProperty(), null, snapshotView.getNode());
            // move that listener from old to new nodes
            snapshotView.nodeProperty().addListener(this::updateNodeClipToChangingNode);

            // set the clip
            clippedNode.setClip(nodeClip);
        }

        private void updateNodeClipToChangingNode(ObservableValue<? extends Node> o, Node oldNode, Node newNode) {
            // resize the rectangle to match the new node
            resizeRectangleToNodeBounds(nodeClip, newNode);

            // move the listener from one node to the next
            if (oldNode != null) {
                oldNode.boundsInParentProperty().removeListener(updateNodeClipToNodeBounds);
            }
            if (newNode != null) {
                newNode.boundsInParentProperty().addListener(updateNodeClipToNodeBounds);
            }
        }

        private static void resizeRectangleToNodeBounds(Rectangle rectangle, Node node) {
            if (node == null) {
                resizeRectangleToZero(rectangle);
            } else {
                resizeRectangleToBounds(rectangle, node.getBoundsInParent());
            }
        }

        private static void resizeRectangleToZero(Rectangle rectangle) {
            rectangle.setX(0);
            rectangle.setY(0);
            rectangle.setWidth(0);
            rectangle.setHeight(0);
        }

        private static void resizeRectangleToBounds(Rectangle rectangle, Bounds bounds) {
            rectangle.setX(bounds.getMinX());
            rectangle.setY(bounds.getMinY());
            rectangle.setWidth(bounds.getWidth());
            rectangle.setHeight(bounds.getHeight());
        }

    }

}
