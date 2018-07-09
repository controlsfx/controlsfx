/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package org.controlsfx.samples.checked;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.samples.Utils;

public class HelloCheckListView extends ControlsFXSample {
    
    private final Label checkedItemsLabel = new Label();
    private final Label selectedItemsLabel = new Label();
    
    private CheckListView<String> checkListView;
    
    @Override public String getSampleName() {
        return "CheckListView";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckListView.html";
    }
    
    @Override public String getSampleDescription() {
        return "A simple UI control that makes it possible to select zero or "
                + "more items within a ListView without the need to set a custom "
                + "cell factory or manually create boolean properties for each "
                + "row - simply use the check model property to request the "
                + "current selection state.";
    }
    
    @Override public Node getPanel(Stage stage) {
        final ObservableList<String> strings = FXCollections.observableArrayList();
        for (int i = 0; i <= 100; i++) {
            strings.add("Item " + i);
        }
        
        // CheckListView
        checkListView = new CheckListView<>(strings);
        checkListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        checkListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<String>) c -> updateText(selectedItemsLabel, c.getList()));
        checkListView.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            updateText(checkedItemsLabel, change.getList());
            
            while (change.next()) {
                System.out.println("============================================");
                System.out.println("Change: " + change);
                System.out.println("Added sublist " + change.getAddedSubList());
                System.out.println("Removed sublist " + change.getRemoved());
                System.out.println("List " + change.getList());
                System.out.println("Added " + change.wasAdded() + " Permutated " + change.wasPermutated() + " Removed " + change.wasRemoved() + " Replaced "
                        + change.wasReplaced() + " Updated " + change.wasUpdated());
                System.out.println("============================================");
            }
        });
        
        StackPane stackPane = new StackPane(checkListView);
        stackPane.setPadding(new Insets(30));
        return stackPane;
    }
    
    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        Label label1 = new Label("Checked items: ");
        label1.getStyleClass().add("property");
        grid.add(label1, 0, 0);
        grid.add(checkedItemsLabel, 1, row++);
        updateText(checkedItemsLabel, null);
        
        Label label2 = new Label("Selected items: ");
        label2.getStyleClass().add("property");
        grid.add(label2, 0, 1);
        grid.add(selectedItemsLabel, 1, row++);
        updateText(selectedItemsLabel, null);
        
        
        Label checkItem2Label = new Label("Check 'Item 2': ");
        checkItem2Label.getStyleClass().add("property");
        grid.add(checkItem2Label, 0, row);
        final CheckBox checkItem2Btn = new CheckBox();
        checkItem2Btn.setOnAction(e -> {
            IndexedCheckModel<String> cm = checkListView.getCheckModel();
            if (cm != null) {
                cm.toggleCheckState(2);
            }
        });
        grid.add(checkItem2Btn, 1, row);
        
        
        return grid;
    }
    
    protected void updateText(Label label, ObservableList<? extends String> list) {
        final StringBuilder sb = new StringBuilder();
        
        if (list != null) {
            for (int i = 0, max = list.size(); i < max; i++) {
                sb.append(list.get(i));
                if (i < max - 1) {
                    sb.append(", ");
                }
            }
        }
        
        final String str = sb.toString();
        label.setText(str.isEmpty() ? "<empty>" : str);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
