package org.controlsfx.control;

import java.util.HashMap;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.util.Builder;
import javafx.util.Callback;

public class GridViewBuilder<B extends GridViewBuilder<B, T>, T> implements Builder<GridView<T>> {
    private static final String ITEMS = "items";
    private static final String CELL_FACTORY = "cellFactory";
    private static final String CELL_WIDTH = "cellWidth";
    private static final String CELL_HEIGHT = "cellHeight";
    private static final String HORIZONTAL_CELL_SPACING = "horizontalCellSpacing";
    private static final String VERTICAL_CELL_SPACING = "verticalCellSpacing";
    @SuppressWarnings("rawtypes")
    private HashMap<String, Property> properties = new HashMap<String, Property>();

    protected GridViewBuilder() {
        super();
    };

    public static final <T, U extends GridViewBuilder<U, T>> GridViewBuilder<U, T> create(Class<T> cls) {
        return new GridViewBuilder<>();
    }

    public final GridViewBuilder<B, T> items(ObservableList<T> items) {
        properties.put(ITEMS, new SimpleObjectProperty<ObservableList<T>>(items));
        return this;
    }

    public final GridViewBuilder<B, T> cellFactory(Callback<GridView<T>, GridCell<T>> cellFactory) {
        properties.put(CELL_FACTORY, new SimpleObjectProperty<Callback<GridView<T>, GridCell<T>>>(cellFactory));
        return this;
    }

    public final GridViewBuilder<B, T> cellWidth(double cellWidth) {
        properties.put(CELL_WIDTH, new SimpleObjectProperty<Double>(cellWidth));
        return this;
    }

    public final GridViewBuilder<B, T> cellHeight(double cellHeight) {
        properties.put(CELL_HEIGHT, new SimpleObjectProperty<Double>(cellHeight));
        return this;
    }

    public final GridViewBuilder<B, T> horizontalCellSpacing(double horizontalCellSpacing) {
        properties.put(HORIZONTAL_CELL_SPACING, new SimpleObjectProperty<Double>(horizontalCellSpacing));
        return this;
    }

    public final GridViewBuilder<B, T> verticalCellSpacing(double verticalCellSpacing) {
        properties.put(VERTICAL_CELL_SPACING, new SimpleObjectProperty<Double>(verticalCellSpacing));
        return this;
    }

    @SuppressWarnings("unchecked") @Override public GridView<T> build() {
        final GridView<T> control = new GridView<>();
        for (String key : properties.keySet()) {
            if (ITEMS.equals(key)) {
                control.setItems(((SimpleObjectProperty<ObservableList<T>>) properties.get(key)).get());
            } else if (CELL_FACTORY.equals(key)) {
                control.setCellFactory(((SimpleObjectProperty<Callback<GridView<T>, GridCell<T>>>) properties.get(key)).get());
            } else if (CELL_WIDTH.equals(key)) {
                control.setCellWidth(((SimpleObjectProperty<Double>) properties.get(key)).get());
            } else if (CELL_HEIGHT.equals(key)) {
                control.setCellHeight(((SimpleObjectProperty<Double>) properties.get(key)).get());
            } else if (HORIZONTAL_CELL_SPACING.equals(key)) {
                control.setHorizontalCellSpacing(((SimpleObjectProperty<Double>) properties.get(key)).get());
            } else if (VERTICAL_CELL_SPACING.equals(key)) {
                control.setVerticalCellSpacing(((SimpleObjectProperty<Double>) properties.get(key)).get());
            }
        }
        return control;
    }

}