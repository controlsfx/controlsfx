/**
 * Copyright (c) 2016, ControlsFX
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
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A control that makes it easy to create a horizontal bar that visualizes the
 * segmentation of a total value. It consists of several segments, each segment
 * representing a value. The sum of all values are the total value of the bar
 * (see {@link #totalProperty()}). The bar can be customized by setting a
 * factory for the creation of the segment views.
 * <br>
 * <center> <img src="segmentedbar.png" alt="Segmented Bar"> </center> <br>
 * <h3>Example:</h3>
 * In this example the bar is used to visualize the usage of disk space for
 * various media types (photos, videos, music, ...).
 * <pre>
 * SegmentedBar<TypeSegment> typesBar = new SegmentedBar<>();
 * typesBar.setPrefWidth(600);
 * typesBar.setSegmentViewFactory(segment -> new TypeSegmentView(segment));
 * typesBar.getSegments().add(new TypeSegment(14, MediaType.PHOTOS));
 * typesBar.getSegments().add(new TypeSegment(32, MediaType.VIDEO));
 * typesBar.getSegments().add(new TypeSegment(9, MediaType.APPS));
 * typesBar.getSegments().add(new TypeSegment(40, MediaType.MUSIC));
 * typesBar.getSegments().add(new TypeSegment(5, MediaType.OTHER));
 * typesBar.getSegments().add(new TypeSegment(35, MediaType.FREE));
 * </pre>
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

        setSegmentViewFactory(segment -> {
            Region region = new Region();
            region.setPrefHeight(16);
            region.setPrefWidth(16);
            return region;
        });

        getStyleClass().add(DEFAULT_STYLE);

        orientationProperty().addListener(it -> requestLayout());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedBarSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(SegmentedBar.class, "segmentedbar.css");
    }


    // orientation

    private ObjectProperty<Orientation> orientation= new StyleableObjectProperty<Orientation>(null) {
        @Override
        protected void invalidated() {
            final boolean vertical = (get() == Orientation.VERTICAL);
            pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE,
                    vertical);
            pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE,
                    !vertical);
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
    }

    /**
     * A model class used by the {@link SegmentedBar} control. Applications
     * usually subclass this type for their own specific needs.
     */
    public static class Segment {

        private double value;

        /**
         * Constructs a new segment with the given value.
         *
         * @param value the segment value
         */
        public Segment(double value) {
            if (value < 0) {
                throw new IllegalArgumentException("value must be larger or equal to 0 but was " + value);
            }
            this.value = value;
        }

        /**
         * Returns the value represented by the segment.
         *
         * @return the segment value
         */
        public final double getValue() {
            return value;
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
}
