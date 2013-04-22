package org.controlsfx.control;

import impl.org.controlsfx.skin.GridRowSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;

public class GridRow<T> extends IndexedCell<T>{


    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/	

    public GridRow() {
        super();
        getStyleClass().add("grid-row");

        // we need to do this (or something similar) to allow for mouse wheel
        // scrolling, as the GridRow has to report that it is non-empty (which
        // is the second argument going into updateItem).
        indexProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                updateItem(null, getIndex() == -1);
            }
        });
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
    private final SimpleObjectProperty<GridView<T>> gridView = new SimpleObjectProperty<>(this, "gridView");

    public final void updateGridView(GridView<T> gridView) {
        this.gridView.set(gridView);
    }
    
    public GridView<T> getGridView() {
        return gridView.get();
    }

    public SimpleObjectProperty<GridView<T>> gridViewProperty() {
        return gridView;
    }
}
