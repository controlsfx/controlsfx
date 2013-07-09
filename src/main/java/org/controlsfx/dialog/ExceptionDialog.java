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
package org.controlsfx.dialog;

import static org.controlsfx.dialog.DialogResources.getMessage;
import static org.controlsfx.dialog.DialogResources.getString;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;

// Not public API (class is package-protected), so no JavaDoc is required.
class ExceptionDialog extends HeavyweightDialog {

    public ExceptionDialog(Window owner, String moreDetails) {
        super(getMessage("exception.dialog.title"), owner, false);

//        initModality(Modality.APPLICATION_MODAL);
        initComponents(moreDetails);
    }

    /*
     * Initialize components for this dialog.
     */
    private void initComponents(String moreDetails) {
        VBox contentPanel = new VBox();
        contentPanel.getStyleClass().add("more-info-dialog");

        contentPanel.setPrefSize(800, 600);

        if (moreDetails != null) {
            BorderPane labelPanel = new BorderPane();

            Label label = new Label(getString("exception.dialog.label"));
            labelPanel.setLeft(label);

            contentPanel.getChildren().add(labelPanel);

            TextArea text = new TextArea(moreDetails);
            text.setEditable(false);
            text.setWrapText(true);
            text.setPrefWidth(60 * 8);
            text.setPrefHeight(20 * 12);

            VBox.setVgrow(text, Priority.ALWAYS);
            contentPanel.getChildren().add(text);
        }
        contentPanel.getChildren().add(getBtnPanel());

        setContentPane(contentPanel);
    }

    /*
     * This panel contains right-aligned "Close" button.  It should
     * dismiss the dialog and dispose of it.
     */
    private Node getBtnPanel() {
        Button dismissBtn = new Button(getMessage("common.close.button"));
        dismissBtn.setPrefWidth(80);
        dismissBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                dismissAction();
            }
        });

        dismissBtn.setDefaultButton(true);
        
        ButtonBar buttonBar = new ButtonBar();
        buttonBar.addButton(dismissBtn, ButtonType.CANCEL_CLOSE);
        return buttonBar;
    }

    /*
     * Close this dialog and dispose of it.
     */
    private void dismissAction() {
        hide();
    }
}
