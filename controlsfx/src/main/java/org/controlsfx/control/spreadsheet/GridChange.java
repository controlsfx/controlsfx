/**
 * Copyright (c) 2013, 2018 ControlsFX 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met: *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. * Redistributions in binary
 * form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of ControlsFX, any associated
 * website, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control.spreadsheet;

import java.io.Serializable;
import java.util.UUID;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * This class represents a single change happening in a {@link Grid}.
 * 
 * @see Grid
 * @see GridBase
 */
public class GridChange extends Event implements Serializable {

    /**
     * This is the event used by {@link GridChange}.
     */
    public static final EventType<GridChange> GRID_CHANGE_EVENT 
            = new EventType<>(Event.ANY, "GridChange" + UUID.randomUUID().toString()); //$NON-NLS-1$

    private static final long serialVersionUID = 210644901287223524L;
    private final int modelRow;
    private final int column;
    private final Object oldValue;
    private final Object newValue;

    /**
     * Constructor of a GridChange when a change inside a
     * {@link SpreadsheetCell} is happening.
     *
     * @param modelRow the row index for this change
     * @param column the column index for this change
     * @param oldValue the previous value for this change
     * @param newValue the current value for this change
     */
    public GridChange(int modelRow, int column, Object oldValue, Object newValue) {
        super(GRID_CHANGE_EVENT);
        this.modelRow = modelRow;
        this.column = column;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * *************************************************************************
     * * Public methods * *
     ************************************************************************* 
     */
    /**
     * Returns the row index of this change.
     * 
     * @return the row number of this change
     */
    public int getRow() {
        return modelRow;
    }

    /**
     * Returns the column index of this change.
     *
     * @return the column number of this change
     */
    public int getColumn() {
        return column;
    }

    /**
     * Returns the value before the change.
     * 
     * @return the value before the change
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value after the change.
     *
     * @return the value after the change
     */
    public Object getNewValue() {
        return newValue;
    }
}
