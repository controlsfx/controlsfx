/**
 * Copyright (c) 2019, ControlsFX
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.SearchableComboBox;

public class HelloSearchableComboBox extends ControlsFXSample {

    public static void main(String[] args) {
        launch(args);
    }

    @Override public String getSampleName() {
        return "Searchable ComboBox";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SearchableComboBox.html";
    }

    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(12);
        grid.setPadding(new Insets(24));

        ObservableList<String> stringList = FXCollections.observableArrayList("1111", "2222", "Aaaaa", "Abbbb", "Abccc", "Abcdd", "Abcde", "Bbbb", "bbbb", "Cccc", "Dddd", "Eeee", "Ffff", "gggg", "hhhh", "3333");

        grid.add(new Label("Searchable ComboBox<String>"), 0, 2);
        ComboBox<String> searchableStringBox = new SearchableComboBox<>();
        searchableStringBox.setItems(stringList);
        searchableStringBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(searchableStringBox, 1, 2);
        CheckBox searchableStringBoxEditable = new CheckBox("Make ComboBox editable");
        searchableStringBoxEditable.selectedProperty().bindBidirectional(searchableStringBox.editableProperty());
        grid.add(searchableStringBoxEditable, 2, 2);

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

        grid.add(new Label("SearchableComboBox<Person>"), 0, 5);
        ComboBox<Person> searchablePersonBox = new SearchableComboBox<>();
        searchablePersonBox.setItems(personList);
        searchablePersonBox.setMaxWidth(Double.MAX_VALUE);
        grid.add(searchablePersonBox, 1, 5);
        CheckBox searchablePersonBoxEditable = new CheckBox("Make ComboBox editable");
        searchablePersonBoxEditable.selectedProperty().bindBidirectional(searchablePersonBox.editableProperty());
        grid.add(searchablePersonBoxEditable, 2, 5);

        return grid;
    }

    @Override public String getSampleDescription() {
        return "This ComboBox implementation can be used to create a ComboBox"
                + " that can be filtered. While the popup is showing, a filter text field"
                + " allows to filter the displayed items by searching for all items containing"
                + " any of the filter words case insensitively.";
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