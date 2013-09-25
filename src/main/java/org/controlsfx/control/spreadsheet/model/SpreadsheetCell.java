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
package org.controlsfx.control.spreadsheet.model;

import java.io.Serializable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.controlsfx.control.SpreadsheetView;


/**
 * The model cell that hold the data. It has all the information needed by the
 * View.
 * 
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
        STRING, ENUM, DOUBLE, DATE, SPLITTER;
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
