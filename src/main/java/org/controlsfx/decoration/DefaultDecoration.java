package org.controlsfx.decoration;

import javafx.geometry.Pos;
import javafx.scene.Node;

public class DefaultDecoration implements Decoration {
	
	private final Node node;
	private final Pos pos;

	public DefaultDecoration( Node decoration, Pos position ) {
		this.node = decoration;
		this.pos = position;
	}
	
	public DefaultDecoration( Node decoration ) {
		this( decoration, Pos.TOP_LEFT);
	}

	@Override
	public Node getNode() {
		return node;
	}
	
	@Override
	public Pos getPosition() {
		return pos;
	}
	
}
