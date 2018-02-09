/**
 * Copyright (c) 2018 ControlsFX
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;

import java.util.List;

public class HelloListActionView extends ControlsFXSample {

    private ListActionView<String> view;

    @Override
    public String getSampleName() {
        return "List Action View";
    }

    @Override
    public Node getPanel(Stage stage) {
        view = new ListActionView<>();
        view.getItems().addAll("Dirk", "Jonathan", "Eugene","Abhinay", "Samir");
        view.getActions().addAll(createActions());

        GridPane pane = new GridPane();
        pane.add(view, 0, 0);
        pane.setAlignment(Pos.CENTER);

        return pane;
    }

    @Override
    public Node getControlPanel() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        
        CheckBox useCellFactory = new CheckBox("Use cell factory");
        useCellFactory.setOnAction(evt -> {
            if (useCellFactory.isSelected()) {
                view.setCellFactory(listView -> {
                    ListCell<String> cell = new ListCell<String>() {
                        @Override
                        public void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);

                            if (empty) {
                                setText(null);
                                setGraphic(null);
                            } else {
                                setText(item == null ? "null" : item);
                                setGraphic(null);
                            }
                        }
                    };
                    cell.setFont(Font.font("Arial", FontWeight.BOLD,
                            FontPosture.ITALIC, 18));
                    return cell;
                });
            } else {
                view.setCellFactory(null);
            }
        });

        CheckBox clearActions = new CheckBox("Clear Actions");
        clearActions.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                view.getActions().clear();
            } else {
                view.getActions().addAll(createActions());
            }
        });

        ComboBox<Side> sideComboBox = new ComboBox<>(FXCollections.observableArrayList(Side.values()));
        sideComboBox.setTooltip(new Tooltip("The side of the ListView on which the actions should be displayed"));

        sideComboBox.getSelectionModel().select(Side.LEFT);
        view.sideProperty().bind(sideComboBox.getSelectionModel().selectedItemProperty());
        
        root.getChildren().addAll(useCellFactory, clearActions, sideComboBox);
        
        return root;
    }

    @Override
    public String getSampleDescription() {
        return "A control used to let the user select multiple values from a "
                + "list of available values. Selected values are moved into a "
                + "second list that is showing the current selection. Items can "
                + "be moved by double clicking on them or by first selecting "
                + "them and then pressing one of the buttons in the center.";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/ListActionView.html";
    }

    public static void main(String[] args) {
        launch(args);
    }

    private ObservableList<Action> createActions() {
        ListActionView.ListAction<String> moveUp = new ListActionView.ListAction<String>(
                new FontAwesome().create(FontAwesome.Glyph.ANGLE_UP)) {
            @Override
            public void initialize(ListView<String> listView) {
                setEventHandler(e -> moveSelectedItemsUp(listView));
            }
        };
        ListActionView.ListAction<String> moveDown = new ListActionView.ListAction<String>(new FontAwesome().create(FontAwesome.Glyph.ANGLE_DOWN)) {
            @Override
            public void initialize(ListView<String> listView) {
                setEventHandler(e -> moveSelectedItemsDown(listView));
            }
        };
        return FXCollections.observableArrayList(moveUp, ActionUtils.ACTION_SEPARATOR, moveDown);
    }

    private void moveSelectedItemsUp(ListView<String> listView) {
        final MultipleSelectionModel<String> selectionModel = listView.getSelectionModel();
        final ObservableList<Integer> selectedIndices = selectionModel.getSelectedIndices();
        final ObservableList<String> items = listView.getItems();
        for (final Integer selectedIndex : selectedIndices) {
            if (selectedIndex > 0) {
                if (selectedIndices.contains(selectedIndex - 1)) continue;
                final String item = items.get(selectedIndex);
                final String itemToBeReplaced = items.get(selectedIndex - 1);
                items.set(selectedIndex - 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex - 1);
            }
        }
    }

    private void moveSelectedItemsDown(ListView<String> listView) {
        final ObservableList<String> items = listView.getItems();
        final MultipleSelectionModel<String> selectionModel = listView.getSelectionModel();
        final List<Integer> selectedIndices = selectionModel.getSelectedIndices();
        int lastIndex = items.size() - 1;
        // Last selected item is to be replaced first
        for (int index = selectedIndices.size() - 1; index >= 0; index--) {
            final Integer selectedIndex = selectedIndices.get(index);
            if (selectedIndex < lastIndex) {
                if (selectedIndices.contains(selectedIndex + 1)) continue;
                final String item = items.get(selectedIndex);
                final String itemToBeReplaced = items.get(selectedIndex + 1);
                items.set(selectedIndex + 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex + 1);
            }
        }
    }
}
