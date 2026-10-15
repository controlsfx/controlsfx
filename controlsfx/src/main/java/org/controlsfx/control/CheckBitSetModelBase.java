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

import impl.org.controlsfx.collections.NonIterableChange;
import impl.org.controlsfx.collections.ReadOnlyUnbackedObservableList;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Check model backed by a {@link BitSet} of the checked indices.
 *
 * <p>The check state belongs to the items: the rows holding equal items share a single
 * {@link BooleanProperty}, so checking or clearing any of those rows reaches them all, and a
 * change of the item list re-indexes the checks so that they stay on the items holding them.
 * The observable lists of checked indices and items expose the state as last reported, so that
 * a change handed to a listener stays readable whatever that listener does to the model.
 */
// not public API
abstract class CheckBitSetModelBase<T> implements IndexedCheckModel<T> {

    /***********************************************************************
     *                                                                     *
     * Internal properties                                                 *
     *                                                                     *
     **********************************************************************/

    private final Map<T, BooleanProperty> itemBooleanMap;
    private final Map<T, FollowedProperty> followedProperties = new HashMap<>();
    private Map<T, List<Integer>> itemRows = Collections.emptyMap();

    private final BitSet checkedIndices = new BitSet();

    // the state the two lists expose, never mutated but replaced on each change, so that the
    // sublists handed to the listeners stay readable
    private List<Integer> checkedIndicesSnapshot = Collections.emptyList();
    private List<T> checkedItemsSnapshot = Collections.emptyList();
    // the rows checked as last reported, telling the rows a change touched apart from the ones
    // it left alone
    private BitSet reportedChecks = new BitSet();
    // the content the listeners of each list hold, which a re-entrant change leaves further
    // along than the change being reported
    private List<Integer> lastReportedIndices = Collections.emptyList();
    private List<T> lastReportedItems = Collections.emptyList();
    // the checked items, each with the number of checked rows holding it
    private Map<T, Integer> checkedRowCounts = new HashMap<>();

    private final ReadOnlyUnbackedObservableList<Integer> checkedIndicesList = new CheckedIndicesList();
    private final ReadOnlyUnbackedObservableList<T> checkedItemsList = new CheckedItemsList();


    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    CheckBitSetModelBase(final Map<T, BooleanProperty> itemBooleanMap) {
        this.itemBooleanMap = itemBooleanMap;
    }


    /***********************************************************************
     *                                                                     *
     * Abstract API                                                        *
     *                                                                     *
     **********************************************************************/

    @Override
    public abstract T getItem(int index);

    @Override
    public abstract int getItemCount();

    @Override
    public abstract int getItemIndex(T item);

    BooleanProperty getItemBooleanProperty(T item) {
        return itemBooleanMap.get(item);
    }


    /***********************************************************************
     *                                                                     *
     * Public selection API                                                *
     *                                                                     *
     **********************************************************************/

    /**
     * Returns a read-only list of the currently checked indices in the CheckBox.
     */
    @Override
    public ObservableList<Integer> getCheckedIndices() {
        return checkedIndicesList;
    }

    /**
     * Returns a read-only list of the currently checked items in the CheckBox.
     */
    @Override
    public ObservableList<T> getCheckedItems() {
        return checkedItemsList;
    }

    /** {@inheritDoc} */
    @Override
    public void checkAll() {
        checkedIndices.set(0, getItemCount());
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void checkIndices(int... indices) {
        for (int index : indices) {
            setRowsOf(index, true);
        }
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void check(int index) {
        setRowsOf(index, true);
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void check(T item) {
        setItemChecked(item, true);
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void clearCheck(int index) {
        setRowsOf(index, false);
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void clearCheck(T item) {
        setItemChecked(item, false);
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public void clearChecks() {
        checkedIndices.clear();
        fireChanges();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return checkedIndices.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isChecked(int index) {
        return isValidIndex(index) && checkedIndices.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isChecked(T item) {
        return checkedRowCounts.containsKey(item);
    }

    /** {@inheritDoc} */
    @Override
    public void toggleCheckState(int index) {
        if (isChecked(index)) {
            clearCheck(index);
        } else {
            check(index);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void toggleCheckState(T item) {
        if (isChecked(item)) {
            clearCheck(item);
        } else {
            check(item);
        }
    }


    /***********************************************************************
     *                                                                     *
     * Item list support                                                   *
     *                                                                     *
     **********************************************************************/

    /**
     * Follows the current item list: indexes the rows of every item, keeps a
     * {@link BooleanProperty} for each item of the list and re-indexes the checks so that they
     * stay on the items holding them.
     */
    protected void updateMap() {
        Map<T, List<Integer>> rows = new HashMap<>();
        for (int i = 0, count = getItemCount(); i < count; i++) {
            rows.computeIfAbsent(getItem(i), item -> new ArrayList<>(1)).add(i);
        }
        itemRows = rows;

        checkedIndices.clear();
        for (T item : checkedRowCounts.keySet()) {
            setItemChecked(item, true);
        }

        // reported before the properties are written: a listener of a property this model is
        // clearing is free to reach back into it, and has to be answered the checks it now holds
        fireChangesAfterItemsChanged();
        updateFollowedProperties();
    }

    /**
     * Attaches this model to the item list and to the item properties it drives, aligning them on
     * the checks it holds. To be called by the control taking this model as its own.
     */
    void attach() {
        updateMap();
    }

    /**
     * Detaches this model from the item list and from the item properties it drives. To be called
     * by the control dropping this model, be it for another one built on the same properties.
     */
    void detach() {
        for (FollowedProperty followed : followedProperties.values()) {
            followed.detach();
        }
        followedProperties.clear();
    }


    /***********************************************************************
     *                                                                     *
     * Private implementation                                              *
     *                                                                     *
     **********************************************************************/

    private boolean isValidIndex(int index) {
        return index >= 0 && index < getItemCount();
    }

    /** Sets the check state of the given row along with every other row holding the same item. */
    private void setRowsOf(int index, boolean checked) {
        if (isValidIndex(index)) {
            checkedIndices.set(index, checked);
            setItemChecked(getItem(index), checked);
        }
    }

    /**
     * Sets the check state of every row holding the given item; returns whether any row changed.
     * The rows are read from the index this model holds of the item list, which a model detached
     * from that list stops following: a row which no longer holds the item is left alone.
     */
    private boolean setItemChecked(T item, boolean checked) {
        boolean changed = false;
        List<Integer> rows = itemRows.get(item);
        if (rows != null) {
            for (int row : rows) {
                if (isValidIndex(row) && Objects.equals(getItem(row), item)
                        && checkedIndices.get(row) != checked) {
                    checkedIndices.set(row, checked);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private void onItemPropertyInvalidated(T item, BooleanProperty property) {
        if (setItemChecked(item, property.get())) {
            fireChanges();
        }
    }

    private void updateFollowedProperties() {
        final Map<T, List<Integer>> rows = itemRows;
        for (Iterator<Map.Entry<T, FollowedProperty>> it = followedProperties.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<T, FollowedProperty> followed = it.next();
            if (!rows.containsKey(followed.getKey())) {
                followed.getValue().detach();
                it.remove();
            }
        }

        // the property of an item which is not in the list leaves the map whichever model put it
        // there, the map being shared with the control and with the models built on it afterwards
        final List<BooleanProperty> droppedProperties = new ArrayList<>();
        for (Iterator<Map.Entry<T, BooleanProperty>> it = itemBooleanMap.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<T, BooleanProperty> entry = it.next();
            if (!rows.containsKey(entry.getKey())) {
                droppedProperties.add(entry.getValue());
                it.remove();
            }
        }

        for (T item : rows.keySet()) {
            if (itemRows != rows) {
                // a listener of a property written above changed the item list, which indexed
                // this model on it again: the rows left to walk here are those of a list which is
                // gone, and the properties of the one the model now holds are that pass's to write
                break;
            }
            BooleanProperty property = itemBooleanMap.computeIfAbsent(item,
                    key -> new SimpleBooleanProperty(key, "selected", false)); //$NON-NLS-1$
            FollowedProperty followed = followedProperties.get(item);
            if (followed == null || followed.property != property) {
                if (followed != null) {
                    followed.detach();
                }
                followedProperties.put(item, new FollowedProperty(item, property));
                property.set(isChecked(item));
            }
        }

        // an item which left the list holds no check any more. Cleared once the map is up to
        // date, as a listener of such a property is free to reach back into this model
        for (BooleanProperty dropped : droppedProperties) {
            dropped.set(false);
        }
    }

    /**
     * Reports the rows checked or cleared since the state was last reported, the item list being
     * the same as then. The rows a change left alone keep the position and the item they had,
     * so the state as last reported is carried over and only the touched rows are looked at.
     */
    private void fireChanges() {
        final BitSet changedRows = (BitSet) checkedIndices.clone();
        changedRows.xor(reportedChecks);
        if (changedRows.isEmpty()) {
            return;
        }

        final List<Integer> previousIndices = checkedIndicesSnapshot;
        final List<T> previousItems = checkedItemsSnapshot;
        final int size = checkedIndices.cardinality();
        final List<Integer> indices = new ArrayList<>(size);
        final List<T> items = new ArrayList<>(size);
        // for each item, the change of the number of checked rows holding it
        final Map<T, Integer> rowCountDeltas = new HashMap<>();

        int position = 0;
        for (int row = changedRows.nextSetBit(0); row >= 0; row = changedRows.nextSetBit(row + 1)) {
            // the rows left alone below this one are carried over as they were
            final int rowPosition = positionOf(previousIndices, position, row);
            indices.addAll(previousIndices.subList(position, rowPosition));
            items.addAll(previousItems.subList(position, rowPosition));
            position = rowPosition;
            if (checkedIndices.get(row)) {
                final T item = getItem(row);
                indices.add(row);
                items.add(item);
                rowCountDeltas.merge(item, 1, Integer::sum);
            } else {
                // the row was checked as last reported, so it is the one at this position
                rowCountDeltas.merge(previousItems.get(position), -1, Integer::sum);
                position++;
            }
        }
        indices.addAll(previousIndices.subList(position, previousIndices.size()));
        items.addAll(previousItems.subList(position, previousItems.size()));

        final Set<T> affectedItems = new HashSet<>();
        for (Map.Entry<T, Integer> delta : rowCountDeltas.entrySet()) {
            final T item = delta.getKey();
            final int before = checkedRowCounts.getOrDefault(item, 0);
            final int after = before + delta.getValue();
            if (after == 0) {
                checkedRowCounts.remove(item);
            } else {
                checkedRowCounts.put(item, after);
            }
            if ((before == 0) != (after == 0)) {
                affectedItems.add(item);
            }
        }

        publish(indices, items, affectedItems);
    }

    /**
     * Reports the difference between the state as last reported and the current one after a
     * change of the item list. Such a change is free to leave the very same rows checked with
     * other items sitting at them, so nothing of the state as last reported is carried over.
     */
    private void fireChangesAfterItemsChanged() {
        final Map<T, Integer> previousRowCounts = checkedRowCounts;

        final List<Integer> indices = new ArrayList<>(checkedIndices.cardinality());
        final List<T> items = new ArrayList<>(indices.size());
        final Map<T, Integer> rowCounts = new HashMap<>();
        for (int i = checkedIndices.nextSetBit(0); i >= 0; i = checkedIndices.nextSetBit(i + 1)) {
            final T item = getItem(i);
            indices.add(i);
            items.add(item);
            rowCounts.merge(item, 1, Integer::sum);
        }
        checkedRowCounts = rowCounts;

        final Set<T> affectedItems = new HashSet<>();
        for (T item : previousRowCounts.keySet()) {
            if (!rowCounts.containsKey(item)) {
                affectedItems.add(item);
            }
        }
        for (T item : rowCounts.keySet()) {
            if (!previousRowCounts.containsKey(item)) {
                affectedItems.add(item);
            }
        }

        publish(indices, items, affectedItems);
    }

    /**
     * Takes the given state as the current one, reports to the listeners of both lists what it
     * changes of the content they hold, then aligns the properties of the affected items. Both
     * lists are moved on before either is reported, so that a listener reads them in step.
     */
    private void publish(List<Integer> indices, List<T> items, Set<T> affectedItems) {
        reportedChecks = (BitSet) checkedIndices.clone();
        checkedIndicesSnapshot = Collections.unmodifiableList(indices);
        checkedItemsSnapshot = Collections.unmodifiableList(items);

        final List<Integer> previousIndices = lastReportedIndices;
        lastReportedIndices = checkedIndicesSnapshot;
        report(checkedIndicesList, previousIndices, lastReportedIndices);

        // read after the indices are reported: a listener told about them may have changed the
        // model, which reported the items list ahead of this one
        final List<T> previousItems = lastReportedItems;
        lastReportedItems = checkedItemsSnapshot;
        report(checkedItemsList, previousItems, lastReportedItems);

        // written last and to the current state: a listener above may have changed the model again
        for (T item : affectedItems) {
            FollowedProperty followed = followedProperties.get(item);
            if (followed != null) {
                followed.property.set(isChecked(item));
            }
        }
    }

    /** Reports the single replacement turning {@code previous} into {@code current}, past the positions they have in common at their head and at their tail. */
    private static <E> void report(ReadOnlyUnbackedObservableList<E> list, List<E> previous, List<E> current) {
        if (previous == current) {
            return;
        }
        final int from = commonPrefix(previous, current);
        final int tail = commonSuffix(previous, current, from);
        final List<E> removed = previous.subList(from, previous.size() - tail);
        final List<E> added = current.subList(from, current.size() - tail);
        if (!removed.isEmpty() || !added.isEmpty()) {
            list.callObservers(new SnapshotChange<>(from, removed, added, list));
        }
    }

    /**
     * The position the given index holds, or would hold, in the given ascending list of indices,
     * looking from the given position on.
     */
    private static int positionOf(List<Integer> indices, int from, int index) {
        final int found = Collections.binarySearch(indices.subList(from, indices.size()), index);
        return from + (found >= 0 ? found : -found - 1);
    }

    /** The number of leading elements the two lists have in common. */
    private static <E> int commonPrefix(List<E> previous, List<E> current) {
        final int common = Math.min(previous.size(), current.size());
        int from = 0;
        while (from < common && Objects.equals(previous.get(from), current.get(from))) {
            from++;
        }
        return from;
    }

    /** The number of trailing elements the two lists have in common past their common prefix. */
    private static <E> int commonSuffix(List<E> previous, List<E> current, int from) {
        final int common = Math.min(previous.size(), current.size());
        int tail = 0;
        while (tail < common - from
                && Objects.equals(previous.get(previous.size() - 1 - tail), current.get(current.size() - 1 - tail))) {
            tail++;
        }
        return tail;
    }


    /***********************************************************************
     *                                                                     *
     * Support classes                                                     *
     *                                                                     *
     **********************************************************************/

    /** The property of an item along with the listener through which this model follows it. */
    private final class FollowedProperty {
        private final BooleanProperty property;
        private final InvalidationListener listener;

        FollowedProperty(T item, BooleanProperty property) {
            this.property = property;
            this.listener = o -> onItemPropertyInvalidated(item, property);
            property.addListener(listener);
        }

        void detach() {
            property.removeListener(listener);
        }
    }

    private final class CheckedIndicesList extends ReadOnlyUnbackedObservableList<Integer> {

        /**
         * {@inheritDoc}
         *
         * @return the checked index at the given position, or {@code -1} for a position which
         *         holds none
         */
        @Override
        public Integer get(int index) {
            if (index < 0 || index >= checkedIndicesSnapshot.size()) {
                return -1;
            }
            return checkedIndicesSnapshot.get(index);
        }

        @Override
        public int size() {
            return checkedIndicesSnapshot.size();
        }

        @Override
        public int indexOf(Object o) {
            if (!(o instanceof Number)) {
                return -1;
            }
            final int position = Collections.binarySearch(checkedIndicesSnapshot, ((Number) o).intValue());
            return position < 0 ? -1 : position;
        }
    }

    private final class CheckedItemsList extends ReadOnlyUnbackedObservableList<T> {

        /**
         * {@inheritDoc}
         *
         * @return the checked item at the given position, or {@code null} for a position which
         *         holds none
         */
        @Override
        public T get(int index) {
            if (index < 0 || index >= checkedItemsSnapshot.size()) {
                return null;
            }
            return checkedItemsSnapshot.get(index);
        }

        @Override
        public int size() {
            return checkedItemsSnapshot.size();
        }
    }

    /** A single replacement built from snapshots, readable whatever happens to the list afterwards. */
    private static final class SnapshotChange<E> extends NonIterableChange.GenericAddRemoveChange<E> {
        private final List<E> added;

        SnapshotChange(int from, List<E> removed, List<E> added, ObservableList<E> list) {
            super(from, from + added.size(), removed, list);
            this.added = added;
        }

        @Override
        public List<E> getAddedSubList() {
            checkState();
            return added;
        }

        @Override
        public int getAddedSize() {
            checkState();
            return added.size();
        }
    }
}
