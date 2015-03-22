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

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.beans.Observable;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckListView;


public final class FilterPanel<T> extends Pane {
    
    private final ColumnFilter<T> columnFilter;
    private final CheckListView<ColumnFilter.FilterValue<?>> checkListView = new CheckListView<>();
    private final FilteredList<ColumnFilter.FilterValue<?>> filterList;
    private static final String promptText = "Search...";
    private final TextField searchBox = new TextField();


    private static final class FilterItemCell extends CheckBoxListCell<ColumnFilter.FilterValue<?>> {
       private FilterItemCell() {}
        @Override
        public void updateItem(ColumnFilter.FilterValue<?> item, boolean empty) {
            super.updateItem(item, empty);
            setText(item == null ? "" : item.getValueProperty().getValue().toString());
        }
    }
    FilterPanel(ColumnFilter<T> columnFilter) { 
        this.columnFilter = columnFilter;

        //initialize search box
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(3));
        
        searchBox.setPromptText(promptText);
        vBox.getChildren().add(searchBox);
        searchBox.setPadding(new Insets(0,0,10,0));


        filterList = new FilteredList<>(new SortedList<>(columnFilter.getFilterValues()), t -> true);
        checkListView.setItems(filterList);

       filterList.stream().forEach(item -> checkListView.getItemBooleanProperty(item).bindBidirectional(item.getSelectedProperty()));

        vBox.getChildren().add(checkListView);
        
        //initialize apply button
        HBox bttnBox = new HBox();
        Button applyBttn = new Button("APPLY");
        
        applyBttn.setOnAction(e -> { 
            columnFilter.getTableFilter().executeFilter(); 
            searchBox.clear(); 
        });
        
        bttnBox.getChildren().add(applyBttn);
        
        
        //initialize clear button
        Button clearButton = new Button("CLEAR");
        
        clearButton.setOnAction(e -> { 
            columnFilter.getTableFilter().getFilteredList().setPredicate(t -> true);
            searchBox.clear(); 
        });
        
        bttnBox.getChildren().add(clearButton);
        
        vBox.getChildren().add(bttnBox);
        this.getChildren().add(vBox);
    }
    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
    }
    public static <T> FilterMenuItem<T> getInMenuItem(ColumnFilter<T> columnFilter) { 
        
        FilterPanel<T> filterPanel = new FilterPanel<T>(columnFilter);
        FilterMenuItem<T> menuItem = new FilterMenuItem<T>(filterPanel);
        
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
        searchBox.textProperty().addListener(l -> filterList.setPredicate(val -> searchBox.getText().isEmpty() || val.toString().contains(searchBox.getText())));


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
