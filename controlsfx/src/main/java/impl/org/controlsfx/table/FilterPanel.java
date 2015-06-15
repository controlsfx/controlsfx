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
package impl.org.controlsfx.table;

import impl.org.controlsfx.table.ColumnFilter.FilterValue;

import java.util.Comparator;
import java.util.function.Function;

import javafx.beans.Observable;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkin;



public final class FilterPanel<T> extends Pane {
    
    private final ColumnFilter<T> columnFilter;
    private final ListView<CheckItem> checkListView = new ListView<>();

    private final FilteredList<CheckItem> filterList;
    private static final String promptText = "Search...";
    private final TextField searchBox = new TextField();
    private volatile boolean searchMode = false;
    
    FilterPanel(ColumnFilter<T> columnFilter) {
        this.columnFilter = columnFilter;

        //initialize search box
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(3));
        
        searchBox.setPromptText(promptText);
        searchBox.setPadding(new Insets(0, 0, 10, 0));
        vBox.getChildren().add(searchBox);

        //initialize checklist view
        Function<ColumnFilter.FilterValue,CheckItem>  newCheckItemFx = v -> {
            CheckItem chkItem = new CheckItem(v);
            chkItem.checkBox.selectedProperty().setValue(true);
            return chkItem;
        };

        filterList = new FilteredList<>(new SortedList<>(new MappedList<>(new SortedList<>(this.columnFilter.getFilterValues(), new FilterValueComparator()), newCheckItemFx)), t -> true);
        checkListView.setItems(filterList);

        vBox.getChildren().add(checkListView);
        
        //initialize apply button
        HBox bttnBox = new HBox();
        Button applyBttn = new Button("APPLY");

        applyBttn.setOnAction(e -> {
        	if (searchMode) { 
        		filterList.forEach(v -> v.filterValue.getSelectedProperty().setValue(true));
        		
        		columnFilter.getFilterValues().stream()
        			.filter(v -> filterList.stream().filter(fl -> fl.filterValue.equals(v)).findAny().isPresent() == false)
        			.forEach(v -> v.getSelectedProperty().setValue(false));
        		
        		resetSearchFilter();
        	}
        	columnFilter.applyFilter();
        });
        
        bttnBox.getChildren().add(applyBttn);
        
        
        //initialize reset buttons
        Button clearButton = new Button("RESET");

        clearButton.setOnAction(e -> {
        	columnFilter.resetAllFilters();
        	filterList.setPredicate(v -> true);
        });

        bttnBox.getChildren().add(clearButton);

        Button clearAllButton = new Button("RESET ALL");
        clearAllButton.setOnAction(e -> {
            columnFilter.resetAllFilters();
        });
        bttnBox.getChildren().add(clearAllButton);

        
        vBox.getChildren().add(bttnBox);
        this.getChildren().add(vBox);
    }
    private static final class CheckItem extends HBox {
        private final CheckBox checkBox = new CheckBox();
        private final Label label = new Label();
        private final FilterValue filterValue;
        
        CheckItem(ColumnFilter.FilterValue filterValue) {
        	this.filterValue = filterValue;
            label.setText(filterValue.getValueProperty().getValue().toString());
            
            filterValue.getInScopeProperty().addListener((Observable v) -> label.textFillProperty().set(filterValue.getInScopeProperty().get() ? Color.BLACK : Color.LIGHTGRAY));
            checkBox.selectedProperty().bindBidirectional(filterValue.getSelectedProperty());
            this.getChildren().addAll(checkBox, label);
        }
    }
    private static final class FilterValueComparator implements Comparator<FilterValue> {

		@Override
		public int compare(FilterValue first, FilterValue second) {
			if (first.getInScopeProperty().get() && !second.getInScopeProperty().get())
				return 1;
			int compare = first.getValueProperty().getValue().toString().compareTo(second.getValueProperty().getValue().toString());
			if (compare > 0) 
				return 1;
			if (compare < 0) 
				return -1;
			return 0;
		}
    	
    }
    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
        searchBox.clear();
    }
    public static <T> CustomMenuItem getInMenuItem(ColumnFilter<T> columnFilter) { 
        
        FilterPanel<T> filterPanel = new FilterPanel<T>(columnFilter);
        CustomMenuItem menuItem = new CustomMenuItem();
        
        filterPanel.initializeListeners();
        
        menuItem.contentProperty().set(filterPanel);
        
        columnFilter.getTableFilter().getTableView().skinProperty().addListener((w, o, n) -> {
            if (n instanceof TableViewSkin) {
                TableViewSkin<?> skin = (TableViewSkin<?>) n;
                    checkChangeContextMenu(skin, columnFilter.getTableColumn());
            }
        });
        menuItem.setHideOnClick(false);
        
        return menuItem;
    }
    private void initializeListeners() { 
        searchBox.textProperty().addListener(l -> {
        	searchMode = !searchBox.getText().isEmpty();
        	filterList.setPredicate(val -> searchBox.getText().isEmpty() || val.filterValue.toString().contains(searchBox.getText()));
        });
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
