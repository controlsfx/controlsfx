/**
 * Copyright (c) 2014, 2016 ControlsFX
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

/**
 *
 * Interface of the cells used in the {@link SpreadsheetView}.
 * 
 * See {@link SpreadsheetCellBase} for a complete and detailed documentation.
 * @see SpreadsheetCellBase
 */
public interface SpreadsheetCell  {
    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when the editable state of a SpreadsheetCell is changed.
     */
    public static final EventType<Event> EDITABLE_EVENT_TYPE 
            = new EventType<>("EditableEventType" + UUID.randomUUID().toString()); //$NON-NLS-1$
    
    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when the wrap text state of a SpreadsheetCell is changed.
     */
    public static final EventType<Event> WRAP_EVENT_TYPE 
            = new EventType<>("WrapTextEventType" + UUID.randomUUID().toString()); //$NON-NLS-1$
    
    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when a corner state of a SpreadsheetCell is changed.
     */
    public static final EventType<Event> CORNER_EVENT_TYPE 
            = new EventType<>("CornerEventType" + UUID.randomUUID().toString()); //$NON-NLS-1$
    
    /**
     * This enum states the four different corner available for positioning 
     * some elements in a cell.
     */
    public static enum CornerPosition {

        TOP_LEFT ,
        TOP_RIGHT ,
        BOTTOM_RIGHT ,
        BOTTOM_LEFT 
    }
    
    /**
     * Verify that the upcoming cell value can be set to the current cell. This
     * is currently used by the Copy/Paste.
     *
     * @param cell
     * @return true if the upcoming cell value can be set to the current cell.
     */
    public boolean match(SpreadsheetCell cell);

    /**
     * Sets the value of the property Item. This should be used only at
     * initialization. Prefer {@link Grid#setCellValue(int, int, Object)} after
     * because it will compute correctly the modifiedCell. If
     * {@link #isEditable()} return false, nothing is done.
     *
     * @param value
     */
    public void setItem(Object value);

    /**
     * Return the value contained in the cell.
     *
     * @return the value contained in the cell.
     */
    public Object getItem();

    /**
     * The item property represents the currently-set value inside this
     * SpreadsheetCell instance.
     *
     * @return the item property which contains the value.
     */
    public ObjectProperty<Object> itemProperty();

    /**
     * Return if this cell can be edited or not.
     *
     * @return true if this cell is editable.
     */
    public boolean isEditable();

    /**
     * Change the editable state of this cell
     *
     * @param editable
     */
    public void setEditable(boolean editable);
    
    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     *
     * @return the value of wrapText property.
     */
    public boolean isWrapText();
    
    
    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     * @param wrapText
     */
    public void setWrapText(boolean wrapText);

    /**
     * If some options cannot be factorized in a {@link SpreadsheetCellType} and
     * are specific to a cell, you can return them here and the
     * {@link SpreadsheetCellEditor} will receive them.
     *
     * @return a List of options for the {@link SpreadsheetCellEditor}.
     */
    public List<Object> getOptionsForEditor();
    
    /**
     * Return true if this cell needs to display a popup when clicked in order
     * to show some {@link MenuItem} like a {@link MenuButton}.
     *
     * @return true if this cell needs to display a popup.
     */
    public boolean hasPopup();

    /**
     * Set to true if this cell needs to display a popup when clicked in order
     * to show some {@link MenuItem} like a {@link MenuButton}.
     *
     * @param value
     */
    public void setHasPopup(boolean value);

    /**
     * If {@link #hasPopup() } is set to true, this method will be called when
     * the user clicks on the cell in order to gather the {@link MenuItem} to
     * show in the Popup.
     *
     * @return the {@link MenuItem} to show in the Popup.
     */
    public List<MenuItem> getPopupItems();
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @param style
     */
    public void setStyle(String style);
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @return The inline CSS style associated with this Node. If this Node does
     * not have an inline style, an empty String is returned.
     */
    public String getStyle();
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @return a string representation of the CSS style
     */
    public StringProperty styleProperty();
    
    /**
     * This activate the given cornerPosition.
     * @param position
     */
    public void activateCorner(CornerPosition position);
    
    /**
     * This deactivate the given cornerPosition.
     * @param position
     */
    public void deactivateCorner(CornerPosition position);

    /**
     * 
     * @param position
     * @return the current state of a specific corner.
     */
    public boolean isCornerActivated(CornerPosition position);
    
    /**
     * The {@link StringProperty} linked with the format.
     *
     * @return The {@link StringProperty} linked with the format state.
     */
    public StringProperty formatProperty();

    /**
     * Return the format of this cell or an empty string if no format has been
     * specified.
     *
     * @return Return the format of this cell or an empty string if no format
     * has been specified.
     */
    public String getFormat();
    
    /**
     * Set a new format for this Cell. You can specify how to represent the
     * value in the cell.
     *
     * @param format
     */
    public void setFormat(String format);

    /**
     * Return the StringProperty of the representation of the value.
     *
     * @return the StringProperty of the representation of the value.
     */
    public ReadOnlyStringProperty textProperty();

    /**
     * Return the String representation currently used for display in the
     * {@link SpreadsheetView}.
     *
     * @return text representation of the value.
     */
    public String getText();

    /**
     * Return the {@link SpreadsheetCellType} of this particular cell.
     *
     * @return the {@link SpreadsheetCellType} of this particular cell.
     */
    public SpreadsheetCellType getCellType();

    /**
     * Return the row of this cell.
     *
     * @return the row of this cell.
     */
    public int getRow();

    /**
     * Return the column of this cell.
     *
     * @return the column of this cell.
     */
    public int getColumn();

    /**
     * Return how much this cell is spanning in row, 1 is normal.
     *
     * @return how much this cell is spanning in row, 1 is normal.
     */
    public int getRowSpan();

    /**
     * Sets how much this cell is spanning in row. See {@link SpreadsheetCell}
     * description for information. You should use
     * {@link Grid#spanRow(int, int, int)} instead of using this method
     * directly.
     *
     * @param rowSpan
     */
    public void setRowSpan(int rowSpan);

    /**
     * Return how much this cell is spanning in column, 1 is normal.
     *
     * @return how much this cell is spanning in column, 1 is normal.
     */
    public int getColumnSpan();

    /**
     * Sets how much this cell is spanning in column. See
     * {@link SpreadsheetCell} description for information. You should use
     * {@link Grid#spanColumn(int, int, int)} instead of using this method
     * directly.
     *
     * @param columnSpan
     */
    public void setColumnSpan(int columnSpan);

    /**
     * Return an ObservableList of String of all the style class associated with
     * this cell. You can easily modify its appearance by adding a style class
     * (previously set in CSS).
     *
     * @return an ObservableList of String of all the style class
     */
    public ObservableSet<String> getStyleClass();

    /**
     * @return an ObjectProperty wrapping a Node for the graphic.
     */
    public ObjectProperty<Node> graphicProperty();

    /**
     * Set a graphic for this cell to display aside with the text.
     *
     * @param graphic
     */
    public void setGraphic(Node graphic);

    /**
     * Return the graphic node associated with this cell. Return null if nothing
     * has been associated.
     *
     * @return the graphic node associated with this cell.
     */
    public Node getGraphic();
    
    /**
     * @return the tooltip associated with this SpreadsheetCell.
     */
    public Optional<String> getTooltip();
    
    /**
     * Registers an event handler to this SpreadsheetCell. 
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
    
    /**
     * Unregisters a previously registered event handler from this SpreadsheetCell. 
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
}
