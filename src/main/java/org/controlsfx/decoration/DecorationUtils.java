package org.controlsfx.decoration;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.Node;

public class DecorationUtils {

	public final static String DECORATIONS_PROPERTY_KEY = "org.controlsfx.decorations";
	
	private DecorationUtils() {
		// no op
	}
	
	public static final void registerDecoration( Node target, Decoration decoration ) {
		getDecorations(target, true).add(decoration);
	}
	
	public static final void unregisterDecoration( Node target, Decoration decoration ) {
		getDecorations(target, true).remove(decoration);
	}
	
	
	
	public static final ObservableSet<Decoration> getDecorations(Node target, boolean createIfAbsent) {
		@SuppressWarnings("unchecked")
		ObservableSet<Decoration> decorations = 
				(ObservableSet<Decoration>) target.getProperties().get(DECORATIONS_PROPERTY_KEY);
		if (decorations == null && createIfAbsent ) {
			decorations = FXCollections.observableSet();
			target.getProperties().put(DECORATIONS_PROPERTY_KEY, decorations);
		}
		return decorations;
	}
	
	public static final ObservableSet<Decoration> getDecorations(Node target) {
		return getDecorations(target, false);
	}
	
}
