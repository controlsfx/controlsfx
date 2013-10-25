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

import javafx.application.Application;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.control.CheckTreeView;
import org.controlsfx.samples.Utils;

import fxsampler.Sample;
import fxsampler.SampleBase;

public class HelloCheckTreeView extends SampleBase {
    
    @Override public String getSampleName() {
        return "CheckTreeView";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckTreeView.html";
    }
    
    @Override public boolean isVisible() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<String>("Root");
        root.setExpanded(true);
        root.getChildren().addAll(
                new CheckBoxTreeItem<String>("Jonathan"),
                new CheckBoxTreeItem<String>("Eugene"),
                new CheckBoxTreeItem<String>("Henri"),
                new CheckBoxTreeItem<String>("Samir"));
        
        final Label checkedItemsLabel = new Label();
        final Label selectedItemsLabel = new Label();

        // CheckListView
        final CheckTreeView<String> checkTreeView = new CheckTreeView<>(root);
        checkTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        checkTreeView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<String>>() {
            @Override public void onChanged(ListChangeListener.Change<? extends TreeItem<String>> c) {
                updateText(selectedItemsLabel, c.getList());
            }
        });
        checkTreeView.getCheckModel().getSelectedItems().addListener(new ListChangeListener<TreeItem<String>>() {
            @Override public void onChanged(ListChangeListener.Change<? extends TreeItem<String>> c) {
                updateText(checkedItemsLabel, c.getList());
            }
        });
        grid.add(checkTreeView, 0, 0, 1, 3);
        
        // labels displaying state
        grid.add(new Label("Checked items: "), 1, 0);
        grid.add(checkedItemsLabel, 2, 0);
        
        grid.add(new Label("Selected items: "), 1, 1);
        grid.add(selectedItemsLabel, 2, 1);
        
        return grid;
    }
    
    protected void updateText(Label label, ObservableList<? extends TreeItem<String>> list) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, max = list.size(); i < max; i++) {
            sb.append(list.get(i).getValue());
            if (i < max - 1) {
                sb.append(", ");
            }
        }
        label.setText(sb.toString());
    }

    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("CheckTreeView Demo");
        
        Scene scene = new Scene((Parent) getPanel(stage), 550, 550);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
