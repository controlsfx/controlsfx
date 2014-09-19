/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.control.table.model;

import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 *
 */
//not public as not ready for 8.20.7
class TableModelTableView<S> extends TableView<TableModelRow<S>> {

    public TableModelTableView(final JavaFXTableModel<S> tableModel) {
        // create a dummy items list of the appropriate size, where the returned
        // value is the index of the row
        setItems(new ReadOnlyUnbackedObservableList<TableModelRow<S>>() {
            @Override public TableModelRow<S> get(int row) {
                if (row < 0 || row >= tableModel.getRowCount()) return null;
                TableModelRow<S> backingRow = new TableModelRow<>(tableModel, row);
                return backingRow;
            }

            @Override public int size() {
                return tableModel.getRowCount();
            }
        });
        
        setSortPolicy(table -> {
            tableModel.sort(table);
            return true;
        });
        
        // create columns from the table model
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            TableColumn<TableModelRow<S>,?> column = new TableColumn<>(tableModel.getColumnName(i));
            column.setCellValueFactory(new TableModelValueFactory<>(tableModel, i));
            getColumns().add(column);
        }
    }
}
