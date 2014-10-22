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
package org.controlsfx.samples.textfields;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.samples.Utils;

public class HelloTextFields extends ControlsFXSample {
    
    private static final Image image = new Image("/org/controlsfx/samples/security-low.png");
    
    @Override public String getSampleName() {
        return "TextFields";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/textfield/TextFields.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        // TextField and PasswordField labels
        Label textFieldLabel = new Label("TextField");
        textFieldLabel.setFont(Font.font(24));
        GridPane.setHalignment(textFieldLabel, HPos.CENTER);
        Label passwordFieldLabel = new Label("PasswordField");
        passwordFieldLabel.setFont(Font.font(24));
        GridPane.setHalignment(passwordFieldLabel, HPos.CENTER);
        grid.add(textFieldLabel, 1, row);
        grid.add(passwordFieldLabel, 2, row);
        row++;

        // normal TextField / PasswordField
        grid.add(new Label("Normal TextField / PasswordField: "), 0, row);
        grid.add(new TextField(), 1, row);
        grid.add(new PasswordField(), 2, row++);
        
        // Clearable*Field
        grid.add(new Label("Clearable*Field: "), 0, row);
        TextField clearableTextField = TextFields.createClearableTextField();
        PasswordField clearablePasswordField = TextFields.createClearablePasswordField();
		ToggleButton btToggle = new ToggleButton("Enable/Disable");
		ToggleButton btEditable = new ToggleButton("Toggle Editable");
		clearableTextField.disableProperty().bind(btToggle.selectedProperty());
		clearablePasswordField.disableProperty().bind(btToggle.selectedProperty());
		clearableTextField.editableProperty().bindBidirectional(btEditable.selectedProperty());
		clearablePasswordField.editableProperty().bindBidirectional(btEditable.selectedProperty());
		btEditable.setSelected(true);
		grid.add(clearableTextField, 1, row);
		grid.add(clearablePasswordField, 2, row);
		grid.add(btEditable, 3, row);
		grid.add(btToggle, 4, row++);
        
        // Custom*Field
        grid.add(new Label("Custom*Field (no additional nodes): "), 0, row);
        grid.add(new CustomTextField(), 1, row);
        grid.add(new CustomPasswordField(), 2, row++);
        
        // Custom*Field (w/ right node)
        grid.add(new Label("Custom*Field (w/ right node): "), 0, row);
        CustomTextField customTextField1 = new CustomTextField();
        customTextField1.setRight(new ImageView(image));
        grid.add(customTextField1, 1, row);
        
        CustomPasswordField customPasswordField1 = new CustomPasswordField();
        customPasswordField1.setRight(new ImageView(image));
        grid.add(customPasswordField1, 2, row++);
        
        // Custom*Field (w/ left node)
        grid.add(new Label("Custom*Field (w/ left node): "), 0, row);
        CustomTextField customTextField2 = new CustomTextField();
        customTextField2.setLeft(new ImageView(image));
        grid.add(customTextField2, 1, row);
        
        CustomPasswordField customPasswordField2 = new CustomPasswordField();
        customPasswordField2.setLeft(new ImageView(image));
        grid.add(customPasswordField2, 2, row++);
        
        // Custom*Field (w/ left + right node)
        grid.add(new Label("Custom*Field (w/ left + right node): "), 0, row);
        CustomTextField customTextField3 = new CustomTextField();
        customTextField3.setLeft(new ImageView(image));
        customTextField3.setRight(new ImageView(image));
        grid.add(customTextField3, 1, row);
        
        CustomPasswordField customPasswordField3 = new CustomPasswordField();
        customPasswordField3.setLeft(new ImageView(image));
        customPasswordField3.setRight(new ImageView(image));
        grid.add(customPasswordField3, 2, row++);
        
        return grid;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
