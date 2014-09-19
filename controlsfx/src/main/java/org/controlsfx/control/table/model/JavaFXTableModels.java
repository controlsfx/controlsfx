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

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 */
//not public as not ready for 8.20.7
class JavaFXTableModels {
    
    /**
     * Swing
     */
    public static <S> JavaFXTableModel<S> wrap(final TableModel tableModel) {
        
        return new JavaFXTableModel<S>() {
            final TableRowSorter<TableModel> sorter;
            
            {
                sorter = new TableRowSorter<>(tableModel);
            }
            
            @SuppressWarnings("unchecked")
            @Override public S getValueAt(int rowIndex, int columnIndex) {
                return (S) tableModel.getValueAt(sorter.convertRowIndexToView(rowIndex), columnIndex);
            }

            @Override public void setValueAt(S value, int rowIndex, int columnIndex) {
                tableModel.setValueAt(value, rowIndex, columnIndex);
            }

            @Override public int getRowCount() {
                return tableModel.getRowCount();
            }

            @Override public int getColumnCount() {
                return tableModel.getColumnCount();
            }

            @Override public String getColumnName(int columnIndex) {
                return tableModel.getColumnName(columnIndex);
            }
            
            @Override public void sort(TableView<TableModelRow<S>> table) {
                List<SortKey> sortKeys = new ArrayList<>();
                
                for (TableColumn<TableModelRow<S>, ?> column : table.getSortOrder()) {
                    final int columnIndex = table.getVisibleLeafIndex(column);
                    final SortType sortType = column.getSortType();
                    SortOrder sortOrder = sortType == SortType.ASCENDING ? SortOrder.ASCENDING :
                                          sortType == SortType.DESCENDING ? SortOrder.DESCENDING :
                                          SortOrder.UNSORTED;
                    SortKey sortKey = new SortKey(columnIndex, sortOrder);
                    sortKeys.add(sortKey);
                    
                    sorter.setComparator(columnIndex, column.getComparator());
                }
                
                sorter.setSortKeys(sortKeys);
                sorter.sort();
            }
        };
    }
}
