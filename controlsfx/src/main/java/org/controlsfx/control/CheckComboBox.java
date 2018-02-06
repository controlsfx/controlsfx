/**
 * Copyright (c) 2013, 2017, ControlsFX
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

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

/**
 * A simple UI control that makes it possible to select zero or more items within
 * a ComboBox-like control. Each row item shows a {@link CheckBox}, and the state
 * of each row can be queried via the {@link #checkModelProperty() check model}.
 * 
 * <h3>Screenshot</h3>
 * <p>The following screenshot shows the CheckComboBox with some sample data:
 * 
 * <br>
 * <img src="checkComboBox.png" alt="Screenshot of CheckComboBox">
 * 
 * <h3>Code Example:</h3>
 * <p>To create the CheckComboBox shown in the screenshot, simply do the 
 * following:
 * 
 * <pre>
 * {@code
 * // create the data to show in the CheckComboBox 
 * final ObservableList<String> strings = FXCollections.observableArrayList();
 * for (int i = 0; i <= 100; i++) {
 *     strings.add("Item " + i);
 * }
 * 
 * // Create the CheckComboBox with the data 
 * final CheckComboBox<String> checkComboBox = new CheckComboBox<String>(strings);
 * 
 * // and listen to the relevant events (e.g. when the selected indices or 
 * // selected items change).
 * checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
 *     public void onChanged(ListChangeListener.Change<? extends String> c) {
 *         System.out.println(checkComboBox.getCheckModel().getSelectedItems());
 *     }
 * });}
 * }</pre>
 *
 * @param <T> The type of the data in the ComboBox.
 */
public class CheckComboBox<T> extends ControlsFXControl {
    
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
     * Creates a new CheckComboBox instance with an empty list of choices.
     */
    public CheckComboBox() {
        this(null);
    }
    
    /**
     * Creates a new CheckComboBox instance with the given items available as
     * choices.
     * 
     * @param items The items to display within the CheckComboBox.
     */
    public CheckComboBox(final ObservableList<T> items) {
        final int initialSize = items == null ? 32 : items.size();
        
        this.itemBooleanMap = new HashMap<>(initialSize);
        this.items = items == null ? FXCollections.<T>observableArrayList() : items;
        setCheckModel(new CheckComboBoxBitSetCheckModel<>(this.items, itemBooleanMap));
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
    
    /**
     * Represents the list of choices available to the user, from which they can
     * select zero or more items.
     */
    public ObservableList<T> getItems() {
        return items;
    }
    
    /**
     * Returns the {@link BooleanProperty} for a given item index in the 
     * CheckComboBox. This is useful if you want to bind to the property.
     */
    public BooleanProperty getItemBooleanProperty(int index) {
        if (index < 0 || index >= items.size()) return null;
        return getItemBooleanProperty(getItems().get(index));
    }
    
    /**
     * Returns the {@link BooleanProperty} for a given item in the 
     * CheckComboBox. This is useful if you want to bind to the property.
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
     * Sets the 'check model' to be used in the CheckComboBox - this is the
     * code that is responsible for representing the selected state of each
     * {@link CheckBox} - that is, whether each {@link CheckBox} is checked or 
     * not (and not to be confused with the 
     * selection model concept, which is used in the ComboBox control to 
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
     * to check single or multiple items within a CheckComboBox, as  well as inspect
     * which items have been checked by the user. Note that it has a generic
     * type that must match the type of the CheckComboBox itself.
     */
    public final ObjectProperty<IndexedCheckModel<T>> checkModelProperty() {
        return checkModel;
    }
    
    // --- converter
    private ObjectProperty<StringConverter<T>> converter = 
            new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    
    /**
     * A {@link StringConverter} that, given an object of type T, will 
     * return a String that can be used to represent the object visually.
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() { 
        return converter; 
    }
    
    /** 
     * Sets the {@link StringConverter} to be used in the control.
     * @param value A {@link StringConverter} that, given an object of type T, will 
     * return a String that can be used to represent the object visually.
     */
    public final void setConverter(StringConverter<T> value) { 
        converterProperty().set(value); 
    }
    
    /**
     * A {@link StringConverter} that, given an object of type T, will 
     * return a String that can be used to represent the object visually.
     */
    public final StringConverter<T> getConverter() { 
        return converterProperty().get(); 
    }
    
    // --- title
    private StringProperty title = new SimpleStringProperty(null);
    
    /**
     * The title to use for this control. If a non null value is explicitly 
     * set by the client, then that string will be used, otherwise a title 
     * will be constructed concatenating the selected items
     */
    public final StringProperty titleProperty() {
        return title;
    }
    
    /**
     * Sets the title to use. If it is not null it will be used as title,
     * otherwise title will be constructed by the skin
     * @param value the string to use as title
     */
    public final void setTitle(String value) {
        title.setValue(value);
    }
    
    /**
     * The title set for this control, if it has been set explicitly by the client.
     * @return the title if it has been set, null otherwise
     */
    public final String getTitle() {
        return title.getValue();
    }      
    
    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    private static class CheckComboBoxBitSetCheckModel<T> extends CheckBitSetModelBase<T> {
        
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
        
        CheckComboBoxBitSetCheckModel(final ObservableList<T> items, final Map<T, BooleanProperty> itemBooleanMap) {
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
