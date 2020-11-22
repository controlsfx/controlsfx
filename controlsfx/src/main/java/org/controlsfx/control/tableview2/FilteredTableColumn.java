/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package org.controlsfx.control.tableview2;

import impl.org.controlsfx.tableview2.RowHeader;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An extension of {@link TableColumn2} that allows filtering options.
 * 
 * A filter button is displayed in the column header. If no predicate is applied
 * to the column, it is grayed, else it is blue.
 * 
 * The developer can select the action that will be performed when this button
 * is clicked.
 * 
 * @param <S> The type of the objects contained within the TableView items list.
 * @param <T> The type of the content in all cells in this TableColumn
 */
public class FilteredTableColumn<S, T> extends TableColumn2<S, T> {

    private final static PseudoClass FILTER_ON = PseudoClass.getPseudoClass("filtered");

    /***************************************************************************
     * 
     * Private Fields
     * 
     **************************************************************************/
    
    private final HashMap<CellIdentity<S>, ChangeListener<T>> trackedCells = new HashMap<>();
    private final Button filterButton;
    
    private final ListChangeListener<S> backingListListener = lc -> {
        while (lc.next()) {
            if (lc.wasAdded()) {
                lc.getAddedSubList()
                        .forEach(s -> addBackingItem(s, getCellObservableValue(s)));
            }
            if (lc.wasRemoved()) {
                lc.getRemoved()
                        .forEach(s -> removeBackingItem(s, getCellObservableValue(s)));
            }
        }
    };
    private final WeakListChangeListener<S> weakListChangeListener = new WeakListChangeListener<>(backingListListener);
    
    private final ChangeListener<T> changeListener = (obs, ov, nv) ->
        Platform.runLater(() -> runOnFilteredTableView(FilteredTableView::filter));
    
    private final ChangeListener<Predicate<?>> filterListener = (obs, ov, nv) -> updateButton(nv != null);
    private final WeakChangeListener<Predicate<?>> weakFilterListener = new WeakChangeListener<>(filterListener);
    
    private final InvalidationListener tableListener = o -> init();
    private final WeakInvalidationListener weakTableListener = new WeakInvalidationListener(tableListener);
    
    private InvalidationListener itemsPropertyListener;
    private WeakInvalidationListener weakItemsPropertyListener;
    
    private InvalidationListener parentListener;
    private WeakInvalidationListener weakParentListener;
    
    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/

    /**
     * Creates a FilteredTableColumn control.
     */
    public FilteredTableColumn() {
        super();
        tableViewProperty().addListener(weakTableListener);
        
        filterButton = new Button();
        filterButton.getStyleClass().add("filter");
        filterButton.onActionProperty().bind(onFilterActionProperty());
        filterButton.disableProperty().bind(filterableProperty().not());
        setGraphic(filterButton);
        updateButton(getPredicate() != null);
        predicateProperty().addListener(weakFilterListener);
    }
    
    public FilteredTableColumn(String text) {
        this();
        setText(text);
    }
    
    /***************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    /**
     * When the filterable property is set to true, the column can be filtered. 
     */
    private final BooleanProperty filterable = new SimpleBooleanProperty(this, "filterable", true);
    public final void setFilterable(boolean value) { filterable.set(value); }
    public final boolean isFilterable() { return filterable.get(); }
    public final BooleanProperty filterableProperty() { return filterable; }
    
    /**
     * This property allows defining a predicate for the column. 
     * This predicate can be nullified when the table's predicate is reset,
     * so it is convenient that this property can be set again dynamically via
     * the UI option {@link #southNode}.
     */
    private final ObjectProperty<Predicate<? super T>> predicate = new SimpleObjectProperty<Predicate<? super T>>(this, "predicate", null) {
        @Override
        protected void invalidated() {
            // Auto filter table based on column's predicate changes
            getFilteredTableView().ifPresent(FilteredTableView::filter);
        }
    };
    public final void setPredicate(Predicate<? super T> value) { predicate.set(value); }
    public final Predicate<? super T> getPredicate() { return predicate.get(); }
    public final ObservableValue<Predicate<? super T>> predicateProperty() { return predicate; }
    
    /**
     * The filter button's action, which is invoked whenever the filter button is 
     * fired.
     */
    private final ObjectProperty<EventHandler<ActionEvent>> onFilterAction = new SimpleObjectProperty<>(this, "onFilterAction", null);
    public final ObjectProperty<EventHandler<ActionEvent>> onFilterActionProperty() { return onFilterAction; }
    public final void setOnFilterAction(EventHandler<ActionEvent> value) { onFilterAction.set(value); }
    public final EventHandler<ActionEvent> getOnFilterAction() { return onFilterAction.get(); }
    
    /***************************************************************************
     * 
     * Private Implementation
     * 
     **************************************************************************/

    private void init() {
        runOnFilteredTableView(filteredTableView -> {
            itemsPropertyListener = (Observable o) -> {
                if (filteredTableView.getItems() != null) {
                    ObservableList<S> backingList = filteredTableView.getBackingList();
                    if (backingList != null) {
                        backingList.forEach(s -> addBackingItem(s, getCellObservableValue(s)));
                        backingList.addListener(weakListChangeListener);
                    }
                    filteredTableView.itemsProperty().removeListener(weakItemsPropertyListener);
                }
            };
            weakItemsPropertyListener = new WeakInvalidationListener(itemsPropertyListener);
            filteredTableView.itemsProperty().addListener(weakItemsPropertyListener);
            
            if (filteredTableView.getParent() != null) {
                updateFilterButton(filteredTableView);
            } else {
                parentListener = (Observable o) -> {
                    if (filteredTableView.getParent() != null) {
                        updateFilterButton(filteredTableView);
                        filteredTableView.parentProperty().removeListener(weakParentListener);
                    }
                };
                weakParentListener = new WeakInvalidationListener(parentListener);
                filteredTableView.parentProperty().addListener(weakParentListener);
            }
        });
        
    }
       
    private void updateButton(boolean value) {
        filterButton.pseudoClassStateChanged(FILTER_ON, value);
    }
    
    private void updateFilterButton(TableView<S> tableView) {
        if (tableView.getParent() instanceof RowHeader) {
            FilteredTableView<S> parentTableView = (FilteredTableView<S>) ((RowHeader) tableView.getParent()).getParentTableView();
            updateButton(parentTableView.getPredicate() != null);
            parentTableView.predicateProperty().addListener(weakFilterListener);
        }
    }
    
    private void addBackingItem(S item, ObservableValue<T> cellValue) {
        if (cellValue == null) {
            return;
        }
        //listen to cell value and track it
        CellIdentity<S> trackedCellValue = new CellIdentity<>(item);

        ChangeListener<T> cellListener = new WeakChangeListener(changeListener);
        cellValue.addListener(cellListener);
        trackedCells.put(trackedCellValue, cellListener);
    }
    
    private void removeBackingItem(S item, ObservableValue<T> cellValue) {
        if (cellValue == null) {
            return;
        }
        //remove listener from cell
        ChangeListener<T> listener = trackedCells.remove(new CellIdentity<>(item));
        cellValue.removeListener(listener);
    }
    
    private void runOnFilteredTableView(Consumer<FilteredTableView> consumer) {
        getFilteredTableView().ifPresent(consumer);
    }
     
    private Optional<FilteredTableView<S>> getFilteredTableView() {
        TableView<S> tableView = getTableView();
        if (tableView != null && tableView instanceof FilteredTableView) {
            return Optional.of((FilteredTableView<S>) tableView);
        }
        return Optional.empty();
    }
    
    private static final class CellIdentity<S> {
        private final S item;

        CellIdentity(S item) {
            this.item = item;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CellIdentity<?> other = (CellIdentity<?>) obj;
            return Objects.equals(this.item, other.item);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(item);
        }
    }
}
