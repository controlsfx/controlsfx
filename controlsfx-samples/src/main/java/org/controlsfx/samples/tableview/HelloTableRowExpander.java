/**
 * Copyright (c) 2016 ControlsFX
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

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.table.TableRowExpanderColumn;

public class HelloTableRowExpander extends ControlsFXSample {
    @Override
    public String getSampleName() {
        return "TableRowExpanderColumn";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Node getPanel(Stage stage) {
        TableView<Customer> tableView = new TableView<>();
        TableRowExpanderColumn<Customer> expanderColumn = new TableRowExpanderColumn<>(this::createEditor);

        TableColumn<Customer, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Customer, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Customer, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        tableView.getColumns().addAll(expanderColumn, idColumn, nameColumn, emailColumn);

        tableView.setItems(getCustomers());

        return tableView;
    }

    private GridPane createEditor(TableRowExpanderColumn.TableRowDataFeatures<Customer> param) {
        GridPane editor = new GridPane();
        editor.setPadding(new Insets(10));
        editor.setHgap(10);
        editor.setVgap(5);

        Customer customer = param.getValue();

        TextField nameField = new TextField(customer.getName());
        TextField emailField = new TextField(customer.getEmail());

        editor.addRow(0, new Label("Name"), nameField);
        editor.addRow(1, new Label("Email"), emailField);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {
            customer.setName(nameField.getText());
            customer.setEmail(emailField.getText());
            param.toggleExpanded();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> param.toggleExpanded());

        editor.addRow(2, saveButton, cancelButton);

        return editor;
    }

    @Override
    public String getJavaDocURL() {
        return "org/controlsfx/control/table/TableRowExpanderColumn.html";
    }

    @Override
    public String getSampleDescription() {
        return "An extension to TableView which lets the user expand a table row to reveal a custom "
                + "editor right below the cells of the current row. "
                + "Any arbitrary Node can be used as the expanded row editor and there is an API to "
                + "toggle the expanded state of each row. "
                + "The toggle button can be customized by providing setting a custom cellFactory "
                + "for the TableRowExpanderColumn.";
    }

    private ObservableList<Customer> getCustomers() {
        return FXCollections.observableArrayList(
                new Customer(1, "Samantha Stuart", "samantha.stuart@contoso.com"),
                new Customer(2, "Tom Marks", "tom.marks@contoso.com"),
                new Customer(3, "Stuart Gills", "stuart.gills@contoso.com"),
                new Customer(4, "Nicole Williams", "nicole.williams@contoso.com")
        );
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class Customer {
        public SimpleIntegerProperty idProperty = new SimpleIntegerProperty(this, "id");
        public SimpleStringProperty nameProperty = new SimpleStringProperty(this, "name");
        public SimpleStringProperty emailProperty = new SimpleStringProperty(this, "email");

        public Customer(Integer id, String name, String email) {
            setId(id);
            setName(name);
            setEmail(email);
        }

        public Integer getId() {
            return idProperty.get();
        }

        public SimpleIntegerProperty idProperty() {
            return idProperty;
        }

        public void setId(int id) {
            this.idProperty.set(id);
        }

        public String getName() {
            return nameProperty.get();
        }

        public SimpleStringProperty nameProperty() {
            return nameProperty;
        }

        public void setName(String name) {
            this.nameProperty.set(name);
        }

        public String getEmail() {
            return emailProperty.get();
        }

        public SimpleStringProperty emailProperty() {
            return emailProperty;
        }

        public void setEmail(String email) {
            this.emailProperty.set(email);
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Customer customer = (Customer) o;

            return getId() != null ? getId().equals(customer.getId()) : customer.getId() == null;
        }

        @Override
        public int hashCode() {
            return getId() != null ? getId().hashCode() : 0;
        }
    }
}
