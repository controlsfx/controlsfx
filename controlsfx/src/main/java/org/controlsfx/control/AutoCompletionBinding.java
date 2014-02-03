package org.controlsfx.control;

import java.util.Collection;

import org.controlsfx.control.AutoCompletePopup.SuggestionChoosenEvent;

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
    private final Callback<ISuggestionRequest, Collection<T>> suggestionProvider;

    private FetchSuggestionsTask suggestionsTask = null;


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/


    /**
     * Creates a new AutoCompletionBinding
     * 
     * @param completionTarget The target node to which auto-completion shall be added
     * @param autoCompletionPopup The auto-completion popup
     * @param suggestionProvider The strategy to retrieve suggestions 
     */
    protected AutoCompletionBinding(Node completionTarget, AutoCompletePopup<T> autoCompletionPopup, Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this.completionTarget = completionTarget;
        this.suggestionProvider = suggestionProvider;
        this.autoCompletionPopup = autoCompletionPopup;


        getPopup().setOnSuggestionChoosen(new EventHandler<AutoCompletePopup.SuggestionChoosenEvent<T>>() {
            @Override
            public void handle(SuggestionChoosenEvent<T> sce) {
                completeUserInput(sce.getSelectedSuggestion());
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
     * Get the auto-completion popup
     * @return
     */
    public final AutoCompletePopup<T> getPopup(){
        return autoCompletionPopup;
    }

    /**
     * Gets the target node for auto completion
     * @return
     */
    public Node getCompletionTarget(){
        return completionTarget;
    }

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


    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    protected final void onUserInputChanged(final String userText){
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
            final Collection<T> fetchedSuggestions = suggestionProvider.call(this);
            if(!isCancelled()){
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if(!fetchedSuggestions.isEmpty()){
                            autoCompletionPopup.getSuggestions().addAll(fetchedSuggestions);
                            showPopup();
                        }else{
                            // No suggestions found, so hide the popup
                            hidePopup();
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
