/**
 * Copyright (c) 2014, ControlsFX
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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Objects;
import javafx.event.ActionEvent;



/**
 * An action that invokes a method that has been annotated with {@link ActionProxy}. These actions are created via
 * {@link ActionMap#register(java.lang.Object)}, which delegates the actual instantiation to an {@link AnnotatedActionFactory}.
 * 
 * Note that this class maintains a WeakReference to the supplied target object, so the existence of an 
 * AnnotatedAction instance will not prevent the target from being garbage-collected.
 */
public class AnnotatedAction extends Action {

    private final Method method;
    private final WeakReference<Object> target;

    /**
     * Instantiates an action that will call the specified method on the specified target.
     */
    public AnnotatedAction(String text, Method method, Object target) {
        super(text);
        Objects.requireNonNull( method );
        Objects.requireNonNull( target );
        
        setEventHandler(this::handleAction);
        
        this.method = method;
        this.method.setAccessible(true);
        this.target = new WeakReference( target );
    }
    
    /**
     * Returns the target object (the object on which the annotated method will be called).
     * 
     * @return The target object, or null if the target object has been garbage-collected.
     */
    public Object getTarget() {
        return target.get();
    }

    /**
     * Handle the action-event by invoking the annotated method on the target object. If an exception is
     * thrown, then the default implementation of this method will call handleActionException().
     */
    protected void handleAction(ActionEvent ae) {
        try {
            Object actionTarget = getTarget();
            if (actionTarget == null) {
                throw new IllegalStateException( "Action target object is no longer reachable" );
            }
            
            int paramCount =  method.getParameterCount(); 
            if ( paramCount == 0 ) {
                method.invoke(actionTarget);
                
            } else if ( paramCount == 1) {
                method.invoke(actionTarget, ae);
                
            } else if ( paramCount == 2) {
                method.invoke(actionTarget, ae, this);
            }
        } catch (Throwable e) {
            handleActionException( ae, e );
        }
    }
    
    
    /**
     * Called if the annotated method throws an exception when invoked. The default implementation of this method simply prints
     * the stack trace of the specified exception.
     */
    protected void handleActionException( ActionEvent ae, Throwable ex ) {
        ex.printStackTrace();
    }
    
    
    /**
     * Overridden to return the text of this action.
     */
    @Override
    public String toString() {
        return getText();
    }
}
