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

import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.controlsfx.control.CheckListView;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkin;


public final class FilterPanel<T> extends Pane {
    
    private final ColumnFilter<T> columnFilter;
    private final CheckListView<Object> checkListView = new CheckListView<>();
    private final FilteredList<Object> filterList;
    private static final String promptText = "Search...";
    private final TextField searchBox = new TextField();
    
    FilterPanel(ColumnFilter<T> columnFilter) { 
        this.columnFilter = columnFilter;
        
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(3));
        
        searchBox.setPromptText(promptText);
        vBox.getChildren().add(searchBox);
        searchBox.setPadding(new Insets(0,0,10,0)); 
        
        filterList = new FilteredList<Object>(new SortedList<Object>(columnFilter.getDistinctValues()));
        checkListView.setItems(filterList);
        
        vBox.getChildren().add(checkListView);
        
        Button applyBttn = new Button("APPLY");
        applyBttn.setOnAction(e -> columnFilter.getTableFilter().executeFilter());
        vBox.getChildren().add(applyBttn);
        
        this.getChildren().add(vBox);
    }
    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
    }
    public static <T> FilterMenuItem<T> getInMenuItem(ColumnFilter<T> columnFilter) { 
        
        FilterPanel<T> filterPanel = new FilterPanel<T>(columnFilter);
        
        FilterMenuItem<T> menuItem = new FilterMenuItem<T>(filterPanel);
        
        ListChangeListener<Object> checkListListener = l -> columnFilter.getSelectedDistinctValues().setAll(filterPanel.checkListView.getSelectionModel().getSelectedItems());
   
        
        filterPanel.checkListView.getSelectionModel().getSelectedItems().addListener(checkListListener);
        filterPanel.initializeListeners();
        
        columnFilter.getTableColumn().setOnEditCommit(e -> { 
            columnFilter.rebuildAllVals(); 
        });
        
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
    public static class FilterMenuItem<T> extends CustomMenuItem { 
        private final FilterPanel<T> filterPanel;
        private FilterMenuItem(FilterPanel<T> filterPanel) { 
            this.filterPanel = filterPanel;
        }
        public FilterPanel<T> getFilterPanel() {
            return filterPanel;
        }
    }
    private void initializeListeners() { 
        searchBox.textProperty().addListener(l -> filterList.setPredicate(val -> val.toString().contains(searchBox.getText())));
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
