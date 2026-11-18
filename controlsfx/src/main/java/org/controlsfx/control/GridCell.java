/**
 * Copyright (c) 2013, 2026, ControlsFX
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
package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Skin;

/**
 * A virtualized cell representing an item in a {@link GridView}. Selection and
 * item focus follow the cell's current index, including when the cell is reused.
 * Custom cells should call {@code super.updateItem(item, empty)} when rendering.
 * @param <T> the item type
 */
public class GridCell<T> extends IndexedCell<T> {
    /**************************************************************************
     *
     * Private fields
     *
     **************************************************************************/

    private MultipleSelectionModel<T> observedSelection;
    private FocusModel<T> observedFocus;
    private ObservableList<T> observedItems;
    private final ListChangeListener<Integer> selectionListener = change -> updateState();
    private final WeakListChangeListener<Integer> weakSelectionListener =
            new WeakListChangeListener<>(selectionListener);
    private final InvalidationListener focusListener = obs -> updateState();
    private final WeakInvalidationListener weakFocusListener = new WeakInvalidationListener(focusListener);
    private final InvalidationListener modelsListener = obs -> updateModels();
    private final WeakInvalidationListener weakModelsListener = new WeakInvalidationListener(modelsListener);
    private final ListChangeListener<T> itemsListener = change -> updateCell();
    private final WeakListChangeListener<T> weakItemsListener = new WeakListChangeListener<>(itemsListener);
    private final InvalidationListener itemsPropertyListener = obs -> updateItems();
    private final WeakInvalidationListener weakItemsPropertyListener =
            new WeakInvalidationListener(itemsPropertyListener);

    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates an empty cell.
     */
    public GridCell() {
        getStyleClass().add("grid-cell");
        setFocusTraversable(false);
        indexProperty().addListener(obs -> updateCell());
        gridView.addListener((obs, oldView, newView) -> {
            if (oldView != null) {
                oldView.selectionModelProperty().removeListener(weakModelsListener);
                oldView.focusModelProperty().removeListener(weakModelsListener);
                oldView.focusedProperty().removeListener(weakFocusListener);
                oldView.itemsProperty().removeListener(weakItemsPropertyListener);
            }
            if (newView != null) {
                newView.selectionModelProperty().addListener(weakModelsListener);
                newView.focusModelProperty().addListener(weakModelsListener);
                newView.focusedProperty().addListener(weakFocusListener);
                newView.itemsProperty().addListener(weakItemsPropertyListener);
            }
            updateModels();
            updateItems();
        });
    }

    private void updateModels() {
        if (observedSelection != null) observedSelection.getSelectedIndices().removeListener(weakSelectionListener);
        if (observedFocus != null) observedFocus.focusedIndexProperty().removeListener(weakFocusListener);
        GridView<T> view = getGridView();
        observedSelection = view == null ? null : view.getSelectionModel();
        observedFocus = view == null ? null : view.getFocusModel();
        if (observedSelection != null) observedSelection.getSelectedIndices().addListener(weakSelectionListener);
        if (observedFocus != null) observedFocus.focusedIndexProperty().addListener(weakFocusListener);
        updateState();
    }

    private void updateItems() {
        if (observedItems != null) observedItems.removeListener(weakItemsListener);
        observedItems = getGridView() == null ? null : getGridView().getItems();
        if (observedItems != null) observedItems.addListener(weakItemsListener);
        updateCell();
    }

    private void updateCell() {
        boolean valid = observedItems != null && getIndex() >= 0 && getIndex() < observedItems.size();
        T item = valid ? observedItems.get(getIndex()) : null;
        updateItem(item, !valid || item == null);
        updateState();
    }

    private void updateState() {
        boolean valid = !isEmpty() && getGridView() != null && getIndex() >= 0;
        updateSelected(valid && observedSelection != null && observedSelection.isSelected(getIndex()));
        setFocused(valid && observedFocus != null && observedFocus.isFocused(getIndex())
                && getGridView().isFocused());
    }

    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new GridCellSkin<>(this);
    }

    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/

    /**
     * The {@link GridView} that this GridCell exists within.
     */
    public SimpleObjectProperty<GridView<T>> gridViewProperty() {
        return gridView;
    }
    private final SimpleObjectProperty<GridView<T>> gridView =
            new SimpleObjectProperty<>(this, "gridView"); //$NON-NLS-1$

    /**
     * Sets the {@link GridView} that this GridCell exists within.
     */
    public final void updateGridView(GridView<T> gridView) {
        this.gridView.set(gridView);
    }

    /**
     * Returns the {@link GridView} that this GridCell exists within.
     */
    public GridView<T> getGridView() {
        return gridView.get();
    }
}
