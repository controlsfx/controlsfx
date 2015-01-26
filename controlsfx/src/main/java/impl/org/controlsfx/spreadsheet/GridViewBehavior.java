/**
 * Copyright (c) 2015 ControlsFX All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of ControlsFX, any associated
 * website, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

/**
 *
 * This overrides {@link TableViewBehavior} in order to modify the selection
 * with Shift. The current selection will select just like Excel, by extending
 * the selection with the entire row or column.
 *
 */
public class GridViewBehavior extends TableViewBehavior<ObservableList<SpreadsheetCell>> {

    public GridViewBehavior(TableView<ObservableList<SpreadsheetCell>> control) {
        super(control);
    }

    @Override
    protected void updateCellVerticalSelection(int delta, Runnable defaultAction) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

        TableFocusModel fm = getFocusModel();
        if (fm == null) {
            return;
        }

        final TablePositionBase focusedCell = getFocusedCell();

        if (isShiftDown && getAnchor() != null) {

            final SpreadsheetCell cell = getControl().getItems().get(fm.getFocusedIndex()).get(focusedCell.getColumn());
            final int rowCell = cell.getRow() + cell.getRowSpan() - 1;

            int newRow = rowCell + delta;
            // we don't let the newRow go outside the bounds of the data
            newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

            final TablePositionBase<?> anchor = getAnchor();
            int minRow = Math.min(anchor.getRow(), newRow);
            int maxRow = Math.max(anchor.getRow(), newRow);
            int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
            int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());

            sm.clearSelection();
            if (minColumn != -1 && maxColumn != -1) {
                sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow,
                        getControl().getColumns().get(maxColumn));
            }
            fm.focus(newRow, focusedCell.getTableColumn());
        } else {
            final int focusIndex = fm.getFocusedIndex();
            if (!sm.isSelected(focusIndex, focusedCell.getTableColumn())) {
                sm.select(focusIndex, focusedCell.getTableColumn());
            }
            defaultAction.run();
        }
    }

    @Override
    protected void updateCellHorizontalSelection(int delta, Runnable defaultAction) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

        TableFocusModel fm = getFocusModel();
        if (fm == null) {
            return;
        }

        final TablePositionBase focusedCell = getFocusedCell();
        if (focusedCell == null || focusedCell.getTableColumn() == null) {
            return;
        }

        TableColumnBase adjacentColumn = getColumn(focusedCell.getTableColumn(), delta);
        if (adjacentColumn == null) {
            return;
        }

        final int focusedCellRow = focusedCell.getRow();

        if (isShiftDown && getAnchor() != null) {
            final int columnPos = getVisibleLeafIndex(focusedCell.getTableColumn());

            final SpreadsheetCell cell = getControl().getItems().get(focusedCellRow).get(columnPos);
            final int columnCell = cell.getColumn() + cell.getColumnSpan() - 1;

            final int newColumn = columnCell + delta;

            final TablePositionBase<?> anchor = getAnchor();
            int minRow = Math.min(anchor.getRow(), focusedCellRow);
            int maxRow = Math.max(anchor.getRow(), focusedCellRow);
            int minColumn = Math.min(anchor.getColumn(), newColumn);
            int maxColumn = Math.max(anchor.getColumn(), newColumn);

            sm.clearSelection();
            if (minColumn != -1 && maxColumn != -1) {
                sm.selectRange(minRow, getControl().getColumns().get(minColumn), maxRow,
                        getControl().getColumns().get(maxColumn));
            }
            fm.focus(focusedCell.getRow(), getColumn(newColumn));
        } else {
            defaultAction.run();
        }
    }
}
