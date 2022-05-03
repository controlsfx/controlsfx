/**
 * Copyright (c) 2013, 2022, ControlsFX
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

import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.HyperlinkLabel;

public class HelloHyperlinkLabel extends ControlsFXSample {
    
    private HyperlinkLabel label;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Hyperlink Label";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/HyperlinkLabel.html";
    }

    @Override public String getSampleDescription() {
        return "HyperlinkLabel provides an easy way to create labels and hyperlinks.";
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        
        final TextField textToShowField = new TextField();
        textToShowField.setMaxWidth(Double.MAX_VALUE);
        textToShowField.setPromptText("Type text in here to display - use [] to indicate a hyperlink - e.g. [hello]");
        root.getChildren().add(textToShowField);
        
        final TextField selectedLinkField = new TextField();
        selectedLinkField.setMaxWidth(Double.MAX_VALUE);
        selectedLinkField.setEditable(false);
        selectedLinkField.setPromptText("Click a link - I'll show you which one you clicked :-)");
        root.getChildren().add(selectedLinkField);
        
        label = new HyperlinkLabel();
        // label.setFocusTraversable(false);
        label.textProperty().bind(new StringBinding() {
            {
                bind(textToShowField.textProperty());
            }
            
            @Override protected String computeValue() {
                final String str = textToShowField.getText();
                if (str == null || str.isEmpty()) {
                    return "Hello [world]! I [wonder] what hyperlink [you] [will] [click]";
                }
                return str;
            }
        });
        label.setOnAction(event -> {
            Hyperlink link = (Hyperlink)event.getSource();
            final String str = link == null ? "" : "You clicked on '" + link.getText() + "'";
            selectedLinkField.setText(str);
        });
        root.getChildren().add(label);
        root.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        
        return root;
    }

    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // focus traversable
        Label lblFocusTraversable = new Label("Focus Traversable: ");
        lblFocusTraversable.getStyleClass().add("property");
        grid.add(lblFocusTraversable, 0, row);
        CheckBox focusTraversable = new CheckBox();
        focusTraversable.setSelected(true);
        label.focusTraversableProperty().bind(focusTraversable.selectedProperty());
        grid.add(focusTraversable, 1, row);

        return grid;
    }

}
