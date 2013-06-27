package org.controlsfx.control;

import impl.org.controlsfx.skin.CustomTextFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

/**
 * A base class for people wanting to customise a {@link TextField} to contain nodes
 * inside the text field itself, without being on top of the users typed-in text.
 * 
 * @see SearchField
 */
public class CustomTextField extends Control {

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
     * Instantiates a default CustomTextField.
     */
    public CustomTextField() {
        getStyleClass().add("custom-text-field");
    }

    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- left
    private ObjectProperty<Node> left = new SimpleObjectProperty<Node>(this, "left");
    
    /**
     * Property representing the {@link Node} that is placed on the left of
     * the text field.
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }
    
    public final Node getLeft() {
        return left.get();
    }
    
    public final void setLeft(Node value) {
        left.set(value);
    }
    
    
    // --- right
    private ObjectProperty<Node> right = new SimpleObjectProperty<Node>(this, "right");
    
    /**
     * Property representing the {@link Node} that is placed on the right of
     * the text field.
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }
    
    public final Node getRight() {
        return right.get();
    }
    
    public final void setRight(Node value) {
        right.set(value);
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
        return new CustomTextFieldSkin(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return CustomTextField.class.getResource("customtextfield.css").toExternalForm();
    }

    /**
     * Returns the wrapped TextField, if the API on this control itself is not 
     * sufficient.
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
