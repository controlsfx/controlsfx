package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class GridCell<T> extends IndexedCell<T> {
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    /**
     * 
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
	
	@Override protected Skin<?> createDefaultSkin() {
        return new GridCellSkin<T>(this);
    }
	
	
	
	/**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/

    /**
     * 
     */
    private final SimpleObjectProperty<GridRow<T>> gridRow = new SimpleObjectProperty<>(this, "gridRow");

    public final void setGridRow(GridRow<T> value) {
        gridRow.set(value);
    }
    public GridRow<T> getGridRow() {
        return gridRow.get();
    }
    public SimpleObjectProperty<GridRow<T>> gridRowProperty() {
        return gridRow;
    }
	
}