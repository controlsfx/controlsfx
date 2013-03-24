package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellCache;
import impl.org.controlsfx.skin.GridRowSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class GridRow<T> extends IndexedCell<T>{

	private GridCellCache<T> cellCache;
	
	
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/	
	
	public GridRow(GridCellCache<T> cellCache) {
		super();
		this.cellCache = cellCache;
		getStyleClass().add("grid-row");
		
	}
	
	@Override protected Skin<?> createDefaultSkin() {
        return new GridRowSkin<T>(this);
    }
	
	
	
	/**************************************************************************
	 * 
	 * Properties
	 * 
	 **************************************************************************/
	
	/**
	 * 
	 */
	private final SimpleObjectProperty<GridView<T>> gridView = new SimpleObjectProperty<>();
	
	public final void updateGridView(GridView<T> gridView) {
        this.gridView.set(gridView);
    }
	
	public SimpleObjectProperty<GridView<T>> gridViewProperty() {
		return gridView;
	}
	
	
	
   /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
	
	/**
	 * 
	 * @return
	 */
	public GridCellCache<T> getCellCache() {
		return cellCache;
	}
}
