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

package org.controlsfx.samples.SpreadsheetView;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.model.Grid;
import org.controlsfx.control.spreadsheet.view.SpreadsheetCells;

/**
 *
 * Build the UI and launch the Application
 */
public class HelloEmptySpreadsheet extends Application implements Sample {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public String getSampleName() {
		return "Empty SpreadsheetView";
	}

	@Override
	public Node getPanel(Stage stage) {
		BorderPane borderPane = new BorderPane();
		
		int rowCount = 15;
		int columnCount = 10;
		Grid grid = new Grid(rowCount, columnCount);
		blankGrid(grid);
//		buildBothGrid(grid);
		
		

		SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
//		spreadSheetView.buildSpreadsheetView(grid);
		
		borderPane.setCenter(spreadSheetView);

//		borderPane.setLeft(buildCommonControlGrid(spreadSheetView));
		
		return borderPane;
	}
	
//	/**
//	 * Build a common control Grid with some options on the left to control the
//	 * SpreadsheetViewInternal
//	 *
//	 * @param spreadsheetView
//	 * @return
//	 */
//	private GridPane buildCommonControlGrid(final SpreadsheetView spv) {
//		final GridPane grid = new GridPane();
//		grid.setHgap(5);
//		grid.setVgap(5);
//		grid.setPadding(new Insets(5, 5, 5, 5));
//
//		final ChoiceBox<Integer> fixedRows = new ChoiceBox<>(FXCollections.observableArrayList(0, 1, 2));
//		fixedRows.getSelectionModel().select(0);
//		fixedRows.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> arg0,
//					Number arg1, Number arg2) {
//				spv.fixRows(arg2.intValue());
//			}
//		});
//
//		final ChoiceBox<Integer> fixedColumns = new ChoiceBox<>(FXCollections.observableArrayList(0, 1, 2));
//		fixedColumns.getSelectionModel().select(0);
//		fixedColumns.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//				spv.fixColumns(arg2.intValue());
//			}
//		});
//
//		final CheckBox rowHeader = new CheckBox("Row Header");
//		rowHeader.setSelected(true);
//		rowHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
//
//			@Override
//			public void changed(ObservableValue<? extends Boolean> arg0,
//					Boolean arg1, Boolean arg2) {
//				spv.setRowHeader(arg2);
//			}
//		});
//
//		final CheckBox columnHeader = new CheckBox("Column Header");
//		columnHeader.setSelected(true);
//		columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
//
//			@Override
//			public void changed(
//					ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
//				spv.setColumnHeader(arg2);
//			}
//		});
//		grid.add(new Label("Freeze Rows:"), 1, 1);
//		grid.add(fixedRows, 1, 2);
//		grid.add(new Label("Freeze Columns:"), 1, 3);
//		grid.add(fixedColumns, 1, 4);
//		grid.add(rowHeader, 1, 5);
//		grid.add(columnHeader, 1, 6);
//
//		return grid;
//	}

	@Override
	public String getJavaDocURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean includeInSamples() {
		return true;
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("SpreadsheetView");

		final Scene scene = new Scene((Parent) getPanel(primaryStage), 600, 400);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Generate a blank grid, with only TextField and no data.
	 * @param grid
	 */
	private void blankGrid(Grid grid) {
		ArrayList<DataRow> rows = new ArrayList<DataRow>(grid.getRowCount());
		for (int row = 0; row < grid.getRowCount(); ++row) {
			final DataRow dataRow = new DataRow(row, grid.getColumnCount());
			for (int column = 0; column < grid.getColumnCount(); ++column) {
				dataRow.add(SpreadsheetCells.createTextCell(row, column, 1, 1,""));
			}
			rows.add(dataRow);
		}
		grid.setRows(rows);
	}
//
//	/**
//	 * Randomly generate a dataCell(list or text)
//	 *
//	 * @param row
//	 * @param column
//	 * @param rowSpan
//	 * @param colSpan
//	 * @return
//	 */
//	private DataCell<?> generateCell(int row, int column, int rowSpan, int colSpan) {
//		DataCell<?> cell;
//		final double random = Math.random();
//		if (random < 0.3) {
//			List<String> stringList = Arrays.asList("Banana","Apple","Mango","Cherry","Watermelon");
//			cell = SpreadsheetCells.createListCell(row, column, rowSpan, colSpan, stringList);
//		} else if (random >= 0.3 && random < 0.8) {
//			cell = SpreadsheetCells.createTextCell(row, column, rowSpan, colSpan,Integer.toString((int)(Math.random()*100)));
//		}else{
//			cell = SpreadsheetCells.createDateCell(row, column, rowSpan, colSpan, LocalDate.now().plusDays((int)(Math.random()*10)));
//		}
//
//		// Styling for preview
//		if(row%5 ==0){
//			cell.setStyleCss("five_rows");
//		}
//		if(row == 0) {
//			cell.setStyleCss("row_header");
//		}
//		if(column == 0 && rowSpan == 1){
//			cell.setStyleCss("col_header");
//		}
//		return cell;
//	}
//	
//	/**
//	 * Build a sample RowSpan grid
//	 * @param grid
//	 */
//	private void buildRowGrid(Grid grid) {
//		for (int row = 0; row < grid.getRowCount(); ++row) {
//			for (int column = 0; column < grid.getColumnCount(); ++column) {
//				if (row % 3 == 0 && column % 2 == 0) {
//					grid.spanRow(2, row, column);
//				} else if ((row - 1) % 3 == 0 && column % 2 == 0) {
//				} else {
//					grid.getRows().get(row).set(column, generateCell(row, column, 1, 1));
//				}
//			}
//		}
//	}
//	
//	/**
//	 * Build a sample RowSpan and ColSpan grid
//	 * @param grid
//	 */
//	private void buildBothGrid(Grid grid) {
//		grid.spanRow(2, 2, 2);
//		grid.spanCol(2, 2, 2);
//
//		grid.spanRow(4, 2, 4);
//
//		grid.spanCol(5, 8, 2);
//
//		grid.spanRow(15, 3, 8);
//
//		grid.spanRow(3, 5, 5);
//		grid.spanCol(3, 5, 5);
//
//		grid.spanRow(2, 10, 4);
//		grid.spanCol(3, 10, 4);
//
//		grid.spanRow(2, 12, 3);
//		grid.spanCol(3, 22, 3);
//
//		grid.spanRow(1, 27, 4);
//
//		grid.spanCol(4, 30, 3);
//		grid.spanRow(4, 30, 3);
//	}
//
//	/**
//	 * Build a sample ColumSpan grid
//	 * @param grid
//	 */
//	private void buildColumnGrid(Grid grid) {
//		for (int column = 0; column < grid.getColumnCount(); ++column) {
//			for (int row = 0; row < grid.getRowCount(); ++row) {
//				if (column % 3 == 0 && row % 2 == 0) {
//					grid.spanCol(2, row, column);
//				} else if ((column - 1) % 3 == 0 && row % 2 == 0) {
//				} else {
//					grid.getRows().get(row).set(column, generateCell(row, column, 1, 1));
//				}
//			}
//		}
//	}


}
