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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.table.TableFilter;

import java.util.Optional;
import java.util.function.BiPredicate;

public final class ColumnFilter<T> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;

    private final DistinctMappingList<T,FilterValue> filterValues;
    private final MappedList<FilterValue,T> scopedValues;
    private volatile boolean lastFilter = false;

    private BiPredicate<String,String> searchStrategy = (inputString, subjectString) -> subjectString.contains(inputString);

    public ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;

        //Build distinct mapped list of filter values from column values
        this.filterValues = new DistinctMappingList<>(tableFilter.getBackingList(),
                v -> new FilterValue(tableColumn.getCellObservableValue(v), this));

        this.scopedValues = new MappedList<>(tableFilter.getTableView().getItems(),
                v -> new FilterValue(tableColumn.getCellObservableValue(v), this));
        
        this.attachContextMenu();
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
    	tableFilter.getColumnFilters().stream().flatMap(c -> c.filterValues.stream()).forEach(fv -> fv.isSelected.set(true));
    	tableFilter.executeFilter();
    	tableFilter.getColumnFilters().stream().forEach(c -> c.lastFilter = false);
    	tableFilter.getColumnFilters().stream().flatMap(c -> c.filterValues.stream()).forEach(FilterValue::refreshScope);
    }
    public static final class FilterValue {

        private final ObservableValue<?> value;
        private final BooleanProperty isSelected = new SimpleBooleanProperty(true);
        private final BooleanProperty inScope = new SimpleBooleanProperty(true);
        private final ColumnFilter<?> columnFilter;
        
        private FilterValue(ObservableValue<?> value, ColumnFilter<?> columnFilter) {
            this.value = value;
            this.columnFilter = columnFilter;
        }
        public ObservableValue<?> getValueProperty() {
            return value;
        }
        public BooleanProperty getSelectedProperty() {
            return isSelected;
        }
        public BooleanProperty getInScopeProperty() { 
        	return inScope;
        }
        private void refreshScope() { 
        	inScope.setValue(columnFilter.lastFilter || columnFilter.scopedValues.contains(this));
        }

        @Override
        public String toString() {
            return value.getValue().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterValue that = (FilterValue) o;

            return Optional.ofNullable(value).map(ObservableValue::getValue).equals(Optional.ofNullable(that.value).map(ObservableValue::getValue))
                    || Optional.ofNullable(value.getValue()).equals(Optional.ofNullable(that.value.getValue()));

        }

        @Override
        public int hashCode() {
            return value == null || value.getValue() == null ? 0 : value.getValue().hashCode();
        }
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
        return filterValues.stream().filter(fv -> Optional.ofNullable(fv.value).map(ObservableValue::getValue)
                .equals(Optional.ofNullable(value).map(ObservableValue::getValue))).findAny();
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
