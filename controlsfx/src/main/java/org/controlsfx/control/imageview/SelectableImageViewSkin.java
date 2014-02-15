package org.controlsfx.control.imageview;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import org.controlsfx.control.imageview.SelectableImageViewBehavior.SelectionEvent;
import org.controlsfx.tools.MathTools;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 * View for the {@link SelectableImageView}. It displays the image and the selection and manages their positioning.
 * MouseEvents are handed over to the {@link SelectableImageViewBehavior} which uses them to change the selection.
 */
public class SelectableImageViewSkin extends BehaviorSkinBase<SelectableImageView, SelectableImageViewBehavior> {

    /*
     * IMAGE:
     * This skin uses an ImageView to display the Image.
     * 
     * POSITION:
     * The contract states that the image must always be centered within the control. Since a grid pane makes it
     * very easy to define relative positions of its children, it is used to implement this.
     * The grid pane is at the root of this control's scene graph and contains a single child. This child is a
     * single pane which uses absolute positioning for its children. The image view always stays at (0, 0) as the
     * pane is constantly resized to exactly fit the image view.
     * The rectangles marking the selection are positioned by converting the original selection's coordinates,
     * which are relative to the image, to coordinates relative to the image view. To prevent the unselected area's
     * large bounds from messing up the layout, it is not managed by its parent.
     * 
     * MOUSE:
     * To capture mouse events an additional node is added on top of the image view and the selection areas (see
     * below). These events are handed over to the 'SelectableImageViewBehavior' which uses them to determine a
     * cursor and change the selection.
     * 
     * SELECTION:
     * Displaying the selection consists of three parts:
     *  - selected area
     *  - border
     *  - unselected area
     * This is done by using two rectangles with identical size and position. Both are bound to the control's
     * selection-property, which represents the selection in term of the _Image's_ coordinates, in such a way that
     * they represent the selection in term of the _ImageView's_ coordinates.
     * One rectangle is used to display the selected area and its border. The other has its stroke set to such
     * a width that it covers the rest of the ImageView. This means it effectively covers exactly the unselected
     * area.
     * The pane containing these rectangles clips anything it contains to its own bounds.
     * 
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
    private final ImageView imageView;

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
     * Creates a new skin for the specified {@link SelectableImageView}.
     * 
     * @param selectableImageView
     *            the control which this skin will display
     */
    public SelectableImageViewSkin(SelectableImageView selectableImageView) {
        super(selectableImageView, new SelectableImageViewBehavior(selectableImageView));

        this.pane = createClippingPane();
        this.imageView = new ImageView();
        this.selectedArea = new Rectangle();
        this.unselectedArea = new Rectangle();
        this.mouseNode = createMouseNode(pane);

        buildSceneGraph();
        bindImageViewProperties();
        enableResizing();
        initializeAreas();
        listenToMouseEvents();
    }

    /**
     * Creates a pane for the selection which clips its content to its own size.
     * 
     * @return a {@link Pane} with a clip whose width and height is bound to the pane's width and height
     */
    private static Pane createClippingPane() {
        Pane pane = new Pane();

        // create the clipping rectangle which always resizes with the pane
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(pane.widthProperty());
        clip.heightProperty().bind(pane.heightProperty());

        pane.setClip(clip);
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
    private static Node createMouseNode(Region root) {
        Rectangle mouseNode = new Rectangle();

        // make the node transparent and make sure its size does not affect the control's size
        mouseNode.setFill(Color.TRANSPARENT);
        mouseNode.setManaged(false);

        // bind width and height to the root region
        mouseNode.widthProperty().bind(root.widthProperty());
        mouseNode.heightProperty().bind(root.heightProperty());

        return mouseNode;
    }

    /**
     * Builds this skin's scene graph.
     */
    private void buildSceneGraph() {
        // build the scene graph top to bottom

        // create an outer pane which allows the ImageView to always be centered within this control
        // TODO decide whether the alignment should be editable
        GridPane outerPane = new GridPane();
        getChildren().add(outerPane);
        outerPane.setAlignment(Pos.CENTER);

        // add the pane to the outer pane and the other controls to first one
        outerPane.add(pane, 0, 0);
        pane.getChildren().addAll(imageView, unselectedArea, selectedArea, mouseNode);
    }

    /**
     * Some of the {@link SelectableImageView}'s properties origin from {@link ImageView}. Those properties of the
     * {@link #getSkinnable() skinnable} and the {@link #imageView} are bidirectionally bound together.
     */
    private void bindImageViewProperties() {
        SelectableImageView selectableImageView = getSkinnable();
        Bindings.bindBidirectional(imageView.imageProperty(), selectableImageView.imageProperty());
        Bindings.bindBidirectional(imageView.preserveRatioProperty(), selectableImageView.preserveImageRatioProperty());
    }

    /**
     * Binds the {@link #imageView}'s {@link ImageView#fitHeightProperty() fitHeight} and
     * {@link ImageView#fitWidthProperty() fitWidth} properties to he {@link #getSkinnable() skinnable}'s height and
     * width property.
     */
    private void enableResizing() {
        SelectableImageView selectableImageView = getSkinnable();

        // resize all the internal controls when the selectable image view is resized
        selectableImageView.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resize();
            }
        });
        selectableImageView.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resize();
            }
        });
    }

    /**
     * Initializes the {@link #selectedArea} and the {@link #unselectedArea}. This includes their style and their
     * bindings to the {@link SelectableImageView#selectionProperty() selection} property.
     */
    private void initializeAreas() {
        styleAreas();
        bindAreaCoordinatesTogether();
        bindAreaCoordinatesToSelection();
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
     * Binds the visibility, position and size of {@link #selectedArea} to the {@link SelectableImageView}'s
     * {@link SelectableImageView#selectionActiveProperty() selectionActive} and
     * {@link SelectableImageView#selectionProperty() selection} propertyies.
     */
    private void bindAreaCoordinatesToSelection() {
        selectedArea.visibleProperty().bind(getSkinnable().selectionActiveProperty());
        unselectedArea.visibleProperty().bind(getSkinnable().selectionActiveProperty());

        getSkinnable().selectionProperty().addListener(new ChangeListener<Rectangle2D>() {
            @Override
            public void changed(ObservableValue<? extends Rectangle2D> observable, Rectangle2D oldValue,
                    Rectangle2D newValue) {
                updateSelection();
            }
        });
    }

    /**
     * Lets this control listen to all relevant mouse events.
     */
    private void listenToMouseEvents() {
        mouseNode.addEventHandler(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                handleMouseEvent(event);
            }
        });
    }

    /* ************************************************************************
     *                                                                         *
     * Resizing                                                                *
     *                                                                         *
     **************************************************************************/

    /*
     * When the control is resized, the following steps are taking place:
     *  - the ImageView's fitWidth and fitHeight properties are explicitly set to the new size
     *  - this triggers a resize of the containing pane since the view is the largest control in it
     *  - by bindings the resizing pane resizes the clipping rectangle and the unselected area's stroke width
     * This brings the control to a coherent visual state but the selection is displayed relative to the old size,
     * so it must be updates as well.
     */

    /**
     * Resizes the contained controls when the control itself is resized.
     */
    private void resize() {
        imageView.setFitWidth(getSkinnable().getWidth());
        imageView.setFitHeight(getSkinnable().getHeight());

        updateSelection();
    }

//    /**
//     * Prints the sizes of the most relevant controls to {@link System#out}. Can be used to debug the control.
//     */
//    private void outputSizes() {
//        System.out.println("Scene: " + getNode().getScene().getWidth() + " x " + getNode().getScene().getHeight());
//        System.out.println("Selectable Image View: " + getSkinnable().getWidth() + " x " + getSkinnable().getHeight());
//        System.out.println("Pane: " + pane.getWidth() + " x " + pane.getHeight());
//        System.out.println("Image View: " + imageView.getFitWidth() + " x " + imageView.getFitHeight());
//        System.out.println("Image View local bounds: " + imageView.getBoundsInLocal().getWidth() + " x "
//                + imageView.getBoundsInLocal().getHeight());
//        System.out.println("Selection: " + selectedArea.getWidth() + " x " + selectedArea.getHeight() + " at ("
//                + selectedArea.getX() + ", " + selectedArea.getY() + ")");
//        System.out.println();
//    }

    /* ************************************************************************
     *                                                                         *
     * Selection                                                               *
     *                                                                         *
     **************************************************************************/

    /**
     * Returns the value contained in the {@link SelectableImageView}'s {@link SelectableImageView#selectionProperty()
     * selection} property.
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
        if (imageView.getImage() == null)
            setSelectionDirectly(0, 0, 0, 0);
        else if (getImageSelection() == null)
            setSelectionDirectly(0, 0, 0, 0);
        else
            // in this case, the image and the selection are not-null, so the selection can be properly displayed
            setTransformedSelection();
    }

    /**
     * Transforms the {@link #getImageSelection() image selection} from Image coordinates to ImageView coordinates and
     * sets the position and size of {@link #selectedArea} (and by binding that of {@link #unselectedArea}). <br>
     * This call will fail if the {@link #imageView imageView.get()} or {@link #getImageSelection()} returns
     * {@code null}.
     */
    private void setTransformedSelection() {
        // get the image view's width and height and compute the ratio between the image size and the view's size
        double imageViewWidth = imageView.getBoundsInLocal().getWidth();
        double imageViewHeight = imageView.getBoundsInLocal().getHeight();
        double widthRatio = imageViewWidth / imageView.getImage().getWidth();
        double heightRatio = imageViewHeight / imageView.getImage().getHeight();

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
     * @param newX
     *            the new x coordinate of the upper left corner
     * @param newY
     *            the new y coordinate of the upper left corner
     * @param newWidth
     *            the new width
     * @param newHeight
     *            the new height
     */
    private void setSelectionDirectly(double newX, double newY, double newWidth, double newHeight) {
        selectedArea.setX(newX);
        selectedArea.setY(newY);
        selectedArea.setWidth(newWidth);
        selectedArea.setHeight(newHeight);
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
        boolean imageExists = imageView.getImage() != null;
        if (imageExists) {
            Point2D pointInImage = transformToImageCoordiantes(event.getX(), event.getY());
            SelectionEvent selectionEvent = new SimpleSelectionEvent(event, pointInImage);
            newCursor = getBehavior().handleSelectionEvent(selectionEvent);
        } else
            newCursor = Cursor.DEFAULT;

        mouseNode.setCursor(newCursor);
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
    private Point2D transformToImageCoordiantes(double x, double y) {
        double xRatio = mouseNode.getBoundsInParent().getWidth() / imageView.getImage().getWidth();
        double yRatio = mouseNode.getBoundsInParent().getHeight() / imageView.getImage().getHeight();

        double xInPicture = MathTools.inInterval(0, x / xRatio, imageView.getImage().getWidth());
        double yInPicture = MathTools.inInterval(0, y / yRatio, imageView.getImage().getHeight());

        return new Point2D(xInPicture, yInPicture);
    }

    /* ************************************************************************
     *                                                                         *
     * Private Classes                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Simple implementation of {@link SelectableImageViewBehavior.SelectionEvent}.
     */
    private class SimpleSelectionEvent implements SelectableImageViewBehavior.SelectionEvent {

        /**
         * The mouse event.
         */
        private final MouseEvent mouseEvent;

        /**
         * The event's x/y-coordinates transformed to a point in the image.
         */
        private final Point2D pointInImage;

        /**
         * Creates a new selection event with the specified arguments.
         * 
         * @param mouseEvent
         *            the mouse event
         * @param pointInImage
         *            the event's x/y-coordinates transformed to a point in the image
         */
        public SimpleSelectionEvent(MouseEvent mouseEvent, Point2D pointInImage) {
            super();
            this.mouseEvent = mouseEvent;
            this.pointInImage = pointInImage;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MouseEvent getMouseEvent() {
            return mouseEvent;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Point2D getPointInImage() {
            return pointInImage;
        }

    }
}
