/**
 * Copyright (c) 2014, ControlsFX
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

import impl.org.controlsfx.spreadsheet.GridViewSkin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.util.Callback;

import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;

import com.sun.javafx.event.EventHandlerManager;

/**
 * A base implementation of the {@link Grid} interface.
 * 
 * You can specify some row height in the constructor for some of your rows.
 * Just give a Map of Integer (the number of the concerned row) and Double (the
 * height, default is 24.0).
 * 
 * <h3>Cell values</h3>
 * <p>
 * If you want to change the value of a cell, you have to go through the API
 * with {@link #setCellValue(int, int, Object)}. This method will verify that
 * the value is corresponding to the {@link SpreadsheetCellType} of the cell and
 * try to convert it if possible. It will also fire a {@link GridChange} event
 * in order to notify all listeners that a value has changed. <br>
 * <p>
 * If you want to listen to those changes, you can use the
 * {@link #addEventHandler(EventType, EventHandler)} and
 * {@link #removeEventHandler(EventType, EventHandler)} methods. <br>
 * A basic listener for implementing a undo/redo in the SpreadsheetView could be
 * like that:
 * 
 * <pre>
 * Grid grid = ...;
 * Stack&lt;GridChange&gt; undoStack = ...;
 * grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, new EventHandler&lt;GridChange&gt;() {
 *         
 *         public void handle(GridChange change) {
 *                 undoStack.push(change);
 *             }
 *         });
 * 
 * </pre>
 * 
 * 
 * @see Grid
 * @see GridChange
 */
public class GridBase implements Grid, EventTarget {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    private ObservableList<ObservableList<SpreadsheetCell>> rows;

    private int rowCount;
    private int columnCount;
    private Callback<Integer, Double> rowHeightFactory;
    private final BooleanProperty locked;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private final ObservableList<String> rowsHeader;
    private final ObservableList<String> columnsHeader;

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
     * 
     * @param rowCount
     * @param columnCount
     */
    public GridBase(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        rowsHeader = FXCollections.observableArrayList();
        columnsHeader = FXCollections.observableArrayList();
        locked = new SimpleBooleanProperty(false);
        rowHeightFactory = new MapBasedRowHeightFactory(new HashMap<Integer,Double>());
    }

    /***************************************************************************
     * 
     * Public Methods (Inherited from Grid)
     * 
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    public ObservableList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    /** {@inheritDoc} */
    @Override
    public void setCellValue(int row, int column, Object value) {
        if (row < rowCount && column < columnCount && !isLocked()) {
            SpreadsheetCell cell = getRows().get(row).get(column);
            Object item = cell.getItem();
            Object convertedValue = cell.getCellType().convertValue(value);
            cell.setItem(convertedValue);
            if (item != value && (item == null || !item.equals(cell.getItem()))) {
                GridChange cellChange = new GridChange(row, column, item, convertedValue);
                Event.fireEvent(this, cellChange);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return rowCount;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return columnCount;
    }

    /** {@inheritDoc} */
    @Override
    public SpanType getSpanType(final SpreadsheetView spv, final int row, final int column) {
        if (row < 0 || column < 0 /* || !containsRow(spv, row) */) {
            return SpanType.NORMAL_CELL;
        }
        final SpreadsheetCell cellSpan = ((ObservableList<SpreadsheetCell>) getRows().get(row)).get(column);

        final int cellSpanColumn = cellSpan.getColumn();
        final int cellSpanRow = cellSpan.getRow();
        final int cellSpanRowSpan = cellSpan.getRowSpan();
        final int cellSpanColumnSpan = cellSpan.getColumnSpan();
        final boolean containsRowMinusOne = spv.getCellsViewSkin().containsRow(row - 1);

        if (cellSpanColumn == column && cellSpanRow == row && cellSpanRowSpan == 1) {
            return SpanType.NORMAL_CELL;
        } else if (containsRowMinusOne && cellSpanColumnSpan > 1 && cellSpanColumn != column && cellSpanRowSpan > 1
                && cellSpanRow != row) {
            return SpanType.BOTH_INVISIBLE;
        } else if (cellSpanRowSpan > 1 && cellSpanColumn == column) {
            if ((cellSpanRow == row || !containsRowMinusOne)) {
                return SpanType.ROW_VISIBLE;
            } else {
                return SpanType.ROW_SPAN_INVISIBLE;
            }
        } else if (cellSpanColumnSpan > 1 && cellSpanColumn != column && (cellSpanRow == row || !containsRowMinusOne)) {
            return SpanType.COLUMN_SPAN_INVISIBLE;
        } else {
            return SpanType.NORMAL_CELL;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getRowHeight(int row) {
        return rowHeightFactory.call((Integer) row);
    }

    /***************************************************************************
     * 
     * Public Methods
     * 
     **************************************************************************/

    /**
     * Set a new {@link Callback} for this grid in order to specify height of
     * each row.
     * 
     * @param rowHeight
     */
    public void setRowHeightCallback(Callback<Integer, Double> rowHeight) {
        this.rowHeightFactory = rowHeight;
    }

    /**
     * Returns an ObservableList of string to display in the row headers.
     * 
     * @return an ObservableList of string to display in the row headers.
     */
    public ObservableList<String> getRowHeaders() {
        return rowsHeader;
    }

    /**
     * Returns an ObservableList of string to display in the column headers.
     * 
     * @return an ObservableList of string to display in the column headers.
     */
    public ObservableList<String> getColumnHeaders() {
        return columnsHeader;
    }

    /**
     * Return a BooleanProperty associated with the locked grid state. It means
     * that the Grid is in a read-only mode and that no SpreadsheetCell can be
     * modified, no regards for their own
     * {@link SpreadsheetCell#editableProperty()}.
     * 
     * @return a BooleanProperty associated with the locked grid state.
     */
    public BooleanProperty lockedProperty() {
        return locked;
    }

    /**
     * Return whether this Grid id locked or not.
     * 
     * @return whether this Grid id locked or not.
     */
    public boolean isLocked() {
        return locked.get();
    }

    /**
     * Lock or unlock this Grid.
     * 
     * @param lock
     */
    public void setLocked(Boolean lock) {
        locked.setValue(lock);
    }

    /**
     * Span in row the cell situated at rowIndex and colIndex by the number
     * count
     * 
     * @param count
     * @param rowIndex
     * @param colIndex
     */
    public void spanRow(int count, int rowIndex, int colIndex) {
        final SpreadsheetCell cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = cell.getColumnSpan();
        final int rowSpan = count;
        cell.setRowSpan(rowSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
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
        final SpreadsheetCell cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = count;
        final int rowSpan = cell.getRowSpan();
        cell.setColumnSpan(colSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, cell);
                }
            }
        }
    }

    /**
     * This method sets the rows used by the grid, and updates the rowCount.
     */
    public void setRows(Collection<ObservableList<SpreadsheetCell>> rows) {
        if (rows instanceof ObservableList) {
            this.rows = (ObservableList<ObservableList<SpreadsheetCell>>) rows;
        } else {
            this.rows = FXCollections.observableArrayList(rows);
        }

        setRowCount(rows.size());
        setColumnCount(rowCount == 0 ? 0 : this.rows.get(0).size());
    }

    /** {@inheritDoc} */
    @Override
    public <E extends GridChange> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /** {@inheritDoc} */
    @Override
    public <E extends GridChange> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    /** {@inheritDoc} */
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(eventHandlerManager);
    }

    /***************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/

    /**
     * Set a new rowCount for the grid.
     * 
     * @param rowCount
     */
    private void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Set a new columnCount for the grid.
     * 
     * @param columnCount
     */
    private void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    /**
     * This class serves as a bridge between row height Callback needed by the
     * GridBase and a Map<Integer,Double> that one could have (each Integer
     * specify a row index and its associated height).
     */
    public static class MapBasedRowHeightFactory implements Callback<Integer, Double> {
        private Map<Integer, Double> rowHeightMap;

        public MapBasedRowHeightFactory(Map<Integer, Double> rowHeightMap) {
            this.rowHeightMap = rowHeightMap;
        }

        @Override
        public Double call(Integer index) {
            Double value = rowHeightMap.get(index);
            return value == null ? GridViewSkin.DEFAULT_CELL_HEIGHT : value;
        }

    }
}
