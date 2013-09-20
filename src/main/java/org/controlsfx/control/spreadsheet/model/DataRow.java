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

import java.util.ArrayList;

/**
 * 
 * The model row that holds the {@link DataCell}. It is used by the view to get
 * access to the different {@link DataCell}.
 */
public class DataRow {

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    private final int rowNumber;
    private final ArrayList<DataCell<?>> list;

    
    
    /***************************************************************************
     *
     * Constructor
     * 
     **************************************************************************/
    public DataRow(int rowNumber, int columnCount) {
        this.rowNumber = rowNumber;
        list = new ArrayList<>(columnCount);
    }

    
    
    /***************************************************************************
     * 
     * Public Methods
     * 
     **************************************************************************/
    public int getRowNumber() {
        return rowNumber;
    }
    
    public void add(DataCell<?> cell) {
        list.add(cell);
    }
    
    public DataCell<?> get(int i) {
        return list.get(i);
    }

    public void set(int i, DataCell<?> cell) {
        list.set(i, cell);
    }
    
    public DataCell<?> getCell(int col) {
        return list.get(col);
    }
}
