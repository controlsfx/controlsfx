package org.controlsfx.control;

import javafx.beans.Observable;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;

import java.util.Arrays;
import java.util.List;

public class SegmentedBarSkin extends SkinBase<SegmentedBar> {

    public SegmentedBarSkin(SegmentedBar bar) {
        super(bar);

        bar.getSegments().addListener((Observable it) -> buildSegments());
        buildSegments();
    }

    private void buildSegments() {
        getChildren().clear();
        getSkinnable().requestLayout();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        // only compute PREF width, we still want the bar to be resizable, so no min or max
        return getSkinnable().getPrefWidth();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double total = getSkinnable().getTotal();
        double x = contentX;

        List<SegmentedBar.Segment> segments = getSkinnable().getSegments();
        int size = segments.size();

        for (int i = 0; i < size; i++) {
            SegmentedBar.Segment segment = segments.get(i);

            double segmentValue = segment.getValue();
            double segmentWidth = segmentValue / total * contentWidth;

            Region segmentRegion = new Region();
            getChildren().add(segmentRegion);
            segmentRegion.resizeRelocate(x, contentY, segmentWidth, contentHeight);
            x += segmentWidth;

            segmentRegion.getStyleClass().addAll("segment", segment.getStyle());

            if (i == 0) {
                if (size == 1) {
                    segmentRegion.getStyleClass().add("only-segment");
                } else {
                    segmentRegion.getStyleClass().add("first-segment");
                }
            } else if (i == size - 1) {
                segmentRegion.getStyleClass().add("last-segment");
            } else {
                segmentRegion.getStyleClass().add("middle-segment");
            }

            System.out.println("styles: " + Arrays.toString(segmentRegion.getStyleClass().toArray()));
        }
    }
}
