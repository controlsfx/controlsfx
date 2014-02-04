package org.controlsfx.control.imageview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public class SelectableImageView extends Control {

    /**
     * The rectangle which is used when nothing is selected.
     */
    private static final Rectangle NO_SELECTION = new Rectangle();

    /* ************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/

    // IMAGE VIEW

    /**
     * The height of the bounding box within which the source image is resized as necessary to fit.
     */
    private final DoubleProperty fitHeight;

    /**
     * The width of the bounding box within which the source image is resized as necessary to fit.
     */
    private final DoubleProperty fitWidth;

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     */
    private final ObjectProperty<Image> image;

    // SELECTION

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link selectedArea} should not be
     * evaluated as its state is undefined.
     */
    private final BooleanProperty selectionActive;

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. This will never be
     * null but the value is only well defined if {@link selectionActive} is true.
     */
    private final ObjectProperty<Rectangle> selectedArea;

    /**
     * Indicates whether the {@link #selectedAreaProperty() selectedArea} property is currently changing. This will be
     * set to true when changing the selection begins and set to false when it ends.
     */
    private final BooleanProperty selectionAreaChanging;

    /* ************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new SelectableImageView.
     */
    public SelectableImageView() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        // Image View
        this.fitHeight = new SimpleDoubleProperty(this, "fitHeightProperty");
        this.fitWidth = new SimpleDoubleProperty(this, "fitWidthProperty");
        this.image = new SimpleObjectProperty<Image>(this, "imageProperty");

        // Selection
        this.selectionActive = new SimpleBooleanProperty(this, "selectionActiveProperty", false);
        this.selectedArea = new SimpleObjectProperty<Rectangle>(this, "selectedAreaProperty", NO_SELECTION);
        this.selectionAreaChanging = new SimpleBooleanProperty(this, "selectionAreaChangingProperty", false);
    }

    /**
     * Creates a new SelectableImageView using the specified image.
     * 
     * @param image
     *            the image to show after construction
     */
    public SelectableImageView(Image image) {
        this();
        setImage(image);
    }

    /**
     * Creates a new SelectableImageView using the image loaded from the specified URL.
     * 
     * @param url
     *            the string representing the URL from which to load the image
     */
    public SelectableImageView(String url) {
        this();
        Image image = new Image(url);
        setImage(image);
    }

    /* ************************************************************************
     *                                                                         *
     * Style Sheet & Skin Handling                                             *
     *                                                                         *
     **************************************************************************/

    /**
     * The name of the style class used in CSS for instances of this class.
     */
    private static final String DEFAULT_STYLE_CLASS = "selectable-image-view";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAgentStylesheet() {
        return getClass().getResource("selectableimageview.css").toExternalForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new SelectableImageViewSkin(this);
    }

    /* ************************************************************************
     *                                                                         *
     * Property Access                                                         *
     *                                                                         *
     **************************************************************************/

    // IMAGE VIEW

    /**
     * The height of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @return the fitHeight as a property
     */
    public DoubleProperty fitHeightProperty() {
        return fitHeight;
    }

    /**
     * The height of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @return the fitHeight
     */
    public double getFitHeight() {
        return fitHeightProperty().get();
    }

    /**
     * The height of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @param fitHeight
     *            the fitHeight to set
     */
    public void setFitHeight(double fitHeight) {
        fitHeightProperty().set(fitHeight);
    }

    /**
     * The width of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @return the fitWidth as a property
     */
    public DoubleProperty fitWidthProperty() {
        return fitWidth;
    }

    /**
     * The width of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @return the fitWidth
     */
    public double getFitWidth() {
        return fitWidthProperty().get();
    }

    /**
     * The width of the bounding box within which the source image is resized as necessary to fit.
     * 
     * @param fitWidth
     *            the fitWidth to set
     */
    public void setFitWidth(double fitWidth) {
        fitWidthProperty().set(fitWidth);
    }

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @return the image as a property
     */
    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @return the image
     */
    public Image getImage() {
        return imageProperty().get();
    }

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     * 
     * @param image
     *            the image to set
     */
    public void setImage(Image image) {
        imageProperty().set(image);
    }

    // SELECTION

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selectedAreaProperty()
     * selectedArea} property should not be evaluated as its state is undefined.
     * 
     * @return the selectionActive as a property
     */
    public BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selectedAreaProperty()
     * selectedArea} should not be evaluated as its state is undefined.
     * 
     * @return the selectionActive
     */
    public boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selectedAreaProperty()
     * selectedArea} should not be evaluated as its state is undefined.
     * 
     * @param selectionActive
     *            the selectionActive to set
     */
    public void setSelectionActive(boolean selectionActive) {
        selectionActiveProperty().set(selectionActive);
    }

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. This will never be
     * null but the value is only used when {@link #selectionActiveProperty() selectionActive} property is true.
     * 
     * @return the selectedArea as a property
     */
    public ObjectProperty<Rectangle> selectedAreaProperty() {
        return selectedArea;
    }

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. This will never be
     * null but the value is only used when {@link #selectionActiveProperty() selectionActive} property is true.
     * 
     * @return the selectedArea
     */
    public Rectangle getSelectedArea() {
        return selectedAreaProperty().get();
    }

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. This will never be
     * null but the value is only used when {@link #selectionActiveProperty() selectionActive} property is true.
     * 
     * @param selectedArea
     *            the selectedArea to set
     */
    public void setSelectedArea(Rectangle selectedArea) {
        selectedAreaProperty().set(selectedArea);
    }

    /**
     * Indicates whether the {@link #selectedAreaProperty() selectedArea} property is currently changing. This will be
     * set to true when changing the selection begins and set to false when it ends.
     * 
     * @return the selectionAreaChanging as a property
     */
    public BooleanProperty selectionAreaChangingProperty() {
        return selectionAreaChanging;
    }

    /**
     * Indicates whether the {@link #selectedAreaProperty() selectedArea} property is currently changing. This will be
     * set to true when changing the selection begins and set to false when it ends.
     * 
     * @return the selectionAreaChanging
     */
    public boolean isSelectionAreaChanging() {
        return selectionAreaChangingProperty().get();
    }

    /**
     * Indicates whether the {@link #selectedAreaProperty() selectedArea} property is currently changing. This will be
     * set to true when changing the selection begins and set to false when it ends.
     * 
     * @param selectionAreaChanging
     *            the selectionAreaChanging to set
     */
    public void setSelectionAreaChanging(boolean selectionAreaChanging) {
        selectionAreaChangingProperty().set(selectionAreaChanging);
    }

    /* ************************************************************************
     *                                                                         *
     * Private Classes                                                         *
     *                                                                         *
     **************************************************************************/

}
