package org.controlsfx.control.autocompletion;

import java.util.Collection;

import javax.xml.bind.Binder;

import org.controlsfx.control.autocompletion.AutoCompletePopup.SuggestionChoosenEvent;
import org.controlsfx.control.autocompletion.AutoCompletionController.ISuggestionRequest;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/**
 * Represents a binding between a text field and a auto-completion popup
 *
 * @param <T>
 */
public class AutoCompletionTextFieldBinding<T> {

    private final TextField textField;
    private final AutoCompletionController<T> completionController;
    private final AutoCompletePopup<T> popup;

    /**
     * Create a new auto-completion binding between the given textField and the given suggestion provider
     * 
     * @param textField
     * @param suggestionProvider
     * @return
     */
    public static <T> AutoCompletionTextFieldBinding<T> createBinding(TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        return new AutoCompletionTextFieldBinding<>(textField, suggestionProvider);
    }


    /**
     * Create a new auto-completion binding between the given textField and the given suggestion provider
     * 
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextFieldBinding(final TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this(textField, new AutoCompletePopup<T>(), suggestionProvider);
    }


    private AutoCompletionTextFieldBinding(
            final TextField textField,
            final AutoCompletePopup<T> autoCompletionPopup,
            Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this(textField, new AutoCompletionController<>(autoCompletionPopup, suggestionProvider, new Callback<Void,Void>() {
            @Override
            public Void call(Void empty) {
                autoCompletionPopup.showBelowNode(textField);
                return null;
            }
        }));
    }

    /**
     * Create a new auto-completion binding between the given textField and auto-completion controller
     * 
     * @param textField
     * @param completionController
     */
    protected AutoCompletionTextFieldBinding(TextField textField, AutoCompletionController<T> completionController){
        this.textField = textField;
        this.completionController = completionController;
        this.popup = completionController.getPopup();
        bind();
    }

    /**
     * Bind the TextField
     */
    public void bind(){
        textField.textProperty().addListener(textChangeListener);
        popup.setOnSuggestionChoosen(new EventHandler<AutoCompletePopup.SuggestionChoosenEvent<T>>() {
            @Override
            public void handle(SuggestionChoosenEvent<T> sce) {
                completeUserInput(sce.getSelectedSuggestion());
            }
        });
    }

    /**
     * Complete the current user-input with the provided completion
     * @param completion
     */
    protected void completeUserInput(T completion){
        String newText = completion.toString(); // TODO Handle generic parameter better

        textField.setText(newText);
        textField.positionCaret(newText.length());
    }

    /**
     * Remove the binding
     */
    public void unbind(){
        textField.textProperty().removeListener(textChangeListener);
    }

    private final ChangeListener<String> textChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> obs, String oldText, String newText) {
            completionController.setUserInput(newText);
        }
    };
}
