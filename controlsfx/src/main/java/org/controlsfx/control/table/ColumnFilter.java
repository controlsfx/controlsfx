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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.table.FilterPanel.FilterMenuItem;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ColumnFilter<T> implements IndexedCheckModel<ColumnFilter.FilterValue<?>> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;

    private final ObservableList<FilterValue<?>> filterValues = FXCollections.observableArrayList();
    private final FilteredList<FilterValue<?>> selectedValues = new FilteredList<>(new SortedList<>(filterValues), v -> v.getSelectedProperty().getValue());
    private final FilteredList<FilterValue<?>> unselectedValues = new FilteredList<>(new SortedList<>(filterValues), v -> ! v.getSelectedProperty().getValue());


    private ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
    }

    @Override
    public FilterValue<?> getItem(int index) {
        return filterValues.get(index);
    }

    @Override
    public int getItemIndex(FilterValue<?> item) {
        return IntStream.range(0,filterValues.size()).filter(i -> filterValues.get(i).equals(item)).findAny().getAsInt();
    }

    @Override
    public ObservableList<Integer> getCheckedIndices() {
        return FXCollections.observableArrayList(IntStream.range(0, filterValues.size()).filter(i -> filterValues.get(i).getSelectedProperty().get())
                .mapToObj(i -> Integer.valueOf(i)).collect(Collectors.toList()));
    }

    @Override
    public void checkIndices(int... indices) {
        Arrays.stream(indices).forEach(i -> filterValues.get(i).getSelectedProperty().setValue(true));
    }

    @Override
    public void clearCheck(int index) {
        filterValues.get(index).getSelectedProperty().setValue(false);
    }

    @Override
    public boolean isChecked(int index) {
        return filterValues.get(index).getSelectedProperty().getValue();
    }

    @Override
    public void check(int index) {
        filterValues.get(index).getSelectedProperty().setValue(true);
    }

    @Override
    public int getItemCount() {
        return filterValues.size();
    }


    @Override
    public ObservableList<FilterValue<?>> getCheckedItems() {
        return selectedValues;
    }

    @Override
    public void checkAll() {
        filterValues.stream().forEach(v -> v.getSelectedProperty().setValue(true));
    }

    @Override
    public void clearCheck(FilterValue<?> item) {
        item.getSelectedProperty().setValue(false);
    }

    @Override
    public void clearChecks() {
        filterValues.stream().forEach(v -> v.getSelectedProperty().setValue(false));
    }

    @Override
    public boolean isEmpty() {
        return filterValues.size() == 0;
    }

    @Override
    public boolean isChecked(FilterValue<?> item) {
        return item.getSelectedProperty().getValue();
    }

    @Override
    public void check(FilterValue<?> item) {
        item.getSelectedProperty().setValue(true);
    }

    static final class FilterValue<V> {

        private final ObservableValue<V> value;
        private final BooleanProperty isSelected = new SimpleBooleanProperty(true);

        private FilterValue(ObservableValue<V> value) {
            this.value = value;
            isSelected.addListener(c -> System.out.println("FilterValue " + value + " set to " + isSelected.getValue()));
        }
        public ObservableValue<V> getValueProperty() {
            return value;
        }
        public BooleanProperty getSelectedProperty() {
            return isSelected;
        }

        @Override
        public String toString() {
            return value.getValue().toString();
        }
    }
    public ObservableList<FilterValue<?>> getFilterValues() {
        return filterValues;
    }

    public TableColumn<T,?> getTableColumn() { 
        return tableColumn;
    }
    public TableFilter<T> getTableFilter() { 
        return tableFilter;
    }

    public FilterValue<?> getFilterValue(ObservableValue<?> value) {
        return filterValues.stream().filter(fv -> fv.value.getValue().equals(value.getValue())).findAny()
                .orElseThrow(() ->
                        new RuntimeException("Value " + value.getValue() +
                                " is not in the filterValues for column "
                                + this.tableColumn.getText()));
    }
    public void rebuildAllVals() {

        final List<FilterValue<?>> previousValues = filterValues.stream().collect(Collectors.toList());

        final ConcurrentHashMap<Object,Boolean> distinctMap = new ConcurrentHashMap<>();

        filterValues.clear();
        tableFilter.getBackingList().stream().map(item -> tableColumn.getCellObservableValue(item))
                .filter(ov -> distinctMap.putIfAbsent(ov.getValue(), Boolean.TRUE) == null)
                .map(v -> new FilterValue<>(v))
                .forEach(v -> filterValues.add(v));

/*
        filterValues.stream().filter(f -> unSelectedValues.stream().filter(uv -> uv.getValueProperty().getValue().equals(f.getValueProperty().getValue())).findAny().isPresent())
                .forEach(v -> v.isSelected.setValue(false));
                */
    }

    private void attachContextMenu() { 
        FilterMenuItem<T> item = FilterPanel.getInMenuItem(this);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(item);
        tableColumn.setContextMenu(contextMenu);
        
        //contextMenu.setOnHiding(e -> item.getFilterPanel().resetSearchFilter());
    }
    static <T> ColumnFilter<T> getInstance(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) { 
        final ColumnFilter<T> columnFilter = new ColumnFilter<>(tableFilter, tableColumn);
        columnFilter.rebuildAllVals();

        columnFilter.attachContextMenu();
        
        return columnFilter;
    }
}
