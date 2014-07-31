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
package org.controlsfx.samples.button;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.samples.Utils;

public class HelloSegmentedButton extends ControlsFXSample {
    
    @Override public String getSampleName() {
        return "SegmentedButton";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedButton.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/segmentedbutton.css";
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
        
        
        // without toggle group
        grid.add(new Label("SegmentedButton without a ToggleGroup (multiple selection): "), 0, 3);
        
        ToggleButton nogrp_b1 = new ToggleButton("day");
        ToggleButton nogrp_b2 = new ToggleButton("week");
        ToggleButton nogrp_b3 = new ToggleButton("month");
        ToggleButton nogrp_b4 = new ToggleButton("year");
        
        SegmentedButton segmentedButton_nogrp = new SegmentedButton(nogrp_b1, nogrp_b2, nogrp_b3, nogrp_b4);
        segmentedButton_nogrp.setToggleGroup(null);
        grid.add(segmentedButton_nogrp, 1, 3);
        
        
        // combined toggle group
        grid.add(new Label("SegmentedButtons with a combined ToggleGroup: "), 0, 4);
        ToggleGroup combgrp_grp = new ToggleGroup();
        
        ToggleButton combgrp_a_b1 = new ToggleButton("day");
        ToggleButton combgrp_a_b2 = new ToggleButton("week");
        ToggleButton combgrp_a_b3 = new ToggleButton("month");
        ToggleButton combgrp_a_b4 = new ToggleButton("year");
        
        SegmentedButton segmentedButton_combgrp_a = new SegmentedButton(combgrp_a_b1, combgrp_a_b2, combgrp_a_b3, combgrp_a_b4);
        segmentedButton_combgrp_a.setToggleGroup(combgrp_grp);
        
        ToggleButton combgrp_b_b1 = new ToggleButton("hour");
        ToggleButton combgrp_b_b2 = new ToggleButton("minute");
        ToggleButton combgrp_b_b3 = new ToggleButton("second");
        
        SegmentedButton segmentedButton_combgrp_b = new SegmentedButton(combgrp_b_b1, combgrp_b_b2, combgrp_b_b3);
        segmentedButton_combgrp_b.setToggleGroup(combgrp_grp);
        
        HBox combgrp_pane = new HBox(5);
        combgrp_pane.getChildren().addAll(segmentedButton_combgrp_a, segmentedButton_combgrp_b);
        grid.add(combgrp_pane, 1, 4);
        

        return grid;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
