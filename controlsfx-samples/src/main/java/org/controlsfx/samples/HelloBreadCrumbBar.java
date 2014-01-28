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
package org.controlsfx.samples;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.breadcrumbs.BreadCrumbBar;
import org.controlsfx.control.breadcrumbs.BreadCrumbBar.BreadCrumbActionEvent;
import org.controlsfx.dialog.Dialogs;

public class HelloBreadCrumbBar extends ControlsFXSample {

    BreadCrumbBar<String> sampleBreadCrumbBar;

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

        BorderPane root = new BorderPane();

        sampleBreadCrumbBar = new BreadCrumbBar<>();
        sampleBreadCrumbBar.appendCrumbs("Hello", "World", "This", "Is", "cool");

        root.setTop(sampleBreadCrumbBar);
        BorderPane.setMargin(sampleBreadCrumbBar, new Insets(20));


        sampleBreadCrumbBar.setOnCrumbAction(new EventHandler<BreadCrumbBar.BreadCrumbActionEvent<String>>() {
            @Override
            public void handle(BreadCrumbActionEvent<String> bae) {
                Dialogs.create()
                .title("BreadCrumbBar")
                .masthead("Bread Crumb Action")
                .message("You just clicked on '" + bae.getCrumbModel() + "'!") 
                .showInformation();
            }
        });

        return root;
    }

    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        // TODO Add customization example controls
        Button btn = new Button("Add Crumb");
        grid.add(btn, 0, 0);


        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ae) {
                sampleBreadCrumbBar.appendCrumbs("New!");
            }
        });

        return grid;
    }


    public static void main(String[] args) {
        launch(args);
    }

}