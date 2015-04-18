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
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.collections.transformation.TransformationList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.table.TableFilter;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public final class ColumnFilter<T> implements IndexedCheckModel {
    private final TableFilter<T> tableFilter;
    private final TableColumn<T,?> tableColumn;

    private final ObservableDistinctList<T,FilterValue<?>> filterValues;
    private final ObservableList<FilterValue<?>> checkedFilterValues;
    private final TransformationList<Integer,FilterValue<?>> checkedIndexes;

    public ColumnFilter(TableFilter<T> tableFilter, TableColumn<T,?> tableColumn) {
        this.tableFilter = tableFilter;
        this.tableColumn = tableColumn;
        this.filterValues = new ObservableDistinctList<>(tableFilter.getBackingList(),
                v -> new FilterValue<>(tableColumn.getCellObservableValue(v)));
        this.checkedFilterValues = new FilteredList<>(new SortedList<>(filterValues));

        this.checkedIndexes = new MappedList<>(checkedFilterValues,
                v -> IntStream.range(0,filterValues.size()).filter(i -> filterValues.get(i).equals(v)).findAny().orElse(-1));

        this.attachContextMenu();
    }

    @Override
    public int getItemCount() {
        return filterValues.size();
    }

    @Override
    public ObservableList getCheckedItems() {
        return checkedFilterValues;
    }

    @Override
    public void checkAll() {
        filterValues.forEach(v -> v.getSelectedProperty().setValue(true));
    }

    @Override
    public void clearCheck(Object item) {
        filterValues.stream().filter(v -> v.equals(item)).forEach(v -> v.getSelectedProperty().setValue(false));
    }

    @Override
    public void clearChecks() {
        filterValues.forEach(v -> v.getSelectedProperty().setValue(false));
    }

    @Override
    public boolean isEmpty() {
        return filterValues.size() == 0;
    }

    @Override
    public boolean isChecked(Object item) {
        return filterValues.stream().filter(v -> v.equals(item)).findAny().map(v -> v.getSelectedProperty().getValue()).orElseThrow(() -> new RuntimeException("Item " + item + " not found"));
    }

    @Override
    public void check(Object item) {
        filterValues.stream().filter(v -> v.equals(item)).forEach(v -> v.getSelectedProperty().setValue(true));
    }

    @Override
    public Object getItem(int index) {
        return filterValues.get(index);
    }

    @Override
    public int getItemIndex(Object item) {
        return IntStream.range(0,filterValues.size()).filter(i -> filterValues.get(i).equals(item)).findAny().orElseThrow(() -> new RuntimeException("Item " + item + " not found"));
    }

    @Override
    public ObservableList<Integer> getCheckedIndices() {
        return checkedIndexes;
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
        return   filterValues.get(index).getSelectedProperty().getValue();
    }

    @Override
    public void check(int index) {
        filterValues.get(index).getSelectedProperty().setValue(true);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FilterValue<?> that = (FilterValue<?>) o;

            return value.getValue().equals(that.value.getValue());

        }

        @Override
        public int hashCode() {
            return value.hashCode();
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

    private void attachContextMenu() {
        FilterPanel.FilterMenuItem<T> item = FilterPanel.getInMenuItem(this);
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(item);
        tableColumn.setContextMenu(contextMenu);

        //contextMenu.setOnHiding(e -> item.getFilterPanel().resetSearchFilter());
    }
}
