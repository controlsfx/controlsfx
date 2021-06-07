/**
 * Copyright (c) 2015, 2021, ControlsFX
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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.ToggleSwitch;

public class HelloToggleSwitch extends ControlsFXSample
{
    static final String RESOURCE = "ToggleSwitch.fxml";

    @Override
    public String getSampleName()
    {
        return "ToggleSwitch";
    }

    @Override
    public Node getPanel(Stage stage)
    {
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.setPrefHeight(316);
        anchorPane.setPrefWidth(444);

        Label headerLabel = new Label("Toggle Switch");
        headerLabel.getStyleClass().add("header");
        headerLabel.setLayoutX(44);
        headerLabel.setLayoutY(34);

        Label itemTitle1 = new Label("Normal unselected");
        itemTitle1.getStyleClass().add("item-title");
        itemTitle1.setLayoutX(70);
        itemTitle1.setLayoutY(145);

        ToggleSwitch toggleSwitch1 = new ToggleSwitch("Off");
        toggleSwitch1.setLayoutX(70);
        toggleSwitch1.setLayoutY(168);

        Label itemTitle2 = new Label("Disabled");
        itemTitle2.getStyleClass().add("item-title");
        itemTitle2.setLayoutX(271);
        itemTitle2.setLayoutY(145);

        ToggleSwitch toggleSwitch2 = new ToggleSwitch("Off");
        toggleSwitch2.setLayoutX(271);
        toggleSwitch2.setLayoutY(168);
        toggleSwitch2.setDisable(true);

        Label itemTitle3 = new Label("Normal selected");
        itemTitle3.getStyleClass().add("item-title");
        itemTitle3.setLayoutX(70);
        itemTitle3.setLayoutY(227);

        ToggleSwitch toggleSwitch3 = new ToggleSwitch("On");
        toggleSwitch3.setLayoutX(70);
        toggleSwitch3.setLayoutY(250);
        toggleSwitch3.setSelected(true);
        
        Label itemTitle4 = new Label("Styled");
        itemTitle4.getStyleClass().add("item-title");
        itemTitle4.setLayoutX(70);
        itemTitle4.setLayoutY(309);
        
        ToggleSwitch toggleSwitch4 = new ToggleSwitch("Styled");
        toggleSwitch4.getStyleClass().add("styled-toggle");
        toggleSwitch4.setLayoutX(70);
        toggleSwitch4.setLayoutY(332);

        anchorPane.getChildren().addAll(headerLabel, itemTitle1, toggleSwitch1, itemTitle2, toggleSwitch2, itemTitle3, toggleSwitch3);
        anchorPane.getChildren().addAll(itemTitle4, toggleSwitch4);

        anchorPane.getStylesheets().add(getClass().getResource("toggleSwitchSample.css").toExternalForm());
        return anchorPane;
    }
    
    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/samples/toggleSwitchSample.css";
    }

    @Override
    public String getJavaDocURL()
    {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/ToggleSwitch.html";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
