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
import java.util.HashMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

/**
 * A simple UI control that makes it possible to select zero or more items within
 * a ComboBox-like way. Each row item shows a {@link CheckBox}.
 *
 * @param <T> The type of the data in the ComboBox.
 */
public class CheckComboBox<T> extends ComboBox<T> {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private ObservableList<T> items;
    private final Map<T, BooleanProperty> itemBooleanMap;
    
    private BitSet selectedIndices;
    
    private ReadOnlyUnbackedObservableList<Integer> selectedIndicesList;
    private ReadOnlyUnbackedObservableList<T> selectedItemsList;
    
    private ListCell<T> buttonCell;

    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * 
     */
    public CheckComboBox() {
        this(null);
    }
    
    /**
     * 
     * @param items
     */
    public CheckComboBox(final ObservableList<T> items) {
        super(items);
        
        final int initialSize = items == null ? 32 : items.size(); 
        this.itemBooleanMap = new HashMap<T, BooleanProperty>(initialSize);
        
        this.items = items == null ? FXCollections.<T>observableArrayList() : items;
        this.items.addListener(new ListChangeListener<T>() {
            @Override public void onChanged(Change<? extends T> c) {
                updateMap(c);
            }
        });
        updateMap(null);
        
        this.selectedIndices = new BitSet(initialSize);
        
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
        
        // installs a custom CheckBoxListCell cell factory
        setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            public ListCell<T> call(ListView<T> listView) {
                return new CheckBoxListCell<T>(new Callback<T, ObservableValue<Boolean>>() {
                    @Override public ObservableValue<Boolean> call(T item) {
                        return itemBooleanMap.get(item);
                    }
                });
            };
        });
        
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
                
                // we also update the display of the ComboBox button cell by
                // just dumbly updating the index every time selection changes.
                buttonCell.updateIndex(1);
            }
        });
        
        // we render the selection into a custom button cell, so that it can 
        // be pretty printed (e.g. 'Item 1, Item 2, Item 10').
        buttonCell = new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                // we ignore whatever item is selected, instead choosing
                // to display the selected item text using commas to separate
                // each item
                StringBuilder sb = new StringBuilder();
                for (int i = 0, max = selectedItemsList.size(); i < max; i++) {
                    sb.append(selectedItemsList.get(i));
                    if (i < max - 1) {
                        sb.append(", ");
                    }
                }
                setText(sb.toString());
            }
        };
        setButtonCell(buttonCell);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new ComboBoxListViewSkin<T>(this) {
            // overridden to prevent the popup from disappearing
            @Override protected boolean isHideOnClickEnabled() {
                return false;
            }
        };
    }
    
    /**
     * Returns a read-only list of the currently selected indices in the CheckBox.
     */
    public ObservableList<Integer> getSelectedIndices() {
        return selectedIndicesList;
    }
    
    /**
     * Returns a read-only list of the currently selected items in the CheckBox.
     */
    public ObservableList<T> getSelectedItems() {
        return selectedItemsList;
    }
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
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
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
}
