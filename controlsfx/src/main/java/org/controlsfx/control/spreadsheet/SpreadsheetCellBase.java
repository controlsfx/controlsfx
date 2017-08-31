/**
 * Copyright (c) 2013, 2016 ControlsFX
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

import com.sun.javafx.event.EventHandlerManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;

/**
 * The SpreadsheetCells serve as model for the {@link SpreadsheetView}. <br>
 * You will provide them when constructing a {@link Grid}.
 * 
 * <br>
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
 * <br>
 * 
 * <p>
 * If you want to create a SpreadsheetCell of your own, you simply have to
 * use one of the provided constructor. Usually you will let your {@link SpreadsheetCellType}
 * create the cells. For example 
 * {@link SpreadsheetCellType.StringType#createCell(int, int, int, int, java.lang.String) }.
 * You will also have to provide a custom {@link SpreadsheetCellEditor}.
 * 
 * <h2>Configuration</h2>
 * You will have to indicate the coordinates of the cell together with the
 * {@link #setRowSpan(int) row} and {@link #setColumnSpan(int) column} span. You
 * can specify if you want the cell to be editable or not using
 * {@link #setEditable(boolean)}. Be advised that a cell with a rowSpan means
 * that the cell will replace all the cells situated in the rowSpan range. Same
 * with the column span. 
 * <br>
 * So the best way to handle spanning is to fill your grid
 * with unique cells, and then call at the end {@link GridBase#spanColumn(int, int, int)}
 * or {@link GridBase#spanRow(int, int, int)}. These methods will handle the span
 * for you.
 * 
 * <br>
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
 * <center><img src="dateFormat.PNG" alt="SpreadsheetCellBase with custom format"></center>
 * 
 * <h3>Popup</h3>
 * Each cell can display a {@link Popup} when clicked. This is useful when some
 * non editable cell wants to display several actions to take on the grid. This
 * feature is completely different from the {@link Filter}. Filters are shown on
 * one particular row whereas popup can be added to every cell.
 *
 * 
 * <h3>Graphic</h3>
 * Each cell can have a graphic to display next to the text in the cells. Just
 * use the {@link #setGraphic(Node)} in order to specify the graphic you want.
 * If you specify an {@link ImageView}, the SpreadsheetView will try to resize it in
 * order to fit the space available in the cell.
 * 
 * For example :
 * 
 * <pre>
 * cell.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(&quot;icons/exclamation.png&quot;))));
 * </pre>
 * 
 * <center><img src="graphicNodeToCell.png" alt="SpreadsheetCellBase with graphic"></center> <br>
 * In addition to that, you can also specify another graphic property to your
 * cell with {@link #activateCorner(org.controlsfx.control.spreadsheet.SpreadsheetCell.CornerPosition) }.
 * This allow you to activate or deactivate some graphics on the cell in every 
 * corner. Right now it's a little red triangle but you can modify this in your CSS by
 * using the "<b>cell-corner</b>" style class.
 * 
 * <pre>
 * .cell-corner.top-left{
 *     -fx-background-color: red;
 *     -fx-shape : "M 0 0 L 1 0 L 0 1 z";
 * }
 * </pre>
 * 
 * <center><img src="triangleCell.PNG" alt="SpreadsheetCellBase with a styled cell-corner"></center>
 * 
 * 
 * <br>
 * You can also customize the tooltip of your SpreadsheetCell by specifying one
 * with {@link #setTooltip(java.lang.String) }.
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
 *     List&lt;String&gt; stringListTextCell = Arrays.asList("Shanghai","Paris","New York City","Bangkok","Singapore","Johannesburg","Berlin","Wellington","London","Montreal");
 *     final double random = Math.random();
 *     if (random &lt; 0.10) {
 *         List&lt;String&gt; stringList = Arrays.asList("China","France","New Zealand","United States","Germany","Canada");
 *         cell = SpreadsheetCellType.LIST(stringList).createCell(row, column, rowSpan, colSpan, stringList.get((int) (Math.random() * 6)));
 *     } else if (random &gt;= 0.10 &amp;&amp; random &lt; 0.25) {
 *         cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan,stringListTextCell.get((int)(Math.random()*10)));
 *     } else if (random &gt;= 0.25 &amp;&amp; random &lt; 0.75) {
 *         cell = SpreadsheetCellType.DOUBLE.createCell(row, column, rowSpan, colSpan,(double)Math.round((Math.random()*100)*100)/100);
 *     } else {
 *         cell = SpreadsheetCellType.DATE.createCell(row, column, rowSpan, colSpan, LocalDate.now().plusDays((int)(Math.random()*10)));
 *     }
 *     return cell;
 * }
 * </pre>
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditor
 * @see SpreadsheetCellType
 */
public class SpreadsheetCellBase implements SpreadsheetCell, EventTarget{

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/

    //The Bit position for the editable Property.
    private static final int EDITABLE_BIT_POSITION = 4;
    private static final int WRAP_BIT_POSITION = 5;
    private static final int POPUP_BIT_POSITION = 6;
    private final SpreadsheetCellType type;
    private final int row;
    private final int column;
    private int rowSpan;
    private int columnSpan;
    private final StringProperty format;
    private final StringProperty text;
    private final StringProperty styleProperty;
    private final ObjectProperty<Node> graphic;
    private String tooltip;
    /**
     * This variable handles all boolean values of this SpreadsheetCell inside
     * its bits. Instead of using regular boolean, we use that int so that we 
     * can reduce memory usage to the bare minimum.
     */
    private int propertyContainer = 0;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    private ObservableSet<String> styleClass;
    private List<MenuItem> actionsList;

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    /**
     * Constructs a SpreadsheetCell with the given configuration.
     * Use the {@link SpreadsheetCellType#OBJECT} type.
     * @param row
     * @param column
     * @param rowSpan
     * @param columnSpan
     */
    public SpreadsheetCellBase(final int row, final int column, final int rowSpan, final int columnSpan) {
        this(row, column, rowSpan, columnSpan, SpreadsheetCellType.OBJECT);
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
    public SpreadsheetCellBase(final int row, final int column, final int rowSpan, final int columnSpan,
            final SpreadsheetCellType<?> type) {
        this.row = row;
        this.column = column;
        this.rowSpan = rowSpan;
        this.columnSpan = columnSpan;
        this.type = type;
        text = new SimpleStringProperty(""); //$NON-NLS-1$
        format = new SimpleStringProperty(""); //$NON-NLS-1$
        graphic = new SimpleObjectProperty<>();
        format.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                updateText();
            }
        });
        //Editable is true at the initialisation
        setEditable(true);
        getStyleClass().add("spreadsheet-cell"); //$NON-NLS-1$
        styleProperty = new SimpleStringProperty();
    }

   /***************************************************************************
     * 
     * Public Methods
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override
    public boolean match(SpreadsheetCell cell) {
        return type.match(cell, getOptionsForEditor());
    }

    // --- item
    private final ObjectProperty<Object> item = new SimpleObjectProperty<Object>(this, "item") { //$NON-NLS-1$
        @Override
        protected void invalidated() {
            updateText();
        }
    };

   /** {@inheritDoc} */
    @Override
    public final void setItem(Object value) {
        if (isEditable())
            item.set(value);
    }

    /** {@inheritDoc} */
    @Override
    public final Object getItem() {
        return item.get();
    }

    /** {@inheritDoc} */
    @Override
    public final ObjectProperty<Object> itemProperty() {
        return item;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isEditable() {
        return isSet(EDITABLE_BIT_POSITION);
    }

    /** {@inheritDoc} */
    @Override
    public final void setEditable(boolean editable) {
        if(setMask(editable, EDITABLE_BIT_POSITION)){
            Event.fireEvent(this, new Event(EDITABLE_EVENT_TYPE));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isWrapText(){
        return isSet(WRAP_BIT_POSITION);
    }

    /** {@inheritDoc} */
    @Override
    public void setWrapText(boolean wrapText) {
        if (setMask(wrapText, WRAP_BIT_POSITION)) {
            Event.fireEvent(this, new Event(WRAP_EVENT_TYPE));
        }
    }
    
     /** {@inheritDoc} */
    @Override
    public List<Object> getOptionsForEditor(){
        return Collections.emptyList();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean hasPopup(){
        return isSet(POPUP_BIT_POSITION);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setHasPopup(boolean value){
        setMask(value, POPUP_BIT_POSITION);
        //We want to refresh the cell.
        Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
    }
    
    /** {@inheritDoc} */
    @Override
    public List<MenuItem> getPopupItems(){
        if(actionsList == null){
            actionsList = new ArrayList<>();
        }
        return actionsList;
    }

   /** {@inheritDoc} */
    @Override
    public final StringProperty formatProperty() {
        return format;
    }

    /** {@inheritDoc} */
    @Override
    public final String getFormat() {
        return format.get();
    }

    /** {@inheritDoc} */
    @Override
    public final void setFormat(String format) {
        formatProperty().set(format);
        updateText();
    }

    /** {@inheritDoc} */
    @Override
    public final ReadOnlyStringProperty textProperty() {
        return text;
    }

    /** {@inheritDoc} */
    @Override
    public final String getText() {
        return text.get();
    }

   /** {@inheritDoc} */
    @Override
    public final SpreadsheetCellType getCellType() {
        return type;
    }

   /** {@inheritDoc} */
    @Override
    public final int getRow() {
        return row;
    }

    /** {@inheritDoc} */
    @Override
    public final int getColumn() {
        return column;
    }

   /** {@inheritDoc} */
    @Override
    public final int getRowSpan() {
        return rowSpan;
    }

    /** {@inheritDoc} */
    @Override
    public final void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }

    /** {@inheritDoc} */
    @Override
    public final int getColumnSpan() {
        return columnSpan;
    }

   /** {@inheritDoc} */
    @Override
    public final void setColumnSpan(int columnSpan) {
        this.columnSpan = columnSpan;
    }

    /** {@inheritDoc} */
    @Override
    public final ObservableSet<String> getStyleClass() {
        if (styleClass == null) {
            styleClass = FXCollections.observableSet();
        }
        return styleClass;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setStyle(String style){
        styleProperty.set(style);
    }
    
    /** {@inheritDoc} */
    @Override
    public String getStyle(){
        return styleProperty.get();
    }
    
    /** {@inheritDoc} */
    @Override
    public StringProperty styleProperty(){
        return styleProperty;
    }

    /** {@inheritDoc} */
    @Override
    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    /** {@inheritDoc} */
    @Override
    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    /** {@inheritDoc} */
    @Override
    public Node getGraphic() {
        return graphic.get();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> getTooltip() {
        return Optional.ofNullable(tooltip);
    }
    
    /**
     * Set a new tooltip for this cell.
     * @param tooltip 
     */
    public void setTooltip(String tooltip){
        this.tooltip = tooltip;
    }
    
    /** {@inheritDoc} */
    @Override
    public void activateCorner(CornerPosition position) {
        if(setMask(true, getCornerBitNumber(position))){
            Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void deactivateCorner(CornerPosition position) {
        if(setMask(false, getCornerBitNumber(position))){
             Event.fireEvent(this, new Event(CORNER_EVENT_TYPE));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCornerActivated(CornerPosition position) {
        return isSet(getCornerBitNumber(position));
    }
    
    /** {@inheritDoc} */
    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.append(eventHandlerManager);
    }
    
    /***************************************************************************
     * 
     * Overridden Methods
     * 
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "cell[" + row + "][" + column + "]" + rowSpan + "-" + columnSpan; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof SpreadsheetCell))
            return false;

        final SpreadsheetCell otherCell = (SpreadsheetCell) obj;
        return otherCell.getRow() == row && otherCell.getColumn() == column
                && Objects.equals(otherCell.getText(), getText())
                && rowSpan == otherCell.getRowSpan()
                && columnSpan == otherCell.getColumnSpan()
                && Objects.equals(getStyleClass(), otherCell.getStyleClass());
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        result = prime * result + rowSpan;
        result = prime * result + columnSpan;
        result = prime * result + Objects.hashCode(getText());
        result = prime * result + Objects.hashCode(getStyleClass());
        return result;
    }
    
    /**
     * Registers an event handler to this SpreadsheetCell. The SpreadsheetCell class allows 
     * registration of listeners which will be notified when a corner state of
     * the editable state of this SpreadsheetCell have changed.
     *
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    @Override
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /**
     * Unregisters a previously registered event handler from this SpreadsheetCell. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    @Override
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler) {
         eventHandlerManager.removeEventHandler(eventType, eventHandler);
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
        if(getItem() == null){
            text.setValue(""); //$NON-NLS-1$
        }else if (!("").equals(getFormat())) { //$NON-NLS-1$
            text.setValue(type.toString(getItem(), getFormat()));
        } else {
            text.setValue(type.toString(getItem()));
        }
    }

    /**
     * Return the Bit position for each corner.
     * @param position
     * @return 
     */
    private int getCornerBitNumber(CornerPosition position) {
        switch (position) {
            case TOP_LEFT:
                return 0;

            case TOP_RIGHT:
                return 1;

            case BOTTOM_RIGHT:
                return 2;

            case BOTTOM_LEFT:
            default:
                return 3;
        }
    }

    /**
     * Set the specified bit position at the value specified by flag.
     * @param flag
     * @param position
     * @return whether a change has really occured.
     */
    private boolean setMask(boolean flag, int position) {
        int oldCorner = propertyContainer;
        if (flag) {
            propertyContainer |= (1 << position);
        } else {
            propertyContainer &= ~(1 << position);
        }
        return propertyContainer != oldCorner;
    }

    /**
     * @param mask
     * @param position
     * @return whether the specified bit position is true.
     */
    private boolean isSet(int position) {
        return (propertyContainer & (1 << position)) != 0;
    }
}
