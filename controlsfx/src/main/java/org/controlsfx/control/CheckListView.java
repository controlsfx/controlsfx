/**
 * Copyright (c) 2013, 2015, ControlsFX
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

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

/**
 * A simple UI control that makes it possible to select zero or more items within
 * a ListView without the need to set a custom cell factory or manually create
 * boolean properties for each row - simply use the 
 * {@link #checkModelProperty() check model} to request the current selection 
 * state.
 * 
 * <h3>Screenshot</h3>
 * <p>The following screenshot shows the CheckListView with some sample data:
 * 
 * <br>
 * <img src="checkListView.png" alt="Screenshot of CheckListView">
 * 
 * <h3>Code Example:</h3>
 * <p>To create the CheckListView shown in the screenshot, simply do the 
 * following:
 * 
 * <pre>
 * {@code
 * // create the data to show in the CheckListView 
 * final ObservableList<String> strings = FXCollections.observableArrayList();
 * for (int i = 0; i <= 100; i++) {
 *     strings.add("Item " + i);
 * }
 * 
 * // Create the CheckListView with the data 
 * final CheckListView<String> checkListView = new CheckListView<>(strings);
 *       
 * // and listen to the relevant events (e.g. when the selected indices or 
 * // selected items change).
 * checkListView.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
 *     public void onChanged(ListChangeListener.Change<? extends String> c) {
 *         System.out.println(checkListView.getCheckModel().getCheckedItems());
 *     }
 * });
 * }</pre>
 *
 * @param <T> The type of the data in the CheckListView.
 */
public class CheckListView<T> extends ListView<T> {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final Map<T, BooleanProperty> itemBooleanMap;
    

    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a new CheckListView instance with an empty list of choices.
     */
    public CheckListView() {
        this(FXCollections.<T> observableArrayList());
    }
    
    /**
     * Creates a new CheckListView instance with the given items available as
     * choices.
     * 
     * @param items The items to display within the CheckListView.
     */
    public CheckListView(ObservableList<T> items) {
        super(items);
        this.itemBooleanMap = new HashMap<>();
        
        setCheckModel(new CheckListViewBitSetCheckModel<>(getItems(), itemBooleanMap));
        itemsProperty().addListener(ov -> {
            setCheckModel(new CheckListViewBitSetCheckModel<>(getItems(), itemBooleanMap));
        });
        
        setCellFactory(listView -> new CheckBoxListCell<>(new Callback<T, ObservableValue<Boolean>>() {
            @Override public ObservableValue<Boolean> call(T item) {
                return getItemBooleanProperty(item);
            }
        }));
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * Returns the {@link BooleanProperty} for a given item index in the 
     * CheckListView. This is useful if you want to bind to the property.
     */
    public BooleanProperty getItemBooleanProperty(int index) {
        if (index < 0 || index >= getItems().size()) return null;
        return getItemBooleanProperty(getItems().get(index));
    }
    
    /**
     * Returns the {@link BooleanProperty} for a given item in the 
     * CheckListView. This is useful if you want to bind to the property.
     */
    public BooleanProperty getItemBooleanProperty(T item) {
        return itemBooleanMap.get(item);
    }
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    // --- Check Model
    private ObjectProperty<IndexedCheckModel<T>> checkModel = 
            new SimpleObjectProperty<>(this, "checkModel"); //$NON-NLS-1$
    
    /**
     * Sets the 'check model' to be used in the CheckListView - this is the
     * code that is responsible for representing the selected state of each
     * {@link CheckBox} - that is, whether each {@link CheckBox} is checked or 
     * not (and not to be confused with the 
     * selection model concept, which is used in the ListView control to 
     * represent the selection state of each row).. 
     */
    public final void setCheckModel(IndexedCheckModel<T> value) {
        checkModelProperty().set(value);
    }

    /**
     * Returns the currently installed check model.
     */
    public final IndexedCheckModel<T> getCheckModel() {
        return checkModel == null ? null : checkModel.get();
    }

    /**
     * The check model provides the API through which it is possible
     * to check single or multiple items within a CheckListView, as  well as inspect
     * which items have been checked by the user. Note that it has a generic
     * type that must match the type of the CheckListView itself.
     */
    public final ObjectProperty<IndexedCheckModel<T>> checkModelProperty() {
        return checkModel;
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
    
    private static class CheckListViewBitSetCheckModel<T> extends CheckBitSetModelBase<T> {
        
        /***********************************************************************
         *                                                                     *
         * Internal properties                                                 *
         *                                                                     *
         **********************************************************************/
        
        private final ObservableList<T> items;
        
        
        
        /***********************************************************************
         *                                                                     *
         * Constructors                                                        *
         *                                                                     *
         **********************************************************************/
        
        CheckListViewBitSetCheckModel(final ObservableList<T> items, final Map<T, BooleanProperty> itemBooleanMap) {
            super(itemBooleanMap);
            
            this.items = items;
            this.items.addListener(new ListChangeListener<T>() {
                @Override public void onChanged(Change<? extends T> c) {
                    updateMap();
                }
            });
            
            updateMap();
        }
        
        
        
        /***********************************************************************
         *                                                                     *
         * Implementing abstract API                                           *
         *                                                                     *
         **********************************************************************/

        @Override public T getItem(int index) {
            return items.get(index);
        }
        
        @Override public int getItemCount() {
            return items.size();
        }
        
        @Override public int getItemIndex(T item) {
            return items.indexOf(item);
        }
    }
}
