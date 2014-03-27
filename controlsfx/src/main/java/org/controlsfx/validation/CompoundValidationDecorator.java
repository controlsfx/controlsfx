package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.controlsfx.control.decoration.Decoration;

public class CompoundValidationDecorator implements ValidationDecorator{

	private final Set<ValidationDecorator> decorators = new HashSet<>();
	
	public CompoundValidationDecorator( Collection<ValidationDecorator> decorators ) {
		this.decorators.addAll(decorators); 
	}
	
	public CompoundValidationDecorator( ValidationDecorator... decorators ) {
		this( Arrays.asList(decorators));
	}	
	
	@Override
	public Collection<Decoration> createDecorations(ValidationMessage message) {
		List<Decoration> decorations = new ArrayList<>();
		decorators.stream().forEach( d -> decorations.addAll(d.createDecorations(message)));
		return decorations;
	}
	
	@Override
	public String toString() {
		return "Compound Validation Decorator";
	}
	
}
