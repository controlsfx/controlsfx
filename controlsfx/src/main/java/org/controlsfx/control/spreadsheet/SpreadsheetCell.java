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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.Node;

/**
 * The SpreadsheetCells serve as model for the {@link SpreadsheetView}. <br/>
 * You will provide them when constructing a {@link Grid}.
 * 
 * <br/>
 * <h3>SpreadsheetCell Types</h3> Each SpreadsheetCell has its own
 * {@link SpreadsheetCellType} which has its own {@link SpreadsheetCellEditor}
 * in order to control very closely the possible modifications.
 * 
 * <p>
 * Different {@link SpreadsheetCellType SpreadsheetCellTypes} are available
 * depending on the data you want to represent in your {@link SpreadsheetView}.
 * You can use the different static method provided in
 * {@link SpreadsheetCellType} in order to create the specialized
 * SpreadsheetCell that suits your need.
 * 
 * 
 * <br/>
 * 
 * <p>
 * If you want to create a SpreadsheetCell of your own, you simply have to
 * create your own {@link SpreadsheetCellType} and implement the abstract method
 * {@link SpreadsheetCellType#createCell(int, int, int, int, Object)}. You will
 * also have to provide a custom {@link SpreadsheetCellEditor}.
 * 
 * <h2>Configuration</h2>
 * You will have to indicate the coordinates of the cell together with the
 * {@link #setRowSpan(int) row} and {@link #setColumnSpan(int) column} span. You
 * can specify if you want the cell to be editable or not using
 * {@link #setEditable(boolean)}. Be advised that a cell with a rowSpan means
 * that the cell will replace all the cells situated in the rowSpan range. Same
 * with the column span. The best way to handle spanning is to fill your grid
 * with unique cells, and then call {@link GridBase#spanColumn(int, int, int)}
 * or {@link GridBase#spanRow(int, int, int)}. <br>
 * 
 * <h3>Format</h3>
 * Your cell can have its very own format. If you want to display some dates
 * with different format, you just have to create a unique
 * {@link SpreadsheetCellType} and then specify for each cell their format with
 * {@link #setFormat(String)}. You will then have the guaranty that all your
 * cells will have a LocalDate as a value, but the value will be displayed
 * differently for each cell. This will also guaranty that copy/paste and other
 * operation will be compatible since every cell will share the same
 * {@link SpreadsheetCellType}. <br>
 * Here an example : <br>
 * 
 * 
 * <pre>
 * SpreadsheetCell cell = SpreadsheetCellType.DATE.createCell(row, column, rowSpan, colSpan,
 *         LocalDate.now().plusDays((int) (Math.random() * 10))); // Random value
 * // given here
 * final double random = Math.random();
 * if (random &lt; 0.25) {
 *     cell.setFormat(&quot;EEEE d&quot;);
 * } else if (random &lt; 0.5) {
 *     cell.setFormat(&quot;dd/MM :YY&quot;);
 * } else {
 *     cell.setFormat(&quot;dd/MM/YYYY&quot;);
 * }
 * </pre>
 * 
 * <center><img src="dateFormat.png"></center>
 * 
 * <h3>Graphic</h3>
 * Each cell can have a graphic to display next to the text in the cells. Just
 * use the {@link #setGraphic(Node)} in order to specify the graphic you want.
 * Be aware that no verification are made on the Image. So reduce wisely your
 * image to fit the exact space available on your grid or the result will be
 * incoherent.
 * 
 * For example :
 * 
 * <pre>
 * cell.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(&quot;icons/exclamation.png&quot;))));
 * </pre>
 * 
 * <center><img src="graphicNodeToCell.png"></center> <br>
 * In addition to that, you can also specify another graphic property to your
 * cell with {@link #commentedProperty()}. This allow you to specify whether
 * this cell has or not a unique property (here a comment). Therefore, you will
 * have a visual feedback for every cell that has that property set to true.
 * Right now it's a little red triangle but you can modify this in your CSS by
 * using the "<b>comment</b>" style class.
 * 
 * <pre>
 * .comment{
 *     -fx-background-color: red;
 *     -fx-shape : "M 0 0 L -1 0 L 0 1 z";
 * }
 * </pre>
 * 
 * <center><img src="triangleCell.png"></center>
 * 
 * <h3>Style with CSS</h3>
 * You can style your cell by specifying some styleClass with
 * {@link #getStyleClass()}. You just have to create and custom that class in
 * your CSS stylesheet associated with your {@link SpreadsheetView}. Also note
 * that all {@link SpreadsheetCell} have a "<b>spreadsheet-cell</b>" styleClass
 * added by default. Here is a example :<br>
 * 
 * <pre>
 * cell.getStyleClass().add(&quot;row_header&quot;);
 * </pre>
 * 
 * And in the CSS:
 * 
 * <pre>
 *  .spreadsheet-cell.row_header{
 *     -fx-background-color: #b4d4ad ;
 *     -fx-background-insets: 0, 0 1 1 0;
 *     -fx-alignment: center;
 * }
 * </pre>
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
 * <p>
 * When you are using {@link SpreadsheetCellType.DoubleType}, you will then be
 * sure that your cells contain only {@link Double} value. If the user wants to
 * enter a {@link String}, the value will be ignored. Moreover, the
 * {@link SpreadsheetCellEditor} background color will turn red when the value
 * is incorrect to notify the user that his value will not be be saved.
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 * @see SpreadsheetCellType
 */
public class SpreadsheetCell {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/

    @SuppressWarnings("rawtypes")
    private final SpreadsheetCellType type;
    private final int row;
    private final int column;
    private int rowSpan;
    private int columnSpan;
    private final StringProperty format;
    private final StringProperty text;
    private final ObjectProperty<Node> graphic;

    private ObservableSet<String> styleClass;

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    @SuppressWarnings("rawtypes")
    public SpreadsheetCell(final int row, final int column, final int rowSpan, final int columnSpan) {
        this(row, column, rowSpan, columnSpan, (SpreadsheetCellType) SpreadsheetCellType.OBJECT);
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
    public SpreadsheetCell(final int row, final int column, final int rowSpan, final int columnSpan,
            final SpreadsheetCellType<?> type) {
        this.row = row;
        this.column = column;
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
        this.type = type;
        text = new SimpleStringProperty("");
        format = new SimpleStringProperty("");
        graphic = new SimpleObjectProperty<>();
        format.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                updateText();
            }
        });
        getStyleClass().add("spreadsheet-cell");
    }

    /***************************************************************************
     * 
     * Abstract Methods
     * 
     **************************************************************************/

    /**
     * Verify that the upcoming cell value can be set to the current cell. This
     * is currently used by the Copy/Paste.
     * 
     * @param cell
     * @return true if the upcoming cell value can be set to the current cell.
     */
    public boolean match(SpreadsheetCell cell) {
        return type.match(cell);
    }

    /***************************************************************************
     * 
     * Properties
     * 
     ***************************************************************************/

    // --- item
    private final ObjectProperty<Object> item = new SimpleObjectProperty<Object>(this, "item") {
        @Override
        protected void invalidated() {
            updateText();
        }
    };

    /**
     * Sets the value of the property Item. This should be used only at
     * initialization. Prefer {@link Grid#setCellValue(int, int, Object)} after
     * because it will compute correctly the modifiedCell. If
     * {@link #isEditable()} return false, nothing is done.
     * 
     * @param value
     */
    public final void setItem(Object value) {
        if (isEditable())
            item.set(value);
    }

    /**
     * Return the value contained in the cell.
     * 
     * @return the value contained in the cell.
     */
    public final Object getItem() {
        return item.get();
    }

    /**
     * The item property represents the currently-set value inside this
     * SpreadsheetCell instance.
     * 
     * @return the item property which contains the value.
     */
    public final ObjectProperty<Object> itemProperty() {
        return item;
    }

    // --- editable
    private BooleanProperty editable;

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

    // --- comment
    private final BooleanProperty commented = new SimpleBooleanProperty(this, "commented", false);

    /**
     * Return if this cell has a comment or not.
     * 
     * @return true if this cell has a comment.
     */
    public final boolean isCommented() {
        return commented == null ? true : commented.get();
    }

    /**
     * Change the commented state of this cell.
     * 
     * @param flag
     */
    public final void setCommented(boolean flag) {
        commentedProperty().set(flag);
    }

    /**
     * The {@link BooleanProperty} linked with the commented state.
     * 
     * @return The {@link BooleanProperty} linked with the commented state.
     */
    public final BooleanProperty commentedProperty() {
        return commented;
    }

    /**
     * The {@link StringProperty} linked with the format.
     * 
     * @return The {@link StringProperty} linked with the format state.
     */
    public final StringProperty formatProperty() {
        return format;
    }

    /**
     * Return the format of this cell or an empty string if no format has been
     * specified.
     * 
     * @return Return the format of this cell or an empty string if no format
     *         has been specified.
     */
    public final String getFormat() {
        return format.get();
    }

    /**
     * Set a new format for this Cell. You can specify how to represent the
     * value in the cell.
     * 
     * @param format
     */
    public final void setFormat(String format) {
        formatProperty().set(format);
        updateText();
    }

    /***************************************************************************
     * 
     * Public Methods
     * 
     **************************************************************************/

    /**
     * Return the StringProperty of the representation of the value.
     * 
     * @return the StringProperty of the representation of the value.
     */
    public final ReadOnlyStringProperty textProperty() {
        return text;
    }

    /**
     * Return the String representation currently used for display in the
     * {@link SpreadsheetView}.
     * 
     * @return text representation of the value.
     */
    public final String getText() {
        return text.get();
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
     * 
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
    public final ObservableSet<String> getStyleClass() {
        if (styleClass == null) {
            styleClass = FXCollections.observableSet();
        }
        return styleClass;
    }

    // A map containing a set of properties for this cell
    private ObservableMap<Object, Object> properties;

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

    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    /**
     * Set a graphic for this cell to display aside with the text.
     * 
     * @param graphic
     */
    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    /**
     * Return the graphic node associated with this cell. Return null if nothing
     * has been associated.
     * 
     * @return the graphic node associated with this cell.
     */
    public Node getGraphic() {
        return graphic.get();
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

        final SpreadsheetCell cell = (SpreadsheetCell) obj;
        return cell != null && cell.getRow() == row && cell.getColumn() == column && cell.getText().equals(getText());
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
        if (!("").equals(getFormat())) {
            text.setValue(type.toString(getItem(), getFormat()));
        } else {
            text.setValue(type.toString(getItem()));
        }
    }
}
