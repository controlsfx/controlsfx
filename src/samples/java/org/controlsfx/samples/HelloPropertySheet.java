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
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.ActionUtils;
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
    
    @Override public boolean includeInSamples() {
        return true;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Property Sheet");
        
        Scene scene = new Scene( (Parent)getPanel(primaryStage), 800, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class ActionShowInPropertySheet extends AbstractAction {
        
        private Object bean;

        public ActionShowInPropertySheet( String title, Object bean ) {
            super(title);
            this.bean = bean;
        }

        @Override public void execute(ActionEvent ae) {
            
            // retrieving bean properties may take some time
            // so we have to put it on separated thread to keep UI responsive

            Service<?> service = new Service<ObservableList<Item>>() {

                @Override protected Task<ObservableList<Item>> createTask() {
                    return new Task<ObservableList<Item>>() {
                        @Override protected ObservableList<Item> call() throws Exception {
                            return BeanPropertyUtils.getProperties(bean);
                        }
                    };
                }

            };
            service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

                @SuppressWarnings("unchecked") @Override public void handle(WorkerStateEvent e) {
                    propertySheet.getItems().setAll((ObservableList<Item>) e.getSource().getValue());

                }
            });
            service.start();
            
        }
        
    }
    
    @Override public Node getPanel(Stage stage) {
        
        VBox infoPane = new VBox(10);
        infoPane.setPadding( new Insets(20,20,20,20));
        
        Button button = new Button("Title");
        TextField textField = new TextField();
        SegmentedButton segmentedButton = ActionUtils.createSegmentedButton(
                new ActionShowInPropertySheet( "Bean: Button", button ),
                new ActionShowInPropertySheet( "Bean: TextField", textField )
            );
        segmentedButton.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);
        segmentedButton.getButtons().get(0).fire();
        
        CheckBox toolbarVisible = new CheckBox("Toolbar Visible");
        toolbarVisible.selectedProperty().bindBidirectional( propertySheet.toolbarVisibleProperty() );

        CheckBox toolbarModeVisible = new CheckBox("Toolbar Mode Visible");
        toolbarModeVisible.selectedProperty().bindBidirectional( propertySheet.toolbarModeVisibleProperty() );
        

        CheckBox toolbarSeacrhVisible = new CheckBox("Toolbar Search Visible");
        toolbarSeacrhVisible.selectedProperty().bindBidirectional( propertySheet.toolbarSearchVisibleProperty() );
        
        infoPane.getChildren().add(toolbarVisible);
        infoPane.getChildren().add(toolbarModeVisible);
        infoPane.getChildren().add(toolbarSeacrhVisible);
        infoPane.getChildren().add(segmentedButton);
        infoPane.getChildren().add(button);
        infoPane.getChildren().add(textField);
        
        
        
        SplitPane pane = new SplitPane();
        pane.getItems().addAll( infoPane, propertySheet );
        
        return pane;
    }
    
    class ActionModeChange extends AbstractAction {
        
        private Mode mode;
        
        public ActionModeChange( String title, Mode mode ) {
            super( title );
            this.mode = mode;
        }

        @Override public void execute(ActionEvent ae) {
            propertySheet.modeProperty().set(mode);
        }
        
    }
    
}