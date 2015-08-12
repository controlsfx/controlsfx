/**
 * Copyright (c) 2015, ControlsFX
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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.SegmentedButton;

public class HelloMaskerPane extends ControlsFXSample {

    private MaskerPane masker = new MaskerPane();

    public static void main(String[] args) { launch(args); }

    @Override public String getSampleName() { return "MaskerPane"; } //$NON-NLS-1$

    @Override public String getJavaDocURL() { return Utils.JAVADOC_BASE + "org/controlsfx/control/MaskerPane.html"; } //$NON-NLS-1$

    @Override public String getControlStylesheetURL() { return "/org/controlsfx/control/maskerpane.css"; } //$NON-NLS-1$

    @Override
    public Node getPanel(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        root.setTop(getSwitcher());
        root.setCenter(getBody());

        return root;
    }

    private Node getSwitcher() {
        Label label = new Label("Masker Visibility:");
        ToggleButton onButton = new ToggleButton("On");
        onButton.setSelected(false);
        ToggleButton offButton = new ToggleButton("Off");
        offButton.setSelected(false);

        SegmentedButton segmentedButton = new SegmentedButton(onButton, offButton);
        segmentedButton.getToggleGroup().selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(onButton)) {
                masker.setVisible(true);
            } else if (newValue.equals(offButton)) {
                masker.setVisible(false);
            }
        });

        Label progressLabel = new Label("Progress:");
        Slider progressSlider = new Slider(0, 1, 0);
        progressSlider.valueProperty().bindBidirectional(masker.progressProperty());

        Label progressVisible = new Label("Progress Visible:");
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().bindBidirectional(masker.progressVisibleProperty());

        Label text = new Label("Text:");
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(masker.textProperty());

        HBox hBox = new HBox(label, segmentedButton, progressLabel, progressSlider, progressVisible, checkBox, text, textField);
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        return hBox;
    }

    private Node getBody() {
        StackPane body = new StackPane();
        body.setPadding(new Insets(10, 0, 0, 0));

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(20));

        Label description = new Label("This is an example form where you may wish to block user interaction for a short period, possibly after " +
                "selecting 'Submit'. This allows for client-server communication to finish, and the user is notified that the application is not " +
                "frozen.\n\n" +
                "While the masker is visible, the form elements underneath are effectively disabled (that is, the user cannot interact with them).");
        description.setWrapText(true);
        description.setPadding(new Insets(0, 0, 20, 0));

        HBox hBox = new HBox(new Label("Username:"), new TextField());
        hBox.setSpacing(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        HBox hBox1 = new HBox(new Label("Password:"), new TextField());
        hBox1.setSpacing(10);
        hBox1.setAlignment(Pos.CENTER_LEFT);

        HBox hBox2 = new HBox(new Button("Submit"));
        hBox2.setAlignment(Pos.CENTER_LEFT);

        vBox.getChildren().addAll(description, hBox, hBox1, hBox2);

        body.getChildren().addAll(vBox, masker);

        return body;
    }
}
