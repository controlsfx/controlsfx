package impl.org.controlsfx.autocompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.util.Callback;

import org.controlsfx.control.AutoCompletionBinding.ISuggestionRequest;

/**
 * This is a simple implementation of a generic suggestion provider callback.
 * The complexity of suggestion generation is O(n) where n is the number of possible suggestions.
 * @param <T>
 */
public abstract class SuggestionProvider<T> implements Callback<ISuggestionRequest, Collection<T>>{

    private final List<T> possibleSuggestions = new ArrayList<T>();
    private final Object possibleSuggestionsLock = new Object();


    /**
     * Add the given new possible suggestions to this  SuggestionProvider
     * @param newPossible
     */
    public void addPossibleSuggestions(T... newPossible){     
        addPossibleSuggestions(Arrays.asList(newPossible));
    }

    /**
     * Add the given new possible suggestions to this  SuggestionProvider
     * @param newPossible
     */
    public void addPossibleSuggestions(Collection<T> newPossible){
        synchronized (possibleSuggestionsLock) {
            possibleSuggestions.addAll(newPossible);
        }
    }

    /**
     * Remove all current possible suggestions
     */
    public void clearSuggestions(){
        synchronized (possibleSuggestionsLock) {
            possibleSuggestions.clear();
        }
    }

    @Override
    public final Collection<T> call(final ISuggestionRequest request) {
        List<T> suggestions = new ArrayList<>();
        if(!request.getUserText().isEmpty()){
            synchronized (possibleSuggestionsLock) {
                for (T possibleSuggestion : possibleSuggestions) {
                    if(isMatch(possibleSuggestion, request)){
                        suggestions.add(possibleSuggestion);
                    }
                }
            }
            Collections.sort(suggestions, getComparator());
        }
        return suggestions;
    }

    /**
     * Get the comparator to order the suggestions
     * @return
     */
    protected abstract Comparator<T> getComparator();

    /**
     * Check the given possible suggestion is a match (is a valid suggestion)
     * @param suggestion
     * @param request
     * @return
     */
    protected abstract boolean isMatch(T suggestion, ISuggestionRequest request);


    /***************************************************************************
     *                                                                         *
     * Static methods                                                          *
     *                                                                         *
     **************************************************************************/


    /**
     * Create a default suggestion provider based on the toString() method of the generic objects
     * @param possibleSuggestions
     * @return
     */
    public static <T> SuggestionProvider<T> create(T... possibleSuggestions){
        SuggestionProviderString<T> suggestionProvider = new SuggestionProviderString<>();
        suggestionProvider.addPossibleSuggestions(possibleSuggestions);
        return suggestionProvider;
    }



    /***************************************************************************
     *                                                                         *
     * Default implementations                                                 *
     *                                                                         *
     **************************************************************************/


    /**
     * This is a simple string based suggestion provider.
     *
     */
    private static class SuggestionProviderString<T> extends SuggestionProvider<T> {

        private final Comparator<T> stringComparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return o1.toString().compareTo(o2.toString());
            }
        };

        @Override
        protected Comparator<T> getComparator() {
            return stringComparator;
        }

        @Override
        protected boolean isMatch(T suggestion, ISuggestionRequest request) {
            String userTextLower = request.getUserText().toLowerCase();
            String suggestionStr = suggestion.toString().toLowerCase();
            return suggestionStr.contains(userTextLower) 
                    && !suggestionStr.equals(userTextLower);
        }
    }
}
