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

import java.util.List;

/**
 * 
 * This class will save the state of your SpreadsheetView in order to restore
 * it.
 * 
 */
public class SpreadsheetViewState {

    boolean showColumnHeader;
    boolean showRowHeader;
    List<Integer> fixedRows;
    List<Integer> fixedColumns;
    Double vBarValue;
    Double hBarValue;

    // Columns width
    // Rows height

    SpreadsheetViewState(SpreadsheetView spv) {
        showColumnHeader = spv.isShowColumnHeader();
        showRowHeader = spv.isShowRowHeader();

        fixedRows = spv.getFixedRows();
        for (SpreadsheetColumn column : spv.getFixedColumns()) {
            fixedColumns.add(spv.getColumns().indexOf(column));
        }

        vBarValue = spv.getCellsViewSkin().getVBar().getValue();
        hBarValue = spv.getCellsViewSkin().getHBar().getValue();
    }
}
