/**
 * Copyright (c) 2014, ControlsFX
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

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.InfoOverlay;
import org.controlsfx.control.SegmentedBar;

import java.util.Stack;

public class HelloSegmentedBar extends ControlsFXSample {

    public static void main(String[] args) {
        launch(args);
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
        return "A simple horizontal bar showing multiple segments, each representing a fraction of a total value.";
    }

    @Override
    public Node getPanel(Stage stage) {
        SegmentedBar<SegmentedBar.Segment> simpleBar = new SegmentedBar<>();

        simpleBar.setPrefWidth(600);
        simpleBar.getSegments().add(new SegmentedBar.Segment(30, "segment1"));
        simpleBar.getSegments().add(new SegmentedBar.Segment(20, "segment2"));
        simpleBar.getSegments().add(new SegmentedBar.Segment(50, "segment3"));

        SegmentedBar<TypeSegment> typesBar = new SegmentedBar<>();
        typesBar.setPrefWidth(600);
        typesBar.setCellFactory(segment -> new TypeSegmentView(segment));
        typesBar.getSegments().add(new TypeSegment(14, "Photos", "segment1"));
        typesBar.getSegments().add(new TypeSegment(32, "Videos", "segment2"));
        typesBar.getSegments().add(new TypeSegment(9, "Apps", "segment3"));
        typesBar.getSegments().add(new TypeSegment(40, "Music", "segment4"));
        typesBar.getSegments().add(new TypeSegment(5, "Other", "segment5"));
        typesBar.getSegments().add(new TypeSegment(35, "Free", "segment6"));

        VBox box = new VBox();
        box.setSpacing(40);
        box.setFillWidth(true);
        box.getChildren().addAll(simpleBar, typesBar);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: white; fx-border-color: gray;");
        box.maxHeightProperty().bind(Bindings.add(simpleBar.prefHeightProperty(), 40));
        box.maxWidthProperty().bind(Bindings.add(simpleBar.prefWidthProperty(), 100));

        StackPane.setAlignment(box, Pos.CENTER);

        StackPane pane = new StackPane();
        StackPane.setAlignment(simpleBar, Pos.CENTER);
        pane.getChildren().add(box);

        return pane;
    }

    public static class TypeSegmentView extends Label {

        public TypeSegmentView(TypeSegment segment) {
            super(segment.getName());
            setTextOverrun(OverrunStyle.CLIP);
            setStyle("-fx-pref-height: 30; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.2em;");
        }
    }

    public static class TypeSegment extends SegmentedBar.Segment {

        private String name;

        public TypeSegment(double value, String name, String style) {
            super(value, style);
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Override
    public Node getControlPanel() {
        return null;
    }
}
