/**
 * Copyright (c) 2013, 2015 ControlsFX
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

import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.samples.Utils;

public class HelloButtonBar extends ControlsFXSample {
    
    private ButtonBar buttonBar;

    @Override public String getSampleName() {
        return "ButtonBar";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/ButtonBar.html";
    }
    
    @Override public String getSampleDescription() {
        return "The ButtonBar allows for buttons to be positioned" +
                " in a way that is OS-specific (or in any way that suits your use case." +
                " For example, try toggling the OS toggle buttons below (note, you'll want " +
                "to increase the width of this window first!)";
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);
        
        buttonBar = new ButtonBar();

        // spacer to push button bar to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);
        
        // create button bar
        buttonBar.getButtons().addAll(
            createButton("OK", ButtonBar.ButtonData.OK_DONE),
            createButton("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE),
            createButton("Left 1", ButtonBar.ButtonData.LEFT),
            createButton("Left 2", ButtonBar.ButtonData.LEFT),
            createButton("Left 3", ButtonBar.ButtonData.LEFT),
            createButton("Right 1", ButtonBar.ButtonData.RIGHT),
            createButton("Unknown 1", ButtonBar.ButtonData.OTHER),
            createButton("Help(R)", ButtonBar.ButtonData.HELP),
            createButton("Help(L)", ButtonBar.ButtonData.HELP_2),
            createButton("Unknown 2 xxxxxxxxxx", ButtonBar.ButtonData.OTHER),
            createButton("Yes", ButtonBar.ButtonData.YES),
            createButton("No", ButtonBar.ButtonData.NO),
            createButton("Next", ButtonBar.ButtonData.NEXT_FORWARD),
            createButton("Unknown 3", ButtonBar.ButtonData.OTHER),
            createButton("Back", ButtonBar.ButtonData.BACK_PREVIOUS),
            createButton("Right 2", ButtonBar.ButtonData.RIGHT),
            createButton("Finish", ButtonBar.ButtonData.FINISH),
            createButton("Right 3", ButtonBar.ButtonData.RIGHT),
            createButton("Apply", ButtonBar.ButtonData.APPLY)
        );
        
        // put the ButtonBar inside a ScrollPane so that the user can scroll horizontally
        // when the button width is large
        ScrollPane sp = new ScrollPane(buttonBar);
        sp.setStyle("-fx-background-color: -fx-background; -fx-background-insets: 0");
        
        root.getChildren().add(sp);
        VBox.setVgrow(sp, Priority.ALWAYS);
        
        return root;
    }
    
    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;

        // operating system button order
        Label osChoiceLabel = new Label("Operating system button order: ");
        osChoiceLabel.getStyleClass().add("property");
        grid.add(osChoiceLabel, 0, 0);
        final ToggleButton windowsBtn = createToggle("Windows", buttonBar, ButtonBar.BUTTON_ORDER_WINDOWS);
        final ToggleButton macBtn = createToggle("Mac OS", buttonBar, ButtonBar.BUTTON_ORDER_MAC_OS);
        final ToggleButton linuxBtn = createToggle("Linux", buttonBar, ButtonBar.BUTTON_ORDER_LINUX);
        windowsBtn.selectedProperty().set(true);
        SegmentedButton operatingSystem = new SegmentedButton(
                FXCollections.observableArrayList(windowsBtn, macBtn, linuxBtn));
        grid.add(operatingSystem, 1, row);
        row++;
        
        // uniform size
        Label uniformSizeLabel = new Label("Set all buttons to a uniform size: ");
        uniformSizeLabel.getStyleClass().add("property");
        grid.add(uniformSizeLabel, 0, row);
        final CheckBox uniformButtonBtn = new CheckBox();
        uniformButtonBtn.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isUniform) {
                for(Node button :buttonBar.getButtons()){
                    ButtonBar.setButtonUniformSize(button, isUniform);
                }
            }
        });
//        uniformButtonBtn.selectedProperty().bindBidirectional( buttonBar.buttonUniformSizeProperty());
        grid.add(uniformButtonBtn, 1, row);
        row++;
        
        // minimum size slider / label
        Label minSizeLabel = new Label("Button min size: ");
        minSizeLabel.getStyleClass().add("property");
        grid.add(minSizeLabel, 0, row);
        
        final Slider minSizeSlider = new Slider(0, 200, 0);
        Label pixelCountLabel = new Label();
        pixelCountLabel.textProperty().bind(new StringBinding() {
            { bind(minSizeSlider.valueProperty()); }
            
            @Override protected String computeValue() {
                return (int)minSizeSlider.getValue() + "px";
            }
        });
        HBox minSizeBox = new HBox(10, minSizeSlider, pixelCountLabel);
        buttonBar.buttonMinWidthProperty().bind(minSizeSlider.valueProperty());
        grid.add(minSizeBox, 1, row);
        row++;
        
        return grid;
    }
    
    private ToggleButton createToggle( final String caption, final ButtonBar buttonBar, final String buttonBarOrder ) {
        final ToggleButton btn = new ToggleButton(caption);
        btn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
                if ( newValue) buttonBar.setButtonOrder(buttonBarOrder);
            }});
        return btn;
    }
     
    private Button createButton( String title, ButtonBar.ButtonData buttonData) {
        Button button = new Button(title);
        ButtonBar.setButtonData(button, buttonData);
        return button;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}