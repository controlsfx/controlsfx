/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import impl.org.controlsfx.behavior.RatingBehavior;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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
    
    /***************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private static final String STRONG = "strong";
    
    private boolean updateOnHover;
    private boolean partialRating;
    
    // the container for the traditional rating control. If updateOnHover and
    // partialClipping are disabled, this will show a combination of strong
    // and non-strong graphics, depending on the current rating value
    private Pane backgroundContainer;
    
    // the container for the strong graphics which may be partially clipped.
    // Note that this only exists if updateOnHover or partialClipping is enabled.
    private Pane foregroundContainer;
    
    private double rating = -1;

    private Rectangle forgroundClipRect;
    
    private Point2D lastMouseLocation = new Point2D(0, 0);
    
    private final EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent event) {
            lastMouseLocation = new Point2D(event.getSceneX(), event.getSceneY());
            
            // if we support partial ratings, we will use the partial rating value
            // directly. If we don't, but we support updateOnHover, then we will
            // ceil it. Otherwise, the rest of this method is a no op
            double newRating = partialRating || updateOnHover ?
                    calculateRating() : -1;
            
            if (partialRating && updateOnHover) {
                updateClip();
            }
            
            if (updateOnHover && newRating > -1) {
                updateRating(newRating);
            }
        }
    };
    
    private final EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent event) {
            if (updateOnHover) return;
            
            if (partialRating) {
                updateClip();
            }
            
            updateRating(calculateRating());
        }
    };
    
    

    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    public RatingSkin(Rating control) {
        super(control, new RatingBehavior(control));
        
        this.updateOnHover = control.isUpdateOnHover();
        this.partialRating = control.isPartialRating();
        
        // init
        recreateButtons();
        updateRating(getSkinnable().getRating());
        // -- end init
        
        registerChangeListener(control.ratingProperty(), "RATING");
        registerChangeListener(control.maxProperty(), "MAX");
        registerChangeListener(control.orientationProperty(), "ORIENTATION");
        registerChangeListener(control.updateOnHoverProperty(), "UPDATE_ON_HOVER");
        registerChangeListener(control.partialRatingProperty(), "PARTIAL_RATING");
    }

    
    
    /***************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
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
            recreateButtons();
        }
    }
    
    private void recreateButtons() {
        backgroundContainer = null;
        foregroundContainer = null;
        
        backgroundContainer = isVertical() ? new VBox() : new HBox();
        backgroundContainer.getStyleClass().add("container");
        getChildren().setAll(backgroundContainer);
        
        if (updateOnHover || partialRating) {
            foregroundContainer = isVertical() ? new VBox() : new HBox();
            foregroundContainer.getStyleClass().add("container");
            foregroundContainer.setMouseTransparent(true);
            getChildren().add(foregroundContainer);
            
            forgroundClipRect = new Rectangle();
            foregroundContainer.setClip(forgroundClipRect);
        }
        
        for (int index = 0; index <= getSkinnable().getMax(); index++) {
            Node backgroundNode = createButton();
            
            if (index > 0) {
                if (isVertical()) {
                    backgroundContainer.getChildren().add(0,backgroundNode);
                } else {
                    backgroundContainer.getChildren().add(backgroundNode);
                }
                
                if (partialRating) {
                    Node foregroundNode = createButton();
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
    
    private double calculateRating() {
        final Point2D b = backgroundContainer.sceneToLocal(lastMouseLocation.getX(), lastMouseLocation.getY());
        
        final double x = b.getX();
        final double y = b.getY();
        
        final Rating control = getSkinnable();
        
        final int max = control.getMax();
        final double w = control.getWidth() - (snappedLeftInset() + snappedRightInset());
        final double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
        
        double newRating = -1;
        
        if (isVertical()) {
            newRating = ((h - y) / h) * max;
        } else {
            newRating = (x / w) * max;
        }
        
        if (! partialRating) {
            newRating = Utils.clamp(1, Math.ceil(newRating), control.getMax());
        }
        
        return newRating;
    }
    
    private void updateClip() {
        final Point2D b = backgroundContainer.sceneToLocal(lastMouseLocation.getX(), lastMouseLocation.getY());
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
    
    private Node createButton() {
        Region btn = new Region();
        btn.getStyleClass().add("button");
        
        btn.setOnMouseMoved(mouseMoveHandler);
        btn.setOnMouseClicked(mouseClickHandler);
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
            final int max = getSkinnable().getMax();

            // make a copy of the buttons list so that we can reverse the order if
            // the list is vertical (as the buttons are ordered bottom to top).
            List<Node> buttons = new ArrayList<Node>(backgroundContainer.getChildren());
            if (isVertical()) {
                Collections.reverse(buttons);
            }
            
            for (int i = 0; i < max; i++) {
                Node button = buttons.get(i);
    
                final List<String> styleClass = button.getStyleClass();
                final boolean containsStrong = styleClass.contains(STRONG);
                
                if (i < rating) {
                    if (! containsStrong) {
                        styleClass.add(STRONG);
                    }
                } else if (containsStrong) {
                    styleClass.remove(STRONG);
                }
            }
        }
    }
    
    private boolean isVertical() {
        return getSkinnable().getOrientation() == Orientation.VERTICAL;
    }
    
    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
