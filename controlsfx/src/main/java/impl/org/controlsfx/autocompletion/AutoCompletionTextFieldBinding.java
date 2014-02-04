package impl.org.controlsfx.autocompletion;

import java.util.Collection;

import javax.xml.bind.Binder;

import org.controlsfx.control.AutoCompletePopup;
import org.controlsfx.control.AutoCompletionBinding;
import org.controlsfx.control.AutoCompletePopup.SuggestionChoosenEvent;
import org.controlsfx.control.AutoCompletionBinding.ISuggestionRequest;

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


    /** {@inheritDoc} */
    @Override
    public void bind(){
        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }

    /** {@inheritDoc} */
    @Override
    public void unbind(){
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
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

    private final ChangeListener<Boolean> focusChangedListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> obs, Boolean oldFocused, Boolean newFocused) {
            System.out.println("focused: " + newFocused);
            if(newFocused == false)
                hidePopup();
        }
    };
}
