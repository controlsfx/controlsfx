/**
 * Copyright (c) 2013, ControlsFX
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

import java.util.BitSet;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;

// not public API
abstract class CheckBitSetModelBase<T> extends MultipleSelectionModel<T> {
    
    /***********************************************************************
     *                                                                     *
     * Internal properties                                                 *
     *                                                                     *
     **********************************************************************/

    private final Map<T, BooleanProperty> itemBooleanMap;
    
    private final BitSet selectedIndices;
    private final ReadOnlyUnbackedObservableList<Integer> selectedIndicesList;
    private final ReadOnlyUnbackedObservableList<T> selectedItemsList;
    
    

    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    CheckBitSetModelBase(final Map<T, BooleanProperty> itemBooleanMap) {
        this.itemBooleanMap = itemBooleanMap;
        
        this.selectedIndices = new BitSet();
        
        this.selectedIndicesList = new ReadOnlyUnbackedObservableList<Integer>() {
            @Override public Integer get(int index) {
                if (index < 0 || index >= getItemCount()) return -1;

                for (int pos = 0, val = selectedIndices.nextSetBit(0);
                    val >= 0 || pos == index;
                    pos++, val = selectedIndices.nextSetBit(val+1)) {
                        if (pos == index) return val;
                }

                return -1;
            }

            @Override public int size() {
                return selectedIndices.cardinality();
            }

            @Override public boolean contains(Object o) {
                if (o instanceof Number) {
                    Number n = (Number) o;
                    int index = n.intValue();

                    return index >= 0 && index < selectedIndices.length() &&
                            selectedIndices.get(index);
                }

                return false;
            }
        };
        
        this.selectedItemsList = new ReadOnlyUnbackedObservableList<T>() {
            @Override public T get(int i) {
                int pos = selectedIndicesList.get(i);
                if (pos < 0 || pos >= getItemCount()) return null;
                return getItem(pos);
            }

            @Override public int size() {
                return selectedIndices.cardinality();
            }
        };
        
        final MappingChange.Map<Integer,T> map = new MappingChange.Map<Integer,T>() {
            @Override public T map(Integer f) {
                return getItem(f);
            }
        };
        selectedIndicesList.addListener(new ListChangeListener<Integer>() {
            @Override public void onChanged(final Change<? extends Integer> c) {
                // when the selectedIndices ObservableList changes, we manually call
                // the observers of the selectedItems ObservableList.
                selectedItemsList.callObservers(new MappingChange<Integer,T>(c, map, selectedItemsList));
                c.reset();
            }
        });
        
        // this code is to handle the situation where a developer is manually
        // toggling the check model, and expecting the UI to update (without
        // this it won't happen!).
        getSelectedItems().addListener(new ListChangeListener<T>() {
            @Override public void onChanged(ListChangeListener.Change<? extends T> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (T item : c.getAddedSubList()) {
                            BooleanProperty p = getItemBooleanProperty(item);
                            if (p != null) {
                                p.set(true);
                            }
                        }
                    } 
                    
                    if (c.wasRemoved()) {
                        for (T item : c.getRemoved()) {
                            BooleanProperty p = getItemBooleanProperty(item);
                            if (p != null) {
                                p.set(false);
                            }
                        }
                    }
                }
            }
        });
    }
    
    
    
    /***********************************************************************
     *                                                                     *
     * Abstract API                                                        *
     *                                                                     *
     **********************************************************************/

    public abstract T getItem(int index);
    
    public abstract int getItemCount();
    
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
     * Returns a read-only list of the currently selected indices in the CheckBox.
     */
    @Override public ObservableList<Integer> getSelectedIndices() {
        return selectedIndicesList;
    }
    
    /**
     * Returns a read-only list of the currently selected items in the CheckBox.
     */
    @Override public ObservableList<T> getSelectedItems() {
        return selectedItemsList;
    }

    /** {@inheritDoc} */
    @Override public void selectAll() {
        for (int i = 0; i < getItemCount(); i++) {
            select(i);
        }
    }

    /** {@inheritDoc} */
    @Override public void selectFirst() {
        select(0);
    }

    /** {@inheritDoc} */
    @Override public void selectIndices(int index, int... indices) {
        select(index);
        for (int i = 0; i < indices.length; i++) {
            select(indices[i]);
        }
    }

    /** {@inheritDoc} */
    @Override public void selectLast() {
        select(getItemCount() - 1);
    }

    /** {@inheritDoc} */
    @Override public void clearAndSelect(int index) {
        clearSelection();
        select(index);
    }

    /** {@inheritDoc} */
    @Override public void clearSelection() {
        selectedIndices.clear();
    }

    /** {@inheritDoc} */
    @Override public void clearSelection(int index) {
        selectedIndices.clear(index);
        
        final int changeIndex = selectedIndicesList.indexOf(index);
        selectedIndicesList.callObservers(new NonIterableChange.SimpleRemovedChange<Integer>(changeIndex, changeIndex+1, index, selectedIndicesList));
    }
    
    /** {@inheritDoc} */
    @Override public boolean isEmpty() {
        return selectedIndices.isEmpty();
    }

    /** {@inheritDoc} */
    @Override public boolean isSelected(int index) {
        return selectedIndices.get(index);
    }

    /** {@inheritDoc} */
    @Override public void select(int index) {
        if (index < 0 || index >= selectedIndices.size());
        selectedIndices.set(index);
        
        final int changeIndex = selectedIndicesList.indexOf(index);
        selectedIndicesList.callObservers(new NonIterableChange.SimpleAddChange<Integer>(changeIndex, changeIndex+1, selectedIndicesList));
    }

    /** {@inheritDoc} */
    @Override public void select(T item) {
        int index = getItemIndex(item);
        select(index);
    }

    /** {@inheritDoc} */
    @Override public void selectNext() {
        // no-op
    }

    /** {@inheritDoc} */
    @Override public void selectPrevious() {
        // no-op
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
            booleanProperty.addListener(new InvalidationListener() {
                @Override public void invalidated(Observable o) {
                    final int changeIndex = selectedIndicesList.indexOf(index);
                    
                    if (booleanProperty.get()) {
                        selectedIndices.set(index);
                        selectedIndicesList.callObservers(new NonIterableChange.SimpleAddChange<Integer>(changeIndex, changeIndex+1, selectedIndicesList));                            
                    } else {
                        selectedIndices.clear(index);
                        selectedIndicesList.callObservers(new NonIterableChange.SimpleRemovedChange<Integer>(changeIndex, changeIndex+1, index, selectedIndicesList));
                    }
                }
            });
        }
    }
}