package org.controlsfx.control.spreadsheet.model;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class GridBase implements Grid {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    private ObservableList<ObservableList<SpreadsheetCell>> rows;
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
        this(rowCount, columnCount,FXCollections.<ObservableList<SpreadsheetCell>> emptyObservableList());
    }

    public GridBase(int rowCount, int columnCount, ObservableList<ObservableList<SpreadsheetCell>> rows) {
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
    @Override public ObservableList<ObservableList<SpreadsheetCell>> getRows() {
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
    public void setRows(ObservableList<ObservableList<SpreadsheetCell>> rows) {
        this.rows = rows;
        setRowCount(rows.size());
    }
    
    /**
     * Set the rows used by the Grid.
     * the rowCount is then updated
     * @param rows
     */
    public void setRows(ArrayList<ObservableList<SpreadsheetCell>> rows) {
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
}
