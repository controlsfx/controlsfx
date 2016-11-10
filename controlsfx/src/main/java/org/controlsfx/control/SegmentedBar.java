package org.controlsfx.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by dirk on 08/11/16.
 */
public class SegmentedBar<T extends SegmentedBar.Segment> extends ControlsFXControl {

    private static final String DEFAULT_STYLE = "segmented-bar";

    public SegmentedBar() {
        segments.addListener((Observable it) -> listenToValues());
        listenToValues();

        setCellFactory(segment -> {
            Region region = new Region();
            region.setPrefHeight(16);
            return region;
        });

        getStyleClass().add(DEFAULT_STYLE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SegmentedBarSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(SegmentedBar.class, "segmentedbar.css");
    }

    private final ObjectProperty<Callback<T, Node>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    public final ObjectProperty<Callback<T, Node>> cellFactoryProperty() {
        return cellFactory;
    }

    public final Callback<T, Node> getCellFactory() {
        return cellFactory.get();
    }

    public final void setCellFactory(Callback<T, Node> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    private final ListProperty<T> segments = new SimpleListProperty<>(this, "segments", FXCollections.observableArrayList());

    public final ListProperty<T> segmentsProperty() {
        return segments;
    }

    public final ObservableList<T> getSegments() {
        return segments.get();
    }

    public void setSegments(ObservableList<T> segments) {
        this.segments.set(segments);
    }

    private final ReadOnlyDoubleWrapper total = new ReadOnlyDoubleWrapper(this, "total");

    public final ReadOnlyDoubleProperty totalProperty() {
        return total.getReadOnlyProperty();
    }

    public final double getTotal() {
        return total.get();
    }

    private final InvalidationListener sumListener = (Observable it) ->
            total.set(segments.stream().collect(Collectors.summingDouble(segment -> segment.getValue())));

    private final WeakInvalidationListener weakSumListener = new WeakInvalidationListener(sumListener);

    private void listenToValues() {
        segments.get().addListener(weakSumListener);
    }

    public static class Segment {

        private String style;
        private double value;

        public Segment(double value, String style) {
            this.style = Objects.requireNonNull(style, "missing segment style");
            this.value = value;
        }

        public final String getStyle() {
            return style;
        }

        public final double getValue() {
            return value;
        }
    }
}
