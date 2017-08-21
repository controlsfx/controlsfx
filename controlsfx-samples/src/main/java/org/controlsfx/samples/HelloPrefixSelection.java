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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.PrefixSelectionChoiceBox;
import org.controlsfx.control.PrefixSelectionComboBox;

public class HelloPrefixSelection extends ControlsFXSample {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Prefix Selection ComboBox/ChoiceBox";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/tools/PrefixSelectionCustomizer.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);
        grid.setPadding(new Insets(24));
        
        ObservableList<String> stringList = FXCollections.observableArrayList("1111", "2222", "Aaaaa", "Abbbb", "Abccc", "Abcdd", "Abcde", "Bbbb", "bbbb", "Cccc", "Dddd", "Eeee", "Ffff", "gggg", "hhhh", "3333");

        grid.add(new Label("ChoiceBox<String>"), 0, 0);
        PrefixSelectionChoiceBox<String> choice1 = new PrefixSelectionChoiceBox<>();
        choice1.setItems(stringList);
        choice1.setMaxWidth(Double.MAX_VALUE);
        grid.add(choice1, 1, 0);
        
        grid.add(new Label("ComboBox<String>"), 0, 1);
        PrefixSelectionComboBox<String> combo1 = new PrefixSelectionComboBox<>();
        combo1.setItems(stringList);
        combo1.setMaxWidth(Double.MAX_VALUE);
        grid.add(combo1, 1, 1);
        CheckBox cb1 = new CheckBox("Make ComboBox editable");
        cb1.selectedProperty().bindBidirectional(combo1.editableProperty());
        grid.add(cb1, 2, 1);
        
        ObservableList<Person> personList = FXCollections.observableArrayList(
                new Person("Jack Nicholson"),
                new Person("Marlon Brando"), 
                new Person("Robert De Niro"), 
                new Person("Al Pacino"),
                new Person("Daniel Day-Lewis"),
                new Person("Dustin Hoffman"),
                new Person("Tom Hanks"),
                new Person("Anthony Hopkins"),
                new Person("Paul Newman"),
                new Person("Denzel Washington"),
                new Person("Spencer Tracy"),
                new Person("Laurence Olivier"),
                new Person("Jack Lemmon"),
                new Person("Jeff Bridges"),
                new Person("James Stewart"),
                new Person("Sean Penn"),
                new Person("Michael Caine"),
                new Person("Morgan Freeman"),
                new Person("Robert Duvall"),
                new Person("Gene Hackman"),
                new Person("Clint Eastwood"),
                new Person("Gregory Peck"),
                new Person("Robin Williams"),
                new Person("Ben Kingsley"),
                new Person("Philip Seymour Hoffman")
        );
        
        grid.add(new Label("ChoiceBox<Person>"), 0, 3);
        PrefixSelectionChoiceBox<Person> choice2 = new PrefixSelectionChoiceBox<>();
        choice2.setItems(personList);
        choice2.setMaxWidth(Double.MAX_VALUE);
        grid.add(choice2, 1, 3);
        
        grid.add(new Label("ComboBox<Person>"), 0, 4);
        PrefixSelectionComboBox<Person> combo2 = new PrefixSelectionComboBox<>();
        combo2.setItems(personList);
        combo2.setMaxWidth(Double.MAX_VALUE);
        grid.add(combo2, 1, 4);
        CheckBox cb2 = new CheckBox("Make ComboBox editable");
        cb2.selectedProperty().bindBidirectional(combo2.editableProperty());
        grid.add(cb2, 2, 4);
        
        grid.add(new Button("Press Tab"), 0, 5);
        PrefixSelectionComboBox<String> combo3 = new PrefixSelectionComboBox<>();
        combo3.setDisplayOnFocusedEnabled(true);
        combo3.setNumberOfDigits(2);
        combo3.setTypingDelay(1000);
        combo3.setBackSpaceAllowed(true);
        combo3.setItems(FXCollections.observableArrayList(
            "17 ACURA", "19 AUDI", "64 BENTLEY", "98 BLUE BIRD", "21 BMW", "02 BUICK", "03 CADILLAC", 
                "05 CHEVROLET", "90 CHEVY-MEDIUM DUTY", "06 CHRYSLER", "23 DAEWOO", "07 DODGE", 
                "72 FERRARI", "08 FORD", "92 FORD-MEDIUM DUTY", "09 GCM TRUCK", "93 GCM-MEDIUM DUTY", 
                "94 HINO", "27 HONDA"));
        
        combo3.setMaxWidth(Double.MAX_VALUE);
        grid.add(combo3, 1, 5);
        grid.add(new TextField(), 2, 5);
        
        return grid;
    }
    
    @Override public String getSampleDescription() {
        return "This utility class can be used to customize a ChoiceBox or ComboBox"
                + " and enable the prefix selection feature. This will enable the user to type letters or"
                + " digits on the keyboard and the ChoiceBox or ComboBox will attempt to"
                + " select the first item it can find with a matching prefix.";
    }
    
    private static class Person {
        public String name;
        public Person(String string) {
            name = string;
        }
        @Override
        public String toString() {
            return name;
        }
    }
}
