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
package org.controlsfx.control.table;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;

import org.controlsfx.control.table.FilterPanel.FilterMenuItem;

final class ColumnFilter<T> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;
    
    private final ObservableList<Object> allVals = FXCollections.observableArrayList();
    private final ObservableList<Object> selectedVals = FXCollections.observableArrayList();
    
    private ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) { 
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
    }
    
    ObservableList<Object> getDistinctValues() { 
        return allVals;
    }
    ObservableList<Object> getSelectedDistinctValues() { 
        return selectedVals;
    }
    public boolean isSelected(T value) { 
        final boolean result = getPredicate().test(value);
        return result;
    }
    public TableColumn<T,?> getColumn() { 
        return tableColumn;
    }
    public Predicate<T> getPredicate() { 
        return item -> {
            Object value = tableColumn.getCellObservableValue(item).getValue();
            boolean result = selectedVals.contains(value);
            return result;
        };
    }
    public TableColumn<T,?> getTableColumn() { 
        return tableColumn;
    }
    public TableFilter<T> getTableFilter() { 
        return tableFilter;
    }
    
    public boolean selectVal(Object selectedVal) { 
        if (! selectedVals.contains(selectedVal)) { 
            selectedVals.add(selectedVal);
            return true;
        } else { 
            return false;
        }
    }
    
    public boolean deselectVal(Object deselectedVal) { 
        if (selectedVals.contains(deselectedVal)) { 
            selectedVals.remove(deselectedVal);
            return true;
        } else { 
            return false;
        }
    }

    public void rebuildAllVals() { 
        final List<Object> unselectedVals = allVals.stream().filter(v -> selectedVals.contains(v) == false).collect(Collectors.toList());
        
        allVals.clear();
        tableFilter.getBackingList().stream().map(item -> tableColumn.getCellObservableValue(item).getValue()).distinct()
        .forEach(val -> allVals.add(val));
        selectedVals.setAll(allVals);
        
        selectedVals.removeAll(unselectedVals);
    }
    
    private void connectListeners() { 
        final ListChangeListener<T> dataListener = c -> rebuildAllVals();
        tableFilter.getBackingList().addListener(dataListener);
        tableColumn.onEditCommitProperty().addListener(e -> rebuildAllVals());
        
    }
    private void initializeData() { 
        rebuildAllVals();
    }
    private void attachContextMenu() { 
        FilterMenuItem<T> item = FilterPanel.getInMenuItem(this);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(item);
        tableColumn.setContextMenu(contextMenu);
        
        contextMenu.setOnHiding(e -> item.getFilterPanel().resetSearchFilter());
    }
    static <T> ColumnFilter<T> getInstance(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) { 
        final ColumnFilter<T> columnFilter = new ColumnFilter<T>(tableFilter, tableColumn);
        
        columnFilter.initializeData();
        columnFilter.connectListeners();
        columnFilter.attachContextMenu();
        
        return columnFilter;
    }
}
