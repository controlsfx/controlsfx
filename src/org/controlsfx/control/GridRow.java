package org.controlsfx.control;

import impl.org.controlsfx.skin.GridCellCache;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.IndexedCell;

public class GridRow<T> extends IndexedCell<T>{

	private SimpleObjectProperty<GridView<T>> gridView;
	
	private SimpleBooleanProperty dirtyProperty;
	
	private GridCellCache<T> cellCache;
	
	public GridRow(GridCellCache<T> cellCache) {
		super();
		this.cellCache = cellCache;
		getStyleClass().add("grid-row");
		gridView = new SimpleObjectProperty<>();
		dirtyProperty = new SimpleBooleanProperty(false);
		indexProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0,
					Number arg1, Number arg2) {
				markAsDirty();
			}
		});
		markAsDirty();
	}
	
	public void markAsDirty() {
		dirtyProperty.set(true);
	}
	
	public SimpleBooleanProperty dirtyProperty() {
		return dirtyProperty;
	}
	
	public final void updateGridView(GridView<T> gridView) {
        this.gridView.set(gridView);
    }
	
	public SimpleObjectProperty<GridView<T>> gridView() {
		return gridView;
	}
	
	public GridCellCache<T> getCellCache() {
		return cellCache;
	}
}
