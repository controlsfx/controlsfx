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
package impl.org.controlsfx.tableview2;

import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.util.Pair;
import org.controlsfx.control.tableview2.TableView2;

/**
 *
 * This class provides basic support for common interaction on the
 * {@link TableView2}.
 *
 * Due to the complexity induced by cell's span, it is not possible to give a
 * full access to selectionModel like in the {@link TableView}.
 * 
 * NOT IN USE
 */
public class TableView2SelectionModel {

    private final TableViewSpanSelectionModel selectionModel;
    private final TableView2<?> tableView;

    TableView2SelectionModel(TableView2<?> tableView, TableViewSpanSelectionModel selectionModel) {
        this.tableView = tableView;
        this.selectionModel = selectionModel;
    }

    /**
     * Clears all selection, and then selects the cell at the given row/column
     * intersection.
     *
     * @param row the number of row
     * @param column the {@link TableColumn}
     */
    public final void clearAndSelect(int row, TableColumn column) {
        selectionModel.clearAndSelect(row, column);
    }

    /**
     * Private method for inner selection when we already have the ViewRow.
     *
     * @param row
     * @param column
     */
    private void clearAndSelectView(int row, TableColumn column) {
        selectionModel.clearAndSelect(row, column);
    }

    /**
     * Selects the cell at the given row/column intersection.
     *
     * @param row the number of row
     * @param column the {@link javafx.scene.control.TableColumn}
     */
    public final void select(int row, TableColumn column) {
        selectionModel.select(row, column);
    }

    /**
     * Clears the selection model of all selected indices.
     */
    public final void clearSelection() {
        selectionModel.clearSelection();
    }

    /**
     * A read-only ObservableList representing the currently selected cells in
     * this TableView2.
     *
     * @return A read-only ObservableList.
     */
    public final ObservableList<TablePosition> getSelectedCells() {
        return selectionModel.getSelectedCells();
    }

    /**
     * Select all the possible cells.
     */
    public final void selectAll() {
        selectionModel.selectAll();
    }

    /**
     * Return the position of the cell that has current focus.
     *
     * @return the position of the cell that has current focus.
     */
    public final TablePosition getFocusedCell() {
        return selectionModel.getTableView().getFocusModel().getFocusedCell();
    }

    /**
     * Causes the cell at the given index to receive the focus.
     *
     * @param row The row index of the item to give focus to.
     * @param column The column of the item to give focus to. Can be null.
     */
    public final void focus(int row, TableColumn column) {
        selectionModel.getTableView().getFocusModel().focus(row, column);
    }

    /**
     * Specifies the selection mode to use in this selection model. The
     * selection mode specifies how many items in the underlying data model can
     * be selected at any one time. By default, the selection mode is
     * {@link SelectionMode#MULTIPLE}.
     *
     * @param value A valid {@link SelectionMode}
     */
    public final void setSelectionMode(SelectionMode value) {
        selectionModel.setSelectionMode(value);
    }

    /**
     * Return the selectionMode currently used.
     *
     * @return the selectionMode currently used.
     */
    public SelectionMode getSelectionMode() {
        return selectionModel.getSelectionMode();
    }

    /**
     * Use this method to select discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, javafx.scene.control.TableColumn) }.
     *
     * @param selectedCells  a list of {@link Pair} with contain the row indices
     */
    public void selectCells(List<Pair<Integer, Integer>> selectedCells) {
        selectionModel.verifySelectedCells(selectedCells);
    }

    /**
     * Use this method to select discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, javafx.scene.control.TableColumn)  }.
     *
     * @param selectedCells a list of {@link Pair} with contain the row indices
     */
    public void selectCells(Pair<Integer, Integer>... selectedCells) {
        selectionModel.verifySelectedCells(Arrays.asList(selectedCells));
    }

    /**
     * Selects the cells in the range (minRow, minColumn) to (maxRow,
     * maxColumn), inclusive.
     *
     * @param minRow minimum row number
     * @param minColumn minimum column number
     * @param maxRow maximum row number
     * @param maxColumn maximum column number
     */
    public void selectRange(int minRow, TableColumn minColumn, int maxRow, TableColumn maxColumn) {
        selectionModel.selectRange(minRow, minColumn, maxRow, maxColumn);
    }

    /**
     * Clear the current selection and select the cell on the left of the
     * current focused cell. If the cell is the first one on a row, the last
     * cell of the preceding row is selected.
     */
    public void clearAndSelectLeftCell() {
        TablePosition<?, ?> position = getFocusedCell();
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
            int modelColumn = tableView.getColumns().indexOf(tableView.getVisibleLeafColumn(column));
            clearAndSelectView(row, tableView.getColumns().get(modelColumn));
        } else {
//            ((TableView2Behavior) ((TableView2Skin) tableView.getSkin()).getBehavior()).selectCell(0, -1);
        }
    }

    /**
     * Clear the current selection and select the cell on the right of the
     * current focused cell. If the cell is the last one on a row, the first
     * cell of the next row is selected.
     */
    public void clearAndSelectRightCell() {
        TablePosition<?, ?> position = getFocusedCell();
        int row = position.getRow();
        int column = position.getColumn();
        column += 1;
        if (column >= selectionModel.getTableView().getVisibleLeafColumns().size()) {
            if (row == tableView.getItems().size() - 1) {
                column--;
            } else {
                selectionModel.direction = new Pair<>(1, 1);
                column = 0;
                row++;
            }
            int modelColumn = tableView.getColumns().indexOf(tableView.getVisibleLeafColumn(column));
            clearAndSelectView(row, tableView.getColumns().get(modelColumn));
        } else {
//            ((TableView2Behavior) ((TableView2Skin) tableView.getSkin()).getBehavior()).selectCell(0, 1);
        }
    }

//    /**
//     * Clear the current selection and select the cell on the previous row.
//     */
//    public void clearAndSelectPreviousCell() {
//        TablePosition<?, ?> position = getFocusedCell();
//        int nextRow = FocusModelListener.getPreviousRowNumber(position, selectionModel.getTableView(), spv);
//        if (nextRow >= 0) {
//            clearAndSelectView(nextRow, spv.getColumns().get(spv.getModelColumn(position.getColumn())));
//        }
//    }
//
//    /**
//     * Clear the current selection and select the cell on the next row.
//     */
//    public void clearAndSelectNextCell() {
//        TablePosition<?, ?> position = getFocusedCell();
//        int nextRow = FocusModelListener.getNextRowNumber(position, selectionModel.getTableView(), spv);
//        if (nextRow < spv.getRowCount()) {
//            clearAndSelectView(nextRow, spv.getColumns().get(spv.getModelColumn(position.getColumn())));
//        }
//    }
}
