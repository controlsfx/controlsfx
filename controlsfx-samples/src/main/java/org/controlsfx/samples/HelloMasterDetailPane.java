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

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.MasterDetailPane;

public class HelloMasterDetailPane extends ControlsFXSample {

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/MasterDetailsPane.html";
    }

    private MasterDetailPane masterDetailPane;

    @Override
    public Node getPanel(Stage stage) {
        masterDetailPane = new MasterDetailPane(Side.BOTTOM);
        masterDetailPane.setShowDetailNode(true);
        return masterDetailPane;
    }

    @Override
    public Node getControlPanel() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(5, 5, 5, 5));

        Button hide = new Button("Hide");
        vbox.getChildren().add(hide);

        Button show = new Button("Show");
        vbox.getChildren().add(show);

        hide.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                masterDetailPane.setShowDetailNode(false);
            }
        });

        show.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent evt) {
                masterDetailPane.setShowDetailNode(true);
            }
        });

        ComboBox<Side> positionBox = new ComboBox<>();
        positionBox.getItems().addAll(Side.values());
        vbox.getChildren().add(positionBox);
        positionBox.setValue(masterDetailPane.getDetailPos());
        masterDetailPane.detailPosProperty().bind(positionBox.valueProperty());

        final Label expandedLabel = new Label();
        if (masterDetailPane.isShowDetailNode()) {
            expandedLabel.setText("Drawer is initially open");
        } else {
            expandedLabel.setText("Drawer is initially closed");
        }
        vbox.getChildren().add(expandedLabel);

        masterDetailPane.showDetailNodeProperty().addListener(
                new ChangeListener<Boolean>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Boolean> value,
                            Boolean oldValue, Boolean newValue) {
                        if (newValue) {
                            expandedLabel.setText("Drawer is open");
                        } else {
                            expandedLabel.setText("Drawer is closed");
                        }
                    }
                });

        CheckBox animatedBox = new CheckBox("Animated");
        animatedBox.setSelected(true);
        masterDetailPane.animatedProperty()
                .bind(animatedBox.selectedProperty());
        vbox.getChildren().add(animatedBox);

        return vbox;
    }

    @Override
    public String getSampleDescription() {
        return "A control used to display a master node and a detail node. The detail can be shown / hidden at the top, the bottom, to the left or to the right.";
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public String getSampleName() {
        return "Master Detail Pane";
    }
}
