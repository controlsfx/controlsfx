package org.controlsfx.control.textfield;

import com.sun.javafx.event.EventHandlerManager;
import impl.org.controlsfx.skin.AutoCompletePopup;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.scene.Node;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;

/**
 * The AutoCompletionBinding is the abstract base class of all auto-completion bindings.
 * This class is the core logic for the auto-completion feature but highly customizable.
 * 
 * <p>To use the autocompletion functionality, refer to the {@link TextFields} class.
 *
 * @param <T> Model-Type of the suggestions
 * @see TextFields
 */
public abstract class AutoCompletionBinding<T> implements EventTarget {


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
    private boolean ignoreInputChanges = false;

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
     * @param converter The converter to be used to convert suggestions to strings 
     */
    protected AutoCompletionBinding(Node completionTarget, 
            Callback<ISuggestionRequest, Collection<T>> suggestionProvider,
            StringConverter<T> converter){

        this.completionTarget = completionTarget;
        this.suggestionProvider = suggestionProvider;
        this.autoCompletionPopup = new AutoCompletePopup<T>();
        this.autoCompletionPopup.setConverter(converter);

        autoCompletionPopup.setOnSuggestion(sce -> {
            try{
                setIgnoreInputChanges(true);
                completeUserInput(sce.getSuggestion());
                fireAutoCompletion(sce.getSuggestion());
                hidePopup();
            }finally{
                // Ensure that ignore is always set back to false
                setIgnoreInputChanges(false);
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
        if(!isIgnoreInputChanges()){
            onUserInputChanged(userText);
        }
    }

    /**
     * Gets the target node for auto completion
     * @return the target node for auto completion
     */
    public Node getCompletionTarget(){
        return completionTarget;
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

    protected void fireAutoCompletion(T completion){
        Event.fireEvent(this, new AutoCompletionEvent<T>(completion));
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

    /**
     * Shall changes to the user input be ignored?
     * @return
     */
    private boolean isIgnoreInputChanges(){
        return ignoreInputChanges;
    }

    /**
     * If IgnoreInputChanges is set to true, all changes to the user input are
     * ignored. This is primary used to avoid self triggering while
     * auto completing.
     * @param state
     */
    private void setIgnoreInputChanges(boolean state){
        ignoreInputChanges = state;
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
         * @return {@code true} if the request is canceled, otherwise {@code false}
         */
        public boolean isCancelled();

        /**
         * Get the user text to which suggestions shall be found
         * @return {@link String} containing the user text
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
                    Platform.runLater(() -> {
                        if(fetchedSuggestions != null && !fetchedSuggestions.isEmpty()){
                            autoCompletionPopup.getSuggestions().addAll(fetchedSuggestions);
                            showPopup();
                        }else{
                            // No suggestions found, so hide the popup
                            hidePopup();
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

    /***************************************************************************
     *                                                                         *
     * Events                                                                  *
     *                                                                         *
     **************************************************************************/


    // --- AutoCompletionEvent

    /**
     * Represents an Event which is fired after an auto completion.
     */
    @SuppressWarnings("serial")
    public static class AutoCompletionEvent<TE> extends Event {

        /**
         * The event type that should be listened to by people interested in 
         * knowing when an auto completion has been performed.
         */
        @SuppressWarnings("rawtypes")
        public static final EventType<AutoCompletionEvent> AUTO_COMPLETED = 
        new EventType<AutoCompletionEvent>("AUTO_COMPLETED"); //$NON-NLS-1$

        private final TE completion;

        /**
         * Creates a new event that can subsequently be fired.
         */
        public AutoCompletionEvent(TE completion) {
            super(AUTO_COMPLETED);
            this.completion = completion;
        }

        /**
         * Returns the chosen completion.
         */
        public TE getCompletion() {
            return completion;
        }
    }


    private ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompleted;

    /**
     * Set a event handler which is invoked after an auto completion.
     * @param value
     */
    public final void setOnAutoCompleted(EventHandler<AutoCompletionEvent<T>> value) {
        onAutoCompletedProperty().set( value);
    }

    public final EventHandler<AutoCompletionEvent<T>> getOnAutoCompleted() {
        return onAutoCompleted == null ? null : onAutoCompleted.get();
    }

    public final ObjectProperty<EventHandler<AutoCompletionEvent<T>>> onAutoCompletedProperty() {
        if (onAutoCompleted == null) {
            onAutoCompleted = new ObjectPropertyBase<EventHandler<AutoCompletionEvent<T>>>() {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                @Override protected void invalidated() {
                    eventHandlerManager.setEventHandler(
                            AutoCompletionEvent.AUTO_COMPLETED,
                            (EventHandler<AutoCompletionEvent>)(Object)get());
                }

                @Override
                public Object getBean() {
                    return AutoCompletionBinding.this;
                }

                @Override
                public String getName() {
                    return "onAutoCompleted"; //$NON-NLS-1$
                }
            };
        }
        return onAutoCompleted;
    }


    /***************************************************************************
     *                                                                         *
     * EventTarget Implementation                                              *
     *                                                                         *
     **************************************************************************/

    final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);

    /**
     * Registers an event handler to this EventTarget. The handler is called when the
     * menu item receives an {@code Event} of the specified type during the bubbling
     * phase of event delivery.
     *
     * @param <E> the specific event class of the handler
     * @param eventType the type of the events to receive by the handler
     * @param eventHandler the handler to register
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    /**
     * Unregisters a previously registered event handler from this EventTarget. One
     * handler might have been registered for different event types, so the
     * caller needs to specify the particular event type from which to
     * unregister the handler.
     *
     * @param <E> the specific event class of the handler
     * @param eventType the event type from which to unregister
     * @param eventHandler the handler to unregister
     * @throws NullPointerException if the event type or handler is null
     */
    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    /** {@inheritDoc} */
    @Override public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.prepend(eventHandlerManager);
    }


}
