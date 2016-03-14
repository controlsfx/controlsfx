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

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.table.TableFilter;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ColumnFilter<T> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;

    private final ObservableList<FilterValue> filterValues;
    private final ObservableList<FilterValue> visibleValues;

    private final DupeCounter<Object> filterValuesDupeCounter = new DupeCounter<>();
    private final DupeCounter<Object> visibleValuesDupeCounter = new DupeCounter<>();

    private boolean lastFilter = false;
    private BiPredicate<String,String> searchStrategy = (inputString, subjectString) -> subjectString.contains(inputString);


    private final Function<T,FilterValue> itemToFilterValue;

    public ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
        this.itemToFilterValue = t -> new FilterValue(tableColumn.getCellObservableValue(t),this);

        //Build distinct mapped list of filter values from column values
       /* this.filterValues = new DistinctMappingList<>(tableFilter.getBackingList(),
                v -> new FilterValue(tableColumn.getCellObservableValue(v), this));

        this.visibleValues = new MappedList<>(tableFilter.getTableView().getItems(),
                v -> new FilterValue(tableColumn.getCellObservableValue(v), this));
                */

        this.filterValues = FXCollections.observableArrayList();
        this.visibleValues = FXCollections.observableArrayList();
        this.attachContextMenu();
    }

    public void initialize() {
        initializeValues();
        initializeListeners();
    }
    public ObservableList<FilterValue> getVisibleValues() {
        return visibleValues;
    }
    public boolean wasLastFiltered() {
        return lastFilter;
    }
    public void setSearchStrategy(BiPredicate<String,String> searchStrategy) {
        this.searchStrategy = searchStrategy;
    }
    public BiPredicate<String,String> getSearchStrategy() {
        return searchStrategy;
    }
    public void applyFilter() {
    	tableFilter.executeFilter();
    	lastFilter = true;
    	tableFilter.getColumnFilters().stream().filter(c -> !c.equals(this)).forEach(c -> c.lastFilter = false);
    	tableFilter.getColumnFilters().stream().flatMap(c -> c.filterValues.stream()).forEach(FilterValue::refreshScope);
    }

    public void resetAllFilters() { 
    	tableFilter.getColumnFilters().stream().flatMap(c -> c.filterValues.stream()).forEach(fv -> fv.selectedProperty().set(true));
    	tableFilter.executeFilter();
    	tableFilter.getColumnFilters().stream().forEach(c -> c.lastFilter = false);
    	tableFilter.getColumnFilters().stream().flatMap(c -> c.filterValues.stream()).forEach(FilterValue::refreshScope);
    }
    public boolean isFiltered() {
        return filterValues.stream().filter(v -> !v.selectedProperty().get()).findAny().isPresent();
    }

    public ObservableList<FilterValue> getFilterValues() {
        return filterValues;
    }

    public TableColumn<T,?> getTableColumn() { 
        return tableColumn;
    }
    public TableFilter<T> getTableFilter() { 
        return tableFilter;
    }

    public Optional<FilterValue> getFilterValue(ObservableValue<?> value) {
        return filterValues.stream().filter(fv -> Optional.ofNullable(fv.valueProperty())
                    .map(ObservableValue::getValue)
                        .equals(Optional.ofNullable(value).map(ObservableValue::getValue)))
                .findAny();
    }

    private void initializeValues() {
        tableFilter.getBackingList().stream()
                .map(itemToFilterValue)
                .filter(t -> filterValuesDupeCounter.add(t) == 1).forEach(filterValues::add);

        tableFilter.getBackingList().stream()
                .map(itemToFilterValue)
                .filter(t -> visibleValuesDupeCounter.add(t) == 1).forEach(visibleValues::add);
    }
    private void addBackingItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (filterValuesDupeCounter.add(newValue) == 1) {
            filterValues.add(newValue);
        }
    }
    private void removeBackingItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (filterValuesDupeCounter.remove(newValue) == 0) {
            filterValues.remove(newValue);
        }
    }
    private void addVisibleItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (visibleValuesDupeCounter.add(newValue) == 1) {
            visibleValues.add(newValue);
        }
    }
    private void removeVisibleItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (visibleValuesDupeCounter.remove(newValue) == 0) {
            visibleValues.remove(newValue);
        }
    }
    private void initializeListeners() {

        //listen to backing list and update distinct values accordingly
        tableFilter.getBackingList().addListener((ListChangeListener<T>) lc -> {
            while (lc.next()) {
                if (lc.wasAdded()) {
                    lc.getAddedSubList().stream().forEach(this::addBackingItem);
                }
                if (lc.wasRemoved()) {
                    lc.getRemoved().stream().forEach(this::removeBackingItem);
                }
            }
        });

        //listen to visible items and update visible values accordingly
        tableFilter.getTableView().getItems().addListener((ListChangeListener<T>) lc -> {
            while (lc.next()) {
                if (lc.wasAdded()) {
                    lc.getAddedSubList().stream().forEach(this::addVisibleItem);
                }
                if (lc.wasRemoved()) {
                    lc.getRemoved().stream().forEach(this::removeVisibleItem);
                }
            }
        });
    }

    /**Leverages tableColumn's context menu to attach filter panel */
    private void attachContextMenu() {
        CustomMenuItem item = FilterPanel.getInMenuItem(this);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("column-filter");
        contextMenu.getItems().add(item);

        tableColumn.setContextMenu(contextMenu);
    }
}
