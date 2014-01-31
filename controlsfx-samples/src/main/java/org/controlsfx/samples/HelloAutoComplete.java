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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.autocompletion.AutoCompletePopup;
import org.controlsfx.control.autocompletion.AutoCompletionTextFieldBinding;
import org.controlsfx.control.autocompletion.SuggestionProviderString;

public class HelloAutoComplete extends ControlsFXSample {

    private AutoCompletePopup<String> suggestionsPopup;

    private Popup debugPopup;

    @Override public String getSampleName() {
        return "AutoComplete";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/AutoComplete.html";
    }

    @Override public String getSampleDescription() {
        return "AutoComplete helps a user with suggestions to type faster, " +
                "but does not limit him to enter something else.\n";
    }

    @Override public Node getPanel(final Stage stage) {

        BorderPane root = new BorderPane();
        VBox topBox = new VBox();

        // Simple suggestion popup
        final Button btnShowSuggestions = new Button(">Show sugestions<");
        btnShowSuggestions.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent arg0) {

                if(suggestionsPopup != null) suggestionsPopup.hide();
                suggestionsPopup =  new AutoCompletePopup<String>();
                suggestionsPopup.getSuggestions().addAll("Fruit","Fruits","Frites", "Cheese!");
                suggestionsPopup.showBelowNode(btnShowSuggestions);
            }
        });
        topBox.getChildren().add(btnShowSuggestions);
        VBox.setMargin(btnShowSuggestions, new Insets(20));

        // TextField with auto-complete functionality
        TextField textField = new TextField();

        SuggestionProviderString suggestionProvider = new SuggestionProviderString();
        suggestionProvider.addPossibleSuggetions("Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola");
        AutoCompletionTextFieldBinding.createBinding(textField, suggestionProvider);

        topBox.getChildren().add(textField);
        VBox.setMargin(textField, new Insets(20));

        root.setTop(topBox);
        return root;
    }

    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        // TODO Add customization example controls
        //Button btn = new Button("Add Crumb");
        //grid.add(btn, 0, 0);

        //CheckBox chkAutoNav = new CheckBox("Enable auto navigation");
        //grid.add(chkAutoNav, 0, 1);


        return grid;
    }


    public static void main(String[] args) {
        launch(args);
    }

}