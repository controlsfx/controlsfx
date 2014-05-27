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

import java.util.Arrays;
import java.util.EnumSet;

import javafx.event.ActionEvent;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog.ActionTrait;
import org.controlsfx.dialog.Dialog.DialogAction;

/**
 * A convenience class that implements the {@link Action} and {@link DialogAction} interfaces and provides
 * a simpler API. It is highly recommended to use this class rather than 
 * implement the {@link Action} or the {@link DialogAction} interfaces directly.
 * 
 * <p>To better understand how to use actions, and where they fit within the
 * JavaFX ecosystem, refer to the {@link Action} class documentation.
 * 
 * @see Action
 * @see DialogAction
 */
public class DefaultDialogAction extends AbstractAction implements DialogAction {
    
    private final EnumSet<ActionTrait> traits;

    /**
     * Creates a dialog action with given text and traits
     * @param text
     * @param traits
     */
    public DefaultDialogAction(String text, ActionTrait... traits) {
        super(text);
        this.traits = (traits == null || traits.length == 0) ? 
                EnumSet.noneOf(ActionTrait.class) : 
                EnumSet.copyOf(Arrays.asList(traits));
    }

    /**
     * Creates a dialog action with given text and common set of traits: CLOSING and DEFAULT
     * @param text
     */
    public DefaultDialogAction(String text) {
        this(text, ActionTrait.CLOSING, ActionTrait.DEFAULT);
    }
    
    
    /** {@inheritDoc} */
    @Override public boolean hasTrait(ActionTrait trait) {
        return traits.contains(trait);
    }

    
    /** {@inheritDoc} */
    @Override public void handle(ActionEvent ae) {
    	DialogAction.super.handle(ae);
    }

}
