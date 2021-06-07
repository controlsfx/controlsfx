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
import java.util.List;
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
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.FilteredTableView;
import org.controlsfx.control.tableview2.actions.ColumnFixAction;
import org.controlsfx.control.tableview2.actions.RowFixAction;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupFilter;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupNumberFilter;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupStringFilter;
import org.controlsfx.samples.Utils;

/**
 *
 * Build the UI and launch the Application
 */
public class HelloFilteredTableView extends ControlsFXSample {

    private final ObservableList<Person> data = generateData(100);
    private FilteredTableViewSample table;
    private StackPane centerPane;
    private BooleanProperty southVisible = new SimpleBooleanProperty();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public String getSampleName() {
        return "FilteredTableView";
    }

    @Override
    public String getSampleDescription() {
        return "The FilteredTableView is a subclass of TableView2,"
                + " an advanced JavaFX TableView control that provides extended filtering options. It can"
                + " be used as drop-in replacement control for the existing TableView, and provides"
                + " different functionalities and use cases like row and column fixing, row header and"
                + " filtering editors.";
    }

    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/tableview2/tableview2.css";
    }

    @Override
    public Node getPanel(Stage stage) {
        table = new FilteredTableViewSample();
        centerPane = new StackPane(table);
        return centerPane;
    }

    @Override
    public Node getControlPanel() {
        return new VBox(10, buildCommonControl(), buildTableView2Control(), buildFilteredTableViewControl());
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/tableview2/FilteredTableView.html";
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * FilteredTableView
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
        CheckBox sortedList = new CheckBox("Use SortedList");
        showData.setSelected(true);
        showData.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                if (sortedList.isSelected()) {
                    FilteredTableView.configureForFiltering(table, data);
                } else {
                    table.setItems(data);
                }
            } else {
                table.setItems(null);
            }
        });
        grid.add(showData, 0, row++);

        sortedList.selectedProperty().addListener((obs, ov, nv) -> {
            if (nv) {
                FilteredTableView.configureForFiltering(table, data);
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

        CheckBox blendSouthFilter = new CheckBox("Blend SouthNode");
        blendSouthFilter.disableProperty().bind(southVisible.not());
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
                FilteredTableColumn<Person, Number> tc = new FilteredTableColumn<>();
                final MenuItem menuItem = new MenuItem("Reset Filter");
                menuItem.disableProperty().bind(table.predicateProperty().isNull());
                menuItem.setOnAction(e -> table.resetFilter());
                tc.setContextMenu(new ContextMenu(menuItem));
                tc.setOnFilterAction(e -> {
                    if (table.getPredicate() != null) {
                        table.resetFilter();
                    }
                });

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

    private Node buildFilteredTableViewControl() {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));

        int row = 0;
        final Label label = new Label("Select the columns filter editor from the options below");
        label.setWrapText(true);
        grid.add(label, 0, row++);

        ToggleGroup filterGroup = new ToggleGroup();
        RadioButton popupFilter = new RadioButton("Use PopupFilter");
        popupFilter.setSelected(true);
        popupFilter.setToggleGroup(filterGroup);
        grid.add(popupFilter, 0, row++);
        RadioButton southFilter = new RadioButton("Use FilterEditor in SouthNode");
        southFilter.setToggleGroup(filterGroup);
        grid.add(southFilter, 0, row++);
        filterGroup.selectedToggleProperty().addListener((obs, ov, nv) -> table.setupFilter(nv == southFilter));

        return new TitledPane("FilteredTableView Options", grid);
    }

    private class FilteredTableViewSample extends FilteredTableView<Person> {

        private final FilteredTableColumn<Person, String> firstName = new FilteredTableColumn<>("First Name");
        private final FilteredTableColumn<Person, String> lastName = new FilteredTableColumn<>("Last Name");
        private final FilteredTableColumn<Person, Integer> age = new FilteredTableColumn<>("Age");
        private final FilteredTableColumn<Person, Color> color = new FilteredTableColumn<>("Color");
        private final FilteredTableColumn<Person, String> city = new FilteredTableColumn<>("City");
        private final FilteredTableColumn<Person, LocalDate> birthday = new FilteredTableColumn<>("Birthday");
        private final FilteredTableColumn<Person, Boolean> active = new FilteredTableColumn<>("Active");
        private SouthFilter<Person, String> editorFirstName;
        private SouthFilter<Person, String> editorLastName;
        private SouthFilter<Person, Integer> editorAge;
        private SouthFilter<Person, Color> editorColor;

        private final List<Color> colors = Arrays.asList(Color.CADETBLUE, Color.CHARTREUSE, Color.CHOCOLATE, Color.CORAL, Color.CORNSILK, Color.CORNFLOWERBLUE);
        private final List<String> scolors = Arrays.asList("cadetblue", "chartreuse", "chocolate", "coral", "cornsilk", "cornflowerblue");

        public FilteredTableViewSample() {

            firstName.setCellValueFactory(p -> p.getValue().firstNameProperty());
            firstName.setCellFactory(ComboBox2TableCell.forTableColumn("Name 1", "Name 2", "Name 3", "Name 4"));
            firstName.setPrefWidth(110);

            lastName.setCellValueFactory(p -> p.getValue().lastNameProperty());
            lastName.setCellFactory(TextField2TableCell.forTableColumn());
            lastName.setPrefWidth(130);

            age.setCellValueFactory(p -> p.getValue().ageProperty().asObject());
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
            age.setPrefWidth(90);

            color.setCellValueFactory(p -> p.getValue().colorProperty());
            color.setCellFactory(p -> new TableCell<Person, Color>() {
                private final HBox box;
                private final Circle circle;
                {
                    circle = new Circle(10);
                    box = new HBox(circle);
                    box.setAlignment(Pos.CENTER);
                    setText(null);
                }
                @Override
                protected void updateItem(Color item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && ! empty) {
                        circle.setFill(item);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }

            });
            color.setPrefWidth(90);

            city.setCellValueFactory(p -> p.getValue().cityProperty());
            city.setCellFactory(TextField2TableCell.forTableColumn());

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
            getColumns().setAll(fullNameColumn, age, color, city, birthday, active);

            editorFirstName = new SouthFilter<>(firstName, String.class);
            editorLastName = new SouthFilter<>(lastName, String.class);
            editorAge = new SouthFilter<>(age, Integer.class);
            editorColor = new SouthFilter<>(color, Color.class);
            editorColor.getFilterEditor().setCellFactory(c -> new ListCell<Color>() {
                private final HBox box;
                private final Circle circle;
                {
                    circle = new Circle(10);
                    box = new HBox(circle);
                    box.setAlignment(Pos.CENTER);
                    setText(null);
                }
                @Override
                protected void updateItem(Color item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null && ! empty) {
                        circle.setFill(item);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                    }
                }

            });
            editorColor.getFilterEditor().setConverter(new StringConverter<Color>() {
                @Override
                public String toString(Color object) {
                    return object != null && colors.indexOf(object) > -1 ?
                            scolors.get(colors.indexOf(object)) : editorColor.getFilterEditor().getEditor().getText();
                }

                @Override
                public Color fromString(String string) {
                    if (string == null || string.isEmpty())
                        return Color.CADETBLUE;
                    try {
                        if (Color.web(string) != null) {
                            return Color.web(string);
                        }
                    } catch (Exception e) {}
                    return Color.CADETBLUE;
                }
            });

            filterAction();

            ContextMenu cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(fullNameColumn)));
            fullNameColumn.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(firstName)));
            final CheckMenuItem miFirstName = new CheckMenuItem("Filter " + firstName.getText());
            firstName.predicateProperty().addListener(o -> miFirstName.setSelected(firstName.getPredicate() != null));
            miFirstName.setOnAction(e ->
                    firstName.setPredicate(miFirstName.isSelected() ? p -> p.contains("1") : null));
            final Menu menuFirstName = new Menu("Filter");
            menuFirstName.getItems().addAll(miFirstName);
            cm.getItems().addAll(menuFirstName);
            firstName.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(lastName)));
            final CheckMenuItem miLastName = new CheckMenuItem("Filter " + lastName.getText());
            lastName.predicateProperty().addListener(o -> miLastName.setSelected(lastName.getPredicate() != null));
            miLastName.setOnAction(e ->
                    lastName.setPredicate(miLastName.isSelected() ? p -> p.contains("2") : null));

            final Menu menuLastName = new Menu("Filter");
            menuLastName.getItems().addAll(miLastName);
            cm.getItems().addAll(menuLastName);
            lastName.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(age)));
            final CheckMenuItem miAge = new CheckMenuItem("Filter " + age.getText());
            age.predicateProperty().addListener(o -> miAge.setSelected(age.getPredicate() != null));
            miAge.setOnAction(e ->
                    age.setPredicate(miAge.isSelected() ? p -> p > 50 : null));

            final Menu menuAge = new Menu("Filter");
            menuAge.getItems().addAll(miAge);
            cm.getItems().addAll(menuAge);
            age.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(city)));
            city.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(birthday), ActionUtils.ACTION_SEPARATOR));
            final CheckMenuItem miBirthday = new CheckMenuItem("Filter " + birthday.getText());
            birthday.predicateProperty().addListener(o -> miBirthday.setSelected(birthday.getPredicate() != null));
            miBirthday.setOnAction(e ->
                    birthday.setPredicate(miBirthday.isSelected() ? p -> p.isAfter(LocalDate.of(1950, 1, 1)) : null));

            final Menu menuBirthday = new Menu("Filter");
            menuBirthday.getItems().addAll(miBirthday);
            cm.getItems().addAll(menuBirthday);
            birthday.setContextMenu(cm);

            cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(active)));
            CheckMenuItem miActive = new CheckMenuItem("Filter " + active.getText());

            miActive.setOnAction(e -> active.setPredicate(miActive.isSelected() ? p -> p : null));
            final Menu menuActive = new Menu("Filter");
            menuActive.getItems().addAll(miActive);
            cm.getItems().addAll(menuActive);
            active.setContextMenu(cm);

            setRowHeaderContextMenuFactory((i, person) -> {
                ContextMenu rowCM = ActionUtils.createContextMenu(Arrays.asList(new RowFixAction(this, i), ActionUtils.ACTION_SEPARATOR));
                if (person != null) {
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
                    rowCM.getItems().add(menuItem);
                }
                final MenuItem menuItemAdd = new MenuItem("Add new Person");
                menuItemAdd.setOnAction(e -> {
                    data.add(new Person());
                });
                rowCM.getItems().add(menuItemAdd);
                return rowCM;
            });

            getFixedColumns().setAll(fullNameColumn);
            getFixedRows().setAll(0, 1, 2);

        }

        public void setupFilter(boolean southFilter) {
            if (southFilter) {
                southNodeFilterAction();
                firstName.setSouthNode(editorFirstName);
                lastName.setSouthNode(editorLastName);
                age.setSouthNode(editorAge);
                color.setSouthNode(editorColor);
            } else {
                filterAction();
                firstName.setSouthNode(null);
                lastName.setSouthNode(null);
                age.setSouthNode(null);
                color.setSouthNode(null);
            }
            southVisible.set(southFilter);
        }

        private void southNodeFilterAction() {
            firstName.setOnFilterAction(e -> {
                if (firstName.getPredicate() != null) {
                    editorFirstName.getFilterEditor().cancelFilter();
                }
            });
            lastName.setOnFilterAction(e -> {
                if (lastName.getPredicate() != null) {
                    editorLastName.getFilterEditor().cancelFilter();
                }
            });
            age.setOnFilterAction(e -> {
                if (age.getPredicate() != null) {
                    editorAge.getFilterEditor().cancelFilter();
                }
            });

            color.setOnFilterAction(e -> {
                if (color.getPredicate() != null) {
                    editorColor.getFilterEditor().cancelFilter();
                }
            });
        }

        private void filterAction() {
            PopupFilter<Person, String> popupFirstNameFilter = new PopupStringFilter<>(firstName);
            firstName.setOnFilterAction(e -> popupFirstNameFilter.showPopup());

            PopupFilter<Person, String> popupLastNameFilter = new PopupStringFilter<>(lastName);
            lastName.setOnFilterAction(e -> popupLastNameFilter.showPopup());

            PopupFilter<Person, Integer> popupAgeFilter = new PopupNumberFilter<>(age);
            age.setOnFilterAction(e -> popupAgeFilter.showPopup());

            PopupStringFilter<Person, Color> popupColorFilter = new PopupStringFilter<>(color);
            popupColorFilter.setConverter(new StringConverter<Color>() {
                @Override
                public String toString(Color object) {
                    return object != null ? scolors.get(colors.indexOf(object)) : "";
                }

                @Override
                public Color fromString(String string) {
                    return string == null || string.isEmpty() ? Color.CADETBLUE : Color.web(string);
                }
            });
            color.setOnFilterAction(e -> popupColorFilter.showPopup());
        }
    }

    private ObservableList<Person> generateData(int numberOfPeople) {
        ObservableList<Person> persons = FXCollections.observableArrayList(e -> new Observable[]{ e.lastNameProperty() });
        List<Color> colors = Arrays.asList(Color.CADETBLUE, Color.CHARTREUSE, Color.CHOCOLATE, Color.CORAL, Color.CORNSILK, Color.CORNFLOWERBLUE);

        for (int i = 0; i < numberOfPeople; i++) {
            final LocalDate date = LocalDate.of(1910 + new Random().nextInt(100), 1+i%11, 1+i%29);
            persons.add(new Person("First Name:  " + i%20, "Last Name: " + i%10,  Period.between(date, LocalDate.now()).getYears(),
                    "City: " + i%3, i%10 != 0, date, colors.get(new Random().nextInt(colors.size()))));
        }

        return persons;
    }

    class Person {

        private final StringProperty firstName = new SimpleStringProperty();
        private final StringProperty lastName = new SimpleStringProperty();
        private final IntegerProperty age = new SimpleIntegerProperty();
        private final StringProperty city = new SimpleStringProperty();
        private final BooleanProperty active = new SimpleBooleanProperty();
        private final ObjectProperty<LocalDate> birthday = new SimpleObjectProperty<>();
        private final ObjectProperty<Color> color = new SimpleObjectProperty<>();

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

        public final Color getColor() {
            return color.get();
        }

        public final void setColor(Color value) {
            color.set(value);
        }

        public final ObjectProperty<Color> colorProperty() {
            return color;
        }

        public Person (String firstName, String lastName, Integer age,
                       String city, boolean active, LocalDate birthday, Color color){
            this.firstName.set(firstName);
            this.lastName.set(lastName);
            this.age.set(age);
            this.city.set(city);
            this.active.set(active);
            this.birthday.set(birthday);
            this.color.set(color);
        }

        @Override
        public String toString() {
            return "Person{" + "firstName=" + firstName.get() + ", lastName=" + lastName.get() +
                    ", age=" + age.get() + ", city=" + city.get() + ", active=" + active.get() +
                    ", birthday=" + birthday.get() + ", color=" + color.get() + '}';
        }

        public Person() {
            this.firstName.set("");
            this.lastName.set("");
            this.age.set(18);
            this.city.set("");
            this.active.set(false);
            this.birthday.set(LocalDate.now());
            this.color.set(Color.CADETBLUE);
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