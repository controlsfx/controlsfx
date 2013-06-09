package impl.org.controlsfx.skin;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.DecorationPane;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class DecorationPaneSkin  extends BehaviorSkinBase<DecorationPane, BehaviorBase<DecorationPane>> {
	
	private final StackPane stackPane = new StackPane();
	private final Node node;
	
	public DecorationPaneSkin(DecorationPane control, Region overlay) {
		super(control, new BehaviorBase<>(control));
		node = control.getNode();
		stackPane.getChildren().add(node);
		stackPane.getChildren().add(overlay);
	}
	
	@Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
    	stackPane.resizeRelocate(x, y, w, h);
    }


}
