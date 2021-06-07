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
package org.controlsfx.control.tableview2.filter.filtereditor;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.filtermenubutton.FilterMenuButton;

import java.util.function.Predicate;

/**
 * The FilterEditor allows filtering the column by adding text to its editor or
 * by selecting an item from the popup list.
 * 
 * @param <S> Type of the objects contained within the 
 *      {@link org.controlsfx.control.tableview2.FilteredTableView} items list.
 * @param <T> Type of the content to be filtered,
 *           which is similar to the type of cells contained in the 
 *      {@link FilteredTableColumn}.
 */
public class FilterEditor<S, T> extends ComboBox<T> {

    private static final PseudoClass PSEUDO_CLASS_ERROR = PseudoClass.getPseudoClass("error");

    private final FilteredTableColumn<S, T> tableColumn;
    private final ObservableList<T> filterValues;

    private final ListChangeListener<S> itemsListener = lc -> loadItems();
    private final WeakListChangeListener<S> weakListChangeListener = new WeakListChangeListener<>(itemsListener);
    
    private final ChangeListener<String> editorListener = (obs, ov, nv) -> {
        if (getTableColumn() == null || getTableColumn().getTableView() == null) {
            return;
        }
        ObservableList<S> items = getTableColumn().getTableView().getItems();
        if (items != null) {
            items.removeListener(weakListChangeListener);
        } 
        updatePredicate(nv);
        if (items != null) {
            items.addListener(weakListChangeListener);
        }
    };
    private final WeakChangeListener<String> weakEditorListener = new WeakChangeListener<>(editorListener);
    
    private final ChangeListener<ObservableList<S>> itemsPropertyListener = (obs, ov, nv) -> {
            if (ov != null) {
                ov.removeListener(weakListChangeListener);
            }
            if (nv != null) {
                nv.addListener(weakListChangeListener);
            } 
            Platform.runLater(() -> loadItems());
        };
    private final WeakChangeListener<ObservableList<S>> weakItemsPropertyListener = new WeakChangeListener<>(itemsPropertyListener);
    
    private InvalidationListener parentListener;
    private WeakInvalidationListener weakParentListener;

    private InvalidationListener tableListener;
    private WeakInvalidationListener weakTableListener;
    
    private FilterMenuButton menuButton;
    private final ChangeListener<Boolean> menuButtonListener = (obs, ov, nv) -> {
            if (! nv && ov) {
                updatePredicate(getEditor().getText());
            }
        };
    private final WeakChangeListener<Boolean> weakMenuButtonListener = new WeakChangeListener<>(menuButtonListener);
    
    public FilterEditor(FilteredTableColumn<S, T> tableColumn) {
        this.tableColumn = tableColumn;
        this.filterValues = FXCollections.<T>observableArrayList();
        if (getParent() != null) {
            setMenuButton();
        } else {
            parentListener = (Observable o) -> {
                if (getParent() != null) {
                    setMenuButton();
                    parentProperty().removeListener(weakParentListener);
                }
            };
            weakParentListener = new WeakInvalidationListener(parentListener);
            parentProperty().addListener(weakParentListener);
        }
        getStyleClass().add("filter-editor");
        
        if (tableColumn.getTableView() != null) {
            addItemsListeners();
        } else {
            tableListener = (Observable o) -> {
                if (tableColumn.getTableView() != null) {
                    addItemsListeners();
                    this.tableColumn.tableViewProperty().removeListener(weakTableListener);
                }
            };
            weakTableListener = new WeakInvalidationListener(tableListener);
            this.tableColumn.tableViewProperty().addListener(weakTableListener);
        }
        
        setItems(filterValues);
        setEditable(true);
        // Prevents the event to be propagated to TableRow/TableCell
        setOnAction(e -> e.consume());
        
        getEditor().textProperty().addListener(weakEditorListener);
    }
    
    private void setMenuButton() {
        menuButton = ((SouthFilter) getParent()).getMenuButton();
        menuButton.showingProperty().addListener(weakMenuButtonListener);
    }
    
    private void addItemsListeners() {
        final TableView<S> tableView = tableColumn.getTableView();
        
        tableView.itemsProperty().addListener(weakItemsPropertyListener);
        tableView.getItems().addListener(weakListChangeListener);
        loadItems();
    }
    
    private void loadItems() {
        T selection = getValue();
        filterValues.clear();
        if (tableColumn != null && tableColumn.getTableView() != null && 
                tableColumn.getTableView().getItems() != null) {
            tableColumn.getTableView().getItems().stream()
                    .filter((s) -> tableColumn.getCellData(s) != null)
                    .map(tableColumn::getCellData)
                    .distinct()
                    .forEach(filterValues::add);
            setValue(selection);
        } else {
            setValue(null);
        }
    }
    
    private void updatePredicate(String text) {
        if (text == null || text.isEmpty()) {
            getTableColumn().setPredicate(null);
        } else {
            getTableColumn().setPredicate((Predicate<T>) menuButton.parse(text, getConverter()));
        }
        updateError();
    }

    private void updateError() {
        String text = getEditor().getText();
        if (text == null || text.isEmpty()) {
            setTooltip(null);
            getEditor().pseudoClassStateChanged(PSEUDO_CLASS_ERROR, false);
        } else {
            String error = menuButton.getErrorMessage();
            if (error == null || error.isEmpty()) {
                setTooltip(null);
                getEditor().pseudoClassStateChanged(PSEUDO_CLASS_ERROR, false);
            } else {
                setTooltip(new Tooltip(error));
                getEditor().pseudoClassStateChanged(PSEUDO_CLASS_ERROR, true);
            }
        }
    }
    
    public FilteredTableColumn<S, T> getTableColumn() {
        return tableColumn;
    }
    
    public void cancelFilter() {
        setValue(null);
    }
    
    /** {@inheritDoc} */
    @Override protected double computeMaxWidth(double height) {
        return Double.MAX_VALUE;
    }
    
}
