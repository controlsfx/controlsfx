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
