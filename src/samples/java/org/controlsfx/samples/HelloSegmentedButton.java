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

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.SegmentedButton;

public class HelloSegmentedButton extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "SegmentedButton";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedButton.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        // without segmented button
        grid.add(new Label("Without SegmentedButton (with 10px spacing): "), 0, 0);
        
        ToggleButton without_b1 = new ToggleButton("day"); 
        ToggleButton without_b2 = new ToggleButton("week");
        ToggleButton without_b3 = new ToggleButton("month");
        ToggleButton without_b4 = new ToggleButton("year");
        
        final ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(without_b1, without_b2, without_b3, without_b4);
        
        HBox toggleButtons = new HBox(without_b1, without_b2, without_b3, without_b4);
        toggleButtons.setSpacing(10);
        grid.add(toggleButtons, 1, 0);
        
        
        // Using modena segmented button
        grid.add(new Label("With SegmentedButton (with default (modena) styling): "), 0, 1);
        
        ToggleButton modena_b1 = new ToggleButton("day");
        ToggleButton modena_b2 = new ToggleButton("week");
        ToggleButton modena_b3 = new ToggleButton("month");
        ToggleButton modena_b4 = new ToggleButton("year");
        SegmentedButton segmentedButton_modena = new SegmentedButton(modena_b1, modena_b2, modena_b3, modena_b4); 
        grid.add(segmentedButton_modena, 1, 1);
        
        
        // with segmented button and dark styling
        grid.add(new Label("With SegmentedButton (using dark styling): "), 0, 2);
        
        ToggleButton dark_b1 = new ToggleButton("day");
        ToggleButton dark_b2 = new ToggleButton("week");
        ToggleButton dark_b3 = new ToggleButton("month");
        ToggleButton dark_b4 = new ToggleButton("year");
        
        SegmentedButton segmentedButton_dark = new SegmentedButton(dark_b1, dark_b2, dark_b3, dark_b4);   
        segmentedButton_dark.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        grid.add(segmentedButton_dark, 1, 2);
        

        return grid;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("SegmentedButton Demo");
        
        
        Scene scene = new Scene((Parent) getPanel(stage), 350, 150);
//        scene.getStylesheets().addAll(SegmentedButton.class.getResource("segmentedbutton.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
