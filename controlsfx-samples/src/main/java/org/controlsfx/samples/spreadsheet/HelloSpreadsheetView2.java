/**
 * Copyright (c) 2013, 2016 ControlsFX
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
package org.controlsfx.samples.spreadsheet;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.spreadsheet.Filter;
import org.controlsfx.control.spreadsheet.FilterBase;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.Picker;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.controlsfx.samples.Utils;

/**
 *
 * Build the UI and launch the Application
 */
public class HelloSpreadsheetView2 extends ControlsFXSample {

    public static void main(String[] args) {
        launch(args);
    }

    private SpreadsheetView spreadSheetView;
    private StackPane centerPane;
    private final CheckBox rowHeader = new CheckBox();
    private final CheckBox columnHeader = new CheckBox();
    private final CheckBox selectionMode = new CheckBox();
    private final CheckBox displaySelection = new CheckBox();
    private final CheckBox editable = new CheckBox();

    @Override
    public String getSampleName() {
        return "SpreadsheetView 2";
    }

    @Override
    public String getSampleDescription() {
        return "The SpreadsheetView is a control similar to the JavaFX TableView control "
                + "but with different functionalities and use cases. The aim is to have a "
                + "powerful grid where data can be written and retrieved.\n\n"
                + "Here you have an example where some information about fictive "
                + "companies are displayed. They have different type and format.\n\n"
                + "After that, some random generated cells are displayed with some span.\n\n"
                + "Don't forget to right-click on headers and cells to discover some features.";
    }

    @Override
    public String getControlStylesheetURL() {
        return "/org/controlsfx/samples/spreadsheetSample.css";
    }

    @Override
    public Node getPanel(Stage stage) {
        spreadSheetView = new SpreadsheetViewExample2();
        centerPane = new StackPane(spreadSheetView);
        return centerPane;
    }

    @Override
    public Node getControlPanel() {
        return buildCommonControlGrid();
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/spreadsheet/SpreadsheetView.html";
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * SpreadsheetViewInternal
     *
     * @param gridType
     *
     * @param spreadsheetView
     * @return
     */
    private GridPane buildCommonControlGrid() {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        int row = 0;

        // row header
        Label rowHeaderLabel = new Label("Row header: ");
        rowHeaderLabel.getStyleClass().add("property");
        grid.add(rowHeaderLabel, 0, row);
        rowHeader.setSelected(true);
        spreadSheetView.setShowRowHeader(true);

        grid.add(rowHeader, 1, row++);
        rowHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowRowHeader(arg2);
            }
        });

        // column header
        Label columnHeaderLabel = new Label("Column header: ");
        columnHeaderLabel.getStyleClass().add("property");
        grid.add(columnHeaderLabel, 0, row);
        columnHeader.setSelected(true);
        spreadSheetView.setShowColumnHeader(true);

        grid.add(columnHeader, 1, row++);
        columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowColumnHeader(arg2);
            }
        });

        // editable
        Label editableLabel = new Label("Editable: ");
        editableLabel.getStyleClass().add("property");
        grid.add(editableLabel, 0, row);
        editable.setSelected(true);
        spreadSheetView.setEditable(true);
        grid.add(editable, 1, row++);
        spreadSheetView.editableProperty().bind(editable.selectedProperty());

        //Row Header width
        Label rowHeaderWidth = new Label("Row header width: ");
        rowHeaderWidth.getStyleClass().add("property");
        grid.add(rowHeaderWidth, 0, row);
        Slider slider = new Slider(15, 100, 30);
        spreadSheetView.rowHeaderWidthProperty().bind(slider.valueProperty());

        grid.add(slider, 1, row++);

        //Zoom
        Label zoom = new Label("Zoom: ");
        zoom.getStyleClass().add("property");
        grid.add(zoom, 0, row);
        Slider sliderZoom = new Slider(0.25, 2, 1);
        spreadSheetView.zoomFactorProperty().bindBidirectional(sliderZoom.valueProperty());

        grid.add(sliderZoom, 1, row++);

        // Multiple Selection
        Label selectionModeLabel = new Label("Multiple selection: ");
        selectionModeLabel.getStyleClass().add("property");
        grid.add(selectionModeLabel, 0, row);
        selectionMode.setSelected(true);
        grid.add(selectionMode, 1, row++);
        selectionMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean isSelected) {
                spreadSheetView.getSelectionModel().clearSelection();
                spreadSheetView.getSelectionModel().setSelectionMode(isSelected ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
            }
        });

        // Display selection
        Label displaySelectionLabel = new Label("Display selection: ");
        displaySelectionLabel.getStyleClass().add("property");
        grid.add(displaySelectionLabel, 0, row);
        displaySelection.setSelected(true);
        grid.add(displaySelection, 1, row++);
        displaySelection.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean isSelected) {
                spreadSheetView.getGrid().setDisplaySelection(isSelected);
                spreadSheetView.getSelectionModel().clearSelection();
            }
        });

        return grid;
    }

    public class SpreadsheetViewExample2 extends SpreadsheetView {

        public SpreadsheetViewExample2() {
            int rowCount = 31; //Will be re-calculated after if incorrect.
            int columnCount = 8;

            GridBase grid = new GridBase(rowCount, columnCount);
            buildGrid(grid);

            setGrid(grid);
            Filter filter = new FilterBase(this, 0);
            Filter filter1 = new FilterBase(this, 1);
            Filter filter2 = new FilterBase(this, 2);
            Filter filter3 = new FilterBase(this, 3);
            Filter filter4 = new FilterBase(this, 4);
            setFilteredRow(11);

            getColumns().get(0).setFilter(filter);
            getColumns().get(1).setFilter(filter1);
            getColumns().get(2).setFilter(filter2);
            getColumns().get(3).setFilter(filter3);
            getColumns().get(4).setFilter(filter4);

            getFixedRows().add(10);

            //Hiding rows.
            Picker picker = new Picker() {
                @Override
                public void onClick() {
                    //If my details are hidden
                    if (getHiddenRows().get(3)) {
                        getStyleClass().remove("plus-picker");
                        getStyleClass().add("minus-picker");
                        showRow(3);
                        showRow(4);
                        showRow(5);
                        showRow(6);
                    } else {
                        getStyleClass().remove("minus-picker");
                        getStyleClass().add("plus-picker");
                        hideRow(3);
                        hideRow(4);
                        hideRow(5);
                        hideRow(6);
                    }
                }
            };
            picker.getStyleClass().setAll("plus-picker");
            getRowPickers().put(2, picker);
            hideRow(3);
            hideRow(4);
            hideRow(5);
            hideRow(6);
            getStylesheets().add(Utils.class.getResource("spreadsheetSample2.css").toExternalForm());
        }

        /**
         * Build the grid.
         *
         * @param grid
         */
        private void buildGrid(GridBase grid) {
            ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

            int rowIndex = 0;
            rows.add(getSeparator(grid, rowIndex++));
            rows.add(getTitle(grid, rowIndex++));
            rows.add(getSubTitle(grid, rowIndex++));
            rows.add(getSeparator(grid, rowIndex++));
            rows.add(getContact1(grid, rowIndex++));
            rows.add(getContact2(grid, rowIndex++));
            rows.add(getContact3(grid, rowIndex++));
            rows.add(getSeparator(grid, rowIndex++));
            rows.add(getOrderTitle(grid, rowIndex++));
            rows.add(getClickMe(grid, rowIndex++));
            rows.add(getSeparator(grid, rowIndex++));
            rows.add(getHeader(grid, rowIndex++));

            for (int i = rowIndex; i < rowIndex + 100; ++i) {
                final ObservableList<SpreadsheetCell> randomRow = FXCollections.observableArrayList();
                randomRow.add(SpreadsheetCellType.INTEGER.createCell(i, 0, 1, 1, (int) (Math.random() * 100)));
                randomRow.add(SpreadsheetCellType.INTEGER.createCell(i, 1, 1, 1, i));
                randomRow.add(SpreadsheetCellType.INTEGER.createCell(i, 2, 1, 1, (int) (Math.random() * 100)));
                SpreadsheetCell cell = SpreadsheetCellType.DOUBLE.createCell(i, 3, 1, 1, (Math.random() * 100));
                cell.setFormat("##.##");
                randomRow.add(cell);
                randomRow.add(SpreadsheetCellType.INTEGER.createCell(i, 4, 1, 1, (int) (Math.random() * 2)));

                for (int column = 5; column < grid.getColumnCount(); column++) {
                    randomRow.add(SpreadsheetCellType.STRING.createCell(i, column, 1, 1, ""));
                }
                rows.add(randomRow);
            }
            grid.setRows(rows);

            grid.spanColumn(4, 1, 0);
            grid.spanColumn(4, 2, 0);
            grid.spanColumn(4, 8, 0);
        }

        private ObservableList<SpreadsheetCell> getTitle(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Customer order details");
            cell.setEditable(false);
            cell.getStyleClass().add("title");
            title.add(cell);

            for (int column = 1; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getSubTitle(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Customer details");
            cell.setEditable(false);
            cell.getStyleClass().add("subtitle");
            title.add(cell);

            for (int column = 1; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getSeparator(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> separator = FXCollections.observableArrayList();

            for (int column = 0; column < grid.getColumnCount(); ++column) {
                SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                cell.getStyleClass().add("separator");
                separator.add(cell);
            }
            return separator;
        }

        private ObservableList<SpreadsheetCell> getContact1(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Number");
            cell.setEditable(false);
            cell.getStyleClass().add("customer");
            title.add(cell);
            title.add(SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, "156"));

            for (int column = 2; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getContact2(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Customer name");
            cell.setEditable(false);
            cell.getStyleClass().add("customer");
            title.add(cell);
            title.add(SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, "Samir"));

            for (int column = 2; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getContact3(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "City");
            cell.setEditable(false);
            cell.getStyleClass().add("customer");
            title.add(cell);
            title.add(SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, "Paris"));
            cell = SpreadsheetCellType.STRING.createCell(row, 2, 1, 1, "");
            cell.setGraphic(new ImageView(new Image(Utils.class.getResourceAsStream("frenchFlag.png"))));
            cell.setEditable(false);
            title.add(cell);

            for (int column = 3; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getOrderTitle(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Order details");
            cell.setEditable(false);
            cell.getStyleClass().add("subtitle");
            title.add(cell);

            for (int column = 1; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getClickMe(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Click me!");
            MenuItem item = new MenuItem("Go to ControlsFX");
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    try {
                        desktop.browse(new URI("http://fxexperience.com/controlsfx/"));
                    } catch (IOException | URISyntaxException ex) {
//                        Logger.getLogger(HelloSpreadsheetView2.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            cell.getPopupItems().add(item);
            cell.setHasPopup(true);
            cell.setEditable(false);
            title.add(cell);

            for (int column = 1; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }

        private ObservableList<SpreadsheetCell> getHeader(GridBase grid, int row) {

            final ObservableList<SpreadsheetCell> title = FXCollections.observableArrayList();

            SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, 0, 1, 1, "Order ID");
            cell.setEditable(false);
            cell.getStyleClass().add("header");
            title.add(cell);

            cell = SpreadsheetCellType.STRING.createCell(row, 1, 1, 1, "Employee ID");
            cell.setEditable(false);
            cell.getStyleClass().add("header");
            title.add(cell);

            cell = SpreadsheetCellType.STRING.createCell(row, 2, 1, 1, "Product ID");
            cell.setEditable(false);
            cell.getStyleClass().add("header");
            title.add(cell);

            cell = SpreadsheetCellType.STRING.createCell(row, 3, 1, 1, "Unit Price");
            cell.setEditable(false);
            cell.getStyleClass().add("header");
            title.add(cell);

            cell = SpreadsheetCellType.STRING.createCell(row, 4, 1, 1, "Quantity");
            cell.setEditable(false);
            cell.getStyleClass().add("header");
            title.add(cell);

            for (int column = 5; column < grid.getColumnCount(); ++column) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "");
                cell.setEditable(false);
                title.add(cell);
            }
            return title;
        }
    }
}
