/**
 * Copyright (c) 2018, 2020 ControlsFX
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
package org.controlsfx.samples.tableview2;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Random;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.controlsfx.control.tableview2.actions.ColumnFixAction;
import org.controlsfx.control.tableview2.actions.RowFixAction;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.controlsfx.samples.Utils;

/**
 *
 * Build the UI and launch the Application
 */
public class HelloTableView2 extends ControlsFXSample {

    private final ObservableList<Person> data = generateData(100);
    private TableView2Sample table;

    private StackPane centerPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public String getSampleName() {
        return "TableView2";
    }

    @Override
    public String getSampleDescription() {
        return "The TableView2 is an advanced JavaFX TableView control, that can "
                + " be used as drop-in replacement control for the existing TableView, and provides"
                + " different functionalities and use cases like row and column fixing and row header.";
    }

    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/tableview2/tableview2.css";
    }

    @Override
    public Node getPanel(Stage stage) {
        table = new TableView2Sample();
        centerPane = new StackPane(table);
        return centerPane;
    }

    @Override
    public Node getControlPanel() {
        return new VBox(10, buildCommonControl(), buildTableView2Control());
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/tableview2/TableView2.html";
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * TableView2
     *
     * @return
     */
    private Node buildCommonControl() {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));

        int row = 0;

        CheckBox tableEditingEnabled = new CheckBox("Table Editing Enabled");
        table.editableProperty().bind(tableEditingEnabled.selectedProperty());
        tableEditingEnabled.setSelected(true);
        grid.add(tableEditingEnabled, 0, row++);

        CheckBox columnsEditingEnabled = new CheckBox("Columns Editing Enabled");
        columnsEditingEnabled.selectedProperty().addListener((obs, ov, nv) -> {
            table.getVisibleLeafColumns().forEach(column -> column.setEditable(nv));
        });
        columnsEditingEnabled.setSelected(true);
        grid.add(columnsEditingEnabled, 0, row++);

        CheckBox cellSelectionEnabled = new CheckBox("Cell Selection Enabled");
        table.getSelectionModel().cellSelectionEnabledProperty().bind(cellSelectionEnabled.selectedProperty());
        grid.add(cellSelectionEnabled, 0, row++);

        CheckBox multipleSelection = new CheckBox("Multiple Selection");
        table.getSelectionModel().selectionModeProperty().bind(Bindings.when(multipleSelection.selectedProperty()).then(SelectionMode.MULTIPLE).otherwise(SelectionMode.SINGLE));
        grid.add(multipleSelection, 0, row++);

        CheckBox constrainedColumnPolicy = new CheckBox("Constrained Column Policy");
        table.columnResizePolicyProperty().bind(Bindings.when(constrainedColumnPolicy.selectedProperty()).then(TableView.CONSTRAINED_RESIZE_POLICY).otherwise(TableView.UNCONSTRAINED_RESIZE_POLICY));
        grid.add(constrainedColumnPolicy, 0, row++);

        CheckBox fixedCellSize = new CheckBox("Set Fixed Cell Size");
        table.fixedCellSizeProperty().bind(Bindings.when(fixedCellSize.selectedProperty()).then(40).otherwise(0));
        grid.add(fixedCellSize, 0, row++);

        CheckBox showTableMenuButton = new CheckBox("Show Table Menu Button");
        table.tableMenuButtonVisibleProperty().bind(showTableMenuButton.selectedProperty());
        grid.add(showTableMenuButton, 0, row++);

        CheckBox showData = new CheckBox("Show Data");
        showData.setSelected(true);
        showData.selectedProperty().addListener((obs, ov, nv) -> {
            table.setItems(nv ? data : null);
        });
        grid.add(showData, 0, row++);

        CheckBox sortedList = new CheckBox("Use SortedList");
        sortedList.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                FilteredList<Person> filteredData = new FilteredList<>(data, p -> true);
                SortedList<Person> sortedData = new SortedList<>(filteredData);
                sortedData.comparatorProperty().bind(table.comparatorProperty());
                table.setItems(sortedData);
            } else {
                table.setItems(data);
            }
        });
        grid.add(sortedList, 0, row++);

        return new TitledPane("TableView Options", grid);
    }

    private Node buildTableView2Control() {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));

        int row = 0;

        CheckBox columnFixing = new CheckBox("Column Fixing Enabled");
        columnFixing.setSelected(true);
        columnFixing.selectedProperty().addListener((obs, ov, nv) -> {
            table.setColumnFixingEnabled(nv);
        });
        grid.add(columnFixing, 0, row++);

        CheckBox rowFixing = new CheckBox("Row Fixing Enabled");
        rowFixing.setSelected(true);
        rowFixing.selectedProperty().addListener((obs, ov, nv) -> {
            table.setRowFixingEnabled(nv);
        });
        grid.add(rowFixing, 0, row++);

        CheckBox southFilter = new CheckBox("Use SouthNode");
        southFilter.selectedProperty().addListener((obs, ov, nv) -> {
            table.setupSouth(nv);
        });
        grid.add(southFilter, 0, row++);

        CheckBox blendSouthFilter = new CheckBox("Blend SouthNode");
        blendSouthFilter.disableProperty().bind(southFilter.selectedProperty().not());
        table.southHeaderBlendedProperty().bind(blendSouthFilter.selectedProperty());
        blendSouthFilter.setSelected(true);
        grid.add(blendSouthFilter, 0, row++);

        CheckBox showRowHeader = new CheckBox("Show Row Header");
        table.rowHeaderVisibleProperty().bind(showRowHeader.selectedProperty());
        grid.add(showRowHeader, 0, row++);

        CheckBox rowFactory = new CheckBox("Use Row Header Factory");
        rowFactory.disableProperty().bind(showRowHeader.selectedProperty().not());
        rowFactory.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                TableColumn2<Person, Number> tc = new TableColumn2<>();
                tc.setContextMenu(new ContextMenu(new MenuItem("Corner")));
                tc.setGraphic(new Rectangle(20, 20, Color.ORANGE));

                Label labelTotal = new Label("Total #1:");
                HBox box = new HBox(labelTotal);
                box.setAlignment(Pos.CENTER_RIGHT);
                box.setPadding(new Insets(0, 5, 0, 0));
                tc.setSouthNode(box);

                tc.setCellValueFactory(p -> p.getValue().getTotalSum());
                tc.setCellFactory(p -> new TableCell<Person, Number>() {
                    private final HBox box;
                    private final Circle circle;
                    private final Label label;
                    {
                        circle = new Circle(5);
                        label = new Label();
                        box = new HBox(10, circle, label);
                    }

                    @Override
                    protected void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && ! empty) {
                            setText(null);
                            circle.setFill(getIndex() % 5 == 0 ? Color.RED : Color.BLUE);
                            label.setText("" + table.getItems().get(getIndex()).getBirthday().getYear() + " " + String.valueOf(item));
                            box.setAlignment(Pos.CENTER);
                            setGraphic(box);
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }

                });
                table.setRowHeader(tc);
            } else {
                table.setRowHeader(null);
            }

            table.setRowHeaderWidth(nv ? 100 : 40);
        });
        grid.add(rowFactory, 0, row++);

        return new TitledPane("TableView2 Options", grid);
    }

    private ObservableList<Person> generateData(int numberOfPeople) {
        ObservableList<Person> persons = FXCollections.observableArrayList(e ->
                new Observable[]{ e.firstNameProperty(), e.lastNameProperty() });

        for (int i = 0; i < numberOfPeople; i++) {
            final LocalDate date = LocalDate.of(1910 + new Random().nextInt(100), 1+i%11, 1+i%29);
            persons.add(new Person("First Name:  " + i%20, "Last Name: " + i%10,  Period.between(date, LocalDate.now()).getYears(),
                    "City: " + i%3, i%10 != 0, date));
        }

        return persons;
    }

    private class TableView2Sample extends TableView2<Person> {

        private final TableColumn2<Person, String> firstName = new TableColumn2<>("First Name");
        private final TableColumn2<Person, String> lastName = new TableColumn2<>("Last Name");
        private final TableColumn2<Person, String> city = new TableColumn2<>("City");
        private final TableColumn2<Person, Integer> age = new TableColumn2<>("Age");
        private final TableColumn2<Person, LocalDate> birthday = new TableColumn2<>("Birthday");
        private final TableColumn2<Person, Boolean> active = new TableColumn2<>("Active");
        private HBox boxFirstName, boxLastName;

        public TableView2Sample() {

            firstName.setCellValueFactory(p -> p.getValue().firstNameProperty());
            firstName.setCellFactory(ComboBox2TableCell.forTableColumn("Name 1", "Name 2", "Name 3", "Name 4"));
            firstName.setPrefWidth(110);

            lastName.setCellValueFactory(p -> p.getValue().lastNameProperty());
            lastName.setCellFactory(TextField2TableCell.forTableColumn());
            lastName.setPrefWidth(130);

            city.setCellValueFactory(p -> p.getValue().cityProperty());
            city.setCellFactory(TextField2TableCell.forTableColumn());
            city.setPrefWidth(90);

            age.setCellValueFactory((TableColumn.CellDataFeatures<Person, Integer> p) -> p.getValue().ageProperty().asObject());
            age.setCellFactory(TextField2TableCell.forTableColumn(new StringConverter<Integer>() {
                @Override
                public String toString(Integer object) {
                    return String.valueOf(object);
                }

                @Override
                public Integer fromString(String string) {
                    return Integer.parseInt(string);
                }
            }));
            age.setPrefWidth(60);

            birthday.setCellValueFactory(p -> p.getValue().birthdayProperty());
            birthday.setPrefWidth(100);
            birthday.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<LocalDate>() {
                @Override
                public String toString(LocalDate date) {
                    if (date == null) {
                        return "" ;
                    }
                    return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date);
                }

                @Override
                public LocalDate fromString(String string) {
                    return LocalDate.now();
                }

            }));

            active.setText("Active");
            active.setCellValueFactory(p -> p.getValue().activeProperty());
            active.setCellFactory(CheckBoxTableCell.forTableColumn(active));
            active.setPrefWidth(60);

            setItems(data);

            TableColumn fullNameColumn = new TableColumn("Full Name");
            fullNameColumn.getColumns().addAll(firstName, lastName);
            getColumns().setAll(fullNameColumn, city, age, birthday, active);

            ContextMenu cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(fullNameColumn)));
            fullNameColumn.setContextMenu(cm);

            Label labelFirstName = new Label("#1:");
            labelFirstName.textProperty().bind(Bindings.createStringBinding(() ->
                    "#1: " + getItems().stream().filter(t -> t.getFirstName().contains("1")).count(), getItems()));
            boxFirstName = new HBox(10, labelFirstName);
            boxFirstName.setAlignment(Pos.CENTER);

            Label labelLastName = new Label("#1:");
            labelLastName.textProperty().bind(Bindings.createStringBinding(() ->
                    "#1: " + getItems().stream().filter(t -> t.getLastName().contains("1")).count(), getItems()));
            boxLastName = new HBox(10, labelLastName);
            boxLastName.setAlignment(Pos.CENTER);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(city)));
            city.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(birthday), ActionUtils.ACTION_SEPARATOR));
            birthday.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(active)));
            active.setContextMenu(cm);

            setRowHeaderContextMenuFactory((i, person) -> {
                ContextMenu rowCM = ActionUtils.createContextMenu(Arrays.asList(new RowFixAction(this, i), ActionUtils.ACTION_SEPARATOR));

                final MenuItem menuItem = new MenuItem("Remove  " + person.getFirstName());
                menuItem.setOnAction(e -> {
                    if (i >= 0) {
                        final ObservableList<Person> items = getItems();
                        if (items instanceof SortedList) {
                            int sourceIndex = ((SortedList<Person>) items).getSourceIndexFor(data, i);
                            data.remove(sourceIndex);
                        } else {
                            data.remove(i.intValue());
                        }
                    }
                });
                final MenuItem menuItemAdd = new MenuItem("Add new Person");
                menuItemAdd.setOnAction(e -> data.add(new Person()));
                rowCM.getItems().addAll(menuItem, menuItemAdd);
                return rowCM;
            });

            getFixedColumns().setAll(fullNameColumn);
            getFixedRows().setAll(0, 1, 2);
        }

        public void setupSouth(boolean useSouthNode) {
            if (useSouthNode) {
                firstName.setSouthNode(boxFirstName);
                lastName.setSouthNode(boxLastName);
            } else {
                firstName.setSouthNode(null);
                lastName.setSouthNode(null);
            }
        }
    }

    class Person {

        private final StringProperty firstName = new SimpleStringProperty();
        private final StringProperty lastName = new SimpleStringProperty();
        private final IntegerProperty age = new SimpleIntegerProperty();
        private final StringProperty city = new SimpleStringProperty();
        private final BooleanProperty active = new SimpleBooleanProperty();
        private final ObjectProperty<LocalDate> birthday = new SimpleObjectProperty<>();

        public final LocalDate getBirthday() {
            return birthday.get();
        }

        public final void setBirthday(LocalDate value) {
            birthday.set(value);
        }

        public final ObjectProperty<LocalDate> birthdayProperty() {
            return birthday;
        }


        public final StringProperty firstNameProperty() {
            return this.firstName;
        }

        public final java.lang.String getFirstName() {
            return this.firstNameProperty().get();
        }

        public final void setFirstName(final java.lang.String firstName) {
            this.firstNameProperty().set(firstName);
        }

        public final StringProperty lastNameProperty() {
            return this.lastName;
        }

        public final java.lang.String getLastName() {
            return this.lastNameProperty().get();
        }

        public final void setLastName(final java.lang.String lastName) {
            this.lastNameProperty().set(lastName);
        }

        public final StringProperty cityProperty() {
            return this.city;
        }

        public final java.lang.String getCity() {
            return this.cityProperty().get();
        }

        public final void setCity(final java.lang.String city) {
            this.cityProperty().set(city);
        }

        public final BooleanProperty activeProperty() {
            return this.active;
        }

        public final boolean isActive() {
            return this.activeProperty().get();
        }

        public final void setActive(final boolean active) {
            this.activeProperty().set(active);
        }

        public final int getAge() {
            return age.get();
        }

        public final IntegerProperty ageProperty() {
            return age;
        }

        public final void setAge(int age) {
            this.age.set(age);
        }

        public Person (String firstName, String lastName, Integer age,
                       String city, boolean active, LocalDate birthday){
            this.firstName.set(firstName);
            this.lastName.set(lastName);
            this.age.set(age);
            this.city.set(city);
            this.active.set(active);
            this.birthday.set(birthday);
        }

        @Override
        public String toString() {
            return "Person{" + "firstName=" + firstName.get() + ", lastName=" + lastName.get() +
                    ", age=" + age.get() + ", city=" + city.get() + ", active=" + active.get() +
                    ", birthday=" + birthday.get() + '}';
        }


        public Person() {
            this.firstName.set("");
            this.lastName.set("");
            this.age.set(18);
            this.city.set("");
            this.active.set(false);
            this.birthday.set(LocalDate.now());
        }

        public IntegerProperty getTotalSum() {
            IntegerProperty sum = new SimpleIntegerProperty();
            sum.bind(Bindings.createIntegerBinding(() -> getNumberOf(getFirstName()) + getNumberOf(getLastName()) + getNumberOf(getCity()),
                    firstName, lastName, city));
            return sum;
        }

        private int getNumberOf(String text) {
            try {
                final String[] split = text.split(":");
                if (split.length == 2) {
                    return Integer.parseInt(split[1].trim());
                }
            } catch (NumberFormatException e) {}
            return 0;
        }
    }
}