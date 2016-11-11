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
package org.controlsfx.samples;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SegmentedBar;

public class HelloSegmentedBar extends ControlsFXSample {

    private PopOver popOver;

    private Label popOverLabel;

    private VBox vbox = new VBox(10);

    private HBox hbox = new HBox(10);

    private SegmentedBar<TypeSegment> innerShadowBar = new SegmentedBar<>();

    private SegmentedBar<IssueStatusSegment> issueStatusBar = new SegmentedBar<>();

    private SegmentedBar<SegmentedBar.Segment> simpleBar = new SegmentedBar<>();

    private SegmentedBar<TypeSegment> typesBar = new SegmentedBar<>();

    private StackPane innerShadowPane = new StackPane();

    private StackPane contentPane = new StackPane();

    public HelloSegmentedBar() {
        vbox.setSpacing(40);
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(20));

        hbox.setSpacing(40);
        hbox.setFillHeight(true);
        hbox.setPadding(new Insets(20));

        // The out of the box bar. It uses the already set default cell factory.
        simpleBar.orientationProperty().bind(orientation);
        simpleBar.getSegments().addAll(
                new SegmentedBar.Segment(10),
                new SegmentedBar.Segment(10),
                new SegmentedBar.Segment(10),
                new SegmentedBar.Segment(10),
                new SegmentedBar.Segment(10),
                new SegmentedBar.Segment(50));

        // A bar used for visualizing the number of issues (e.g. JIRA) based on
        // their status.
        issueStatusBar.orientationProperty().bind(orientation);
        issueStatusBar.setSegmentViewFactory(segment -> new IssueStatusSegmentView(segment));

        issueStatusBar.getSegments().addAll(
                new IssueStatusSegment(30, IssueStatus.TODO),
                new IssueStatusSegment(20, IssueStatus.INPROGRESS),
                new IssueStatusSegment(50, IssueStatus.DONE)
        );

        // A bar used to visualize the disk space used by various media types (e.g. iTunes).
        typesBar.orientationProperty().bind(orientation);
        typesBar.setSegmentViewFactory(segment -> new TypeSegmentView(segment));
        typesBar.getSegments().addAll(
                new TypeSegment(14, MediaType.PHOTOS),
                new TypeSegment(32, MediaType.VIDEO),
                new TypeSegment(9, MediaType.APPS),
                new TypeSegment(40, MediaType.MUSIC),
                new TypeSegment(5, MediaType.OTHER),
                new TypeSegment(35, MediaType.FREE)
        );


        // A bar like above but with an inner shadow
        innerShadowBar.orientationProperty().bind(orientation);
        innerShadowBar.setSegmentViewFactory(segment -> new TypeSegmentView(segment));
        innerShadowBar.getSegments().addAll(new TypeSegment(14, MediaType.PHOTOS),
                new TypeSegment(32, MediaType.VIDEO),
                new TypeSegment(9, MediaType.APPS),
                new TypeSegment(40, MediaType.MUSIC),
                new TypeSegment(5, MediaType.OTHER),
                new TypeSegment(35, MediaType.FREE)
        );

        innerShadowPane.setStyle("-fx-background-color: darkgrey;");
        innerShadowPane.getChildren().add(innerShadowBar);
        innerShadowPane.setEffect(new InnerShadow());

        StackPane.setAlignment(vbox, Pos.CENTER);
        StackPane.setAlignment(hbox, Pos.CENTER);

        orientation.addListener(it -> updateParentPane());

        updateParentPane();
    }

    @Override
    public String getSampleName() {
        return "SegmentedBar";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedBar.html";
    }


    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/control/segmentedbar.css";
    }

    @Override
    public String getSampleDescription() {
        return "This control is a simple horizontal bar showing multiple segments, " +
                "each representing a fraction of a total value. A cell factory can be " +
                "used to create the segment views dynamically. The value passed to the " +
                "factory makes it possible to completely customize each segment.";
    }

    @Override
    public Node getPanel(Stage stage) {
        return contentPane;
    }

    private void updateParentPane() {
        Pane pane = vbox;
        if (popOver != null) {
            popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
        }
        if (orientation.get().equals(Orientation.VERTICAL)) {
            pane = hbox;
            if (popOver != null) {
                popOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
            }
        }

        pane.getChildren().clear();

        if (orientation.get().equals(Orientation.HORIZONTAL)) {
            pane.getChildren().add(new WrapperPane("Simple Bar", simpleBar));
            pane.getChildren().add(new WrapperPane("Issue Status (Hover for PopOver)", issueStatusBar));
            pane.getChildren().add(new WrapperPane("Disk Usage (Hover for PopOver)", typesBar));
            pane.getChildren().add(new WrapperPane("Inner Shadow (Hover for PopOver)", innerShadowPane));
        } else {
            pane.getChildren().add(simpleBar);
            pane.getChildren().add(issueStatusBar);
            pane.getChildren().add(typesBar);
            pane.getChildren().add(innerShadowPane);
        }

        contentPane.getChildren().setAll(pane);
    }

    private class WrapperPane extends BorderPane {

        public WrapperPane(String title, Node content) {
            BorderPane.setMargin(content, new Insets(5, 0, 0, 0));
            setTop(new Label(title));
            setBottom(content);
        }
    }

    public class TypeSegmentView extends StackPane {

        private Label label;

        public TypeSegmentView(TypeSegment segment) {
            label = new Label();
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.2em;");
            label.setTextOverrun(OverrunStyle.CLIP);
            StackPane.setAlignment(label, Pos.CENTER_LEFT);

            getChildren().add(label);
            switch (segment.getType()) {
                case APPS:
                    label.setText("Apps");
                    setStyle("-fx-background-color: orange;");
                    break;
                case FREE:
                    label.setText("Free");
                    setStyle("-fx-border-width: 1px; -fx-background-color: steelblue;");
                    break;
                case OTHER:
                    label.setText("Other");
                    setStyle("-fx-background-color: green;");
                    break;
                case PHOTOS:
                    label.setText("Photos");
                    setStyle("-fx-background-color: purple;");
                    break;
                case VIDEO:
                    label.setText("Video");
                    setStyle("-fx-background-color: cadetblue;");
                    break;
                case MUSIC:
                    label.setText("Music");
                    setStyle("-fx-background-color: lightcoral;");
                    break;
            }
            setPadding(new Insets(5));
            setPrefHeight(30);

            setOnMouseEntered(evt -> showPopOver(this, label.getText() + " " + segment.getValue() + " GB"));
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            label.setVisible(label.prefWidth(-1) < getWidth() - getPadding().getLeft() - getPadding().getRight());
        }
    }

    private void showPopOver(Node owner, String label) {
        if (popOver == null) {
            popOverLabel = new Label();
            popOverLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
            popOver = new PopOver(popOverLabel);
            popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
            popOver.setDetachable(false);
            popOver.setArrowSize(6);
            popOver.setCornerRadius(3);
            popOver.setAutoFix(false);
        }

        popOverLabel.setText(label);
        popOver.show(owner, -2);
    }

    public class IssueStatusSegmentView extends Region {

        public IssueStatusSegmentView(IssueStatusSegment segment) {
            setPrefHeight(16);
            setPrefWidth(16);

            switch (segment.getStatus()) {
                case DONE:
                    setStyle("-fx-background-color: green;");
                    setOnMouseEntered(evt -> showPopOver(this, "Done: " + (int) segment.getValue()));
                    break;
                case INPROGRESS:
                    setStyle("-fx-background-color: orange;");
                    setOnMouseEntered(evt -> showPopOver(this, "In Progress: " + (int) segment.getValue()));
                    break;
                case TODO:
                    setStyle("-fx-background-color: steelblue;");
                    setOnMouseEntered(evt -> showPopOver(this, "To Do: " + (int) segment.getValue()));
                    break;
            }
        }
    }

    public enum MediaType {
        MUSIC,
        VIDEO,
        FREE,
        OTHER,
        PHOTOS,
        APPS;
    }

    public static class TypeSegment extends SegmentedBar.Segment {

        private MediaType type;

        public TypeSegment(double value, MediaType type) {
            super(value);
            this.type = type;
        }

        public MediaType getType() {
            return type;
        }
    }

    public enum IssueStatus {
        DONE,
        INPROGRESS,
        TODO;
    }

    public static class IssueStatusSegment extends SegmentedBar.Segment {

        private IssueStatus status;

        public IssueStatusSegment(double value, IssueStatus status) {
            super(value);
            this.status = status;
        }

        public IssueStatus getStatus() {
            return status;
        }
    }

    private ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    @Override
    public Node getControlPanel() {
        ComboBox<Orientation> box = new ComboBox<>();
        box.getItems().addAll(Orientation.values());
        box.getSelectionModel().select(Orientation.HORIZONTAL);
        orientation.bind(box.valueProperty());
        return box;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
