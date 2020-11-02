/**
 * Copyright (c) 2013, 2018 ControlsFX
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

import java.util.Collection;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * That class holds some {@link SpreadsheetCell} in order
 * to be used by the {@link SpreadsheetView}.
 * 
 * A Grid is used by {@link SpreadsheetView} to represent the data to show on
 * screen. A default implementation is provided by {@link GridBase}, but for 
 * more custom purposes (e.g. loading data on demand), this Grid interface may
 * prove useful.
 * 
 * <p>A Grid at its essence consists of rows and columns. Critical to the 
 * SpreadsheetView is that the {@link #getRowCount() row count} and 
 * {@link #getColumnCount() column count} are accurately returned when requested
 * (even if the data returned by {@link #getRows()} is not all fully loaded into
 * memory). 
 * 
 * <p>Whilst the {@link #getRows()} return type may appear confusing, it is 
 * actually quite logical when you think about it: {@link #getRows()} returns an
 * ObservableList of ObservableList of {@link SpreadsheetCell} instances. In other
 * words, this is your classic 2D collection, where the outer ObservableList
 * can be thought of as the rows, and the inner ObservableList as the columns
 * within each row. Therefore, if you are wanting to iterate through all columns
 * in every row of the grid, you would do something like this:
 * 
 * 
 * <h3> Code Sample </h3>
 * <pre>
 * Grid grid = ...
 * for (int row = 0; row &lt; grid.getRowCount(); row++) {
 *     for (int column = 0; column &lt; grid.getColumnCount(); column++) {
 *         SpreadsheetCell&lt;?&gt; cell = getRows().get(row).get(column);
 *         doStuff(cell);
 *     }
 * }
 * 
 * </pre>
 * 
 * @see SpreadsheetView
 * @see GridBase
 * @see SpreadsheetCell
 */
public interface Grid {
    /**
     * This value may be returned from {@link #getRowHeight(int) } in order to
     * let the system compute the best row height.
     */
    public static final double AUTOFIT = -1;
   
    /**
     * Returns how many rows are inside the {@code Grid}.
     *
     * @return the number of rows in the {@code Grid}.
     */
    public int getRowCount();
    
    /**
     * Returns how many columns are inside the {@code Grid}.
     * 
     * @return the number of columns in the {@code Grid}.
     */
    public int getColumnCount();
    
    /**
     * Returns an {@code ObservableList} of {@code ObservableList} of
     * {@link SpreadsheetCell} instances. Refer to the {@link Grid} class
     * javadoc for more detail.
     *
     * @return an {@code ObservableList} of {@code ObservableList} of
     * {@link SpreadsheetCell} instances
     */
    public ObservableList<ObservableList<SpreadsheetCell>> getRows();

    /**
     * Changes the value situated at the intersection if possible. Verification
     * and conversion of the value should be done before with
     * {@link SpreadsheetCellType#match(Object)} and
     * {@link SpreadsheetCellType#convertValue(Object)}.
     *
     * @param row the row index issued from the {@code SpreadsheetCell}
     * @param column the column index issued from the {@code SpreadsheetCell}
     * @param value the value to set to the {@code SpreadsheetCell}
     */
    public void setCellValue(int row, int column, Object value);
    
    /**
     * Returns the height of a row. {@link #AUTOFIT } can be returned in order
     * to let the system compute the best row height.
     *
     * @param row the row index
     * @return the height in pixels of the given row.
     */
    public double getRowHeight(int row);

    /**
     * Returns true if the specified row is resizable.
     *
     * @param row the row index
     * @return {@code true} if the specified row is resizable
     */
    public boolean isRowResizable(int row);
    
    /**
     * Returns an {@code ObservableList} of {@code String} to display in the row
     * headers.
     *
     * @return an {@code ObservableList} of {@code String} to display in the row
     * headers
     */
    public ObservableList<String> getRowHeaders();
    
    /**
     * Returns an {@code ObservableList} of {@code String} to display in the
     * column headers.
     *
     * @return an {@code ObservableList} of {@code String} to display in the
     * column headers
     */
    public ObservableList<String> getColumnHeaders();
    
    /**
     * Spans in row the cell situated at rowIndex and colIndex by the number
     * count.
     * 
     * @param count the span range
     * @param rowIndex the row index
     * @param colIndex the column index
     */
    public void spanRow(int count, int rowIndex, int colIndex);
    
    /**
     * Spans in column the cell situated at rowIndex and colIndex by the number
     * count.
     *
     * @param count the span range
     * @param rowIndex the row index
     * @param colIndex the column index
     */
    public void spanColumn(int count, int rowIndex, int colIndex);
    
    /**
     * Sets the rows used by the grid, and updates the rowCount. This method
     * should be called before the {@code Grid} is actually given to a
     * {@code SpreadsheetView}. If this method is called after, you should give
     * the {@code Grid} again to the {@code SpreadsheetView} by using {@link SpreadsheetView#setGrid(org.controlsfx.control.spreadsheet.Grid)
     * }.
     *
     * @param rows the rows to set for this {@code Grid}
     */
    public void setRows(Collection<ObservableList<SpreadsheetCell>> rows);
    
    /**
     * Return {@code true} if the selection (black rectangle) is displayed on
     * the {@code Grid}. Cells may override this property with {@link #setCellDisplaySelection(int, int, boolean)
     * }.
     *
     * @return {@code true} if the selection (black rectangle) is displayed on
     * the Grid
     */
    public boolean isDisplaySelection();

    /**
     * If set to true, the selection (black rectangle) will be displayed on the
     * {@code Grid}. Cells may override this property with {@link #setCellDisplaySelection(int, int, boolean)
     * }.
     *
     * @param value {@code true} if the selection should be displayed
     */
    public void setDisplaySelection(boolean value);

    /**
     * Returns the Boolean property associated with the displayed selection of the
     * {@code Grid}.
     *
     * @return the Boolean property associated with the displayed selecion of the
     * {@code Grid}
     */
    public BooleanProperty displaySelectionProperty();

    /**
     * Overrides the value defined by {@link #isDisplaySelection() }
     * so that no matter what is defined on the grid, the given cell will always
     * have its selection set to the displaySelection parameter.
     *
     * @param row the row index
     * @param column the column index
     * @param displaySelection {@code true} is the selection should always be
     * displayed on this cell
     */
    public void setCellDisplaySelection(int row, int column, boolean displaySelection);

    /**
     * Returns true if the given cell will display a selection rectangle when
     * selected. If nothing is defined for this cell, 
     * {@link #isDisplaySelection() } is returned.
     *
     * @param row the row index
     * @param column the column index
     * @return {@code true} if the given cell will display a selection rectangle
     */
    public boolean isCellDisplaySelection(int row, int column);
    /**
     * Registers an event handler to this Grid. The Grid class allows 
     * registration of listeners which will be notified as a {@link SpreadsheetCell}'s value 
     * will change.
     *
     * @param <E>
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends GridChange> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler);
    
    /**
     * Unregisters a previously registered event handler from this Grid. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param <E>
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends GridChange> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler);
}