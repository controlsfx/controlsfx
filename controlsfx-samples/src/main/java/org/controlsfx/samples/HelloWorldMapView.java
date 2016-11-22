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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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

        BorderPane borderPane = new BorderPane();
        BorderPane.setMargin(clearButton, new Insets(0, 0, 10, 0));

        borderPane.setTop(clearButton);
        borderPane.setCenter(listView);

        return borderPane;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
