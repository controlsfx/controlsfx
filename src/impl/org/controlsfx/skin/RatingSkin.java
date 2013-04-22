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
    
    private boolean updateOnHover;
    private boolean partialRating;
    
    // the container for the 'non-strong' graphics
    private Pane backgroundContainer;
    
    // the container for the strong graphics
    private Pane foregroundContainer;
    
    private ToggleGroup group;
    private double rating = -1;

    private Rectangle forgroundClipRect;

    public RatingSkin(Rating control) {
        super(control, new RatingBehavior(control));
        
        this.updateOnHover = control.isUpdateOnHover();
        this.partialRating = control.isPartialRating();
        
        // init
        this.group = new ToggleGroup();
        this.group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                if (updateOnHover || t1 == null) return;
                
                if (partialRating) {
                    updateClip();
                    updateRating(calculatePartialRating());
                } else {
                    updateRating((Integer) t1.getUserData());
                }
            }
        });
        
        recreateButtons();
        updateRating(getSkinnable().getRating());
        // -- end init
        
        registerChangeListener(control.ratingProperty(), "RATING");
        registerChangeListener(control.maxProperty(), "MAX");
        registerChangeListener(control.orientationProperty(), "ORIENTATION");
        registerChangeListener(control.updateOnHoverProperty(), "UPDATE_ON_HOVER");
        registerChangeListener(control.partialRatingProperty(), "PARTIAL_RATING");
    }

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "RATING") {
            updateRating();
        } else if (p == "MAX") {
            recreateButtons();
        } else if (p == "ORIENTATION") {
            recreateButtons();
        } else if (p == "PARTIAL_RATING") {
            this.partialRating = getSkinnable().isPartialRating();
            recreateButtons();
        } else if (p == "UPDATE_ON_HOVER") {
            this.updateOnHover = getSkinnable().isUpdateOnHover();
        }
    }
    
    private void recreateButtons() {
        backgroundContainer = isVertical() ? new VBox() : new HBox();
        backgroundContainer.getStyleClass().add("container");
        getChildren().setAll(backgroundContainer);
        
        foregroundContainer = isVertical() ? new VBox() : new HBox();
        foregroundContainer.getStyleClass().add("container");
        foregroundContainer.setMouseTransparent(true);
        getChildren().add(foregroundContainer);
        
        forgroundClipRect = new Rectangle();
        foregroundContainer.setClip(forgroundClipRect);
        
        for (int index = 0; index <= getSkinnable().getMax(); index++) {
            Node backgroundNode = createButton(index);
            
            if (index > 0) {
                if (isVertical()) {
                    backgroundContainer.getChildren().add(0,backgroundNode);
                } else {
                    backgroundContainer.getChildren().add(backgroundNode);
                }
                
                if (partialRating) {
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
            lastMouseLocation = new Point2D(event.getSceneX(), event.getSceneY());
            
            final Rating control = getSkinnable();

            // if we support partial ratings, we will use the partial rating value
            // directly. If we don't, but we support updateOnHover, then we will
            // ceil it. Otherwise, the rest of this method is a no op
            double newRating = partialRating || updateOnHover ?
                    calculatePartialRating() : -1;
            
            if (partialRating && updateOnHover) {
                updateClip();
            } else if (updateOnHover) {
                newRating = Utils.clamp(1, Math.ceil(newRating), control.getMax());
            }
            
            if (newRating > -1) {
                updateRating(newRating);
            }
        }
    };
    
    private Point2D lastMouseLocation = new Point2D(0, 0);
    private double calculatePartialRating() {
        final Point2D b = foregroundContainer.sceneToLocal(lastMouseLocation.getX(), lastMouseLocation.getY());
        
        final double x = b.getX();
        final double y = b.getY();
        
        final Rating control = getSkinnable();
        final int max = control.getMax();
        final double w = control.getWidth() - (snappedLeftInset() + snappedRightInset());
        final double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
        final double _w = w - getSpacing() * max;
        
        double newRating = -1;
        
        if (isVertical()) {
            newRating = ((h - y) / h) * max;
        } else {
            newRating = (x / _w * 2.5) * max;
        }
        
        return newRating;
    }
    
    private void updateClip() {
        final Point2D b = foregroundContainer.sceneToLocal(lastMouseLocation.getX(), lastMouseLocation.getY());
        final Rating control = getSkinnable();
        final double x = b.getX();
        final double y = b.getY();
        final double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
        
        if (isVertical()) {
            forgroundClipRect.relocate(0, y);
            forgroundClipRect.setWidth(control.getWidth());
            forgroundClipRect.setHeight(h - y);
        } else {
            forgroundClipRect.setWidth(x);
            forgroundClipRect.setHeight(control.getHeight());
        }
    }
    
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
        
        if (! getSkinnable().ratingProperty().isBound()) {
            getSkinnable().setRating(rating);
        }
        
        // if we immediately change the rating, then we don't need the following
        if (! partialRating) {
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
