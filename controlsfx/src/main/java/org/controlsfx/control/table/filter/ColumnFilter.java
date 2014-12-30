package org.controlsfx.control.table.filter;

import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

final class ColumnFilter<T> {
	private final TableView<T> tableView;
	private final TableColumn<T,?> tableColumn;
	
	private final ObservableList<Object> allVals = FXCollections.observableArrayList();
	private final ObservableList<Object> selectedVals = FXCollections.observableArrayList();
	
	private final ListChangeListener<T> listListener = c -> rebuildAllVals();
	
	private ColumnFilter(TableView<T> tableView, TableColumn<T,?> tableColumn) { 
		this.tableView = tableView;
		this.tableColumn = tableColumn;
	}
	
	public ObservableList<Object> getAllVals() { 
		return allVals;
	}
	public ObservableList<Object> getSelectedVals() { 
		return selectedVals;
	}
	public boolean isSelected(T value) { 
		return getPredicate().test(value);
	}
	public Predicate<T> getPredicate() { 
		return item -> selectedVals.contains(tableColumn.getCellObservableValue(item));
	}
	public TableColumn<T,?> getTableColumn() { 
		return tableColumn;
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
	
	private void rebuildAllVals() { 
		allVals.clear();
		tableView.itemsProperty().get().stream().map(item -> tableColumn.getCellObservableValue(item).getValue()).distinct()
		.forEach(val -> allVals.add(val));
	}
	
	private void connectListener() { 
		tableView.itemsProperty().get().addListener(listListener);
	}
	private void initializeData() { 
		rebuildAllVals();
		selectedVals.addAll(allVals);
	}
	private void attachContextMenu() { 
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.getItems().add(FilterPanel.getInMenuItem(this));
		tableColumn.setContextMenu(contextMenu);
	}
	static <T> ColumnFilter<T> getInstance(TableView<T> tableView, TableColumn<T,?> tableColumn) { 
		final ColumnFilter<T> columnFilter = new ColumnFilter<T>(tableView, tableColumn);
		
		columnFilter.connectListener();
		columnFilter.initializeData();
		columnFilter.attachContextMenu();
		return columnFilter;
	}
}
