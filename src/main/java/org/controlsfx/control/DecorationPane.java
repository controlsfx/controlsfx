package org.controlsfx.control;

import impl.org.controlsfx.skin.DecorationPaneSkin;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class DecorationPane extends Control {
	
	final Node node;

	public DecorationPane( Node node ) {
		this.node = node;
		
	}
    
    @Override protected Skin<?> createDefaultSkin() {
        return new DecorationPaneSkin(this);
    }
    
    public Node getNode() {
		return node;
	}
}
