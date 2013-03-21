package impl.org.controlsfx.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import org.controlsfx.control.SegmentedButton;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class SegmentedButtonSkin extends BehaviorSkinBase<SegmentedButton, BehaviorBase<SegmentedButton>>{

    private final ToggleGroup group;
    
    private final HBox container;

    /**
     * 
     */
    public SegmentedButtonSkin(SegmentedButton control) {
        super(control, new BehaviorBase<>(control));
        
        group = new ToggleGroup();
        container = new HBox();
        
        getChildren().add(container);
        
        updateButtons();
        getButtons().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable observable) {
                updateButtons();
            }
        });
    }
    
    private ObservableList<ToggleButton> getButtons() {
        return getSkinnable().getButtons();
    }
    
    private void updateButtons() {
        ObservableList<ToggleButton> buttons = getButtons();
        
        for (int i = 0; i < getButtons().size(); i++) {
            ToggleButton t = buttons.get(i);
            t.setToggleGroup(group);
            container.getChildren().add(t);

            if (i == buttons.size() - 1) {
                if(i == 0) {
                    t.getStyleClass().add("only-button");
                } else {
                    t.getStyleClass().add("last-button");
                }
            } else if (i == 0) {
                t.getStyleClass().add("first-button");
            } else {
                t.getStyleClass().add("middle-button");
            }
        }
    }
}
