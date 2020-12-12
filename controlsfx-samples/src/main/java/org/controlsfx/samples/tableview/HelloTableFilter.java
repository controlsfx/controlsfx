/**
 * Copyright (c) 2016, 2020 ControlsFX
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
package org.controlsfx.samples.tableview;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.table.TableFilter;

public class HelloTableFilter extends ControlsFXSample {
    @Override
    public String getSampleName() {
        return "TableFilter";
    }

    private TableFilter<Person> tableFilter;
    private TableColumn<Person, String> firstNameCol;
    private TableColumn<Person, String> lastNameCol;
    private TableColumn<Person, String> emailCol;

    @Override
    @SuppressWarnings("unchecked")
    public Node getPanel(Stage stage) {
        TableView<Person> tableView = new TableView<>();

        firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.getColumns().addAll(firstNameCol, lastNameCol, emailCol);

        tableView.getItems().add(new Person("Jacob", "Smith", "jacob.smith@example.com"));
        tableView.getItems().add(new Person("Isabella", "Johnson", "isabella.johnson@example.com"));
        tableView.getItems().add(new Person("Ethan", "Williams", "ethan.williams@example.com"));
        tableView.getItems().add(new Person("Emma", "Jones", "emma.jones@example.com"));
        tableView.getItems().add(new Person("Michael", "Brown", "michael.brown@example.com"));
        tableView.getItems().add(new Person("Isabella", "Smith", "isabella.smith@example.com"));

        // apply filter
        tableFilter = TableFilter.forTableView(tableView).apply();

        return tableView;
    }

    @Override
    public Node getControlPanel() {
        VBox controlPane = new VBox(10);

        String sFirstname = "Isabella";
        Button buttonFilterFirstname = new Button("Filter First Name '" + sFirstname + "'");
        buttonFilterFirstname.setOnAction(e -> {
            tableFilter.unSelectAllValues(firstNameCol);
            tableFilter.selectValue(firstNameCol, sFirstname);
            tableFilter.executeFilter();
        });
        Button buttonFilterFirstnameReset = new Button("\u21BA");
        buttonFilterFirstnameReset.setOnAction(e -> {
            tableFilter.selectAllValues(firstNameCol);
            tableFilter.executeFilter();
        });

        String sLastname = "Smith";
        Button buttonFilterLastname = new Button("Filter Last Name '" + sLastname + "'");
        buttonFilterLastname.setOnAction(e -> {
            tableFilter.unSelectAllValues(lastNameCol);
            tableFilter.selectValue(lastNameCol, sLastname);
            tableFilter.executeFilter();
        });
        Button buttonFilterLastnamReset = new Button("\u21BA");
        buttonFilterLastnamReset.setOnAction(e -> {
            tableFilter.selectAllValues(lastNameCol);
            tableFilter.executeFilter();
        });

        String sEmail = "michael.brown@example.com";
        Button buttonFilterEmail = new Button("Filter Email '" + sEmail + "'");
        buttonFilterEmail.setOnAction(e -> {
            tableFilter.unSelectAllValues(emailCol);
            tableFilter.selectValue(emailCol, sEmail);
            tableFilter.executeFilter();
        });
        Button buttonFilterEmailReset = new Button("\u21BA");
        buttonFilterEmailReset.setOnAction(e -> {
            tableFilter.selectAllValues(emailCol);
            tableFilter.executeFilter();
        });

        Button buttonResetAll = new Button("Reset All Filters \u21BA");
        buttonResetAll.setOnAction(e -> {
            tableFilter.resetAllFilters();
        });

        CheckBox cbRegularExpSearchStrategy = new CheckBox("Use regular expressions");
        cbRegularExpSearchStrategy.selectedProperty().addListener((ov, old_val, new_val) -> {
            if(cbRegularExpSearchStrategy.isSelected()) {
                tableFilter.setSearchStrategy((input,target) -> {
                    try {
                        return target.matches(input);
                    } catch (Exception e) {
                        return false;
                    }
                });
            } else {
                // fallback to normal
                tableFilter.setSearchStrategy((inputString, subjectString) -> subjectString.toLowerCase().contains(inputString.toLowerCase()));
            }
        });

        controlPane.getChildren().add(new Label("Programmatic Filtering:"));
        controlPane.getChildren().add(new HBox(10, buttonFilterFirstname, buttonFilterFirstnameReset));
        controlPane.getChildren().add(new HBox(10,buttonFilterLastname, buttonFilterLastnamReset));
        controlPane.getChildren().add(new HBox(10,buttonFilterEmail, buttonFilterEmailReset));
        buttonResetAll.setPrefWidth(Double.MAX_VALUE);
        controlPane.getChildren().add(buttonResetAll);
        controlPane.getChildren().add(new Label("Custom Search Strategies (e.g., '.*o.*n' for 'Last Name')"));
        controlPane.getChildren().add(cbRegularExpSearchStrategy);

        return controlPane;
    }

    @Override
    public String getJavaDocURL() {
        return "org/controlsfx/control/table/TableFilter.html";
    }

    @Override
    public String getSampleDescription() {
        return "Applies a filtering control to a provided TableView instance. "
                + "The filter will be applied immediately on construction, "
                + "and can be made visible by right-clicking the desired column to filter on.";
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Person {

        private final SimpleStringProperty firstName;
        private final SimpleStringProperty lastName;
        private final SimpleStringProperty email;

        public Person(String firstName, String lastName, String email) {
            this.firstName = new SimpleStringProperty(firstName);
            this.lastName = new SimpleStringProperty(lastName);
            this.email = new SimpleStringProperty(email);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String firstName) {
            this.firstName.set(firstName);
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String lastName) {
            this.lastName.set(lastName);
        }

        public String getEmail() {
            return email.get();
        }

        public void setEmail(String fName) {
            email.set(fName);
        }
    }
}
