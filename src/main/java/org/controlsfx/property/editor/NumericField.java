package org.controlsfx.property.editor;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

public class NumericField extends TextField {

    private static String regex = "[-+]?[0-9]*\\.?[0-9]+";

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