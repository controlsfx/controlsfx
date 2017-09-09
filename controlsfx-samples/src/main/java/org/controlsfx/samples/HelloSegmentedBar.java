/**
 * Copyright (c) 2016, 2017 ControlsFX
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SegmentedBar;

public class HelloSegmentedBar extends ControlsFXSample {

    private VBox vbox = new VBox(40);

    private HBox hbox = new HBox(40);

    private SegmentedBar<TypeSegment> innerShadowBar = new SegmentedBar<>();

    private SegmentedBar<IssueStatusSegment> issueStatusBar = new SegmentedBar<>();

    private SegmentedBar<SegmentedBar.Segment> simpleBar = new SegmentedBar<>();

    private SegmentedBar<TypeSegment> typesBar = new SegmentedBar<>();

    private StackPane innerShadowPane = new StackPane();

    private StackPane contentPane = new StackPane();

    public HelloSegmentedBar() {
        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(20));
        
        hbox.setFillHeight(true);
        hbox.setPadding(new Insets(20));

        // The out of the box bar. It uses the already set default cell factory.
        simpleBar.orientationProperty().bind(orientation);
        simpleBar.getSegments().addAll(
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(10, "10"),
                new SegmentedBar.Segment(50, "50"));

        // A bar used for visualizing the number of issues (e.g. JIRA) based on
        // their status.
        issueStatusBar.orientationProperty().bind(orientation);
        issueStatusBar.setSegmentViewFactory(IssueStatusSegmentView::new);
        issueStatusBar.setInfoNodeFactory(segment -> new InfoLabel(segment.getStatus() + ": " + segment.getValue() + " Issues"));
        issueStatusBar.getSegments().addAll(
                new IssueStatusSegment(3, IssueStatus.TODO),
                new IssueStatusSegment(2, IssueStatus.INPROGRESS),
                new IssueStatusSegment(5, IssueStatus.DONE)
        );

        // A bar used to visualize the disk space used by various media types (e.g. iTunes).
        typesBar.orientationProperty().bind(orientation);
        typesBar.setSegmentViewFactory(TypeSegmentView::new);
        typesBar.setInfoNodeFactory(segment -> new InfoLabel(segment.getText() + " " + segment.getValue() + " GB"));
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
        innerShadowBar.setSegmentViewFactory(TypeSegmentView::new);
        innerShadowBar.setInfoNodeFactory(segment -> new InfoLabel(segment.getText() + " " + segment.getValue() + " GB"));
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

    class InfoLabel extends Label {

        public InfoLabel(String text) {
            super(text);
            setPadding(new Insets(4));
            setStyle("-fx-font-weight: bold; -fx-font-size: 1.2em;");
        }
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

        if (orientation.get().equals(Orientation.HORIZONTAL)) {
            vbox.getChildren().clear();
            vbox.getChildren().add(new WrapperPane("Simple Bar", simpleBar));
            vbox.getChildren().add(new WrapperPane("Issue Status (Hover for PopOver)", issueStatusBar));
            vbox.getChildren().add(new WrapperPane("Disk Usage (Hover for PopOver)", typesBar));
            vbox.getChildren().add(new WrapperPane("Inner Shadow (Hover for PopOver)", innerShadowBar, innerShadowPane));
            contentPane.getChildren().setAll(vbox);
        } else {
            hbox.getChildren().clear();
            hbox.getChildren().add(simpleBar);
            hbox.getChildren().add(issueStatusBar);
            hbox.getChildren().add(typesBar);
            hbox.getChildren().add(innerShadowPane);
            contentPane.getChildren().setAll(hbox);
        }
    }

    private class WrapperPane extends VBox {

        public WrapperPane(String title, SegmentedBar bar) {
            this(title, bar, bar);
        }

        public WrapperPane(String title, SegmentedBar bar, Node content) {
            BorderPane.setMargin(content, new Insets(5, 0, 0, 0));
            getChildren().add(new Label(title));
            getChildren().add(content);

            Label total = new Label();
            getChildren().add(total);
            total.setText("Total: " + bar.getTotal());
            bar.totalProperty().addListener(it -> total.setText("Total: " + bar.getTotal()));
        }
    }

    public class TypeSegmentView extends StackPane {

        private Label label;

        public TypeSegmentView(TypeSegment segment) {
            label = new Label();
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.2em;");
            label.setTextOverrun(OverrunStyle.CLIP);
            label.textProperty().bind(segment.textProperty());
            StackPane.setAlignment(label, Pos.CENTER_LEFT);

            getChildren().add(label);
            switch (segment.getType()) {
                case APPS:
                    setStyle("-fx-background-color: orange;");
                    break;
                case FREE:
                    setStyle("-fx-border-width: 1px; -fx-background-color: steelblue;");
                    break;
                case OTHER:
                    setStyle("-fx-background-color: green;");
                    break;
                case PHOTOS:
                    setStyle("-fx-background-color: purple;");
                    break;
                case VIDEO:
                    setStyle("-fx-background-color: cadetblue;");
                    break;
                case MUSIC:
                    setStyle("-fx-background-color: lightcoral;");
                    break;
            }
            setPadding(new Insets(5));
            setPrefHeight(30);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            label.setVisible(label.prefWidth(-1) < getWidth() - getPadding().getLeft() - getPadding().getRight());
        }
    }

    public class IssueStatusSegmentView extends Region {

        public IssueStatusSegmentView(final IssueStatusSegment segment) {
            setPrefHeight(16);
            setPrefWidth(16);

            switch (segment.getStatus()) {
                case DONE:
                    setStyle("-fx-background-color: green;");
                    break;
                case INPROGRESS:
                    setStyle("-fx-background-color: orange;");
                    break;
                case TODO:
                    setStyle("-fx-background-color: steelblue;");
                    break;
            }

            ContextMenu menu = new ContextMenu();
            for (int i = 1; i <= 10; i++) {
                MenuItem item = new MenuItem(Integer.toString(i));
                final int value = i;
                item.setOnAction(evt -> segment.setValue(value));
                menu.getItems().add(item);
            }

            setOnContextMenuRequested(evt -> menu.show(getScene().getWindow()));
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

            switch (type) {
                case APPS:
                    setText("Apps");
                    break;
                case FREE:
                    setText("Free");
                    break;
                case OTHER:
                    setText("Other");
                    break;
                case PHOTOS:
                    setText("Photos");
                    break;
                case VIDEO:
                    setText("Video");
                    break;
                case MUSIC:
                    setText("Music");
                    break;
            }
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
