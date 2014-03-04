package impl.org.controlsfx.autocompletion;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

import org.controlsfx.control.textfield.AutoCompletionBinding;

/**
 * Represents a binding between a text field and a auto-completion popup
 *
 * @param <T>
 */
public class AutoCompletionTextFieldBinding<T>  extends AutoCompletionBinding<T>{

    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/
    
    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override public String toString(T t) {
                return t == null ? null : t.toString();
            }
            @SuppressWarnings("unchecked")
			@Override public T fromString(String string) {
                return (T) string;
            }
        };
    }

    /***************************************************************************
     *                                                                         *
     * Private fields                                                          *
     *                                                                         *
     **************************************************************************/
	
    /**
     * String converter to be used to convert suggestions to strings.
     */
	private StringConverter<T> converter;

	
    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a new auto-completion binding between the given textField 
     * and the given suggestion provider.
     * 
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextFieldBinding(final TextField textField,
            Callback<ISuggestionRequest, Collection<T>> suggestionProvider) {
    	
        this(textField, suggestionProvider, AutoCompletionTextFieldBinding
    			.<T>defaultStringConverter());
    }

    /**
     * Creates a new auto-completion binding between the given textField 
     * and the given suggestion provider.
     * 
     * @param textField
     * @param suggestionProvider
     */
    public AutoCompletionTextFieldBinding(final TextField textField,
            Callback<ISuggestionRequest, Collection<T>> suggestionProvider,
            final StringConverter<T> converter) {
    	
        super(textField, suggestionProvider, converter);
        this.converter = converter; 

        getCompletionTarget().textProperty().addListener(textChangeListener);
        getCompletionTarget().focusedProperty().addListener(focusChangedListener);
    }


    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override public TextField getCompletionTarget(){
        return (TextField)super.getCompletionTarget();
    }

    /** {@inheritDoc} */
    @Override public void dispose(){
        getCompletionTarget().textProperty().removeListener(textChangeListener);
        getCompletionTarget().focusedProperty().removeListener(focusChangedListener);
    }

    /** {@inheritDoc} */
    @Override protected void completeUserInput(T completion){
    	String newText = converter.toString(completion);     	
        getCompletionTarget().setText(newText);
        getCompletionTarget().positionCaret(newText.length());
    }


    /***************************************************************************
     *                                                                         *
     * Event Listeners                                                         *
     *                                                                         *
     **************************************************************************/


    private final ChangeListener<String> textChangeListener = new ChangeListener<String>() {
        @Override public void changed(ObservableValue<? extends String> obs, String oldText, String newText) {
            setUserInput(newText);
        }
    };

    private final ChangeListener<Boolean> focusChangedListener = new ChangeListener<Boolean>() {
        @Override public void changed(ObservableValue<? extends Boolean> obs, Boolean oldFocused, Boolean newFocused) {
            if(newFocused == false)
                hidePopup();
        }
    };
}
