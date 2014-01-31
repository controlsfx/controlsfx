package org.controlsfx.control.autocompletion;

import java.util.Collection;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.util.Callback;

/**
 * The AutoCompletionController represents a generic auto-completion handler
 *
 * @param <T> Model-Type of the suggestions
 */
public class AutoCompletionController<T> {


    /**
     * Represents a suggestion fetch request
     *
     */
    public static interface ISuggestionRequest {
        /**
         * Is this request canceled?
         * @return
         */
        public boolean isCancelled();

        /**
         * Get the user text to which suggestions shall be found
         * @return
         */
        public String getUserText();
    }

    private final Callback<Void, Void> showPopupRequest;
    private final AutoCompletePopup<T> autoCompletionPopup;

    private FetchSuggestionsTask suggestionsTask = null;
    private final Object suggestionsTaskLock = new Object();
    private Callback<ISuggestionRequest, Collection<T>> suggestionProvider;





    public AutoCompletionController(AutoCompletePopup<T> autoCompletionPopup, Callback<ISuggestionRequest, Collection<T>> suggestionProvider, Callback<Void, Void> showPopupRequest){
        setSuggestionProvider(suggestionProvider);
        this.showPopupRequest = showPopupRequest;
        this.autoCompletionPopup = autoCompletionPopup;
    }


    /**
     * Set the current text the user has entered
     * @param userText
     */
    public final void setUserInput(String userText){
        onUserInputChanged(userText);
    }

    /**
     * Set the suggestion provider
     * @param suggestionProvider
     */
    public final void setSuggestionProvider(Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this.suggestionProvider = suggestionProvider;
    }

    public final AutoCompletePopup<T> getPopup(){
        return autoCompletionPopup;
    }


    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    protected void onUserInputChanged(final String userText){
        autoCompletionPopup.getSuggestions().clear();

        synchronized (suggestionsTaskLock) {
            if(suggestionsTask != null && suggestionsTask.isRunning()){
                // cancel the current running task
                suggestionsTask.cancel(); 
            }
            // create a new fetcher task
            suggestionsTask = new FetchSuggestionsTask(userText);
            new Thread(suggestionsTask).start();
        }
    }

    /**
     * This task is responsible to fetch suggestions asynchronous
     * by using the current defined suggestionProvider
     *
     */
    private class FetchSuggestionsTask extends Task<Void> implements ISuggestionRequest {
        private final String userText;

        public FetchSuggestionsTask(String userText){
            this.userText = userText;
        }

        @Override
        protected Void call() throws Exception {
            final Collection<T> fetchedSuggestions = suggestionProvider.call(this);
            if(!isCancelled()){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(!fetchedSuggestions.isEmpty()){
                            autoCompletionPopup.getSuggestions().addAll(fetchedSuggestions);
                            showPopupRequest.call(null);
                        }else{
                            // No suggestions found, so hide the popup
                            autoCompletionPopup.hide();
                        }
                    }
                });
            }
            return null;
        }

        @Override
        public String getUserText() {
            return userText;
        }
    }


}
