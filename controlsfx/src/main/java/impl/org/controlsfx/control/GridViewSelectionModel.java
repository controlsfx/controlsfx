/**
 * Copyright (c) 2026, ControlsFX
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
package impl.org.controlsfx.control;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import org.controlsfx.control.GridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Default index-based selection for GridView, implemented using public JavaFX APIs.
 */
public final class GridViewSelectionModel<T> extends MultipleSelectionModel<T> {
    private final GridView<T> view;
    private TreeSet<Integer> selected = new TreeSet<>();
    private final SelectionList<Integer> indices = new SelectionList<>();
    private final SelectionList<T> items = new SelectionList<>();
    private final ListChangeListener<T> itemsListener = this::itemsChanged;
    private final WeakListChangeListener<T> weakItemsListener = new WeakListChangeListener<>(itemsListener);
    private final ChangeListener<ObservableList<T>> listListener = (obs, oldList, newList) -> {
        if (oldList != null) oldList.removeListener(weakItemsListener);
        if (newList != null) newList.addListener(weakItemsListener);
        clearSelection();
    };

    public GridViewSelectionModel(GridView<T> view) {
        this.view = view;
        view.itemsProperty().addListener(new WeakChangeListener<>(listListener));
        if (view.getItems() != null) view.getItems().addListener(weakItemsListener);
        selectionModeProperty().addListener(obs -> {
            if (getSelectionMode() != SelectionMode.MULTIPLE && selected.size() > 1) {
                clearAndSelect(getSelectedIndex());
            }
        });
    }

    @Override public ObservableList<Integer> getSelectedIndices() { return indices; }
    @Override public ObservableList<T> getSelectedItems() { return items; }
    @Override public boolean isSelected(int index) { return selected.contains(index); }
    @Override public boolean isEmpty() { return selected.isEmpty(); }

    @Override public void clearSelection() {
        publish(new TreeSet<>(), -1);
    }

    @Override public void clearSelection(int index) {
        if (!isSelected(index)) return;
        TreeSet<Integer> next = new TreeSet<>(selected);
        next.remove(index);
        publish(next, getSelectedIndex());
    }

    @Override public void clearAndSelect(int index) {
        if (index == -1) {
            clearSelection();
            return;
        }
        if (!valid(index)) return;
        TreeSet<Integer> next = new TreeSet<>();
        next.add(index);
        publish(next, index);
        focus(index);
    }

    @Override public void select(int index) {
        if (index == -1) {
            clearSelection();
        } else if (valid(index)) {
            if (getSelectionMode() != SelectionMode.MULTIPLE) {
                clearAndSelect(index);
            } else {
                TreeSet<Integer> next = new TreeSet<>(selected);
                next.add(index);
                publish(next, index);
                focus(index);
            }
        }
    }

    @Override public void select(T item) {
        int index = view.getItems() == null ? -1 : view.getItems().indexOf(item);
        if (index >= 0) {
            select(index);
        } else {
            clearSelection();
            setSelectedItem(item);
        }
    }

    @Override public void selectIndices(int index, int... others) {
        TreeSet<Integer> next = new TreeSet<>(selected);
        int lead = -1;
        if (valid(index)) {
            next.add(index);
            lead = index;
        }
        if (others != null) {
            for (int i : others) {
                if (valid(i)) {
                    next.add(i);
                    lead = i;
                }
            }
        }
        if (lead < 0) return;
        if (getSelectionMode() != SelectionMode.MULTIPLE) {
            clearAndSelect(lead);
        } else {
            publish(next, lead);
            focus(lead);
        }
    }

    @Override public void selectAll() {
        if (getSelectionMode() != SelectionMode.MULTIPLE || size() == 0) return;
        TreeSet<Integer> next = new TreeSet<>();
        for (int i = 0; i < size(); i++) next.add(i);
        publish(next, size() - 1);
        focus(getSelectedIndex());
    }

    @Override public void selectFirst() { select(0); }
    @Override public void selectLast() { select(size() - 1); }
    @Override public void selectNext() {
        if (getSelectedIndex() < size() - 1) select(getSelectedIndex() + 1);
    }
    @Override public void selectPrevious() {
        if (getSelectedIndex() == -1) selectLast();
        else if (getSelectedIndex() > 0) select(getSelectedIndex() - 1);
    }

    private int size() { return view.getItems() == null ? 0 : view.getItems().size(); }
    private boolean valid(int index) { return index >= 0 && index < size(); }
    private void focus(int index) {
        if (view.getFocusModel() != null) view.getFocusModel().focus(index);
    }

    private void itemsChanged(ListChangeListener.Change<? extends T> change) {
        TreeSet<Integer> next = new TreeSet<>(selected);
        int lead = getSelectedIndex();
        boolean updated = false;
        while (change.next()) {
            if (change.wasUpdated() && !next.subSet(change.getFrom(), change.getTo()).isEmpty()) {
                updated = true;
            }
            TreeSet<Integer> mapped = new TreeSet<>();
            for (int index : next) {
                int shifted = mapIndex(index, change);
                if (shifted >= 0) mapped.add(shifted);
            }
            next = mapped;
            lead = mapIndex(lead, change);
        }
        publish(next, lead, updated);
    }

    // Replacements retain a selected position when a replacement item exists.
    // Removals discard the removed positions; permutations follow the items.
    public static int mapIndex(int index, ListChangeListener.Change<?> change) {
        if (index < 0) return -1;
        if (change.wasPermutated()) {
            return index >= change.getFrom() && index < change.getTo()
                    ? change.getPermutation(index) : index;
        }
        if (change.wasUpdated() || index < change.getFrom()) return index;
        if (index >= change.getFrom() + change.getRemovedSize()) {
            return index + change.getAddedSize() - change.getRemovedSize();
        }
        return index - change.getFrom() < change.getAddedSize() ? index : -1;
    }

    private void publish(TreeSet<Integer> next, int lead) {
        publish(next, lead, false);
    }

    private void publish(TreeSet<Integer> next, int lead, boolean updated) {
        if (!next.contains(lead)) lead = next.isEmpty() ? -1 : next.last();
        List<Integer> nextIndices = new ArrayList<>(next);
        List<T> nextItems = new ArrayList<>(next.size());
        for (int index : next) nextItems.add(view.getItems().get(index));
        List<Integer> oldIndices = indices.values;
        List<T> oldItems = items.values;
        // Install both snapshots before notifying observers of either list.
        selected = next;
        indices.values = nextIndices;
        items.values = nextItems;
        setSelectedIndex(lead);
        setSelectedItem(lead < 0 ? null : view.getItems().get(lead));
        indices.changed(oldIndices, false);
        items.changed(oldItems, updated);
    }

    private static final class SelectionList<E> extends ObservableListBase<E> {
        private List<E> values = Collections.emptyList();
        @Override public E get(int index) { return values.get(index); }
        @Override public int size() { return values.size(); }
        private void changed(List<E> old, boolean updated) {
            boolean same = old.equals(values);
            if (same && !updated) return;
            beginChange();
            if (same) {
                for (int i = 0; i < size(); i++) nextUpdate(i);
            } else {
                nextReplace(0, size(), old);
            }
            endChange();
        }
    }

    /**
     * Default item focus, independently tracked from selection.
     */
    public static final class GridFocusModel<T> extends FocusModel<T> {
        private final GridView<T> view;
        private final ListChangeListener<T> itemsListener = change -> {
            int index = getFocusedIndex();
            while (change.next()) index = mapIndex(index, change);
            focus(index < 0 && getItemCount() > 0 ? 0 : index);
        };
        private final WeakListChangeListener<T> weakItemsListener = new WeakListChangeListener<>(itemsListener);
        private final ChangeListener<ObservableList<T>> listListener = (obs, oldList, newList) -> {
            if (oldList != null) oldList.removeListener(weakItemsListener);
            if (newList != null) newList.addListener(weakItemsListener);
            focus(getItemCount() == 0 ? -1 : 0);
        };

        public GridFocusModel(GridView<T> view) {
            this.view = view;
            view.itemsProperty().addListener(new WeakChangeListener<>(listListener));
            if (view.getItems() != null) view.getItems().addListener(weakItemsListener);
            focus(getItemCount() == 0 ? -1 : 0);
        }

        @Override protected int getItemCount() {
            return view.getItems() == null ? 0 : view.getItems().size();
        }
        @Override protected T getModelItem(int index) {
            return index < 0 || index >= getItemCount() ? null : view.getItems().get(index);
        }
    }
}
