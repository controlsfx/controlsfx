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

import static org.controlsfx.control.ButtonBar.ButtonType.APPLY;
import static org.controlsfx.control.ButtonBar.ButtonType.BACK_PREVIOUS;
import static org.controlsfx.control.ButtonBar.ButtonType.CANCEL_CLOSE;
import static org.controlsfx.control.ButtonBar.ButtonType.FINISH;
import static org.controlsfx.control.ButtonBar.ButtonType.HELP;
import static org.controlsfx.control.ButtonBar.ButtonType.HELP_2;
import static org.controlsfx.control.ButtonBar.ButtonType.LEFT;
import static org.controlsfx.control.ButtonBar.ButtonType.NEXT_FORWARD;
import static org.controlsfx.control.ButtonBar.ButtonType.NO;
import static org.controlsfx.control.ButtonBar.ButtonType.OK_DONE;
import static org.controlsfx.control.ButtonBar.ButtonType.OTHER;
import static org.controlsfx.control.ButtonBar.ButtonType.RIGHT;
import static org.controlsfx.control.ButtonBar.ButtonType.YES;

import java.util.Arrays;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;

public class HelloButtonBar extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "ButtonBar";
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        
        final ButtonBar buttonBar = new ButtonBar();
        buttonBar.setButtonMinWidth(0);
        
        // explanation text
        Label details = new Label("The ButtonBar allows for buttons to be positioned" +
        		" in a way that is OS-specific (or in any way that suits your use case." +
        		" For example, try toggling the OS toggle buttons below (note, you'll want " +
        		"to increase the width of this window first!\n\n" +
        		"Note that, to allow for this sample to display the buttons are not configured" +
        		" to use a consistent minimum width, but that is possible vis API on ButtonBar.");
        details.setWrapText(true);
        root.getChildren().add(details);
        
        // create toggle button to switch button orders 
        HBox buttonOrderHbox = new HBox(10);
        ToggleGroup group = new ToggleGroup();
        final ToggleButton windowsBtn = new ToggleButton("Windows");
        final ToggleButton macBtn = new ToggleButton("Mac OS");
        final ToggleButton linuxBtn = new ToggleButton("Linux");
        buttonOrderHbox.getChildren().setAll(windowsBtn, macBtn, linuxBtn);
        group.getToggles().setAll(windowsBtn, macBtn, linuxBtn);
        windowsBtn.setSelected(true);
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (windowsBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_WINDOWS);
                } else if (macBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_MAC_OS);
                } else if (linuxBtn.equals(newValue)) {
                    buttonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_LINUX);
                }
            }
        });
        root.getChildren().add(buttonOrderHbox);
        VBox.setVgrow(buttonOrderHbox, Priority.NEVER);
        
        final ToggleButton uniformButtonBtn = new ToggleButton("Uniform Button Size");
        uniformButtonBtn.selectedProperty().bindBidirectional( buttonBar.buttonUniformSizeProperty());
        HBox panel = new HBox(uniformButtonBtn);
        
        // spacer to push button bar to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);
        root.getChildren().add( panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
        
        // create button bar
        buttonBar.getButtons().addAll(Arrays.asList(
                createButton("OK", OK_DONE),
                createButton("Cancel", CANCEL_CLOSE),
                createButton("Left 1", LEFT),
                createButton("Left 2", LEFT),
                createButton("Left 3", LEFT),
                createButton("Right 1", RIGHT),
                createButton("Unknown 1", OTHER),
                createButton("Help(R)", HELP),
                createButton("Help(L)", HELP_2),
                createButton("Unknown 2 xxxxxxxxxx", OTHER),
                createButton("Yes", YES),
                createButton("No", NO),
                createButton("Next", NEXT_FORWARD),
                createButton("Unknown 3", OTHER),
                createButton("Back", BACK_PREVIOUS),
                createButton("Right 2", RIGHT),
                createButton("Finish", FINISH),
                createButton("Right 3", RIGHT),
                createButton("Apply", APPLY)
                
        ));
        root.getChildren().add(buttonBar);
        
        root.getChildren().add(buttonBar);
        VBox.setVgrow(buttonBar, Priority.NEVER);
        
        return root;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("ButtonBar Demo");
        
        Scene scene = new Scene((Parent)getPanel(stage), 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    private Button createButton( String title, ButtonType type) {
        Button button = new Button(title);
        ButtonBar.setType(button, type);
        return button;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}