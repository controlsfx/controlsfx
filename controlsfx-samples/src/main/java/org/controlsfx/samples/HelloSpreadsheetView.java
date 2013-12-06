/**
 * Copyright (c) 2013, ControlsFX
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * Build the UI and launch the Application
 */
public class HelloSpreadsheetView extends ControlsFXSample {

    public static void main(String[] args) {
        launch(args);
    }

    private SpreadsheetView spreadSheetView;
    private StackPane centerPane;
    private int typeOfCell = 0;
    private CheckBox rowHeader = new CheckBox();
    private CheckBox columnHeader = new CheckBox();
    private CheckBox editable = new CheckBox();
    
    @Override public String getSampleName() {
        return "SpreadsheetView";
    }

    @Override
    public Node getPanel(Stage stage) {
        centerPane = new StackPane();
        centerPane.setPadding(new Insets(30));

        int rowCount = 50;
        int columnCount = 10;

        GridBase grid = new GridBase(rowCount, columnCount, generateRowHeight());
        buildGrid(grid,1);//Build both Grid

        generateSpreadsheetView(grid);
        centerPane.getChildren().setAll(spreadSheetView);

        return centerPane;
    }

    /**
     * FIXME need to be removed after
     * Compute RowHeight for test
     * @return
     */
    private Map<Integer,Double> generateRowHeight(){
        Map<Integer,Double> rowHeight = new HashMap<>();
        rowHeight.put(0, 50.0);
        rowHeight.put(5, 50.0);
        rowHeight.put(8, 70.0);
        rowHeight.put(12, 40.0);
        return rowHeight;
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/spreadsheet/SpreadsheetView.html";
    }

    /**
     * Randomly generate a {@link SpreadsheetCell}.
     * Also use the value inside {@link #typeOfCell} to display all cells, only numbers or only dates.
     */
    private SpreadsheetCell generateCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell;
        if(typeOfCell == 0){
            List<String> stringListTextCell = Arrays.asList("Shanghai","Paris","New York City","Bangkok","Singapore","Johannesburg","Berlin","Wellington","London","Montreal");
            final double random = Math.random();
            if (random < 0.25) {
                List<String> stringList = Arrays.asList("China","France","New Zealand","United States","Germany","Canada");
                cell = SpreadsheetCellType.LIST(stringList).createCell(row, column, rowSpan, colSpan, stringList.get((int)(Math.random()*6)));
            } else if (random >= 0.25 && random < 0.5) {
                cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan,stringListTextCell.get((int)(Math.random()*10)));
            }else if (random >= 0.5 && random < 0.75) {
               cell = generateNumberCell(row, column, rowSpan, colSpan);
            }else{
               cell = generateDateCell(row, column, rowSpan, colSpan);
            }
        }else if(typeOfCell == 1){
            cell = generateNumberCell(row, column, rowSpan, colSpan);
        }else{
            cell = generateDateCell(row, column, rowSpan, colSpan);
        }

        // Styling for preview
        if(row%5 ==0){
            cell.getStyleClass().add("five_rows");
        }
        if(column == 0 && rowSpan == 1){
            cell.getStyleClass().add("row_header");
        }
        if(row == 0) {
            cell.getStyleClass().add("col_header");
        }
        return cell;
    }
    
    /**
     * Generate a Date Cell with a random format.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return
     */
    private SpreadsheetCell generateDateCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell = SpreadsheetCellType.DATE.createCell(row, column, rowSpan, colSpan, LocalDate.now().plusDays((int)(Math.random()*10)));
        final double random = Math.random();
        if(random < 0.25){
            cell.setFormat("EEEE d");
        }else if (random < 0.5){
            cell.setFormat("dd/MM :YY");
        }else{
            cell.setFormat("dd/MM/YYYY");
        }
        return cell;
    }

    /**
     * Generate a Number Cell with a random format.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return
     */
    private SpreadsheetCell generateNumberCell(int row, int column, int rowSpan, int colSpan) {
        final double random = Math.random();
        SpreadsheetCell cell;
        if(random < 0.3){
            cell = SpreadsheetCellType.INTEGER.createCell(row, column, rowSpan, colSpan,Math.round((float)Math.random()*100));
        }else{
            cell = SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, colSpan,(double)Math.round((Math.random()*100)*100)/100);
            final double randomFormat = Math.random();
            if(randomFormat < 0.25){
                cell.setFormat("#,##0.00€");
            }else if (randomFormat < 0.5){
                cell.setFormat("0.###E0 km/h");
            }else{
                cell.setFormat("0.###E0");
            }
        }
        return cell;
    }

    
    private void generateSpreadsheetView(GridBase grid){
        spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.setShowRowHeader(rowHeader.isSelected());
        spreadSheetView.setShowColumnHeader(columnHeader.isSelected());
        spreadSheetView.setEditable(editable.isSelected());
    }
    /**
     * Build the grid with the type specifying for normal(0) or Both span(1).
     * @param grid
     * @param type
     */
    private void buildGrid(GridBase grid, int type){
        normalGrid(grid);
        if(type == 0){
            buildBothGrid(grid);
        }
    }
    /**
     * Build the grid with no span.
     * @param grid
     */
    private void normalGrid(GridBase grid) {
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>(grid.getRowCount());
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList(); //new DataRow(row, grid.getColumnCount());
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                dataRow.add(generateCell(row, column, 1, 1));
            }
            rows.add(dataRow);
        }
        grid.setRows(rows);
    }
    
    /**
     * Build a sample RowSpan and ColSpan grid
     * @param grid
     */
    private void buildBothGrid(GridBase grid) {
        grid.spanRow(2, 2, 2);
        grid.spanColumn(2, 2, 2);

        grid.spanRow(4, 2, 4);

        grid.spanColumn(5, 8, 2);

        grid.spanRow(15, 3, 8);

        grid.spanRow(3, 5, 5);
        grid.spanColumn(3, 5, 5);

        grid.spanRow(2, 10, 4);
        grid.spanColumn(3, 10, 4);

        grid.spanRow(2, 12, 3);
        grid.spanColumn(3, 22, 3);

        grid.spanRow(1, 27, 4);

        grid.spanColumn(4, 30, 3);
        grid.spanRow(4, 30, 3);
    }

    @Override public Node getControlPanel() {
        return buildCommonControlGrid("Both");
    }
    
    /**
     * Build a common control Grid with some options on the left to control the
     * SpreadsheetViewInternal
     * @param gridType 
     *
     * @param spreadsheetView
     * @return
     */
    private GridPane buildCommonControlGrid(String gridType) {
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
            @Override public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowRowHeader(arg2);
            }
        });

        // column header
        Label columnHeaderLabel = new Label("Column header: ");
        columnHeaderLabel.getStyleClass().add("property");
        grid.add(columnHeaderLabel, 0, row);
        columnHeader = new CheckBox();
        columnHeader.setSelected(true);
        spreadSheetView.setShowColumnHeader(true);
        grid.add(columnHeader, 1, row++);
        columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowColumnHeader(arg2);
            }
        });

        // editable
        Label editableLabel = new Label("Editable: ");
        editableLabel.getStyleClass().add("property");
        grid.add(editableLabel, 0, row);
        editable = new CheckBox();
        editable.setSelected(true);
        spreadSheetView.setEditable(true);
        grid.add(editable, 1, row++);
        spreadSheetView.editableProperty().bind(editable.selectedProperty());

        // span style
        Label spanModelLabel = new Label("Span model: ");
        spanModelLabel.getStyleClass().add("property");
        grid.add(spanModelLabel, 0, row);
        
        final ChoiceBox<String> typeOfGrid = new ChoiceBox<String>(FXCollections.observableArrayList("Normal", "Both"));
        typeOfGrid.setValue(gridType);
        typeOfGrid.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                    int rowCount = 50;
                    int columnCount = 10;
                    GridBase grid = new GridBase(rowCount, columnCount, generateRowHeight());
                    buildGrid(grid, arg2.intValue());

                    generateSpreadsheetView(grid);
                    centerPane.getChildren().setAll(spreadSheetView);
            }
        });
        grid.add(typeOfGrid, 1, row++);
        
        Label typeOfCellLabel = new Label("Type of cell: ");
        typeOfCellLabel.getStyleClass().add("property");
        grid.add(typeOfCellLabel, 0, row);
        
        final Label indicationLabel = new Label("This mode displays all kind of different values with different formats.");
        indicationLabel.setWrapText(true);
        indicationLabel.getStyleClass().add("indicationLabel");
        final ChoiceBox<String> cellChoices = new ChoiceBox<String>(FXCollections.observableArrayList("All", "Numbers", "Date"));
        cellChoices.setValue("All");
        cellChoices.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                    typeOfCell = arg2.intValue();
                    if(typeOfCell == 0){
                        indicationLabel.setText("This mode displays all kind of different values with different formats.");
                    }else if(typeOfCell == 1){
                        indicationLabel.setText("This mode displays Numbers (Integer and Double) with different formats."
                                + "Copy pasting is working independently of the format.");
                    }else{
                        indicationLabel.setText("This mode displays dates with different formats."
                                + "Copy pasting is working independently of the format.");
                    }
                    int rowCount = 50;
                    int columnCount = 10;
                    GridBase grid = new GridBase(rowCount, columnCount, generateRowHeight());
                    
                    buildGrid(grid, typeOfGrid.getSelectionModel().getSelectedIndex());

                    generateSpreadsheetView(grid);
                    centerPane.getChildren().setAll(spreadSheetView);
            }
        });
        grid.add(cellChoices, 1, row++);
        grid.add(indicationLabel, 0, row++);
        GridPane.setColumnSpan(indicationLabel, 2);
        return grid;
    }
}
