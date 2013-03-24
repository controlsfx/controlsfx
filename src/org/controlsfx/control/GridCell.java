package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.media.Media;

public class GridCell<T> extends IndexedCell<T> {

	public GridCell() {
		getStyleClass().add("grid-cell");
		
		itemProperty().addListener(new ChangeListener<T>() {
            @Override public void changed(ObservableValue<? extends T> arg0, T oldItem, T newItem) {
                updateItem(newItem, newItem == null);
            }
        });
	}
	
	@Override protected Skin<?> createDefaultSkin() {
        return new GridCellSkin<T>(this);
    }
	
	/**
	 * For a better performance cells can be cached. Once a row is recycled it can use cached cells instead of creating a new one.
	 * @return
	 */
	public boolean isCacheable() {
		return true;
	}
}