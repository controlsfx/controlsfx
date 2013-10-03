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
package impl.org.controlsfx.skin;

import java.lang.reflect.Field;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;

import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.TableRowSkin;

public class SpreadsheetRowSkin<T extends ObservableList<SpreadsheetCell<?>>> extends TableRowSkin<ObservableList<SpreadsheetCell<?>>> {
    
    static final double DEFAULT_CELL_SIZE;
    static {
        double cell_size = 24.0;
        try {
            Class<?> clazz = com.sun.javafx.scene.control.skin.CellSkinBase.class;
            Field f = clazz.getDeclaredField("DEFAULT_CELL_SIZE");
            f.setAccessible(true);
            cell_size = f.getDouble(null);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DEFAULT_CELL_SIZE = cell_size;
    }
    
    SpreadsheetView spreadsheetView;
    private TableView<ObservableList<SpreadsheetCell<?>>> tableView;
    
    public SpreadsheetRowSkin(TableRow<ObservableList<SpreadsheetCell<?>>> tableRow,
            SpreadsheetView spreadsheetView) {
        super(tableRow);
        this.spreadsheetView = spreadsheetView;
        this.tableView = (TableView<ObservableList<SpreadsheetCell<?>>>) spreadsheetView.getSpreadsheetSkin().getNode();
    }

    /**
     * We need to override this since B105 because it's messing up our
     * fixedRows and also our rows CSS.. (kind of flicker)
     */
    @Override protected void handleControlPropertyChanged(String p) {
        if ("ITEM".equals(p)) {
            updateCells = true;
            getSkinnable().requestLayout();
        } else if ("INDEX".equals(p)){
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

        /**
         * RT-26743:TreeTableView: Vertical Line looks unfinished. We used to
         * not do layout on cells whose row exceeded the number of items, but
         * now we do so as to ensure we get vertical lines where expected in
         * cases where the vertical height exceeds the number of items.
         */
        // I put that at the very beginning in the hope that I will not have
        // that extra row at the bottom layouting.
        final TableRow<ObservableList<SpreadsheetCell<?>>> control = (TableRow<ObservableList<SpreadsheetCell<?>>>) getSkinnable();
        final int index = control.getIndex();
        if (index < 0 || index >= tableView.getItems().size()) {
            control.setOpacity(0);
            return;
        }
        control.setOpacity(1);
        checkState(true);
        if (cellsMap.isEmpty()) { return; }

        final ObservableList<? extends TableColumnBase<?, ?>> visibleLeafColumns = getVisibleLeafColumns();
        if (visibleLeafColumns.isEmpty()) {
            super.layoutChildren(x, y, w, h);
            return;
        }

     
        // determine the width of the visible portion of the table
        double headerWidth =  tableView.getWidth();
        
        Grid grid = spreadsheetView.getGrid();
        
        // layout the individual column cells
        double width;
        double height;

        final double verticalPadding = snappedTopInset() + snappedBottomInset();
        final double horizontalPadding = snappedLeftInset()
                + snappedRightInset();
        final double controlHeight = control.getHeight();

        
        /**
         * FOR FIXED ROWS
         */
        double tableCellY = 0;
        int positionY = spreadsheetView.getFixedRows().indexOf(index);
        //If true, this row is fixed
        if (positionY != -1 && getSkinnable().getLocalToParentTransform().getTy() <= positionY * DEFAULT_CELL_SIZE) {
        	//This row is a bit hidden on top so we translate then for it to be fully visible
            tableCellY = positionY * DEFAULT_CELL_SIZE
                    - getSkinnable().getLocalToParentTransform().getTy();
            ((SpreadsheetRowImpl)getSkinnable()).setCurrentlyFixed(true);
        }else{
        	((SpreadsheetRowImpl)getSkinnable()).setCurrentlyFixed(false);
        }

        double fixedColumnWidth = 0;
        for (int column = 0; column < cells.size(); column++) {

            final SpreadsheetCellImpl<?> tableCell = (SpreadsheetCellImpl<?>) cells.get(column);

            // In case the node was treated previously
            tableCell.setOpacity(1);

            width = snapSize(tableCell.prefWidth(-1))
                    - snapSize(horizontalPadding);
            height = Math.max(controlHeight, tableCell.prefHeight(-1));
            height = snapSize(height) - snapSize(verticalPadding);

            /**
             * FOR FIXED COLUMNS
             */
            double tableCellX = 0;
            final double hbarValue = spreadsheetView.getSpreadsheetSkin().getHBar().getValue();
            
            //Virtualization of column
            final SpreadsheetCell<?> cellSpan = grid.getRows().get(index).get(column);
            boolean isVisible = !isInvisible(x,width,hbarValue,headerWidth,cellSpan.getColumnSpan());
            
            // We translate that column by the Hbar Value if it's fixed
            if (spreadsheetView.getColumns().get(column).isFixed()) {
                
                 if(hbarValue + fixedColumnWidth >x){
                	 tableCellX = Math.abs(hbarValue - x + fixedColumnWidth); 
                	 tableCell.toFront();
                	 fixedColumnWidth += tableCell.getWidth();
                	 isVisible = true; // If in fixedColumn, it's obviously visible
                 }
            }

            
//            if (fixedCellSizeProperty().get() > 0) {
                // we determine if the cell is visible, and if not we have the
                // ability to take it out of the scenegraph to help improve
                // performance. However, we only do this when there is a
                // fixed cell length specified in the TableView. This is because
                // when we have a fixed cell length it is possible to know with
                // certainty the height of each TableCell - it is the fixed
                // value
                // provided by the developer, and this means that we do not have
                // to concern ourselves with the possibility that the height
                // may be variable and / or dynamic.
//                isVisible = isColumnPartiallyOrFullyVisible(tableColumn);
//            }

            if (isVisible) {
                if (/*fixedCellSizeProperty().get() > 0
                        &&*/ tableCell.getParent() == null) {
                    getChildren().add(0,tableCell);
                }

                final SpreadsheetView.SpanType spanType = grid.getSpanType(spreadsheetView, index, column);

                switch (spanType) {
                    case ROW_SPAN_INVISIBLE :
                    case BOTH_INVISIBLE :
                        tableCell.setOpacity(0);
                        tableCell.resize(width, height);
                        tableCell.relocate(x + tableCellX, snappedTopInset()
                                + tableCellY);

                        x += width;
                        continue; // we don't want to fall through
                    case COLUMN_SPAN_INVISIBLE :
                        tableCell.setOpacity(0);
                        tableCell.resize(width, height);
                        tableCell.relocate(x + tableCellX, snappedTopInset()
                                + tableCellY);
                        continue; // we don't want to fall through
                        // infact, we return to the loop here
                    case ROW_VISIBLE :
                        // To be sure that the text is the same
                        // in case we modified the DataCell after that
                        // SpreadsheetCell was created
                        final TableViewSelectionModel<ObservableList<SpreadsheetCell<?>>> sm = spreadsheetView.getSelectionModel();
                        final TableColumn<ObservableList<SpreadsheetCell<?>>, ?> col = tableView.getColumns().get(column);

                        // In case this cell was selected before but we scroll
                        // up/down and it's invisible now.
                        // It has to pass his "selected property" to the new
                        // Cell in charge of spanning
                        final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> selectedPosition = isSelectedRange(index, col, column);
                        // If the selected cell is in the same row, no need to re-select it
                        if (selectedPosition != null
                                && selectedPosition.getRow() != index) {
                            sm.clearSelection(selectedPosition.getRow(),
                                    selectedPosition.getTableColumn());
                            sm.select(index, col);
                        }
                    case NORMAL_CELL : // fall through and carry on
                        tableCell.show();
                }

                if (cellSpan != null) {
                    if (cellSpan.getColumnSpan() > 1) {
                        // we need to span multiple columns, so we sum up
                        // the width of the additional columns, adding it
                        // to the width variable
                        for (int i = 1, colSpan = cellSpan.getColumnSpan(), max1 = cells
                                .size() - column; i < colSpan && i < max1; i++) {
                            // calculate the width
//                            final Node adjacentNode = (Node) getChildren().get(
//                                    column + i);
                            width += snapSize(spreadsheetView.getColumns().get(column+i).getWidth());//adjacentNode.maxWidth(-1));
                        }
                    }

                    if (cellSpan.getRowSpan() > 1) {
                        // we need to span multiple rows, so we sum up
                        // the height of the additional rows, adding it
                        // to the height variable
                        for (int i = 1; i < cellSpan.getRowSpan(); i++) {
                            // calculate the height
                            final double rowHeight = DEFAULT_CELL_SIZE;// getTableRowHeight(index
                                                                       // + i,
                                                                       // getSkinnable());
                            height += snapSize(rowHeight);
                        }
                    }
                }

                tableCell.resize(width, height);
                // We want to place the layout always at the starting cell.
                final double spaceBetweenTopAndMe = (index - cellSpan.getRow())
                        * DEFAULT_CELL_SIZE;
                tableCell.relocate(x + tableCellX, snappedTopInset()
                        - spaceBetweenTopAndMe + tableCellY);

                // Request layout is here as (partial) fix for RT-28684
                // tableCell.requestLayout();
            } else {
//                if (fixedCellSizeProperty().get() > 0) {
                    // we only add/remove to the scenegraph if the fixed cell
                    // length support is enabled - otherwise we keep all
                    // TableCells in the scenegraph
                    getChildren().remove(tableCell);
//                }
            }
            
            x += width;
           
        }
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
		return (x+width <hbarValue && columnSpan == 1) || (x> hbarValue+headerWidth);
	}
    public TablePosition<ObservableList<SpreadsheetCell<?>>, ?> isSelectedRange(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column, int col) {

        final SpreadsheetCell<?> cellSpan = tableView.getItems().get(row).get(col);
        final int infRow = cellSpan.getRow();
        final int supRow = infRow + cellSpan.getRowSpan();

        final int infCol = cellSpan.getColumn();
        final int supCol = infCol + cellSpan.getColumnSpan();
        for (Object tp : spreadsheetView.getSelectionModel().getSelectedCells()) {
        	 TablePosition<ObservableList<SpreadsheetCell<?>>, ?> tp1 = ( TablePosition<ObservableList<SpreadsheetCell<?>>, ?>)tp;
            if (tp1.getRow() >= infRow && tp1.getRow() < supRow && tp1.getColumn() >= infCol && tp1.getColumn() < supCol) {
                return tp1;
            }
        }
        return null;
    }
}
