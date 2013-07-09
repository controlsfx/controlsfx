/**
 * Copyright (c) 2013, ControlsFX
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;

/**
 * Common interface for dialog actions, where Actions are converted into buttons 
 * in the dialogs button bar. It is highly recommended that rather than 
 * implement this interface that developers instead use {@link AbstractAction}.
 * 
 * <h3>What is an Action?</h3>
 * An action in JavaFX can be used to separate functionality and state from a 
 * control. For example, if you have two or more controls that perform the same 
 * function (e.g. one in a {@link Menu} and another on a toolbar), consider 
 * using an Action object to implement the function. An Action object provides 
 * centralized handling of the state of action-event-firing components such as 
 * buttons, menu items, etc. The state that an action can handle includes text, 
 * graphic, long text (i.e. tooltip text), and disabled.
 * 
 * @see AbstractAction
 */
public interface Action {

    /**
     * The text to show to the user.
     * 
     * @return An observable {@link StringProperty} that represents the current
     *      text for this property, and which can be observed for changes.
     */
    public StringProperty textProperty();
    
    /**
     * This represents whether the action should be available to the end user,
     * or whether it should appeared 'grayed out'.
     * 
     * @return An observable {@link BooleanProperty} that represents the current
     *      disabled state for this property, and which can be observed for 
     *      changes.
     */
    public BooleanProperty disabledProperty();

    /**
     * The longer form of the text to show to the user (e.g. on a 
     * {@link Button}, it is usually a tooltip that should be shown to the user 
     * if their mouse hovers over this action).
     * 
     * @return An observable {@link StringProperty} that represents the current
     *      long text for this property, and which can be observed for changes.
     */
    public StringProperty longTextProperty();
    
    /**
     * This graphic that should be shown to the user in relation to this action.
     * 
     * @return An observable {@link ObjectProperty} that represents the current
     *      graphic for this property, and which can be observed for changes.
     */
    public ObjectProperty<Node> graphicProperty();
    
    /**
     * Returns an observable map of properties on this Action for use primarily
     * by application developers.
     *
     * @return An observable map of properties on this Action for use primarily
     * by application developers
     */
    public ObservableMap<Object, Object> getProperties();
    
    /**
     * This method is called when the user selects this action. 
     * 
     * @param ae The action context.
     */
    public void execute(ActionEvent ae);
}