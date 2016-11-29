/**
 * Copyright (c) 2014 - 2016 ControlsFX
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.MasterDetailPane;

public class HelloMasterDetailPane extends ControlsFXSample {

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/MasterDetailPane.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/masterdetailpane.css";
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
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // show details
        Label lblShowDetail = new Label("Show details: ");
        lblShowDetail.getStyleClass().add("property");
        grid.add(lblShowDetail, 0, row);
        CheckBox chkShowDetails = new CheckBox();
        grid.add(chkShowDetails, 1, row++);
        chkShowDetails.selectedProperty().bindBidirectional(masterDetailPane.showDetailNodeProperty());
        
     
        // animated
        Label lblAnimated = new Label("Animated: ");
        lblAnimated.getStyleClass().add("property");
        grid.add(lblAnimated, 0, row);
        CheckBox chkAnimated = new CheckBox();
        grid.add(chkAnimated, 1, row++);
        chkAnimated.selectedProperty().bindBidirectional(masterDetailPane.animatedProperty());


        // side
        Label lblSide = new Label("Side: ");
        lblSide.getStyleClass().add("property");
        grid.add(lblSide, 0, row);
        ComboBox<Side> positionBox = new ComboBox<>();
        positionBox.getItems().addAll(Side.values());
        grid.add(positionBox, 1, row++);
        positionBox.setValue(masterDetailPane.getDetailSide());
        masterDetailPane.detailSideProperty().bind(positionBox.valueProperty());

        // detail node
        Label lblDetailNode = new Label("Detail Node: ");
        lblDetailNode.getStyleClass().add("property");
        grid.add(lblDetailNode, 0, row);
        ComboBox<DetailType> detailNodeBox = new ComboBox<>();
        detailNodeBox.getItems().addAll(DetailType.values());
        grid.add(detailNodeBox, 1, row++);
        detailNodeBox.valueProperty().addListener(it -> {
            switch (detailNodeBox.getValue()) {
                case LABEL:
                    final Label label = new Label("Detail");
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setAlignment(Pos.CENTER);
                    label.setStyle("-fx-background-color: lightcoral;");
                    masterDetailPane.setDetailNode(label);
                    break;
                case NONE:
                    masterDetailPane.setDetailNode(null);
                    break;
                case LIST_VIEW:
                    final ListView<String> listView = new ListView<>();
                    listView.getItems().addAll("Katja", "Dirk", "Philip", "Jule", "Armin");
                    listView.setPrefSize(150,150);
                    masterDetailPane.setDetailNode(listView);
                    break;
            }
        });

        // reset position
        Button resetButton = new Button("Reset Divider");
        resetButton.setOnAction(evt -> masterDetailPane.resetDividerPosition());
        grid.add(resetButton, 1, row++);

        return grid;
    }

    public enum DetailType {
        NONE,
        LABEL,
        LIST_VIEW
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
