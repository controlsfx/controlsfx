package org.controlsfx.control.imageview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

/**
 * <b>SELECTION</b> <br>
 * <br>
 * 
 * <b>Position</b> <br>
 * TODO Define contract about position<br>
 * <br>
 * 
 * <b>Model</b> <br>
 * The model for the selection is the {@link SelectableImageView#selectionProperty() selection} property. It contains a
 * {@link Rectangle2D}, which might be null if the selection is deactivated (see below). The returned rectangle is
 * immutable and the selection can only be changed by setting a new one. <br>
 * <br>
 * 
 * <b>Active</b> <br>
 * The {@link SelectableImageView#selectionActiveProperty() selectionActive} property indicates whether the selection is
 * currently active. It is displayed by the control if and only if it is active.<br>
 * The selection is automatically deactivated when a new image is set (more precisely: <i>before</i> the
 * {@link SelectableImageView#imageProperty() image} property changes) and automatically activated when a selection is
 * explicitly set (via {@link SelectableImageView#setSelection(Rectangle2D) setSelection} or the
 * {@link SelectableImageView#selectionProperty() selection} property; more precisely: <i>after</i> the property was
 * updated to its new value). <br>
 * If it is deactivated, the selection is in an undefined state and the property should not be accessed.
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
    private final BooleanProperty preserveImageRatio;

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image. <br>
     * The value is only well defined if {@link #selectionActiveProperty()} holds {@code true}.
     */
    private final ObjectProperty<Rectangle2D> selection;

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

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() selectionRatio}
     * property will immediately be enforced so consider setting it first.
     */
    private final BooleanProperty selectionRatioFixed;

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}
     * property will immediately be enforced so consider setting it first.
     */
    private final DoubleProperty fixedSelectionRatio;

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
                // set the selection to be null
                setSelection(null);
            }
        };
        this.preserveImageRatio = new SimpleBooleanProperty(this, "preserveImageRatioProperty", false);

        // Selection
        this.selection = new SimpleObjectProperty<Rectangle2D>(this, "selectionProperty") {
            @Override
            public void set(Rectangle2D newValue) {
                super.set(newValue);
                // activate the selection when a new value is set
                boolean activateSelection = newValue != null;
                setSelectionActive(activateSelection);
            }
        };
        this.selectionActive = new SimpleBooleanProperty(this, "selectionActiveProperty", false);
        this.selectionChanging = new SimpleBooleanProperty(this, "selectionChangingProperty", false);
        this.selectionRatioFixed = new SimpleBooleanProperty(this, "selectionRatioFixedProperty", false);
        this.fixedSelectionRatio = new SimpleDoubleProperty(this, "fixedSelectionRatioProperty", 1) {
            @Override
            public void set(double newValue) {
                if (newValue <= 0)
                    throw new IllegalArgumentException("The fixed selection ratio must be positive.");
                super.set(newValue);
            }
        };
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
     * @return the preserveImageRatio as a property
     */
    public BooleanProperty preserveImageRatioProperty() {
        return preserveImageRatio;
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     * 
     * @return the preserveImageRatio
     */
    public boolean isPreserveImageRatio() {
        return preserveImageRatioProperty().get();
    }

    /**
     * Indicates whether to preserve the aspect ratio of the source image when scaling to fit the image within the
     * fitting bounding box.
     * 
     * @param preserveImageRatio
     *            the preserveImageRatio to set
     */
    public void setPreserveImageRatio(boolean preserveImageRatio) {
        preserveImageRatioProperty().set(preserveImageRatio);
    }

    // SELECTION

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image. <br>
     * The value is only well defined if {@link #selectionActive} is true.
     * 
     * @return the selection as a property
     */
    public ObjectProperty<Rectangle2D> selectionProperty() {
        return selection;
    }

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image. <br>
     * The value is only well defined if {@link #selectionActive} is true.
     * 
     * @return the selection
     */
    public Rectangle2D getSelection() {
        return selectionProperty().get();
    }

    /**
     * The selected area as a rectangle. The coordinates are interpreted relative to the currently shown image. <br>
     * The value is only well defined if {@link #selectionActive} is true.
     * 
     * @param selection
     *            the selection to set
     */
    public void setSelection(Rectangle2D selection) {
        selectionProperty().set(selection);
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
     * Indicates whether the {@link #selection} is currently changing. This will be set to true when changing the
     * selection begins and set to false when it ends.
     * 
     * @return the selectionChanging as a property
     */
    public BooleanProperty selectionChangingProperty() {

        // TODO It would be very nice if this could be a read only property
        // but it is unclear how it could then be edited by 'SelectableImageViewBehavior'.

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

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}
     * property will immediately be enforced so consider setting it first.
     * 
     * @return the selectionRatioFixed as a property
     */
    public BooleanProperty selectionRatioFixedProperty() {
        return selectionRatioFixed;
    }

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}
     * property will immediately be enforced so consider setting it first.
     * 
     * @return the selectionRatioFixed
     */
    public boolean isSelectionRatioFixed() {
        return selectionRatioFixedProperty().get();
    }

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}
     * property will immediately be enforced so consider setting it first.
     * 
     * @param selectionRatioFixed
     *            the selectionRatioFixed to set
     */
    public void setSelectionRatioFixed(boolean selectionRatioFixed) {
        selectionRatioFixedProperty().set(selectionRatioFixed);
    }

    /**
     * The fixed ratio of the {@link #selectionProperty() selection}. Is only enforced if and only if the
     * {@link #selectionRatioFixedProperty() selectionRatioFixed} property holds {@code true}. <br>
     * Settings non-positive values throws an {@link IllegalArgumentException}.
     * 
     * @return the fixedSelectionRatio as a property
     */
    public DoubleProperty fixedSelectionRatioProperty() {
        return fixedSelectionRatio;
    }

    /**
     * The fixed ratio of the {@link #selectionProperty() selection}. Is only enforced if and only if the
     * {@link #selectionRatioFixedProperty() selectionRatioFixed} property holds {@code true}. <br>
     * Settings non-positive values throws an {@link IllegalArgumentException}.
     * 
     * @return the fixedSelectionRatio
     */
    public double getFixedSelectionRatio() {
        return fixedSelectionRatioProperty().get();
    }

    /**
     * The fixed ratio of the {@link #selectionProperty() selection}. Is only enforced if and only if the
     * {@link #selectionRatioFixedProperty() selectionRatioFixed} property holds {@code true}. <br>
     * Settings non-positive values throws an {@link IllegalArgumentException}.
     * 
     * @param fixedSelectionRatio
     *            the fixedSelectionRatio to set
     * @throws IllegalArgumentException
     *             if {@code fixedSelectionRatio} is non-positive
     */
    public void setFixedSelectionRatio(double fixedSelectionRatio) {
        fixedSelectionRatioProperty().set(fixedSelectionRatio);
    }

    /* ************************************************************************
     *                                                                         *
     * Private Classes                                                         *
     *                                                                         *
     **************************************************************************/

}
