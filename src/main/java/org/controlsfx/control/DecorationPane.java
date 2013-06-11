package org.controlsfx.control;

import impl.org.controlsfx.skin.DecorationPaneSkin;
import javafx.scene.Node;
import javafx.scene.control.Control;

public class DecorationPane extends Control {
	
	private final Node base;
	
	public DecorationPane( Node base ) {
		this.base = base;
	}

    @Override protected DecorationPaneSkin createDefaultSkin() {
        return new DecorationPaneSkin(this, base);
    }
    
}
