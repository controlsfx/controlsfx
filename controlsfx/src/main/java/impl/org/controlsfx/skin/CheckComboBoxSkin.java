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
package impl.org.controlsfx.skin;

import java.util.Collections;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

import org.controlsfx.control.CheckComboBox;

import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

public class CheckComboBoxSkin<T> extends BehaviorSkinBase<CheckComboBox<T>, BehaviorBase<CheckComboBox<T>>> {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    
    
    /**************************************************************************
     * 
     * fields
     * 
     **************************************************************************/
    
    // visuals
    private final ComboBox<T> comboBox;
    private final ListCell<T> buttonCell;
    
    // data
    private final CheckComboBox<T> control;
    private final ObservableList<T> items;
    private final ReadOnlyUnbackedObservableList<Integer> selectedIndices;
    private final ReadOnlyUnbackedObservableList<T> selectedItems;
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    @SuppressWarnings("unchecked")
    public CheckComboBoxSkin(final CheckComboBox<T> control) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));
        
        this.control = control;
        this.items = control.getItems();
        
        selectedIndices = (ReadOnlyUnbackedObservableList<Integer>) control.getCheckModel().getCheckedIndices();
        selectedItems = (ReadOnlyUnbackedObservableList<T>) control.getCheckModel().getCheckedItems();
        
        comboBox = new ComboBox<T>(items) {
            @Override protected javafx.scene.control.Skin<?> createDefaultSkin() {
                return new ComboBoxListViewSkin<T>(this) {
                    // overridden to prevent the popup from disappearing
                    @Override protected boolean isHideOnClickEnabled() {
                        return false;
                    }
                };
            }
        };
        comboBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        // installs a custom CheckBoxListCell cell factory
        comboBox.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override public ListCell<T> call(ListView<T> listView) {
                CheckBoxListCell<T> result = new CheckBoxListCell<>(item -> control.getItemBooleanProperty(item));
                //clicking on the label checks/unchecks the item
                result.setOnMouseClicked(e -> {
                    T item = result.getItem();
                    if (control.getCheckModel().isChecked(item)) {                        
                        control.getCheckModel().clearCheck(item);
                    } else {
                        control.getCheckModel().check(item);                        
                    }                    
                });                
                result.converterProperty().bind(control.converterProperty());
                return result;
            };
        });
        
        // we render the selection into a custom button cell, so that it can 
        // be pretty printed (e.g. 'Item 1, Item 2, Item 10').
        buttonCell = new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                // we ignore whatever item is selected, instead choosing
                // to display the selected item text using commas to separate
                // each item
                setText(getTextString());
            }
        };
        comboBox.setButtonCell(buttonCell);
        comboBox.setValue((T)getTextString());
        
        // The zero is a dummy value - it just has to be legally within the bounds of the
        // item count for the CheckComboBox items list.
        selectedIndices.addListener((ListChangeListener<Integer>) c -> buttonCell.updateIndex(0));
        
        getChildren().add(comboBox);
    }
    
    
    /**************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return comboBox.minWidth(height);
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return comboBox.minHeight(width);
    }
    
    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return comboBox.prefWidth(height);
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return comboBox.prefHeight(width);
    }
    
    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }
    
    
    
    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    protected String getTextString() {
        
        if (control.getTitle() != null) {
            //if a title has been set, we use it...
            return control.getTitle();
        } else {            
            //...otherwise we generate a string concatenating the items
            return buildString();
        }
        
    }
    
    private String buildString() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, max = selectedItems.size(); i < max; i++) {
            T item = selectedItems.get(i);
            if (control.getConverter() == null) {
                sb.append(item);
            } else {
                sb.append(control.getConverter().toString(item));
            }
            if (i < max - 1) {
                sb.append(", "); //$NON-NLS-1$
            }
        }
        return sb.toString();
    }
    
    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/


    
}
