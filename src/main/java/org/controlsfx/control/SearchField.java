package org.controlsfx.control;

import impl.org.controlsfx.skin.SearchFieldSkin;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A simple control that adds a clear button to a TextField control. Instantiate
 * as you would a {@link TextField}.
 */
public class SearchField extends Control {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final TextField textField = new TextField();

    
    
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
        return textField;
    }

    /**
     * Delegate method that forwards on to {@link TextField#getPromptText()}.
     */
    public final String getPromptText() {
        return textField.getPromptText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#getText()}.
     */
    public final String getText() {
        return textField.getText();
    }

    /**
     * Delegate method that forwards on to {@link TextField#promptTextProperty()}.
     */
    public final StringProperty promptTextProperty() {
        return textField.promptTextProperty();
    }

    /**
     * Delegate method that forwards on to {@link TextField#setPromptText(String)}.
     */
    public final void setPromptText(String value) {
        textField.setPromptText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#setText(String)}.
     */
    public final void setText(String value) {
        textField.setText(value);
    }

    /**
     * Delegate method that forwards on to {@link TextField#textProperty()}.
     */
    public final StringProperty textProperty() {
        return textField.textProperty();
    }


}
