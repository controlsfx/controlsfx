/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.samples;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.InfoOverlay;
import org.controlsfx.control.SegmentedBar;

import java.util.Stack;

public class HelloSegmentedBar extends ControlsFXSample {

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "SegmentedBar";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedBar.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/segmentedbar.css";
    }
    
    @Override public String getSampleDescription() {
        return "A simple horizontal bar showing multiple segments, each representing a fraction of a total value.";
    }

    @Override public Node getPanel(Stage stage) {
        SegmentedBar bar = new SegmentedBar();
        bar.setPrefWidth(200);
        bar.getSegments().add(new SegmentedBar.Segment(30, "segment1"));
        bar.getSegments().add(new SegmentedBar.Segment(20, "segment2"));
        bar.getSegments().add(new SegmentedBar.Segment(50, "segment3"));

        StackPane wrapper = new StackPane();
        wrapper.getChildren().add(bar);
        wrapper.setPadding(new Insets(20));
        wrapper.setStyle("-fx-background-color: white; fx-border-color: gray;");
        wrapper.maxHeightProperty().bind(Bindings.add(bar.prefHeightProperty(), 40));
        wrapper.maxWidthProperty().bind(Bindings.add(bar.prefWidthProperty(), 100));

        StackPane.setAlignment(wrapper, Pos.CENTER);

        StackPane pane = new StackPane();
        StackPane.setAlignment(bar, Pos.CENTER);
        pane.getChildren().add(wrapper);

        return pane;
    }
    
    @Override public Node getControlPanel() {
        return null;
    }
}
