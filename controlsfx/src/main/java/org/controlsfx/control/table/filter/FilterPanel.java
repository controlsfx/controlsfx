package org.controlsfx.control.table.filter;

import java.util.stream.Collectors;

import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;


public final class FilterPanel<T> extends Pane {
	
	private final ColumnFilter<T> columnFilter;
	private final ListView<FilterItem> listView = new ListView<>();
	
	FilterPanel(ColumnFilter<T> tableFilter) { 
		this.columnFilter = tableFilter;
		
		listView.itemsProperty().get().setAll(columnFilter.getAllVals().stream()
				.map(v -> new FilterItem(v)).collect(Collectors.toList()));
		
		this.getChildren().add(listView);
	}
	
	private static class FilterItem extends Pane { 
		private final CheckBox checkBox = new CheckBox();
		private final Label label = new Label();
		
		FilterItem(Object value) { 
			HBox hBox = new HBox();
			hBox.getChildren().add(checkBox);
			label.setText(value.toString());
			hBox.getChildren().add(label);
			this.getChildren().add(hBox);
			
		}
	}
	
	public static <T> MenuItem getInMenuItem(ColumnFilter<T> columnFilter) { 
		CustomMenuItem menuItem = new CustomMenuItem();
		menuItem.contentProperty().set(new FilterPanel<T>(columnFilter));
		return menuItem;
	}
}
