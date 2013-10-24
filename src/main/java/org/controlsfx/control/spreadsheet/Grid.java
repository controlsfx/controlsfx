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

import javafx.collections.ObservableList;

import org.controlsfx.control.spreadsheet.SpreadsheetView.SpanType;

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
 * <h3> Code Sample </h3>
 * <pre>
 * Grid grid = ...
 * for (int row = 0; row < grid.getRowCount(); row++) {
 *     for (int column = 0; column < grid.getColumnCount(); column++) {
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
     * Return how many rows are inside the grid.
     */
    public int getRowCount();
    
    /**
     * Return how many columns are inside the grid.
     */
    public int getColumnCount();
    
    /**
     * Returns an ObservableList of ObservableList of {@link SpreadsheetCell}
     * instances. Refer to the {@link Grid} class javadoc for more detail.
     */
    public ObservableList<ObservableList<SpreadsheetCell>> getRows();

    /**
     * Return the {@link SpanType} for a given cell row/column intersection.
     */
    public SpanType getSpanType(final SpreadsheetView spv, final int row, final int column);
    
    /**
     * Return the height of a row.
     * @param row
     * @return
     */
    public double getRowHeight(int row);
}