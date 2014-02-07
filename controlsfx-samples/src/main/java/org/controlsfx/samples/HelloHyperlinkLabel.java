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
package org.controlsfx.samples;

import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
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
        label.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                Hyperlink link = (Hyperlink)event.getSource();
                final String str = link == null ? "" : "You clicked on '" + link.getText() + "'";
                selectedLinkField.setText(str);
            }
        });
        root.getChildren().add(label);
        
        return root;
    }
}
