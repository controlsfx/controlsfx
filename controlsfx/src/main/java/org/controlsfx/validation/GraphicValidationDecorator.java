package org.controlsfx.validation;

import java.util.Arrays;
import java.util.Collection;

import javafx.geometry.Pos;
import javafx.scene.Node;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.GraphicDecoration;

public abstract class GraphicValidationDecorator implements ValidationDecorator {

	private Pos decorationPosition = Pos.BOTTOM_LEFT;
	
	public Pos getDecorationPosition() {
		return decorationPosition;
	}
	
	public void setDecorationPosition(Pos decorationPosition) {
		this.decorationPosition = decorationPosition;
	}
	
	@Override
	public final Collection<Decoration> createDecorations(ValidationMessage message) {
		return Arrays.asList(new GraphicDecoration(createDecorationNode(message),getDecorationPosition()));
	}

	protected abstract Node createDecorationNode(ValidationMessage message);

}
