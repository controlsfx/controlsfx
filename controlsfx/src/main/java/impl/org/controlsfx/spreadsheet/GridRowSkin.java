/**
 * Copyright (c) 2013, 2014 ControlsFX
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
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.skin.TableRowSkin;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class GridRowSkin extends TableRowSkin<ObservableList<SpreadsheetCell>> {
    
    private final SpreadsheetHandle handle;
    private SpreadsheetView spreadsheetView;
    public GridRowSkin(SpreadsheetHandle handle, GridRow gridRow) {
        super(gridRow);
        this.handle = handle;
        spreadsheetView = handle.getView();
    }

    /**
     * FIXME Look into and understand the deep cause of that
     * We need to override this since B105 because it's messing up our
     * fixedRows and also our rows CSS.. (kind of flicker)
     */
    @Override protected void handleControlPropertyChanged(String p) {
        if ("ITEM".equals(p)) { //$NON-NLS-1$
            updateCells = true;
            getSkinnable().requestLayout();
        } else if ("INDEX".equals(p)){ //$NON-NLS-1$
           /* // update the index of all children cells (RT-29849)
            final int newIndex = getSkinnable().getIndex();
            for (int i = 0, max = cells.size(); i < max; i++) {
                cells.get(i).updateIndex(newIndex);
            }*/
        } else  {
            super.handleControlPropertyChanged(p);
        }
    }
    
    @Override
    protected void layoutChildren(double x, final double y, final double w,
            final double h) {
        checkState(true);
        if (cellsMap.isEmpty()) return;
        
        
        final ObservableList<? extends TableColumnBase<?, ?>> visibleLeafColumns = getVisibleLeafColumns();
        if (visibleLeafColumns.isEmpty()) {
            super.layoutChildren(x, y, w, h);
            return;
        }
        
        final GridRow control = (GridRow) getSkinnable();
        final SpreadsheetGridView gridView = (SpreadsheetGridView) handle.getGridView();
        final Grid grid = spreadsheetView.getGrid();
        final int index = control.getIndex();
        
        // I put that at the very beginning in the hope that I will not have
        // that extra row at the bottom layouting.
        if (index < 0 || index >= gridView.getItems().size()) {
            /**
             * Investigate if the row at the bottom could be still present, because
             * this opacity is doing trouble when doing:
             * right, scroll down, left, unfix first, go up.
             */
//            control.setOpacity(0);
            return;
        }
        
        final List<SpreadsheetCell> row = grid.getRows().get(index);
        /**
         * If we use "setGrid" on SpreadsheetView, we must be careful because we
         * set our columns after (due to threading safety). So if, by mistake,
         * we are in layout and the columns are set in SpreadsheetView, but not
         * in TableView (yet). Then just return and wait for next calling.
         */
        if (spreadsheetView.getColumns().size() != gridView.getColumns().size()) {
            return;
        }
        checkState(true);
        
        // layout the individual column cells
        double width;
        double height;

        final double verticalPadding = snappedTopInset() + snappedBottomInset();
        final double horizontalPadding = snappedLeftInset()
                + snappedRightInset();
        final double controlHeight = getTableRowHeight(index);
        
        
        handle.getCellsViewSkin().hBarValue.set(index, true);

        // determine the width of the visible portion of the table
        double headerWidth = gridView.getWidth();

        /**
         * FOR FIXED ROWS
         */
        double tableCellY = getFixedRowShift(index);
        

        double fixedColumnWidth = 0;
        List<CellView> fixedCells = new ArrayList();
        for (int column = 0; column < cells.size(); column++) {

            final CellView tableCell = (CellView) cells.get(column);

            // In case the node was treated previously
            tableCell.setManaged(true);

            width = snapSize(tableCell.prefWidth(-1))
                    - snapSize(horizontalPadding);
            height = controlHeight;
            height = snapSize(height) - snapSize(verticalPadding);

            /**
             * FOR FIXED COLUMNS
             */
            double tableCellX = 0;
            final double hbarValue = handle.getCellsViewSkin().getHBar().getValue();

            //Virtualization of column
            final SpreadsheetCell spreadsheetCell = row.get(column);
            boolean isVisible = !isInvisible(x, width, hbarValue, headerWidth, spreadsheetCell.getColumnSpan());

            // We translate that column by the Hbar Value if it's fixed
            if (spreadsheetView.getColumns().get(column).isFixed()) {
                if (hbarValue + fixedColumnWidth > x) {
                    tableCellX = Math.abs(hbarValue - x + fixedColumnWidth);
//                	 tableCell.toFront();
                    fixedColumnWidth += tableCell.getWidth();
                    isVisible = true; // If in fixedColumn, it's obviously visible
                    fixedCells.add(tableCell);
                }
            }
            
//            GridRow row = handle.getCellsViewSkin().getFlow().getTopRow();
//            row.removeCell(tableCell);
            getChildren().remove(tableCell);

            if (isVisible) {
                final SpreadsheetView.SpanType spanType = grid.getSpanType(spreadsheetView, index, column);

                switch (spanType) {
                    case ROW_SPAN_INVISIBLE:
                    case BOTH_INVISIBLE:
                        tableCell.setManaged(false);
                        fixedCells.remove(tableCell);
                        x += width;
                        continue; // we don't want to fall through
                    case COLUMN_SPAN_INVISIBLE:
                        tableCell.setManaged(false);
                        fixedCells.remove(tableCell);
                        continue; // we don't want to fall through
                    case ROW_VISIBLE:
                        final SpreadsheetViewSelectionModel sm = (SpreadsheetViewSelectionModel) spreadsheetView.getSelectionModel();
                        final TableColumn<ObservableList<SpreadsheetCell>, ?> col = gridView.getColumns().get(column);

                        /**
                         * In case this cell was selected before but we scroll
                         * up/down and it's invisible now. It has to pass his
                         * "selected property" to the new Cell in charge of
                         * spanning
                         */
                        final TablePosition<ObservableList<SpreadsheetCell>, ?> selectedPosition = sm.isSelectedRange(index, col, column);
                        // If the selected cell is in the same row, no need to re-select it
                        if (selectedPosition != null
                                //When shift selecting, all cells become ROW_VISIBLE so
                                //We avoid loop selecting here
                                && handle.getCellsViewSkin().containsRow(index)
                                && selectedPosition.getRow() != index) {
                            sm.clearSelection(selectedPosition.getRow(),
                                    selectedPosition.getTableColumn());
                            sm.select(index, col);
                        }
                    case NORMAL_CELL: // fall through and carry on
                        tableCell.show();
                        /**
                         * Here we need to add the cells on the first position
                         * because this row may contain some deported cells from
                         * other rows in order to be on top in term of z-order.
                         * So the cell we're currently adding must not recover
                         * them.
                         */
                        getChildren().add(0, tableCell);
                }

                if (spreadsheetCell.getColumnSpan() > 1) {
                    /**
                     * we need to span multiple columns, so we sum up the width
                     * of the additional columns, adding it to the width
                     * variable
                     */
                    for (int i = 1, colSpan = spreadsheetCell.getColumnSpan(), max1 = cells
                            .size() - column; i < colSpan && i < max1; i++) {
                        width += snapSize(spreadsheetView.getColumns().get(column + i).getWidth());
                    }
                }

                /**
                 * We need to span multiple rows, so we sum up the height of all
                 * the rows. The height of the current row is ignored and the
                 * whole value is computed.
                 */
                if (spreadsheetCell.getRowSpan() > 1) {
                    height = 0;
                    final int maxRow = spreadsheetCell.getRow() + spreadsheetCell.getRowSpan();
                    for (int i = spreadsheetCell.getRow(); i < maxRow; ++i) {
                        height += snapSize(getTableRowHeight(i));
                    }
                }

                tableCell.resize(width, height);

                // We want to place the layout always at the starting cell.
                double spaceBetweenTopAndMe = 0;
                for (int p = spreadsheetCell.getRow(); p < index; ++p) {
                    spaceBetweenTopAndMe += getTableRowHeight(p);
                }

                tableCell.relocate(x + tableCellX, snappedTopInset()
                        - spaceBetweenTopAndMe + tableCellY);

                // Request layout is here as (partial) fix for RT-28684
                // tableCell.requestLayout();
            }
            x += width;
        }
        
        handleFixedCell(fixedCells, index);
        
    }

    /**
     * This handles the fixed cells in column.
     * @param fixedCells
     * @param index 
     */
    private void handleFixedCell(List<CellView> fixedCells, int index) {
        /**
         * If we have a fixedCell (in column) and that cell may be recovered by
         * a rowSpan, we want to put that tableCell ahead in term of z-order. So
         * we need to put it in another row.
         */
        if (handle.getCellsViewSkin().rowToLayout.get(index)) {
            GridRow gridRow = handle.getCellsViewSkin().getFlow().getTopRow();
            if (gridRow != null) {
                for (CellView cell : fixedCells) {
                    final double originalLayoutY = getSkinnable().getLayoutY() + cell.getLayoutY();
                    gridRow.removeCell(cell);
                    gridRow.addCell(cell);
                    if (handle.getCellsViewSkin().deportedCells.containsKey(gridRow)) {
                        handle.getCellsViewSkin().deportedCells.get(gridRow).add(cell);
                    } else {
                        Set<CellView> temp = new HashSet<>();
                        temp.add(cell);
                        handle.getCellsViewSkin().deportedCells.put(gridRow, temp);
                    }
                    /**
                     * I need to have the layoutY of the original row, but also
                     * to remove the layoutY of the row I'm adding in. Because
                     * if the first row is fixed and is undergoing a bit of
                     * translate in order to be visible, we need to remove that
                     * "bit of translate".
                     */
                    cell.relocate(cell.getLayoutX(), originalLayoutY - gridRow.getLayoutY());
                }
            }
        } else {
            for (CellView cell : fixedCells) {
                cell.toFront();
            }
        }
    }
    
    /**
     * Return the space we need to shift that row if it's fixed. Also update the {@link GridViewSkin#getCurrentlyFixedRow()
     * } .
     *
     * @param index
     * @return
     */
    private double getFixedRowShift(int index) {
        double tableCellY = 0;
        int positionY = spreadsheetView.getFixedRows().indexOf(index);

        //FIXME Integrate if fixedCellSize is enabled
        //Computing how much space we need to translate
        //because each row has different space.
        double space = 0;
        for (int o = 0; o < positionY; ++o) {
            space += getTableRowHeight(spreadsheetView.getFixedRows().get(o));
        }

        //If true, this row is fixed
        if (positionY != -1 && getSkinnable().getLocalToParentTransform().getTy() <= space) {
            //This row is a bit hidden on top so we translate then for it to be fully visible
            tableCellY = space - getSkinnable().getLocalToParentTransform().getTy();
            handle.getCellsViewSkin().getCurrentlyFixedRow().add(index);
        } else {
            handle.getCellsViewSkin().getCurrentlyFixedRow().remove(index);
        }
        return tableCellY;
    }
    
    /**
     * Return the height of a row.
     * @param i
     * @return
     */
    private double getTableRowHeight(int i) {
        return handle.getCellsViewSkin().getRowHeight(i);
    }

    /**
     * Return true if the current cell is part of the sceneGraph.
     *
     * @param x beginning of the cell
     * @param width total width of the cell
     * @param hbarValue
     * @param headerWidth width of the visible portion of the tableView
     * @param columnSpan
     * @return
     */
    private boolean isInvisible(double x, double width, double hbarValue,
            double headerWidth, int columnSpan) {
        return (x + width < hbarValue && columnSpan == 1) || (x > hbarValue + headerWidth);
    }
}
