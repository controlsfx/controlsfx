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
package org.controlsfx.control.spreadsheet;

import java.io.Serializable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.control.SpreadsheetView;


/**
 * The SpreadsheetCells serve as model for the {@link SpreadsheetView}. 
 * <br/>
 * You will provide these when constructing a {@link Grid}.
 * 
 * <br/>
 *  
 * <h3>Type of SpreadsheetCell: </h3>
 * Different type of SpreadsheetCell are available depending on the data you want to represent in your {@link SpreadsheetView}. 
 * Each type has its own {@link SpreadsheetCellEditor} in order to control very closely the possible modifications.
 * <br/>
 * Those {@link CellType} are not directly accessible. You have to use the different static method provided in {@link SpreadsheetCells}
 * in order to create the specialized SpreadsheetCell that suits your need:
 * <br/>
 * 
 * <ul>
 *   <li> String: Accessible with {@link SpreadsheetCells#createTextCell(int, int, int, int, String)} .</li>
 *   <li> List: Accessible with {@link SpreadsheetCells#createListCell(int, int, int, int, java.util.List)} .</li>
 *   <li> Double: Accessible with {@link SpreadsheetCells#createDoubleCell(int, int, int, int, Double)} .</li>
 *   <li> Date: Accessible with {@link SpreadsheetCells#createDateCell(int, int, int, int, java.time.LocalDate)} .</li>
 * </ul>
 * <br/>
 * 
 * <h3>Configuration: </h3>
 * You will have to indicate the coordinates of that Cell together with the row and column Span. You can specify if you want that cell to be editable or not
 * using {@link #setEditable(boolean)}.
 * Be advised that a cell with a rowSpan means that the cell will replace all the cells situated in the rowSpan range. Same with the column span.
 * So the best way to handle spanning is to fill your grid with unique cells, and then call {@link GridBase#spanColumn(int, int, int)} or 
 * {@link GridBase#spanRow(int, int, int)}.
 * <br/>
 * 
 * 
 * <h3>Example: </h3>
 * 
 * Suppose you want to display some numbers in your SpreadsheetView. You will fill your {@link Grid} 
 * using {@link SpreadsheetCells#createDoubleCell(int, int, int, int, Double)}.
 * 
 * You will then be sure that your cells contain only {@link Double} value. If the user wants to enter a {@link String}, the value will be ignored.
 * Moreover, the {@link SpreadsheetCellEditor} background color will turn red when the value is incorrect to notify the user that his value will
 * not be be saved.
 * 
 * @see SpreadsheetCells 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 */
public abstract class SpreadsheetCell<T> implements Serializable {

    /***************************************************************************
     * 
     * Static Fields
     * 
     **************************************************************************/
    private static final long serialVersionUID = -7648169794403402662L;

    /**
     * An enumeration to represent the types of cell available in the
     * {@link SpreadsheetView} control.
     */
    public static enum CellType {
        STRING, LIST, DOUBLE, DATE;
    }
    
    
    
    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/

    private final CellType type;
    private final int row;
    private final int column;
    private int rowSpan;
    private int columnSpan;
    
    protected String str;
    private boolean editable;
    
    /**
     * Not serializable, it's transient right now because
     * we don't need the style in copy/paste. But that option
     * will be provided in the future. 
     * Help for that : http://www.oracle.com/technetwork/articles/java/javaserial-1536170.html
     */
    private transient ObservableList<String> styleClass;
    
    
    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    public SpreadsheetCell(final int row, final int column, final int rowSpan, final int columnSpan) {
        this(row, column, rowSpan, columnSpan, null);
    }
    
    public SpreadsheetCell(final int row, final int column, final int rowSpan, final int columnSpan, final CellType type) {
        this.row = row;
        this.column = column;
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
        this.type = type == null ? CellType.STRING : type;
        str = "";
        editable = true;
    }
    
    
    
    /***************************************************************************
    *
    * Abstract Methods
    * 
    **************************************************************************/

    public abstract void setCellValue(T value);

    public abstract T getCellValue();
    
    /**
     * Verify that the upcoming cell value can be set to the current cell.
     * If it's possible, the cell's value is changed.
     * If not, nothing is done.
     * This is currently used by the Copy/Paste.
     * @param cell
     */
    public abstract void match(SpreadsheetCell<?> cell);
    
    
    
    /***************************************************************************
     *
     * Public Methods
     * 
     **************************************************************************/

    public void setStr(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public CellType getCellType() {
        return type;
    }
    
    @Override
    public String toString() {
        return "cell[" + row + "][" + column + "]" + rowSpan + "-" + columnSpan;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    public int getColumnSpan() {
        return columnSpan;
    }

    public void setColumnSpan(int columnSpan) {
        this.columnSpan = columnSpan;
    }
    
    public ObservableList<String> getStyleClass() {
        if (styleClass == null) {
            styleClass = FXCollections.observableArrayList();
        }
        return styleClass;
    }

    public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean readOnly) {
		this.editable = readOnly;
	}
	
    /**
     * @param cell
     * @return
     */
    public boolean equals(SpreadsheetCell<?> cell) {
        if (cell != null && cell.getRow() == row && cell.getColumn() == column
                && cell.getStr().equals(str)) {
            return true;
        } else {
            return false;
        }
    }
}
