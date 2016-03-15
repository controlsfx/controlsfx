package impl.org.controlsfx.table;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.Optional;

final class FilterValue<T,R> extends HBox implements Comparable<FilterValue> {

    private final ObservableValue<R> value;
    private final BooleanProperty isSelected = new SimpleBooleanProperty(true);
    private final BooleanProperty inScope = new SimpleBooleanProperty(true);
    private final ColumnFilter<T,R> columnFilter;

    private boolean initialized = false;

    FilterValue(ObservableValue<R> value, ColumnFilter<T,R> columnFilter) {
        this.value = value;
        this.columnFilter = columnFilter;
    }
    public void initialize() {
        if (!initialized) {
            final CheckBox checkBox = new CheckBox();
            final Label label = new Label();
            label.setText(Optional.ofNullable(valueProperty()).map(ObservableValue::getValue).map(Object::toString).orElse(null));
            inScope.addListener((Observable v) -> label.textFillProperty().set(getInScopeProperty().get() ? Color.BLACK : Color.LIGHTGRAY));
            checkBox.selectedProperty().bindBidirectional(selectedProperty());
            initialized = true;
            getChildren().addAll(checkBox,label);
        }
    }

    public ObservableValue<R> valueProperty() {
        return value;
    }

    public BooleanProperty selectedProperty() {
        return isSelected;
    }
    public BooleanProperty getInScopeProperty() {
        return inScope;
    }

    public void refreshScope() {
        inScope.setValue(columnFilter.wasLastFiltered() || columnFilter.getVisibleValues().contains(this));
    }

    @Override
    public String toString() {
        return value == null || value.getValue() == null ? "" : value.getValue().toString();
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

    @Override
    public int compareTo(FilterValue other) {
        if (other == null)
            return 1;

        if (other.valueProperty().getValue() == null) {
            if (valueProperty().getValue() == null) {
                return 0;
            }
            else {
                return 1;
            }
        }
        if (valueProperty().getValue() == null) {
            return -1;
        }
        if (valueProperty().getValue() instanceof Comparable<?> && other.valueProperty().getValue() instanceof Comparable<?>) {
            return ((Comparable) this.valueProperty().getValue()).compareTo(other.valueProperty().getValue());
        }
        else {
            return valueProperty().getValue().toString().compareTo(other.valueProperty().getValue().toString());
        }
    }
}
