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
package org.controlsfx.control.spreadsheet;

import com.sun.javafx.event.EventHandlerManager;
import static impl.org.controlsfx.spreadsheet.RectangleSelection.SelectionRange.key;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import javafx.beans.Observable;
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

/**
 * A base implementation of the {@link Grid} interface.
 *
 * <h3>How to span</h3>
 * <p>
 * First of all, the Grid must have all its rows filled with the same number of
 * {@link SpreadsheetCell}. A span is materialized by the same cell (same
 * instance of SpreadsheetCell) repeated all over the covered part. In order to
 * materialize span, you have two ways :
 * <p>
 * - First way is to manually add the same cell where you want to span. For
 * example, we will make the first cell span on two columns :
 * <pre>
 * //I create a sample grid
 * int rowCount = 15;
 *     int columnCount = 10;
 *     GridBase grid = new GridBase(rowCount, columnCount);
 *
 *     ObservableList&lt;ObservableList&lt;SpreadsheetCell&gt;&gt; rows = FXCollections.observableArrayList();
 *     for (int row = 0; row &lt; grid.getRowCount(); ++row) {
 *         final ObservableList&lt;SpreadsheetCell&gt; list = FXCollections.observableArrayList();
 *         for (int column = 0; column &lt; grid.getColumnCount(); ++column) {
 *             list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,"value"));
 *         }
 *         rows.add(list);
 *     }
 *      //I create my SpreadsheetCell spanning on two columns.
 *      SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(0, 0, 1, 2,"value");
 *      //I add them in the area covered by the span.
 *      rows.get(0).set(0, cell);
 *      rows.get(0).set(1, cell);
 *      grid.setRows(rows);
 *
 *     SpreadsheetView spv = new SpreadsheetView(grid);
 * </pre> 
 * 
 * - The second way is to build the SpreadsheetView, but to use the {@link #spanRow(int, int, int)
 * } or {@link #spanColumn(int, int, int) } methods. These methods will take the
 * SpreadsheetCell at the specified position, and enlarge the span by modifying
 * the rowSpan or columnSpan of the cell. And also put the SpreadsheetCell in
 * the area covered by the span.
 * <pre>
 * //I create a sample grid
 * int rowCount = 15;
 *     int columnCount = 10;
 *     GridBase grid = new GridBase(rowCount, columnCount);
 *
 *     ObservableList&lt;ObservableList&lt;SpreadsheetCell&gt;&gt; rows = FXCollections.observableArrayList();
 *     for (int row = 0; row &lt; grid.getRowCount(); ++row) {
 *         final ObservableList&lt;SpreadsheetCell&gt; list = FXCollections.observableArrayList();
 *         for (int column = 0; column &lt; grid.getColumnCount(); ++column) {
 *             list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,"value"));
 *         }
 *         rows.add(list);
 *     }
 *      //I First set the rows in the grid.
 *      grid.setRows(rows);
 *      //Then I simply tell the grid to span the first cell
 *      grid.spanColumn(2,0,0);
 *
 *     SpreadsheetView spv = new SpreadsheetView(grid);
 * </pre> 
 * 
 * <h3>Row Height</h3>
 * 
 * You can specify some row height for some of your rows at the beginning.
 * You have to use the method {@link #setRowHeightCallback(javafx.util.Callback) }
 * in order to specify a Callback that will give you the index of the row, and you 
 * will give back the height of the row.
 * <br>
 * If you just have a {@link Map} available, you can use the {@link MapBasedRowHeightFactory}
 * that will construct the Callback for you.

* The default height is 24.0.
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
 * <h3>Display selection</h3>
 *
 * By default, the SpreadsheetView will display a black rectangle around your
 * selection if it's contiguous. Some may want to disable that effect. Therefore
 * a simple call to {@link #setDisplaySelection(boolean) } with a false value
 * will make that rectangle disappear.
 *
 * <h3>Headers</h3>
 * The SpreadsheetView is displaying row numbers and column letters by default.
 * Just like any other spreadsheet would do. However, some may want to customize
 * theose headers. You can use the {@link #getColumnHeaders() } and {@link #getRowHeaders()
 * } in order to customize what will appear in these headers.
 *  <br>
 * If you put some long text in the row headers, it will not fit. Thus you may
 * consider using {@link SpreadsheetView#setRowHeaderWidth(double) }
 * in order to enlarge the row header so that your text can fit properly.
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
    private BitSet resizableRow;
    private final TreeSet<Long> displaySelectionCells = new TreeSet<>();
    private final TreeSet<Long> noDisplaySelectionCells = new TreeSet<>();
    private final BooleanProperty displaySelection = new SimpleBooleanProperty(true);

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

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
        rowHeightFactory = new MapBasedRowHeightFactory(new HashMap<>());
        rows = FXCollections.observableArrayList();
        rows.addListener((Observable observable) -> {
            setRowCount(rows.size());
        });
        resizableRow = new BitSet(rowCount);
        resizableRow.set(0, rowCount, true);
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
    public void setCellValue(int modelRow, int column, Object value) {
        if (modelRow < getRowCount() && column < columnCount && !isLocked()) {
            SpreadsheetCell cell = getRows().get(modelRow).get(column);
            Object previousItem = cell.getItem();
            Object convertedValue = cell.getCellType().convertValue(value);
            cell.setItem(convertedValue);
            if (!java.util.Objects.equals(previousItem, cell.getItem())) {
                GridChange cellChange = new GridChange(cell.getRow(), cell.getColumn(), previousItem, convertedValue);
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

    /**
     * Return the height of a row. 
     * It will first look into the {@link Callback}provided at the
     * initialization. If nothing's found, {@link #AUTOFIT} will be returned.
     * @param row
     * @return the height of a row.
     */
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

    /** {@inheritDoc} */
    @Override
    public ObservableList<String> getRowHeaders() {
        return rowsHeader;
    }

    /** {@inheritDoc} */
    @Override
    public ObservableList<String> getColumnHeaders() {
        return columnsHeader;
    }

    /**
     * Return a BooleanProperty associated with the locked grid state. It means
     * that the Grid is in a read-only mode and that no SpreadsheetCell can be
     * modified, no regards for their own
     * {@link SpreadsheetCell#isEditable()} state.
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

    /** {@inheritDoc} */
    @Override
    public void spanRow(int count, int rowIndex, int colIndex) {
        if (count <= 0 || count > getRowCount() || rowIndex >= getRowCount() || colIndex >= columnCount) {
            return;
        }
        final SpreadsheetCell cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = cell.getColumnSpan();
        final int rowSpan = count;
        cell.setRowSpan(rowSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < getRowCount(); ++row) {
            for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, cell);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void spanColumn(int count, int rowIndex, int colIndex) {
        if (count <= 0 || count > columnCount || rowIndex >= getRowCount() || colIndex >= columnCount) {
            return;
        }
        final SpreadsheetCell cell = rows.get(rowIndex).get(colIndex);
        final int colSpan = count;
        final int rowSpan = cell.getRowSpan();
        cell.setColumnSpan(colSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < getRowCount(); ++row) {
            for (int col = colIndex; col < colIndex + colSpan && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, cell);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setRows(Collection<ObservableList<SpreadsheetCell>> rows) {
        this.rows.clear();
        this.rows.addAll(rows);

        setRowCount(rows.size());
        setColumnCount(rowCount == 0 ? 0 : this.rows.get(0).size());
    }

    /**
     * Sets the resizable state of all rows. If a bit is set to true in the
     * BitSet, it means the row is resizable.
     *
     * The {@link BitSet#length() } must be equal to the {@link #getRowCount() }
     *
     * @param resizableRow
     */
    public void setResizableRows(BitSet resizableRow) {
        this.resizableRow = resizableRow;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isRowResizable(int row) {
        return resizableRow.get(row);
    }
   
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDisplaySelection() {
        return displaySelection.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDisplaySelection(boolean value) {
        displaySelection.setValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanProperty displaySelectionProperty() {
        return displaySelection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCellDisplaySelection(int row, int column, boolean displaySelection) {
        Long key = key(row, column);
        if (displaySelection) {
            displaySelectionCells.add(key);
            noDisplaySelectionCells.remove(key);
        } else {
            displaySelectionCells.remove(key);
            noDisplaySelectionCells.add(key);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellDisplaySelection(int row, int column) {
        Long key = key(row, column);
        if (displaySelectionCells.contains(key)) {
            return true;
        } else if (noDisplaySelectionCells.contains(key)) {
            return false;
        }
        return isDisplaySelection();
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
     * GridBase and a Map&lt;Integer,Double&gt; that one could have (each Integer
     * specify a row index and its associated height).
     */
    public static class MapBasedRowHeightFactory implements Callback<Integer, Double> {
        private final Map<Integer, Double> rowHeightMap;

        public MapBasedRowHeightFactory(Map<Integer, Double> rowHeightMap) {
            this.rowHeightMap = rowHeightMap;
        }

        @Override
        public Double call(Integer index) {
            Double value = rowHeightMap.get(index);
            return value == null ? AUTOFIT : value;
        }

    }
}
