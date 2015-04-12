/**
 * Copyright (c) 2015, ControlsFX
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

import impl.org.controlsfx.table.ColumnFilter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Optional;
import java.util.stream.Collectors;

public final class TableFilter<T> {
    
    private final TableView<T> tableView;
    private final ObservableList<T> backingList;
    private final FilteredList<T> filteredList;
    
    private final ObservableList<ColumnFilter<T>> columnFilters = FXCollections.observableArrayList();
 
    public TableFilter(TableView<T> tableView) {
        this.tableView = tableView;
        this.backingList = tableView.getItems();
        this.filteredList = new FilteredList<>(new SortedList<>(backingList));
        this.filteredList.setPredicate(v -> true);
        tableView.setItems(filteredList);
        this.applyForAllColumns();
        this.addListeners();
    }
    public ObservableList<T> getBackingList() { 
        return backingList;
    }
    public FilteredList<T> getFilteredList() { 
        return filteredList;
    }
    
    private void applyForAllColumns() { 
        columnFilters.setAll(this.tableView.getColumns().stream()
                .map(c -> new ColumnFilter<>(this, c)).collect(Collectors.toList()));
    }
    public void executeFilter() { 
        filteredList.setPredicate(v -> columnFilters.stream()
                .filter(cf -> cf.getFilterValue(cf.getTableColumn().getCellObservableValue(v)).map(ov -> ov.getSelectedProperty().getValue() == false).orElse(false))
                .findAny().isPresent() == false);
    }

    private void addListeners() {
     /*   backingList.addListener((InvalidationListener) e -> columnFilters.forEach(cf -> cf.rebuildAllVals()));

        columnFilters.forEach(cf -> cf.getTableColumn().onEditCommitProperty().addListener(e -> cf.rebuildAllVals()));*/
    }

    public TableView<T> getTableView() {
        return tableView;
    }
    public ObservableList<ColumnFilter<T>> getColumnFilters() { 
        return columnFilters;
    }
    public Optional<ColumnFilter<T>> getColumnFilter(TableColumn<T,?> tableColumn) { 
        return columnFilters.stream().filter(f -> f.getTableColumn().equals(tableColumn)).findAny();
    }
    
    
}
