/**
 * Copyright (c) 2013, 2020 ControlsFX
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

import impl.org.controlsfx.tableview2.FilteredColumnPredicate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.controlsfx.control.tableview2.event.FilterEvent;
import org.controlsfx.control.tableview2.filter.filtereditor.FilterEditor;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import org.controlsfx.control.tableview2.filter.popupfilter.PopupFilter;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A subclass of {@link TableView2} that provides extended filtering options.
 * 
 * The table items have to be wrapped with a {@link FilteredList}.
 * 
 * {@link #configureForFiltering(FilteredTableView, ObservableList) configureForFiltering}
 * is a convenient method that can be used for that purpose.
 *
 * <h3>Features</h3>
 * <br>
 * A filter icon is displayed in the column's header, and its color will show if
 * the column has a predicate applied or not.
 *
 * <br>
 * A {@link PopupFilter} control can be used to display filtering options. This
 * control can be displayed via {@link FilteredTableColumn#onFilterAction}.
 *
 * <br>Alternatively, a {@link SouthFilter} control can be placed in the south
 * header node.
 *
 * <h2>Sample</h2>
 *
 * <p>Let's provide the underlying data model, based on a <code>Person</code> class.
 *
 * <pre>
 * {@code
 * public class Person {
 *     private StringProperty firstName;
 *     public void setFirstName(String value) { firstNameProperty().set(value); }
 *     public String getFirstName() { return firstNameProperty().get(); }
 *     public StringProperty firstNameProperty() {
 *         if (firstName == null) firstName = new SimpleStringProperty(this, "firstName");
 *         return firstName;
 *     }
 *
 *     private StringProperty lastName;
 *     public void setLastName(String value) { lastNameProperty().set(value); }
 *     public String getLastName() { return lastNameProperty().get(); }
 *     public StringProperty lastNameProperty() {
 *         if (lastName == null) lastName = new SimpleStringProperty(this, "lastName");
 *         return lastName;
 *     }
 * }}</pre>
 *
 * <p>A FilteredTableView can be created, and filled with an observable list of people,
 * that has to be wrapped with a SortedList and a FilteredList, in order to apply
 * sorting and filtering:
 *
 * <pre>
 * {@code
 * FilteredTableView<Person> table = new FilteredTableView<Person>();
 * ObservableList<Person> people = getPeople();
 * FilteredList<Person> filteredPeople = new FilteredList<>(people);
 * filteredPeople.predicateProperty().bind(table.predicateProperty());
 * SortedList<Person> sortedPeople = new SortedList<>(filteredPeople);
 * sortedPeople.comparatorProperty().bind(table.comparatorProperty());
 * table.setItems(sortedPeople);
 * }</pre>
 *
 * <p>Alternatively, {@link #configureForFiltering(FilteredTableView, ObservableList) configureForFiltering}
 * can be used:
 *
 * <pre>
 * {@code
 * FilteredTableView<Person> table = new FilteredTableView<Person>();
 * ObservableList<Person> people = getPeople();
 * FilteredTableView.configureForFiltering(table, people);
 * }</pre>
 *
 * <p>Now we add two {@link FilteredTableColumn columns} to the table:
 *
 * <pre>
 * {@code
 * FilteredTableColumn<Person,String> firstNameCol = new FilteredTableColumn<>("First Name");
 * firstNameCol.setCellValueFactory(p -> p.getValue().firstNameProperty());
 * FilteredTableColumn<Person,String> lastNameCol = new FilteredTableColumn<>("Last Name");
 * lastNameCol.setCellValueFactory(p -> p.getValue().lastNameProperty());
 *
 * table.getColumns().setAll(firstNameCol, lastNameCol);}</pre>
 *
 * <p>A cell factory that allows commit on focus lost can be set:
 *
 * <pre>
 * {@code
 * firstName.setCellFactory(TextField2TableCell.forTableColumn());}</pre>
 *
 * <p>We can fix some row and columns, and also show the row header:
 *
 * <pre>
 * {@code
 * table.getFixedColumns().setAll(firstNameColumn);
 * table.getFixedRows().setAll(0, 1, 2);
 *
 * table.setRowHeaderVisible(true);}</pre>
 *
 * <p>A popup filter editor can be easily added to a column header:
 *
 * <pre>
 * {@code
 * PopupFilter<Person, String> popupFirstNameFilter = new PopupStringFilter<>(firstName);
 * firstName.setOnFilterAction(e -> popupFirstNameFilter.showPopup());}
 * </pre>
 *
 * <p>Alternatively, a south filter editor can be added to the south node:
 *
 * <pre>
 * {@code
 * SouthFilter<Person, String> editorFirstNameFilter = new SouthFilter<>(firstName, String.class);
 * firstName.setSouthNode(editorFirstNameFilter);}
 * </pre>
 *
 * @param <S> The type of the objects contained within the FilteredTableView items list.
 */
public class FilteredTableView<S> extends TableView2<S> {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/

    /**
     * Original observable list, before it is wrapped into a FilteredList and SortedList.
     * It is required to track the changes in the underlying data model (backend,
     * or cell-editing)
     */
    private ObservableList<S> backingList;

    /**
     * The default {@link #filterPolicyProperty() filter policy} that this FilteredTableView
     * will use if no other policy is specified. The filter policy is a simple
     * {@link Callback} that accepts a FilteredTableView as the sole argument and expects
     * a Boolean response representing whether the filter succeeded (true) or not
     * (false).
     */
    public static final Callback<FilteredTableView, Boolean> DEFAULT_FILTER_POLICY = table -> {
        try {
            final ObservableList items = table.getItems();
            FilteredList filteredList = null;
            if (items instanceof FilteredList) {
                filteredList = (FilteredList) items;
            } else if (items instanceof SortedList) {
                SortedList sortedList = (SortedList) items;
                if (sortedList.getSource() instanceof FilteredList) {
                    filteredList = (FilteredList) sortedList.getSource();
                }
            }
            if (filteredList != null) {
                if (table.getBackingList() == null) {
                    table.setBackingList(filteredList.getSource());
                }
                boolean predicatesBound = filteredList.predicateProperty().
                        isEqualTo(table.predicateProperty()).get();
                if (! predicatesBound) {
                    String s = "FilteredTableView items list is a FilteredList, but the FilteredList " +
                        "predicate should be bound to the FilteredTableView predicate for " +
                        "filtering to be enabled (e.g. " +
                        "filteredList.predicateProperty().bind(tableView.predicateProperty());).";
                    Logger.getLogger(FilteredTableView.class.getName()).log(Level.WARNING, s);
                }
                return predicatesBound;
            } else {
                if (items == null || items.isEmpty()) {
                    // filtering is not supported on null or empty lists
                    return true;
                }
                String s = "FilteredTableView items list is not a FilteredList. Filtering options are "
                        + "not available unless the list is wrapped with a FilteredList. "
                        + "FilteredTableView.configureForFiltering(tableView, items); is called";
                Logger.getLogger(FilteredTableView.class.getName()).log(Level.WARNING, s);

                configureForFiltering(table, items);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    };

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Creates a FilteredTableView control.
     *
     */
    public FilteredTableView() {
        super();
    }

    public FilteredTableView(ObservableList<S> items) {
        super(items);
        backingList = items;
    }

    /***************************************************************************
     * * Public static Methods * *
     **************************************************************************/

    /**
     * Convenient method to set the items for the {@link FilteredTableView}
     * by wrapping them with a {@link FilteredList} and a {@link SortedList}, that
     * are also bound properly to the table's {@link #predicateProperty() } and
     * {@link TableView#comparatorProperty()}.
     *
     * @param <S> The type of the objects contained within the FilteredTableView items list
     * @param tableView The FilteredTableView
     * @param items The {@link ObservableList items list}
     */
    public static <S> void configureForFiltering(FilteredTableView<S> tableView, ObservableList<S> items) {
        tableView.setBackingList(items);
        FilteredList<S> filteredData = new FilteredList<>(items);
        filteredData.predicateProperty().bind(tableView.predicateProperty());
        SortedList<S> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedData);
    }


    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * Sets the original observable list, before it is wrapped into a
     * {@link FilteredList} and a {@link SortedList}.
     *
     * It is required to track the changes in the underlying data model (back-end,
     * or cell editing)
     *
     * @see #configureForFiltering(FilteredTableView, ObservableList)
     * 
     * @param backingList The original {@link ObservableList}
     */
    public void setBackingList(ObservableList<S> backingList) {
        this.backingList = backingList;
    }

    // --- Predicate (built via filtered list, so read-only)
    /**
     * The predicate property is a read-only property that is representative of the
     * current state of the filter list. The filter list contains the columns 
     * that have been added to it either programmatically or via a user clicking 
     * on the headers themselves.
     */
    private ReadOnlyObjectWrapper<Predicate<S>> predicate;
    private void setPredicate(Predicate<S> value) {
        predicatePropertyImpl().set(value);
    }
    public final Predicate<S> getPredicate() {
        return predicate == null ? null : predicate.get();
    }
    public final ReadOnlyObjectProperty<Predicate<S>> predicateProperty() {
        return predicatePropertyImpl().getReadOnlyProperty();
    }
    private ReadOnlyObjectWrapper<Predicate<S>> predicatePropertyImpl() {
        if (predicate == null) {
            predicate = new ReadOnlyObjectWrapper<Predicate<S>>(this, "predicate") {
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        // when the table's predicate is set to null, so should
                        // be the filtered columns predicates
                        resetColumnsFilter();
                    }
                }
                
            };
        }
        return predicate;
    }
    
    // --- filterPolicy
    /**
     * The filter policy specifies how filtering in this FilteredTableView should be performed.
     * For example, a basic filter policy may just call
     * {@code FXCollections.filter(tableView.getItems())}, whereas a more advanced
     * filter policy may call to a database to perform the necessary filtering on the
     * server-side.
     *
     * <p>FilteredTableView ships with a {@link FilteredTableView#DEFAULT_FILTER_POLICY default
     * filter policy} that does precisely as mentioned above: it simply attempts
     * to filter the items list in-place.
     *
     * <p>It is recommended that rather than override the {@link FilteredTableView#filter() filter}
     * method that a different filter policy be provided instead.
     */
    private ObjectProperty<Callback<TableView<S>, Boolean>> filterPolicy;
    public final void setFilterPolicy(Callback<TableView<S>, Boolean> callback) {
        filterPolicyProperty().set(callback);
    }
    @SuppressWarnings("unchecked")
    public final Callback<TableView<S>, Boolean> getFilterPolicy() {
        return filterPolicy == null ?
                (Callback<TableView<S>, Boolean>)(Object) DEFAULT_FILTER_POLICY :
                filterPolicy.get();
    }
    @SuppressWarnings("unchecked")
    public final ObjectProperty<Callback<TableView<S>, Boolean>> filterPolicyProperty() {
        if (filterPolicy == null) {
            filterPolicy = new SimpleObjectProperty<Callback<TableView<S>, Boolean>>(
                    this, "filterPolicy", (Callback<TableView<S>, Boolean>)(Object) DEFAULT_FILTER_POLICY) {
                @Override protected void invalidated() {
                    filter();
                }
            };
        }
        return filterPolicy;
    }

    // onFilter
    /**
     * Called when there's a request to filter the control.
     */
    private ObjectProperty<EventHandler<FilterEvent<TableView<S>>>> onFilter;

    public final void setOnFilter(EventHandler<FilterEvent<TableView<S>>> value) {
        onFilterProperty().set(value);
    }

    public final EventHandler<FilterEvent<TableView<S>>> getOnFilter() {
        if (onFilter != null) {
            return onFilter.get();
        }
        return null;
    }

    public final ObjectProperty<EventHandler<FilterEvent<TableView<S>>>> onFilterProperty() {
        if (onFilter == null) {
            onFilter = new ObjectPropertyBase<EventHandler<FilterEvent<TableView<S>>>>() {
                @Override protected void invalidated() {
                    EventType<FilterEvent<TableView<S>>> eventType = FilterEvent.filterEvent();
                    EventHandler<FilterEvent<TableView<S>>> eventHandler = get();
                    setEventHandler(eventType, eventHandler);
                }

                @Override public Object getBean() {
                    return FilteredTableView.this;
                }

                @Override public String getName() {
                    return "onFilter";
                }
            };
        }
        return onFilter;
    }
    
    /**
     * Resets all the filters applied, to both tableView and filtered columns
     */
    public void resetFilter() {
        setPredicate(null);
        resetColumnsFilter();
    }
    
    /**
     * The filter method forces the TableView to re-run its filtering algorithm. More
     * often than not it is not necessary to call this method directly, as it is
     * automatically called when the 
     * {@link #filterPolicyProperty() filter policy}, or the state of the
     * FilteredTableColumn {@link FilteredTableColumn#predicateProperty() filter predicate}
     * changes. In other words, this method should only be called directly when
     * something external changes and a filter is required.
     */
    public void filter() {
        
        Predicate<S> oldPredicate = getPredicate();
        
        final boolean filterExists = getVisibleLeafColumns().stream()
                .filter(FilteredTableColumn.class::isInstance)
                .map(FilteredTableColumn.class::cast)
                .filter(FilteredTableColumn::isFilterable)
                .noneMatch(f -> f.getPredicate() != null);
        
        // update the Predicate property
        setPredicate(filterExists ? null : new FilteredColumnPredicate(getVisibleLeafColumns()));

        // fire the onFilter event and check if it is consumed, if so, don't run the filtering
        FilterEvent<TableView<S>> filterEvent = new FilterEvent<>(FilteredTableView.this, FilteredTableView.this);
        fireEvent(filterEvent);
        if (filterEvent.isConsumed()) {
            setPredicate(oldPredicate);
            return;
        }
        // get the filter policy and run it
        Callback<TableView<S>, Boolean> filterPolicy = getFilterPolicy();
        if (filterPolicy == null) {
            return;
        }
        boolean success = filterPolicy.call(this);
        if (! success) {
            setPredicate(oldPredicate);
        }

    }
    
    /***************************************************************************
     * * Protected/Private Methods * *
     **************************************************************************/

    /**
     * Returns the original observable list, before it is wrapped into a 
     * {@link FilteredList} and a {@link SortedList}.
     *
     * @return The original {@link ObservableList}
     */
    ObservableList<S> getBackingList() {
        return backingList;
    }
    
    private void resetColumnsFilter() {
        getVisibleLeafColumns().stream()
                .filter(FilteredTableColumn.class::isInstance)
                .map(FilteredTableColumn.class::cast)
                .filter(FilteredTableColumn::isFilterable)
                .peek(c -> c.setPredicate(null))
                .map(f -> f.getSouthNode())
                .filter(Objects::nonNull)
                .filter(SouthFilter.class::isInstance)
                .map(f -> ((SouthFilter) f).getFilterEditor())
                .forEach(FilterEditor::cancelFilter);
    }
}
