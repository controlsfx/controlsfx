/**
 * Copyright (c) 2014, 2015, ControlsFX
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * A {@code SnapshotView} is a control which allows the user to select an area of a node in the typical manner used by
 * picture editors and crate snapshots of the selection.
 * <p>
 * While holding the left mouse key down, a rectangular selection can be drawn. This selection can be moved, resized in
 * eight cardinal directions and removed. Additionally, the selection's ratio can be fixed in which case the user's
 * resizing will be limited such that the ratio is always upheld.
 * <p>
 * The area where the selection is possible is either this entire control or limited to the displayed node.
 * 
 * <h3>Screenshots</h3>
 * <center><img src="snapshotView.png" alt="Screenshot of SnapshotView"></center>
 * 
 * <h3>Code Samples</h3>
 * The following snippet creates a new instance with the ControlsFX logo loaded from the web, sets a selected area and
 * fixes its ratio:
 * 
 * <pre>
 * ImageView controlsFxView = new ImageView(
 *         &quot;http://cache.fxexperience.com/wp-content/uploads/2013/05/ControlsFX.png&quot;);
 * SnapshotView snapshotView = new SnapshotView(controlsFxView);
 * snapshotView.setSelection(33, 50, 100, 100);
 * snapshotView.setFixedSelectionRatio(1); // (this is actually the default value)
 * snapshotView.setSelectionRatioFixed(true);
 * </pre>
 * 
 * <h3>Functionality Overview</h3>
 * 
 * This is just a vague overview. The linked properties provide a more detailed explanation.
 * 
 * <h4>Node</h4>
 * 
 * The node which this control displays is held by the {@link #nodeProperty() node} property.
 * 
 * <h4>Selection</h4>
 * 
 * There are several properties which interact to manage and indicate the selection.
 * 
 * <h5>State</h5>
 * <ul>
 * <li>the selection is held by the {@link #selectionProperty() selection} property
 * <li>the {@link #hasSelectionProperty() hasSelection} property indicates whether a selection exists
 * <li>the {@link #selectionActiveProperty() selectionActive} property indicates whether the current selection is active
 * (it is only displayed if it is); by default this property is updated by this control which is determined by the
 * {@link #selectionActivityManagedProperty() selectionActivityManaged} property
 * </ul>
 * 
 * <h5>Interaction</h5>
 * <ul>
 * <li>if the selection is changing due to the user interacting with the control, this is indicated by the
 * {@link #selectionChangingProperty() selectionChanging} property
 * <li>whether the user can select any area of the control or only one above the node is determined by the
 * {@link #selectionAreaBoundaryProperty() selectionAreaBoundary} property
 * <li>with the {@link #selectionMouseTransparentProperty() selectionMouseTransparent} property the control can be made
 * mouse transparent so the user can interact with the displayed node
 * <li>the selection's ratio of width to height can be fixed with the {@link #selectionRatioFixedProperty()
 * selectionRatioFixed} and the {@link #fixedSelectionRatioProperty() fixedSelectionRatio} properties
 * </ul>
 * 
 * <h5>Visualization</h5>
 * <ul>
 * <li> {@link #selectionAreaFillProperty() selectionAreaFill} property for the selected area's paint
 * <li> {@link #selectionBorderPaintProperty() selectionBorderPaint} property for the selection border's paint
 * <li> {@link #selectionBorderWidthProperty() selectionBorderWidth} property for the selection border's width
 * <li> {@link #unselectedAreaFillProperty() unselectedAreaFill} property for the area outside of the selection
 * <li> {@link #unselectedAreaBoundaryProperty() unselectedAreaBoundary} property which defined what the unselected area
 * covers
 * </ul>
 */
public class SnapshotView extends ControlsFXControl {

    /**
     * The maximal divergence between a selection's ratio and the {@link #fixedSelectionRatioProperty()
     * fixedselectionRatio} for the selection to still have the correct ratio (see {@link #hasCorrectRatio(Rectangle2D)
     * hasCorrectRatio}).
     * <p>
     * The divergence is expressed relative to the {@code fixedselectionRatio}.
     */
    public static final double MAX_SELECTION_RATIO_DIVERGENCE = 1e-6;

    /**
     * The key of the {@link #getProperties() property} which is used to update {@link #selectionChangingProperty()
     * selectionChanging}.
     */
    public static final String SELECTION_CHANGING_PROPERTY_KEY =
            SnapshotView.class.getCanonicalName() + ".selection_changing"; //$NON-NLS-1$

    /* ************************************************************************
     *                                                                         * 
     * Attributes & Properties                                                 * 
     *                                                                         * 
     **************************************************************************/

    // NODE

    /**
     * @see #nodeProperty()
     */
    private final ObjectProperty<Node> node;

    // SELECTION

    /**
     * @see #selectionProperty()
     */
    private final ObjectProperty<Rectangle2D> selection;

    /**
     * @see #hasSelectionProperty()
     */
    private final BooleanProperty hasSelection;

    /**
     * @see #selectionActiveProperty()
     */
    private final BooleanProperty selectionActive;

    /**
     * @see #selectionChangingProperty()
     */
    private final BooleanProperty selectionChanging;

    /**
     * @see #selectionRatioFixedProperty()
     */
    private final BooleanProperty selectionRatioFixed;

    /**
     * @see #fixedSelectionRatioProperty()
     */
    private final DoubleProperty fixedSelectionRatio;

    // META

    /**
     * @see #selectionAreaBoundaryProperty()
     */
    private final ObjectProperty<Boundary> selectionAreaBoundary;

    /**
     * @see #selectionActivityManagedProperty()
     */
    private final BooleanProperty selectionActivityManaged;

    /**
     * @see #selectionMouseTransparentProperty()
     */
    private final BooleanProperty selectionMouseTransparent;

    // VISUALIZATION

    /**
     * @see #unselectedAreaBoundaryProperty()
     */
    private final ObjectProperty<Boundary> unselectedAreaBoundary;

    /**
     * @see #selectionBorderPaintProperty()
     */
    private final ObjectProperty<Paint> selectionBorderPaint;

    /**
     * @see #selectionBorderWidthProperty()
     */
    private final DoubleProperty selectionBorderWidth;

    /**
     * @see #selectionAreaFillProperty()
     */
    private final ObjectProperty<Paint> selectionAreaFill;

    /**
     * @see #unselectedAreaFillProperty()
     */
    private final ObjectProperty<Paint> unselectedAreaFill;

    /* ************************************************************************
     *                                                                         * 
     * Construction                                                            * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Creates a new SnapshotView.
     */
    public SnapshotView() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        // NODE
        node = new SimpleObjectProperty<>(this, "node"); //$NON-NLS-1$

        // SELECTION
        selection = new SimpleObjectProperty<Rectangle2D>(this, "selection") { //$NON-NLS-1$
            @Override
            public void set(Rectangle2D selection) {
                if (!isSelectionValid(selection)) {
                    throw new IllegalArgumentException("The selection \"" + selection + "\" is invalid. " + //$NON-NLS-1$ //$NON-NLS-2$
                            "Check the comment on 'SnapshotView.selectionProperty()' " + //$NON-NLS-1$
                            "for all criteria a selection must fulfill."); //$NON-NLS-1$
                }
                super.set(selection);
            }
        };
        hasSelection = new SimpleBooleanProperty(this, "hasSelection", false); //$NON-NLS-1$
        hasSelection.bind(and(isNotNull(selection), notEqual(Rectangle2D.EMPTY, selection)));
        selectionActive = new SimpleBooleanProperty(this, "selectionActive", false); //$NON-NLS-1$
        selectionChanging = new SimpleBooleanProperty(this, "selectionChanging", false); //$NON-NLS-1$

        selectionRatioFixed = new SimpleBooleanProperty(this, "selectionRatioFixed", false); //$NON-NLS-1$
        fixedSelectionRatio = new SimpleDoubleProperty(this, "fixedSelectionRatio", 1) { //$NON-NLS-1$
            @Override
            public void set(double newValue) {
                if (newValue <= 0) {
                    throw new IllegalArgumentException("The fixed selection ratio must be positive."); //$NON-NLS-1$
                }
                super.set(newValue);
            }
        };

        // META
        selectionAreaBoundary = createStylableObjectProperty(
                this, "selectionAreaBoundary", Boundary.CONTROL, Css.SELECTION_AREA_BOUNDARY); //$NON-NLS-1$
        selectionActivityManaged = new SimpleBooleanProperty(this, "selectionActivityManaged", true); //$NON-NLS-1$
        selectionMouseTransparent = new SimpleBooleanProperty(this, "selectionMouseTransparent", false); //$NON-NLS-1$

        // VISUALIZATION
        unselectedAreaBoundary = createStylableObjectProperty(
                this, "unselectedAreaBoundary", Boundary.CONTROL, Css.UNSELECTED_AREA_BOUNDARY); //$NON-NLS-1$
        selectionBorderPaint = createStylableObjectProperty(
                this, "selectionBorderPaint", Color.WHITESMOKE, Css.SELECTION_BORDER_PAINT); //$NON-NLS-1$
        selectionBorderWidth = createStylableDoubleProperty(
                this, "selectionBorderWidth", 2.5, Css.SELECTION_BORDER_WIDTH); //$NON-NLS-1$
        selectionAreaFill = createStylableObjectProperty(
                this, "selectionAreaFill", Color.TRANSPARENT, Css.SELECTION_AREA_FILL); //$NON-NLS-1$
        unselectedAreaFill = createStylableObjectProperty(
                this, "unselectedAreaFill", new Color(0, 0, 0, 0.5), Css.UNSELECTED_AREA_FILL); //$NON-NLS-1$

        addStateUpdatingListeners();
        // update selection when resizing
        new SelectionSizeUpdater().enableResizing();
    }

    /**
     * Adds listeners to the properties which update the control's state.
     */
    private void addStateUpdatingListeners() {
        // update the selection activity state when the selection is set
        selection.addListener((o, oldValue, newValue) -> updateSelectionActivityState());

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

        // set selection changing according to the values set in the property map
        listenToProperty(
                getProperties(), SELECTION_CHANGING_PROPERTY_KEY, (Boolean value) -> selectionChanging.set(value));
    }

    /**
     * Listens to the specified properties. When a pair with the specified key is added, it is processed. If the value
     * has the correct type, it is given to the specified consumer. Even if the type does not match, it is removed from
     * the map.
     * 
     * @param properties
     *            the {@link ObservableMap} which contains the properties; typically {@link Control#getProperties()}
     * @param key
     *            the key for whose value is listened
     * @param processValue
     *            the {@link Consumer} for the new value
     */
    private static <T> void listenToProperty(
            ObservableMap<Object, Object> properties, Object key, Consumer<T> processValue) {

        Objects.requireNonNull(properties, "The argument 'properties' must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(key, "The argument 'key' must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(processValue, "The argument 'processValue' must not be null."); //$NON-NLS-1$

        @SuppressWarnings("unchecked")
        MapChangeListener<Object, Object> listener = change -> {
            boolean addedForKey =
                    change.wasAdded() && Objects.equals(key, change.getKey());
            if (addedForKey) {
                // give the value to the consumer if it has the correct type
                try {
                    // note that this cast does nothing except to calm the compiler
                    // (hence the warning which had to be suppressed)
                    T newValue = (T) change.getValueAdded();
                    // this is where the actual exception is created
                    processValue.accept(newValue);
                } catch (ClassCastException e) {
                    // the value was of the wrong type so it can't be processed by the consumer
                    // -> do nothing
                }
                // remove the value from the properties map
                properties.remove(key);
            }
        };

        properties.addListener(listener);
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

    /* ************************************************************************
     *                                                                         * 
     * Public Methods                                                          * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Transforms the {@link #selectionProperty() selection} to node coordinates by calling
     * {@link #transformToNodeCoordinates(Rectangle2D) transformToNodeCoordinates}.
     * 
     * @return a {@link Rectangle2D} which expresses the selection in the node's coordinates
     * @throws IllegalStateException
     *             if {@link #nodeProperty() node} is {@code null} or {@link #hasSelection() hasSelection} is
     *             {@code false}
     * @see #transformToNodeCoordinates(Rectangle2D)
     */
    public Rectangle2D transformSelectionToNodeCoordinates() {
        if (!hasSelection()) {
            throw new IllegalStateException(
                    "The selection can not be transformed if it does not exist (check 'hasSelection()')."); //$NON-NLS-1$
        }

        return transformToNodeCoordinates(getSelection());
    }

    /**
     * Transforms the specified area's coordinates to coordinates relative to the node. (The node's coordinate system
     * has its origin in the upper left corner of the node.)
     * 
     * @param area
     *            the {@link Rectangle2D} which will be transformed (must not be {@code null}); its coordinates will be
     *            interpreted relative to the control (like the {@link #selectionProperty() selection})
     * @return a {@link Rectangle2D} with the same width and height as the specified {@code area} but with coordinates
     *         which are relative to the current {@link #nodeProperty() node}
     * @throws IllegalStateException
     *             if {@link #nodeProperty() node} is {@code null}
     */
    public Rectangle2D transformToNodeCoordinates(Rectangle2D area) throws IllegalStateException {
        Objects.requireNonNull(area, "The argument 'area' must not be null."); //$NON-NLS-1$
        if (getNode() == null) {
            throw new IllegalStateException(
                    "The selection can not be transformed if the node is null (check 'getNode()')."); //$NON-NLS-1$
        }

        // get the offset from the node's bounds
        Bounds nodeBounds = getNode().getBoundsInParent();
        double xOffset = nodeBounds.getMinX();
        double yOffset = nodeBounds.getMinY();

        // the coordinates of the transformed selection
        double minX = area.getMinX() - xOffset;
        double minY = area.getMinY() - yOffset;

        return new Rectangle2D(minX, minY, area.getWidth(), area.getHeight());
    }

    /**
     * Creates a snapshot of the selected area of the node.
     * 
     * @return the {@link WritableImage} that holds the rendered selection
     * @throws IllegalStateException
     *             if {@link #nodeProperty() node} is {@code null} or {@link #hasSelection() hasSelection} is
     *             {@code false}
     * @see Node#snapshot
     */
    public WritableImage createSnapshot() throws IllegalStateException {
        // make sure the node and the selection exist
        if (getNode() == null) {
            throw new IllegalStateException("No snapshot can be created if the node is null (check 'getNode()')."); //$NON-NLS-1$
        }
        if (!hasSelection()) {
            throw new IllegalStateException(
                    "No snapshot can be created if there is no selection (check 'hasSelection()')."); //$NON-NLS-1$
        }

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setViewport(getSelection());
        return createSnapshot(parameters);
    }

    /**
     * Creates a snapshot of the node with the specified parameters.
     * 
     * @param parameters
     *            the {@link SnapshotParameters} used for the snapshot (must not be {@code null}); the viewport will be
     *            interpreted relative to this control (like the {@link #selectionProperty() selection})
     * @return the {@link WritableImage} that holds the rendered viewport
     * @throws IllegalStateException
     *             if {@link #nodeProperty() node} is {@code null}
     * @see Node#snapshot
     */
    public WritableImage createSnapshot(SnapshotParameters parameters) throws IllegalStateException {
        // make sure the node and the snapshot parameters exist
        Objects.requireNonNull(parameters, "The argument 'parameters' must not be null."); //$NON-NLS-1$
        if (getNode() == null) {
            throw new IllegalStateException("No snapshot can be created if the node is null (check 'getNode()')."); //$NON-NLS-1$
        }

        // take the snapshot
        return getNode().snapshot(parameters, null);
    }

    /* ************************************************************************
     *                                                                         * 
     * Model State                                                             * 
     *                                                                         * 
     **************************************************************************/

    /**
     * Updates the {@link #selectionActiveProperty() selectionActive} property if the
     * {@link #selectionActivityManagedProperty() selectionActivityManaged} property indicates that it is managed by
     * this control.
     */
    private void updateSelectionActivityState() {
        boolean userManaged = !isSelectionActivityManaged();
        if (userManaged) {
            return;
        }

        boolean selectionActive = getSelection() != null && getSelection() != Rectangle2D.EMPTY;
        setSelectionActive(selectionActive);
    }

    /**
     * Resizes the current selection (if it exists) to the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}.
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

    /**
     * 
     * @return the bounds of the current selection according to the {@link #selectionAreaBoundaryProperty()
     *         selectionAreaBoundary}.
     */
    private Rectangle2D getSelectionBounds() {
        Boundary boundary = getSelectionAreaBoundary();
        switch (boundary) {
        case CONTROL:
            return new Rectangle2D(0, 0, getWidth(), getHeight());
        case NODE:
            return Rectangles2D.fromBounds(getNode().getBoundsInParent());
        default:
            throw new IllegalArgumentException("The boundary '" + boundary + "' is not fully implemented yet."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Checks whether the specified selection is valid. This includes checking whether the selection is in bounds and
     * has the correct ratio (if the ratio is fixed).
     * 
     * @param selection
     *            the selection to check as a {@link Rectangle2D}
     * @return {@code true} if the selection is valid; {@code false} otherwise
     */
    private boolean isSelectionValid(Rectangle2D selection) {
        // empty selections are valid
        boolean emptySelection = selection == null || selection == Rectangle2D.EMPTY;
        if (emptySelection) {
            return true;
        }

        // check values
        if (!valuesFinite(selection)) {
            return false;
        }

        // check bounds
        if (!inBounds(selection)) {
            return false;
        }

        // check ratio
        if (!hasCorrectRatio(selection)) {
            return false;
        }

        return true;
    }

    /**
     * Indicates whether the specified selection has only finite values (e.g. width and height).
     * 
     * @param selection
     *            the selection as a {@link Rectangle2D}
     * @return {@code true} if the selection has only finite values.
     */
    private static boolean valuesFinite(Rectangle2D selection) {
        return Double.isFinite(selection.getMinX()) && Double.isFinite(selection.getMinY()) &&
                Double.isFinite(selection.getWidth()) && Double.isFinite(selection.getHeight());
    }

    /**
     * Indicates whether the specified selection is inside the bounds determined by the
     * {@link #selectionAreaBoundaryProperty() selectionAreaBoundary} property.
     * 
     * @param selection
     *            the non-null and non-empty selection as a {@link Rectangle2D}
     * @return {@code true} if the selection is fully contained in the bounds; otherwise {@code false}
     */
    private boolean inBounds(Rectangle2D selection) {
        Boundary boundary = getSelectionAreaBoundary();
        switch (boundary) {
        case CONTROL:
            return inBounds(selection, getBoundsInLocal());
        case NODE:
            if (getNode() == null) {
                return false;
            } else {
                return inBounds(selection, getNode().getBoundsInParent());
            }
        default:
            throw new IllegalArgumentException("The boundary '" + boundary + "' is not fully implemented yet."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * Indicates whether the specified selection is inside the specified bounds.
     * 
     * @param selection
     *            the selection as a {@link Rectangle2D}
     * @param bounds
     *            the {@link Bounds} to check the selection against
     * @return {@code true} if the selection is fully contained in the bounds; otherwise {@code false}
     */
    private static boolean inBounds(Rectangle2D selection, Bounds bounds) {
        return bounds.getMinX() <= selection.getMinX() && bounds.getMinY() <= selection.getMinY() &&
                selection.getMaxX() <= bounds.getMaxX() && selection.getMaxY() <= bounds.getMaxY();
    }

    /**
     * Indicates whether the specified selection has the correct ratio (which depends on whether the ratio is even
     * {@link #selectionRatioFixedProperty() fixed}).
     * 
     * @param selection
     *            the selection to check as a {@link Rectangle2D}
     * @return {@code true} if the selection has the correct ratio.
     */
    private boolean hasCorrectRatio(Rectangle2D selection) {
        if (!isSelectionRatioFixed()) {
            return true;
        }

        double ratio = selection.getWidth() / selection.getHeight();
        // compute the divergence relative to the fixed selection ratio
        double ratioDivergence = Math.abs(1 - ratio / getFixedSelectionRatio());
        return ratioDivergence <= MAX_SELECTION_RATIO_DIVERGENCE;
    }

    /* ************************************************************************
     *                                                                         * 
     * Style Sheet & Skin Handling                                             * 
     *                                                                         * 
     **************************************************************************/

    /**
     * The name of the style class used in CSS for instances of this class.
     */
    private static final String DEFAULT_STYLE_CLASS = "snapshot-view"; //$NON-NLS-1$

    /** {@inheritDoc} */
    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(SnapshotView.class, "snapshot-view.css"); //$NON-NLS-1$
    }

    /**
     * Creates a {@link StyleableDoubleProperty} with the specified arguments.
     * 
     * @param bean
     *            the {@link Property#getBean() bean} the created property belongs to
     * @param name
     *            the property's {@link Property#getName() name}
     * @param initialValue
     *            the property's initial value
     * @param cssMetaData
     *            the {@link CssMetaData} for the created property
     * @return a {@link StyleableDoubleProperty}
     */
    private static StyleableDoubleProperty createStylableDoubleProperty(
            Object bean, String name, double initialValue, CssMetaData<? extends Styleable, Number> cssMetaData) {

        return new StyleableDoubleProperty(initialValue) {

            @Override
            public Object getBean() {
                return bean;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                return cssMetaData;
            }

        };
    }

    /**
     * Creates a {@link StyleableObjectProperty} with the specified arguments.
     * 
     * @param bean
     *            the {@link Property#getBean() bean} the created property belongs to
     * @param name
     *            the property's {@link Property#getName() name}
     * @param initialValue
     *            the property's initial value
     * @param cssMetaData
     *            the {@link CssMetaData} for the created property
     * @return a {@link StyleableObjectProperty}
     */
    private static <T> StyleableObjectProperty<T> createStylableObjectProperty(
            Object bean, String name, T initialValue, CssMetaData<? extends Styleable, T> cssMetaData) {

        return new StyleableObjectProperty<T>(initialValue) {

            @Override
            public Object getBean() {
                return bean;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public CssMetaData<? extends Styleable, T> getCssMetaData() {
                return cssMetaData;
            }

        };
    }

    /**
     * Creates an instance of {@link CssMetaData} with the specified arguments.
     * 
     * @param getProperty
     *            a function from the {@link Styleable} which owns the styled property to the property styled by the
     *            returned {@code CssMetaData}
     * @param cssPropertyName
     *            the name by which the styled property is referenced in CSS files
     * @param styleConverter
     *            the {@link StyleConverter} used to convert the CSS parsed value to a Java object
     * @return an instance of {@link CssMetaData}
     */
    private static <S extends Styleable, T> CssMetaData<S, T> createCssMetaData(
            Function<S, Property<T>> getProperty, String cssPropertyName, StyleConverter<?, T> styleConverter) {

        return new CssMetaData<S, T>(cssPropertyName, styleConverter) {

            @Override
            public boolean isSettable(S styleable) {
                final Property<T> property = getProperty.apply(styleable);
                return property != null && !property.isBound();
            }

            @Override
            @SuppressWarnings("unchecked")
            public StyleableProperty<T> getStyleableProperty(S styleable) {
                return (StyleableProperty<T>) getProperty.apply(styleable);
            }
        };
    }

    /**
     * The class which holds this control's {@link CssMetaData} for the different {@link StyleableProperty
     * StyleableProperties}.
     */
    @SuppressWarnings({ "javadoc", "unchecked" })
    private static class Css {

        public static final CssMetaData<SnapshotView, Boundary> SELECTION_AREA_BOUNDARY =
                createCssMetaData(
                        snapshotView -> snapshotView.selectionAreaBoundary, "-fx-selection-area-boundary", //$NON-NLS-1$
                        (StyleConverter<?, Boundary>) StyleConverter.getEnumConverter(Boundary.class));

        public static final CssMetaData<SnapshotView, Boundary> UNSELECTED_AREA_BOUNDARY =
                createCssMetaData(
                        snapshotView -> snapshotView.unselectedAreaBoundary, "-fx-unselected-area-boundary", //$NON-NLS-1$
                        (StyleConverter<?, Boundary>) StyleConverter.getEnumConverter(Boundary.class));

        public static final CssMetaData<SnapshotView, Paint> SELECTION_BORDER_PAINT =
                createCssMetaData(
                        snapshotView -> snapshotView.selectionBorderPaint, "-fx-selection-border-paint", //$NON-NLS-1$
                        StyleConverter.getPaintConverter());

        public static final CssMetaData<SnapshotView, Number> SELECTION_BORDER_WIDTH =
                createCssMetaData(
                        snapshotView -> snapshotView.selectionBorderWidth, "-fx-selection-border-width", //$NON-NLS-1$
                        StyleConverter.getSizeConverter());

        public static final CssMetaData<SnapshotView, Paint> SELECTION_AREA_FILL =
                createCssMetaData(
                        snapshotView -> snapshotView.selectionAreaFill, "-fx-selection-area-fill", //$NON-NLS-1$
                        StyleConverter.getPaintConverter());

        public static final CssMetaData<SnapshotView, Paint> UNSELECTED_AREA_FILL =
                createCssMetaData(
                        snapshotView -> snapshotView.unselectedAreaFill, "-fx-unselected-area-fill", //$NON-NLS-1$
                        StyleConverter.getPaintConverter());

        /**
         * The {@link CssMetaData} associated with this class, which includes the {@code CssMetaData} of its super
         * classes.
         */
        public static final List<CssMetaData<? extends Styleable, ?>> CSS_META_DATA;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SELECTION_AREA_BOUNDARY);
            styleables.add(UNSELECTED_AREA_BOUNDARY);
            styleables.add(SELECTION_BORDER_PAINT);
            styleables.add(SELECTION_BORDER_WIDTH);
            styleables.add(SELECTION_AREA_FILL);
            styleables.add(UNSELECTED_AREA_FILL);
            CSS_META_DATA = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return the {@link CssMetaData} associated with this class, which includes the {@code CssMetaData} of its super
     *         classes
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return Css.CSS_META_DATA;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SnapshotViewSkin(this);
    }

    /* ************************************************************************
     *                                                                         * 
     * Property Access                                                         * 
     *                                                                         * 
     **************************************************************************/

    // NODE

    /**
     * The {@link Node} which will be displayed in the center of this control.
     * <p>
     * The node's {@link Node#boundsInParentProperty() boundsInParent} show its relative position inside this control.
     * Since the {@link #selectionProperty() selection} property also uses this control as its reference coordinate
     * system, the bounds can be used to compute which area of the node is selected.
     * <p>
     * If this control or the node behaves strangely when resized, try embedding the original node in a {@link Pane} and
     * setting the pane here.
     * 
     * @return the property holding the displayed node
     */
    public final ObjectProperty<Node> nodeProperty() {
        return node;
    }

    /**
     * @return the displayed node
     * @see #nodeProperty()
     */
    public final Node getNode() {
        return nodeProperty().get();
    }

    /**
     * @param node
     *            the node to display
     * @see #nodeProperty()
     */
    public final void setNode(Node node) {
        nodeProperty().set(node);
    }

    // SELECTION

    /**
     * The current selection as a {@link Rectangle2D}. As such an instance is immutable a new one must be set to chane
     * the selection.
     * <p>
     * The rectangle's coordinates are interpreted relative to this control. The top left corner is the origin (0, 0)
     * and the lower right corner is ({@link #widthProperty() width}, {@link #heightProperty() height}). It is
     * guaranteed that the selection always lies within these bounds. If the control is resized, so is the selection. If
     * a selection which violates these bounds is set, an {@link IllegalArgumentException} is thrown.
     * <p>
     * The same is true if the {@link #selectionAreaBoundaryProperty() selectionAreaBoundary} is set to {@code NODE} but
     * with the stricter condition that the selection must lie within the {@link #nodeProperty() node}'s
     * {@link Node#boundsInParentProperty() boundsInParent}.
     * <p>
     * If the selection ratio is {@link #selectionRatioFixedProperty() fixed}, any new selection must have the
     * {@link #fixedSelectionRatioProperty() fixedSelectionRatio}. Otherwise, an {@code IllegalArgumentException} is
     * thrown.
     * <p>
     * An {@code IllegalArgumentException} is also thrown if not all of the selection's values (e.g. width and height)
     * are finite.
     * <p>
     * The selection might be {@code null} or {@link Rectangle2D#EMPTY} in which case no selection is displayed and
     * {@link #hasSelectionProperty() hasSelection} is {@code false}.
     * 
     * @return the property holding the current selection
     * @see #hasSelectionProperty()
     */
    public final ObjectProperty<Rectangle2D> selectionProperty() {
        return selection;
    }

    /**
     * @return the current selection
     * @see #selectionProperty()
     */
    public final Rectangle2D getSelection() {
        return selectionProperty().get();
    }

    /**
     * @param selection
     *            the new selection
     * @throws IllegalArgumentException
     *             if the selection is out of the bounds defined by the {@link #selectionAreaBoundaryProperty()
     *             selectionAreaBoundary} or the selection ratio is {@link #selectionRatioFixedProperty() fixed} and the
     *             new selection does not have the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}.
     * @see #selectionProperty()
     */
    public final void setSelection(Rectangle2D selection) {
        selectionProperty().set(selection);
    }

    /**
     * Creates a new {@link Rectangle2D} from the specified arguments and sets it as the new
     * {@link #selectionProperty() selection}. It will have ({@code upperLeftX}, {@code upperLeftY}) as its upper left
     * point and span {@code width} to the right and {@code height} down.
     * 
     * @param upperLeftX
     *            the x coordinate of the selection's upper left point
     * @param upperLeftY
     *            the y coordinate of the selection's upper left point
     * @param width
     *            the selection's width
     * @param height
     *            the selection's height
     * @throws IllegalArgumentException
     *             if the selection is out of the bounds defined by the {@link #selectionAreaBoundaryProperty()
     *             selectionAreaBoundary} or the selection ratio is {@link #selectionRatioFixedProperty() fixed} and the
     *             new selection does not have the {@link #fixedSelectionRatioProperty() fixedSelectionRatio}.
     * @see #selectionProperty()
     * 
     */
    public final void setSelection(double upperLeftX, double upperLeftY, double width, double height) {
        selectionProperty().set(new Rectangle2D(upperLeftX, upperLeftY, width, height));
    }

    /**
     * Indicates whether there currently is a selection. This will be {@code false} if the {@link #selectionProperty()
     * selection} property holds {@code null} or {@link Rectangle2D#EMPTY} .
     * 
     * @return a property indicating whether there currently is a selection
     */
    public final ReadOnlyBooleanProperty hasSelectionProperty() {
        return hasSelection;
    }

    /**
     * @return whether there currently is a selection
     * @see #hasSelectionProperty()
     */
    public final boolean hasSelection() {
        return hasSelectionProperty().get();
    }

    /**
     * Indicates whether the selection is currently active. Only an active selection will be displayed by the control.
     * <p>
     * See {@link #selectionActivityManagedProperty() selectionActivityManaged} for documentation on how this property
     * might be changed by this control.
     * 
     * @return the property indicating whether the selection is active
     */
    public final BooleanProperty selectionActiveProperty() {
        return selectionActive;
    }

    /**
     * @return whether the selection is active
     * @see #selectionActiveProperty()
     */
    public final boolean isSelectionActive() {
        return selectionActiveProperty().get();
    }

    /**
     * @param selectionActive
     *            the new selection active status
     * @see #selectionActiveProperty()
     */
    public final void setSelectionActive(boolean selectionActive) {
        selectionActiveProperty().set(selectionActive);
    }

    /**
     * Indicates whether the {@link #selectionProperty() selection} is currently changing due to user interaction with
     * the control. It will be set to {@code true} when changing the selection begins and set to {@code false} when it
     * ends.
     * <p>
     * If a selection is set by the code using this control (e.g. by calling {@link #setSelection(Rectangle2D)
     * setSelection}) this property does not change its value.
     * 
     * @return a property indicating whether the selection is changing by user interaction
     */
    public final ReadOnlyBooleanProperty selectionChangingProperty() {
        return selectionChanging;
    }

    /**
     * @return whether the selection is changing by user interaction
     * @see #selectionChangingProperty()
     */
    public final boolean isSelectionChanging() {
        return selectionChangingProperty().get();
    }

    /**
     * Indicates whether the ratio of the {@link #selectionProperty() selection} is fixed.
     * <p>
     * By default this property is {@code false} and the user interacting with this control can make arbitrary
     * selections with any ratio of width to height. If it is {@code true}, the user is limited to making selections
     * with the ratio defined by the {@link #fixedSelectionRatioProperty() fixedSelectionRatio} property. If the ratio
     * is fixed and a selection with a different ratio is set, an {@link IllegalArgumentException} is thrown.
     * <p>
     * If a selection exists and this property is set to {@code true}, the selection is immediately resized to the
     * currently set ratio.
     * 
     * @defaultValue {@code false}
     * @return the property indicating whether the selection ratio is fixed
     */
    public final BooleanProperty selectionRatioFixedProperty() {
        return selectionRatioFixed;
    }

    /**
     * @return whether the selection ratio is fixed
     * @see #selectionRatioFixedProperty()
     */
    public final boolean isSelectionRatioFixed() {
        return selectionRatioFixedProperty().get();
    }

    /**
     * @param selectionRatioFixed
     *            whether the selection ratio will be fixed
     * @see #selectionRatioFixedProperty()
     */
    public final void setSelectionRatioFixed(boolean selectionRatioFixed) {
        selectionRatioFixedProperty().set(selectionRatioFixed);
    }

    /**
     * The value to which the selection ratio is fixed. The ratio is defined as {@code width / height} and its value
     * must be strictly positive.
     * <p>
     * If {@link #selectionRatioFixedProperty() selectionRatioFixed} is {@code true}, this ratio will be upheld by all
     * changes made by user interaction with this control. If the ratio is fixed and a selection is set by code (e.g. by
     * calling {@link #setSelection(Rectangle2D) setSelection}), this ratio is checked and if violated an
     * {@link IllegalArgumentException} is thrown.
     * <p>
     * If a selection exists and {@code selectionRatioFixed} is set to {@code true}, the selection is immediately
     * resized to this ratio. Similarly, if a selection exists and its ratio is fixed, setting a new value resizes the
     * selection to the new ratio.
     * 
     * @defaultValue 1.0
     * @return a property containing the fixed selection ratio
     */
    public final DoubleProperty fixedSelectionRatioProperty() {
        return fixedSelectionRatio;
    }

    /**
     * @return the fixedSelectionRatio, which will always be a strictly positive value
     * @see #fixedSelectionRatioProperty()
     */
    public final double getFixedSelectionRatio() {
        return fixedSelectionRatioProperty().get();
    }

    /**
     * @param fixedSelectionRatio
     *            the fixed selection ratio to set
     * @throws IllegalArgumentException
     *             if {@code fixedSelectionRatio} is not strictly positive
     * @see #fixedSelectionRatioProperty()
     */
    public final void setFixedSelectionRatio(double fixedSelectionRatio) {
        fixedSelectionRatioProperty().set(fixedSelectionRatio);
    }

    // META

    /**
     * Indicates which {@link Boundary} is set for the area the user can select.
     * <p>
     * By default the user can select any area of the control. If this should be limited to the area over the displayed
     * node instead, this property can be set to {@link Boundary#NODE NODE}. If the value is changed from
     * {@code CONTROL} to {@code NODE} a possibly existing selection is resized accordingly.
     * <p>
     * If the boundary is set to {@code NODE}, this is also respected when a new {@link #selectionProperty() selection}
     * is set. This means the condition for the new selection's coordinates is made stricter and setting a selection out
     * of the node's bounds (instead of only out of the control's bounds) throws an {@link IllegalArgumentException}.
     * <p>
     * Note that this does <b>not</b> change the reference coordinate system! The selection's coordinates are still
     * interpreted relative to the {@link #nodeProperty() node}'s {@link Node#boundsInParentProperty() boundsInParent}.
     * 
     * @defaultValue {@link Boundary#CONTROL CONTROL}
     * @return the property indicating the {@link Boundary} for the area the user can select
     */
    public final ObjectProperty<Boundary> selectionAreaBoundaryProperty() {
        return selectionAreaBoundary;
    }

    /**
     * @return the {@link Boundary} for the area the user can select
     */
    public final Boundary getSelectionAreaBoundary() {
        return selectionAreaBoundaryProperty().get();
    }

    /**
     * @param selectionAreaBoundary
     *            the new {@link Boundary} for the area the user can select
     */
    public final void setSelectionAreaBoundary(Boundary selectionAreaBoundary) {
        selectionAreaBoundaryProperty().set(selectionAreaBoundary);
    }

    /**
     * Indicates whether the value of the {@link #selectionActiveProperty() selectionActive} property is managed by this
     * control.
     * <p>
     * If this property is set to {@code true} (which is the default) this control will update the
     * {@code selectionActive} property immediately after a new selection is set: if the new selection is {@code null}
     * or {@link Rectangle2D#EMPTY}, it will be set to {@code false}; otherwise to {@code true}.
     * <p>
     * If this property is {@code false} this control will never change {@code selectionActive}'s value. In this case it
     * must be managed by the using code but it is possible to unidirectionally bind it to another property without this
     * control interfering.
     * 
     * @defaultValue {@code true}
     * @return the property indicating whether the value of the {@link #selectionActiveProperty() selectionActive}
     *         property is managed by this control
     */
    public final BooleanProperty selectionActivityManagedProperty() {
        return selectionActivityManaged;
    }

    /**
     * @return whether the selection activity is managed by this control
     * @see #selectionActivityManagedProperty()
     */
    public final boolean isSelectionActivityManaged() {
        return selectionActivityManagedProperty().get();
    }

    /**
     * @param selectionActivityManaged
     *            whether the selection activity will be managed by this control
     * @see #selectionActivityManagedProperty()
     */
    public final void setSelectionActivityManaged(boolean selectionActivityManaged) {
        selectionActivityManagedProperty().set(selectionActivityManaged);
    }

    /**
     * Indicates whether the overlay which displays the selection is mouse transparent.
     * <p>
     * By default all mouse events are captured by this control and used to interact with the selection. If this
     * property is set to {@code true}, this behavior changes and the user is able to interact with the displayed
     * {@link #nodeProperty() node}.
     * 
     * @defaultValue {@code false}
     * @return the property indicating whether the selection is mouse transparent
     */
    public final BooleanProperty selectionMouseTransparentProperty() {
        return selectionMouseTransparent;
    }

    /**
     * @return whether the selection is mouse transparent
     * @see #selectionMouseTransparentProperty()
     */
    public final boolean isSelectionMouseTransparent() {
        return selectionMouseTransparentProperty().get();
    }

    /**
     * @param selectionMouseTransparent
     *            whether the selection will be mouse transparent
     * @see #selectionMouseTransparentProperty()
     */
    public final void setSelectionMouseTransparent(boolean selectionMouseTransparent) {
        selectionMouseTransparentProperty().set(selectionMouseTransparent);
    }

    // VISUALIZATION

    /**
     * Indicates which {@link Boundary} is set for the visualization of the unselected area (i.e. the area outside of
     * the selection rectangle).
     * <p>
     * If it is set to {@link Boundary#CONTROL CONTROL} (which is the default), the unselected area covers the whole
     * control.
     * <p>
     * If it is set to {@link Boundary#NODE NODE}, the area only covers the displayed {@link #nodeProperty() node}. In
     * most cases this only makes sense if the {@link #selectionAreaBoundaryProperty() selectionAreaBoundary} is also
     * set to {@code NODE}.
     * 
     * @defaultValue {@link Boundary#CONTROL}
     * @return the property defining the {@link Boundary} of the unselected area
     */
    public final ObjectProperty<Boundary> unselectedAreaBoundaryProperty() {
        return unselectedAreaBoundary;
    }

    /**
     * @return the {@link Boundary} for the unselected area
     * @see #unselectedAreaBoundaryProperty()
     */
    public final Boundary getUnselectedAreaBoundary() {
        return unselectedAreaBoundaryProperty().get();
    }

    /**
     * @param unselectedAreaBoundary
     *            the new {@link Boundary} for the unselected area
     * @see #unselectedAreaBoundaryProperty()
     */
    public final void setUnselectedAreaBoundary(Boundary unselectedAreaBoundary) {
        unselectedAreaBoundaryProperty().set(unselectedAreaBoundary);
    }

    /**
     * Determines the visualization of the selection's border.
     * 
     * @defaultValue {@link Color#WHITESMOKE}
     * @return the property holding the {@link Paint} of the selection border
     * @see #selectionBorderWidthProperty()
     */
    public final ObjectProperty<Paint> selectionBorderPaintProperty() {
        return selectionBorderPaint;
    }

    /**
     * @return the {@link Paint} of the selection border
     * @see #selectionBorderPaintProperty()
     */
    public final Paint getSelectionBorderPaint() {
        return selectionBorderPaintProperty().get();
    }

    /**
     * @param selectionBorderPaint
     *            the new {@link Paint} of the selection border
     * @see #selectionBorderPaintProperty()
     */
    public final void setSelectionBorderPaint(Paint selectionBorderPaint) {
        selectionBorderPaintProperty().set(selectionBorderPaint);
    }

    /**
     * Determines the width of the selection's border. The border is always painted to the outside of the selected area,
     * i.e. the selected area is never covered by the border.
     * 
     * @defaultValue 2.5
     * @return the property defining the selection border's width
     * @see #selectionBorderPaintProperty()
     * @see javafx.scene.shape.Shape#strokeWidthProperty() Shape.strokeWidthProperty()
     */
    public final DoubleProperty selectionBorderWidthProperty() {
        return selectionBorderWidth;
    }

    /**
     * @return the selection border width
     * @see #selectionBorderWidthProperty()
     */
    public final double getSelectionBorderWidth() {
        return selectionBorderWidthProperty().get();
    }

    /**
     * @param selectionBorderWidth
     *            the selection border width to set
     * @see #selectionBorderWidthProperty()
     */
    public final void setSelectionBorderWidth(double selectionBorderWidth) {
        selectionBorderWidthProperty().set(selectionBorderWidth);
    }

    /**
     * Determines the visualization of the selected area.
     * 
     * @defaultValue {@link Color#TRANSPARENT}
     * @return the property holding the {@link Paint} of the selected area
     */
    public final ObjectProperty<Paint> selectionAreaFillProperty() {
        return selectionAreaFill;
    }

    /**
     * @return the {@link Paint} of the selected area
     * @see #selectionAreaFillProperty()
     */
    public final Paint getSelectionAreaFill() {
        return selectionAreaFillProperty().get();
    }

    /**
     * @param selectionAreaFill
     *            the new {@link Paint} of the selected area
     * @see #selectionAreaFillProperty()
     */
    public final void setSelectionAreaFill(Paint selectionAreaFill) {
        selectionAreaFillProperty().set(selectionAreaFill);
    }

    /**
     * Determines the visualization of the area outside of the selection.
     * 
     * @defaultValue {@link Color#BLACK black} with {@link Color#getOpacity() opacity} 0.5
     * @return the property holding the {@link Paint} of the area outside of the selection
     */
    public final ObjectProperty<Paint> unselectedAreaFillProperty() {
        return unselectedAreaFill;
    }

    /**
     * @return the {@link Paint} of the area outside of the selection
     * @see #unselectedAreaFillProperty()
     */
    public final Paint getUnselectedAreaFill() {
        return unselectedAreaFillProperty().get();
    }

    /**
     * @param unselectedAreaFill
     *            the new {@link Paint} of the area outside of the selection
     * @see #unselectedAreaFillProperty()
     */
    public final void setUnselectedAreaFill(Paint unselectedAreaFill) {
        unselectedAreaFillProperty().set(unselectedAreaFill);
    }

    /* ************************************************************************
     *                                                                         *
     * Inner Classes                                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * The {@link SnapshotView#selectionAreaBoundaryProperty() selectionArea}, in which the user can create a selection,
     * and the {@link SnapshotView#unselectedAreaBoundaryProperty() unselectedArea}, in which the unselected area is
     * visualized, are limited to a certain area of the control. This area's boundary is represented by this enum.
     *
     */
    public static enum Boundary {

        /**
         * The boundary is this control's bound.
         */
        CONTROL,

        /**
         * The boundary is the displayed node's bound.
         */
        NODE,

    }

    /**
     * Updates the size of the {@link SnapshotView#selectionProperty() selection} whenever necessary. This is the case
     * if the {@link SnapshotView#selectionAreaBoundaryProperty() selectionAreaBoundary} is set to
     * {@link Boundary#CONTROL CONTROL} and the control is resized or when it is set to {@link Boundary#NODE NODE} and
     * the node is changed or resized.
     *
     */
    private class SelectionSizeUpdater {

        /*
         * If the 'selectionAreaBoundary' is set to 'CONTROL', the selection is only updated when the control changes
         * its width or height. If it is set to 'NODE', the selection is resized whenever the node or its
         * 'boundsInParent' change.
         * For both cases methods exist which resize the selection. The listeners which call those methods are only
         * added to the corresponding properties when the matching boundary is selected.
         */

        // CONTROL

        /**
         * Calls {@link #resizeSelectionToNewControlWidth(ObservableValue, Number, Number)
         * updateSelectionToNewControlWidth} whenever the control's width changes.
         */
        private final ChangeListener<Number> resizeSelectionToNewControlWidthListener;

        /**
         * Calls {@link #resizeSelectionToNewControlHeight(ObservableValue, Number, Number)
         * updateSelectionToNewControlWidth} whenever the control's height changes.
         */
        private final ChangeListener<Number> resizeSelectionToNewControlHeightListener;

        // NODE

        /**
         * Calls {@link #updateSelectionToNewNode(ObservableValue, Node, Node) updateSelectionToNewNode} whenever a new
         * {@link SnapshotView#nodeProperty() node} is set.
         */
        private final ChangeListener<Node> updateSelectionToNodeListener;

        /**
         * Calls {@link #resizeSelectionToNewNodeBounds(ObservableValue, Bounds, Bounds) updateSelectionToNewNodeBounds}
         * whenever the node's {@link Node#boundsInParentProperty() boundsInParent} change.
         */
        private final ChangeListener<Bounds> resizeSelectionToNewNodeBoundsListener;

        // CONSTRUCTION

        /**
         * Creates a new selection size updater.
         */
        public SelectionSizeUpdater() {
            // create listeners which point to methods
            resizeSelectionToNewControlWidthListener = this::resizeSelectionToNewControlWidth;
            resizeSelectionToNewControlHeightListener = this::resizeSelectionToNewControlHeight;
            updateSelectionToNodeListener = this::updateSelectionToNewNode;
            resizeSelectionToNewNodeBoundsListener = this::resizeSelectionToNewNodeBounds;
        }

        // ENABLE RESIZING

        /**
         * Enables resizing of the control.
         */
        public void enableResizing() {
            // only resize if the selection is not null
            enableResizingForBoundary(getSelectionAreaBoundary());
            selectionAreaBoundary.addListener((o, oldBoundary, newBoundary) -> enableResizingForBoundary(newBoundary));
        }

        /**
         * Enables resizing for the specified boundary.
         * 
         * @param boundary
         *            the {@link Boundary} for which the control will be resized.
         */
        private void enableResizingForBoundary(Boundary boundary) {
            switch (boundary) {
            case CONTROL:
                enableResizingForControl();
                break;
            case NODE:
                enableResizingForNode();
                break;
            default:
                throw new IllegalArgumentException("The boundary '" + boundary + "' is not fully implemented yet."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * Enables resizing if the {@link SnapshotView#selectionAreaBoundary selectionAreaBoundary} is
         * {@link Boundary#CONTROL CONTROL}.
         */
        private void enableResizingForControl() {
            // remove listeners for node and its bounds
            node.removeListener(updateSelectionToNodeListener);
            if (getNode() != null) {
                getNode().boundsInParentProperty().removeListener(resizeSelectionToNewNodeBoundsListener);
            }

            // add listener for the control's size
            widthProperty().addListener(resizeSelectionToNewControlWidthListener);
            heightProperty().addListener(resizeSelectionToNewControlHeightListener);

            resizeSelectionFromNodeToControl();
        }

        /**
         * Enables resizing if the {@link SnapshotView#selectionAreaBoundary selectionAreaBoundary} is
         * {@link Boundary#NODE NODE}.
         */
        private void enableResizingForNode() {
            // remove listeners for the control's size
            widthProperty().removeListener(resizeSelectionToNewControlWidthListener);
            heightProperty().removeListener(resizeSelectionToNewControlHeightListener);

            // add listener for the node's bounds and for new nodes
            if (getNode() != null) {
                getNode().boundsInParentProperty().addListener(resizeSelectionToNewNodeBoundsListener);
            }
            node.addListener(updateSelectionToNodeListener);

            resizeSelectionFromControlToNode();
        }

        // RESIZE TO CONTROL

        /**
         * Resizes the current {@link SnapshotView#selectionProperty() selection} from the node's to the control's
         * bounds.
         */
        private void resizeSelectionFromNodeToControl() {
            if (getNode() == null) {
                setSelection(null);
            } else {
                // transform the selection from the control's to the node's bounds
                Rectangle2D controlBounds = new Rectangle2D(0, 0, getWidth(), getHeight());
                Rectangle2D nodeBounds = Rectangles2D.fromBounds(getNode().getBoundsInParent());
                resizeSelectionToNewBounds(nodeBounds, controlBounds);
            }
        }

        /**
         * Resizes the current {@link SnapshotView#selectionProperty() selection} from the control's specified old width
         * to its specified new width.
         * <p>
         * Designed to be used as a lambda method reference.
         * 
         * @param o
         *            the {@link ObservableValue} which changed its value
         * @param oldWidth
         *            the control's old width
         * @param newWidth
         *            the control's new width
         */
        private void resizeSelectionToNewControlWidth(
                @SuppressWarnings("unused") ObservableValue<? extends Number> o, Number oldWidth, Number newWidth) {

            Rectangle2D oldBounds = new Rectangle2D(0, 0, oldWidth.doubleValue(), getHeight());
            Rectangle2D newBounds = new Rectangle2D(0, 0, newWidth.doubleValue(), getHeight());
            resizeSelectionToNewBounds(oldBounds, newBounds);
        }

        /**
         * Resizes the current {@link SnapshotView#selectionProperty() selection} from the control's specified old
         * height to its specified new height.
         * <p>
         * Designed to be used as a lambda method reference.
         * 
         * @param o
         *            the {@link ObservableValue} which changed its value
         * @param oldHeight
         *            the control's old height
         * @param newHeight
         *            the control's new height
         */
        private void resizeSelectionToNewControlHeight(
                @SuppressWarnings("unused") ObservableValue<? extends Number> o, Number oldHeight, Number newHeight) {

            Rectangle2D oldBounds = new Rectangle2D(0, 0, getWidth(), oldHeight.doubleValue());
            Rectangle2D newBounds = new Rectangle2D(0, 0, getWidth(), newHeight.doubleValue());
            resizeSelectionToNewBounds(oldBounds, newBounds);
        }

        // RESIZE TO NODE

        /**
         * Resizes the current {@link SnapshotView#selectionProperty() selection} from the control's to the node's
         * bounds
         */
        private void resizeSelectionFromControlToNode() {
            if (getNode() == null) {
                setSelection(null);
            } else {
                // transform the selection from the control's to the node's bounds
                Rectangle2D controlBounds = new Rectangle2D(0, 0, getWidth(), getHeight());
                Rectangle2D nodeBounds = Rectangles2D.fromBounds(getNode().getBoundsInParent());
                resizeSelectionToNewBounds(controlBounds, nodeBounds);
            }
        }

        /**
         * Moves the {@link #resizeSelectionToNewNodeBoundsListener} from the specified old to the specified new node's
         * {@link Node#boundsInParentProperty() boundsInParent} property and resizes the current
         * {@link SnapshotView#selectionProperty() selection} from the old to the new node's bounds.
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
        private void updateSelectionToNewNode(
                @SuppressWarnings("unused") ObservableValue<? extends Node> o, Node oldNode, Node newNode) {

            // move the bounds listener from the old to the new node
            if (oldNode != null) {
                oldNode.boundsInParentProperty().removeListener(resizeSelectionToNewNodeBoundsListener);
            }
            if (newNode != null) {
                newNode.boundsInParentProperty().addListener(resizeSelectionToNewNodeBoundsListener);
            }

            // update selection
            if (oldNode == null || newNode == null) {
                // if one of the nodes is null, set no selection
                setSelection(null);
            } else {
                // transform the current selection
                resizeSelectionToNewNodeBounds(null, oldNode.getBoundsInParent(), newNode.getBoundsInParent());
            }
        }

        /**
         * Resizes the current {@link SnapshotView#selectionProperty() selection} from the specified old to the
         * specified new bounds of the {@link SnapshotView#nodeProperty() node}.
         * 
         * @param o
         *            the {@link ObservableValue} which changed its value
         * @param oldBounds
         *            the node's old bounds
         * @param newBounds
         *            the node's new bounds
         */
        private void resizeSelectionToNewNodeBounds(
                @SuppressWarnings("unused") ObservableValue<? extends Bounds> o, Bounds oldBounds, Bounds newBounds) {

            resizeSelectionToNewBounds(Rectangles2D.fromBounds(oldBounds), Rectangles2D.fromBounds(newBounds));
        }

        // GENERAL RESIZING

        /**
         * If this control {@link SnapshotView#hasSelection() has a selection} it is resized from the specified old to
         * the specified new bounds.
         * 
         * @param oldBounds
         *            the {@link SnapshotView#selectionProperty() selection}'s old bounds as a {@link Rectangle2D}
         * @param newBounds
         *            the {@link SnapshotView#selectionProperty() selection}'s new bounds as a {@link Rectangle2D}
         */
        private void resizeSelectionToNewBounds(Rectangle2D oldBounds, Rectangle2D newBounds) {
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

        /**
         * Returns a new selection which is a transformation of the specified old selection. The transformation is such
         * that the new selection's "relative position" in the specified new bounds is the same as the old selection's
         * relative position in the specified old bounds.
         * <p>
         * Here, "relative position" is a representation of the selection where the coordinates of its upper left point
         * and its width and height are expressed in a percentage of its bounds. Those percentages are the same for
         * "old selection in old bounds" and "returned selection in new bounds"
         * 
         * @param oldSelection
         *            the selection to be transformed as a {@link Rectangle2D}
         * @param oldBounds
         *            the {@code oldSelection}'s old bounds as a {@link Rectangle2D}
         * @param newBounds
         *            the {@code oldSelection}'s new bounds as a {@link Rectangle2D}
         * @return s {@link Rectangle2D} which is the transformation of the old selection to the new bounds
         */
        private Rectangle2D transformSelectionToNewBounds(
                Rectangle2D oldSelection, Rectangle2D oldBounds, Rectangle2D newBounds) {

            Point2D newSelectionCenter = computeNewSelectionCenter(oldSelection, oldBounds, newBounds);

            double widthRatio = newBounds.getWidth() / oldBounds.getWidth();
            double heightRatio = newBounds.getHeight() / oldBounds.getHeight();

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

        /**
         * Computes a point with the same relative position in the specified new bounds as the specified old selection's
         * center point in the specified old bounds. (See
         * {@link #transformSelectionToNewBounds(Rectangle2D, Rectangle2D, Rectangle2D) transformSelectionToNewBounds}
         * for a definition of "relative position").
         * 
         * @param oldSelection
         *            the selection whose center point is the base for the returned center point as a
         *            {@link Rectangle2D}
         * @param oldBounds
         *            the bounds of the old selection as a {@link Rectangle2D}
         * @param newBounds
         *            the bounds for the new selection as a {@link Rectangle2D}
         * @return a {@link Point2D} with the same relative position in the new bounds as the old selection's center
         *         point in the old bounds
         */
        private Point2D computeNewSelectionCenter(Rectangle2D oldSelection, Rectangle2D oldBounds, Rectangle2D newBounds) {

            Point2D oldSelectionCenter = Rectangles2D.getCenterPoint(oldSelection);
            Point2D oldBoundsCenter = Rectangles2D.getCenterPoint(oldBounds);
            Point2D oldSelectionCenterOffset = oldSelectionCenter.subtract(oldBoundsCenter);

            double widthRatio = newBounds.getWidth() / oldBounds.getWidth();
            double heightRatio = newBounds.getHeight() / oldBounds.getHeight();

            Point2D newSelectionCenterOffset = new Point2D(
                    oldSelectionCenterOffset.getX() * widthRatio, oldSelectionCenterOffset.getY() * heightRatio);
            Point2D newBoundsCenter = Rectangles2D.getCenterPoint(newBounds);
            Point2D newSelectionCenter = newBoundsCenter.add(newSelectionCenterOffset);

            return newSelectionCenter;
        }

    }

}
