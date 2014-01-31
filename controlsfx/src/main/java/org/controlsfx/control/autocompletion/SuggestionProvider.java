package org.controlsfx.control.autocompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.util.Callback;

import org.controlsfx.control.autocompletion.AutoCompletionController.ISuggestionRequest;

/**
 * This is a simple implementation of a generic suggestion provider callback.
 *
 * @param <T>
 */
public abstract class SuggestionProvider<T> implements Callback<ISuggestionRequest, Collection<T>>{

    private final List<T> possibleSuggestions = new ArrayList<T>();
    private final Object possibleSuggestionsLock = new Object();


    /**
     * Add the given new possible suggestions to this  SuggestionProvider
     * @param newPossible
     */
    public void addPossibleSuggetions(T... newPossible){     
        addPossibleSuggetions(Arrays.asList(newPossible));
    }

    /**
     * Add the given new possible suggestions to this  SuggestionProvider
     * @param newPossible
     */
    public void addPossibleSuggetions(Collection<T> newPossible){
        synchronized (possibleSuggestionsLock) {
            possibleSuggestions.addAll(newPossible);
        }
    }

    public void clearSuggestions(){
        synchronized (possibleSuggestionsLock) {
            possibleSuggestions.clear();
        }
    }

    @Override
    public final Collection<T> call(final ISuggestionRequest request) {
        List<T> suggestions = new ArrayList<>();

        for (T possibleSuggestion : possibleSuggestions) {
            if(isMatch(possibleSuggestion, request)){
                suggestions.add(possibleSuggestion);
            }
        }
        Collections.sort(suggestions, getComparator());
        return suggestions;
    }

    /**
     * Get the comparator to order the suggestions
     * @return
     */
    protected abstract Comparator<T> getComparator();

    /**
     * Check the given suggestion is a match
     * @param suggestion
     * @param request
     * @return
     */
    protected abstract boolean isMatch(T suggestion, ISuggestionRequest request);
}
