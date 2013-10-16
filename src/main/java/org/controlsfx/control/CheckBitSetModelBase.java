package org.controlsfx.control;

import java.util.BitSet;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.MultipleSelectionModel;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;

class CheckBitSetModelBase<T> extends MultipleSelectionModel<T> {
    
    /***********************************************************************
     *                                                                     *
     * Internal properties                                                 *
     *                                                                     *
     **********************************************************************/

    private final ObservableList<T> items;
    private final Map<T, BooleanProperty> itemBooleanMap;
    
    private final BitSet selectedIndices;
    private final ReadOnlyUnbackedObservableList<Integer> selectedIndicesList;
    private final ReadOnlyUnbackedObservableList<T> selectedItemsList;
    
    

    /***********************************************************************
     *                                                                     *
     * Constructors                                                        *
     *                                                                     *
     **********************************************************************/

    CheckBitSetModelBase(final ObservableList<T> items, final Map<T, BooleanProperty> itemBooleanMap) {
        this.items = items;
        this.itemBooleanMap = itemBooleanMap;
        
        this.items.addListener(new ListChangeListener<T>() {
            @Override public void onChanged(Change<? extends T> c) {
                updateMap(c);
            }
        });
        updateMap(null);
        
        this.selectedIndices = new BitSet();
        
        this.selectedIndicesList = new ReadOnlyUnbackedObservableList<Integer>() {
            @Override public Integer get(int index) {
                if (index < 0 || index >= items.size()) return -1;

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
                if (pos < 0 || pos >= items.size()) return null;
                return items.get(pos);
            }

            @Override public int size() {
                return selectedIndices.cardinality();
            }
        };
        
        final MappingChange.Map<Integer,T> map = new MappingChange.Map<Integer,T>() {
            @Override public T map(Integer f) {
                return items.get(f);
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
        for (int i = 0; i < items.size(); i++) {
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
        select(items.size() - 1);
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
    }

    /** {@inheritDoc} */
    @Override public void select(T item) {
        int index = items.indexOf(item);
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
    
    private void updateMap(Change<? extends T> c) {
        if  (c == null) {
            // reset the map
            itemBooleanMap.clear();
            for (int i = 0; i < items.size(); i++) {
                final int index = i;
                final T item = items.get(index);
                
                final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "selected", false);
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
        } else {
            // TODO
        }
    }
}