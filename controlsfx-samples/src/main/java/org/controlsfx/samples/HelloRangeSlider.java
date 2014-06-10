/**
 * Copyright (c) 2013, ControlsFX
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

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.RangeSlider;

public class HelloRangeSlider extends ControlsFXSample {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "RangeSlider";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/RangeSlider.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/rangeslider.css";
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(15);
        
        Region horizontalRangeSlider = createHorizontalSlider();
        Region verticalRangeSlider = createVerticalSlider();
        root.getChildren().addAll(horizontalRangeSlider, verticalRangeSlider);
        
        return root;
    }
    
    @Override public String getSampleDescription() {
        return "The Slider control in JavaFX is great for selecting a single "
                + "value between a min and max value, but it isn't so great for "
                + "letting users select a range - that's where RangeSlider comes in!";
    }
    
    Region createHorizontalSlider() {
        final TextField minField = new TextField();
        minField.setPrefColumnCount(5);
        final TextField maxField = new TextField();
        maxField.setPrefColumnCount(5);

        final RangeSlider hSlider = new RangeSlider(0, 100, 10, 90);
        hSlider.setShowTickMarks(true);
        hSlider.setShowTickLabels(true);
        hSlider.setBlockIncrement(10);
        hSlider.setPrefWidth(200);

        minField.setText("" + hSlider.getLowValue());
        maxField.setText("" + hSlider.getHighValue());

        minField.setEditable(false);
        minField.setPromptText("Min");

        maxField.setEditable(false);
        maxField.setPromptText("Max");

        minField.textProperty().bind(hSlider.lowValueProperty().asString("%.2f"));
        maxField.textProperty().bind(hSlider.highValueProperty().asString("%.2f"));

        HBox box = new HBox(10);
        box.getChildren().addAll(minField, hSlider, maxField);
        box.setPadding(new Insets(20,0,0,20));
        box.setFillHeight(false);

        return box;
    }
    
    
    Region createVerticalSlider() {
        final TextField minField = new TextField();
        minField.setPrefColumnCount(5);
        final TextField maxField = new TextField();
        maxField.setPrefColumnCount(5);

        final RangeSlider vSlider = new RangeSlider(0, 200, 30, 150);
        vSlider.setOrientation(Orientation.VERTICAL);
        vSlider.setPrefHeight(200);
        vSlider.setBlockIncrement(10);
        vSlider.setShowTickMarks(true);
        vSlider.setShowTickLabels(true);

        minField.setText("" + vSlider.getLowValue());
        maxField.setText("" + vSlider.getHighValue());

        minField.setEditable(false);
        minField.setPromptText("Min");

        maxField.setEditable(false);
        maxField.setPromptText("Max");

        minField.textProperty().bind(vSlider.lowValueProperty().asString("%.2f"));
        maxField.textProperty().bind(vSlider.highValueProperty().asString("%.2f"));

        VBox box = new VBox(10);
        box.setPadding(new Insets(0,0,0, 20));
//        box.setAlignment(Pos.CENTER);
        box.setFillWidth(false);
        box.getChildren().addAll(maxField, vSlider, minField);
        return box;
    }
    
}
