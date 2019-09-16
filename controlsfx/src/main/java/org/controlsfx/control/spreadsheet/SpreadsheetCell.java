/**
 * Copyright (c) 2014, 2018 ControlsFX
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
     * Verifies that the upcoming cell value can be set to the current cell.
     * This is currently used by the Copy/Paste.
     *
     * @param value the value that needs to be tested
     * @return {@code true} if the upcoming cell value can be set to the current
     * cell
     */
    public boolean match(Object value);

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
     * Returns the value contained in this cell.
     *
     * @return the value contained in this cell
     */
    public Object getItem();

    /**
     * The item property represents the currently-set value inside this
     * {@code SpreadsheetCell}.
     *
     * @return the item property which contains the value.
     */
    public ObjectProperty<Object> itemProperty();

    /**
     * Returns {@code true} if this cell can be edited.
     *
     * @return {@code true} if this cell is editable
     */
    public boolean isEditable();

    /**
     * Change the editable state of this cell
     *
     * @param editable {@code true} if this cell should be editable
     */
    public void setEditable(boolean editable);
    
    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     *
     * @return {@code true} if the text should wrap onto another line if it
     * exceeds the width of the {@code Labeled}
     */
    public boolean isWrapText();

    /**
     * Returns {@code true} if this cell contains something particular in its
     * item and a Node given by the {@link CellGraphicFactory} will be used to
     * display it.
     *
     * @return {@code true} if this cell item needs to be given to a particular
     * Node
     */
    public boolean isCellGraphic();

    /**
     * If {@code isCellGraphic} is {@code true}, this cell item contains
     * something particular and should be display by using
     * {@link CellGraphicFactory} object in the CellView.
     *
     * @param isCellGraphic if {@code true}, a Node will be used to display
     * something particular for the cell
     */
    public void setCellGraphic(boolean isCellGraphic);
    
    /**
     * If a run of text exceeds the width of the Labeled, then this variable
     * indicates whether the text should wrap onto another line.
     *
     * @param wrapText {@code true} if the text should wrap onto another line if
     * it exceeds the width of the {@code Labeled}
     */
    public void setWrapText(boolean wrapText);

    /**
     * If some options cannot be factorized in a {@link SpreadsheetCellType} and
     * are specific to a cell, you can return them here and the
     * {@link SpreadsheetCellEditor} will receive them.
     *
     * @return a {@code List} of options for the {@code SpreadsheetCellEditor}
     */
    public List<Object> getOptionsForEditor();
    
    /**
     * Returns true if this cell needs to display a popup when clicked in order
     * to show some {@link MenuItem} like a {@link MenuButton}.
     *
     * The items can be set in {@link #getPopupItems() }.
     *
     * @return {@code true} if this cell needs to display a popup
     */
    public boolean hasPopup();

    /**
     * Sets to {@code true} if this cell needs to display a popup when clicked
     * in order to show some {@link MenuItem} like a {@link MenuButton}.
     *
     * @param value {@code true} to display a {@code Popup} when clicked
     */
    public void setHasPopup(boolean value);

    /**
     * If {@link #hasPopup() } is set to {@code true}, this method will be called when
     * the user clicks on the cell in order to gather the {@link MenuItem} to
     * show in the Popup.
     *
     * @return the {@link MenuItem} to show in the Popup
     */
    public List<MenuItem> getPopupItems();
    
    /**
     * A string representation of the CSS style associated with this specific
     * Node. This is analogous to the "style" attribute of an HTML element. Note
     * that, like the HTML style attribute, this variable contains style
     * properties and values and not the selector portion of a style rule.
     *
     * @param style a string representation of the CSS style associated with
     * this specific Node
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
     * Activates the given {@code CornerPosition} in order to display a little
     * triangle in the cell.
     *
     * @param position the position where the triangle should be displayed
     */
    public void activateCorner(CornerPosition position);
    
    /**
     * This deactivates the given {@code CornerPosition} so that no triangle
     * will be shown for this cell.
     *
     * @param position the position where the triangle should be removed if
     * displayed
     */
    public void deactivateCorner(CornerPosition position);

    /**
     * Returns {@code true} if a triangle is displayed in the cell for the given
     * {@code CornerPosition}.
     *
     * @param position
     * @return {@code true} if a triangle is displayed in the cell for the given
     * {@code CornerPosition}
     */
    public boolean isCornerActivated(CornerPosition position);
    
    /**
     * Returns the {@code StringProperty} linked with the format.
     *
     * @return the {@code StringProperty} linked with the format state
     */
    public StringProperty formatProperty();

    /**
     * Returns the format of this cell or an empty string if no format has been
     * specified.
     *
     * @return the format of this cell or an empty string if no format has been
     * specified
     */
    public String getFormat();
    
    /**
     * Sets a new format for this cell. This format will be used by {@link SpreadsheetCellType#toString(java.lang.Object, java.lang.String)
     * }. This should be used by numbers for example.
     *
     * @param format a string pattern understood by the
     * {@code SpreadsheetCellType}
     */
    public void setFormat(String format);

    /**
     * Returns the StringProperty of the representation of the value.
     *
     * @return the StringProperty of the representation of the value
     */
    public ReadOnlyStringProperty textProperty();

    /**
     * Returns the String representation currently used for display in the
     * {@link SpreadsheetView}.
     *
     * @return the text representation of the value
     */
    public String getText();

    /**
     * Returns the {@link SpreadsheetCellType} of this cell.
     *
     * @return the {@code SpreadsheetCellType} of this cell.
     */
    public SpreadsheetCellType getCellType();

    /**
     * Returns the row index of this cell.
     *
     * @return the row index of this cell
     */
    public int getRow();

    /**
     * Returns the column index of this cell.
     *
     * @return the column index of this cell
     */
    public int getColumn();

    /**
     * Returns how much this cell is spanning in row, 1 means the cell is not
     * spanning.
     *
     * @return how much this cell is spanning in row, 1 is normal
     */
    public int getRowSpan();

    /**
     * Sets how much this cell is spanning in row. See {@link SpreadsheetCell}
     * description for information. You should use
     * {@link Grid#spanRow(int, int, int)} instead of using this method
     * directly.
     *
     * 1 means the cell is not spanning. Thus, the rowSpan should not be
     * inferior to 1.
     *
     * @param rowSpan the rowSpan for this cell
     */
    public void setRowSpan(int rowSpan);

    /**
     * Returns how much this cell is spanning in column, 1 means the cell is not
     * spanning.
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
     * 1 means the cell is not spanning. Thus, the rowSpan should not be
     * inferior to 1.
     *
     * @param columnSpan the columnSpan for this cell
     */
    public void setColumnSpan(int columnSpan);

    /**
     * Returns an {@code ObservableList} of {@code String} of all the style
     * class associated with this cell. You can easily modify its appearance by
     * adding a style class (previously set in CSS).
     *
     * @return an {@code ObservableList} of {@code String} of all the style
     * class of this cell
     */
    public ObservableSet<String> getStyleClass();

    /**
     * Returns the {@code ObjectProperty} representing this cell graphic.
     *
     * @return an ObjectProperty wrapping a Node for the graphic
     */
    public ObjectProperty<Node> graphicProperty();

    /**
     * Sets a graphic for this cell. It is displayed aside with the text if any
     * is specified. Otherwise it's fully displayed in the cell.
     *
     * @param graphic a graphic to display for this cell
     */
    public void setGraphic(Node graphic);

    /**
     * Returns the graphic node associated with this cell. Returns null if
     * nothing has been associated.
     *
     * @return the graphic node associated with this cell
     */
    public Node getGraphic();
    
    /**
     * Returns the tooltip for this cell.
     *
     * @return the tooltip associated with this {@code SpreadsheetCell}
     */
    public Optional<String> getTooltip();
    
    /**
     * Registers an event handler to this SpreadsheetCell.
     *
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public void addEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
    
    /**
     * Unregisters a previously registered event handler from this
     * SpreadsheetCell.
     *
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public void removeEventHandler(EventType<Event> eventType, EventHandler<Event> eventHandler);
}
