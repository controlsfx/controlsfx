package impl.org.controlsfx.table;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

public final class FilterValue {

    private final ObservableValue<?> value;
    private final BooleanProperty isSelected = new SimpleBooleanProperty(true);
    private final BooleanProperty inScope = new SimpleBooleanProperty(true);
    private final ColumnFilter<?> columnFilter;

    FilterValue(ObservableValue<?> value, ColumnFilter<?> columnFilter) {
        this.value = value;
        this.columnFilter = columnFilter;
    }

    public ObservableValue<?> valueProperty() {
        return value;
    }

    public BooleanProperty selectedProperty() {
        return isSelected;
    }
    public BooleanProperty getInScopeProperty() {
        return inScope;
    }

    void refreshScope() {
        inScope.setValue(columnFilter.wasLastFiltered() || columnFilter.getVisibleValues().contains(this));
    }

    @Override
    public String toString() {
        return value == null ? "" : value.getValue().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterValue that = (FilterValue) o;

        return Optional.ofNullable(value).map(ObservableValue::getValue).equals(Optional.ofNullable(that.value).map(ObservableValue::getValue))
                || Optional.ofNullable(value.getValue()).equals(Optional.ofNullable(that.value.getValue()));

    }

    @Override
    public int hashCode() {
        return value == null || value.getValue() == null ? 0 : value.getValue().hashCode();
    }
}
