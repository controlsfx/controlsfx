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

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.InfoOverlay;

public class HelloInfoOverlay extends ControlsFXSample {

    private InfoOverlay infoOverlay;
    private Slider fitHeightSlider = new Slider(250, 800, 400);
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "InfoOverlay";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/InfoOverlay.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/info-overlay.css";
    }
    
    @Override public String getSampleDescription() {
        return "A simple UI control that allows for an information popup to be "
                + "displayed over a node to describe it in further detail. In "
                + "some ways, it can be thought of as a always visible tooltip "
                + "(although by default it is collapsed so only the first line is "
                + "shown - clicking on it will expand it to show all text).";
    }
    
    @Override public Node getPanel(Stage stage) {
        String imageUrl = getClass().getResource("duke_wave.png").toExternalForm();
        ImageView image = new ImageView(imageUrl);
        image.fitHeightProperty().bind(fitHeightSlider.valueProperty());
        image.setPreserveRatio(true);

        String info = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Nam tortor felis, pulvinar in scelerisque cursus, pulvinar at ante. " +
                "Nulla consequat congue lectus in sodales.";

        infoOverlay = new InfoOverlay(image, info);
        
        StackPane stackPane = new StackPane(infoOverlay);
        
        return stackPane;
    }
    
    @Override public Node getControlPanel() {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));
        
        int row = 0;

        // fit height
        Label imageHeightLabel = new Label("Image Size: ");
        imageHeightLabel.getStyleClass().add("property");
        grid.add(imageHeightLabel, 0, row);
        grid.add(fitHeightSlider, 1, row++);
        
        // show on hover
        Label showOnHoverLabel = new Label("Show overlay on hover: ");
        showOnHoverLabel.getStyleClass().add("property");
        grid.add(showOnHoverLabel, 0, row);
        CheckBox showOnHoverChk = new CheckBox();
        showOnHoverChk.setSelected(true);
        infoOverlay.showOnHoverProperty().bind(showOnHoverChk.selectedProperty());
        grid.add(showOnHoverChk, 1, row++);
        
        return grid;
    }
}
