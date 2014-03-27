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

import java.time.LocalDate;
import java.util.Arrays;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.CompoundValidationDecorator;
import org.controlsfx.validation.IconValidationDecorator;
import org.controlsfx.validation.StyleClassValidationDecorator;
import org.controlsfx.validation.ValidationDecorator;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class HelloValidation extends ControlsFXSample {


    @Override public String getSampleName() {
        return "Component Validation";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/validation/ValidationSupport.html";
    }

    @Override public String getSampleDescription() {
        return "Component Validation";
    }
    
    ValidationSupport validationSupport = new ValidationSupport();

    @Override public Node getPanel(final Stage stage) {
    	

        GridPane root = new GridPane();
        root.setVgap(10);
        root.setHgap(10);
        root.setPadding(new Insets(30, 30, 0, 30));
        
        root.sceneProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable o) {
                if (root.getScene() != null) {
                    root.getScene().getStylesheets().add(HelloDecorator.class.getResource("validation.css").toExternalForm());
                }
            }
        });

        
//        final ListView<ValidationMessage> messageList = new ListView<>();
//        validationSupport.validationResultProperty().addListener( (o, oldValue, validationResult) -> {
//        	messageList.getItems().setAll(validationResult.getMessages());
//        }
//        );
        
        
        int row = 0;

        // text field
        TextField textField = new TextField();
        validationSupport.registerValidator(textField, Validator.createEmptyValidator("Text is required"));
        
        TextFields.bindAutoCompletion(
                textField,
                "Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola");

        root.add(new Label("Auto-complete Text"), 0, row);
        root.add(textField, 1, row);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        
        //combobox
        row++;
        ComboBox<String> combobox = new ComboBox<String>();
        combobox.getItems().addAll("Item A", "Item B", "Item C");
        validationSupport.registerValidator(combobox,
        		Validator.createEmptyValidator( "ComboBox Selection required"));
        
        root.add(new Label("Combobox"), 0, row);
        root.add(combobox, 1, row);
        GridPane.setHgrow(combobox, Priority.ALWAYS);

        //choicebox
        row++;
        ChoiceBox<String> choiceBox = new ChoiceBox<String>();
        choiceBox.getItems().addAll("Item A", "Item B", "Item C");
        validationSupport.registerValidator(choiceBox, 
        	Validator.createEmptyValidator("ChoiceBox Selection required"));
        
        root.add(new Label("ChoiceBox"), 0, row);
        root.add(choiceBox, 1, row);
        GridPane.setHgrow(combobox, Priority.ALWAYS);
        
        //checkbox
        row++;
        CheckBox checkBox =  new CheckBox("CheckBox");
        validationSupport.registerValidator(checkBox, (Control c, Boolean newValue) -> 
        	ValidationResult.fromErrorIf( c, "Checkbox should be checked", !newValue)
        );
        root.add(checkBox, 1, row);
        GridPane.setHgrow(checkBox, Priority.ALWAYS);
        
        //slider
        row++;
        Slider slider =  new Slider(-50d, 50d, -10d);
        slider.setShowTickLabels(true);
        validationSupport.registerValidator(slider, (Control c, Double newValue) -> 
        	ValidationResult.fromErrorIf( slider, "Slider value should be > 0",  newValue <= 0 )
        );
       
        root.add(new Label("Slider"), 0, row);
        root.add(slider, 1, row);
        GridPane.setHgrow(checkBox, Priority.ALWAYS);

        // color picker
        row++;
        ColorPicker colorPicker =  new ColorPicker(Color.RED);
        validationSupport.registerValidator(colorPicker, 
        	Validator.createEqualsValidator("Color should be WHITE or BLACK", Arrays.asList(Color.WHITE, Color.BLACK))	
        );
       
        root.add(new Label("Color Picker"), 0, row);
        root.add(colorPicker, 1, row);
        GridPane.setHgrow(checkBox, Priority.ALWAYS);

        // date picker
        row++;
        DatePicker datePicker =  new DatePicker();
        validationSupport.registerValidator(datePicker, (Control c, LocalDate newValue) -> 
        	ValidationResult.fromWarningIf( datePicker, "The date should be today", !LocalDate.now().equals(newValue) )	
        );
       
        root.add(new Label("Date Picker"), 0, row);
        root.add(datePicker, 1, row);
        GridPane.setHgrow(checkBox, Priority.ALWAYS);
        
//        // validation results
//        row++;
//        TitledPane pane = new TitledPane("Validation Results", messageList);
//        pane.setCollapsible(false);
//        root.add(pane, 0, row, 2, 1);
//        GridPane.setHgrow(pane, Priority.ALWAYS);
       
        //root.setTop(grid);
        ScrollPane scrollPane = new ScrollPane(root);
        return scrollPane;
    }


    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        
        
        IconValidationDecorator iconDecorator = new IconValidationDecorator();
        StyleClassValidationDecorator cssDecorator = new StyleClassValidationDecorator();

        int row = 0;
        ComboBox<ValidationDecorator> checkbox =  new ComboBox<>();
        checkbox.getItems().addAll( iconDecorator, cssDecorator, new CompoundValidationDecorator(cssDecorator,iconDecorator));
        checkbox.getSelectionModel().selectedItemProperty().addListener( (o,old,decorator) ->
           validationSupport.setValidationDecorator(decorator)
        );
        checkbox.getSelectionModel().select(0);
        
        
        grid.add(new Label("Validation Decorator"), 0, row);
        grid.add(checkbox, 1, row);
        GridPane.setHgrow(checkbox, Priority.ALWAYS);
        

        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }

}