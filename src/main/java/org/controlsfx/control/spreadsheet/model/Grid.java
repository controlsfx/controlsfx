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

package org.controlsfx.control.spreadsheet.model;

import java.util.ArrayList;

/**
 *
 * That class generate grid filled with Rows and Cells in order to be used by
 * the SpreadSheet View.
 * It can generate grid with spanned cells upon request.
 */
public class Grid {

	public enum GridSpanType {
		NONE,
		COLUMN,
		ROW,
		BOTH;
	}
	private final ArrayList<DataRow> rows;
	private final int rowCount = 50;
	private final int columnCount = 10;

	///////////////////////////////////////////
	// CONSTRUCTORS
	///////////////////////////////////////////
	/**
	 * Empty constructor
	 */
	public Grid() {
		rows = new ArrayList<>(rowCount);
	}

	/**
	 * Create a DataCell table with some rowSpan
	 *
	 * @return
	 */
	public Grid(GridSpanType type) {
		rows = new ArrayList<>(rowCount);
		normalGrid();
		switch (type) {
		case ROW:
			buildRowGrid();
			break;
		case COLUMN:
			buildColumnGrid();
			break;
		case BOTH:
			buildBothGrid();
			break;
		case NONE:
		}

	}

	///////////////////////////////////////////
	// METHOD
	///////////////////////////////////////////
	private void normalGrid() {
		for (int row = 0; row < rowCount; ++row) {
			final DataRow dataRow = new DataRow(row, columnCount);
			for (int column = 0; column < columnCount; ++column) {
				dataRow.add(generateCell(row, column, 1, 1));
			}
			rows.add(dataRow);
		}
	}

	/**
	 * Randomly generate a dataCell(list or text)
	 *
	 * @param row
	 * @param column
	 * @param rowSpan
	 * @param colSpan
	 * @return
	 */
	private DataCell<?> generateCell(int row, int column, int rowSpan, int colSpan) {
		DataCell<?> temp;
		final double random = Math.random();
		if (random < 0.5) {
			temp = new ListCell(row, column, rowSpan, colSpan);
		} else{
			temp = new TextCell(row, column, rowSpan, colSpan);
		}/*else{
			temp = new DateCell(row, column, rowSpan, colSpan);
		}*/

		// Styling for preview
		if(row%5 ==0){
			temp.setStyleCss("five_rows");
		}
		if(row == 0) {
			temp.setStyleCss("row_header");
		}
		if(column == 0 && rowSpan == 1){
			temp.setStyleCss("col_header");
		}
		return temp;
	}

	private void spanRow(int count, int rowIndex, int colIndex) {
		final int colSpan = rows.get(rowIndex).get(colIndex).getColumnSpan();
		final int rowSpan = count;
		rows.get(rowIndex).set(colIndex, generateCell(rowIndex, colIndex, rowSpan, colSpan));
		for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
			for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
				if (row != rowIndex || col != colIndex) {
					rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
				}
			}
		}
	}

	private void spanCol(int count, int rowIndex, int colIndex) {
		final int colSpan = count;
		final int rowSpan = rows.get(rowIndex).get(colIndex).getRowSpan();
		rows.get(rowIndex).set(colIndex, generateCell(rowIndex, colIndex, rowSpan, colSpan));
		for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
			for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
				if (row != rowIndex || col != colIndex) {
					rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
				}
			}
		}
	}

	private void buildRowGrid() {
		for (int row = 0; row < rowCount; ++row) {
			for (int column = 0; column < columnCount; ++column) {
				if (row % 3 == 0 && column % 2 == 0) {
					spanRow(2, row, column);
				} else if ((row - 1) % 3 == 0 && column % 2 == 0) {
				} else {
					rows.get(row).set(column, generateCell(row, column, 1, 1));
				}
			}
		}
	}

	private void buildBothGrid() {
		spanRow(2, 2, 2);
		spanCol(2, 2, 2);

		spanRow(4, 2, 4);

		spanCol(5, 8, 2);

		spanRow(15, 3, 8);

		spanRow(3, 5, 5);
		spanCol(3, 5, 5);

		spanRow(2, 10, 4);
		spanCol(3, 10, 4);

		spanRow(2, 12, 3);
		spanCol(3, 22, 3);

		spanRow(1, 27, 4);

		spanCol(4, 30, 3);
		spanRow(4, 30, 3);
	}

	/**
	 * Create a DataCell table with some rowSpan
	 *
	 * @return
	 */
	private void buildColumnGrid() {
		for (int column = 0; column < columnCount; ++column) {
			for (int row = 0; row < rowCount; ++row) {
				if (column % 3 == 0 && row % 2 == 0) {
					spanCol(2, row, column);
				} else if ((column - 1) % 3 == 0 && row % 2 == 0) {
				} else {
					rows.get(row).set(column, generateCell(row, column, 1, 1));
				}
			}
		}
	}

	/**
	 * Debug function to print the Grid.
	 * @param grid
	 */
	public void print(DataCell<?>[][] grid) {
		for (int row = 0; row < rowCount; ++row) {
			for (int column = 0; column < columnCount; ++column) {
				System.out.print(grid[row][column].toString());
			}
			System.out.println("");
		}
	}

	public ArrayList<DataRow> getRows() {
		return rows;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getColumncount() {
		return columnCount;
	}
}