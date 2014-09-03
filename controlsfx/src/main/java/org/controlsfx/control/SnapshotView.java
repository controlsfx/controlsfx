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
package org.controlsfx.control;

import static javafx.beans.binding.Bindings.and;
import static javafx.beans.binding.Bindings.isNotNull;
import static javafx.beans.binding.Bindings.notEqual;
import impl.org.controlsfx.skin.SnapshotViewSkin;
import impl.org.controlsfx.tools.rectangle.Rectangles2D;

import java.util.Objects;
import java.util.function.Consumer;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class SnapshotView extends ControlsFXControl {

    /* ************************************************************************
     *                                                                         * 
     * Attributes & Properties                                                 * 
     *                                                                         * 
     **************************************************************************/

    // NODE

    /**
     * The {@link Node} to be displayed by this {@code SnapshotView}.
     */
    private final ObjectProperty<Node> node;

    // SELECTION

    private final ObjectProperty<Rectangle2D> selection;

    private final BooleanProperty hasSelection;

    /**
     * Indicates whether an area is currently selected. A selection will only be displayed if it is active (and valid).
     * <p>
     * If {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} is set to
     * {@code false} (which is the default) this control will update this property immediately after a new
     * {@link #selectionProperty() selection} is set: if the new selection is {@code null}, it will be set to false;
     * otherwise to {@code true}.
     * <p>
     * If {@code selectionActivityExplicitlyManaged} is {@code true} this control will never change this property's
     * value. In this case it must be managed by the using code but it is possible to unidirectionally bind it to
     * another property.
     */
    private final BooleanProperty selectionActive;

    /**
     * Indicates whether the {@link #selection} is currently changing due to GUI interaction. This will be set to
     * {@code true} when changing the selection begins and set to {@code false} when it ends.
     */
    private final BooleanProperty selectionChanging;

    /**
     * Indicates whether the ratio of the selection will be fixed. When the value changes from {@code false} to
     * {@code true} and a selection exists, the value of the {@link #fixedSelectionRatioProperty() selectionRatio}
     * property will immediately be enforced so consider setting it first.
     */
    private final BooleanProperty selectionRatioFixed;

    /**
     * The fixed ratio of the selection interpreted as {@code width / height}. If {@link #selectionRatioFixedProperty()
     * selectionRatioFixed} is {@code true}, this ratio will be upheld by all changes made by GUI interaction. This
     * explicitly excludes setting the {@link #selectionProperty() selection} property directly in which case the
     * selection's ratio will not be checked!
     * <p>
     * Only strictly positive values are allowed as ratio, otherwise an {@link IllegalArgumentException} is thrown.
     */
    private final DoubleProperty fixedSelectionRatio;

    // META

    private final ObjectProperty<Boundary> selectionAreaBoundary;

    /**
     * Indicates whether the {@link #selectionActiveProperty() selectionActive} property will be explicitly managed by
     * the code using this control. This can be useful if the {@code selectionActive} property should be bound to
     * another property.
     */
    private final BooleanProperty selectionActivityExplicitlyManaged;

    private final BooleanProperty selectionMouseTransparent;

    // VISUALIZATION

    /**
     * Indicates where the visualization of the unselected area (i.e. the area outside of the selection rectangle) ends.
     * The default value is {@link Boundary#CONTROL}.
     */
    private final ObjectProperty<Boundary> unselectedAreaBoundary;

    private final ObjectProperty<Paint> selectionStroke;

    private final DoubleProperty selectionStrokeWidth;

    private final ObjectProperty<Paint> selectionFill;

    private final ObjectProperty<Paint> unselectedAreaFill;

    /* ************************************************************************
     *                                                                         * 
     * Constructors                                                            * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Creates a new SnapshotView.
     */
    public SnapshotView() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        // NODE
        node = new SimpleObjectProperty<>(this, "node");

        // SELECTION
        selection = new SimpleObjectProperty<>(this, "selection");
        hasSelection = new SimpleBooleanProperty(this, "hasSelection", false);
        hasSelection.bind(and(isNotNull(selection), notEqual(Rectangle2D.EMPTY, selection)));
        selectionActive = new SimpleBooleanProperty(this, "selectionActive", false);
        selectionChanging = new SimpleBooleanProperty(this, "selectionChanging", false);

        selectionRatioFixed = new SimpleBooleanProperty(this, "selectionRatioFixed", false);
        fixedSelectionRatio = new SimpleDoubleProperty(this, "fixedSelectionRatio", 1) {
            @Override
            public void set(double newValue) {
                if (newValue <= 0) {
                    throw new IllegalArgumentException("The fixed selection ratio must be positive.");
                }
                super.set(newValue);
            }
        };

        // META
        selectionAreaBoundary =
                new SimpleObjectProperty<SnapshotView.Boundary>(this, "selectionAreaBoundary", Boundary.CONTROL);
        selectionActivityExplicitlyManaged = new SimpleBooleanProperty(this, "selectionActivityExplicitlyManaged",
                false);
        selectionMouseTransparent = new SimpleBooleanProperty(this, "selectionActivityExplicitlyManaged", false);

        // VISUALIZATION
        unselectedAreaBoundary =
                new SimpleObjectProperty<SnapshotView.Boundary>(this, "unselectedAreaBoundary", Boundary.CONTROL);
        selectionStroke = new SimpleObjectProperty<Paint>(this, "selectionStroke", Color.WHITESMOKE);
        selectionStrokeWidth = new SimpleDoubleProperty(this, "selectionStrokeWidth", 2.5);
        selectionFill = new SimpleObjectProperty<Paint>(this, "selectionFill", Color.TRANSPARENT);
        unselectedAreaFill = new SimpleObjectProperty<Paint>(this, "unselectedAreaFill", new Color(0, 0, 0, 0.5));

        addStateUpdatingListeners();
        // update selection when resizing
        new SelectionSizeUpdater().enableResizing();
    }

    /**
     * Adds listeners to the properties which update the control's state.
     */
    private void addStateUpdatingListeners() {
        // update the selection activity state when the selection is set
        selection.addListener((o, oldValue, newValue) -> updateSelectionActiviteState());

        // ratio
        selectionRatioFixed.addListener((o, oldValue, newValue) -> {
            boolean valueChangedToTrue = !oldValue && newValue;
            if (valueChangedToTrue) {
                fixSelectionRatio();
            }
        });
        fixedSelectionRatio.addListener((o, oldValue, newValue) -> {
            if (isSelectionRatioFixed()) {
                fixSelectionRatio();
            }
        });
    }

    /**
     * Creates a new SnapshotView using the specified node.
     * 
     * @param node
     *            the node to show after construction
     */
    public SnapshotView(Node node) {
        this();
        setNode(node);
    }

    /**
     * Creates a new SnapshotView with an {@link ImageView} as its {@link #nodeProperty() node} which displays the image
     * loaded from the specified URL.
     * 
     * @param url
     *            the string representing the URL from which to load the image
     */
    public SnapshotView(String url) {
        this();
        Image image = new Image(url);
        setNode(new ImageView(image));
    }

    /* ************************************************************************
     *                                                                         * 
     * Public Methods                                                          * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Will return an image of the selected area if the selection is currently {@link #selectionValidProperty() valid};
     * otherwise returns null.
     * 
     * @return the {@link WritableImage} that will be used to hold the rendered selection
     * 
     * @see Node#snapshot
     */
    public WritableImage createSnapshot() {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(getSelection());
        return createSnapshot(parameters);
    }

    /**
     * Will return an image of the selected area if the selection is currently {@link #selectionValidProperty() valid};
     * otherwise returns null.
     * 
     * @param parameters
     *            the {@link SnapshotParameters} used for the snapshot; must not be null
     * 
     * @return the {@link WritableImage} that will be used to hold the rendered selection
     * 
     * @see Node#snapshot
     */
    public WritableImage createSnapshot(SnapshotParameters parameters) {
        Objects.requireNonNull(parameters, "The argument 'parameters' must not be null.");

        boolean noNodeOrNoSelection = getNode() == null || !hasSelection();
        if (noNodeOrNoSelection) {
            return null;
        }

        return snapshot(parameters, null);
    }

    /* ************************************************************************
     *                                                                         * 
     * Model State                                                             * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Updates the {@link #selectionActiveProperty() selectionActive} property if the
     * {@link #selectionActivityExplicitlyManagedProperty() selectionActivityExplicitlyManaged} property indicates that
     * it is not explicitly managed by the using code (i.e. contains {@code false}.
     */
    private void updateSelectionActiviteState() {
        boolean explicitlyManaged = isSelectionActivityExplicitlyManaged();
        if (explicitlyManaged) {
            return;
        }

        boolean selectionActive = getSelection() != null;
        setSelectionActive(selectionActive);
    }

    /**
     * Fixes the ratio of the current selection (if it exists).
     */
    private void fixSelectionRatio() {
        boolean noSelectionToFix = getNode() == null || !hasSelection();
        if (noSelectionToFix) {
            return;
        }

        Rectangle2D selectionBounds = getSelectionBounds();
        Rectangle2D resizedSelection = Rectangles2D.fixRatioWithinBounds(
                getSelection(), getFixedSelectionRatio(), selectionBounds);

        selection.set(resizedSelection);
    }

    private Rectangle2D getSelectionBounds() {
        Boundary boundary = getSelectionAreaBoundary();
        switch (boundary) {
        case CONTROL:
            return new Rectangle2D(0, 0, getWidth(), getHeight());
        case NODE:
            return Rectangles2D.fromBounds(getNode().getBoundsInParent());
        default:
            throw new IllegalArgumentException("The boundary '" + boundary + "' is not fully implemented yet.");
        }
    }

    /* ************************************************************************
     *                                                                         * 
     * Style Sheet & Skin Handling                                             * 
     *                                                                         * 
     **************************************************************************/

    /**
     * The name of the style class used in CSS for instances of this class.
     */
    private static final String DEFAULT_STYLE_CLASS = "snapshot-view";

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getUserAgentStylesheet() {
        return getClass().getResource("snapshot-view.css").toExternalForm();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        Consumer<Boolean> setSelectionChanging = changing -> selectionChanging.set(changing);
        return new SnapshotViewSkin(this, setSelectionChanging);
    }

    /* ************************************************************************
     *                                                                         * 
     * Property Access                                                         * 
     *                                                                         * 
     **************************************************************************/

    // NODE

    /**
     * @return the node as a property
     */
    public final ObjectProperty<Node> nodeProperty() {
        return node;
    }

    /**
     * @return the node
     */
    public final Node getNode() {
        return nodeProperty().get();
    }

    /**
     * @param node
     *            the node to set
     */
    public final void setNode(Node node) {
        nodeProperty().set(node);
    }

    // SELECTION

    /**
     * @return the selection as a property
     */
    public final ObjectProperty<Rectangle2D> selectionProperty() {
        return selection;
    }

    /**
     * @return the selection
     */
    public final Rectangle2D getSelection() {
        return selectionProperty().get();
    }

    /**
     * @param selection
     *            the selection to set
     */
    public final void setSelection(Rectangle2D selection) {
        selectionProperty().set(selection);
    }

    /**
     * Sets the selected area as the rectangle's upper left point's coordinates and the rectangle's width and height.
     * These coordinates are interpreted relative to the node's preferred size.
     * 
     * @param upperLeftX
     *            the x coordinate of the selection's upper left point
     * @param upperLeftY
     *            the y coordinate of the selection's upper left point
     * @param width
     *            the selection's width
     * @param height
     *            the selection's height
     * 
     */
    public final void setSelection(double upperLeftX, double upperLeftY, double width, double height) {
        selectionProperty().set(new Rectangle2D(upperLeftX, upperLeftY, width, height));
    }

    public final ReadOnlyBooleanProperty hasSelectionProperty() {
        return hasSelection;
    }

    public final boolean hasSelection() {
        return hasSelectionProperty().get();
    }

    /**
     * @return the selectionActive as a property
     */
    public final BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * @return whether the selection is active
     */
    public final boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * @param selectionActive
     *            the new selection active status
     */
    public final void setSelectionActive(boolean selectionActive) {
        selectionActiveProperty().set(selectionActive);
    }

    /**
     * @return the selectionChanging as a property
     */
    public final ReadOnlyBooleanProperty selectionChangingProperty() {
        return selectionChanging;
    }

    /**
     * @return the selectionChanging
     */
    public final boolean isSelectionChanging() {
        return selectionChangingProperty().get();
    }

    /**
     * @return the selectionRatioFixed as a property
     */
    public final BooleanProperty selectionRatioFixedProperty() {
        return selectionRatioFixed;
    }

    /**
     * @return the selectionRatioFixed
     */
    public final boolean isSelectionRatioFixed() {
        return selectionRatioFixedProperty().get();
    }

    /**
     * @param selectionRatioFixed
     *            the selectionRatioFixed to set
     */
    public final void setSelectionRatioFixed(boolean selectionRatioFixed) {
        selectionRatioFixedProperty().set(selectionRatioFixed);
    }

    /**
     * @return the fixedSelectionRatio as a property
     */
    public final DoubleProperty fixedSelectionRatioProperty() {
        return fixedSelectionRatio;
    }

    /**
     * This will only ever return strictly positive values.
     * 
     * @return the fixedSelectionRatio
     */
    public final double getFixedSelectionRatio() {
        return fixedSelectionRatioProperty().get();
    }

    /**
     * @param fixedSelectionRatio
     *            the fixedSelectionRatio to set
     * @throws IllegalArgumentException
     *             if {@code fixedSelectionRatio} is not strictly positive
     */
    public final void setFixedSelectionRatio(double fixedSelectionRatio) {
        fixedSelectionRatioProperty().set(fixedSelectionRatio);
    }

    // META

    /**
     * @return the selectionAreaBoundary as a property
     */
    public final ObjectProperty<Boundary> selectionAreaBoundaryProperty() {
        return selectionAreaBoundary;
    }

    /**
     * @return the selectionAreaBoundary
     */
    public final Boundary getSelectionAreaBoundary() {
        return selectionAreaBoundaryProperty().get();
    }

    /**
     * @param selectionAreaBoundary
     *            the selectionAreaBoundary to set
     */
    public final void setSelectionAreaBoundary(Boundary selectionAreaBoundary) {
        selectionAreaBoundaryProperty().set(selectionAreaBoundary);
    }

    /**
     * @return the selectionActivityExplicitlyManaged as a property
     */
    public final BooleanProperty selectionActivityExplicitlyManagedProperty() {
        return selectionActivityExplicitlyManaged;
    }

    /**
     * @return the selectionActivityExplicitlyManaged
     */
    public final boolean isSelectionActivityExplicitlyManaged() {
        return selectionActivityExplicitlyManagedProperty().get();
    }

    /**
     * @param selectionActivityExplicitlyManaged
     *            the selectionActivityExplicitlyManaged to set
     */
    public final void setSelectionActivityExplicitlyManaged(boolean selectionActivityExplicitlyManaged) {
        selectionActivityExplicitlyManagedProperty().set(selectionActivityExplicitlyManaged);
    }

    /**
     * @return the selectionMouseTransparent as a property
     */
    public final BooleanProperty selectionMouseTransparentProperty() {
        return selectionMouseTransparent;
    }

    /**
     * @return the selectionMouseTransparent
     */
    public final boolean isSelectionMouseTransparent() {
        return selectionMouseTransparentProperty().get();
    }

    /**
     * @param selectionMouseTransparent
     *            the selectionMouseTransparent to set
     */
    public final void setSelectionMouseTransparent(boolean selectionMouseTransparent) {
        selectionMouseTransparentProperty().set(selectionMouseTransparent);
    }

    // VISUALIZATION

    /**
     * @return the unselectedAreaBoundary as a property
     */
    public final ObjectProperty<Boundary> unselectedAreaBoundaryProperty() {
        return unselectedAreaBoundary;
    }

    /**
     * @return the unselectedAreaBoundary
     */
    public final Boundary getUnselectedAreaBoundary() {
        return unselectedAreaBoundaryProperty().get();
    }

    /**
     * @param unselectedAreaBoundary
     *            the unselectedAreaBoundary to set
     */
    public final void setUnselectedAreaBoundary(Boundary unselectedAreaBoundary) {
        unselectedAreaBoundaryProperty().set(unselectedAreaBoundary);
    }

    /**
     * @return the selectionStroke as a property
     */
    public final ObjectProperty<Paint> selectionStrokeProperty() {
        return selectionStroke;
    }

    /**
     * @return the selectionStroke
     */
    public final Paint getSelectionStroke() {
        return selectionStrokeProperty().get();
    }

    /**
     * @param selectionStroke
     *            the selectionStroke to set
     */
    public final void setSelectionStroke(Paint selectionStroke) {
        selectionStrokeProperty().set(selectionStroke);
    }

    /**
     * @return the selectionStrokeWidth as a property
     */
    public final DoubleProperty selectionStrokeWidthProperty() {
        return selectionStrokeWidth;
    }

    /**
     * This will only ever return strictly positive values.
     * 
     * @return the selectionStrokeWidth
     */
    public final double getSelectionStrokeWidth() {
        return selectionStrokeWidthProperty().get();
    }

    /**
     * @param selectionStrokeWidth
     *            the selectionStrokeWidth to set
     * @throws IllegalArgumentException
     *             if {@code selectionStrokeWidth} is not strictly positive
     */
    public final void setSelectionStrokeWidth(double selectionStrokeWidth) {
        selectionStrokeWidthProperty().set(selectionStrokeWidth);
    }

    /**
     * @return the selectionFill as a property
     */
    public final ObjectProperty<Paint> selectionFillProperty() {
        return selectionFill;
    }

    /**
     * @return the selectionFill
     */
    public final Paint getSelectionFill() {
        return selectionFillProperty().get();
    }

    /**
     * @param selectionFill
     *            the selectionFill to set
     */
    public final void setSelectionFill(Paint selectionFill) {
        selectionFillProperty().set(selectionFill);
    }

    /**
     * @return the unselectedAreaFill as a property
     */
    public final ObjectProperty<Paint> unselectedAreaFillProperty() {
        return unselectedAreaFill;
    }

    /**
     * @return the unselectedAreaFill
     */
    public final Paint getUnselectedAreaFill() {
        return unselectedAreaFillProperty().get();
    }

    /**
     * @param unselectedAreaFill
     *            the unselectedAreaFill to set
     */
    public final void setUnselectedAreaFill(Paint unselectedAreaFill) {
        unselectedAreaFillProperty().set(unselectedAreaFill);
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    public static enum Boundary {

        CONTROL,

        NODE,

    }

    private class SelectionSizeUpdater {

        // CONTROL

        private final ChangeListener<Number> updateSelectionToNewControlWidthListener;

        private final ChangeListener<Number> updateSelectionToNewControlHeightListener;

        // NODE

        private final ChangeListener<Node> updateSelectionToNodeListener;

        private final ChangeListener<Bounds> updateSelectionToNewNodeBoundsListener;

        public SelectionSizeUpdater() {
            // create listener which point to methods
            updateSelectionToNewControlWidthListener = this::updateSelectionToNewControlWidth;
            updateSelectionToNewControlHeightListener = this::updateSelectionToNewControlHeight;
            updateSelectionToNodeListener = this::updateSelectionToNewNode;
            updateSelectionToNewNodeBoundsListener = this::updateSelectionToNewNodeBounds;
        }

        // ENABLE RESIZING

        public void enableResizing() {
            // only resize if the selection is not null
            enableResizingForBoundary(getSelectionAreaBoundary());
            selectionAreaBoundary.addListener((o, oldBoundary, newBoundary) -> enableResizingForBoundary(newBoundary));
        }

        private void enableResizingForBoundary(Boundary boundary) {
            switch (boundary) {
            case CONTROL:
                enableResizingForControl();
                break;
            case NODE:
                enableResizingForNode();
                break;
            default:
                throw new IllegalArgumentException("The boundary '" + boundary + "' is not fully implemented yet.");
            }
        }

        private void enableResizingForControl() {
            // remove listeners for node and its bounds
            node.removeListener(updateSelectionToNodeListener);
            if (getNode() != null) {
                getNode().boundsInParentProperty().removeListener(updateSelectionToNewNodeBoundsListener);
            }

            // add listener for the control's size
            widthProperty().addListener(updateSelectionToNewControlWidthListener);
            heightProperty().addListener(updateSelectionToNewControlHeightListener);

            // resize the selection
            updateSelectionFromNodeToControl();
        }

        private void enableResizingForNode() {
            // remove listeners for the control's size
            widthProperty().removeListener(updateSelectionToNewControlWidthListener);
            heightProperty().removeListener(updateSelectionToNewControlHeightListener);

            // add listener for new nodes
            node.addListener(updateSelectionToNodeListener);
            // update selection to the current node
            updateSelectionFromControlToNode();
        }

        // RESIZE TO CONTROL

        private void updateSelectionFromNodeToControl() {
            if (getNode() == null) {
                setSelection(null);
            } else {
                // transform the selection from the control's to the node's bounds
                Rectangle2D controlBounds = new Rectangle2D(0, 0, getWidth(), getHeight());
                Rectangle2D nodeBounds = Rectangles2D.fromBounds(getNode().getBoundsInParent());
                updateSelectionToNewBounds(nodeBounds, controlBounds);
            }
        }

        private void updateSelectionToNewControlWidth(
                ObservableValue<? extends Number> o, Number oldWidth, Number newWidth) {

            Rectangle2D oldBounds = new Rectangle2D(0, 0, oldWidth.doubleValue(), getHeight());
            Rectangle2D newBounds = new Rectangle2D(0, 0, newWidth.doubleValue(), getHeight());
            updateSelectionToNewBounds(oldBounds, newBounds);
        }

        private void updateSelectionToNewControlHeight(
                ObservableValue<? extends Number> o, Number oldHeight, Number newHeight) {

            Rectangle2D oldBounds = new Rectangle2D(0, 0, getWidth(), oldHeight.doubleValue());
            Rectangle2D newBounds = new Rectangle2D(0, 0, getWidth(), newHeight.doubleValue());
            updateSelectionToNewBounds(oldBounds, newBounds);
        }

        // RESIZE TO NODE

        private void updateSelectionFromControlToNode() {
            if (getNode() == null) {
                setSelection(null);
            } else {
                // add the listener for the current node's bounds
                getNode().boundsInParentProperty().addListener(updateSelectionToNewNodeBoundsListener);
                // transform the selection from the control's to the node's bounds
                Rectangle2D controlBounds = new Rectangle2D(0, 0, getWidth(), getHeight());
                Rectangle2D nodeBounds = Rectangles2D.fromBounds(getNode().getBoundsInParent());
                updateSelectionToNewBounds(controlBounds, nodeBounds);
            }
        }

        private void updateSelectionToNewNode(ObservableValue<? extends Node> o, Node oldNode, Node newNode) {
            // move the bounds listener from the old to the new node
            if (oldNode != null) {
                oldNode.boundsInParentProperty().removeListener(updateSelectionToNewNodeBoundsListener);
            }
            if (newNode != null) {
                newNode.boundsInParentProperty().addListener(updateSelectionToNewNodeBoundsListener);
            }

            // update selection
            if (oldNode == null || newNode == null) {
                // if one of the nodes is null, set no selection
                setSelection(null);
            } else {
                // transform the current selection
                updateSelectionToNewNodeBounds(null, oldNode.getBoundsInParent(), newNode.getBoundsInParent());
            }
        }

        private void updateSelectionToNewNodeBounds(
                ObservableValue<? extends Bounds> o, Bounds oldBounds, Bounds newBounds) {

            updateSelectionToNewBounds(Rectangles2D.fromBounds(oldBounds), Rectangles2D.fromBounds(newBounds));
        }

        // GENERAL RESIZING

        private void updateSelectionToNewBounds(Rectangle2D oldBounds, Rectangle2D newBounds) {
            if (!hasSelection()) {
                return;
            }

            Rectangle2D newSelection = transformSelectionToNewBounds(getSelection(), oldBounds, newBounds);
            if (isSelectionValid(newSelection)) {
                setSelection(newSelection);
            } else {
                setSelection(null);
            }
        }

        private boolean isSelectionValid(Rectangle2D newSelection) {
            // make sure width and height are finite values
            if (!Double.isFinite(newSelection.getWidth())) {
                return false;
            }
            if (!Double.isFinite(newSelection.getHeight())) {
                return false;
            }

            return true;
        }

        private Rectangle2D transformSelectionToNewBounds(
                Rectangle2D oldSelection, Rectangle2D oldBounds, Rectangle2D newBounds) {

            double widthRatio = newBounds.getWidth() / oldBounds.getWidth();
            double heightRatio = newBounds.getHeight() / oldBounds.getHeight();

            Point2D newSelectionCenter = computeNewSelectionCenter(
                    oldSelection, oldBounds, newBounds, widthRatio, heightRatio);

            if (isSelectionRatioFixed()) {
                double newArea = (oldSelection.getWidth() * widthRatio) * (oldSelection.getHeight() * heightRatio);
                double ratio = getFixedSelectionRatio();
                return Rectangles2D.forCenterAndAreaAndRatioWithinBounds(newSelectionCenter, newArea, ratio, newBounds);
            } else {
                double newWidth = oldSelection.getWidth() * widthRatio;
                double newHeight = oldSelection.getHeight() * heightRatio;
                return Rectangles2D.forCenterAndSize(newSelectionCenter, newWidth, newHeight);
            }
        }

        private Point2D computeNewSelectionCenter(
                Rectangle2D oldSelection, Rectangle2D oldBounds, Rectangle2D newBounds,
                double widthRatio, double heightRatio) {

            Point2D oldSelectionCenter = Rectangles2D.getCenterPoint(oldSelection);
            Point2D oldBoundsCenter = Rectangles2D.getCenterPoint(oldBounds);
            Point2D oldSelectionCenterOffset = oldSelectionCenter.subtract(oldBoundsCenter);

            Point2D newSelectionCenterOffset = new Point2D(
                    oldSelectionCenterOffset.getX() * widthRatio, oldSelectionCenterOffset.getY() * heightRatio);
            Point2D newBoundsCenter = Rectangles2D.getCenterPoint(newBounds);
            Point2D newSelectionCenter = newBoundsCenter.add(newSelectionCenterOffset);

            return newSelectionCenter;
        }

    }

}
