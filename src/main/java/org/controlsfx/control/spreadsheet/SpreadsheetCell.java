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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import javafx.util.converter.DoubleStringConverter;

/**
 * The SpreadsheetCells serve as model for the {@link SpreadsheetView}. <br/>
 * You will provide these when constructing a {@link Grid}.
 * 
 * <br/>
 * 
 * <h3>Type of SpreadsheetCell:</h3> Different type of SpreadsheetCell are
 * available depending on the data you want to represent in your
 * {@link SpreadsheetView}. Each type has its own {@link SpreadsheetCellEditor}
 * in order to control very closely the possible modifications. <br/>
 * Those {@link CellType} are not directly accessible. You have to use the
 * different static method provided in {@link SpreadsheetCells} in order to
 * create the specialized SpreadsheetCell that suits your need: <br/>
 * 
 * <ul>
 * <li><b>String</b>: Accessible with
 * {@link SpreadsheetCells#createTextCell(int, int, int, int, String)} .</li>
 * <li><b>List</b>: Accessible with
 * {@link SpreadsheetCells#createListCell(int, int, int, int, java.util.List)} .
 * </li>
 * <li><b>Double</b>: Accessible with
 * {@link SpreadsheetCells#createDoubleCell(int, int, int, int, Double)} .</li>
 * <li><b>Date</b>: Accessible with
 * {@link SpreadsheetCells#createDateCell(int, int, int, int, java.time.LocalDate)}
 * .</li>
 * </ul>
 * <br/>
 * 
 * <h3>Configuration:</h3> You will have to indicate the coordinates of that
 * Cell together with the row and column Span. You can specify if you want that
 * cell to be editable or not using {@link #setEditable(boolean)}. Be advised
 * that a cell with a rowSpan means that the cell will replace all the cells
 * situated in the rowSpan range. Same with the column span. So the best way to
 * handle spanning is to fill your grid with unique cells, and then call
 * {@link GridBase#spanColumn(int, int, int)} or
 * {@link GridBase#spanRow(int, int, int)}. <br/>
 * 
 * 
 * <h3>Example:</h3>
 * 
 * Suppose you want to display some numbers in your SpreadsheetView. You will
 * fill your {@link Grid} using
 * {@link SpreadsheetCells#createDoubleCell(int, int, int, int, Double)}.
 * 
 * You will then be sure that your cells contain only {@link Double} value. If
 * the user wants to enter a {@link String}, the value will be ignored.
 * Moreover, the {@link SpreadsheetCellEditor} background color will turn red
 * when the value is incorrect to notify the user that his value will not be be
 * saved.
 * 
 * @see SpreadsheetCells
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 */
public class SpreadsheetCell<T> implements Serializable {

	/***************************************************************************
	 * 
	 * Static Fields
	 * 
	 **************************************************************************/
	private static final long serialVersionUID = -7648169794403402662L;

	/***************************************************************************
	 * 
	 * Private Fields
	 * 
	 **************************************************************************/

	private transient final SpreadsheetCellType<T> type;
	private final int row;
	private final int column;
	private transient int rowSpan;
	private transient int columnSpan;

	private String text;
	private transient BooleanProperty editable;

	/**
	 * Not serializable, it's transient right now because we don't need the
	 * style in copy/paste. But that option will be provided in the future. Help
	 * for that :
	 * http://www.oracle.com/technetwork/articles/java/javaserial-1536170.html
	 */
	private transient ObservableList<String> styleClass;

	/***************************************************************************
	 * 
	 * Constructor
	 * 
	 **************************************************************************/

	/**
	 * Constructs a SpreadsheetCell with the given configuration and a
	 * {@link CellType} set to String.
	 * 
	 * @param row
	 * @param column
	 * @param rowSpan
	 * @param columnSpan
	 */
	public SpreadsheetCell(final int row, final int column, final int rowSpan,
			final int columnSpan) {
		this(row, column, rowSpan, columnSpan, SpreadsheetCellType.OBJECT);
	}

	/**
	 * Constructs a SpreadsheetCell with the given configuration.
	 * 
	 * @see SpreadsheetCells
	 * @param row
	 * @param column
	 * @param rowSpan
	 * @param columnSpan
	 * @param type
	 */
	@SuppressWarnings("unchecked")
	public SpreadsheetCell(final int row, final int column, final int rowSpan,
			final int columnSpan, final SpreadsheetCellType<?> type) {
		this.row = row;
		this.column = column;
		this.rowSpan = rowSpan;
		this.columnSpan = columnSpan;
		this.type = (SpreadsheetCellType<T>) type;
		text = "";
		editable = new SimpleBooleanProperty(true);
	}

	/***************************************************************************
	 * 
	 * Abstract Methods
	 * 
	 **************************************************************************/

	/**
	 * Verify that the upcoming cell value can be set to the current cell. If
	 * it's possible, the cell's value is changed. If not, nothing is done. This
	 * is currently used by the Copy/Paste.
	 * 
	 * @param cell
	 */
	public void match(SpreadsheetCell<?> cell) {
		type.copy(cell, this);
	}

	/***************************************************************************
	 * 
	 * Properties
	 * 
	 ***************************************************************************/

	// --- item
	private transient ObjectProperty<T> item = new SimpleObjectProperty<T>(
			this, "item") {
		@Override
		protected void invalidated() {
			updateText();
		}
	};

	public final void setItem(T value) {
		item.set(value);
	}

	public final T getItem() {
		return item.get();
	}

	public final ObjectProperty<T> itemProperty() {
		return item;
	}

	/***************************************************************************
	 * 
	 * Public Methods
	 * 
	 **************************************************************************/

	public String getText() {
		return text;
	}

	/**
	 * Return the {@link CellType} of this particular cell.
	 * 
	 * @return the {@link CellType} of this particular cell.
	 */
	public SpreadsheetCellType<?> getCellType() {
		return type;
	}

	/**
	 * Return the row of this cell.
	 * 
	 * @return the row of this cell.
	 */
	public int getRow() {
		return row;
	}

	/**
	 * Return the column of this cell.
	 * 
	 * @return the column of this cell.
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Return how much this cell is spanning in row, 1 is normal.
	 * 
	 * @return how much this cell is spanning in row, 1 is normal.
	 */
	public int getRowSpan() {
		return rowSpan;
	}

	/**
	 * Sets how much this cell is spanning in row. See {@link SpreadsheetCell}
	 * description for information. You should use
	 * {@link GridBase#spanRow(int, int, int)} instead of using this method
	 * directly.
	 * 
	 * @param rowSpan
	 */
	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	/**
	 * Return how much this cell is spanning in column, 1 is normal.
	 * 
	 * @param rowSpan
	 */
	public int getColumnSpan() {
		return columnSpan;
	}

	/**
	 * Sets how much this cell is spanning in column. See
	 * {@link SpreadsheetCell} description for information. You should use
	 * {@link GridBase#spanColumn(int, int, int)} instead of using this method
	 * directly.
	 * 
	 * @param rowSpan
	 */
	public void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}

	/**
	 * Return an ObservableList of String of all the style class associated with
	 * this cell. You can easily modify its appearance by adding a style class
	 * (previously set in CSS).
	 * 
	 * @return
	 */
	public ObservableList<String> getStyleClass() {
		if (styleClass == null) {
			styleClass = FXCollections.observableArrayList();
		}
		return styleClass;
	}

	/**
	 * Return if this cell can be edited or not.
	 * 
	 * @return true if this cell is editable.
	 */
	public boolean isEditable() {
		return editable.get();
	}

	/**
	 * Change the editable state of this cell
	 * 
	 * @param readOnly
	 */
	public void setEditable(boolean readOnly) {
		editable.set(readOnly);
	}

	/**
	 * The {@link BooleanProperty} linked with the editable state.
	 * 
	 * @return
	 */
	public BooleanProperty editableProperty() {
		return editable;
	}

	// A map containing a set of properties for this cell
	private transient ObservableMap<Object, Object> properties;

	/**
	 * Returns an observable map of properties on this node for use primarily by
	 * application developers.
	 * 
	 * @return an observable map of properties on this node for use primarily by
	 *         application developers
	 */
	public final ObservableMap<Object, Object> getProperties() {
		if (properties == null) {
			properties = FXCollections.observableHashMap();
		}
		return properties;
	}

	/**
	 * Tests if Node has properties.
	 * 
	 * @return true if node has properties.
	 */
	public boolean hasProperties() {
		return properties != null && !properties.isEmpty();
	}

	/***************************************************************************
	 * 
	 * Overridden Methods
	 * 
	 **************************************************************************/

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "cell[" + row + "][" + column + "]" + rowSpan + "-" + columnSpan;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SpreadsheetCell))
			return false;

		final SpreadsheetCell<?> cell = (SpreadsheetCell<?>) obj;
		if (cell != null && cell.getRow() == row && cell.getColumn() == column
				&& cell.getText().equals(text)) {
			return true;
		} else {
			return false;
		}
	}

	/***************************************************************************
	 * 
	 * Private Implementation
	 * 
	 **************************************************************************/

	private void updateText() {
		this.text = type.getConverter().toString(getItem());
	}
}
