package org.controlsfx.control.imageview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

import org.controlsfx.tools.MathTools;

public class SelectableImageView extends Control {

    /* ************************************************************************
     *                                                                         *
     * Attributes & Properties                                                 *
     *                                                                         *
     **************************************************************************/

    // IMAGE VIEW

    /**
     * The {@link Image} to be painted by this {@code SelectableImageView}.
     */
    private final ObjectProperty<Image> image;

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     */
    private final BooleanProperty preserveRatio;

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. <br>
     * The value is only well defined if {@link #selectionActive} is true.
     */
    private final Rectangle selection;

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be evaluated
     * as its state is undefined.
     */
    private final BooleanProperty selectionActive;

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
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
        this.image = new SimpleObjectProperty<Image>(this, "imageProperty");
        this.preserveRatio = new SimpleBooleanProperty(this, "preserveRatioProperty", false);

        // Selection
        this.selection = new Rectangle();
        this.selectionActive = new SimpleBooleanProperty(this, "selectionActiveProperty", false);
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

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     * 
     * @return the preserveRatio as a property
     */
    public BooleanProperty preserveRatioProperty() {
        return preserveRatio;
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     * 
     * @return the preserveRatio
     */
    public boolean isPreserveRatio() {
        return preserveRatioProperty().get();
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     * 
     * @param preserveRatio
     *            the preserveRatio to set
     */
    public void setPreserveRatio(boolean preserveRatio) {
        preserveRatioProperty().set(preserveRatio);
    }

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are relative to the currently shown image. <br>
     * This method will always return the same instance so it is safe to bind to its properties without considering it
     * changing. It will never be null but the value is only well defined if {@link selectionActive} is true.
     * 
     * @return the selection
     */
    public Rectangle getSelection() {
        return selection;
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be evaluated
     * as its state is undefined.
     * 
     * @return the selectionActive as a property
     */
    public BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be evaluated
     * as its state is undefined.
     * 
     * @return whether the selection is active
     */
    public boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be evaluated
     * as its state is undefined.
     * 
     * @param selectionActive
     *            the new selection active status
     */
    public void setSelectionActive(boolean selectionActive) {
        selectionActiveProperty().set(selectionActive);
    }

    /**
     * Sets the selection. The coordinates will be interpreted relative to the currently shown image but might be
     * modified to fit the currently shown image's size. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), an
     * {@link IllegalStateException} will be thrown.
     * 
     * @param upperLeftX
     *            the new x coordinate of the selection's upper left corner
     * @param upperLeftY
     *            the new y coordinate of the selection's upper left corner
     * @param width
     *            the selection's new width
     * @param height
     *            the selection's new height
     * @throws IllegalStateException
     *             if there is no current image, i.e. the {@link #imageProperty()} holds {@code null}
     */
    public void setSelection(double upperLeftX, double upperLeftY, double width, double height)
            throws IllegalStateException {

        if (getImage() == null)
            throw new IllegalStateException("If the imageProperty holds null, the selection can not be set.");

        double imageWidth = getImage().getWidth();
        double imageHeight = getImage().getHeight();

        double correctedUpperLeftX = MathTools.inInterval(0, upperLeftX, imageWidth);
        getSelection().setX(correctedUpperLeftX);
        double correctedUpperLeftY = MathTools.inInterval(0, upperLeftY, imageHeight);
        getSelection().setY(correctedUpperLeftY);

        double correctedWidth = MathTools.inInterval(0, width - correctedUpperLeftX, imageWidth);
        getSelection().setWidth(correctedWidth);
        double correctedHeight = MathTools.inInterval(0, width - correctedUpperLeftX, imageWidth);
        getSelection().setHeight(correctedHeight);
    }

    /**
     * Uses the specified rectangle as a template to set the selection. This means that the specified instance itself
     * will *not* be the new selection. Instead its coordinates will be used. They will be interpreted relative to the
     * currently shown image but might be modified to fit the currently shown image's size. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), an
     * {@link IllegalStateException} will be thrown.
     * 
     * @param selectionTemplate
     *            the template for the new selection
     * @throws IllegalStateException
     *             if there is no current image, i.e. the {@link #imageProperty()} holds {@code null}
     */
    public void setSelectionFromTemplate(Rectangle selectionTemplate) throws IllegalStateException {
        setSelection(selectionTemplate.getX(), selectionTemplate.getY(), selectionTemplate.getWidth(),
                selectionTemplate.getHeight());
    }

    /**
     * Uses the specified rectangle as a template to set the selection. This means that the specified instance itself
     * will *not* be the new selection. Instead its coordinates will be used. They will be interpreted relative to the
     * currently shown image but might be modified to fit the currently shown image's size. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), an
     * {@link IllegalStateException} will be thrown.
     * 
     * @param selectionTemplate
     *            the template for the new selection
     * @throws IllegalStateException
     *             if there is no current image, i.e. the {@link #imageProperty()} holds {@code null}
     */
    public void setSelectionFromTemplate(Rectangle2D selectionTemplate) throws IllegalStateException {
        setSelection(selectionTemplate.getMinX(), selectionTemplate.getMinY(), selectionTemplate.getWidth(),
                selectionTemplate.getHeight());
    }

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     * 
     * @return the selectionAreaChanging as a property
     */
    public BooleanProperty selectionAreaChangingProperty() {
        return selectionAreaChanging;
    }

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     * 
     * @return the selectionAreaChanging
     */
    public boolean isSelectionAreaChanging() {
        return selectionAreaChangingProperty().get();
    }

    /* ************************************************************************
     *                                                                         *
     * Private Classes                                                         *
     *                                                                         *
     **************************************************************************/

}
