package org.controlsfx.control;

import impl.org.controlsfx.skin.DecorationPaneSkin;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;

public class DecorationPane extends Control {
	
	private final Node node;
	private final Region overlay = new Region();

	public DecorationPane( Node node ) {
		this.node = node;
	}
    
    @Override protected Skin<?> createDefaultSkin() {
        return new DecorationPaneSkin(this, overlay);
    }
    
    public Node getNode() {
		return node;
	}
}
