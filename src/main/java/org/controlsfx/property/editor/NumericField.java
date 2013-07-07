package org.controlsfx.property.editor;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

/*
 * TODO replace this with proper API when it becomes available:
 * https://javafx-jira.kenai.com/browse/RT-30881
 */
class NumericField extends TextField {

    private static String regex = "[-+]?[0-9]*\\.?[0-9]+";
    
//    private final DoubleStringConverter converter = new DoubleStringConverter();
    private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0.0) {
        protected void invalidated() {
            setText(Double.toString(get()));
        };
    };
    
    public NumericField() {
        textProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                // TODO handle when the text changes...?
//                value.set(converter.fromString(getText()));
            }
        });
    }
    
    public final DoubleProperty valueProperty() {
        return value;
    }

    @Override public void replaceText(int start, int end, String text) {
        if (replaceMatches(start, end, text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override public void replaceSelection(String text) {
        IndexRange range = getSelection();
        if (replaceMatches(range.getStart(), range.getEnd(), text)) {
            super.replaceSelection(text);
        }
    }

    private Boolean replaceMatches(int start, int end, String fragment) {
        return (getText().substring(0, start) + fragment + getText().substring(end)).matches(regex);
    }
}