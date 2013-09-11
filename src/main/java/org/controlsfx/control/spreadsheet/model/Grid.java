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
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.control.SpreadsheetView;

/**
 * 
 * That class holds some {@link DataRow} filled with {@link DataCell} in order
 * to be used by the {@link SpreadsheetView}
 */
public class Grid {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private ObservableList<DataRow> rows;
    private int rowCount;
    private int columnCount;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Creates grid with 'unlimited' rows and columns
     */
    public Grid() {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public Grid(int rowCount, int columnCount) {
        this(rowCount, columnCount,FXCollections.<DataRow> emptyObservableList());
    }

    public Grid(int rowCount, int columnCount, ObservableList<DataRow> rows) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.rows = rows;
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * Span in row the cell situated at rowIndex and colIndex by the number
     * count
     * 
     * @param count
     * @param rowIndex
     * @param colIndex
     */
    public void spanRow(int count, int rowIndex, int colIndex) {
        final int colSpan = rows.get(rowIndex).get(colIndex).getColumnSpan();
        final int rowSpan = count;
        rows.get(rowIndex).get(colIndex).setRowSpan(rowSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
                }
            }
        }
    }

    /**
     * Span in column the cell situated at rowIndex and colIndex by the number
     * count
     * 
     * @param count
     * @param rowIndex
     * @param colIndex
     */
    public void spanCol(int count, int rowIndex, int colIndex) {
        final int colSpan = count;
        final int rowSpan = rows.get(rowIndex).get(colIndex).getRowSpan();
        rows.get(rowIndex).get(colIndex).setColumnSpan(colSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
                }
            }
        }
    }

    /**
     * Set the rows used by the Grid.
     * the rowCount is then updated
     * @param rows
     */
    public void setRows(ObservableList<DataRow> rows) {
        this.rows = rows;
        setRowCount(rows.size());
    }
    
    /**
     * Set the rows used by the Grid.
     * the rowCount is then updated
     * @param rows
     */
    public void setRows(ArrayList<DataRow> rows) {
        this.rows = FXCollections.observableArrayList(rows);
        setRowCount(rows.size());
    }
    /**
     * Return a list of the {@link DataRow} used by the Grid.
     * @return
     */
    public ObservableList<DataRow> getRows() {
        return rows;
    }
    
    /**
     * Set a new rowCount for the grid.
     * @param rowCount
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Return how many rows are inside the grid.
     * @return
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Set a new columnCount for the grid.
     * @param columnCount
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * Return how many columns are inside the grid.
     * @return
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Debug function to print all the cells inside the grid.
     * 
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
}