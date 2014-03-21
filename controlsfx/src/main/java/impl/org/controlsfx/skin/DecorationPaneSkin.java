package impl.org.controlsfx.skin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import org.controlsfx.control.DecorationPane;
import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.DecorationUtils;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class DecorationPaneSkin extends BehaviorSkinBase<DecorationPane, BehaviorBase<DecorationPane>> {

    private final StackPane stackPane = new StackPane();
    private final Node base;
    
    private final Map<Node, Node> nodeDecorationMap = new WeakHashMap<>();
    
    ChangeListener<Boolean> visibilityListener = new ChangeListener<Boolean>() {
        @Override public void changed(ObservableValue<? extends Boolean> o, Boolean wasVisible, Boolean isVisible) {
            BooleanProperty p = (BooleanProperty)o;
            Node n = (Node) p.getBean();
            
            removeAllDecorationsOnNode(n, DecorationUtils.getAllDecorations(n));
            DecorationUtils.unregisterAllDecorations(n);
        }
    };

    public DecorationPaneSkin(DecorationPane control, Node base) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));
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
        // TODO Respond for component bound changes

        // show target decorations if found
        addAllDecorationsOnNode(target, DecorationUtils.getAllDecorations(target));

        // recursively show decorations for target's children
        if (target instanceof Parent) {
            for (Node n : ((Parent) target).getChildrenUnmodifiable()) {
                showDecorations(n);
            }
        }
    }
    
    public void updateDecorationsOnNode(Node targetNode, List<Decoration> added, List<Decoration> removed) {
        removeAllDecorationsOnNode(targetNode, removed);
        addAllDecorationsOnNode(targetNode, added);
    }

    private void showDecoration(Node targetNode, Decoration decoration) {
        Node decorationNode = decoration.decorate(targetNode);
        if (decorationNode != null) {
            nodeDecorationMap.put(targetNode, decorationNode);
        }
        
        if (decorationNode != null && !stackPane.getChildren().contains(decorationNode)) {
            stackPane.getChildren().add(decorationNode);
            StackPane.setAlignment(decorationNode, Pos.TOP_LEFT); // TODO support for all positions.
        }
        
        targetNode.visibleProperty().addListener(visibilityListener);
    }

    private void removeAllDecorationsOnNode(Node targetNode, List<Decoration> decorations) {
        if (decorations == null) return;
        for (Decoration decoration : decorations) {
            Node decorationNode = nodeDecorationMap.get(targetNode);
            
            if (targetNode != null) {
                stackPane.getChildren().remove(decorationNode);
                decoration.undecorate(targetNode);
            }
        }
    }
    
    private void addAllDecorationsOnNode(Node targetNode, List<Decoration> decorations) {
        if (decorations == null) return;
        for (Decoration decoration : decorations) {
            showDecoration(targetNode, decoration);
        }
    }
}
