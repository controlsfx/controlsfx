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
     * Create a default suggestion provider for the given strings
     * @param possibleSuggestions
     * @return
     */
    public static SuggestionProvider<String> create(String... possibleSuggestions){
        SuggestionProviderString suggestionProvider = new SuggestionProviderString();
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
    private static class SuggestionProviderString extends SuggestionProvider<String> {

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
            String userTextLower = request.getUserText().toLowerCase();
            suggestion = suggestion.toLowerCase();
            return suggestion.contains(userTextLower) 
                    && !suggestion.equals(userTextLower);
        }
    }
}
