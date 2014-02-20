package org.controlsfx.control.textfield;

import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopup.SuggestionEvent;

import java.util.Collection;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Callback;

/**
 * The AutoCompletionBinding is the abstract base class of all auto-completion bindings
 * 
 * This class is the core logic for the auto-completion feature but highly customizable.
 *
 *
 * @param <T> Model-Type of the suggestions
 */
public abstract class AutoCompletionBinding<T> {


    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final Node completionTarget;
    private final AutoCompletePopup<T> autoCompletionPopup;
    private final Object suggestionsTaskLock = new Object();

    private FetchSuggestionsTask suggestionsTask = null;
    private Callback<ISuggestionRequest, Collection<T>> suggestionProvider = null;



    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/


    /**
     * Creates a new AutoCompletionBinding
     * 
     * @param completionTarget The target node to which auto-completion shall be added
     * @param suggestionProvider The strategy to retrieve suggestions 
     */
    protected AutoCompletionBinding(Node completionTarget, Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this.completionTarget = completionTarget;
        this.suggestionProvider = suggestionProvider;
        this.autoCompletionPopup = new AutoCompletePopup<T>();

        autoCompletionPopup.setOnSuggestion(new EventHandler<AutoCompletePopup.SuggestionEvent<T>>() {
            @Override public void handle(SuggestionEvent<T> sce) {
                completeUserInput(sce.getSuggestion());
            }
        });
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/


    /**
     * Set the current text the user has entered
     * @param userText
     */
    public final void setUserInput(String userText){
        onUserInputChanged(userText);
    }

    /**
     * Gets the target node for auto completion
     * @return
     */
    public Node getCompletionTarget(){
        return completionTarget;
    }

    /**
     * Gets the auto-completion Popup-node
     * @return
     */
    public AutoCompletePopup<T> getPopup(){
        return autoCompletionPopup;
    }

    /**
     * Disposes the binding.
     */
    public abstract void dispose();



    /***************************************************************************
     *                                                                         *
     * Protected methods                                                       *
     *                                                                         *
     **************************************************************************/

    /**
     * Complete the current user-input with the provided completion.
     * Sub-classes have to provide a concrete implementation.
     * @param completion
     */
    protected abstract void completeUserInput(T completion);


    /**
     * Show the auto completion popup
     */
    protected void showPopup(){
        autoCompletionPopup.show(completionTarget);
    }

    /**
     * Hide the auto completion targets
     */
    protected void hidePopup(){
        autoCompletionPopup.hide();
    }



    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    private final void onUserInputChanged(final String userText){
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
    /***************************************************************************
     *                                                                         *
     * Inner classes and interfaces                                            *
     *                                                                         *
     **************************************************************************/

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
            Callback<ISuggestionRequest, Collection<T>> provider = suggestionProvider;
            if(provider != null){
                final Collection<T> fetchedSuggestions = provider.call(this);
                if(!isCancelled()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if(fetchedSuggestions != null && !fetchedSuggestions.isEmpty()){
                                autoCompletionPopup.getSuggestions().addAll(fetchedSuggestions);
                                showPopup();
                            }else{
                                // No suggestions found, so hide the popup
                                hidePopup();
                            }
                        }
                    });
                }
            }else {
                // No suggestion provider
                hidePopup();
            }
            return null;
        }

        @Override
        public String getUserText() {
            return userText;
        }
    }


}
