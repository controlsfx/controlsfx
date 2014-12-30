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

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;


public final class FilterPanel<T> extends Pane {
	
	private final ColumnFilter<T> columnFilter;
	private final ListView<FilterItem> listView = new ListView<>();
	private final TextField searchBox = new TextField("Search...");
	
	FilterPanel(ColumnFilter<T> tableFilter) { 
		this.columnFilter = tableFilter;
		
		listView.itemsProperty().get().setAll(columnFilter.getAllVals().stream()
				.map(v -> new FilterItem(v, columnFilter)).collect(Collectors.toList()));
		
		VBox vBox = new VBox();
		vBox.setPadding(new Insets(3));
		
		vBox.getChildren().add(searchBox);
		searchBox.setPadding(new Insets(0,0,10,0));
		vBox.getChildren().add(listView);
		this.getChildren().add(vBox);
	}
	
	private static class FilterItem<T> extends Pane { 
		private final CheckBox checkBox = new CheckBox();
		private final Label label = new Label();
		private final Object value;
		private final ColumnFilter<?> columnFilter;
		
		FilterItem(Object value,  ColumnFilter<?> columnFilter) { 
			this.columnFilter = columnFilter;
			this.value = value;
			
			HBox hBox = new HBox();
			hBox.getChildren().add(checkBox);
			
			label.setText(value.toString());
			
			hBox.getChildren().add(label);
			this.getChildren().add(hBox);
			
			checkBox.setSelected(columnFilter.getSelectedVals().contains(value));
			
			attachListeners();
			
		}
		
		private void attachListeners() { 
			
			checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			    @Override
			    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			       if (oldValue.equals(Boolean.TRUE) && newValue.equals(Boolean.FALSE)) { 
			    	   columnFilter.getSelectedVals().remove(value);
			       }
			       else if (oldValue.equals(Boolean.FALSE) && newValue.equals(Boolean.TRUE)) { 
			    	   columnFilter.getSelectedVals().add(value);
			       }
			    }
			});
		}
	}
	
	public static <T> MenuItem getInMenuItem(ColumnFilter<T> columnFilter) { 
		CustomMenuItem menuItem = new CustomMenuItem();
		menuItem.contentProperty().set(new FilterPanel<T>(columnFilter));
		return menuItem;
	}
}
