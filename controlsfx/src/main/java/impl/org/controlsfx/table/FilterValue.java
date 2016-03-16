package impl.org.controlsfx.table;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.Optional;

final class FilterValue<T,R> extends HBox implements Comparable<FilterValue> {

    private final R value;
    private final BooleanProperty isSelected = new SimpleBooleanProperty(true);
    private final BooleanProperty inScope = new SimpleBooleanProperty(true);
    private final ColumnFilter<T,R> columnFilter;

    private boolean initialized = false;

    FilterValue(R value, ColumnFilter<T,R> columnFilter) {
        this.value = value;
        this.columnFilter = columnFilter;
    }
    public void initialize() {
        if (!initialized) {
            final CheckBox checkBox = new CheckBox();
            final Label label = new Label();
            label.setText(Optional.ofNullable(value).map(Object::toString).orElse(null));
            inScope.addListener((Observable v) -> label.textFillProperty().set(getInScopeProperty().get() ? Color.BLACK : Color.LIGHTGRAY));
            checkBox.selectedProperty().bindBidirectional(selectedProperty());
            initialized = true;
            getChildren().addAll(checkBox,label);
        }
    }

    public R getValue() {
        return value;
    }

    public BooleanProperty selectedProperty() {
        return isSelected;
    }
    public BooleanProperty getInScopeProperty() {
        return inScope;
    }

    public void refreshScope() {
        inScope.setValue(columnFilter.wasLastFiltered() || columnFilter.valueIsVisible(this.value));
    }

    @Override
    public String toString() {
        return Optional.ofNullable(value).map(Object::toString).orElse("");
    }


    @Override
    public int compareTo(FilterValue other) {
        return Optional.ofNullable(value).map(Object::toString).orElse("")
                .compareTo(Optional.ofNullable(other).map(Object::toString).orElse(""));
    }
}
