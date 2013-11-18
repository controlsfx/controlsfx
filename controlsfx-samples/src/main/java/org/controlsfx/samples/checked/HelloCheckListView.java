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
package org.controlsfx.samples.checked;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.CheckListView;
import org.controlsfx.samples.Utils;

public class HelloCheckListView extends ControlsFXSample {
    
    @Override public String getSampleName() {
        return "CheckListView";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckListView.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        final ObservableList<String> strings = FXCollections.observableArrayList();
        for (int i = 0; i <= 100; i++) {
            strings.add("Item " + i);
        }
        
        final Label checkedItemsLabel = new Label();
        final Label selectedItemsLabel = new Label();

        // CheckListView
        final CheckListView<String> checkListView = new CheckListView<>(strings);
        checkListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        checkListView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override public void onChanged(ListChangeListener.Change<? extends String> c) {
                updateText(selectedItemsLabel, c.getList());
            }
        });
        checkListView.getCheckModel().getSelectedItems().addListener(new ListChangeListener<String>() {
            @Override public void onChanged(ListChangeListener.Change<? extends String> c) {
                updateText(checkedItemsLabel, c.getList());
            }
        });
        grid.add(checkListView, 0, 0, 1, 3);
        
        // labels displaying state
        grid.add(new Label("Checked items: "), 1, 0);
        grid.add(checkedItemsLabel, 2, 0);
        
        grid.add(new Label("Selected items: "), 1, 1);
        grid.add(selectedItemsLabel, 2, 1);
        
        return grid;
    }
    
    protected void updateText(Label label, ObservableList<? extends String> list) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, max = list.size(); i < max; i++) {
            sb.append(list.get(i));
            if (i < max - 1) {
                sb.append(", ");
            }
        }
        label.setText(sb.toString());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
