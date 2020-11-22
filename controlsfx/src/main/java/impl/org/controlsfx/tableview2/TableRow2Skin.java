/**
 * Copyright (c) 2013, 2020 ControlsFX
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

import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import org.controlsfx.control.tableview2.TableView2;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public class TableRow2Skin<S> extends CellSkinBase<TableRow<S>> {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass LEFT_CELL = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_CELL = PseudoClass.getPseudoClass("right");
    private static final PseudoClass SINGLE_CELL = PseudoClass.getPseudoClass("single");
    private static final PseudoClass FIXED_CELL = PseudoClass.getPseudoClass("fixed");
            
    private final TableView2<S> tableView;
    private final TableView2Skin<S> skin;

    private Reference<HashMap<TableColumnBase, TableCell<S, ?>>> cellsMap;

    private final List<TableCell<S, ?>> cells = new ArrayList<>();
    
    private final TableView2<S> parentTableView;
    
    public TableRow2Skin(TableView2<S> tableView, TableRow<S> tableRow) {
        super(tableRow);
        this.tableView = tableView;
        
        getSkinnable().setPickOnBounds(false);

        registerChangeListener(tableRow.itemProperty(), t -> requestCellUpdate());
        registerChangeListener(tableRow.indexProperty(), t -> {
            // Fix for JDK-8095357, where empty table cells were showing content, as they
            // had incorrect table cell indices (but the table row index was correct).
            // Note that we only do the update on empty cells to avoid the issue
            // noted below in requestCellUpdate().
            if (getSkinnable().isEmpty()) {
                requestCellUpdate();
            }
        });
        
        if (tableView.getParent() != null && tableView.getParent() instanceof RowHeader) {
            parentTableView = ((RowHeader) tableView.getParent()).getParentTableView();
            this.skin = (TableView2Skin<S>) parentTableView.getSkin();
        } else {
            parentTableView = null;
            this.skin = (TableView2Skin<S>) tableView.getSkin();
        }
    }

    private void requestCellUpdate() {
        getSkinnable().requestLayout();

        // update the index of all children cells (JDK-8119094).
        // Note that we do this after the TableRow item has been updated,
        // rather than when the TableRow index has changed (as this will be
        // before the row has updated its item). This will result in the
        // issue highlighted in JDK-8115269, where the table cell had the correct
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

    /** {@inheritDoc} */
    @Override protected void layoutChildren(double x, final double y, final double w, final double h) {

        final ObservableList<? extends TableColumnBase<?, ?>> visibleLeafColumns = tableView.getVisibleLeafColumns();
        if (visibleLeafColumns.isEmpty()) {
            super.layoutChildren(x, y, w, h);
            return;
        }

        final TableRow2<S> control = (TableRow2<S>) getSkinnable();
        final int index = control.getIndex();

        /**
         * If this row is out of bounds, this means that the row is displayed
         * either at the top or at the bottom. In any case, this row is not
         * meant to be seen so we clear its children list in order not to show
         * previous TableCell that could be there.
         */
        if (index < 0) {
            getChildren().clear();
            putCellsInCache();
            return;
        }
        
        getChildren().removeIf(n -> n.getId() != null && n.getId().equals("pane-fixed-cell"));
            
        Object o = control.getProperties().get("fixed");
        boolean fixedRow = o != null && ((Boolean) o).equals(Boolean.TRUE);

        final List<? extends TableColumn<S, ?>> columns = tableView.getVisibleLeafColumns();

        getSkinnable().setVisible(true);
        // layout the individual column cells
        double width;
        double height = 0;

        final double verticalPadding = snappedTopInset() + snappedBottomInset();
        final double horizontalPadding = snappedLeftInset() + snappedRightInset();
        /**
         * Here we make the distinction between the official controlHeight and
         * the customHeight that we may apply.
         */
        double customHeight = skin.getRowHeight(control.getIndex());
        skin.hBarValue.set(index, true);

        // determine the width of the visible portion of the table
        double headerWidth = tableView.getWidth();
        final double hbarValue = parentTableView != null ? 0 : skin.getHBar().getValue();

        /**
         * FOR FIXED ROWS
         */
        control.verticalShift.setValue(tableView.isRowFixingEnabled() ? getFixedRowShift(index) : 0);

        double fixedColumnWidth = 0;
        List<TableCell<S, ?>> fixedCells = new ArrayList();

        //We compute the cells here
        putCellsInCache();

        boolean firstVisibleCell = false;
        TableCell<S, ?> lastCell = null;
        boolean needToBeShifted;
        boolean rowHeightChange = false;
        boolean isFirstColumn = false;
        for (int indexColumn = 0; indexColumn < columns.size(); indexColumn++) {
            TableColumn<S, ?> column = columns.get(indexColumn);
            //FIXME Problem with column span
            if (! column.isVisible()) {
                continue;
            }
            final TableCell<S, ?> tableCell = getCell(column);
            if (! isFirstColumn) {
                tableCell.pseudoClassStateChanged(LEFT_CELL, true);
                isFirstColumn = true;
            }
            
            TablePosition<S, ?> pos = new TablePosition<>(tableView, tableCell.getIndex(), column);
        
            width = snapSize(column.getWidth()) - snapSize(horizontalPadding);
            //When setting a new grid with less columns, we may have this situation.
            final int columnSpan = tableView.getColumnSpan(pos);
            boolean isVisible = !isInvisible(x, width, hbarValue, headerWidth, columnSpan);
            while (column.getParentColumn() != null) {
                // on nested columns, we check if the root parent is the one fixed
                column = (TableColumn<S, ?>) column.getParentColumn();
            }
            
            if (tableView.isColumnFixingEnabled() && tableView.getFixedColumns().contains(column)) {
                isVisible = true;
            }

            if (!isVisible) {
                if (firstVisibleCell) {
                    break;
                }
                x += width;
                continue;
            }
            
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
            //Virtualization of column
            // We translate that column by the Hbar Value if it's fixed
            if (tableView.isColumnFixingEnabled() && tableView.getFixedColumns().contains(column)) {
                /**
                 * Here we verify if our cell must be shifted. The second
                 * condition is to determine that we are dealing with the very
                 * first cell of a columnSpan. If we have the hidden cells, we
                 * must not increase the fixedColumnWidth.
                 */
                if (hbarValue + fixedColumnWidth > x) {
                    increaseFixedWidth = true;
                    tableCellX = Math.abs(hbarValue - x + fixedColumnWidth);
//                	 tableCell.toFront();
                    fixedColumnWidth += width;
//                    isVisible = true; // If in fixedColumn, it's obviously visible
                    fixedCells.add(tableCell);
                }
            }
            
            if (isVisible) {
                final TableView2.SpanType spanType = tableView.getSpanType(index, indexColumn);

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
//                            tableCell.updateItem(tableCell.getItem(), false); 
                            tableCell.updateIndex(index);
                            tableCell.requestLayout();
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

                /**
                 * Check selection. 
                 * Fixes a bug in cell selection after a column is hidden 
                 */
                if (tableView.getSelectionModel() != null && tableView.getSelectionModel().isCellSelectionEnabled()) {
                    final int ic = indexColumn;
                    boolean select = ! tableView.getSelectionModel().getSelectedCells().stream()
                                .noneMatch(cell -> cell.getRow() == index && cell.getColumn() == ic);
                    tableCell.pseudoClassStateChanged(SELECTED, select);
                }
                
                if (columnSpan > 1) {
                    /**
                     * we need to span multiple columns, so we sum up the width
                     * of the additional columns, adding it to the width
                     * variable
                     */
                    int viewColumn = skin.getViewColumn(indexColumn);
                    final int max = tableView.getVisibleLeafColumns().size() - viewColumn;
                    for (int i = 1, colSpan = columnSpan; i < colSpan && i < max; i++) {
                        double tempWidth = snapSize(tableView.getVisibleLeafColumn(viewColumn + i).getWidth());
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
                if (!tableCell.isEditing()) {
                    //We have the problem when we are just one pixel short in height..
                    double tempHeight = tableCell.prefHeight(width) + tableCell.snappedTopInset() + tableCell.snappedBottomInset();
                    if (parentTableView != null) {
                        // for the row header take the height from the max tempHeight from the parent TableRow
                        if (control.getIndex() < skin.getItemCount()) {
                            tempHeight = skin.rowHeightMap.getOrDefault(control.getIndex(), tempHeight);
                        }
                    }
            
                    if (tempHeight > customHeight) {
                        rowHeightChange = true;
                        skin.rowHeightMap.put(control.getIndex(), tempHeight);
                        for (TableCell<S, ?> cell : cells) {
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
                        if (parentTableView != null) {
                            ((TableView2Skin<S>) tableView.getSkin()).getFlow().layoutChildren();
                        } else {
                            ((TableView2Skin<S>) skin).getFlow().layoutChildren();
                        }
                    }
                }

                height = customHeight;
                height = snapSize(height) - snapSize(verticalPadding);
                /**
                 * We need to span multiple rows, so we sum up the height of all
                 * the rows. The height of the current row is ignored and the
                 * whole value is computed.
                 */
                int rowSpan = tableView.getRowSpan(pos, index);
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
                 * had no right border. We may have the problem
                 * where there is a tiny gap between the cells when scrolling
                 * horizontally. Thus we must enlarge this cell a bit, and shift
                 * it a bit in order to mask that gap. If the cell has a border
                 * defined, the problem seems not to happen.
                 * If the cell is not added to its parent, it has no border by default so we must not check it.
                 */
                if (/*tableView.getFixedRows().contains(tableView2Cell.getRow())
                        && */lastCell != null
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
//                for (int p = tableView2Cell.getRow(); p < index; ++p) {
//                    spaceBetweenTopAndMe += skin.getRowHeight(p);
//                }

                tableCell.relocate(x + tableCellX + (needToBeShifted? -1 : 0), snappedTopInset()
                        - spaceBetweenTopAndMe + control.verticalShift.get());

                // Request layout is here as (partial) fix for JDK-8118040
//                 tableCell.requestLayout();
            } else {
                getChildren().remove(tableCell);
            }
            x += width;
        }
        
        // When the table is wider than the columns, the empty space to the
        // right is visible and can be part of the row selection or scrolled. 
        double paneWidth = tableView.getWidth() - x - tableView.snappedLeftInset() - tableView.snappedRightInset();
        if (tableView.isRowHeaderVisible()) {
            paneWidth -= tableView.getRowHeaderWidth();
        }
        if (skin.getVBar().isVisible()) {
            paneWidth -= skin.getVBar().getWidth();
        }
        
        final int paneBorderInset = 1; // selection border inset in pane
        final boolean paneBorderVisible = paneWidth > paneBorderInset;
        final boolean singleColumn = columns.size() == 1;
        if (fixedRow && paneWidth >= 0 && parentTableView == null) {
            // If there are fixed rows, we add a pane to the row, as an empty cell 
            // that can be styled as the others fixed cells. 
            // As the real fixed cells, this pane will be on top of the other 
            // empty rows when scrolling.
            final Pane pane = new Pane();
            pane.setId("pane-fixed-cell");
            pane.getStyleClass().add("tableview2-cell");
            // provides right selection border in case of row selection
            pane.pseudoClassStateChanged(RIGHT_CELL, paneBorderVisible);
            pane.resizeRelocate(x, snappedTopInset() + control.verticalShift.get(), paneWidth, height);
            getChildren().add(pane);
        } 
        
        if (lastCell != null) {
            // In case of row selection, when the table is wider than the columns, 
            // the cells don't require a right selection border: the tableRow 
            // itself provides this border in case of regular rows, or the above
            // pane will provide it in case of fixed rows.
            lastCell.pseudoClassStateChanged(RIGHT_CELL, ! singleColumn && ! paneBorderVisible);
            lastCell.pseudoClassStateChanged(SINGLE_CELL, singleColumn && ! paneBorderVisible);
         
            if (parentTableView != null) {
                lastCell.pseudoClassStateChanged(FIXED_CELL, fixedRow && control.getIndex() < skin.getItemCount());
            }
        }
        
        skin.fixedColumnWidth = fixedColumnWidth;
        handleFixedCell(fixedCells, index);
        removeUselessCell(index);
        if (skin.lastRowLayout.get() == true) {
            skin.lastRowLayout.setValue(false);
        }
        /**
         * If we modified an height here, ROW_HEIGHT_CHANGE will not be
         * triggered, because it's not the user who has modified that. So the
         * rectangle will not update, we need to force it here.
         */
        if (rowHeightChange && tableView.getFixedRows().contains(index)) {
            skin.computeFixedRowHeight();
        }
    }

    private boolean hasRightBorder(TableCell<S, ?> tableCell) {
        return tableCell.getBorder() != null
                && !tableCell.getBorder().isEmpty()
                && tableCell.getBorder().getStrokes().get(0).getWidths().getRight() > 0;
    }

    private boolean hasLeftBorder(TableCell<S, ?> tableCell) {
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
     * TableCell not contained in cells and with the same index. Thus we preserve
     * the deported cell.
     */
    private void removeUselessCell(int index) {
        getChildren().removeIf((Node t) -> {
            if (t instanceof TableCell) {
                return ! cells.contains((TableCell<S, ?>) t) && ((TableCell<S, ?>) t).getIndex() == index;
            }
            return false;
        });
    }

    private void removeDeportedCells() {
        for (Map.Entry<TableRow2<S>, Set<TableCell<S, ?>>> entry : skin.deportedCells.entrySet()) {
            ArrayList<TableCell<S, ?>> toRemove = new ArrayList<>();
            for (TableCell<S, ?> cell : entry.getValue()) {
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
     private void handleFixedCell(List<TableCell<S, ?>> fixedCells, int index) {
        removeDeportedCells();
        if (fixedCells.isEmpty()) {
            return;
        }
        /**
         * If we have a fixedCell (in column) and that cell may be recovered by
         * a rowSpan, we want to put that tableCell ahead in term of z-order. So
         * we need to put it in another row.
         */
        if (skin.rowToLayout.get(index)) {
            // TODO: Check the required row if rowSpan > 1
            TableRow<S> tableRow = getSkinnable();
            if (tableRow != null && tableRow instanceof TableRow2) {
                final TableRow2<S> tableRow2 = (TableRow2<S>) tableRow;
                for (TableCell<S, ?> cell : fixedCells) {
                    if (!cell.isEditing()) {
                        tableRow2.removeCell(cell);
                        tableRow2.addCell(cell);
                    }
                    final double originalLayoutY = getSkinnable().getLayoutY() + cell.getLayoutY();

                    if (skin.deportedCells.containsKey(tableRow2)) {
                        skin.deportedCells.get(tableRow2).add(cell);
                    } else {
                        Set<TableCell<S, ?>> temp = new HashSet<>();
                        temp.add(cell);
                        skin.deportedCells.put(tableRow2, temp);
                    }
                    /**
                     * I need to have the layoutY of the original row, but also
                     * to remove the layoutY of the row I'm adding in. Because
                     * if the first row is fixed and is undergoing a bit of
                     * translate in order to be visible, we need to remove that
                     * "bit of translate".
                     */
                    cell.relocate(cell.getLayoutX(), originalLayoutY - tableRow.getLayoutY());
                }
            }
        } else {
            for (TableCell<S, ?> cell : fixedCells) {
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
    private HashMap<TableColumnBase, TableCell<S, ?>> getCellsMap() {
        if (cellsMap == null || cellsMap.get() == null) {
            HashMap<TableColumnBase, TableCell<S, ?>> map = new HashMap<>();
            cellsMap = new WeakReference<>(map);
            return map;
        }
        return cellsMap.get();
    }

    /**
     * This will put all current displayed cell into the cache.
     */
    private void putCellsInCache() {
        for (TableCell<S, ?> cell : cells) {
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
    private TableCell<S, ?> getCell(TableColumnBase tcb) {
        TableColumn tableColumn = (TableColumn<TableCell<S, ?>, ?>) tcb;
        TableCell<S, ?> cell;
        if (getCellsMap().containsKey(tableColumn)) {
            cell = getCellsMap().remove(tableColumn);
        } else {
            Callback cellFactory = tableColumn.getCellFactory();
            if (cellFactory == null) {
                cellFactory = TableColumn.DEFAULT_CELL_FACTORY;
            }
            cell = (TableCell<S, ?>) cellFactory.call(tableColumn);
            if (! cell.getStyleClass().contains("tableview2-cell")) {
                cell.getStyleClass().add("tableview2-cell");
            }
            cell.updateTableColumn(tableColumn);
            cell.updateTableView(tableColumn.getTableView());
            cell.updateTableRow(getSkinnable());
        
            if (parentTableView != null) {
                cell.setOnContextMenuRequested(e -> {
                    BiFunction<Integer, S, ContextMenu> cmFactory = parentTableView.getRowHeaderContextMenuFactory();
                    if (tableView.getItems() != null && cmFactory != null) {
                        ContextMenu contextMenu = cmFactory.apply(getSkinnable().getIndex(), getSkinnable().getItem());
                        contextMenu.show(tableView.getScene().getWindow(), e.getScreenX(), e.getScreenY());
                    } 
                });
            }
        }
        
        cell.pseudoClassStateChanged(LEFT_CELL, false);
        cell.pseudoClassStateChanged(RIGHT_CELL, false);
        cell.pseudoClassStateChanged(SINGLE_CELL, false);
        return cell;
    }

    /**
     * Return the space we need to shift that row if it's fixed. Also update the 
     * {@link TableView2Skin#getCurrentlyFixedRow()} .
     *
     * @param index
     * @return
     */
    private double getFixedRowShift(int index) {
        double tableCellY = 0;
        int positionY = tableView.getFixedRows().indexOf(index);

        //FIXME Integrate if fixedCellSize is enabled
        //Computing how much space we need to translate
        //because each row has different space.
        double space = 0;
        for (int o = 0; o < positionY; ++o) {
            space += skin.getRowHeight(tableView.getFixedRows().get(o));
        }

        //If true, this row is fixed
        if (positionY != -1 && getSkinnable().getLocalToParentTransform().getTy() <= space) {
            //This row is a bit hidden on top so we translate then for it to be fully visible
            tableCellY = space - getSkinnable().getLocalToParentTransform().getTy();
            skin.getCurrentlyFixedRow().add(index);
        } else {
            skin.getCurrentlyFixedRow().remove(index);
        }
        return tableCellY;
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

    /** {@inheritDoc} */
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double prefWidth = 0.0;

        final List<? extends TableColumnBase/*<T,?>*/> visibleLeafColumns = tableView.getVisibleLeafColumns();
        for (int i = 0, max = visibleLeafColumns.size(); i < max; i++) {
            prefWidth += visibleLeafColumns.get(i).getWidth();
        }

        return prefWidth;
    }

    /** {@inheritDoc} */
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (parentTableView != null) {
            return skin.getRow(getSkinnable().getIndex()).getPrefHeight();
        }
        return getSkinnable().getPrefHeight();
    }

    /** {@inheritDoc} */
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (parentTableView != null) {
            return skin.getRow(getSkinnable().getIndex()).getPrefHeight();
        }
        return getSkinnable().getPrefHeight();
    }

    /** {@inheritDoc} */
    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
}
