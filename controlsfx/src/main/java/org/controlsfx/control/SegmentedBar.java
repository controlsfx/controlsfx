/**
 * Copyright (c) 2016, 2018 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import com.sun.javafx.css.converters.EnumConverter;
import impl.org.controlsfx.skin.SegmentedBarSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A control that makes it easy to create a horizontal bar that visualizes the
 * segmentation of a total value. It consists of several segments, each segment
 * representing a value. The sum of all values is the total value of the bar
 * (see {@link #totalProperty()}). The bar can be customized by setting a
 * factory for the creation of the segment views. Another factory can be set for
 * for the creation of info nodes shown by a {@link PopOver}.
 * <br>
 * <center> <img src="segmentedbar.png" alt="Segmented Bar"> </center> <br>
 * <h3>Example 1:</h3>
 * The most basic version of the bar. It is using the default segment view
 * factory.
 * <pre>
 * SegmentedBar bar = new SegmentedBar();
 * bar.getSegments().addAll(
 *     new Segment(10, "10"),
 *     new Segment(10, "10"),
 *     new Segment(10, "10"),
 *     new Segment(10, "10"),
 *     new Segment(10, "10"),
 *     new Segment(50, "50"));
 * </pre>
 * <h3>Example 1:</h3>
 * In this example the bar is used to visualize the usage of disk space for
 * various media types (photos, videos, music, ...). A special info node factory
 * is supplied to present a useful detailed description of the segment. The
 * type "TypeSegment" is a subclass of {@link org.controlsfx.control.SegmentedBar.Segment}
 * <pre>
 * typesBar.setSegmentViewFactory(segment -> new TypeSegmentView(segment));
 * typesBar.setInfoNodeFactory(segment -> new InfoLabel(segment.getText() + " " + segment.getValue() + " GB"));
 * typesBar.getSegments().addAll(
 *     new TypeSegment(14, MediaType.PHOTOS),
 *     new TypeSegment(32, MediaType.VIDEO),
 *     new TypeSegment(9, MediaType.APPS),
 *     new TypeSegment(40, MediaType.MUSIC),
 *     new TypeSegment(5, MediaType.OTHER),
 *     new TypeSegment(35, MediaType.FREE));
 * </pre>
 *
 * @see #setSegmentViewFactory(Callback)
 * @see #setInfoNodeFactory(Callback)
 *
 * @param <T></T> the segment type
 */
public class SegmentedBar<T extends SegmentedBar.Segment> extends ControlsFXControl {

    private static final String DEFAULT_STYLE = "segmented-bar";

    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");

    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    /**
     * Constructs a new segmented bar.
     */
    public SegmentedBar() {
        segments.addListener((Observable it) -> listenToValues());
        listenToValues();

        getStyleClass().add(DEFAULT_STYLE);

        setSegmentViewFactory(segment -> new SegmentView(segment));
        setInfoNodeFactory(segment -> {
            Label label = new Label("Value: " + segment.getValue());
            label.setPadding(new Insets(4));
            return label;
        });

    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedBarSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(SegmentedBar.class, "segmentedbar.css");
    }

    // popover node factory

    private final ObjectProperty<Callback<T, Node>> infoNodeFactory = new SimpleObjectProperty<>(this, "infoNodeFactory");

    /**
     * Stores a factory callback for creating nodes that will be shown by a popover when the mouse
     * enters a segment. The popover / node can provide detailed information about a segment.
     *
     * @return the info node factory property
     */
    public final ObjectProperty<Callback<T, Node>> infoNodeFactoryProperty() {
        return infoNodeFactory;
    }

    /**
     * Returns the value of {@link #infoNodeFactoryProperty()}.
     *
     * @return the info node factory
     */
    public final Callback<T, Node> getInfoNodeFactory() {
        return infoNodeFactory.get();
    }

    /**
     * Sets the value of {@link #infoNodeFactoryProperty()}.
     *
     * @param factory the info node factory
     */
    public void setInfoNodeFactory(Callback<T, Node> factory) {
        this.infoNodeFactory.set(factory);
    }

    // orientation

    private ObjectProperty<Orientation> orientation = new StyleableObjectProperty<Orientation>(Orientation.VERTICAL) {
        @Override
        protected void invalidated() {
            final boolean vertical = (get() == Orientation.VERTICAL);
            pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, vertical);
            pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !vertical);
        }

        @Override
        public CssMetaData<SegmentedBar, Orientation> getCssMetaData() {
            return StyleableProperties.ORIENTATION;
        }

        @Override
        public Object getBean() {
            return SegmentedBar.this;
        }

        @Override
        public String getName() {
            return "orientation"; //$NON-NLS-1$
        }
    };

    /**
     * Sets the value of the orientation property.
     *
     * @param value the new orientation (horizontal, vertical).
     * @see #orientationProperty()
     */
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    }

    /**
     * Returns the value of the orientation property.
     *
     * @return the current orientation of the control
     * @see #orientationProperty()
     */
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    /**
     * Returns the styleable object property used for storing the orientation of
     * the segmented bar. The CSS property "-fx-orientation" can be used to
     * initialize this value.
     *
     * @return the orientation property
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    private final ObjectProperty<Callback<T, Node>> segmentViewFactory = new SimpleObjectProperty<>(this, "segmentViewFactory");

    /**
     * Stores the segment view factory that is used to create one view for each segment added to
     * the control.
     *
     * @return the property that stores the factory
     */
    public final ObjectProperty<Callback<T, Node>> segmentViewFactoryProperty() {
        return segmentViewFactory;
    }

    /**
     * Returns the value of {@link #segmentViewFactoryProperty()}.
     *
     * @return the segment view factory
     */
    public final Callback<T, Node> getSegmentViewFactory() {
        return segmentViewFactory.get();
    }

    /**
     * Sets the value of {@link #segmentViewFactoryProperty()}.
     *
     * @param factory the segment view factory
     */
    public final void setSegmentViewFactory(Callback<T, Node> factory) {
        this.segmentViewFactory.set(factory);
    }

    private final ListProperty<T> segments = new SimpleListProperty<>(this, "segments", FXCollections.observableArrayList());

    /**
     * A property used to store the list of segments shown by the bar.
     *
     * @return the segment list
     */
    public final ListProperty<T> segmentsProperty() {
        return segments;
    }

    /**
     * Returns the list of segments (the model).
     *
     * @return the list of segments shown by the bar
     */
    public final ObservableList<T> getSegments() {
        return segments.get();
    }

    /**
     * Sets the list of segments (the model).
     *
     * @param segments the list of segments shown by the bar
     */
    public void setSegments(ObservableList<T> segments) {
        this.segments.set(segments);
    }

    private final ReadOnlyDoubleWrapper total = new ReadOnlyDoubleWrapper(this, "total");

    /**
     * A read-only property that stores the sum of all segment values attached
     * to the bar.
     *
     * @return the total value of the bar (sum of segments)
     */
    public final ReadOnlyDoubleProperty totalProperty() {
        return total.getReadOnlyProperty();
    }

    /**
     * Returns the value of {@link #totalProperty()}.
     *
     * @return the total value of the bar (sum of segments)
     */
    public final double getTotal() {
        return total.get();
    }

    private final InvalidationListener sumListener = (Observable it) ->
            total.set(segments.stream().collect(Collectors.summingDouble(segment -> segment.getValue())));

    private final WeakInvalidationListener weakSumListener = new WeakInvalidationListener(sumListener);

    private void listenToValues() {
        segments.get().addListener(weakSumListener);

        getSegments().forEach(segment -> {
            // first remove then add listener to ensure listener is only added once
            segment.valueProperty().removeListener(weakSumListener);
            segment.valueProperty().addListener(weakSumListener);
        });
    }

    /**
     * A model class used by the {@link SegmentedBar} control. Applications
     * usually subclass this type for their own specific needs. A segment always
     * carries a value and an optional text. Changing the value of the segment
     * will trigger a rebuild of the control.
     */
    public static class Segment {

        /**
         * Constructs a new segment with the given value.
         *
         * @param value the segment's value
         */
        public Segment(double value) {
            if (value < 0) {
                throw new IllegalArgumentException("value must be larger or equal to 0 but was " + value);
            }
            setValue(value);
        }

        /**
         * Constructs a new segment with the given value.
         *
         * @param value the segment's value
         * @param text  the segment's text
         */
        public Segment(double value, String text) {
            this(value);
            setText(text);
        }

        // text support

        private final StringProperty text = new SimpleStringProperty(this, "text");

        /**
         * Stores the text of the segment (optional).
         *
         * @return the text property
         */
        public final StringProperty textProperty() {
            return text;
        }

        /**
         * Sets the value of {@link #textProperty()}.
         *
         * @param text the new text
         */
        public final void setText(String text) {
            this.text.set(text);
        }

        /**
         * Returns the value of {@link #textProperty()}.
         *
         * @return the segment's text
         */
        public final String getText() {
            return text.get();
        }

        // value support

        private final DoubleProperty value = new SimpleDoubleProperty(this, "value") {
            @Override
            public void set(double newValue) {
                if (newValue < 0) {
                    throw new IllegalArgumentException("segment value must be >= 0 but was " + newValue);
                }
                super.set(newValue);
            }
        };

        /**
         * Stores the current value of the segment. The value has to be larger or equal
         * to zero.
         *
         * @return the value property
         */
        public final DoubleProperty valueProperty() {
            return value;
        }

        /**
         * Sets the value of {@link #valueProperty()}.
         *
         * @param value the new value
         */
        public final void setValue(double value) {
            this.value.set(value);
        }

        /**
         * Returns the value of {@link #valueProperty()}.
         *
         * @return the segment's value
         */
        public final double getValue() {
            return value.get();
        }
    }

    private static class StyleableProperties {

        private static final CssMetaData<SegmentedBar, Orientation> ORIENTATION = new CssMetaData<SegmentedBar, Orientation>(
                "-fx-orientation", new EnumConverter<>( //$NON-NLS-1$
                Orientation.class), Orientation.VERTICAL) {

            @Override
            public Orientation getInitialValue(SegmentedBar node) {
                // A vertical bar should remain vertical
                return node.getOrientation();
            }

            @Override
            public boolean isSettable(SegmentedBar n) {
                return n.orientation == null || !n.orientation.isBound();
            }

            @SuppressWarnings("unchecked")
            @Override
            public StyleableProperty<Orientation> getStyleableProperty(
                    SegmentedBar n) {
                return (StyleableProperty<Orientation>) n.orientationProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ORIENTATION);

            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }
    
    /**
     * The view class used by the default segment view factory. The view is a simple
     * StackPane that contains a label. The default style class is "segment-view".
     *
     * @see #setSegmentViewFactory(Callback)
     */
    public class SegmentView extends StackPane {

        private Label label;

        /**
         * Constructs a new segment view.
         *
         * @param segment the segment for which the view will be created.
         */
        public SegmentView(T segment) {
            getStyleClass().add("segment-view");

            label = new Label();
            label.textProperty().bind(segment.textProperty());
            label.setTextOverrun(OverrunStyle.CLIP);
            StackPane.setAlignment(label, Pos.CENTER_LEFT);

            getChildren().add(label);
        }

        /**
         * The layout method has been overridden to ensure that the label used for displaying the
         * text of a segment will be made invisible when there is not enough space to show the entire
         * text.
         */
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            label.setVisible(label.prefWidth(-1) < getWidth() - getPadding().getLeft() - getPadding().getRight());
        }
    }
}
