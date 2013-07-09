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


/**
 * The model cell that hold the data. It has all the information needed by the View.
 * 
 * 
 */
public abstract class DataCell<T> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7648169794403402662L;

	public static enum CellType {
		STRING,
		ENUM,
		NUMBER,
		READ_ONLY,
		DATE,
		SPLITTER;
	}
	protected String styleCss;


	protected CellType type;
	protected String str;

	public void setStr(String str) {
		this.str = str;
		//		setStyleCss("cell_modified");
	}

	public String getStr() {
		return str;
	}
	private int row, column, rowSpan, columnSpan;

	public abstract void setCellValue(T value);

	public abstract T getCellValue();

	public CellType getCellType() {
		return type;
	}

	public void setCellType(CellType type) {
		if(this.type != null && !this.type.equals(CellType.SPLITTER)){
			if(type.equals(CellType.READ_ONLY)){
				this.setStyleCss("fixed");
			}else if(type.equals(CellType.SPLITTER)){
				this.setStyleCss("splitter");
			}
			this.type = type;
		}
	}

	public DataCell(int r, int c, int rs, int cs) {
		super();
		row = r;
		column = c;
		rowSpan = rs;
		columnSpan = cs;
		str="";
	}

	@Override
	public String toString() {
		return "cell[" + row + "][" + column + "]" + rowSpan + "-" + columnSpan;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
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
	public String getStyleCss() {
		return styleCss;
	}

	public void setStyleCss(String style) {
		this.styleCss = style;
	}

	public boolean equals(DataCell<?> cell){
		if(cell != null && cell.getRow() == row && cell.getColumn() == column && cell.getStr().equals(str)) {
			return true;
		} else {
			return false;
		}
	}

	public abstract void match(DataCell<?> cell);
}


