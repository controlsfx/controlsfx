/**
 * Copyright (c) 2015 ControlsFX
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

import impl.org.controlsfx.spreadsheet.TableViewSpanSelectionModel;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;

/**
 *
 * This class provides basic support for common interaction on the
 * {@link SpreadsheetView}.
 *
 * Due to the complexity induced by cell's span, it is not possible to give a
 * full access to selectionModel like in the {@link TableView}.
 */
public class SpreadsheetViewSelectionModel {

    private final TableViewSpanSelectionModel selectionModel;

    SpreadsheetViewSelectionModel(TableViewSpanSelectionModel selectionModel) {
        this.selectionModel = selectionModel;
    }
    
    /**
     * Clears all selection, and then selects the cell at the given row/column intersection.
     * @param row
     * @param column 
     */
    public final void clearAndSelect(int row, SpreadsheetColumn column) {
        selectionModel.clearAndSelect(row, column.column);
    }
    
    /**
     * Selects the cell at the given row/column intersection.
     * @param row
     * @param column 
     */
    public final void select(int row, SpreadsheetColumn column) {
        selectionModel.select(row,column.column);
    }
    
    /**
     * Clears the selection model of all selected indices.
     */
    public final void clearSelection() {
        selectionModel.clearSelection();
    }
    
    /**
     * A read-only ObservableList representing the currently selected cells in this SpreadsheetView. 
     * @return A read-only ObservableList.
     */
    public final ObservableList<TablePosition> getSelectedCells() {
        return selectionModel.getSelectedCells();
    }
    
    /**
     * Select all the possible cells.
     */
    public final void selectAll() {
        selectionModel.selectAll();
    }
    
    /**
     * Return the position of the cell that has current focus. 
     * @return the position of the cell that has current focus. 
     */
    public final TablePosition getFocusedCell(){
        return selectionModel.getTableView().getFocusModel().getFocusedCell();
    }
    
    /**
     * Specifies the selection mode to use in this selection model. The
     * selection mode specifies how many items in the underlying data model can
     * be selected at any one time. By default, the selection mode is
     * {@link SelectionMode#MULTIPLE}.
     *
     * @param value
     */
    public final void setSelectionMode(SelectionMode value) {
        selectionModel.setSelectionMode(value);
    }
    
    /**
     * Return the selectionMode currently used.
     *
     * @return the selectionMode currently used.
     */
    public SelectionMode getSelectionMode() {
        return selectionModel.getSelectionMode();
    }
}
