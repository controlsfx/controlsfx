package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellSkin;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class GridCell<T> extends IndexedCell<T> {

	public GridCell() {
		getStyleClass().add("grid-cell");
	}
	
	@Override protected Skin<?> createDefaultSkin() {
        return new GridCellSkin<T>(this);
    }
	
	@Override protected String getUserAgentStylesheet() {
		return GridView.class.getResource("gridview.css").toExternalForm();
	}
	
	/**
	 * For a better performance cells can be cached. Once a row is recycled it can use cached cells instead of creating a new one.
	 * @return
	 */
	public boolean isCacheable() {
		return true;
	}
}