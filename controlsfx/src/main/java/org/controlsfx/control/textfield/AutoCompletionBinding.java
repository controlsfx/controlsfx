/**
 * Copyright (c) 2014, 2016, 2020 ControlsFX
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
package org.controlsfx.control.textfield;

import com.sun.javafx.event.EventHandlerManager;
import impl.org.controlsfx.skin.AutoCompletePopup;
import impl.org.controlsfx.skin.AutoCompletePopupSkin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.concurrent.Task;
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Collection;
import java.util.UUID;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;

/**
 * The AutoCompletionBinding is the abstract base class of all auto-completion bindings.
 * This class is the core logic for the auto-completion feature but highly customizable.
 * 
 * <p>To use the autocompletion functionality, refer to the {@link TextFields} class.
 *
 * The popup size can be modified through its {@link #setVisibleRowCount(int) }
 * for the height and all the usual methods for the width.
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
    private long delay = 250;

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
        this.autoCompletionPopup = new AutoCompletePopup<>();
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
     * Specifies whether the PopupWindow should be hidden when an unhandled
     * escape key is pressed while the popup has focus.
     *
     * @param value
     */
    public void setHideOnEscape(boolean value) {
        autoCompletionPopup.setHideOnEscape(value);
    }

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
     * Sets the delay in ms between a key press and the suggestion popup being displayed.
     *
     * @param delay
     */
    public final void setDelay(long delay) {
        this.delay = delay;
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


    /**
     * Set the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     * @param value
     */
    public final void setVisibleRowCount(int value) {
        autoCompletionPopup.setVisibleRowCount(value);
    }

    /**
     * Return the maximum number of rows to be visible in the popup when it is
     * showing.
     *
     * @return the maximum number of rows to be visible in the popup when it is
     * showing.
     */
    public final int getVisibleRowCount() {
        return autoCompletionPopup.getVisibleRowCount();
    }

    /**
     * Return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     *
     * @return an property representing the maximum number of rows to be visible
     * in the popup when it is showing.
     */
    public final IntegerProperty visibleRowCountProperty() {
        return autoCompletionPopup.visibleRowCountProperty();
    }
    
    /**
     * Sets the prefWidth of the popup.
     *
     * @param value
     */
    public final void setPrefWidth(double value) {
        autoCompletionPopup.setPrefWidth(value);
    }

    /**
     * Return the pref width of the popup.
     *
     * @return the pref width of the popup.
     */
    public final double getPrefWidth() {
        return autoCompletionPopup.getPrefWidth();
    }

    /**
     * Return the property associated with the pref width.
     * @return 
     */
    public final DoubleProperty prefWidthProperty() {
        return autoCompletionPopup.prefWidthProperty();
    }

    /**
     * Sets the minWidth of the popup.
     *
     * @param value
     */
    public final void setMinWidth(double value) {
        autoCompletionPopup.setMinWidth(value);
    }

    /**
     * Return the min width of the popup.
     *
     * @return the min width of the popup.
     */
    public final double getMinWidth() {
        return autoCompletionPopup.getMinWidth();
    }

    /**
     * Return the property associated with the min width.
     * @return 
     */
    public final DoubleProperty minWidthProperty() {
        return autoCompletionPopup.minWidthProperty();
    }

    /**
     * Sets the maxWidth of the popup.
     *
     * @param value
     */
    public final void setMaxWidth(double value) {
        autoCompletionPopup.setMaxWidth(value);
    }

    /**
     * Return the max width of the popup.
     *
     * @return the max width of the popup.
     */
    public final double getMaxWidth() {
        return autoCompletionPopup.getMaxWidth();
    }

    /**
     * Return the property associated with the max width.
     * @return 
     */
    public final DoubleProperty maxWidthProperty() {
        return autoCompletionPopup.maxWidthProperty();
    }
    
    /**
	 * Get the {@link AutoCompletePopup} used by this binding. Note that this gives access to the
	 * internal API and should be used with great care (and in the expectation that things may break in
	 * the future). All relevant methods of the popup are already exposed in this class.
	 * <p/>
	 * The only reason this is exposed is to allow custom skins for the popup.
	 * 
	 * @return the {@link AutoCompletePopup} used by this binding
	 */
    public AutoCompletePopup<T> getAutoCompletionPopup() {
    	return autoCompletionPopup;
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
        selectFirstSuggestion(autoCompletionPopup);
    }

    /**
     * Hide the auto completion targets
     */
    protected void hidePopup(){
        autoCompletionPopup.hide();
    }

    protected void fireAutoCompletion(T completion){
        Event.fireEvent(this, new AutoCompletionEvent<>(completion));
    }


    /***************************************************************************
     *                                                                         *
     * Private methods                                                         *
     *                                                                         *
     **************************************************************************/

    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion(AutoCompletePopup<?> autoCompletionPopup){
        Skin<?> skin = autoCompletionPopup.getSkin();
        if(skin instanceof AutoCompletePopupSkin){
            AutoCompletePopupSkin<?> au = (AutoCompletePopupSkin<?>)skin;
            ListView<?> li = (ListView<?>)au.getNode();
            if(li.getItems() != null && !li.getItems().isEmpty()){
                li.getSelectionModel().select(0);
            }
        }
    }

    /**
     * Occurs when the user text has changed and the suggestions require an update
     * @param userText
     */
    private final void onUserInputChanged(final String userText){
        synchronized (suggestionsTaskLock) {
            if(suggestionsTask != null && suggestionsTask.isRunning()){
                // cancel the current running task
                suggestionsTask.cancel(); 
            }
            // create a new fetcher task
            suggestionsTask = new FetchSuggestionsTask(userText, delay);
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
        private final long delay;

        public FetchSuggestionsTask(String userText, long delay){
            this.userText = userText;
            this.delay = delay;
        }

        @Override
        protected Void call() throws Exception {
            Callback<ISuggestionRequest, Collection<T>> provider = suggestionProvider;
            if(provider != null){
                long startTime = System.currentTimeMillis();
                long sleepTime = startTime + delay - System.currentTimeMillis();
                if (sleepTime > 0 && !isCancelled()) {
                    Thread.sleep(sleepTime);
                }
                if(!isCancelled()){
                    final Collection<T> fetchedSuggestions = provider.call(this);
                    Platform.runLater(() -> {
                        // check whether completionTarget is still valid
                        boolean validNode = completionTarget.getScene() != null
                                            && completionTarget.getScene().getWindow() != null;
                        if(fetchedSuggestions != null && !fetchedSuggestions.isEmpty() && validNode){
                            autoCompletionPopup.getSuggestions().setAll(fetchedSuggestions);
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
        public static final EventType<AutoCompletionEvent<?>> AUTO_COMPLETED 
                = new EventType<>("AUTO_COMPLETED" + UUID.randomUUID().toString()); //$NON-NLS-1$

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
