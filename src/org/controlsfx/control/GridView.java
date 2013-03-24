package org.controlsfx.control;

import impl.org.controlsfx.skin.GridViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

public class GridView<T> extends Control {

    private ObjectProperty<ObservableList<T>> items;

    private ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactory;

    private DoubleProperty cellWidth;

    private DoubleProperty cellHeight;

    private DoubleProperty horizontalCellSpacing;

    private DoubleProperty verticalCellSpacing;

    private ObjectProperty<HPos> horizontalAlignment;

    public GridView() {
        this(FXCollections.<T> observableArrayList());
    }
    
    public GridView(ObservableList<T> items) {
        getStyleClass().add("grid-view");
        setItems(items);
    }
    
    @Override protected Skin<?> createDefaultSkin() {
        return new GridViewSkin<>(this);
    }

    @Override public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    public void setHorizontalCellSpacing(double value) {
        horizontalCellSpacingProperty().set(value);
    }

    public double getHorizontalCellSpacing() {
        return horizontalCellSpacing == null ? 12.0 : horizontalCellSpacing.get();
    }

    public final DoubleProperty horizontalCellSpacingProperty() {
        if (horizontalCellSpacing == null) {
            horizontalCellSpacing = new SimpleDoubleProperty(12);
        }
        return horizontalCellSpacing;
    }

    public void setVerticalCellSpacing(double value) {
        verticalCellSpacingProperty().set(value);
    }

    public double getVerticalCellSpacing() {
        return verticalCellSpacing == null ? 12.0 : verticalCellSpacing.get();
    }

    public final DoubleProperty verticalCellSpacingProperty() {
        if (verticalCellSpacing == null) {
            verticalCellSpacing = new SimpleDoubleProperty(12);
        }
        return verticalCellSpacing;
    }

    public final DoubleProperty cellWidthProperty() {
        if (cellWidth == null) {
            cellWidth = new SimpleDoubleProperty(64);
        }
        return cellWidth;
    }

    public void setCellWidth(double value) {
        cellWidthProperty().set(value);
    }

    public double getCellWidth() {
        return cellWidth == null ? 64.0 : cellWidth.get();
    }

    public final DoubleProperty cellHeightProperty() {
        if (cellHeight == null) {
            cellHeight = new SimpleDoubleProperty(64);
        }
        return cellHeight;
    }

    public void setCellHeight(double value) {
        cellHeightProperty().set(value);
    }

    public double getCellHeight() {
        return cellHeight == null ? 64.0 : cellHeight.get();
    }

    public final ObjectProperty<HPos> horizontalAlignmentProperty() {
        if (horizontalAlignment == null) {
            horizontalAlignment = new SimpleObjectProperty<HPos>(HPos.CENTER);
        }
        return horizontalAlignment;
    }

    public final void setHorizontalAlignment(HPos value) {
        horizontalAlignmentProperty().set(value);
    }

    public final HPos getHorizontalAlignment() {
        return horizontalAlignment == null ? HPos.CENTER : horizontalAlignment.get();
    }

    public final ObjectProperty<Callback<GridView<T>, GridCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<Callback<GridView<T>, GridCell<T>>>(this, "cellFactory");
        }
        return cellFactory;
    }

    public final void setCellFactory(Callback<GridView<T>, GridCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    public final Callback<GridView<T>, GridCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    public final void setItems(ObservableList<T> value) {
        itemsProperty().set(value);
    }

    public final ObservableList<T> getItems() {
        return items == null ? null : items.get();
    }

    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        if (items == null) {
            items = new SimpleObjectProperty<ObservableList<T>>(this, "items");
        }
        return items;
    }
    
    @Override protected String getUserAgentStylesheet() {
        return GridView.class.getResource("gridview.css").toExternalForm();
    }
}
