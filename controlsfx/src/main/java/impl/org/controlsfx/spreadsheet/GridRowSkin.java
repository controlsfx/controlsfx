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
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.behavior.TableRowBehavior;
import com.sun.javafx.scene.control.skin.CellSkinBase;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableRow;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class GridRowSkin extends CellSkinBase<TableRow<ObservableList<SpreadsheetCell>>, CellBehaviorBase<TableRow<ObservableList<SpreadsheetCell>>>> {

    private final SpreadsheetHandle handle;
    private final SpreadsheetView spreadsheetView;

    private Reference<HashMap<TableColumnBase, CellView>> cellsMap;

    private final List<CellView> cells = new ArrayList<>();

    public GridRowSkin(SpreadsheetHandle handle, TableRow<ObservableList<SpreadsheetCell>> gridRow) {
        super(gridRow, new TableRowBehavior<>(gridRow));
        this.handle = handle;
        spreadsheetView = handle.getView();

        getSkinnable().setPickOnBounds(false);

        registerChangeListener(gridRow.itemProperty(), "ITEM");
        registerChangeListener(gridRow.indexProperty(), "INDEX");
    }

    @Override
    protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);

        if ("INDEX".equals(p)) {
            // Fix for RT-36661, where empty table cells were showing content, as they
            // had incorrect table cell indices (but the table row index was correct).
            // Note that we only do the update on empty cells to avoid the issue
            // noted below in requestCellUpdate().
            if (getSkinnable().isEmpty()) {
                requestCellUpdate();
            }
        } else if ("ITEM".equals(p)) {
            requestCellUpdate();
        } else if ("FIXED_CELL_SIZE".equals(p)) {
//            fixedCellSize = fixedCellSizeProperty().get();
//            fixedCellSizeEnabled = fixedCellSize > 0;
        }
    }

    private void requestCellUpdate() {
        getSkinnable().requestLayout();

        // update the index of all children cells (RT-29849).
        // Note that we do this after the TableRow item has been updated,
        // rather than when the TableRow index has changed (as this will be
        // before the row has updated its item). This will result in the
        // issue highlighted in RT-33602, where the table cell had the correct
        // item whilst the row had the old item.
        final int newIndex = getSkinnable().getIndex();
        /**
         * When the index is changing, we need to clear out all the children
         * because we may end up with useless cell in the row.
         */
        getChildren().clear();
        for (int i = 0, max = cells.size(); i < max; i++) {
            cells.get(i).updateIndex(newIndex);
        }
    }

    @Override
    protected void layoutChildren(double x, final double y, final double w, final double h) {

        final ObservableList<? extends TableColumnBase<?, ?>> visibleLeafColumns = handle.getGridView().getVisibleLeafColumns();
        if (visibleLeafColumns.isEmpty()) {
            super.layoutChildren(x, y, w, h);
            return;
        }

        final GridRow control = (GridRow) getSkinnable();
        final SpreadsheetGridView gridView = (SpreadsheetGridView) handle.getGridView();
        final int index = control.getIndex();

        /**
         * If this row is out of bounds, this means that the row is displayed
         * either at the top or at the bottom. In any case, this row is not
         * meant to be seen so we clear its children list in order not to show
         * previous TableCell that could be there.
         */
        if (index < 0 || index >= gridView.getItems().size()) {
            getChildren().clear();
            putCellsInCache();
            return;
        }

        final List<SpreadsheetCell> row = getSkinnable().getItem();//.get(index);
        final List<SpreadsheetColumn> columns = spreadsheetView.getColumns();
        final ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> tableViewColumns = gridView.getColumns();
        /**
         * If we use "setGrid" on SpreadsheetView, we must be careful because we
         * set our columns after (due to threading safety). So if, by mistake,
         * we are in layout and the columns are set in SpreadsheetView, but not
         * in TableView (yet). Then just return and wait for next calling.
         */
        if (columns.size() != tableViewColumns.size()) {
            return;
        }

        getSkinnable().setVisible(true);
        // layout the individual column cells
        double width;
        double height;

        final double verticalPadding = snappedTopInset() + snappedBottomInset();
        final double horizontalPadding = snappedLeftInset()
                + snappedRightInset();
        /**
         * Here we make the distinction between the official controlHeight and
         * the customHeight that we may apply.
         */
        double controlHeight = getTableRowHeight(index);
        double customHeight = controlHeight == Grid.AUTOFIT ? GridViewSkin.DEFAULT_CELL_HEIGHT : controlHeight;

        final GridViewSkin skin = handle.getCellsViewSkin();
        skin.hBarValue.set(index, true);

        // determine the width of the visible portion of the table
        double headerWidth = gridView.getWidth();
        final double hbarValue = skin.getHBar().getValue();

        /**
         * FOR FIXED ROWS
         */
        ((GridRow) getSkinnable()).verticalShift.setValue(getFixedRowShift(index));

        double fixedColumnWidth = 0;
        List<CellView> fixedCells = new ArrayList();

        //We compute the cells here
        putCellsInCache();

        boolean firstVisibleCell = false;
        CellView lastCell = null;
        boolean needToBeShifted;
        boolean rowHeightChange = false;
        boolean isFixed;
        for (int indexColumn = 0; indexColumn < columns.size(); indexColumn++) {
            //FIXME Problem qwith column span
            if(!skin.getSkinnable().getColumns().get(indexColumn).isVisible()){
                continue;
            }
            width = snapSize(columns.get(indexColumn).getWidth()) - snapSize(horizontalPadding);
            //When setting a new grid with less columns, we may have this situation.
            if (row.size() <= indexColumn) {
                break;
           }
            final SpreadsheetCell spreadsheetCell = row.get(indexColumn);
            final int columnSpan = spreadsheetView.getColumnSpan(spreadsheetCell);
            boolean isVisible = !isInvisible(x, width, hbarValue, headerWidth, columnSpan);
            isFixed = columns.get(indexColumn).isFixed();
            if (isFixed) {
                isVisible = true;
            }

            if (!isVisible) {
                if (firstVisibleCell) {
                    break;
                }
                x += width;
                continue;
            }
            final CellView tableCell = getCell(gridView.getColumns().get(indexColumn));

            cells.add(0, tableCell);

            // In case the node was treated previously
            tableCell.setManaged(true);

            /**
             * FOR FIXED COLUMNS
             */
            double tableCellX = 0;

            /**
             * We need to update the fixedColumnWidth only on visible cell and
             * we need to add the full width including the span.
             *
             * If we fail to do so, we may be in the situation where x will grow
             * with the correct width and not fixedColumnWidth. Thus some cell
             * that should be shifted will not because the computation based on
             * fixedColumnWidth will be wrong.
             */
            boolean increaseFixedWidth = false;
            final int viewColumn =spreadsheetView.getViewColumn(spreadsheetCell.getColumn()); 
            //Virtualization of column
            // We translate that column by the Hbar Value if it's fixed
            if (isFixed) {
                /**
                 * Here we verify if our cell must be shifted. The second
                 * condition is to determine that we are dealing with the very
                 * first cell of a columnSpan. If we have the hidden cells, we
                 * must not increase the fixedColumnWidth.
                 */
                if (hbarValue + fixedColumnWidth > x &&  spreadsheetCell.getColumn() == indexColumn) {
                    increaseFixedWidth = true;
                    tableCellX = Math.abs(hbarValue - x + fixedColumnWidth);
//                	 tableCell.toFront();
                    fixedColumnWidth += width;
//                    isVisible = true; // If in fixedColumn, it's obviously visible
                    fixedCells.add(tableCell);
                }
            }

            if (isVisible) {
                final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(index, indexColumn);

                switch (spanType) {
                    case ROW_SPAN_INVISIBLE:
                    case BOTH_INVISIBLE:
                        fixedCells.remove(tableCell);
                        getChildren().remove(tableCell);
//                        cells.remove(tableCell);
                        x += width;
                        continue; // we don't want to fall through
                    case COLUMN_SPAN_INVISIBLE:
                        fixedCells.remove(tableCell);
                        getChildren().remove(tableCell);
//                        cells.remove(tableCell);
                        continue; // we don't want to fall through
                    case ROW_VISIBLE:
                    case NORMAL_CELL: // fall through and carry on
                        if (tableCell.getIndex() != index) {
                            tableCell.updateIndex(index);
                        } else {
                            tableCell.updateItem(spreadsheetCell, false);
                        }
                        /**
                         * Here we need to add the cells on the first position
                         * because this row may contain some deported cells from
                         * other rows in order to be on top in term of z-order.
                         * So the cell we're currently adding must not recover
                         * them. We must check that the parent is indeed the
                         * getSkinnable because of the deportedCells.
                         */
                        if (!tableCell.isEditing() && tableCell.getParent() != getSkinnable()) {
                            getChildren().add(0, tableCell);
                        }
                }

                if (columnSpan > 1) {
                    /**
                     * we need to span multiple columns, so we sum up the width
                     * of the additional columns, adding it to the width
                     * variable
                     */
                    final int max = skin.getSkinnable().getVisibleLeafColumns().size() - viewColumn;
                    for (int i = 1, colSpan = columnSpan; i < colSpan && i < max; i++) {
                        double tempWidth = snapSize(skin.getSkinnable().getVisibleLeafColumn(viewColumn + i).getWidth());
                        width += tempWidth;
                        if (increaseFixedWidth) {
                            fixedColumnWidth += tempWidth;
                        }
                    }
                }

                /**
                 * If we are in autofit and the prefHeight of this cell is
                 * superior to the default cell height. Then we will use this
                 * new height for row's height.
                 *
                 * We then need to apply the value to previous cell, and also
                 * layout the children because since we are layouting upward,
                 * next rows needs to know that this row is bigger than usual.
                 */
                if (controlHeight == Grid.AUTOFIT && !tableCell.isEditing()) {
                    //We have the problem when we are just one pixel short in height..
                    double tempHeight = tableCell.prefHeight(width) + tableCell.snappedTopInset() + tableCell.snappedBottomInset();
                    if (tempHeight > customHeight) {
                        rowHeightChange = true;
                        skin.rowHeightMap.put(spreadsheetCell.getRow(), tempHeight);
                        for (CellView cell : cells) {
                            /**
                             * We need to add the difference between the
                             * previous height and the new height. If we were
                             * just setting the new height, the row spanning
                             * cell would be shorter. That's why we need to use
                             * the cell height.
                             */
                            cell.resize(cell.getWidth(), cell.getHeight() + (tempHeight - customHeight));
                        }
                        customHeight = tempHeight;
                        skin.getFlow().layoutChildren();
                    }
                }

                height = customHeight;
                height = snapSize(height) - snapSize(verticalPadding);
                /**
                 * We need to span multiple rows, so we sum up the height of all
                 * the rows. The height of the current row is ignored and the
                 * whole value is computed.
                 */
                int rowSpan = spreadsheetView.getRowSpan(spreadsheetCell, index);
                if (rowSpan > 1) {
                    height = 0;
                    final int maxRow = index + rowSpan;
                    for (int i = index; i < maxRow; ++i) {
                        height += snapSize(skin.getRowHeight(i));
                    }
                }

                //Fix for JDK-8146406
                needToBeShifted = false;
                /**
                 * If the current cell has no left border, and the previous cell
                 * had no right border. We may have the problem where there is a
                 * tiny gap between the cells when scrolling horizontally. Thus
                 * we must enlarge this cell a bit, and shift it a bit in order
                 * to mask that gap. If the cell has a border defined, the
                 * problem seems not to happen. If the cell is not added to its
                 * parent, it has no border by default so we must not check it.
                 */
                if (lastCell != null
                        && !hasRightBorder(lastCell)
                        && !hasLeftBorder(tableCell)) {
                    tableCell.resize(width + 1, height);
                    needToBeShifted = true;
                } else {
                    tableCell.resize(width, height);
                }
                lastCell = tableCell;
                // We want to place the layout always at the starting cell.
                double spaceBetweenTopAndMe = 0;

                tableCell.relocate(x + tableCellX + (needToBeShifted ? -1 : 0), snappedTopInset()
                        - spaceBetweenTopAndMe + ((GridRow) getSkinnable()).verticalShift.get());
            } else {
                getChildren().remove(tableCell);
            }
            x += width;
        }
        skin.fixedColumnWidth = fixedColumnWidth;
        handleFixedCell(fixedCells, index);
        removeUselessCell(index);
        if (handle.getCellsViewSkin().lastRowLayout.get() == true) {
            handle.getCellsViewSkin().lastRowLayout.setValue(false);
        }
        /**
         * If we modified an height here, ROW_HEIGHT_CHANGE will not be
         * triggered, because it's not the user who has modified that. So the
         * rectangle will not update, we need to force it here.
         */
        if (rowHeightChange && spreadsheetView.getFixedRows().contains(spreadsheetView.getModelRow(index))) {
            skin.computeFixedRowHeight();
        }
    }

    private boolean hasRightBorder(CellView tableCell) {
        return tableCell.getBorder() != null
                && !tableCell.getBorder().isEmpty()
                && tableCell.getBorder().getStrokes().get(0).getWidths().getRight() > 0;
    }

    private boolean hasLeftBorder(CellView tableCell) {
        return tableCell.getBorder() != null
                && !tableCell.getBorder().isEmpty()
                && tableCell.getBorder().getStrokes().get(0).getWidths().getLeft()> 0;
    }

    /**
     * Here we want to remove of the sceneGraph cells that are not used.
     *
     * Before we were removing the cells that we were getting from the cache.
     * But that is not enough because some cells can be added somehow, and stay
     * within the row. Since we do not often clear the children because of some
     * deportedCell present inside, we must use that Predicate to clear all
     * CellView not contained in cells and with the same index. Thus we preserve
     * the deported cell.
     */
    private void removeUselessCell(int index) {
        getChildren().removeIf((Node t) -> {
            if (t instanceof CellView) {
                return !cells.contains(t) && ((CellView) t).getIndex() == index;
            }
            return false;
        });
    }

    private void removeDeportedCells() {
        GridViewSkin skin = handle.getCellsViewSkin();
        for (Map.Entry<GridRow, Set<CellView>> entry : skin.deportedCells.entrySet()) {
            ArrayList<CellView> toRemove = new ArrayList<>();
            for (CellView cell : entry.getValue()) {
                /**
                 * If that cell is mine, I can remove it because I will replace
                 * it if necessary. If that cell is mine and I'm the top row, no
                 * need to remove it because I may remove cells that were
                 * deported but are not anymore and create blank space.
                 */
                if (!cell.isEditing() && cell.getTableRow() == getSkinnable() && entry.getKey() != getSkinnable()) {
                    entry.getKey().removeCell(cell);
                    toRemove.add(cell);
                }
            }
            entry.getValue().removeAll(toRemove);
        }
    }

    /**
     * This handles the fixed cells in column.
     *
     * @param fixedCells
     * @param index
     */
     private void handleFixedCell(List<CellView> fixedCells, int index) {
        removeDeportedCells();
        if (fixedCells.isEmpty()) {
            return;
        }
        GridViewSkin skin = handle.getCellsViewSkin();
        /**
         * If we have a fixedCell (in column) and that cell may be recovered by
         * a rowSpan, we want to put that tableCell ahead in term of z-order. So
         * we need to put it in another row.
         */
        if (skin.rowToLayout.get(index)) {
            GridRow gridRow = skin.getFlow().getTopRow();
            if (gridRow != null) {
                for (CellView cell : fixedCells) {
                    if (!cell.isEditing()) {
                        gridRow.removeCell(cell);
                        gridRow.addCell(cell);
                    }
                    final double originalLayoutY = getSkinnable().getLayoutY() + cell.getLayoutY();

                    if (skin.deportedCells.containsKey(gridRow)) {
                        skin.deportedCells.get(gridRow).add(cell);
                    } else {
                        Set<CellView> temp = new HashSet<>();
                        temp.add(cell);
                        skin.deportedCells.put(gridRow, temp);
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
     * Return the Cache. Here we use a WeakReference because the WeakHashMap is
     * not working. TableCell added to it are not removed if the GC wants them.
     * So we put the whole cache in WeakReference. In normal condition, the
     * cache is not trashed that much and is efficient. In the case where the
     * user scroll horizontally a lot, that cache can then be trashed in order
     * to avoid OutOfMemoryError.
     *
     * @return
     */
    private HashMap<TableColumnBase, CellView> getCellsMap() {
        if (cellsMap == null || cellsMap.get() == null) {
            HashMap<TableColumnBase, CellView> map = new HashMap<>();
            cellsMap = new WeakReference<>(map);
            return map;
        }
        return cellsMap.get();
    }

    /**
     * This will put all current displayed cell into the cache.
     */
    private void putCellsInCache() {
        for (CellView cell : cells) {
            getCellsMap().put(cell.getTableColumn(), cell);
        }
        cells.clear();
    }

    /**
     * This will retrieve a cell for the specified column. If the cell exists in
     * the cache, it's extracted from it. Otherwise, a cell is created.
     *
     * @param tcb
     * @return
     */
    private CellView getCell(TableColumnBase tcb) {
        TableColumn tableColumn = (TableColumn<CellView, ?>) tcb;
        CellView cell;
        if (getCellsMap().containsKey(tableColumn)) {
            return getCellsMap().remove(tableColumn);
        } else {
            cell = (CellView) tableColumn.getCellFactory().call(tableColumn);
            cell.updateTableColumn(tableColumn);
            cell.updateTableView(tableColumn.getTableView());
            cell.updateTableRow(getSkinnable());
        }
        return cell;
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
        int positionY = spreadsheetView.getFixedRows().indexOf(spreadsheetView.getFilteredSourceIndex(index));

        //FIXME Integrate if fixedCellSize is enabled
        //Computing how much space we need to translate
        //because each row has different space.
        double space = 0;
        for (int o = 0; o < positionY; ++o) {
            if (!spreadsheetView.isRowHidden(o)) {
                space += handle.getCellsViewSkin().getRowHeight(spreadsheetView.getFixedRows().get(o));
            }
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
     *
     * @param row
     * @return
     */
    private double getTableRowHeight(int row) {
        Double rowHeightCache = handle.getCellsViewSkin().rowHeightMap.get(spreadsheetView.getModelRow(row));
        return rowHeightCache == null ? handle.getView().getGrid().getRowHeight(spreadsheetView.getModelRow(row)) : rowHeightCache;
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

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefWidth = 0.0;

        final List<? extends TableColumnBase/*<T,?>*/> visibleLeafColumns = handle.getGridView().getVisibleLeafColumns();
        for (int i = 0, max = visibleLeafColumns.size(); i < max; i++) {
            prefWidth += visibleLeafColumns.get(i).getWidth();
        }

        return prefWidth;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
}
