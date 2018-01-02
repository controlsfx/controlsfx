package org.controlsfx.control.table;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.Optional;

public final class FilterValue<T,R> extends HBox implements Comparable<FilterValue<T,R>> {

    private final R value;
    private final BooleanProperty isSelected = new SimpleBooleanProperty(true);
    private final BooleanProperty inScope = new SimpleBooleanProperty(true);
    private final ColumnFilter<T,R> columnFilter;
    private final InvalidationListener scopeListener;


    FilterValue(R value, ColumnFilter<T,R> columnFilter) {
        this.value = value;
        this.columnFilter = columnFilter;

        final CheckBox checkBox = new CheckBox();
        final Label label = new Label();
        label.setText(Optional.ofNullable(value).map(Object::toString).orElse(null));
        scopeListener = (Observable v) -> label.textFillProperty().set(getInScopeProperty().get() ? Color.BLACK : Color.LIGHTGRAY);
        inScope.addListener(new WeakInvalidationListener(scopeListener));
        checkBox.selectedProperty().bindBidirectional(selectedProperty());
        getChildren().addAll(checkBox,label);
    }

    /**
     * Returns the R value for this given FilterValue
     */
    public R getValue() {
        return value;
    }

    /**
     * Property indicating whether this value is selected or not.
     */
    public BooleanProperty selectedProperty() {
        return isSelected;
    }

    /**
     * Property indicating whether this value is in scope.
     */
    public BooleanProperty getInScopeProperty() {
        return inScope;
    }

    void refreshScope() {
        inScope.setValue(columnFilter.wasLastFiltered() || columnFilter.valueIsVisible(value));
    }

    @Override
    public String toString() {
        return Optional.ofNullable(value).map(Object::toString).orElse("");
    }


    @Override
    public int compareTo(FilterValue<T,R> other) {
        if (value != null && other.value != null) {
            if (value instanceof Comparable<?> && other.value instanceof Comparable<?>) {
                return ((Comparable<Object>) value).compareTo(((Comparable<Object>) other.value));
            }
        }
        return Optional.ofNullable(value).map(Object::toString).orElse("")
                .compareTo(Optional.ofNullable(other).map(Object::toString).orElse(""));
    }
}
