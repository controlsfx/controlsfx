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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 * A simple UI control that makes it possible to select zero or more items within
 * a TreeView without the need to set a custom cell factory or manually create
 * boolean properties for each row - simply use the 
 * {@link #checkModelProperty() check model} to request the current selection 
 * state.
 *
 * @param <T> The type of the data in the TreeView.
 */
public class CheckTreeView<T> extends TreeView<T> {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    

    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * 
     */
    public CheckTreeView() {
        this(null);
    }
    
    /**
     * 
     */
    public CheckTreeView(final CheckBoxTreeItem<T> root) {
        super(root);
        
        setCheckModel(new CheckTreeViewBitSetCheckModel<T>(this));
        setCellFactory(CheckBoxTreeCell.<T>forTreeView());
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    
    
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    // --- Check Model
    private ObjectProperty<MultipleSelectionModel<TreeItem<T>>> checkModel = 
            new SimpleObjectProperty<>(this, "checkModel");
    
    /**
     * Sets the 'check model' to be used in the CheckTreeView - this is the
     * code that is responsible for representing the selected state of each
     * {@link CheckBox} (not to be confused with the 
     * {@link #selectionModelProperty() selection model}, which represents the
     * selection state of each row).. 
     */
    public final void setCheckModel(MultipleSelectionModel<TreeItem<T>> value) {
        checkModelProperty().set(value);
    }

    /**
     * Returns the currently installed check model.
     */
    public final MultipleSelectionModel<TreeItem<T>> getCheckModel() {
        return checkModel == null ? null : checkModel.get();
    }

    /**
     * The check model provides the API through which it is possible
     * to check single or multiple items within a CheckTreeView, as  well as inspect
     * which items have been checked by the user. Note that it has a generic
     * type that must match the type of the CheckTreeView itself.
     */
    public final ObjectProperty<MultipleSelectionModel<TreeItem<T>>> checkModelProperty() {
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
    
    private static class CheckTreeViewBitSetCheckModel<T> extends CheckBitSetModelBase<TreeItem<T>> {
        
        /***********************************************************************
         *                                                                     *
         * Internal properties                                                 *
         *                                                                     *
         **********************************************************************/
        
        private final CheckTreeView<T> treeView;
        private final TreeItem<T> root;
        
        
        
        /***********************************************************************
         *                                                                     *
         * Constructors                                                        *
         *                                                                     *
         **********************************************************************/
        
        CheckTreeViewBitSetCheckModel(final CheckTreeView<T> treeView) {
            super(null);
            
            this.treeView = treeView;
            this.root = treeView.getRoot();
            this.root.addEventHandler(CheckBoxTreeItem.<T>checkBoxSelectionChangedEvent(), new EventHandler<TreeModificationEvent<T>>() {
                public void handle(TreeModificationEvent<T> e) {
                    CheckBoxTreeItem<T> treeItem = e.getTreeItem();
                    
                    final int index = getItemIndex(treeItem);
                    if (treeItem.isSelected() && ! treeItem.isIndeterminate()) {
                        select(index);
                    } else { 
                        clearSelection(index);
                    }
                }
            });
        }
        
        
        
        /***********************************************************************
         *                                                                     *
         * Implementing abstract API                                           *
         *                                                                     *
         **********************************************************************/

        @Override public TreeItem<T> getItem(int index) {
            return treeView.getTreeItem(index);
        }
        
        @Override public int getItemCount() {
            return treeView.getExpandedItemCount();
        }
        
        @Override public int getItemIndex(TreeItem<T> item) {
            return treeView.getRow(item);
        }
        
        
        
        /***********************************************************************
         *                                                                     *
         * Overriding public API                                               *
         *                                                                     *
         **********************************************************************/
        
        @Override protected void updateMap() {
            // no-op
        }
    }
}
