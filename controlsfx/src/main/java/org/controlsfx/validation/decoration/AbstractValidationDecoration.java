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
 * <br/>
 * See {@link GraphicValidationDecoration} or {@link StyleClassValidationDecoration} for examples of such implementations.
 * 
 */
public abstract class AbstractValidationDecoration implements ValidationDecoration {
	
	private static final String VALIDATION_DECORATION = "$org.controlsfx.decoration.vaidation$";
	
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
