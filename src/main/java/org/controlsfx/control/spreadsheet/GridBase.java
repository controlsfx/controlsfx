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
package org.controlsfx.control.spreadsheet;

import impl.org.controlsfx.skin.SpreadsheetViewSkin;

import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;

/**
 * A base implementation of the {@link Grid} interface.
 * 
 * @see Grid
 */
public class GridBase implements Grid {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    private ObservableList<ObservableList<SpreadsheetCell<?>>> rows;
    private int rowCount;
    private int columnCount;
    
    

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    /**
     * Creates grid with 'unlimited' rows and columns
     */
    public GridBase() {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Creates a grid with a fixed number of rows and columns.
     */
    public GridBase(int rowCount, int columnCount) {
        this(rowCount, columnCount,FXCollections.<ObservableList<SpreadsheetCell<?>>> emptyObservableList());
    }

    public GridBase(int rowCount, int columnCount, ObservableList<ObservableList<SpreadsheetCell<?>>> rows) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.rows = rows;
    }

    
    
    /***************************************************************************
     * 
     * Public Methods (Inherited from Grid) 
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override public ObservableList<ObservableList<SpreadsheetCell<?>>> getRows() {
        return rows;
    }
    
    /** {@inheritDoc} */
    @Override public int getRowCount() {
        return rowCount;
    }
    
    /** {@inheritDoc} */
    @Override public int getColumnCount() {
        return columnCount;
    }
    
    /** {@inheritDoc} */
    @Override public SpanType getSpanType(final SpreadsheetView spv, final int row, final int column) {
        if (row < 0 || column < 0 || !containsRow(spv, row)) {
            return SpanType.NORMAL_CELL;
        }
        final SpreadsheetCell<?> cellSpan = ((ObservableList<SpreadsheetCell<?>>)getRows().get(row)).get(column);
        
        final int cellSpanColumn = cellSpan.getColumn();
        final int cellSpanRow = cellSpan.getRow();
        final int cellSpanRowSpan = cellSpan.getRowSpan();
        final int cellSpanColumnSpan = cellSpan.getColumnSpan();
        final boolean containsRowMinusOne = containsRow(spv, row-1);
        
        if (cellSpanColumn == column
                && cellSpanRow == row
                && cellSpanRowSpan == 1) {
            return SpanType.NORMAL_CELL;
        } else if (containsRowMinusOne
                && cellSpanColumnSpan > 1
                && cellSpanColumn != column
                && cellSpanRowSpan > 1
                && cellSpanRow != row) {
            return SpanType.BOTH_INVISIBLE;
        } else if (cellSpanRowSpan > 1
                && cellSpanColumn == column) {
            if (cellSpanRow == row || !containsRowMinusOne) {
                return SpanType.ROW_VISIBLE;
            } else {
                return SpanType.ROW_SPAN_INVISIBLE;
            }
        } else if (cellSpanColumnSpan > 1
                && cellSpanColumn != column
                && (cellSpanRow == row || !containsRowMinusOne)) {
            return SpanType.COLUMN_SPAN_INVISIBLE;
        } else {
            return SpanType.NORMAL_CELL;
        }
    }
    
    
    
    /***************************************************************************
     * 
     * Public Methods
     * 
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
        final SpreadsheetCell<?> cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = cell.getColumnSpan();
        final int rowSpan = count;
        cell.setRowSpan(rowSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, cell);
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
    public void spanColumn(int count, int rowIndex, int colIndex) {
        final SpreadsheetCell<?> cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = count;
        final int rowSpan = cell.getRowSpan();
        cell.setColumnSpan(colSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, cell);
                }
            }
        }
    }

    /**
     * This method sets the rows used by the grid, and updates the rowCount.
     */
    public void setRows(Collection<ObservableList<SpreadsheetCell<?>>> rows) {
        if (rows instanceof ObservableList) {
            this.rows = (ObservableList<ObservableList<SpreadsheetCell<?>>>) rows;
        } else {
            this.rows = FXCollections.observableArrayList(rows);
        }
        setRowCount(rows.size());
    }
    
    
    
    /***************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/
    
    /**
     * Set a new rowCount for the grid.
     * @param rowCount
     */
    private void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Set a new columnCount for the grid.
     * @param columnCount
     */
    private void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }
    
    /**
     * Indicate whether or not the row at the specified index is currently 
     * being displayed.
     * @param index
     * @return
     */
    private final boolean containsRow(final SpreadsheetView spv, int index){
        SpreadsheetViewSkin skin = spv.getSpreadsheetSkin();
        int size = skin.getCellsSize();
        for (int i = 0 ; i < size; ++i) {
            if(skin.getCell(i).getIndex() == index)
                return true;
        }
        return false;
    }
}
