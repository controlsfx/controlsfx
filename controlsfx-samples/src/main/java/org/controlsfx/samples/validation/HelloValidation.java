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
package org.controlsfx.samples.validation;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.samples.Utils;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationResultBuilder;
import org.controlsfx.validation.ValidationSupport;

public class HelloValidation extends ControlsFXSample {


    @Override public String getSampleName() {
        return "Panel Validation";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/validation/ValidationSupport.html";
    }

    @Override public String getSampleDescription() {
        return "Panel Validation";
    }

    @Override public Node getPanel(final Stage stage) {

    	ValidationSupport validationSupport = new ValidationSupport();
        BorderPane root = new BorderPane();

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        
        final ListView<ValidationMessage> messageList = new ListView<>();
        validationSupport.validationResultProperty().addListener( new ChangeListener<ValidationResult>(){
			public void changed(ObservableValue<? extends ValidationResult> o, ValidationResult oldValue, ValidationResult newValue) {
				messageList.getItems().clear();
				newValue.getMessages().stream().forEach(msg -> messageList.getItems().addAll(msg) );
			};
        });
        
        
        //
        // TextField with static auto-complete functionality
        //
        TextField textField = new TextField();
        validationSupport.registerValidator(textField, value -> {
        	
        	String v = value == null? "": value.toString().trim();
        	
        	return new ValidationResultBuilder(textField)
        		.addErrorIf( "Text is required", () -> v.isEmpty())
        	    .addWarningIf( "Text has incorrect length (7)", () -> v.length() != 7 )
        	    .build();
        	
        });

        TextFields.bindAutoCompletion(
                textField,
                "Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola");

        grid.add(new Label("Auto-complete Text"), 0, 0);
        grid.add(textField, 1, 0);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        
        ComboBox<String> combobox = new ComboBox<String>();
        combobox.getItems().addAll("Item A", "Item B", "Item C");
        validationSupport.registerValidator(combobox, value -> {
        	return new ValidationResultBuilder(combobox)
    			.addErrorIf( "Selection required", () -> value == null)
    			.build();
        });
        
        grid.add(new Label("Combobox"), 0, 1);
        grid.add(combobox, 1, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        
        grid.add(messageList, 0, 2, 2, 1);
       
        root.setTop(grid);
        return root;
    }


    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        // TODO Add customization example controls


        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }

}