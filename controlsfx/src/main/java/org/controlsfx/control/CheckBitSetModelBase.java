/**
 * Copyright (c) 2013, 2024, ControlsFX
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

import impl.org.controlsfx.collections.MappingChange;
import impl.org.controlsfx.collections.NonIterableChange;
import impl.org.controlsfx.collections.ReadOnlyUnbackedObservableList;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

// not public API
abstract class CheckBitSetModelBase<T> implements IndexedCheckModel<T> { 
    
    /***********************************************************************
     *                                                                     *
     * Internal properties                                                 *
     *                                                                     *
     **********************************************************************/

    private final Map<T, BooleanProperty> itemBooleanMap;

    private final BitSet checkedIndices;
    private final BitSetReadOnlyUnbackedObservableList checkedIndicesList;
    private final ReadOnlyUnbackedObservableList<T> checkedItemsList;

    private AtomicBoolean listenerFlag = new AtomicBoolean();
    

    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    CheckBitSetModelBase(final Map<T, BooleanProperty> itemBooleanMap) {
        this.itemBooleanMap = itemBooleanMap;

        this.checkedIndices = new BitSet();
        this.checkedIndicesList = new BitSetReadOnlyUnbackedObservableList(checkedIndices);
        this.checkedItemsList = new ReadOnlyUnbackedObservableList<T>() {
            @Override public T get(int i) {
                int pos = checkedIndicesList.get(i);
                if (pos < 0 || pos >= getItemCount()) return null;
                return getItem(pos);
            }

            @Override public int size() {
                return checkedIndices.cardinality();
            }
        };
        
        final MappingChange.Map<Integer,T> map = f -> getItem(f);
        
        checkedIndicesList.addListener((ListChangeListener<Integer>) c -> {
            // when the selectedIndices ObservableList changes, we manually call
            // the observers of the selectedItems ObservableList.
            boolean hasRealChangeOccurred = false;
            while (c.next() && ! hasRealChangeOccurred) {
                hasRealChangeOccurred = c.wasAdded() || c.wasRemoved();
            }

            if (hasRealChangeOccurred) {
                c.reset();
                checkedItemsList.callObservers(new MappingChange<>(c, map, checkedItemsList));
            }
            c.reset();
        });
        
        // this code is to handle the situation where a developer is manually
        // toggling the check model, and expecting the UI to update (without
        // this it won't happen!).
        getCheckedItems().addListener((ListChangeListener<T>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (T item : c.getAddedSubList()) {
                        updateBooleanProperty(item, true);
                    }
                } 
                
                if (c.wasRemoved()) {
                    for (T item : c.getRemoved()) {
                        updateBooleanProperty(item, false);
                    }
                }
            }
        });
    }

    private void updateBooleanProperty(T item, boolean value) {
        BooleanProperty p = getItemBooleanProperty(item);
        if (p != null) {
            listenerFlag.set(true);
            p.set(value);
            listenerFlag.set(false);
        }
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
        for (int i = 0; i < getItemCount(); i++) {
            check(i);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void checkIndices(int... indices) {
        for (int index : indices) {
            checkedIndices.set(index);
        }
        ListChangeListener.Change<Integer> change = createRangeChange(checkedIndicesList, Arrays.stream(indices).boxed().collect(Collectors.toList()), false);
        checkedIndicesList.callObservers(change);
    }
    
    /** {@inheritDoc} */
    @Override public void clearCheck(T item) {
        int index = getItemIndex(item);
        clearCheck(index);        
    }

    /** {@inheritDoc} */
    @Override
    public void clearChecks() {
        List<Integer> removed = new BitSetReadOnlyUnbackedObservableList((BitSet) checkedIndices.clone());
        checkedIndices.clear();
        checkedIndicesList.callObservers(
                new NonIterableChange.GenericAddRemoveChange<>(0, 0, removed, checkedIndicesList));
    }

    /** {@inheritDoc} */
    @Override
    public void clearCheck(int index) {
        if (index < 0 || index >= getItemCount()) return;
        final int changeIndex = checkedIndicesList.indexOf(index);
        checkedIndices.clear(index);
        checkedIndicesList.callObservers(new NonIterableChange.SimpleRemovedChange<>(changeIndex, changeIndex, index, checkedIndicesList));
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return checkedIndices.isEmpty();
    }
    
    /** {@inheritDoc} */
    @Override public boolean isChecked(T item) {
        int index = getItemIndex(item);
        return isChecked(index);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isChecked(int index) {
        return checkedIndices.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void toggleCheckState(T item) {
        int index = getItemIndex(item);
        toggleCheckState(index);
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
    public void check(int index) {
        if (index < 0 || index >= getItemCount()) return;
        checkedIndices.set(index);
        final int changeIndex = checkedIndicesList.indexOf(index);
        checkedIndicesList.callObservers(new NonIterableChange.SimpleAddChange<>(changeIndex, changeIndex+1, checkedIndicesList));
    }

    /** {@inheritDoc} */
    @Override
    public void check(T item) {
        int index = getItemIndex(item);
        check(index);
    }
    
    /***********************************************************************
     *                                                                     *
     * Private implementation                                              *
     *                                                                     *
     **********************************************************************/
    
    protected void updateMap() {
        // reset the map
        itemBooleanMap.clear();
        for (int i = 0; i < getItemCount(); i++) {
            final int index = i;
            final T item = getItem(index);

            final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "selected", false); //$NON-NLS-1$
            itemBooleanMap.put(item, booleanProperty);

            // this is where we listen to changes to the boolean properties,
            // updating the selected indices list (and therefore indirectly
            // the selected items list) when the checkbox is toggled
            booleanProperty.addListener(o -> {
                if (!listenerFlag.get()) {
                    if (booleanProperty.get()) {
                        check(index);
                    } else {
                        clearCheck(index);
                    }
                }
            });
        }
    }

    /***********************************************************************
     *                                                                     *
     * Private implementation                                              *
     *                                                                     *
     **********************************************************************/

    private class BitSetReadOnlyUnbackedObservableList extends ReadOnlyUnbackedObservableList<Integer> {
        private final BitSet bitset;

        private int lastGetIndex = -1;
        private int lastGetValue = -1;

        public BitSetReadOnlyUnbackedObservableList(BitSet bitset) {
            this.bitset = bitset;
        }

        @Override public Integer get(int index) {
            final int itemCount = getItemCount();
            if (index < 0 || index >= itemCount)  {
                return -1;
            }

            if (index == (lastGetIndex + 1) && lastGetValue < itemCount) {
                // we're iterating forward in order, short circuit for
                // performance reasons (RT-39776)
                lastGetIndex++;
                lastGetValue = bitset.nextSetBit(lastGetValue + 1);
                return lastGetValue;
            } else if (index == (lastGetIndex - 1) && lastGetValue > 0) {
                // we're iterating backward in order, short circuit for
                // performance reasons (RT-39776)
                lastGetIndex--;
                lastGetValue = bitset.previousSetBit(lastGetValue - 1);
                return lastGetValue;
            } else {
                for (lastGetIndex = 0, lastGetValue = bitset.nextSetBit(0);
                     lastGetValue >= 0 || lastGetIndex == index;
                     lastGetIndex++, lastGetValue = bitset.nextSetBit(lastGetValue + 1)) {
                    if (lastGetIndex == index) {
                        return lastGetValue;
                    }
                }
            }

            return -1;
        }

        @Override public int indexOf(Object obj) {
            if (!(obj instanceof Number)) {
                return -1;
            }
            Number n = (Number) obj;
            int index = n.intValue();
            if (!bitset.get(index)) {
                return -1;
            }

            // is left most bit
            if (index == 0) {
                return 0;
            }

            // is right most bit
            if (index == bitset.length() - 1) {
                return size() - 1;
            }

            // count right bit
            if (index > bitset.length() / 2) {
                int count = 1;
                for (int i = bitset.nextSetBit(index+1); i >= 0; i = bitset.nextSetBit(i+1)) {
                    count++;
                }
                return size() - count;
            }

            // count left bit
            int count = 0;
            for (int i = bitset.previousSetBit(index-1);  i >= 0; i = bitset.previousSetBit(i-1)) {
                count++;
            }
            return count;
        }

        @Override public int size() {
            return bitset.cardinality();
        }

        @Override public boolean contains(Object o) {
            if (o instanceof Number) {
                Number n = (Number) o;
                int index = n.intValue();

                return index >= 0 && index < bitset.length() &&
                        bitset.get(index);
            }

            return false;
        }

        public void reset() {
            this.lastGetIndex = -1;
            this.lastGetValue = -1;
        }
    }

    private static ListChangeListener.Change<Integer> createRangeChange(final ObservableList<Integer> list, final List<Integer> addedItems, boolean splitChanges) {
        ListChangeListener.Change<Integer> change = new ListChangeListener.Change<Integer>(list) {
            private final int[] EMPTY_PERM = new int[0];
            private final int addedSize = addedItems.size();

            private boolean invalid = true;

            private int pos = 0;
            private int from = pos;
            private int to = pos;

            @Override public int getFrom() {
                checkState();
                return from;
            }

            @Override public int getTo() {
                checkState();
                return to;
            }

            @Override public List<Integer> getRemoved() {
                checkState();
                return Collections.<Integer>emptyList();
            }

            @Override protected int[] getPermutation() {
                checkState();
                return EMPTY_PERM;
            }

            @Override public int getAddedSize() {
                return to - from;
            }

            @Override public boolean next() {
                if (pos >= addedSize) return false;

                // starting from pos, we keep going until the value is
                // not the next value
                int startValue = addedItems.get(pos++);
                from = list.indexOf(startValue);
                to = from + 1;
                int endValue = startValue;
                while (pos < addedSize) {
                    int previousEndValue = endValue;
                    endValue = addedItems.get(pos++);
                    ++to;
                    if (splitChanges && previousEndValue != (endValue - 1)) {
                        break;
                    }
                }

                if (invalid) {
                    invalid = false;
                    return true;
                }

                // we keep going until we've represented all changes!
                return splitChanges && pos < addedSize;
            }

            @Override public void reset() {
                invalid = true;
                pos = 0;
                to = 0;
                from = 0;
            }

            private void checkState() {
                if (invalid) {
                    throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
                }
            }

        };
        return change;
    }
}