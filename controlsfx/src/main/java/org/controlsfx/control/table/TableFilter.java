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

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class TableFilter<T> {
    
    private final TableView<T> tableView;
    private final ObservableList<T> backingList;
    private final FilteredList<T> filteredList;
    
    private final ObservableList<ColumnFilter<T>> columnFilters = FXCollections.observableArrayList();
    
    private final Predicate<T> filterPredicate = v -> columnFilters.stream().filter(f -> f.isSelected(v) == false)
            .findAny().isPresent() == false;
    
    private TableFilter(TableView<T> tableView) { 
        this.tableView = tableView;
        this.backingList = tableView.getItems();
        this.filteredList = new FilteredList<T>(new SortedList<T>(backingList));
        this.filteredList.setPredicate(v -> true);
        tableView.setItems(filteredList);
    }
    public static <B> TableFilter<B> forTable(TableView<B> tableView) { 
        TableFilter<B> tableFilter = new TableFilter<B>(tableView);
        tableFilter.applyForAllColumns();
        return tableFilter;
    }
    
    private void applyForAllColumns() { 
        columnFilters.setAll(this.tableView.getColumns().stream()
                .map(c -> ColumnFilter.getInstance(this, c)).collect(Collectors.toList()));
    }
    public void resetSearchFilter() { 
        this.filteredList.setPredicate(t -> true);
    }
    public void executeFilter() { 
        filteredList.setPredicate(filterPredicate);
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
