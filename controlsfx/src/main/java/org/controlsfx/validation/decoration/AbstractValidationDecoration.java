/**
 * Copyright (c) 2014, 2015, ControlsFX
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
package org.controlsfx.validation.decoration;

import java.util.Collection;
import java.util.List;

import javafx.scene.control.Control;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.ValidationSupport;

/**
 * Implements common functionality for validation decorators.
 * This class intended as a base for custom validation decorators   
 * Custom validation decorator should define only two things:
 * how 'validation' and 'required' decorations should be created
 * <br>
 * See {@link GraphicValidationDecoration} or {@link StyleClassValidationDecoration} for examples of such implementations.
 * 
 */
public abstract class AbstractValidationDecoration implements ValidationDecoration {
	
	private static final String VALIDATION_DECORATION = "$org.controlsfx.decoration.vaidation$"; //$NON-NLS-1$
	
	private static boolean isValidationDecoration( Decoration decoration) {
        return decoration != null && decoration.getProperties().get(VALIDATION_DECORATION) == Boolean.TRUE;
    }

	private static void setValidationDecoration( Decoration decoration ) {
        if ( decoration != null ) {
            decoration.getProperties().put(VALIDATION_DECORATION, Boolean.TRUE);
        }
    }

	protected abstract Collection<Decoration> createValidationDecorations(ValidationMessage message);
	protected abstract Collection<Decoration> createRequiredDecorations(Control target);
	
	/**
	 * Removes all validation related decorations from the target
	 * @param target control
	 */
	@Override
	public void removeDecorations(Control target) {
		 List<Decoration> decorations = Decorator.getDecorations(target);
        if ( decorations != null ) {
        	// conversion to array is a trick to prevent concurrent modification exception 
            for ( Decoration d: Decorator.getDecorations(target).toArray(new Decoration[0]) ) {
            	if (isValidationDecoration(d)) Decorator.removeDecoration(target, d);
            }
        }
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.controlsfx.validation.decorator.ValidationDecorator#applyValidationDecoration(org.controlsfx.validation.ValidationMessage)
	 */
	@Override
	public void applyValidationDecoration(ValidationMessage message) {
		createValidationDecorations(message).stream().forEach( d -> decorate( message.getTarget(), d ));
	}

	/*
	 * (non-Javadoc)
	 * @see org.controlsfx.validation.decorator.ValidationDecorator#applyRequiredDecoration(javafx.scene.control.Control)
	 */
	@Override
	public void applyRequiredDecoration(Control target) {
		if ( ValidationSupport.isRequired(target)) { 
			createRequiredDecorations(target).stream().forEach( d -> decorate( target, d ));
		}
	}
	
	private void decorate( Control target, Decoration d ) {
		setValidationDecoration(d); // mark as validation specific decoration
        Decorator.addDecoration(target, d);
	}

}
