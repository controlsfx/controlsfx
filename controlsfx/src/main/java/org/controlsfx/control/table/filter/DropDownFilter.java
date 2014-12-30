package org.controlsfx.control.table.filter;

import javafx.scene.control.TableView;


public final class DropDownFilter<T> {
	private final TableView<T> tableView;
	
	private DropDownFilter(TableView<T> tableView) { 
		this.tableView = tableView;
		TableFilter.forTable(tableView);
	}
	
	public static <B> DropDownFilter<B> forTable(TableView<B> tableView) { 
		return new DropDownFilter<B>(tableView);
	}
	
}
