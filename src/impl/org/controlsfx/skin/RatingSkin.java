package impl.org.controlsfx.skin;

import impl.org.controlsfx.behavior.RatingBehavior;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.Rating;

import com.sun.javafx.Utils;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 *
 * @author Jonathan Giles
 */
public class RatingSkin extends BehaviorSkinBase<Rating, RatingBehavior> {
    
    private static final String STRONG = "strong";
    
    private static final boolean ALLOW_IMMEDIATE_CHANGE = true;
    private static final boolean ALLOW_PARTIAL_RATING = true;
    
    // the container for the 'non-strong' graphics
    private Pane backgroundContainer;
    
    // the container for the strong graphics
    private Pane foregroundContainer;
    
    private ToggleGroup group;
    private double rating = -1;

    private Rectangle forgroundClipRect;

    public RatingSkin(Rating control) {
        super(control, new RatingBehavior(control));
        
        // init
        this.group = new ToggleGroup();
        this.group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                if (ALLOW_IMMEDIATE_CHANGE || t1 == null) return;
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
        backgroundContainer = isVertical() ? new VBox() : new HBox();
        backgroundContainer.getStyleClass().add("container");
        getChildren().setAll(backgroundContainer);
        
        if (ALLOW_PARTIAL_RATING) {
            foregroundContainer = isVertical() ? new VBox() : new HBox();
            foregroundContainer.getStyleClass().add("container");
            foregroundContainer.setMouseTransparent(true);
            getChildren().add(foregroundContainer);
            
            forgroundClipRect = new Rectangle(50,100);
            forgroundClipRect.heightProperty().bind(getSkinnable().heightProperty());
            foregroundContainer.setClip(forgroundClipRect);
        }
        
        for (int index = 0; index <= getSkinnable().getMax(); index++) {
            Node backgroundNode = createButton(index);
            
            if (index > 0) {
                if (isVertical()) {
                    backgroundContainer.getChildren().add(0,backgroundNode);
                } else {
                    backgroundContainer.getChildren().add(backgroundNode);
                }
                
                if (ALLOW_PARTIAL_RATING) {
                    Node foregroundNode = createButton(index);
                    foregroundNode.getStyleClass().add(STRONG);
                    foregroundNode.setMouseTransparent(true);
                    
                    if (isVertical()) {
                        foregroundContainer.getChildren().add(0,foregroundNode);
                    } else {
                        foregroundContainer.getChildren().add(foregroundNode);
                    }
                }
            }
        }
        
        updateRating();
    }
    
    EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent event) {
            Point2D b = foregroundContainer.sceneToLocal(event.getSceneX(), event.getSceneY());
            forgroundClipRect.setWidth(b.getX());
            
            // 
            if (ALLOW_IMMEDIATE_CHANGE) {
                // TODO update the rating value on the control as a fraction
                // between 0 and max value
                final int max = getSkinnable().getMax();
                final double w = getSkinnable().getWidth() - 
                        (snappedLeftInset() + snappedRightInset()) -
                        getSpacing() * max;
                double newRating = (b.getX() / w * 2.5) * max;
                updateRating(newRating);
            }
        }
    };
    
    private double getSpacing() {
        return (backgroundContainer instanceof HBox) ?
                ((HBox)backgroundContainer).getSpacing() :
                ((VBox)backgroundContainer).getSpacing();
    }
    
    private Node createButton(int index) {
        ToggleButton btn = new ToggleButton();
        btn.setUserData(index);
        btn.setToggleGroup(group);
        
        btn.setOnMouseMoved(mouseMoveHandler);
        return btn;
    }
    
    private void updateRating() {
        updateRating(getSkinnable().getRating());
    }
    
    private void updateRating(double newRating) {
        if (newRating == rating) return;
        
        rating = Utils.clamp(0, newRating, getSkinnable().getMax());
        
        if (rating == 1.0) {
            System.out.println("hit");
        }
        
        if (! getSkinnable().ratingProperty().isBound()) {
            getSkinnable().setRating(rating);
        }
        
        // if we immediately change the rating, then we don't need the following
        if (! ALLOW_IMMEDIATE_CHANGE) {
            int max = getSkinnable().getMax();
            for (int i = 0; i < max; i++) {
                ToggleButton toggle = (ToggleButton) backgroundContainer.getChildren().get(i);
                int toggleIndex = (Integer) toggle.getUserData();
    
                toggle.getStyleClass().remove(STRONG);
                if (toggleIndex <= rating) {
                    toggle.getStyleClass().add(STRONG);
                }
            }
        }
    }
    
    private boolean isVertical() {
        return getSkinnable().getOrientation() == Orientation.VERTICAL;
    }
}
