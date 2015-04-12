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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.table.TableFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ColumnFilter<T> {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;

    private final ObservableList<FilterValue<?>> filterValues = FXCollections.observableArrayList();

    public ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
        this.rebuildAllVals();
        this.attachContextMenu();

        tableFilter.getBackingList().addListener((ListChangeListener.Change<? extends T> c) -> {
            while (c.next()) {

               final List<FilterValue<?>> newValues = c.getAddedSubList().stream()
                       .map(v -> new FilterValue<>(tableColumn.getCellObservableValue(v)))
                       .distinct()
                       .filter(v -> !filterValues.contains(v))
                       .collect(Collectors.toList());

                if (newValues.size() > 0)
                System.out.println("COLUMN " + tableColumn.getText() + " HAD " + newValues.size() + " distinct values added");

                filterValues.addAll(newValues);

                final List<FilterValue<?>> removeValues = c.getRemoved().stream()
                        .map(v -> new FilterValue<>(tableColumn.getCellObservableValue(v)))
                        .distinct()
                        .filter(v -> tableFilter.getTableView().getItems().stream()
                                    .map(cv -> new FilterValue<>(tableColumn.getCellObservableValue(cv)))
                                    .filter(cv -> cv.equals(v)).findAny().isPresent() == false)
                        .collect(Collectors.toList());

                if (removeValues.size() > 0)
                System.out.println("COLUMN " + tableColumn.getText() + " HAD " + removeValues.size() + " distinct values removed");

                filterValues.removeAll(removeValues);
            }
        });

    }


    public static final class FilterValue<V> {

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
        @Override
        public int hashCode() {
            return value.hashCode();
        }
        @Override
        public boolean equals(Object other) {
            if (other == null)
                return false;
            if (! (other instanceof FilterValue<?>))
                return false;
            if (((FilterValue<?>) other).value.getValue().getClass() != this.value.getValue().getClass())
                return false;
            if (((FilterValue<?>) other).value.getValue().equals(this.value.getValue()) == false)
                return false;
            return true;
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

    public Optional<FilterValue<?>> getFilterValue(ObservableValue<?> value) {
        return filterValues.stream().filter(fv -> fv.value.getValue().equals(value.getValue())).findAny();
    }
    public void rebuildAllVals() {
        final HashMap<Object,Boolean> distinctMap = new HashMap<>();

        //capture the current values and store as old values
        final List<FilterValue<?>> oldValues = filterValues.stream().collect(Collectors.toList());

        //extract new list of distinct values with freshest set of table data
        final List<FilterValue<?>> newValues = tableFilter.getBackingList().stream().map(item -> tableColumn.getCellObservableValue(item))
                .filter(ov -> distinctMap.putIfAbsent(ov.getValue(), Boolean.TRUE) == null)
                .map(v -> new FilterValue<>(v))
                .collect(Collectors.toList());

        //remove old values that no longer exist with current data set
        oldValues.stream().filter(ov -> newValues.stream().filter(nv -> nv.equals(ov)).findAny().isPresent() == false).forEach(ov -> filterValues.remove(ov));

        //add new values that are in current data and not in incumbent distinct value set
        newValues.stream().filter(nv -> oldValues.stream().filter(ov -> ov.equals(nv)).findAny().isPresent() == false).forEach(nv -> filterValues.add(nv));
    }

    private void attachContextMenu() {
        FilterPanel.FilterMenuItem<T> item = FilterPanel.getInMenuItem(this);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(item);
        tableColumn.setContextMenu(contextMenu);

        //contextMenu.setOnHiding(e -> item.getFilterPanel().resetSearchFilter());
    }
}
