package org.controlsfx.control;

import javafx.scene.control.IndexedCell;

public class GridCell<T> extends IndexedCell<T> {

	public GridCell() {
		getStyleClass().add("grid-cell");
	}
	
	@Override
	protected String getUserAgentStylesheet() {
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