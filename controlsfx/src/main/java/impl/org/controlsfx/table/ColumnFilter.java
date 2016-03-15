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

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.table.TableFilter;

import java.util.HashSet;
import java.util.function.BiPredicate;
import java.util.function.Function;

public final class ColumnFilter<T,R> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,R> tableColumn;

    private final ObservableList<FilterValue<T,R>> filterValues;
    private final ObservableList<FilterValue<T,R>> visibleValues;

    private final DupeCounter<FilterValue<T,R>> filterValuesDupeCounter = new DupeCounter<>();
    private final DupeCounter<FilterValue<T,R>> visibleValuesDupeCounter = new DupeCounter<>();
    private final HashSet<R> unselectedValues = new HashSet<>();

    private boolean lastFilter = false;
    private BiPredicate<String,String> searchStrategy = (inputString, subjectString) -> subjectString.contains(inputString);

    private final Function<T,FilterValue<T,R>> itemToFilterValue;

    public ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,R> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
        this.itemToFilterValue = t -> new FilterValue<>(tableColumn.getCellObservableValue(t), this);

        this.filterValues = FXCollections.observableArrayList(cb -> new Observable[] { cb.selectedProperty()});
        this.visibleValues = FXCollections.observableArrayList();
        this.attachContextMenu();
    }

    public void initialize() {
        initializeListeners();
        initializeValues();
    }
    public ObservableList<FilterValue<T,R>> getVisibleValues() {
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
    public boolean isFiltered() {
        return unselectedValues.size() > 0;
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

    public ObservableList<FilterValue<T,R>> getFilterValues() {
        return filterValues;
    }

    public TableColumn<T,?> getTableColumn() { 
        return tableColumn;
    }
    public TableFilter<T> getTableFilter() { 
        return tableFilter;
    }
    public boolean evaluate(T item) {
        ObservableValue<R> value = tableColumn.getCellObservableValue(item);

        return unselectedValues.size() == 0
                || unselectedValues.contains(value.getValue());
    }

    private void initializeValues() {
        tableFilter.getBackingList().stream().forEach(this::addBackingItem);

        tableFilter.getTableView().getItems().stream().forEach(this::addVisibleItem);
    }
    private void addBackingItem(T item) {
        FilterValue<T,R> newValue = itemToFilterValue.apply(item);
        if (filterValuesDupeCounter.add(newValue) == 1) {
            filterValues.add(newValue);
            newValue.initialize();
            System.out.println("Added " + newValue);
        }
    }
    private void removeBackingItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (filterValuesDupeCounter.remove(newValue) == 0) {
            filterValues.remove(newValue);
            System.out.println("Removed " + newValue);
        }
    }
    private void addVisibleItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (visibleValuesDupeCounter.add(newValue) == 1) {
            visibleValues.add(newValue);
            System.out.println("Added Visible " + newValue);
        }
    }
    private void removeVisibleItem(T item) {
        FilterValue newValue = itemToFilterValue.apply(item);
        if (visibleValuesDupeCounter.remove(newValue) == 0) {
            visibleValues.remove(newValue);
            System.out.println("Removed Visible " + newValue);
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

        //listen to selections on filterValues
        filterValues.addListener((ListChangeListener<FilterValue<T,R>>) lc -> {
            while (lc.next()) {
                if (lc.wasRemoved()) {
                    lc.getRemoved().stream()
                            .filter(v -> !v.selectedProperty().get())
                            .peek(v -> System.out.println("Removing " + v + " from selections"))
                            .forEach(unselectedValues::remove);
                }
                if (lc.wasUpdated()) {
                    int from = lc.getFrom();
                    int to = lc.getTo();
                    lc.getList().subList(from,to).forEach(v -> {
                        boolean value = v.selectedProperty().getValue();
                        if (!value) {
                            unselectedValues.add(v.valueProperty().getValue());
                            System.out.println("Unselecting " + v);
                        }
                        else {
                            unselectedValues.remove(v.valueProperty().getValue());
                            System.out.println("Selecting " + v);
                        }
                    });
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
