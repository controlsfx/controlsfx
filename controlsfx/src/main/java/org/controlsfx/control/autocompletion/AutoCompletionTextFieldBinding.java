package org.controlsfx.control.autocompletion;

import java.util.Collection;

import javax.xml.bind.Binder;

import org.controlsfx.control.autocompletion.AutoCompletePopup.SuggestionChoosenEvent;
import org.controlsfx.control.autocompletion.AutoCompletionBinding.ISuggestionRequest;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.util.Callback;

/**
 * Represents a binding between a text field and a auto-completion popup
 *
 * @param <T>
 */
public class AutoCompletionTextFieldBinding<T>  extends AutoCompletionBinding<T>{

    /***************************************************************************
     *                                                                         *
     * Static methods                                                          *
     *                                                                         *
     **************************************************************************/

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


    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Create a new auto-completion binding between the given textField and the given suggestion provider
     * 
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextFieldBinding(final TextField textField, Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        this(textField, new AutoCompletePopup<T>(), suggestionProvider);
    }


    /**
     * Create a new auto-completion binding between the given textField and auto-completion controller
     * 
     * @param textField
     * @param completionController
     */
    public AutoCompletionTextFieldBinding(
            final TextField textField,
            final AutoCompletePopup<T> autoCompletionPopup,
            Callback<ISuggestionRequest, Collection<T>> suggestionProvider){
        super(textField, autoCompletionPopup, suggestionProvider);

        bind();
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override
    public TextField getCompletionTarget(){
        return (TextField)super.getCompletionTarget();
    }


    /**
     * Bind the TextField
     */
    public void bind(){
        getCompletionTarget().textProperty().addListener(textChangeListener);
    }

    /**
     * Remove the binding
     */
    public void unbind(){
        getCompletionTarget().textProperty().removeListener(textChangeListener);
    }



    /** {@inheritDoc} */
    @Override
    protected void completeUserInput(T completion){
        String newText = completion.toString(); // TODO Handle generic parameter better

        getCompletionTarget().setText(newText);
        getCompletionTarget().positionCaret(newText.length());
    }


    /***************************************************************************
     *                                                                         *
     * Event Listeners                                                         *
     *                                                                         *
     **************************************************************************/


    private final ChangeListener<String> textChangeListener = new ChangeListener<String>() {
        @Override
        public void changed(ObservableValue<? extends String> obs, String oldText, String newText) {
            setUserInput(newText);
        }
    };
}
