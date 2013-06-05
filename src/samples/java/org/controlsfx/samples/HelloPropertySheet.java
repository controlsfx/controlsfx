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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.JPanel;

import org.controlsfx.Sample;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.property.BeanPropertyUtils;

public class HelloPropertySheet extends Application implements Sample {

    private PropertySheet propertySheet = new PropertySheet();
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override public String getSampleName() {
        return "Property Sheet";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/PropertySheet.html";
    }

    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Property Sheet");
        
        Scene scene = new Scene( (Parent)getPanel(primaryStage), 400, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override public Node getPanel(Stage stage) {
        
        
        VBox infoPane = new VBox(10);
        infoPane.setPadding( new Insets(20,20,20,20));
        
        ToggleButton b1 = new ToggleButton("Button");
        b1.setOnAction( new EventHandler<ActionEvent>() {
            
            @Override public void handle(ActionEvent arg0) {
                propertySheet.getItems().setAll(  BeanPropertyUtils.getProperties( new Button("Title")) );
            }
        });
        ToggleButton b2 = new ToggleButton("JPanel");
        b2.setOnAction( new EventHandler<ActionEvent>() {
            
            @Override public void handle(ActionEvent arg0) {
                propertySheet.getItems().setAll(  BeanPropertyUtils.getProperties( new JPanel()) );
            }
        });
        
        SegmentedButton segmentedButton_dark = new SegmentedButton(b1, b2);   
        segmentedButton_dark.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        b1.fire();
        infoPane.getChildren().add(segmentedButton_dark);
        
        
        SplitPane pane = new SplitPane();
        pane.getItems().addAll( infoPane, propertySheet );
        
        return pane;
    }
}