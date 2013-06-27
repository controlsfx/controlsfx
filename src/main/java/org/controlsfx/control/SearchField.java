package org.controlsfx.control;

import impl.org.controlsfx.skin.SearchFieldSkin;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A simple control that adds a clear button to a TextField control. Instantiate
 * as you would a {@link TextField}.
 * 
 * @see CustomTextField
 */
public class SearchField extends Control {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final CustomTextField customTextField = new CustomTextField();

    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Instantiates a default SearchField.
     */
    public SearchField() {
        getStyleClass().add("search-field");
    }

    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new SearchFieldSkin(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return SearchField.class.getResource("searchfield.css").toExternalForm();
    }

    /**
     * Returns the wrapped TextField, if the API on the SearchField control
     * itself is not sufficient.
     */
    public TextField getTextField() {
        return customTextField.getTextField();
    }

    /**
     * Delegate method that forwards on to {@link TextField#getPromptText()}.
     */
    public final String getPromptText() {
        return getTextField().getPromptText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#getText()}.
     */
    public final String getText() {
        return getTextField().getText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#promptTextProperty()}.
     */
    public final StringProperty promptTextProperty() {
        return getTextField().promptTextProperty();
    }

    /**
     * Delegate method that forwards on to {@link TextField#setPromptText(String)}.
     */
    public final void setPromptText(String value) {
        getTextField().setPromptText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#setText(String)}.
     */
    public final void setText(String value) {
        getTextField().setText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#textProperty()}.
     */
    public final StringProperty textProperty() {
        return getTextField().textProperty();
    }
}
