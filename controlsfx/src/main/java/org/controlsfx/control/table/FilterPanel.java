/**
 * Copyright (c) 2015, 2020 ControlsFX
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

import impl.org.controlsfx.ReflectionUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static impl.org.controlsfx.i18n.Localization.getString;

public final class FilterPanel<T,R> extends VBox {

    private final ColumnFilter<T,R> columnFilter;

    private final FilteredList<FilterValue> filterList;
    private final TextField searchBox = new TextField();

    private final ListView<FilterValue> checkListView;
	
    // This collection will reference column header listeners. References must be kept locally because weak listeners are registered
    private final Collection<InvalidationListener> columnHeadersChangeListeners = new ArrayList();

    private final ChangeListener<Skin<?>> skinListener = (w, o, n) -> {
        // Clear references to listeners, this will (eventually) cause the WeakListeners to expire
        columnHeadersChangeListeners.clear();

        if (n instanceof TableViewSkin) {
            TableViewSkin<?> skin = (TableViewSkin<?>) n;
            checkChangeContextMenu(skin, getColumnFilter().getTableColumn(), this);
        }
    };

    void selectAllValues() {
        checkListView.getItems()
                .forEach(item -> item.selectedProperty().set(true));
    }
    void unSelectAllValues() {
        checkListView.getItems()
                .forEach(item -> item.selectedProperty().set(false));
    }
    void selectValue(Object value) {
        checkListView.getItems().stream().filter(item ->
                (item.getValue() == null && value == null) ||
                        (item.getValue() != null && value != null) && item.getValue().equals(value)
                    )
                .forEach(item -> item.selectedProperty().set(true));
    }
    void unSelectValue(Object value) {
        checkListView.getItems().stream().filter(item ->
                (item.getValue() == null && value == null) ||
                        (item.getValue() != null && value != null) && item.getValue().equals(value)
        ).forEach(item -> item.selectedProperty().set(false));
    }


    FilterPanel(ColumnFilter<T,R> columnFilter, ContextMenu contextMenu) {
        columnFilter.setFilterPanel(this);
        this.columnFilter = columnFilter;
        getStyleClass().add("filter-panel");

        //initialize search box
        setPadding(new Insets(3));

        searchBox.setPromptText(getString("filterpanel.search.field")); //$NON-NLS-1$
        getChildren().add(searchBox);

        //initialize checklist view

        filterList = new FilteredList<>(new SortedList<>(columnFilter.getFilterValues()), t -> true);
        checkListView = new ListView<>();
        checkListView.setItems(new SortedList<>(filterList, FilterValue::compareTo));

        getChildren().add(checkListView);

        //initialize apply button
        HBox buttonBox = new HBox();

        Button applyBttn = new Button(getString("filterpanel.apply.button")); //$NON-NLS-1$
        HBox.setHgrow(applyBttn, Priority.ALWAYS);

        applyBttn.setOnAction(e -> {
            if (columnFilter.getTableFilter().isDirty()) {
                columnFilter.applyFilter();
            }
            contextMenu.hide();
        });

        buttonBox.getChildren().add(applyBttn);

        //initialize unselect all button
        Button unselectAllButton = new Button(getString("filterpanel.none.button")); //$NON-NLS-1$
        HBox.setHgrow(unselectAllButton, Priority.ALWAYS);

        unselectAllButton.setOnAction(e -> columnFilter.getFilterValues().forEach(v -> v.selectedProperty().set(false)));
        buttonBox.getChildren().add(unselectAllButton);

        //initialize reset buttons
        Button selectAllButton = new Button(getString("filterpanel.all.button")); //$NON-NLS-1$
        HBox.setHgrow(selectAllButton, Priority.ALWAYS);

        selectAllButton.setOnAction(e -> {
            columnFilter.getFilterValues().forEach(v -> v.selectedProperty().set(true));
        });

        buttonBox.getChildren().add(selectAllButton);

        Button clearAllButton = new Button(getString("filterpanel.resetall.button")); //$NON-NLS-1$
        HBox.setHgrow(clearAllButton, Priority.ALWAYS);

        clearAllButton.setOnAction(e -> {
            columnFilter.resetAllFilters();
            columnFilter.getTableFilter().getColumnFilters().forEach(cf -> cf.getTableColumn().setGraphic(null));
            contextMenu.hide();
        });
        buttonBox.getChildren().add(clearAllButton);

        buttonBox.setAlignment(Pos.BASELINE_CENTER);


        getChildren().add(buttonBox);
    }

    public void resetSearchFilter() {
        this.filterList.setPredicate(t -> true);
        searchBox.clear();
    }
    public static <T,R> CustomMenuItem getInMenuItem(ColumnFilter<T,R> columnFilter, ContextMenu contextMenu) {

        FilterPanel<T,R> filterPanel = new FilterPanel<>(columnFilter, contextMenu);

        CustomMenuItem menuItem = new CustomMenuItem();

        filterPanel.initializeListeners();

        menuItem.contentProperty().set(filterPanel);

        columnFilter.getTableFilter().getTableView().skinProperty().addListener(new WeakChangeListener<>(filterPanel.skinListener));

        menuItem.setHideOnClick(false);
        return menuItem;
    }
    private void initializeListeners() {
        searchBox.textProperty().addListener(l -> {
            //filter scope based on search text
            filterList.setPredicate(val -> searchBox.getText().isEmpty() ||
                    columnFilter.getSearchStrategy().test(searchBox.getText(), Optional.ofNullable(val.getValue()).map(Object::toString).orElse("")));

            //unselect items out of scope
            columnFilter.getFilterValues().stream()
                    .filter(s -> !columnFilter.getSearchStrategy().test(searchBox.getText(), Optional.ofNullable(s.getValue()).map(Object::toString).orElse("")))
                    .collect(Collectors.toList()).forEach(s -> s.selectedProperty().set(false));

            //select items in scope
            columnFilter.getFilterValues().stream()
                    .filter(s -> columnFilter.getSearchStrategy().test(searchBox.getText(), Optional.ofNullable(s.getValue()).map(Object::toString).orElse("")))
                    .collect(Collectors.toList()).forEach(s -> s.selectedProperty().set(true));
        });
    }

    /* Methods below helps will anchor the context menu under the column */
    private static void checkChangeContextMenu(TableViewSkin<?> skin, TableColumn<?, ?> column, FilterPanel filterPanel) {
        ReflectionUtils.getTableHeaderRowFrom(skin).ifPresent(tableHeaderRow -> {
            ReflectionUtils.getRootHeaderFrom(tableHeaderRow).ifPresent(header -> {
                InvalidationListener listener = filterPanel.getOrCreateChangeListener(header, column);
                header.getColumnHeaders().addListener(new WeakInvalidationListener(listener));
                changeContextMenu(header, column);
            });
        });
    }

    private InvalidationListener getOrCreateChangeListener(NestedTableColumnHeader header, TableColumn<?, ?> column) {
        InvalidationListener listener = (Observable obs) -> changeContextMenu(header, column);

        // Keep a reference locally because this listener will be used with a WeakInvalidationListener
        columnHeadersChangeListeners.add(listener);

        return listener;
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

    public ColumnFilter<T,R> getColumnFilter() {
        return columnFilter;
    }
}
