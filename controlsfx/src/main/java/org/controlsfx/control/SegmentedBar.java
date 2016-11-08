package org.controlsfx.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by dirk on 08/11/16.
 */
public class SegmentedBar extends ControlsFXControl {

    private static final String DEFAULT_STYLE = "segmented-bar";

    public SegmentedBar() {
        segments.addListener((Observable it) -> listenToValues());
        listenToValues();

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

    private final ListProperty<Segment> segments = new SimpleListProperty<>(this, "segments", FXCollections.observableArrayList());

    public final ListProperty<Segment> segmentsProperty() {
        return segments;
    }

    public final ObservableList<Segment> getSegments() {
        return segments.get();
    }

    public void setSegments(ObservableList<Segment> segments) {
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
