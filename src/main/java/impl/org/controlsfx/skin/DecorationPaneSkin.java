package impl.org.controlsfx.skin;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.DecorationPane;
import org.controlsfx.decoration.Decoration;
import org.controlsfx.decoration.DecorationUtils;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class DecorationPaneSkin  extends BehaviorSkinBase<DecorationPane, BehaviorBase<DecorationPane>> {
	
	private final StackPane stackPane = new StackPane();
	private final Node base;
	
	public DecorationPaneSkin(DecorationPane control, Node base) {
		super(control, new BehaviorBase<>(control));
		this.base = base;
		getChildren().add(stackPane);
		
	}
	
	@Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
    	stackPane.getChildren().clear();
    	stackPane.resizeRelocate(x, y, w, h);
    	stackPane.getChildren().add(base);
    	showDecorations(base);
    }
    
    
    private void showDecorations(Node root) {
    	
    	//TODO Respond for component bound changes
    	//TODO Respond for component visibility changes
    	//TODO Respond to  component decoration changes.
    	
    	
    	// show root decoration if found
    	@SuppressWarnings("unchecked")
		ObservableList<Decoration> decorations = 
			(ObservableList<Decoration>) root.getProperties().get(DecorationUtils.DECORATIONS_PROPERTY_KEY);
		if ( decorations != null ) {
			for( Decoration decoration: decorations ) {
				Bounds bounds = getBounds( root );
				Node dnode = decoration.getNode();
				stackPane.getChildren().add(dnode);
				StackPane.setAlignment(dnode, Pos.TOP_LEFT); // TODO support for all positions.
				StackPane.setMargin(dnode, new Insets( bounds.getMinY(),0,0,bounds.getMinX()) );
			}
		}
    	
		// recursively show decorations for children
    	if ( root instanceof Parent ) {
    		for ( Node n: ((Parent)root).getChildrenUnmodifiable()) {
    			showDecorations(n);
    		}
    	}
    }
    
    
    private Bounds getBounds( Node node ) {

    	if ( node == null ) return null;
//    	System.out.println(node.getClass());
    	Node parent = node.getParent();
    	Bounds bounds = null;
//    	System.out.println("----------------------------------------");
    	while ( parent != null && parent != stackPane) {
//    	   System.out.println("parent: " + parent.getClass());
    	   bounds = bounds == null? node.getBoundsInParent(): parent.localToParent(bounds);
//   		System.out.println(bounds);
    	   parent = parent.getParent();
    	}
//    	System.out.println("Parent: " + parent + " "+ bounds);
    	return parent == null? null: parent.localToParent(bounds);
    	
    }   

}
