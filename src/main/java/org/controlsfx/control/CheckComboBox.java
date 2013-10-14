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

import impl.org.controlsfx.skin.CheckComboBoxSkin;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
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
public class CheckComboBox<T> extends Control {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final ObservableList<T> items;
    private final Map<T, BooleanProperty> itemBooleanMap;
    

    
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
        final int initialSize = items == null ? 32 : items.size();
        
        this.itemBooleanMap = new HashMap<T, BooleanProperty>(initialSize);
        
        this.items = items == null ? FXCollections.<T>observableArrayList() : items;
        
        setSelectionModel(new CheckComboBoxBitSetSelectionModel<T>(this));
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new CheckComboBoxSkin<>(this);
    }
    
    public ObservableList<T> getItems() {
        return items;
    }
    
    public BooleanProperty getItemBooleanProperty(int index) {
        if (index < 0 || index >= items.size()) return null;
        return getItemBooleanProperty(getItems().get(index));
    }
    
    public BooleanProperty getItemBooleanProperty(T item) {
        return itemBooleanMap.get(item);
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    // --- Selection Model
    private ObjectProperty<MultipleSelectionModel<T>> selectionModel = 
            new SimpleObjectProperty<MultipleSelectionModel<T>>(this, "selectionModel");
    
    /**
     * Sets the {@link SelectionModel} to be used in the CheckComboBox. 
     * Note that, unlike other UI controls such as ListView, the SelectionModel
     * in this instance is used to represent the items in the CheckComboBox that
     * have had their {@link CheckBox} selected by the user.
     */
    public final void setSelectionModel(MultipleSelectionModel<T> value) {
        selectionModelProperty().set(value);
    }

    /**
     * Returns the currently installed selection model.
     */
    public final MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel == null ? null : selectionModel.get();
    }

    /**
     * The SelectionModel provides the API through which it is possible
     * to select single or multiple items within a CheckComboBox, as  well as inspect
     * which items have been selected by the user. Note that it has a generic
     * type that must match the type of the CheckComboBox itself.
     */
    public final ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    private static class CheckComboBoxBitSetSelectionModel<T> extends MultipleSelectionModel<T> {
        
        /***********************************************************************
         *                                                                     *
         * Internal properties                                                 *
         *                                                                     *
         **********************************************************************/

        private final CheckComboBox<T> checkComboBox;
        private final ObservableList<T> items;
        
        private final BitSet selectedIndices;
        private final ReadOnlyUnbackedObservableList<Integer> selectedIndicesList;
        private final ReadOnlyUnbackedObservableList<T> selectedItemsList;
        
        

        /***********************************************************************
         *                                                                     *
         * Constructors                                                        *
         *                                                                     *
         **********************************************************************/

        public CheckComboBoxBitSetSelectionModel(final CheckComboBox<T> checkComboBox) {
            if (checkComboBox == null) {
                throw new IllegalArgumentException("CheckComboBox can not be null");
            }

            this.checkComboBox = checkComboBox;
            this.items = checkComboBox.getItems();
            
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
                checkComboBox.itemBooleanMap.clear();
                for (int i = 0; i < items.size(); i++) {
                    final int index = i;
                    final T item = items.get(index);
                    
                    final BooleanProperty booleanProperty = new SimpleBooleanProperty(item, "selected", false);
                    checkComboBox.itemBooleanMap.put(item, booleanProperty);
                    
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
}
