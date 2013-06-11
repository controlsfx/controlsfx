package org.controlsfx.decoration;

import javafx.scene.Node;

public class DefaultDecoration implements Decoration {
	
	private Node node;

	public DefaultDecoration( Node decoration) {
		this.node = decoration;
	}

	@Override
	public Node getNode() {
		return node;
	}
}
