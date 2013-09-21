package org.controlsfx.control.spreadsheet.model;

import impl.org.controlsfx.skin.SpreadsheetViewSkin;

import java.util.ArrayList;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.SpreadsheetView.SpanType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GridBase implements Grid {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    private ObservableList<ObservableList<SpreadsheetCell<?>>> rows;
    private int rowCount;
    private int columnCount;
    
    

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    /**
     * Creates grid with 'unlimited' rows and columns
     */
    public GridBase() {
        this(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public GridBase(int rowCount, int columnCount) {
        this(rowCount, columnCount,FXCollections.<ObservableList<SpreadsheetCell<?>>> emptyObservableList());
    }

    public GridBase(int rowCount, int columnCount, ObservableList<ObservableList<SpreadsheetCell<?>>> rows) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.rows = rows;
    }

    
    
    /***************************************************************************
     * 
     * Public Methods (Inherited from Grid) 
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override public ObservableList<ObservableList<SpreadsheetCell<?>>> getRows() {
        return rows;
    }
    
    /** {@inheritDoc} */
    @Override public int getRowCount() {
        return rowCount;
    }
    
    /** {@inheritDoc} */
    @Override public int getColumnCount() {
        return columnCount;
    }
    
    /**
     * Return the {@link SpanType} of a cell.
     * @param row
     * @param column
     * @return
     */
    @Override public SpanType getSpanType(final SpreadsheetView spv, final int row, final int column) {

        if (row < 0 || column < 0 || !containsRow(spv, row)) {
            return SpanType.NORMAL_CELL;
        }
        final SpreadsheetCell<?> cellSpan = ((ObservableList<SpreadsheetCell<?>>)getRows().get(row)).get(column);
        
        final int cellSpanColumn = cellSpan.getColumn();
        final int cellSpanRow = cellSpan.getRow();
        final int cellSpanRowSpan = cellSpan.getRowSpan();
        final int cellSpanColumnSpan = cellSpan.getColumnSpan();
        final boolean containsRowMinusOne = containsRow(spv, row-1);
        
        if (cellSpanColumn == column
                && cellSpanRow == row
                && cellSpanRowSpan == 1) {
            return SpanType.NORMAL_CELL;
        } else if (containsRowMinusOne
                && cellSpanColumnSpan > 1
                && cellSpanColumn != column
                && cellSpanRowSpan > 1
                && cellSpanRow != row) {
            return SpanType.BOTH_INVISIBLE;
        } else if (cellSpanRowSpan > 1
                && cellSpanColumn == column) {
            if (cellSpanRow == row || !containsRowMinusOne) {
                return SpanType.ROW_VISIBLE;
            } else {
                return SpanType.ROW_INVISIBLE;
            }
        } else if (cellSpanColumnSpan > 1
                && cellSpanColumn != column
                && (cellSpanRow == row || !containsRowMinusOne)) {
            return SpanType.COLUMN_INVISIBLE;
        } else {
            return SpanType.NORMAL_CELL;
        }
    }
    
    
    
    /***************************************************************************
     * 
     * Public Methods
     * 
     **************************************************************************/

    /**
     * Span in row the cell situated at rowIndex and colIndex by the number
     * count
     * 
     * @param count
     * @param rowIndex
     * @param colIndex
     */
    public void spanRow(int count, int rowIndex, int colIndex) {
        final int colSpan = rows.get(rowIndex).get(colIndex).getColumnSpan();
        final int rowSpan = count;
        rows.get(rowIndex).get(colIndex).setRowSpan(rowSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
                }
            }
        }
    }

    /**
     * Span in column the cell situated at rowIndex and colIndex by the number
     * count
     * 
     * @param count
     * @param rowIndex
     * @param colIndex
     */
    public void spanCol(int count, int rowIndex, int colIndex) {
        final int colSpan = count;
        final int rowSpan = rows.get(rowIndex).get(colIndex).getRowSpan();
        rows.get(rowIndex).get(colIndex).setColumnSpan(colSpan);
        for (int row = rowIndex; row < rowIndex + rowSpan && row < rowCount; ++row) {
            for (int col = colIndex; col < colIndex + colSpan
                    && col < columnCount; ++col) {
                if (row != rowIndex || col != colIndex) {
                    rows.get(row).set(col, rows.get(rowIndex).get(colIndex));
                }
            }
        }
    }

    /**
     * Set the rows used by the Grid.
     * the rowCount is then updated
     * @param rows
     */
    public void setRows(ObservableList<ObservableList<SpreadsheetCell<?>>> rows) {
        this.rows = rows;
        setRowCount(rows.size());
    }
    
    /**
     * Set the rows used by the Grid.
     * the rowCount is then updated
     * @param rows
     */
    public void setRows(ArrayList<ObservableList<SpreadsheetCell<?>>> rows) {
        this.rows = FXCollections.observableArrayList(rows);
        setRowCount(rows.size());
    }
    
    
    /**
     * Set a new rowCount for the grid.
     * @param rowCount
     */
    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    /**
     * Set a new columnCount for the grid.
     * @param columnCount
     */
    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

//    /**
//     * Debug function to print all the cells inside the grid.
//     * 
//     * @param grid
//     */
//    public void print(DataCell<?>[][] grid) {
//        for (int row = 0; row < rowCount; ++row) {
//            for (int column = 0; column < columnCount; ++column) {
//                System.out.print(grid[row][column].toString());
//            }
//            System.out.println("");
//        }
//    }
    
    
    
    
    /**
     * Indicate whether or not the row at the specified index is currently 
     * being displayed.
     * @param index
     * @return
     */
    private final boolean containsRow(final SpreadsheetView spv, int index){
        SpreadsheetViewSkin skin = SpreadsheetViewSkin.getSkin(spv);
        int size = skin.getCellsSize();
        for (int i = 0 ; i < size; ++i) {
            if(skin.getCell(i).getIndex() == index)
                return true;
        }
        return false;
    }
}
