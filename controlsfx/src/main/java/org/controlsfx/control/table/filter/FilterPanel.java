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

import java.util.stream.Collectors;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkin;


public final class FilterPanel<T> extends Pane {
	
	private final ColumnFilter<T> columnFilter;
	private final ListView<FilterItem<T>> listView = new ListView<>();
	private final TextField searchBox = new TextField("Search...");
	
	FilterPanel(ColumnFilter<T> columnFilter) { 
		this.columnFilter = columnFilter;
		
		buildCheckList();
		
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(3));
		
		vBox.getChildren().add(searchBox);
		searchBox.setPadding(new Insets(0,0,10,0));
		vBox.getChildren().add(listView);
		this.getChildren().add(vBox);
	}
	private void buildCheckList() { 
		listView.itemsProperty().get().setAll(columnFilter.getAllVals().stream()
				.map(v -> new FilterItem<T>(v, this)).collect(Collectors.toList()));
	}
	private static class FilterItem<T> extends Pane { 
		private final CheckBox checkBox = new CheckBox();
		private final Label label = new Label();
		private final Object value;
		private final FilterPanel<?> filterPanel;
		
		FilterItem(Object value,  FilterPanel<?> filterPanel) { 
			this.filterPanel = filterPanel;
			this.value = value;
			
			HBox hBox = new HBox();
			hBox.getChildren().add(checkBox);
			
			label.setText(value.toString());
			
			hBox.getChildren().add(label);
			this.getChildren().add(hBox);
			
			checkBox.setSelected(filterPanel.columnFilter.getSelectedVals().contains(value));
			
			attachListeners();
			
		}
		
		private void attachListeners() { 
			
			final ListChangeListener<Object> selectionListener = l -> filterPanel.buildCheckList();
			filterPanel.columnFilter.getSelectedVals().addListener(selectionListener);
			
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			       if (oldValue.equals(Boolean.TRUE) && newValue.equals(Boolean.FALSE)) { 
			    	   filterPanel.columnFilter.getSelectedVals().remove(value);
			    	   filterPanel.columnFilter.getTableFilter().executeFilter();
			       }
			       else if (oldValue.equals(Boolean.FALSE) && newValue.equals(Boolean.TRUE)) { 
			    	   filterPanel.columnFilter.getSelectedVals().add(value);
			    	   filterPanel.columnFilter.getTableFilter().executeFilter();
			       }
			    }
			});
		}
	}
	
	public static <T> MenuItem getInMenuItem(ColumnFilter<T> columnFilter) { 
		CustomMenuItem menuItem = new CustomMenuItem();
		menuItem.contentProperty().set(new FilterPanel<T>(columnFilter));
		
		columnFilter.getTableFilter().getTableView().skinProperty().addListener((w, o, n) -> {
		    if (n instanceof TableViewSkin) {
		        TableViewSkin<?> skin = (TableViewSkin<?>) n;
		            checkChangeContextMenu(skin, columnFilter.getTableColumn());
		    }
		});
		
		return menuItem;
	}
	
	/* Methods below helps will anchor the context menu under the column */
	private static void checkChangeContextMenu(TableViewSkin<?> skin, TableColumn<?, ?> column) {
	    NestedTableColumnHeader header = skin.getTableHeaderRow()
	            .getRootHeader();
	    header.getColumnHeaders().addListener((Observable obs) -> changeContextMenu(header,column));
	    changeContextMenu(header, column);
	}

	private static void changeContextMenu(NestedTableColumnHeader header, TableColumn<?, ?> column) {
	    TableColumnHeader headerSkin = scan(column, header);
	    if (headerSkin != null) {
	        headerSkin.setOnContextMenuRequested(ev -> {
	            ContextMenu cMenu = column.getContextMenu();
	            if (cMenu != null) {
	                cMenu.show(headerSkin, Side.BOTTOM, 5, 5);
	            }
	            ev.consume();
	        });
	    }
	}

	private static TableColumnHeader scan(TableColumn<?, ?> search,
	        TableColumnHeader header) {
	    // firstly test that the parent isn't what we are looking for
	    if (search.equals(header.getTableColumn())) {
	        return header;
	    }

	    if (header instanceof NestedTableColumnHeader) {
	        NestedTableColumnHeader parent = (NestedTableColumnHeader) header;
	        for (int i = 0; i < parent.getColumnHeaders().size(); i++) {
	            TableColumnHeader result = scan(search, parent
	                    .getColumnHeaders().get(i));
	            if (result != null) {
	                return result;
	            }
	        }
	    }

	    return null;
	}
}
