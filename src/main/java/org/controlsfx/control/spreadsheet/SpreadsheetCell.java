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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * The SpreadsheetCells serve as model for the {@link SpreadsheetView}. <br/>
 * You will provide them when constructing a {@link Grid}.
 * 
 * <br/>
 * 
 * <h3>SpreadsheetCell Types</h3> 
 * Each SpreadsheetCell has its own {@link SpreadsheetCellType}.
 * Different {@link SpreadsheetCellType SpreadsheetCellTypes} are
 * available depending on the data you want to represent in your
 * {@link SpreadsheetView}. MoreOver, each {@link SpreadsheetCellType} has its 
 * own {@link SpreadsheetCellEditor} in order to control very closely the possible 
 * modifications.
 * 
 * <p>You can use the different static method provided in 
 * {@link SpreadsheetCellType} in order to create the specialized 
 * SpreadsheetCell that suits your need:
 * 
 * <ul>
 * <li><b>String</b>: Accessible with
 * {@link SpreadsheetCellType.StringType#createCell(int, int, int, int, String)}.</li>
 * <li><b>List</b>: Accessible with
 * {@link SpreadsheetCellType.ListType#createCell(int, int, int, int, String)}.
 * </li>
 * <li><b>Double</b>: Accessible with
 * {@link  SpreadsheetCellType.DoubleType#createCell(int, int, int, int, Double)}.</li>
 * <li><b>Date</b>: Accessible with
 * {@link  SpreadsheetCellType.DateType#createCell(int, int, int, int, LocalDate)}.</li>
 * </ul>
 * <br/>
 * 
 * <p>If you want to create a SpreadsheetCell of your own, you simply have to create 
 * your own {@link  SpreadsheetCellType} and implement the abstract method 
 * {@link  SpreadsheetCellType#createCell(int, int, int, int, Object)}.
 * You will also have to provide a custom {@link SpreadsheetCellEditor}.
 * 
 * <h3>Configuration</h3>
 * You will have to indicate the coordinates of the cell together with the 
 * {@link #setRowSpan(int) row} and {@link #setColumnSpan(int) column} span. You 
 * can specify if you want the cell to be editable or not using 
 * {@link #setEditable(boolean)}. Be advised that a cell with a rowSpan means 
 * that the cell will replace all the cells situated in the rowSpan range. 
 * Same with the column span. The best way to handle spanning is to fill your 
 * grid with unique cells, and then call
 * {@link GridBase#spanColumn(int, int, int)} or
 * {@link GridBase#spanRow(int, int, int)}.
 * 
 * 
 * <h3>Examples</h3>
 * Here is an example that uses all the pre-built {@link SpreadsheetCellType}
 * types. The generation is random here so you will want to replace the logic to
 * suit your needs.
 * 
 * <pre>
 * private SpreadsheetCell&lt;?&gt; generateCell(int row, int column, int rowSpan, int colSpan) {
 *     List&lt;String?&gt; stringListTextCell = Arrays.asList("Shanghai","Paris","New York City","Bangkok","Singapore","Johannesburg","Berlin","Wellington","London","Montreal");
 *     final double random = Math.random();
 *     if (random &lt; 0.10) {
 *         List&lt;String?&gt; stringList = Arrays.asList("China","France","New Zealand","United States","Germany","Canada");
 *         cell = SpreadsheetCellType.LIST(stringList).createCell(row, column, rowSpan, colSpan, null);
 *     } else if (random ?&gt;= 0.10 && random &lt; 0.25) {
 *         cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan,stringListTextCell.get((int)(Math.random()*10)));
 *     } else if (random ?&gt;= 0.25 && random &lt; 0.75) {
 *         cell = SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, colSpan,(double)Math.round((Math.random()*100)*100)/100);
 *     } else {
 *         cell = SpreadsheetCellType.DATE.createCell(row, column, rowSpan, colSpan, LocalDate.now().plusDays((int)(Math.random()*10)));
 *     }
 *     return cell;
 * }
 * </pre>
 * 
 * <p>When you are using {@link SpreadsheetCellType.DoubleType},
 * you will then be sure that your cells contain only {@link Double} value. If
 * the user wants to enter a {@link String}, the value will be ignored.
 * Moreover, the {@link SpreadsheetCellEditor} background color will turn red
 * when the value is incorrect to notify the user that his value will not be be
 * saved.
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 */
public class SpreadsheetCell implements Serializable {

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

	@SuppressWarnings("rawtypes")
	private transient final SpreadsheetCellType type;
	private final int row;
	private final int column;
	private transient int rowSpan;
	private transient int columnSpan;

	private String text;

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

	@SuppressWarnings("rawtypes")
	public SpreadsheetCell(final int row, final int column, final int rowSpan,
			final int columnSpan) {
		this(row, column, rowSpan, columnSpan, (SpreadsheetCellType)SpreadsheetCellType.OBJECT);
	}

	/**
	 * Constructs a SpreadsheetCell with the given configuration.
	 * 
	 * @param row
	 * @param column
	 * @param rowSpan
	 * @param columnSpan
	 * @param type
	 */
	public SpreadsheetCell(final int row, final int column, final int rowSpan,
			final int columnSpan, final SpreadsheetCellType<?> type) {
		this.row = row;
		this.column = column;
		this.rowSpan = rowSpan;
		this.columnSpan = columnSpan;
		this.type = type;
		text = "";
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
	public boolean match(SpreadsheetCell cell) {
		return type.copy(cell, this);
	}
	
	

	/***************************************************************************
	 * 
	 * Properties
	 * 
	 ***************************************************************************/

	// --- item
	private transient ObjectProperty<Object> item = new SimpleObjectProperty<Object>(this, "item") {
		@Override protected void invalidated() {
			updateText();
		}
	};

    /**
     * Sets the value of the property Item
     * If {@link #isEditable()} return false, nothing is done.
     * @param value
     */
	public final void setItem(Object value) {
		if(isEditable())
			item.set(value);
	}

	// auto-generated JavaDoc
	public final Object getItem() {
		return item.get();
	}

	/**
	 * The item property represents the currently-set value inside this
	 * SpreadsheetCell instance.
	 */
	public final ObjectProperty<?> itemProperty() {
		return item;
	}
	
	
	// --- editable
	private transient BooleanProperty editable;
	
	/**
     * Return if this cell can be edited or not.
     * 
     * @return true if this cell is editable.
     */
    public final boolean isEditable() {
        return editable == null ? true : editable.get();
    }

    /**
     * Change the editable state of this cell
     * 
     * @param readOnly
     */
    public final void setEditable(boolean readOnly) {
        editableProperty().set(readOnly);
    }

    /**
     * The {@link BooleanProperty} linked with the editable state.
     * 
     * @return The {@link BooleanProperty} linked with the editable state.
     */
    public final BooleanProperty editableProperty() {
        if (editable == null) {
            editable = new SimpleBooleanProperty(this, "editable", true);
        }
        return editable;
    }

	
	
	/***************************************************************************
	 * 
	 * Public Methods
	 * 
	 **************************************************************************/

	/**
	 * Return the String representation currently used for display
	 * in the {@link SpreadsheetView}.
	 * @return text representation of the value.
	 */
	public final String getText() {
		return text;
	}

	/**
	 * Return the {@link SpreadsheetCellType} of this particular cell.
	 * 
	 * @return the {@link SpreadsheetCellType} of this particular cell.
	 */
	public final SpreadsheetCellType<?> getCellType() {
		return type;
	}

	/**
	 * Return the row of this cell.
	 * 
	 * @return the row of this cell.
	 */
	public final int getRow() {
		return row;
	}

	/**
	 * Return the column of this cell.
	 * 
	 * @return the column of this cell.
	 */
	public final int getColumn() {
		return column;
	}

	/**
	 * Return how much this cell is spanning in row, 1 is normal.
	 * 
	 * @return how much this cell is spanning in row, 1 is normal.
	 */
	public final int getRowSpan() {
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
	public final void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	/**
	 * Return how much this cell is spanning in column, 1 is normal.
	 * @return how much this cell is spanning in column, 1 is normal.
	 */
	public final int getColumnSpan() {
		return columnSpan;
	}

	/**
	 * Sets how much this cell is spanning in column. See
	 * {@link SpreadsheetCell} description for information. You should use
	 * {@link GridBase#spanColumn(int, int, int)} instead of using this method
	 * directly.
	 * 
	 * @param columnSpan
	 */
	public final void setColumnSpan(int columnSpan) {
		this.columnSpan = columnSpan;
	}

	/**
	 * Return an ObservableList of String of all the style class associated with
	 * this cell. You can easily modify its appearance by adding a style class
	 * (previously set in CSS).
	 * 
	 * @return an ObservableList of String of all the style class
	 */
	public final ObservableList<String> getStyleClass() {
		if (styleClass == null) {
			styleClass = FXCollections.observableArrayList();
		}
		return styleClass;
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
	public final boolean hasProperties() {
		return properties != null && !properties.isEmpty();
	}

	
	
	/***************************************************************************
	 * 
	 * Overridden Methods
	 * 
	 **************************************************************************/

	/** {@inheritDoc} */
	@Override public String toString() {
		return "cell[" + row + "][" + column + "]" + rowSpan + "-" + columnSpan;
	}

	/** {@inheritDoc} */
	@Override public boolean equals(Object obj) {
		if (!(obj instanceof SpreadsheetCell))
			return false;

		final SpreadsheetCell cell = (SpreadsheetCell) obj;
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

	/**
	 * Update the text for the SpreadsheetView.
	 */
	@SuppressWarnings("unchecked")
	private void updateText() {
		this.text = type.toString(getItem());
	}

}
