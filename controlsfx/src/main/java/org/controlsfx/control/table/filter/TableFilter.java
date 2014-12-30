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
	private final Callback<TableView<T>, TableRow<T>> rowFactory;
	
	private final Predicate<T> filterPredicate = v -> columnFilters.stream().filter(f -> f.isSelected(v) == false)
			.findAny().isPresent() == false;
	
	private TableFilter(TableView<T> tableView) { 
		this.tableView = tableView;
		this.rowFactory = tableView.getRowFactory();
		/*
		tableView.setRowFactory(t -> { 
			TableRow<T> tableRow = rowFactory.call(t); 
			return tableRow;
		});*/
	}
	public static <B> TableFilter<B> forTable(TableView<B> tableView) { 
		TableFilter<B> tableFilter = new TableFilter<B>(tableView);
		tableFilter.applyForAllColumns();
		return tableFilter;
	}
	
	private void applyForAllColumns() { 
		columnFilters.setAll(tableView.getColumns().stream()
				.map(c -> ColumnFilter.getInstance(tableView, c)).collect(Collectors.toList()));
	}
	public void executeFilter() { 
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
