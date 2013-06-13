package impl.org.controlsfx.skin;

import javafx.collections.ObservableSet;
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
    
    
    private void showDecorations(Node target) {
    	
    	//TODO Respond for component bound changes
    	//TODO Respond for component visibility changes
    	//TODO Respond to  component decoration changes.
    	
    	// show target decorations if found 
    	ObservableSet<Decoration> decorations = DecorationUtils.getDecorations(target);
		if ( decorations != null ) {
			for( Decoration decoration: decorations ) {
				showDecoration(target, decoration);
			}
		}
    	
		// recursively show decorations for target's children
    	if ( target instanceof Parent ) {
    		for ( Node n: ((Parent)target).getChildrenUnmodifiable()) {
    			showDecorations(n);
    		}
    	}
    }
    
    private void showDecoration( Node target, Decoration decoration ) {
		Node dnode = decoration.getNode();
		if ( !stackPane.getChildren().contains(dnode)) {
	   	   stackPane.getChildren().add(dnode);
		   StackPane.setAlignment(dnode, Pos.TOP_LEFT); // TODO support for all positions.
		}
		Bounds targetBounds = getDecorationBounds( target );
		Bounds dbounds = dnode.getBoundsInLocal();
		Insets margin = new Insets( targetBounds.getMinY()-dbounds.getHeight()/2 + getVInset(targetBounds,decoration),
				                    0,0,
				                    targetBounds.getMinX()-dbounds.getWidth()/2 + getHInset(targetBounds,decoration) );
		StackPane.setMargin(dnode, margin);
		
    }
    
    private double getHInset( Bounds targetBounds, Decoration decoration ) {
		switch( decoration.getPosition().getHpos() ) {
		    case CENTER: return targetBounds.getWidth()/2;
		    case RIGHT : return targetBounds.getWidth();
		    default    : return 0;
		}
    }
    
    private double getVInset( Bounds targetBounds, Decoration decoration ) {
		switch( decoration.getPosition().getVpos() ) {
		    case CENTER: return targetBounds.getHeight()/2;
		    case BOTTOM: return targetBounds.getHeight();
		    default    : return 0;
		}
    }
    
    /**
     * Computes bounds on decoration pane for any node
     * as long as the node is a child of decoration pane. 
     * @param node node to compute bounds for
     * @return bounds for give node on decoration pane or null
     *          node is not a child of decoration pane 
     */
    private Bounds getDecorationBounds( Node node ) {

    	if ( node == null ) return null;
    	Node parent = node.getParent();
    	Bounds bounds = null;
    	while ( parent != null && parent != stackPane) {
    	   bounds = bounds == null? node.getBoundsInParent(): parent.localToParent(bounds);
    	   parent = parent.getParent();
    	}
    	return parent == null? null: parent.localToParent(bounds);
    	
    }   

}
