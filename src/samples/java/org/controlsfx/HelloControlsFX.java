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
package org.controlsfx;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.samples.HelloButtonBar;
import org.controlsfx.samples.HelloDialog;
import org.controlsfx.samples.HelloGridView;
import org.controlsfx.samples.HelloRangeSlider;
import org.controlsfx.samples.HelloRating;
import org.controlsfx.samples.HelloSegmentedButton;
import org.controlsfx.samples.HelloToggleSwitch;
import org.controlsfx.samples.SVGTest;

public class HelloControlsFX extends Application {

    // TODO dynamically discover samples
    private final Class[] samplesArray = new Class[] {
        HelloButtonBar.class,
        HelloDialog.class,
        HelloGridView.class,
        HelloRangeSlider.class,
        HelloRating.class,
        HelloSegmentedButton.class,
        HelloToggleSwitch.class,
        SVGTest.class
    };
    
    private GridPane grid;
    private StackPane samplePane;
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public void start(final Stage primaryStage) throws Exception {
        // instantiate the samples
        ObservableList<Sample> samples = FXCollections.observableArrayList();
        for (Class clazz : samplesArray) {
            Sample sample = (Sample) clazz.newInstance();
            samples.add(sample);
        }
        
        // simple layout: ListView on left, sample area on right
        
        grid = new GridPane();
        
        //left hand side
        ListView<Sample> samplesListView = new ListView<>(samples);
        samplesListView.setMinWidth(150);
        samplesListView.setMaxWidth(150);
        samplesListView.setCellFactory(new Callback<ListView<Sample>, ListCell<Sample>>() {
            @Override public ListCell<Sample> call(ListView<Sample> param) {
                return new ListCell<Sample>() {
                    @Override protected void updateItem(Sample item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setText("");
                        } else {
                            setText(item.getSampleName());
                        }
                    }
                };
            }
        });
        samplesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
            @Override public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newSample) {
                changeSample(newSample, primaryStage);
            }
        });
        GridPane.setVgrow(samplesListView, Priority.ALWAYS);
        GridPane.setMargin(samplesListView, new Insets(10, 10, 10, 10));
        grid.add(samplesListView, 0, 0);
        
        // right hand side
        samplePane = new StackPane();
        samplePane.setStyle("-fx-border-color: lightgray");
        samplePane.setMinWidth(600);
//        samplePane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(samplePane, Priority.ALWAYS);
        GridPane.setVgrow(samplePane, Priority.ALWAYS);
        GridPane.setMargin(samplePane, new Insets(10, 10, 10, 0));
        grid.add(samplePane, 1, 0);
        
        // by default we'll have a welcome message in the right-hand side
        samplePane.getChildren().add(buildInitialVBox());
        
        // put it all together
        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.setTitle("ControlsFX!");
        primaryStage.show();
    }

    private void changeSample(Sample newSample, final Stage stage) {
        samplePane.getChildren().setAll(newSample.getPanel(stage));
    }
    
    private Node buildInitialVBox() {
        // line 1
        Label welcomeLabel1 = new Label("Welcome to ControlsFX!");
        welcomeLabel1.setStyle("-fx-font-size: 2em; -fx-padding: 0 0 0 5;");
        
        // line 2
        Label welcomeLabel2 = new Label("Explore the available UI controls by clicking on the options to the left.\n\n" +
                "There have been many contributors to this project, including:\n" +
                "   Jonathan Giles\n" +
                "   Eugene Ryzhikov\n" +
                "   Hendrik Ebbers\n" +
                "   Danno Ferrin\n" +
                "   Paru Somashekar\n\n" +
                "If you ever meet any of these wonderful contributors, tell them how great they are! :-)\n\n" +
                "To keep up to date with the ControlsFX project, visit the website at http://www.fxexperience.com/controlsfx");
        welcomeLabel2.setStyle("-fx-font-size: 1.25em; -fx-padding: 0 0 0 5;");
        
        VBox initialVBox = new VBox(5, welcomeLabel1, welcomeLabel2);
        return initialVBox;
    }
}
