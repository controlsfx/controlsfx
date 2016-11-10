package org.controlsfx.control;

import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.util.Callback;

import java.util.*;

public class SegmentedBarSkin<T extends SegmentedBar.Segment> extends SkinBase<SegmentedBar<T>> {

    private Map<T, Node> segmentNodes = new HashMap<>();

    public SegmentedBarSkin(SegmentedBar bar) {
        super(bar);

        bar.getSegments().addListener((Observable it) -> buildSegments());
        buildSegments();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefHeight = 0;
        for (Node node : getChildren()) {
            prefHeight = Math.max(prefHeight, node.prefHeight(-1));
        }
        System.out.println("ph: " + prefHeight);
        return prefHeight;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefWidth();
    }

    private void buildSegments() {
        segmentNodes.clear();
        getChildren().clear();

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        Callback<T, Node> cellFactory = getSkinnable().getCellFactory();

        for (int i = 0; i < size; i++) {
            T segment = segments.get(i);
            Node segmentNode = cellFactory.call(segment);
            segmentNodes.put(segment, segmentNode);
            getChildren().add(segmentNode);

            segmentNode.getStyleClass().addAll("segment", segment.getStyle());

            if (i == 0) {
                if (size == 1) {
                    segmentNode.getStyleClass().add("only-segment");
                } else {
                    segmentNode.getStyleClass().add("first-segment");
                }
            } else if (i == size - 1) {
                segmentNode.getStyleClass().add("last-segment");
            } else {
                segmentNode.getStyleClass().add("middle-segment");
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double total = getSkinnable().getTotal();
        double x = contentX;

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        for (int i = 0; i < size; i++) {
            SegmentedBar.Segment segment = segments.get(i);

            double segmentValue = segment.getValue();
            double segmentWidth = segmentValue / total * contentWidth;

            Node segmentNode = segmentNodes.get(segment);
            segmentNode.resizeRelocate(x, contentY, segmentWidth, contentHeight);
            x += segmentWidth;
        }
    }
}
