package org.controlsfx.control.autocompletion;

import java.util.Comparator;

import org.controlsfx.control.autocompletion.AutoCompletionController.ISuggestionRequest;

/**
 * This is a simple string based suggestion provider.
 *
 */
public class SuggestionProviderString extends SuggestionProvider<String> {

    private static final Comparator<String> stringComparator = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    };

    @Override
    protected Comparator<String> getComparator() {
        return stringComparator;
    }

    @Override
    protected boolean isMatch(String suggestion, ISuggestionRequest request) {
        return suggestion.contains(request.getUserText());
    }
}
