/**
 * Copyright (c) 2013, 2014 ControlsFX
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
package org.controlsfx.dialog;

import java.util.function.Consumer;

import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.Action;

/**
 * A specialized dialog {@link Action} that knows how to properly return the
 * result back to the owner {@link Dialog} such that it can be returned via
 * {@link Dialog#show()). To be even more useful, DialogAction instances can
 * be 
 * 
 * <p>To better understand how to use actions, and where they fit within the
 * JavaFX ecosystem, refer to the {@link Action} class documentation.
 * 
 * @see Action
 */
@Deprecated
public class DialogAction extends Action {

	private final boolean _cancel;
	private final boolean _closing;
	private final boolean _default;
    
    /**
     * Creates a dialog action with the given text and several required attributes.
     * 
     * @param text The string to display in the text property of controls such
     *      as {@link Button#textProperty() Button}.
     * @param cancelAction
     * @param closingAction
     * @param defaultAction
     * @param userEventHandler custom action event handler
     */
    public DialogAction(@NamedArg("text") String text, 
                        @NamedArg("buttonType") ButtonType buttonType, 
                        @NamedArg("cancelAction") boolean cancelAction, 
                        @NamedArg("closingAction") boolean closingAction, 
                        @NamedArg("defaultAction") boolean defaultAction,
                        @NamedArg("eventHandler") Consumer<ActionEvent> userEventHandler) {
        super(text);
        setEventHandler( ae -> {
        	
        	if ( userEventHandler != null ) userEventHandler.accept(ae);

        	// continue with dialog related operations
        	if (ae.getSource() instanceof Dialog ) {
                ((Dialog) ae.getSource()).setResult(this);
            }
        	
        });

        _cancel = cancelAction;
        _closing = closingAction;
        _default = defaultAction;
        
        if ( buttonType != null ) {        
        	ButtonBar.setType(this, buttonType);
        }
    }
    
    /**
     * Creates a dialog action with the given text and several required attributes.
     * 
     * @param text The string to display in the text property of controls such
     *      as {@link Button#textProperty() Button}.
     * @param cancelAction
     * @param closingAction
     * @param defaultAction
     */
    public DialogAction(@NamedArg("text") String text, 
            @NamedArg("buttonType") ButtonType buttonType, 
            @NamedArg("cancelAction") boolean cancelAction, 
            @NamedArg("closingAction") boolean closingAction, 
            @NamedArg("defaultAction") boolean defaultAction ) {
    	this( text, buttonType, cancelAction, closingAction, defaultAction, null);
    }
    
    /**
     * Creates a dialog action with the given text 
     * @param text The string to display in the text property of controls such
     *      as {@link Button#textProperty() Button}
     * @param buttonType type assigned to a related dialog button 
     * @param userEventHandler custom action event handler
     */
    public DialogAction(@NamedArg("text") String text, 
                        @NamedArg("buttonType") ButtonType buttonType, 
                        @NamedArg("eventHandler") Consumer<ActionEvent> userEventHandler) {
        this(text, buttonType, true, true, true, userEventHandler);
    }
    
    public DialogAction(@NamedArg("text") String text, 
                        @NamedArg("buttonType") ButtonType buttonType) {
    	this( text, buttonType, null);
    }

    /**
     * Creates a dialog action with given text and common set of attributes: Closing and Default
     * @param text
     * @param userEventHandler custom action event handler
     */
    public DialogAction(@NamedArg("text") String text,
    		            @NamedArg("eventHandler") Consumer<ActionEvent> userEventHandler) {
        this(text, null, false, true, true, userEventHandler);
    }


    /**
     * Creates a dialog action with given text and common set of attributes: Closing and Default
     * @param text
     */    
    public DialogAction(@NamedArg("text") String text) {
    	this(text, (Consumer<ActionEvent>)null);
    }
    
    /**
     * Is dialog cancel action
     * @return true if action should cancel the dialog
     */
    public boolean isCancel() { return _cancel; }

    /**
     * Is dialog closing action 
     * @return true if action should close the dialog
     */
    public boolean isClosing() { return _closing; }

    /**
     * Is dialog default action 
     * @return true if action is default in the dialog
     */
    public boolean isDefault() { return _default; }

}
