/**
 * Copyright (c) 2016, ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.WorldMapView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelloWorldMapView extends ControlsFXSample {

    private WorldMapView worldMapView = new WorldMapView();

    public HelloWorldMapView() {
    }

    @Override
    public String getSampleName() {
        return "WorldMapView";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/WorldMapView.html";
    }


    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/control/world.css";
    }

    @Override
    public String getSampleDescription() {
        return "A map view of the world.";
    }

    @Override
    public Node getPanel(Stage stage) {
        StackPane stackPane = new StackPane();
        StackPane.setAlignment(worldMapView, Pos.CENTER);
        stackPane.getChildren().add(worldMapView);

        worldMapView.getLocations().addAll(
                new WorldMapView.Location("SFO", 37.619751, -122.374366),
                new WorldMapView.Location("YYC", 51.128148, -114.010791),
                new WorldMapView.Location("ORD", 41.975806, -87.905294),
                new WorldMapView.Location("YOW", 45.321867, -75.668200),
                new WorldMapView.Location("JFK", 40.642660, -73.781232),
                new WorldMapView.Location("GRU", -23.427337, -46.478853),
                new WorldMapView.Location("RKV", 64.131830, -21.945686),
                new WorldMapView.Location("MAD", 40.483162, -3.579211),
                new WorldMapView.Location("CDG", 49.014162, 2.541908),
                new WorldMapView.Location("LHR", 51.471125, -0.461951),
                new WorldMapView.Location("SVO", 55.972401, 37.412537),
                new WorldMapView.Location("DEL", 28.555839, 77.100956),
                new WorldMapView.Location("PEK", 40.077624, 116.605458),
                new WorldMapView.Location("NRT", 35.766948, 140.385254),
                new WorldMapView.Location("SYD", -33.939040, 151.174996));

        return stackPane;
    }

    @Override
    public Node getControlPanel() {
        List<WorldMapView.Country> countries = new ArrayList<>();
        for (WorldMapView.Country c : WorldMapView.Country.values()) {
            countries.add(c);
        }

        Collections.sort(countries, (c1, c2) -> c1.getLocale().getDisplayCountry().compareTo(c2.getLocale().getDisplayCountry()));

        ListView<WorldMapView.Country> listView = new ListView<>();
        listView.getItems().setAll(countries);
        listView.setCellFactory(list -> new ListCell<WorldMapView.Country>() {
            @Override
            protected void updateItem(WorldMapView.Country item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getLocale().getDisplayCountry());
                }
            }
        });

        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().getSelectedItems().addListener((Observable it) -> {
            worldMapView.getCountries().setAll(listView.getSelectionModel().getSelectedItems());
        });

        Button clearButton = new Button("Clear / Show World");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(evt -> listView.getSelectionModel().clearSelection());

        Slider zoomSlider = new Slider();
        zoomSlider.setMin(1);
        zoomSlider.setMax(10);
        Bindings.bindBidirectional(zoomSlider.valueProperty(), worldMapView.zoomFactorProperty());

        ComboBox<WorldMapView.SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().addAll(WorldMapView.SelectionMode.values());
        Bindings.bindBidirectional(selectionModeComboBox.valueProperty(), worldMapView.selectionModeProperty());

        HBox hbox = new HBox(10);
        Label label = new Label("Use SHIFT or CTRL/CMD");
        hbox.setAlignment(Pos.BASELINE_LEFT);
        label.visibleProperty().bind(Bindings.equal(WorldMapView.SelectionMode.MULTIPLE, worldMapView.selectionModeProperty()));
        hbox.getChildren().addAll(selectionModeComboBox, label);


        VBox optionsBox = new VBox(10);
        optionsBox.getChildren().add(zoomSlider);
        optionsBox.getChildren().add(hbox);

        BorderPane borderPane = new BorderPane();
        BorderPane.setMargin(clearButton, new Insets(0, 0, 10, 0));
        BorderPane.setMargin(optionsBox, new Insets(10, 0, 10, 0));

        borderPane.setTop(clearButton);
        borderPane.setCenter(listView);
        borderPane.setBottom(optionsBox);

        return borderPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
