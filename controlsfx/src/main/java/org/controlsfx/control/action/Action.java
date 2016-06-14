/**
 * Copyright (c) 2013, 2015, ControlsFX
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
package org.controlsfx.control.action;

import impl.org.controlsfx.i18n.Localization;
import impl.org.controlsfx.i18n.SimpleLocalizedStringProperty;

import java.util.function.Consumer;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;

/**
 * A base class for Action API. 
 * 
 * <h3>What is an Action?</h3>
 * An action in JavaFX can be used to separate functionality and state from a 
 * control. For example, if you have two or more controls that perform the same 
 * function (e.g. one in a {@link Menu} and another on a toolbar), consider 
 * using an Action object to implement the function. An Action object provides 
 * centralized handling of the state of action-event-firing components such as 
 * buttons, menu items, etc. The state that an action can handle includes text, 
 * graphic, long text (i.e. tooltip text), and disabled.
 */
public class Action implements EventHandler<ActionEvent> {
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private boolean locked = false;
    
    private Consumer<ActionEvent> eventHandler;
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    public Action(@NamedArg("text") String text) {
        this(text, null);
    }
    
    public Action(Consumer<ActionEvent> eventHandler) {
        this("", eventHandler); //$NON-NLS-1$
    }
   
    /**
     * Creates a new AbstractAction instance with the given String set as the 
     * {@link #textProperty() text} value, as well as the {@code Consumer<ActionEvent>}
     * set to be called when the action event is fired.
     *  
     * @param text The string to display in the text property of controls such
     *      as {@link Button#textProperty() Button}.
     * @param eventHandler This will be called when the ActionEvent is fired.
     */
    public Action(@NamedArg("text") String text, Consumer<ActionEvent> eventHandler) {
        setText(text);
        setEventHandler(eventHandler);
        getStyleClass().add( "action" ); // this class will be added to all bound controls
    }
    
    protected void lock() {
    	locked = true;
    }
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- style
    /**
     * A string representation of the CSS style associated with this
     * Action instance and passed to related UI controls. 
     * This is analogous to the "style" attribute of an
     * HTML element. Note that, like the HTML style attribute, this
     * variable contains style properties and values and not the
     * selector portion of a style rule.
     * <p>
     * Parsing this style might not be supported on some limited
     * platforms. It is recommended to use a standalone CSS file instead.
     */
    private StringProperty style;
    public final void setStyle(String value) { styleProperty().set(value); }
    public final String getStyle() { return style == null ? "" : style.get(); } //$NON-NLS-1$
    public final StringProperty styleProperty() {
        if (style == null) {
            style = new SimpleStringProperty(this, "style") { //$NON-NLS-1$
            	@Override
            	public void set(String style) {
            		if (locked) throw new UnsupportedOperationException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
            		super.set(style);
            	}
            };
        }
        return style;
    }


    // --- Style class
    private final ObservableList<String> styleClass = FXCollections.observableArrayList();
    /**
     * A list of String identifiers which can be used to logically group
     * Nodes, specifically for an external style engine. This variable is
     * analogous to the "class" attribute on an HTML element and, as such,
     * each element of the list is a style class to which this Node belongs.
     *
     * @see <a href="http://www.w3.org/TR/css3-selectors/#class-html">CSS3 class selectors</a>
     */
    public ObservableList<String> getStyleClass() {
        return styleClass;
    }    
    
    
    // --- selected 
    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(this, "selected") { //$NON-NLS-1$
    	@Override public void set(boolean selected) {
    		if (locked) throw new UnsupportedOperationException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(selected);
    	}
    };
    
    /**
     * Represents action's selected state. 
     * Usually bound to selected state of components such as Toggle Buttons, CheckBOxes  etc
     *
     * @return An observable {@link BooleanProperty} that represents the current
     *      selected state, and which can be observed for changes.
     */
    public final BooleanProperty selectedProperty() {
    	return selectedProperty;
    }
    
    /**
     * Selected state of the Action.
     * @return The selected state of this action.
     */
    public final boolean isSelected() {
    	return selectedProperty.get();
    }
    
    /**
     * Sets selected state of the Action
     * @param selected
     */
    public final void setSelected( boolean selected ) {
    	selectedProperty.set(selected);
    }
    
    
    // --- text
    private final StringProperty textProperty = new SimpleLocalizedStringProperty(this, "text"){ //$NON-NLS-1$
    	@Override public void set(String value) {
    		if ( locked ) throw new RuntimeException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(value);
    	}
    };
    
    /**
     * The text to show to the user.
     * 
     * @return An observable {@link StringProperty} that represents the current
     *      text for this property, and which can be observed for changes.
     */
    public final StringProperty textProperty() {
        return textProperty;
    }
    
    /**
     * 
     * @return the text of the Action.
     */
    public final String getText() {
        return textProperty.get();
    }

    /**
     * Sets the text of the Action.
     * @param value 
     */
    public final void setText(String value) {
        textProperty.set(value);
    }
    
    
    // --- disabled
    private final BooleanProperty disabledProperty = new SimpleBooleanProperty(this, "disabled"){ //$NON-NLS-1$
        @Override public void set(boolean value) {
    		if ( locked ) throw new RuntimeException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(value);
    	}
    };
    
    /**
     * This represents whether the action should be available to the end user,
     * or whether it should appeared 'grayed out'.
     * 
     * @return An observable {@link BooleanProperty} that represents the current
     *      disabled state for this property, and which can be observed for 
     *      changes.
     */
    public final BooleanProperty disabledProperty() {
        return disabledProperty;
    }
    
    /**
     * 
     * @return whether the action is available to the end user,
     * or whether it should appeared 'grayed out'.
     */
    public final boolean isDisabled() {
        return disabledProperty.get();
    }
    
    /**
     * Sets whether the action should be available to the end user,
     * or whether it should appeared 'grayed out'.
     * @param value 
     */
    public final void setDisabled(boolean value) {
        disabledProperty.set(value);
    }

    
    // --- longText
    private final StringProperty longTextProperty = new SimpleLocalizedStringProperty(this, "longText"){ //$NON-NLS-1$
        @Override public void set(String value) {
    		if ( locked ) throw new RuntimeException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(value);

    	}
    };
    
    /**
     * The longer form of the text to show to the user (e.g. on a 
     * {@link Button}, it is usually a tooltip that should be shown to the user 
     * if their mouse hovers over this action).
     * 
     * @return An observable {@link StringProperty} that represents the current
     *      long text for this property, and which can be observed for changes.
     */
    public final StringProperty longTextProperty() {
        return longTextProperty;
    }
    
    /**
     * @see #longTextProperty() 
     * @return The longer form of the text to show to the user
     */
    public final String getLongText() {
        return Localization.localize(longTextProperty.get());
    }
    
    /**
     * Sets the longer form of the text to show to the user
     * @param value 
     * @see #longTextProperty() 
     */
    public final void setLongText(String value) {
        longTextProperty.set(value);
    }
    
    
    // --- graphic
    private final ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>(this, "graphic"){ //$NON-NLS-1$
        @Override public void set(Node value) {
    		if ( locked ) throw new RuntimeException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(value);

    	}
    };
    
    /**
     * The graphic that should be shown to the user in relation to this action.
     * 
     * @return An observable {@link ObjectProperty} that represents the current
     *      graphic for this property, and which can be observed for changes.
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphicProperty;
    }
    
    /**
     * 
     * @return The graphic that should be shown to the user in relation to this action.
     */
    public final Node getGraphic() {
        return graphicProperty.get();
    }
    
    /**
     * Sets the graphic that should be shown to the user in relation to this action.
     * @param value 
     */
    public final void setGraphic(Node value) {
        graphicProperty.set(value);
    }
    
    
    // --- accelerator
    private final ObjectProperty<KeyCombination> acceleratorProperty = new SimpleObjectProperty<KeyCombination>(this, "accelerator"){ //$NON-NLS-1$
        @Override public void set(KeyCombination value) {
    		if ( locked ) throw new RuntimeException("The action is immutable, property change support is disabled."); //$NON-NLS-1$
    		super.set(value);

    	}
    };
    
    /**
     * The accelerator {@link KeyCombination} that should be used for this action,
     * if it is used in an applicable UI control (most notably {@link MenuItem}).
     * 
     * @return An observable {@link ObjectProperty} that represents the current
     *      accelerator for this property, and which can be observed for changes.
     */
    public final ObjectProperty<KeyCombination> acceleratorProperty() {
        return acceleratorProperty;
    }
    
    /**
     * 
     * @return The accelerator {@link KeyCombination} that should be used for this action,
     * if it is used in an applicable UI control
     */
    public final KeyCombination getAccelerator() {
        return acceleratorProperty.get();
    }
    
    /**
     * Sets the accelerator {@link KeyCombination} that should be used for this action,
     * if it is used in an applicable UI control
     * @param value 
     */
    public final void setAccelerator(KeyCombination value) {
        acceleratorProperty.set(value);
    }
    
    
    // --- properties
    private ObservableMap<Object, Object> props;
    
    /**
     * Returns an observable map of properties on this Action for use primarily
     * by application developers.
     *
     * @return An observable map of properties on this Action for use primarily
     * by application developers
     */
    public final synchronized ObservableMap<Object, Object> getProperties() {
    	if ( props == null ) props = FXCollections.observableHashMap();
    	return props;
    }
    
    protected Consumer<ActionEvent> getEventHandler() {
		return eventHandler;
	}
    
    protected void setEventHandler(Consumer<ActionEvent> eventHandler) {
		this.eventHandler = eventHandler;
	}
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /** 
     * Defers to the {@code Consumer<ActionEvent>} passed in to the Action constructor.
     */
    @Override public final void handle(ActionEvent event) {
        if (eventHandler != null && !isDisabled()) {
            eventHandler.accept(event);
        }
    }
    
//    public void bind(ButtonBase button) {
//        ActionUtils.configureButton(this, button);
//    }
//    
//    public void bind(MenuItem menuItem) {
//        ActionUtils.configureMenuItem(this, menuItem);
//    }
}