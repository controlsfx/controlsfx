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
import org.controlsfx.tools.Shapes;

/**
 * <b>SELECTION</b> <br>
 * <br>
 * 
 * <b>Model</b> <br>
 * The model for the selection can be accessed with {@link SelectableImageView#getSelection()}. This method always
 * returns the same {@link Rectangle} instance so it is safe to bind to its values.<br>
 * <br>
 * 
 * <b>Active</b> <br>
 * The {@link SelectableImageView#selectionActiveProperty() selectionActive} property indicates whether the selection is
 * currently active. It is only displayed by the control if and only if it is active.<br>
 * The selection is automatically deactivated when a new image is set (more precisely: <i>before</i> the
 * {@link SelectableImageView#imageProperty() image} property changes) and automatically activated when a selection is
 * explicitly set via the {@code setSelection}-methods (more precisely: <i>after</i> the
 * {@link SelectableImageView#getSelection() selection} was updated to the new values). <br>
 * If it is deactivated, the {@link SelectableImageView#getSelection() selection} is in an undefined state and should
 * not be accessed.
 */
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
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be accessed
     * as its state is undefined.
     */
    private final BooleanProperty selectionActive;

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     */
    private final BooleanProperty selectionChanging;

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
        this.image = new SimpleObjectProperty<Image>(this, "imageProperty") {
            @Override
            public void set(Image newValue) {
                // deactivate the selection when a new image is set
                setSelectionActive(false);
                super.set(newValue);
                setSelectionDirectly(0, 0, 0, 0);
            }
        };
        this.preserveRatio = new SimpleBooleanProperty(this, "preserveRatioProperty", false);

        // Selection
        this.selection = new Rectangle();
        this.selectionActive = new SimpleBooleanProperty(this, "selectionActiveProperty", false);
        this.selectionChanging = new SimpleBooleanProperty(this, "selectionChangingProperty", false);
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
     * The {@link Image} to be painted by this {@code SelectableImageView}. The {@link #getSelection() selection} is
     * {@link #selectionActiveProperty() deactivated} before the specified image is set.
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
     * changing. It will never be null but the value is only well defined if {@link #selectionActive} is true.
     * 
     * @return the selection
     */
    public Rectangle getSelection() {
        return selection;
    }

    /**
     * A copy of the selected area as a rectangle. The coordinates are relative to the currently shown image. <br>
     * This method will always return a different instance which will not be changed by this class. It will never be
     * null but the value is only well defined if {@link #selectionActive} is true.
     * 
     * @return a copy of the selection
     */
    public Rectangle getSelectionCopy() {
        return Shapes.copy(selection);
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be accessed
     * as its state is undefined.
     * 
     * @return the selectionActive as a property
     */
    public BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be accessed
     * as its state is undefined.
     * 
     * @return whether the selection is active
     */
    public boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * Indicates whether an area is currently selected. If this is false, the {@link #selection} should not be accessed
     * as its state is undefined. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), the selection will stay
     * deactivated.
     * 
     * @param selectionActive
     *            the new selection active status
     */
    public void setSelectionActive(boolean selectionActive) {
        if (getImage() != null)
            selectionActiveProperty().set(selectionActive);
    }

    /**
     * Sets the selection. The coordinates will be interpreted relative to the currently shown image but might be
     * modified to fit the currently shown image's size. This also activates the selection. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), this call does nothing.
     * 
     * @param upperLeftX
     *            the new x coordinate of the selection's upper left corner
     * @param upperLeftY
     *            the new y coordinate of the selection's upper left corner
     * @param width
     *            the selection's new width
     * @param height
     *            the selection's new height
     */
    public void setSelection(double upperLeftX, double upperLeftY, double width, double height) {
        if (getImage() == null)
            return;

        setCorrectedSelection(upperLeftX, upperLeftY, width, height);
        setSelectionActive(true);
    }

    /**
     * Sets the selection after correcting the specified arguments.
     * 
     * @param upperLeftX
     *            the new x coordinate of the selection's upper left corner
     * @param upperLeftY
     *            the new y coordinate of the selection's upper left corner
     * @param width
     *            the selection's new width
     * @param height
     *            the selection's new height
     */
    private void setCorrectedSelection(double upperLeftX, double upperLeftY, double width, double height) {
        // compute corrected position and size and set them
        double imageWidth = getImage().getWidth();
        double imageHeight = getImage().getHeight();
        double correctedUpperLeftX = MathTools.inInterval(0, upperLeftX, imageWidth);
        double correctedUpperLeftY = MathTools.inInterval(0, upperLeftY, imageHeight);
        double correctedWidth = MathTools.inInterval(0, width - correctedUpperLeftX, imageWidth);
        double correctedHeight = MathTools.inInterval(0, height - correctedUpperLeftX, imageWidth);

        setSelectionDirectly(correctedUpperLeftX, correctedUpperLeftY, correctedWidth, correctedHeight);
    }

    /**
     * Sets the selection without any further checks.
     * 
     * @param upperLeftX
     *            the new x coordinate of the selection's upper left corner
     * @param upperLeftY
     *            the new y coordinate of the selection's upper left corner
     * @param width
     *            the selection's new width
     * @param height
     *            the selection's new height
     */
    private void setSelectionDirectly(double upperLeftX, double upperLeftY, double width, double height) {
        getSelection().setX(upperLeftX);
        getSelection().setY(upperLeftY);
        getSelection().setWidth(width);
        getSelection().setHeight(height);
    }

    /**
     * Uses the specified rectangle as a template to set the selection. This means that the specified instance itself
     * will *not* be the new selection. Instead its coordinates will be used. They will be interpreted relative to the
     * currently shown image but might be modified to fit the currently shown image's size. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), this call does nothing.
     * 
     * @param selectionTemplate
     *            the template for the new selection
     */
    public void setSelectionFromTemplate(Rectangle selectionTemplate) {
        setSelection(selectionTemplate.getX(), selectionTemplate.getY(), selectionTemplate.getWidth(),
                selectionTemplate.getHeight());
    }

    /**
     * Uses the specified rectangle as a template to set the selection. This means that the specified instance itself
     * will *not* be the new selection. Instead its coordinates will be used. They will be interpreted relative to the
     * currently shown image but might be modified to fit the currently shown image's size. <br>
     * If there is no current image (i.e. the {@link #imageProperty()} holds {@code null}), this call does nothing.
     * 
     * @param selectionTemplate
     *            the template for the new selection
     */
    public void setSelectionFromTemplate(Rectangle2D selectionTemplate) {
        setSelection(selectionTemplate.getMinX(), selectionTemplate.getMinY(), selectionTemplate.getWidth(),
                selectionTemplate.getHeight());
    }

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     * 
     * @return the selectionChanging as a property
     */
    public BooleanProperty selectionChangingProperty() {
        return selectionChanging;
    }

    /**
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     * 
     * @return the selectionChanging
     */
    public boolean isSelectionChanging() {
        return selectionChangingProperty().get();
    }

    /* ************************************************************************
     *                                                                         *
     * Private Classes                                                         *
     *                                                                         *
     **************************************************************************/

}
