/**
 * Copyright (c) 2014 ControlsFX
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

import java.util.Optional;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Node;

/**
 *
 * Interface of the cells used in the {@link SpreadsheetView}.
 * 
 * See {@link SpreadsheetCellBase} for a complete and detailed documentation.
 * @see SpreadsheetCellBase
 */
public interface SpreadsheetCell {

    /**
     * This enum states the four different corner available for positioning 
     * some elements in a cell.
     */
    public static enum CornerPosition {

        TOP_LEFT {
                    @Override
                    int getBitNumber() {
                        return 0;
                    }
                },
        TOP_RIGHT {
                    @Override
                    int getBitNumber() {
                        return 1;
                    }
                },
        BOTTOM_RIGHT {
                    @Override
                    int getBitNumber() {
                        return 2;
                    }
                },
        BOTTOM_LEFT {
                    @Override
                    int getBitNumber() {
                        return 3;
                    }
                };

        abstract int getBitNumber();

        public int setMask(int mask, boolean flag) {
            if (flag) {
                return mask | (1 << getBitNumber());
            } else {
                return mask & ~(1 << getBitNumber());
            }
        }

        public boolean isSet(int mask) {
            return (mask & (1 << getBitNumber())) != 0;
        }
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
     * @param readOnly
     */
    public void setEditable(boolean readOnly);

    /**
     * The {@link BooleanProperty} linked with the editable state.
     *
     * @return The {@link BooleanProperty} linked with the editable state.
     */
    public BooleanProperty editableProperty();

    /**
     * This property allow you to track any change occuring on the corners.
     * 
     * @return a BooleanProperty related to the corners.
     */
    public ReadOnlyIntegerProperty cornerProperty();
    
    /**
     * This allow you to change the state of a specific corner.
     * @param position
     * @param activated 
     */
    public void setCorner(CornerPosition position, boolean activated);

    /**
     * 
     * @param position
     * @return the current state of a specific corner.
     */
    public boolean getCorner(CornerPosition position);
    
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
}
