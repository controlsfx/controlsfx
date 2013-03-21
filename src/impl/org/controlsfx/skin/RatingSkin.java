package impl.org.controlsfx.skin;

import impl.org.controlsfx.behavior.RatingBehavior;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import org.controlsfx.control.Rating;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 *
 * @author Jonathan Giles
 */
public class RatingSkin extends BehaviorSkinBase<Rating, RatingBehavior> {
    
    private static final String STRONG = "strong";
    
    private Pane btnContainer;
    private ToggleGroup group;
    private int rating = -1;

    public RatingSkin(Rating control) {
        super(control, new RatingBehavior(control));
        
        // init
        this.group = new ToggleGroup();
        this.group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                if (t1 == null) return;
                updateRating((Integer) t1.getUserData());
            }
        });
        
        recreateButtons();
        updateRating(getSkinnable().getRating());
        // -- end init
        
        registerChangeListener(control.ratingProperty(), "RATING");
        registerChangeListener(control.maxProperty(), "MAX");
        registerChangeListener(control.orientationProperty(), "ORIENTATION");
    }

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "RATING") {
            updateRating();
        } else if (p == "MAX") {
            recreateButtons();
        } else if (p == "ORIENTATION") {
            recreateButtons();
        }
    }
    
    private void recreateButtons() {
        btnContainer = isVertical() ? new VBox() : new HBox();
        btnContainer.getStyleClass().add("container");
        getChildren().setAll(btnContainer);
        
        for (int index = 0; index <= getSkinnable().getMax(); index++) {
            Node n = createButton(index);
            if (index > 0) {
                if (isVertical()) {
                    btnContainer.getChildren().add(0,n);
                } else {
                    btnContainer.getChildren().add(n);
                }
            }
        }
        
        updateRating();
    }
    
    private Node createButton(int index) {
        ToggleButton btn = new ToggleButton();
        btn.setUserData(index);
        btn.setToggleGroup(group);
        return btn;
    }
    
//    public void setRating(int rating) {
//        if (rating >= 0 && rating <= max) {
//            this.rating = rating;
//            group.selectToggle((ToggleButton)getChildren().get(rating == 0 ? 0 : rating - 1));
//        }
//    }
    
    private void updateRating() {
        updateRating(getSkinnable().getRating());
    }
    
    private void updateRating(int newRating) {
//        this.rating = newRating;
        
//        group.selectToggle((ToggleButton)getChildren().get(newRating == 0 ? 0 : newRating - 1));
        if (newRating == rating) return;
        rating = newRating;
        
        if (! getSkinnable().ratingProperty().isBound()) {
            getSkinnable().setRating(newRating);
        }
        
        int max = getSkinnable().getMax();
        for (int i = 0; i < max; i++) {
            ToggleButton toggle = (ToggleButton) btnContainer.getChildren().get(i);
            int toggleIndex = (Integer) toggle.getUserData();

            toggle.getStyleClass().remove(STRONG);
            if (toggleIndex <= newRating) {
                toggle.getStyleClass().add(STRONG);
            }
        }
    }
    
    private boolean isVertical() {
        return getSkinnable().getOrientation() == Orientation.VERTICAL;
    }
}
