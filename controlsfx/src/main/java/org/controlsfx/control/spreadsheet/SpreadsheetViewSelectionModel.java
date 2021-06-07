/**
 * Copyright (c) 2015, 2018 ControlsFX
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

import impl.org.controlsfx.spreadsheet.GridViewBehavior;
import impl.org.controlsfx.spreadsheet.TableViewSpanSelectionModel;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.util.Pair;

/**
 *
 * This class provides basic support for common interaction on the
 * {@link SpreadsheetView}.
 *
 * Due to the complexity induced by cell's span, it is not possible to give a
 * full access to selectionModel like in the {@link TableView}.
 */
public class SpreadsheetViewSelectionModel {

    private final TableViewSpanSelectionModel selectionModel;
    private final SpreadsheetView spv;

    //Package protected access only.
    SpreadsheetViewSelectionModel(SpreadsheetView spv, TableViewSpanSelectionModel selectionModel) {
        this.spv = spv;
        this.selectionModel = selectionModel;
    }

    /**
     * Clears all selection, and then selects the cell at the given row/column
     * intersection in the {@code SpreadsheetView}. This method does not
     * consider sorting and filtering. If you want to select a
     * {@link SpreadsheetCell} in a filtered/sorted grid, use {@link #clearAndSelect(org.controlsfx.control.spreadsheet.SpreadsheetCell)
     * } instead.
     *
     * @param row the row index to select
     * @param column the column to select
     */
    public final void clearAndSelect(int row, SpreadsheetColumn column) {
        selectionModel.clearAndSelect(row, column.column);
    }

    /**
     * Clears all selection, and then selects the given {@link SpreadsheetCell}.
     * This method allow to select a cell no matter if the Grid is Filtered or
     * sorted. Beware, this method can be time-consuming if lots of rows are
     * present.
     *
     * @param cell the cell to select
     */
    public final void clearAndSelect(SpreadsheetCell cell) {
        if (spv.isRowHidden(cell.getRow()) || spv.isColumnHidden(cell.getColumn())) {
            return;
        }
        int row = spv.getViewRow(cell.getRow());
        if (row != -1) {
            selectionModel.clearAndSelect(row, spv.getColumns().get(cell.getColumn()).column);
        }
    }
    
    /**
     * Private method for inner selection when we already have the ViewRow.
     *
     * @param row
     * @param column
     */
    private void clearAndSelectView(int row, SpreadsheetColumn column) {
        selectionModel.clearAndSelect(row, column.column);
    }

    /**
     * Selects the cell at the given row/column intersection.
     *
     * @param row the row index to select
     * @param column the column to select
     */
    public final void select(int row, SpreadsheetColumn column) {
        selectionModel.select(spv.getFilteredRow(row), column.column);
    }

    /**
     * Clears the selection model of all selected indices.
     */
    public final void clearSelection() {
        selectionModel.clearSelection();
    }

    /**
     * Returns a read-only {@code ObservableList} representing the currently selected cells in
     * this {@code SpreadsheetView}.
     *
     * @return a read-only ObservableList
     */
    public final ObservableList<TablePosition> getSelectedCells() {
        return selectionModel.getSelectedCells();
    }

    /**
     * Selects all the possible cells.
     */
    public final void selectAll() {
        selectionModel.selectAll();
    }

    /**
     * Returns the position of the cell that has current focus.
     *
     * @return the position of the cell that has current focus
     */
    public final TablePosition getFocusedCell() {
        return selectionModel.getTableView().getFocusModel().getFocusedCell();
    }

    /**
     * Causes the cell at the given index to receive the focus.
     *
     * @param row the row index of the item to give focus to
     * @param column the column of the item to give focus to. Can be null
     */
    public final void focus(int row, SpreadsheetColumn column) {
        selectionModel.getTableView().getFocusModel().focus(row, column.column);
    }

    /**
     * Specifies the selection mode to use in this selection model. The
     * selection mode specifies how many items in the underlying data model can
     * be selected at any one time. By default, the selection mode is
     * {@link SelectionMode#MULTIPLE}.
     *
     * @param value the {@code SelectionMode} to use
     */
    public final void setSelectionMode(SelectionMode value) {
        selectionModel.setSelectionMode(value);
    }

    /**
     * Returns the {@code SelectionMode} currently used.
     *
     * @return the {@code SelectionMode} currently used
     */
    public SelectionMode getSelectionMode() {
        return selectionModel.getSelectionMode();
    }

    /**
     * Selects discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, org.controlsfx.control.spreadsheet.SpreadsheetColumn) }.
     *
     * @param selectedCells the cells to select
     */
    public void selectCells(List<Pair<Integer, Integer>> selectedCells) {
        selectionModel.verifySelectedCells(selectedCells);
    }

    /**
     * Selects discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, org.controlsfx.control.spreadsheet.SpreadsheetColumn) }.
     *
     * @param selectedCells the cells to select
     */
    public void selectCells(Pair<Integer, Integer>... selectedCells) {
        selectionModel.verifySelectedCells(Arrays.asList(selectedCells));
    }

    /**
     * Selects the cells in the range (minRow, minColumn) to (maxRow,
     * maxColumn), inclusive.
     *
     * @param minRow the minimum row in the range
     * @param minColumn the minimum column in the range
     * @param maxRow the maximum row in the range
     * @param maxColumn the maximum column in the range
     */
    public void selectRange(int minRow, SpreadsheetColumn minColumn, int maxRow, SpreadsheetColumn maxColumn) {
        selectionModel.selectRange(spv.getFilteredRow(minRow), minColumn.column, spv.getFilteredRow(maxRow), maxColumn.column);
    }

    /**
     * Clears the current selection and select the cell on the left of the
     * current focused cell. If the cell is the first one on a row, the last
     * cell of the preceding row is selected.
     */
    public void clearAndSelectLeftCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int row = position.getRow();
        int column = position.getColumn();
        column -= 1;
        if (column < 0) {
            if (row == 0) {
                column++;
            } else {
                column = selectionModel.getTableView().getVisibleLeafColumns().size() - 1;
                row--;
                selectionModel.direction = new Pair<>(-1, -1);
            }
            clearAndSelectView(row, spv.getColumns().get(spv.getModelColumn(column)));
        } else {
            ((GridViewBehavior) spv.getCellsViewSkin().getBehavior()).selectCell(0, -1);
        }
    }

    /**
     * Clears the current selection and select the cell on the right of the
     * current focused cell. If the cell is the last one on a row, the first
     * cell of the next row is selected.
     */
    public void clearAndSelectRightCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int row = position.getRow();
        int column = position.getColumn();
        column += 1;
        if (column >= selectionModel.getTableView().getVisibleLeafColumns().size()) {
            if (row == spv.getGrid().getRowCount() - 1) {
                column--;
            } else {
                selectionModel.direction = new Pair<>(1, 1);
                column = 0;
                row++;
            }
            clearAndSelectView(row, spv.getColumns().get(spv.getModelColumn(column)));
        } else {
            ((GridViewBehavior) spv.getCellsViewSkin().getBehavior()).selectCell(0, 1);
        }
    }
}
