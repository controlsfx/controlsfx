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

import java.util.Comparator;
import java.util.function.Predicate;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.samples.HelloActionGroup;
import org.controlsfx.samples.HelloButtonBar;
import org.controlsfx.samples.HelloDialog;
import org.controlsfx.samples.HelloGridView;
import org.controlsfx.samples.HelloPropertySheet;
import org.controlsfx.samples.HelloRangeSlider;
import org.controlsfx.samples.HelloRating;
import org.controlsfx.samples.HelloSegmentedButton;
import org.controlsfx.samples.HelloWorkerProgressPane;

public class HelloControlsFX extends Application {

    // TODO dynamically discover samples
    private final Class<?>[] samplesArray = new Class<?>[] {
        HelloButtonBar.class,
        HelloDialog.class,
        HelloGridView.class,
        HelloRangeSlider.class,
        HelloRating.class,
        HelloSegmentedButton.class,
        HelloActionGroup.class,
        HelloWorkerProgressPane.class,
        HelloPropertySheet.class
//        SVGTest.class
    };
    
    private GridPane grid;

    private TabPane tabPane;
    private Tab welcomeTab;
    private Tab sampleTab;
    private Tab javadocTab;
    
    private WebView webview;
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public void start(final Stage primaryStage) throws Exception {
        setUserAgentStylesheet(STYLESHEET_MODENA);
        
        // instantiate the samples into a normal list
        ObservableList<Sample> samples = FXCollections.observableArrayList();
        for (Class<?> clazz : samplesArray) {
            Sample sample = (Sample) clazz.newInstance();
            samples.add(sample);
        }
        
        // then we'll sort that list based on the sample names
        final SortedList<Sample> sortedSamples = new SortedList<Sample>(samples, new Comparator<Sample>() {
            @Override public int compare(Sample s1, Sample s2) {
                return s1.getSampleName().compareTo(s2.getSampleName());
            }
        });
        
        final FilteredList<Sample> filteredSamples = new FilteredList<>(sortedSamples);
        
        
        // simple layout: ListView on left, sample area on right
        
        grid = new GridPane();
        grid.setPadding(new Insets(5, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);
        
        // --- left hand side
        // firstly, we have a search box
        final TextField searchBox = new TextField();
        searchBox.setPromptText("Type to filter samples");
        searchBox.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent e) {
                final String typedInput = searchBox.getText().toUpperCase(); 
                
                Predicate<Sample> predicate = new Predicate<Sample>() {
                    @Override public boolean test(Sample t) {
                        return t.getSampleName().toUpperCase().contains(typedInput);
                    }
                };
                
                filteredSamples.setPredicate(predicate);
            }
        });
        GridPane.setMargin(searchBox, new Insets(5, 0, 0, 0));
        grid.add(searchBox, 0, 0);
        
        // then the listview goes beneath the search box
        ListView<Sample> samplesListView = new ListView<>(filteredSamples);
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
        grid.add(samplesListView, 0, 1);
        
        // right hand side
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        tabPane.getStyleClass().add(TabPane.STYLE_CLASS_FLOATING);
        GridPane.setHgrow(tabPane, Priority.ALWAYS);
        GridPane.setVgrow(tabPane, Priority.ALWAYS);
        grid.add(tabPane, 1, 0, 1, 2);
        
        sampleTab = new Tab("Sample");
        javadocTab = new Tab("JavaDoc");
        webview = new WebView();
    	javadocTab.setContent(webview);
        
        // by default we'll have a welcome message in the right-hand side
        buildInitialVBox();
        
        // put it all together
        Scene scene = new Scene(grid);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(600);
        primaryStage.setTitle("ControlsFX!");
        primaryStage.show();
        
        samplesListView.requestFocus();
    }

    private void changeSample(Sample newSample, final Stage stage) {
        if (newSample == null) {
            return;
        }
        
    	if (tabPane.getTabs().contains(welcomeTab)) {
    		tabPane.getTabs().setAll(sampleTab, javadocTab);
    	}
    	
    	// update the sample tab
    	sampleTab.setContent(newSample.getPanel(stage));
    	
    	// update the javadoc tab
    	webview.getEngine().load(newSample.getJavaDocURL());
    }
    
    private void buildInitialVBox() {
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
        
        welcomeTab = new Tab("Welcome to ControlsFX!");
        welcomeTab.setContent(initialVBox);
        
        tabPane.getTabs().add(welcomeTab);
    }
}
