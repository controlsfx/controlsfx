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

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import org.controlsfx.glyphfont.Glyph;

import java.lang.reflect.Method;

/**
 * The default {@link AnnotatedActionFactory} to be used when no alternative has been specified. This class creates
 * instances of {@link AnnotatedAction}.
 */
public class DefaultActionFactory implements AnnotatedActionFactory {

    /**
     * Create an {@link AnnotatedAction}. This method is called by {@link ActionMap#register(java.lang.Object)}.
     * 
     * @param annotation The annotation specified on the method.
     * @param method The method to be invoked when an action is fired.
     * @param target The target object on which the method will be invoked.
     * @return An {@link AnnotatedAction} instance.
     */
    @Override
    public AnnotatedAction createAction( ActionProxy annotation, Method method, Object target ) {
        AnnotatedAction action;
        if ( method.isAnnotationPresent(ActionCheck.class)) {
            action = new AnnotatedCheckAction(annotation.text(), method, target);
        } else {
            action = new AnnotatedAction(annotation.text(), method, target);
        }
        
        configureAction( annotation, action );
        
        return action;
    }
    
    
    /**
     * Configures the newly-created action before it is returned to {@link ActionMap}. Subclasses can override this method
     * to change configuration behavior.
     * 
     * @param annotation The annotation specified on the method.
     * @param action The newly-created action.
     */
    protected void configureAction( ActionProxy annotation, AnnotatedAction action ) {
        Node graphic = resolveGraphic(annotation);
        action.setGraphic(graphic);
        
        // set long text / tooltip
        String longText = annotation.longText().trim();
        if ( graphic != null ) {
            action.setLongText(longText);
        }
        
        // set accelerator
        String acceleratorText = annotation.accelerator().trim();
        if (!acceleratorText.isEmpty()) {
            action.setAccelerator(KeyCombination.keyCombination(acceleratorText));
        }

    }
    
    
    /**
     * Resolve the graphical representation of this action. The default implementation of this method implements the protocol described
     * in {@link ActionProxy#graphic()}, but subclasses can override this method to provide alternative behavior.
     * 
     * @param annotation The annotation specified on the method.
     * @return A JavaFX Node for the graphic associated with this action.
     */
    protected Node resolveGraphic( ActionProxy annotation ) {
        String graphicDef = annotation.graphic().trim();
        if ( !graphicDef.isEmpty()) {
            
            String[] def = graphicDef.split("\\>");  // cannot use ':' because it used in urls //$NON-NLS-1$
            if ( def.length == 1 ) return new ImageView(new Image(def[0]));
            switch (def[0]) {
               case "font"    : return Glyph.create(def[1]);   //$NON-NLS-1$
               case "image"   : return new ImageView(new Image(def[1])); //$NON-NLS-1$
               default: throw new IllegalArgumentException( String.format("Unknown ActionProxy graphic protocol: %s", def[0])); //$NON-NLS-1$
            }
        }
        return null;
    }
    
}
