package org.controlsfx.control;

import impl.org.controlsfx.skin.DecorationPaneSkin;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Pane;

public class DecorationPane extends Control {
	
	private final Node node;
	private final Pane overlay = new Pane();

	public DecorationPane( Node node ) {
		this.node = node;
	}
    
    @Override protected Skin<?> createDefaultSkin() {
        return new DecorationPaneSkin(this, overlay);
    }
    
    public Node getNode() {
		return node;
	}
    
    public final ObservableList<Node> getChildren() {
    	return overlay.getChildren();
    }
}
