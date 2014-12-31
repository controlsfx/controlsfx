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
package org.controlsfx.control.table.filter;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public final class TableFilter<T> {
	
	private final TableView<T> tableView;
	private final ObservableList<ColumnFilter<T>> columnFilters = FXCollections.observableArrayList();
	//private final Callback<TableView<T>, TableRow<T>> rowFactory;
	private static final String HIDE_ROW_CSS = TableFilter.class.getResource("CSS_HIDE_ROW").toExternalForm();

	private final Predicate<T> filterPredicate = v -> columnFilters.stream().filter(f -> f.isSelected(v) == false)
			.findAny().isPresent() == false;
	
	private TableFilter(TableView<T> tableView) { 
		this.tableView = tableView;
		
		this.tableView.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
		        @Override
		        public TableRow<T> call(TableView<T> tableView) {
		            final TableRow<T> row = new TableRow<T>() {
		                @Override
		                protected void updateItem(T row, boolean empty) {
		                    super.updateItem(row, empty);
		                    if (! filterPredicate.test(row)) {
		                    	this.getStylesheets().add(HIDE_ROW_CSS);
		                    }
		                    else { 
		                    	
		                    	this.getStylesheets().remove(HIDE_ROW_CSS);
		                    }
		                }
		            };
		            return row;
		        }
		    });
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
	public void executeFilter() { 
		TableColumn<T,?> column = tableView.getColumns().get(tableView.getColumns().size() -1);
		tableView.getColumns().remove(column);
		tableView.getColumns().add(column);
		
		//FilteredList<T> filteredList = new FilteredList<T>();
		
		// SortedList<T> sortedData = new SortedList<>(filteredList);

	    // sortedData.comparatorProperty().bind(filteredList.comparatorProperty());
	   //  tableView.setItems(filteredList);
	   //  tableView.setRowFactory(value);
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
