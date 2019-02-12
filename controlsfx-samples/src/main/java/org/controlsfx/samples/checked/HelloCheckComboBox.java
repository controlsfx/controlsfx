/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package org.controlsfx.samples.checked;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.samples.Utils;

public class HelloCheckComboBox extends ControlsFXSample {
    
    private final Label checkedItemsLabel = new Label();
    private CheckComboBox<String> checkComboBox;
    
    @Override public String getSampleName() {
        return "CheckComboBox";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/CheckComboBox.html";
    }
    
    @Override public String getSampleDescription() {
        return "A simple UI control that makes it possible to select zero or "
                + "more items within a ComboBox without the need to set a custom "
                + "cell factory or manually create boolean properties for each "
                + "row - simply use the check model property to request the "
                + "current selection state.";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        final ObservableList<String> strings = FXCollections.observableArrayList();
        for (int i = 0; i <= 100; i++) {
            strings.add("Item " + i);
        }

        // normal ComboBox
        grid.add(new Label("Normal ComboBox: "), 0, row);
        final ComboBox<String> comboBox = new ComboBox<>(strings);
        comboBox.focusedProperty().addListener((o, ov, nv) -> {
            if(nv) comboBox.show(); else comboBox.hide();   
        });
        grid.add(comboBox, 1, row++);
        
        // CheckComboBox
        checkComboBox = new CheckComboBox<>(strings);
        checkComboBox.focusedProperty().addListener((o, ov, nv) -> {
            if(nv) checkComboBox.show(); else checkComboBox.hide();
        });
        checkComboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) change -> {
            updateText(checkedItemsLabel, change.getList());
            
            while (change.next()) {
                System.out.println("============================================");
                System.out.println("Change: " + change);
                System.out.println("Added sublist " + change.getAddedSubList());
                System.out.println("Removed sublist " + change.getRemoved());
                System.out.println("List " + change.getList());
                System.out.println("Added " + change.wasAdded() + " Permutated " + change.wasPermutated() + " Removed " + change.wasRemoved() + " Replaced "
                        + change.wasReplaced() + " Updated " + change.wasUpdated());
                System.out.println("============================================");
            }
        });
        grid.add(new Label("CheckComboBox: "), 0, row);
        grid.add(checkComboBox, 1, row++);
        
        CheckComboBox<Person> checkComboBox2 = new CheckComboBox<>(Person.createDemoList());
        checkComboBox2.setConverter(new StringConverter<Person>() {
            @Override
            public String toString(Person object) {
                return object.getFullName();
            }
            @Override
            public Person fromString(String string) {
                return null;
            }
        });
        checkComboBox2.focusedProperty().addListener((o, ov, nv) -> {
            if(nv) checkComboBox2.show(); else checkComboBox2.hide();
        });
        grid.add(new Label("CheckComboBox with data objects: "), 0, row);
        grid.add(checkComboBox2, 1, row);
        
        return grid;
    }
    
    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        Label label1 = new Label("Checked items: ");
        label1.getStyleClass().add("property");
        grid.add(label1, 0, 0);
        grid.add(checkedItemsLabel, 1, row++);
        updateText(checkedItemsLabel, null);
        
        Label checkItem2Label = new Label("Check 'Item 2': ");
        checkItem2Label.getStyleClass().add("property");
        grid.add(checkItem2Label, 0, row);
        final CheckBox checkItem2Btn = new CheckBox();
        checkItem2Btn.setOnAction(e -> {
            IndexedCheckModel<String> cm = checkComboBox.getCheckModel();
            if (cm != null) {
                cm.toggleCheckState(2);
            }
        });
        grid.add(checkItem2Btn, 1, row);
        
        return grid;
    }
    
    protected void updateText(Label label, ObservableList<? extends String> list) {
        final StringBuilder sb = new StringBuilder();
        
        if (list != null) {
            for (int i = 0, max = list.size(); i < max; i++) {
                sb.append(list.get(i));
                if (i < max - 1) {
                    sb.append(", ");
                }
            }
        }
        
        final String str = sb.toString();
        label.setText(str.isEmpty() ? "<empty>" : str);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}

class Person {
    private StringProperty firstName = new SimpleStringProperty();
    private StringProperty lastName = new SimpleStringProperty();
    private ReadOnlyStringWrapper fullName = new ReadOnlyStringWrapper();
    
    public Person(String firstName, String lastName) {
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        fullName.bind(Bindings.concat(firstName, " ", lastName));
    }
    
    public static final ObservableList<Person> createDemoList() {
        final ObservableList<Person> result = FXCollections.observableArrayList();
        result.add(new Person("Paul", "McCartney"));
        result.add(new Person("Andrew Lloyd", "Webber"));
        result.add(new Person("Herb", "Alpert"));
        result.add(new Person("Emilio", "Estefan"));
        result.add(new Person("Bernie", "Taupin"));
        result.add(new Person("Elton", "John"));
        result.add(new Person("Mick", "Jagger"));
        result.add(new Person("Keith", "Richerds"));
        return result;
    }

    public final StringProperty firstNameProperty() {
        return this.firstName;
    }

    public final java.lang.String getFirstName() {
        return this.firstNameProperty().get();
    }

    public final void setFirstName(final String firstName) {
        this.firstNameProperty().set(firstName);
    }

    public final StringProperty lastNameProperty() {
        return this.lastName;
    }

    public final String getLastName() {
        return this.lastNameProperty().get();
    }

    public final void setLastName(final String lastName) {
        this.lastNameProperty().set(lastName);
    }

    public final ReadOnlyStringProperty fullNameProperty() {
        return this.fullName.getReadOnlyProperty();
    }

    public final String getFullName() {
        return this.fullNameProperty().get();
    }
}
