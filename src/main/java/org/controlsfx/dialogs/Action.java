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
 package org.controlsfx.dialogs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;

/**
 * Common interface for dialog actions, where Actions are converted into buttons 
 * in the dialogs button bar. It is highly recommended that rather than 
 * implement this interface that developers instead use {@link AbstractAction}.
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
     * This {@link Tooltip} that should be shown to the user if their mouse 
     * hovers over this action.
     * 
     * @return An observable {@link ObjectProperty} that represents the current
     *      Tooltip for this property, and which can be observed for changes.
     */
    public ObjectProperty<Tooltip> tooltipProperty();
    
    /**
     * This graphic that should be shown to the user in relation to this action.
     * 
     * @return An observable {@link ObjectProperty} that represents the current
     *      graphic for this property, and which can be observed for changes.
     */
    public ObjectProperty<Node> graphicProperty();

    /**
     * This method is called when the user selects this action. 
     * 
     * @param ae The action context.
     */
    public void execute(ActionEvent ae);
}