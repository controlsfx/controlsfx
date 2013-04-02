package org.controlsfx.dialogs;

import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javafx.stage.Modality;

import static org.controlsfx.dialogs.DialogResources.*;

class ExceptionDialog extends FXDialog {

    public ExceptionDialog(Stage parent, String moreDetails) {
        super(getMessage("exception.dialog.title"));

        initModality(Modality.APPLICATION_MODAL);
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
    private Pane getBtnPanel() {
        HBox btnPanel = new HBox();
        btnPanel.getStyleClass().add("button-panel");

        Button dismissBtn = new Button(getMessage("common.close.btn"));
        dismissBtn.setPrefWidth(80);
        dismissBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                dismissAction();
            }
        });

        dismissBtn.setDefaultButton(true);
        btnPanel.getChildren().add(dismissBtn);
        return btnPanel;
    }

    /*
     * Close this dialog and dispose of it.
     */
    private void dismissAction() {
        hide();
    }
}
