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
package org.controlsfx.samples.dialogs;

import java.util.Date;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.dialog.Dialogs;
import org.controlsfx.samples.Utils;

import fxsampler.Sample;

public class HelloLightweightDialogInTabPane extends Application implements Sample {

    @Override public String getSampleName() {
        return "Lightweight Dialogs";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/dialog/Dialogs.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    private Stage stage;
    
    
    @Override public Node getPanel(final Stage stage) {
        final Tab tab1 = new Tab("Tab 1");
        buildTab1(tab1);
        
        final Tab tab2 = new Tab("Tab 2");
        buildTab2(tab2);

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        tabPane.getTabs().addAll(tab1, tab2);
        tabPane.setPadding(new Insets(10));
        
        StackPane pane = new StackPane(tabPane);
        return pane;
    }
    
    private void buildTab1(final Tab tab1) {
        Button showDialogBtn = new Button("Show lightweight dialog in this Tab");
        showDialogBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                Dialogs.create()
                    .lightweight()
                    .owner(tab1)
                    .title("Lightweight Dialog")
                    .message("This should only block Tab 1 - try going to Tab 2")
                    .showInformation();
            }
        });
        
        Button printToConsole = new Button("Print to console");
        printToConsole.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println(new Date());
            }
        });
        
        VBox tab1Content = new VBox(10);
        tab1Content.setPadding(new Insets(10));
        tab1Content.getChildren().setAll(showDialogBtn, printToConsole);
        
        tab1.setContent(tab1Content);
    }
    
    private void buildTab2(final Tab tab2) {
        final ListView<String> listView = new ListView<>();
        listView.getItems().setAll("Jonathan", "Eugene", "Hendrik", "Danno", "Paru");
        GridPane.setHgrow(listView, Priority.ALWAYS);
        
        Button showDialogBtn = new Button("Show dialog in list");
        showDialogBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                Dialogs.create()
                    .lightweight()
                    .owner(listView)
                    .title("Lightweight Dialog")
                    .message("This should only block the listview")
                    .showInformation();
            }
        });
        
        Button printToConsole = new Button("Print to console");
        printToConsole.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                System.out.println(new Date());
            }
        });
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        
        grid.add(listView, 0, 0, 1, 3);
        grid.add(showDialogBtn, 1, 0);
        grid.add(printToConsole, 1, 1);
        
        tab2.setContent(grid);
    }

    @Override public void start(final Stage stage) {
        // setUserAgentStylesheet(STYLESHEET_MODENA);
        this.stage = stage;

        stage.setTitle("Lightweight Dialogs Sample");

        Scene scene = new Scene((Parent)getPanel(stage), 800, 400);
        scene.setFill(Color.WHITE);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
}
