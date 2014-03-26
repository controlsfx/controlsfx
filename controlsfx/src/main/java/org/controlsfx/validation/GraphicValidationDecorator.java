package org.controlsfx.validation;

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
	public final Decoration createDecoration(ValidationMessage message) {
		return new GraphicDecoration(createDecorationNode(message),getDecorationPosition());
	}

	protected abstract Node createDecorationNode(ValidationMessage message);

}
