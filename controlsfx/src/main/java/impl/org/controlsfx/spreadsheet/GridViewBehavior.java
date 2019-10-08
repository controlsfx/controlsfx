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
import javafx.util.Pair;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

/**
 *
 * This overrides {@link TableViewBehavior} in order to modify the selection
 * behavior. The selection will basically work like Excel:
 *
 * Selection will always be rectangles. So selection by SHIFT will produce a
 * rectangle extending your selection.
 *
 * Pressing SHORTCUT with an arrow will on a cell will do:
 *
 * - If the cell is empty, we go to the next (in the direction) non empty cell.
 *
 * - If the cell is not empty, then we either go to the last non empty cell if
 * the next is not empty, or the first non empty cell if the next is empty.
 *
 * This is meant to increase navigation on non empty cell in a Spreadsheet more
 * easily.
 *
 * Pressing SHORTCUT and SHIFT together will behave as the ShortCut previously
 * explained but the selection will be extended instead of just selecting the
 * new cell.
 *
 */
public class GridViewBehavior extends TableViewBehavior<ObservableList<SpreadsheetCell>> {

    private GridViewSkin skin;

    public GridViewBehavior(TableView<ObservableList<SpreadsheetCell>> control) {
        super(control);
    }

    void setGridViewSkin(GridViewSkin skin) {
        this.skin = skin;
    }

    @Override
    protected void updateCellVerticalSelection(int delta, Runnable defaultAction) {
        TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel) getSelectionModel();
        if (sm == null || sm.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

        TableFocusModel fm = getFocusModel();
        if (fm == null) {
            return;
        }

        final TablePositionBase focusedCell = getFocusedCell();

        if (isShiftDown && getAnchor() != null) {

            final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());
            sm.direction = new Pair<>(delta, 0);
            /**
             * If the delta is >0, it means we want to go down, so we need to
             * target the cell that is after our cell. So we need to take the
             * last row of our cell if spanning. If the delta is < 0, it means
             * we want to go up, so we just take the row.
             */
            int newRow;
            if (delta < 0) {
                newRow = skin.getFirstRow(cell, focusedCell.getRow()) + delta;
            } else {
                newRow = focusedCell.getRow() + skin.spreadsheetView.getRowSpan(cell, focusedCell.getRow()) - 1 + delta;
            }

            // we don't let the newRow go outside the bounds of the data
            newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

            final TablePositionBase<?> anchor = getAnchor();
            int minRow = Math.min(anchor.getRow(), newRow);
            int maxRow = Math.max(anchor.getRow(), newRow);
            int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
            int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());

            sm.clearSelection();
            if (minColumn != -1 && maxColumn != -1) {
                sm.selectRange(minRow, getNode().getVisibleLeafColumn(minColumn), maxRow,
                        getNode().getVisibleLeafColumn(maxColumn));
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
        TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel) getSelectionModel();
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

            final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());
            
            sm.direction = new Pair<>(0, delta);
            final int newColumn;// = columnCell + delta;
            if (delta < 0) {
                newColumn = skin.spreadsheetView.getViewColumn(cell.getColumn()) + delta;
            } else {
                newColumn = skin.spreadsheetView.getViewColumn(cell.getColumn()) + skin.spreadsheetView.getColumnSpan(cell) - 1 + delta;
            }
            final TablePositionBase<?> anchor = getAnchor();
            int minRow = Math.min(anchor.getRow(), focusedCellRow);
            int maxRow = Math.max(anchor.getRow(), focusedCellRow);
            int minColumn = Math.min(anchor.getColumn(), newColumn);
            int maxColumn = Math.max(anchor.getColumn(), newColumn);

            sm.clearSelection();
            if (minColumn != -1 && maxColumn != -1) {
                sm.selectRange(minRow, getNode().getVisibleLeafColumn(minColumn), maxRow,
                        getNode().getVisibleLeafColumn(maxColumn));
            }
            fm.focus(focusedCell.getRow(), getColumn(newColumn));
        } else {
            defaultAction.run();
        }

    }

    @Override
    protected void focusPreviousRow() {
        focusVertical(true);
    }

    @Override
    protected void focusNextRow() {
        focusVertical(false);
    }

    @Override
    protected void focusLeftCell() {
        focusHorizontal(true);
    }

    @Override
    protected void focusRightCell() {
        focusHorizontal(false);
    }

    @Override
    protected void discontinuousSelectPreviousRow() {
        discontinuousSelectVertical(true);
    }

    @Override
    protected void discontinuousSelectNextRow() {
        discontinuousSelectVertical(false);
    }

    @Override
    protected void discontinuousSelectPreviousColumn() {
        discontinuousSelectHorizontal(true);
    }

    @Override
    protected void discontinuousSelectNextColumn() {
        discontinuousSelectHorizontal(false);
    }

    private void focusVertical(boolean previous) {
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

        final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());
        sm.clearAndSelect(previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell), focusedCell.getTableColumn());
        skin.focusScroll();
    }

    private void focusHorizontal(boolean previous) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null) {
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

        final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());

        sm.clearAndSelect(focusedCell.getRow(), getNode().getVisibleLeafColumn(previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell)));
        skin.focusScroll();
    }

    private int findPreviousRow(TablePositionBase focusedCell, SpreadsheetCell cell) {
        final ObservableList<ObservableList<SpreadsheetCell>> items = getNode().getItems();
        SpreadsheetCell temp;
        //If my cell is empty, I seek the next non-empty
        if (isEmpty(cell)) {
            for (int row = focusedCell.getRow() - 1; row >= 0; --row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (!isEmpty(temp)) {
                    return row;
                }
            }
        } else if (focusedCell.getRow() - 1 >= 0 && !isEmpty(items.get(focusedCell.getRow() - 1).get(focusedCell.getColumn()))) {
            for (int row = focusedCell.getRow() - 2; row >= 0; --row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (isEmpty(temp)) {
                    return row + 1;
                }
            }
        } else {
            //If I'm not empty and the next is empty, I seek the first non empty
            for (int row = focusedCell.getRow() - 2; row >= 0; --row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (!isEmpty(temp)) {
                    return row;
                }
            }
        }

        //If we're here, we then select the last on
        return 0;
    }

    
    @Override
    public void selectCell(int rowDiff, int columnDiff) {
        TableViewSpanSelectionModel sm = (TableViewSpanSelectionModel) getSelectionModel();
        if (sm == null) {
            return;
        }
        sm.direction = new Pair<>(rowDiff, columnDiff);

        TableFocusModel fm = getFocusModel();
        if (fm == null) {
            return;
        }

        TablePositionBase focusedCell = getFocusedCell();
        int currentRow = focusedCell.getRow();
        int currentColumn = getVisibleLeafIndex(focusedCell.getTableColumn());

        if (rowDiff < 0 && currentRow <= 0) return;
        else if (rowDiff > 0 && currentRow >= getItemCount() - 1) return;
        else if (columnDiff < 0 && currentColumn <= 0) return;
        else if (columnDiff > 0 && currentColumn >= getVisibleLeafColumns().size() - 1) return;
        else if (columnDiff > 0 && currentColumn == -1) return;

        TableColumnBase tc = focusedCell.getTableColumn();
        tc = getColumn(tc, columnDiff);

        int row = focusedCell.getRow() + rowDiff;
        
        sm.clearAndSelect(row, tc);
        setAnchor(row, tc);
    }

    private int findNextRow(TablePositionBase focusedCell, SpreadsheetCell cell) {
        final ObservableList<ObservableList<SpreadsheetCell>> items = getNode().getItems();
        final int itemCount = getItemCount();
        SpreadsheetCell temp;
        //If my cell is empty, I seek the next non-empty
        if (isEmpty(cell)) {
            for (int row = focusedCell.getRow() + 1; row < itemCount; ++row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (!isEmpty(temp)) {
                    return row;
                }
            }
        } else if (focusedCell.getRow() + 1 < itemCount && !isEmpty(items.get(focusedCell.getRow() + 1).get(focusedCell.getColumn()))) {
            for (int row = focusedCell.getRow() + 2; row < getItemCount(); ++row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (isEmpty(temp)) {
                    return row - 1;
                }
            }
        } else {
            for (int row = focusedCell.getRow() + 2; row < itemCount; ++row) {
                temp = items.get(row).get(focusedCell.getColumn());
                if (!isEmpty(temp)) {
                    return row;
                }
            }
        }
        return itemCount - 1;
    }

    private void discontinuousSelectVertical(boolean previous) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null) {
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

        final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());

        /**
         * If the delta is >0, it means we want to go down, so we need to target
         * the cell that is after our cell. So we need to take the last row of
         * our cell if spanning. If the delta is < 0, it means we want to go up,
         * so we just take the row.
         */
        int newRow = previous ? findPreviousRow(focusedCell, cell) : findNextRow(focusedCell, cell);;

        // we don't let the newRow go outside the bounds of the data
        newRow = Math.max(Math.min(getItemCount() - 1, newRow), 0);

        final TablePositionBase<?> anchor = getAnchor();
        int minRow = Math.min(anchor.getRow(), newRow);
        int maxRow = Math.max(anchor.getRow(), newRow);
        int minColumn = Math.min(anchor.getColumn(), focusedCell.getColumn());
        int maxColumn = Math.max(anchor.getColumn(), focusedCell.getColumn());

        sm.clearSelection();
        if (minColumn != -1 && maxColumn != -1) {
            sm.selectRange(minRow, getNode().getVisibleLeafColumn(minColumn), maxRow,
                    getNode().getVisibleLeafColumn(maxColumn));
        }
        fm.focus(newRow, focusedCell.getTableColumn());
        skin.focusScroll();
    }

    private void discontinuousSelectHorizontal(boolean previous) {
        TableSelectionModel sm = getSelectionModel();
        if (sm == null) {
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

        final int columnPos = getVisibleLeafIndex(focusedCell.getTableColumn());
        int focusedCellRow = focusedCell.getRow();
        final SpreadsheetCell cell = (SpreadsheetCell) focusedCell.getTableColumn().getCellData(focusedCell.getRow());

        final int newColumn = previous ? findPreviousColumn(focusedCell, cell) : findNextColumn(focusedCell, cell);

        final TablePositionBase<?> anchor = getAnchor();
        int minRow = Math.min(anchor.getRow(), focusedCellRow);
        int maxRow = Math.max(anchor.getRow(), focusedCellRow);
        int minColumn = Math.min(anchor.getColumn(), newColumn);
        int maxColumn = Math.max(anchor.getColumn(), newColumn);

        sm.clearSelection();
        if (minColumn != -1 && maxColumn != -1) {
            sm.selectRange(minRow, getNode().getVisibleLeafColumn(minColumn), maxRow,
                    getNode().getVisibleLeafColumn(maxColumn));
        }
        fm.focus(focusedCell.getRow(), getColumn(newColumn));
        skin.focusScroll();
    }

    private int findNextColumn(TablePositionBase focusedCell, SpreadsheetCell cell) {
        final ObservableList<ObservableList<SpreadsheetCell>> items = getNode().getItems();
        final int itemCount = getNode().getColumns().size();
        SpreadsheetCell temp;
        //If my cell is empty, I seek the next non-empty
        if (isEmpty(cell)) {
            for (int column = focusedCell.getColumn() + 1; column < itemCount; ++column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (!isEmpty(temp)) {
                    return column;
                }
            }
        } else if (focusedCell.getColumn() + 1 < itemCount && !isEmpty(items.get(focusedCell.getRow()).get(focusedCell.getColumn() + 1))) {
            for (int column = focusedCell.getColumn() + 2; column < itemCount; ++column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (isEmpty(temp)) {
                    return column - 1;
                }
            }
        } else {
            for (int column = focusedCell.getColumn() + 2; column < itemCount; ++column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (!isEmpty(temp)) {
                    return column;
                }
            }
        }
        return itemCount - 1;
    }

    private int findPreviousColumn(TablePositionBase focusedCell, SpreadsheetCell cell) {
        final ObservableList<ObservableList<SpreadsheetCell>> items = getNode().getItems();
        SpreadsheetCell temp;
        //If my cell is empty, I seek the next non-empty
        if (isEmpty(cell)) {
            for (int column = focusedCell.getColumn() - 1; column >= 0; --column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (!isEmpty(temp)) {
                    return column;
                }
            }
        } else if (focusedCell.getColumn() - 1 >= 0 && !isEmpty(items.get(focusedCell.getRow()).get(focusedCell.getColumn() - 1))) {
            for (int column = focusedCell.getColumn() - 2; column >= 0; --column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (isEmpty(temp)) {
                    return column + 1;
                }
            }
        } else {
            for (int column = focusedCell.getColumn() - 2; column >= 0; --column) {
                temp = items.get(focusedCell.getRow()).get(column);
                if (!isEmpty(temp)) {
                    return column;
                }
            }
        }
        return 0;
    }

    /**
     * Cell is empty if there's nothing in it or if we have a NaN instead of a
     * proper Double.
     *
     * @param cell
     * @return
     */
    private boolean isEmpty(SpreadsheetCell cell) {
        return cell.getGraphic() == null && (cell.getItem() == null
                || (cell.getItem() instanceof Double && ((Double) cell.getItem()).isNaN()));
    }
}
