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
import impl.org.controlsfx.tools.MathTools;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import org.controlsfx.control.SnapshotView;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * View for the {@link SnapshotView}. It displays the image and the selection and manages their positioning.
 * MouseEvents are handed over to the {@link SnapshotViewBehavior} which uses them to change the selection.
 */
public class SnapshotViewSkin extends BehaviorSkinBase<SnapshotView, SnapshotViewBehavior> {
    
    static {
        // refer to ControlsFXControl for why this is necessary
        StyleManager.getInstance().addUserAgentStylesheet(
                SnapshotView.class.getResource("snapshot-view.css").toExternalForm());
    }

    /*
     * IMAGE:
     * This skin uses an ImageView to display the Image.
     * 
     * POSITION:
     * The contract states that the image must always be centered within the control. Since a grid pane makes it very
     * easy to define relative positions of its children, it is used to implement this.
     * The grid pane is at the root of this control's scene graph and contains a single child. This child is a simple
     * 'Pane' which uses absolute positioning for its children. The image view always stays at (0, 0) as the pane is
     * constantly resized to exactly fit the image view.
     * The rectangles marking the selection (see below) are positioned by converting the original selection's
     * coordinates, which are relative to the image, to coordinates relative to the image view. To prevent the
     * unselected area's large bounds from messing up the layout, it is not managed by its parent.
     * 
     * FIXME There seems to be a problem when the control could grow. At the moment it does not behave well when it
     * shares a 'VBox' with another control. In that case it shrinks if images change but never grows. Didn't have time
     * to test this more thoroughly.
     * 
     * MOUSE:
     * To capture mouse events an additional node is added on top of the image view and the selection areas (see
     * below). These events are handed over to the 'SnapshotViewBehavior' which uses them to determine a cursor
     * and change the selection.
     * 
     * SELECTION:
     * Displaying the selection consists of three parts:
     *  - selected area
     *  - border
     *  - unselected area
     * This is done by using two rectangles with identical size and position. Both are bound to the control's
     * 'selectionProperty', which represents the selection in term of the _Image's_ coordinates, in such a way that
     * they represent the selection in term of the _ImageView's_ coordinates.
     * One rectangle is used to display the selected area and its border. The other has its stroke set to such a width
     * that it covers the rest of the ImageView. This means it effectively covers exactly the unselected area.
     * The pane containing these rectangles clips anything it contains to its own bounds.
     * 
     * VALID & ACTIVE
     * The selection areas visibility is bound to the Boolean term (selectionValid && selectionActive). Their position
     * and size is updated whenever the image or the selection changes unless their combination is invalid.
     * So a valid selection is always properly represented but only visible if the selection is active. An invalid
     * selection can not be properly represented and is hence set to an arbitrary value like (0, 0, 0, 0). If a
     * becomes valid, the visibility changes but size and position are not explicitly updated. This is not necessary
     * because a selection's validity can only change if either the image or the selection does and this case is
     * already covered.
     */

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    /**
     * The pane displaying the {@link #imageView} and the selection areas.
     */
    private final Pane pane;

    /**
     * The image view which displays the image.
     */
    private Node node;

    /**
     * The rectangle which represents the selected area.
     */
    private final Rectangle selectedArea;

    /**
     * The rectangle whose stroke represents the unselected area. Note that binding is used to ensure that the rectangle
     * itself always has the same size and position as the {@link #selectedArea}.
     */
    private final Rectangle unselectedArea;

    /**
     * A node which exactly overlays the {@link #imageView} and captures mouse events.
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
     *            the control which this skin will display
     */
    public SnapshotViewSkin(SnapshotView snapshotView) {
        super(snapshotView, new SnapshotViewBehavior(snapshotView));

        this.pane = createClippingPane();
        this.selectedArea = new Rectangle();
        this.unselectedArea = new Rectangle();
        this.mouseNode = createMouseNode(pane);

        buildSceneGraph();
        initializeAreas();
        
        registerChangeListener(snapshotView.nodeProperty(), "NODE");
        registerChangeListener(snapshotView.selectionProperty(), "SELECTION");
        registerChangeListener(snapshotView.widthProperty(), "WIDTH");
        registerChangeListener(snapshotView.heightProperty(), "HEIGHT");
    }
    
    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("NODE".equals(p)) {
            updateNode();
            updateSelection();
        } else if ("WIDTH".equals(p) || "HEIGHT".equals(p)) {
            updateSelection();
        } else if ("SELECTION".equals(p)) {
            updateSelection();
        }
    }

    /**
     * Creates a pane for the selection which clips its content to its own size.
     * 
     * @return a {@link Pane} with a clip whose width and height is bound to the pane's width and height
     */
    private static Pane createClippingPane() {
        Pane pane = new Pane();

        // create the clipping rectangle which always resizes with the pane
//        Rectangle clip = new Rectangle();
//        clip.widthProperty().bind(pane.widthProperty());
//        clip.heightProperty().bind(pane.heightProperty());

//        pane.setClip(clip);
        return pane;
    }

    /**
     * Creates a {@link Region} which resizes together with the specified root.
     * 
     * @param root
     *            the region which determines the returned region's size
     * @return a region whose {@link Region#widthProperty() width} and {@link Region#heightProperty() height} properties
     *         are bound to the root's width and height
     */
    private Node createMouseNode(Region root) {
        Rectangle mouseNode = new Rectangle();

        // make the node transparent and make sure its size does not affect the control's size
        mouseNode.setFill(Color.TRANSPARENT);
        mouseNode.setManaged(false);

        // bind width and height to the root region
        mouseNode.widthProperty().bind(root.widthProperty());
        mouseNode.heightProperty().bind(root.heightProperty());
        
        mouseNode.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                handleMouseEvent(event);
            }
        });

        return mouseNode;
    }

    /**
     * Builds this skin's scene graph.
     */
    private void buildSceneGraph() {
        // build the scene graph top to bottom

        // create an outer pane which allows the ImageView to always be centered within this control
        GridPane outerPane = new GridPane();
        getChildren().add(outerPane);
        outerPane.setAlignment(Pos.CENTER);

        // add the pane to the outer pane and the other controls to first one
        outerPane.add(pane, 0, 0);
        
        pane.getChildren().addAll(unselectedArea, selectedArea, mouseNode);
        updateNode();
    }
    
    private void updateNode() {
        if (node != null) {
            pane.getChildren().remove(node);
        }

        node = getSkinnable().getNode();
        if (node != null) {
            pane.getChildren().add(0, node);
        }
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
        // TODO: use CSS

        selectedArea.setFill(Color.TRANSPARENT);
        selectedArea.setStroke(Color.WHITESMOKE);
        selectedArea.setStrokeWidth(2d);
        selectedArea.setStrokeType(StrokeType.OUTSIDE);
        // if the control's layout depends on this rectangle,
        // the stroke's width messes up the layout if the selection is on the pane's edge
        selectedArea.setManaged(false);

        unselectedArea.setFill(Color.TRANSPARENT);
        unselectedArea.setStroke(new Color(0, 0, 0, 0.5));
        unselectedArea.strokeWidthProperty().bind(Bindings.max(pane.widthProperty(), pane.heightProperty()));
        unselectedArea.setStrokeType(StrokeType.OUTSIDE);
        // this call is crucial! it prevents the enormous unselected area from messing up the layout
        unselectedArea.setManaged(false);
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
     * Binds the visibility of {@link #selectedArea} and {@link #unselectedArea unselected} to the
     * {@link SnapshotView}'s {@link SnapshotView#selectionActiveProperty() selectionActive} and
     * {@link SnapshotView#selectionValidProperty() selectionValid} properties.
     */
    private void bindAreaVisibilityToSelection() {
        ReadOnlyBooleanProperty selectionValid = getSkinnable().selectionValidProperty();
        ReadOnlyBooleanProperty selectionActive = getSkinnable().selectionActiveProperty();
        BooleanBinding validAndVisible = Bindings.and(selectionValid, selectionActive);

        selectedArea.visibleProperty().bind(validAndVisible);
        unselectedArea.visibleProperty().bind(validAndVisible);
    }


    /* ************************************************************************
     *                                                                         *
     * Selection                                                               *
     *                                                                         *
     **************************************************************************/

    /**
     * Usability method. Returns the value contained in the {@link SnapshotView}'s
     * {@link SnapshotView#selectionProperty() selection} property.
     * 
     * @return the image's selection
     */
    private Rectangle2D getImageSelection() {
        return getSkinnable().getSelection();
    }

    /**
     * Updates the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}). This
     * needs to be done whenever either the image, selection or control size changes.
     */
    private void updateSelection() {
        boolean showSelection =
                getSkinnable().getNode() != null && getSkinnable().isSelectionValid();

        if (showSelection) {
            // the selection can be properly displayed
            setTransformedSelection();
        } else {
            // in this case the selection areas are invisible,
            // so the only thing left to do is to make sure their coordinates are not all over the place
            // (this is not strictly necessary but makes the skin's state cleaner)
            setSelectionDirectly(0, 0, 0, 0);
        }
    }

    /**
     * Transforms the {@link #getImageSelection() image selection} from Image coordinates to ImageView coordinates and
     * sets the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}). <br>
     * This call will fail if the {@link #imageView imageView.get()} or {@link #getImageSelection()} returns
     * {@code null}.
     */
    private void setTransformedSelection() {
        // get the image view's width and height and compute the ratio between the image size and the view's size
        Node n = getSkinnable().getNode();
        double imageViewWidth = n == null ? 0 : n.getBoundsInLocal().getWidth();
        double imageViewHeight = n == null ? 0 : n.getBoundsInLocal().getHeight();
        double widthRatio = n == null ? 0 : imageViewWidth / n.prefWidth(-1);
        double heightRatio = n == null ? 0 : imageViewHeight / n.prefHeight(-1);

        // compute the new position and size such that it is always within the image view's area
        Rectangle2D selection = getImageSelection();
        double newX = MathTools.inInterval(0, widthRatio * selection.getMinX(), imageViewWidth);
        double newY = MathTools.inInterval(0, heightRatio * selection.getMinY(), imageViewHeight);
        double newWidth = widthRatio * selection.getWidth();
        boolean tooWide = newX + newWidth > imageViewWidth;
        if (tooWide)
            newWidth = imageViewWidth - newX;
        double newHeight = heightRatio * selection.getHeight();
        boolean tooHigh = newY - newHeight > imageViewHeight;
        if (tooHigh)
            newHeight = imageViewHeight - newY;

        // set the new values
        setSelectionDirectly(newX, newY, newWidth, newHeight);
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
    private void setSelectionDirectly(double x, double y, double width, double height) {
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
        Cursor newCursor;
        boolean imageExists = getSkinnable().getNode() != null;
        if (imageExists) {
            newCursor = getBehavior().handleSelectionEvent(event);
        } else {
            newCursor = Cursor.DEFAULT;
        }

        mouseNode.setCursor(newCursor);
    }
}
