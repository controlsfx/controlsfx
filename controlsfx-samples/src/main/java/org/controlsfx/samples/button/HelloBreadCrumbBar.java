/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.samples.button;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.BreadCrumbBar;
import org.controlsfx.control.BreadCrumbBar.BreadCrumbActionEvent;
import org.controlsfx.samples.Utils;

public class HelloBreadCrumbBar extends ControlsFXSample {

    private BreadCrumbBar<String> sampleBreadCrumbBar;
    private final Label selectedCrumbLbl = new Label();
    
    private int newCrumbCount = 0;

    @Override public String getSampleName() {
        return "BreadCrumbBar";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/BreadCrumbBar.html";
    }

    @Override public String getSampleDescription() {
        return "The BreadCrumbBar provides an easy way to navigate hirarchical structures " +
                "such as file systems.";
    }

    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        sampleBreadCrumbBar = new BreadCrumbBar<>();
        resetModel();

        root.getChildren().add(sampleBreadCrumbBar);
        BorderPane.setMargin(sampleBreadCrumbBar, new Insets(20));

        sampleBreadCrumbBar.setOnCrumbAction(new EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>>() {
            @Override public void handle(BreadCrumbActionEvent<String> bae) {
                selectedCrumbLbl.setText("You just clicked on '" + bae.getSelectedCrumb() + "'!");
            }
        });
        
        root.getChildren().add(selectedCrumbLbl);

        return root;
    }

    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // add crumb
        Label lblAddCrumb = new Label("Add crumb: ");
        lblAddCrumb.getStyleClass().add("property");
        grid.add(lblAddCrumb, 0, row);
        Button btnAddCrumb = new Button("Press");
        btnAddCrumb.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                // Construct a new leaf node and append it to the previous leaf
                TreeItem<String> leaf = new TreeItem<>("New Crumb #" + newCrumbCount++);
                sampleBreadCrumbBar.getSelectedCrumb().getChildren().add(leaf);
                sampleBreadCrumbBar.setSelectedCrumb(leaf);
            }
        });
        grid.add(btnAddCrumb, 1, row++);
        
        // reset
        Label lblReset = new Label("Reset model: ");
        lblReset.getStyleClass().add("property");
        grid.add(lblReset, 0, row);
        Button btnReset = new Button("Press");
        btnReset.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent ae) {
                resetModel();
            }
        });
        grid.add(btnReset, 1, row++);
        
        // auto navigation
        Label lblAutoNavigation = new Label("Enable auto navigation: ");
        lblAutoNavigation.getStyleClass().add("property");
        grid.add(lblAutoNavigation, 0, row);
        CheckBox chkAutoNav = new CheckBox();
        chkAutoNav.selectedProperty().bindBidirectional(sampleBreadCrumbBar.autoNavigationEnabledProperty());
        grid.add(chkAutoNav, 1, row++);

        return grid;
    }
    
    private void resetModel() {
        TreeItem<String> model = BreadCrumbBar.buildTreeModel("Hello", "World", "This", "is", "cool");
        sampleBreadCrumbBar.setSelectedCrumb(model);
    }


    public static void main(String[] args) {
        launch(args);
    }

}