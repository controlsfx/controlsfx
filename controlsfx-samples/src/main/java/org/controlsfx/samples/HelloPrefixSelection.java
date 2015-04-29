package org.controlsfx.samples;

import impl.org.controlsfx.tools.PrefixSelectionCustomizer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
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
        
        return grid;
    }
    
    @Override public String getSampleDescription() {
        return "This utility class can be used to customize a ChoiceBox or ComboBox"
                + " and enable the prefix selection feature. This will enable the user to type letters or"
                + " digits on the keyboard and die ChoiceBox or ComboBox will attempt to"
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
