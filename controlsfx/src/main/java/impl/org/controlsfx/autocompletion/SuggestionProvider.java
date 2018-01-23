/**
 * Copyright (c) 2014, 2016 ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.autocompletion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Callback;

import org.controlsfx.control.textfield.AutoCompletionBinding.ISuggestionRequest;

/**
 * This is a simple implementation of a generic suggestion provider callback.
 * The complexity of suggestion generation is O(n) where n is the number of possible suggestions.
 * 
 * @param <T> Type of suggestions
 */
public abstract class SuggestionProvider<T> implements Callback<ISuggestionRequest, Collection<T>>{

    private final List<T> possibleSuggestions = new ArrayList<>();
    private final Object possibleSuggestionsLock = new Object();

    /**
     * Tell the provider to show all suggestions if empty text given
     *
     * @defaultValue false
     */
    private final BooleanProperty showAllIfEmptyProperty = new SimpleBooleanProperty(false);

    /**
     * Gets showAllIfEmptyProperty
     *
     * @return the property
     */
    public final BooleanProperty showAllIfEmptyProperty() {
        return showAllIfEmptyProperty;
    }

    /**
     * Gets the value of the property showAllIfEmptyProperty
     *
     * @return the value of the property
     */
    public final boolean isShowAllIfEmpty() {
        return showAllIfEmptyProperty.get();
    }

    /**
     * Sets the value of the property showAllIfEmptyProperty
     *
     * @param showAllIfEmpty if true, the provider will show all suggestions if
     * empty text given
     */
    public final void setShowAllIfEmpty(boolean showAllIfEmpty) {
        showAllIfEmptyProperty.set(showAllIfEmpty);
    }

    /**
     * Add the given new possible suggestions to this  SuggestionProvider
     * @param newPossible
     */
    public void addPossibleSuggestions(@SuppressWarnings("unchecked") T... newPossible){     
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
    public Collection<T> call(final ISuggestionRequest request) {
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
        } else {
            if (isShowAllIfEmpty()) {
                synchronized (possibleSuggestionsLock) {
                    suggestions.addAll(possibleSuggestions);
                }
            }
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
     * @param possibleSuggestions All possible suggestions
     * @return
     */
    public static <T> SuggestionProvider<T> create(Collection<T> possibleSuggestions){
        return create(null, possibleSuggestions);
    }

    /**
     * Create a default suggestion provider based on the toString() method of the generic objects
     * using the provided stringConverter
     * 
     * @param stringConverter A stringConverter which converts generic T into a string
     * @param possibleSuggestions All possible suggestions
     * @return
     */
    public static <T> SuggestionProvider<T> create(Callback<T, String> stringConverter, Collection<T> possibleSuggestions){
        SuggestionProviderString<T> suggestionProvider = new SuggestionProviderString<>(stringConverter);
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
     * All generic suggestions T are turned into strings for processing.
     * 
     */
    private static class SuggestionProviderString<T> extends SuggestionProvider<T> {

        private Callback<T, String> stringConverter;

        private final Comparator<T> stringComparator = new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                String o1str = stringConverter.call(o1);
                String o2str = stringConverter.call(o2);
                return o1str.compareTo(o2str);
            }
        };

        /**
         * Create a new SuggestionProviderString
         * @param stringConverter
         */
        public SuggestionProviderString(Callback<T, String> stringConverter){
            this.stringConverter = stringConverter;

            // In case no stringConverter was provided, use the default strategy
            if(this.stringConverter == null){
                this.stringConverter = new Callback<T, String>() {
                    @Override
                    public String call(T obj) {
                        return obj != null ? obj.toString() : ""; //$NON-NLS-1$
                    }
                };
            }
        }

        /**{@inheritDoc}*/
        @Override
        protected Comparator<T> getComparator() {
            return stringComparator;
        }

        /**{@inheritDoc}*/
        @Override
        protected boolean isMatch(T suggestion, ISuggestionRequest request) {
            String userTextLower = request.getUserText().toLowerCase();
            String suggestionStr = stringConverter.call(suggestion).toLowerCase();
            return suggestionStr.contains(userTextLower);
        }
    }
}
