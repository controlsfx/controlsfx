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

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

/**
 * A GridCell is created to represent items in the {@link GridView} 
 * {@link GridView#getItems() items list}. As with other JavaFX UI controls
 * (like {@link ListView}, {@link TableView}, etc), the {@link GridView} control
 * is virtualised, meaning it is exceedingly memory and CPU efficient. Refer to
 * the {@link GridView} class documentation for more details.
 *  
 * @see GridView
 * @see GridCell
 */
public class GridCell<T> extends IndexedCell<T> {
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    /**
     * Creates a default GridCell instance.
     */
	public GridCell() {
		getStyleClass().add("grid-cell");
		
//		itemProperty().addListener(new ChangeListener<T>() {
//            @Override public void changed(ObservableValue<? extends T> arg0, T oldItem, T newItem) {
//                updateItem(newItem, newItem == null);
//            }
//        });
		
		// TODO listen for index change and update index and item, rather than
		// listen to just item update as above. This requires the GridCell to 
		// know about its containing GridRow (and the GridRow to know its 
		// containing GridView)
		indexProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                final GridRow<T> gridRow = getGridRow();
                if (gridRow == null) return;
                
                GridView<T> gridView = gridRow.getGridView();
                T item = gridView.getItems().get(getIndex());
                
//                updateIndex(getIndex());
                updateItem(item, item == null);
            }
        });
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override protected Skin<?> createDefaultSkin() {
        return new GridCellSkin<T>(this);
    }
	
	
	
	/**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    /**
     * The {@link GridRow} that this GridCell instance is currently placed within.
     */
	public SimpleObjectProperty<GridRow<T>> gridRowProperty() {
        return gridRow;
    }
	private final SimpleObjectProperty<GridRow<T>> gridRow = new SimpleObjectProperty<>(this, "gridRow");
	
	/**
     * Sets the {@link GridRow} that this GridCell instance is placed within.
     */
    public final void setGridRow(GridRow<T> value) {
        gridRow.set(value);
    }
    
    /**
     * Returns the {@link GridRow} that this GridCell instance is currently placed within.
     */
    public GridRow<T> getGridRow() {
        return gridRow.get();
    }
}