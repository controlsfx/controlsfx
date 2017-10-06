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

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.PrefixSelectionChoiceBox;
import org.controlsfx.control.PrefixSelectionComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HelloPrefixSelection extends ControlsFXSample {
    
    private final int hidingDelay = 200;
    
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
        PrefixSelectionComboBox<String> customLookupCombo = new PrefixSelectionComboBox<>();
        customLookupCombo.setDisplayOnFocusedEnabled(true);
        customLookupCombo.setTypingDelay(1000);
        customLookupCombo.setBackSpaceAllowed(true);
        customLookupCombo.setItems(FXCollections.observableArrayList(
            "00 Abb", "01 Acc", "02 Add", "10 Baa", "11 Bcc", "12 Bdd", "13 Bee", 
                "20 Caa", "21 Cbb", "22 Cdd", "23 Cee", "24 Cff", "30 Daa"));
        customLookupCombo.setLookup((combo, u) -> combo.getItems().stream()
                .filter(item -> {
                    String s = combo.getConverter().toString(item);
                    if (s != null && ! s.isEmpty() && ! u.isEmpty()) {
                        s = s.toUpperCase(Locale.ROOT);
                        String firstLetter = u.substring(0, 1).toUpperCase(Locale.ROOT);
                        int numberOfDigits = 2;
                        final int numberOfOccurrences = u.toUpperCase(Locale.ROOT).replaceFirst(".*?(" + firstLetter + "+).*", "$1").length();
                        if (isValidNumber(u, numberOfDigits) && s.startsWith(u)) {
                            // two digits: select that line and tab to the next field
                            commitSelection(combo);
                            return true;
                        } else if (s.substring(numberOfDigits + 1).startsWith(u.toUpperCase(Locale.ROOT))) {
                            // alpha characters: highlight closest (first) match
                            return true;
                        } else if (numberOfOccurrences > 1 && 
                            s.substring(numberOfDigits + 1, numberOfDigits + 2).equals(firstLetter)) {
                            final int numberOfItems = getItemsByLetter(combo, firstLetter, numberOfDigits).size();
                            final int index = getItemsByLetter(combo, firstLetter, numberOfDigits).indexOf(item);
                            // repeated alpha characters: highlight match based on order
                            if (index == (numberOfOccurrences - 1) % numberOfItems) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .findFirst());
        customLookupCombo.setMaxWidth(Double.MAX_VALUE);
        grid.add(customLookupCombo, 1, 5);
        grid.add(new TextField(), 2, 5);
        
        grid.add(new Button("Press Tab"), 0, 6);
        PrefixSelectionComboBox<Person> combo4 = new PrefixSelectionComboBox<>();
        combo4.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person object) {
                return String.format("%02d ", personList.indexOf(object)) + (object != null ? object.toString() : "");
            }

            @Override
            public Person fromString(String string) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        combo4.setItems(personList);
        combo4.setBackSpaceAllowed(true);
        combo4.setDisplayOnFocusedEnabled(true);
        combo4.setTypingDelay(1000);
        combo4.setLookup((combo, u) -> combo.getItems().stream()
                .filter(item -> {
                    String s = combo.getConverter().toString(item);
                    if (s != null && ! s.isEmpty() && ! u.isEmpty()) {
                        s = s.toUpperCase(Locale.ROOT);
                        String firstLetter = u.substring(0, 1).toUpperCase(Locale.ROOT);
                        final int numberOfOccurrences = u.toUpperCase(Locale.ROOT).replaceFirst(".*?(" + firstLetter + "+).*", "$1").length();
                        int numberOfDigits = 2;
                        if (isValidNumber(u, numberOfDigits) && s.startsWith(u)) {
                            // two digits: select that line and tab to the next field
                            commitSelection(combo);
                            return true;
                        } else if (s.substring(numberOfDigits + 1).startsWith(u.toUpperCase(Locale.ROOT))) {
                            // alpha characters: highlight closest (first) match
                            return true;
                        } else if (numberOfOccurrences > 1 && 
                            s.substring(numberOfDigits + 1, numberOfDigits + 2).equals(firstLetter)) {
                            final int numberOfItems = getItemsByLetter(combo, firstLetter, numberOfDigits).size();
                            final int index = getItemsByLetter(combo, firstLetter, numberOfDigits).indexOf(item);
                            // repeated alpha characters: highlight match based on order
                            if (index == (numberOfOccurrences - 1) % numberOfItems) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .findFirst());
        combo4.setMaxWidth(Double.MAX_VALUE);
        grid.add(combo4, 1, 6);
        grid.add(new TextField(), 2, 6);
        
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
    
    private boolean isValidNumber(String prefix, int numberOfDigits) {
        if (prefix == null || prefix.length() != numberOfDigits) {
            return false;
        }
        try {
            Integer.parseInt(prefix);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }
    
    private <T> void commitSelection(ComboBox<T> combo) {
        if (combo == null) {
            return;
        }
        PauseTransition pause = new PauseTransition(Duration.millis(hidingDelay));
        pause.setOnFinished(f -> {
            combo.hide();
            combo.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB, false, false, false, false));
        });
        pause.playFromStart();
    }
    
    private <T> List<T> getItemsByLetter(ComboBox<T> combo, String firstLetter, int numberOfDigits) {
        if (combo == null) {
            return new ArrayList<>();
        }
        return combo.getItems().stream()
            .filter(item -> {
                String s = combo.getConverter().toString(item);
                return (s != null && ! s.isEmpty() && s.length() >= numberOfDigits + 2 && 
                    s.substring(numberOfDigits + 1, numberOfDigits + 2).toUpperCase(Locale.ROOT).equals(firstLetter));
            })
            .collect(Collectors.toList());
    }
}
