/**
 * Copyright (c) 2013, 2015 ControlsFX
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

import impl.org.controlsfx.behavior.RatingBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.controlsfx.tools.Utils;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

/**
 *
 */
public class RatingSkin extends BehaviorSkinBase<Rating, RatingBehavior> {
        
    /***************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private static final String STRONG = "strong"; //$NON-NLS-1$
    
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
        
    private final EventHandler<MouseEvent> mouseMoveHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent event) {

        	// if we support updateOnHover, calculate the intended rating based on the mouse 
        	// location and update the control property with it.
        	
            if (updateOnHover) {
            	updateRatingFromMouseEvent(event);
            }
        }
    };
    
    private final EventHandler<MouseEvent> mouseClickHandler = new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent event) {

        	// if we are not updating on hover, calculate the intended rating based on the mouse 
        	// location and update the control property with it.
        	
            if (! updateOnHover) {
            	updateRatingFromMouseEvent(event);
            }
        }
    };
    
    private void updateRatingFromMouseEvent(MouseEvent event) {
    	Rating control = getSkinnable();
    	if (! control.ratingProperty().isBound()) {
        	Point2D mouseLocation = new Point2D(event.getSceneX(), event.getSceneY());
    		control.setRating(calculateRating(mouseLocation));
    	}
    }

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
        updateRating();
        // -- end init
        
        registerChangeListener(control.ratingProperty(), "RATING"); //$NON-NLS-1$
        registerChangeListener(control.maxProperty(), "MAX"); //$NON-NLS-1$
        registerChangeListener(control.orientationProperty(), "ORIENTATION"); //$NON-NLS-1$
        registerChangeListener(control.updateOnHoverProperty(), "UPDATE_ON_HOVER"); //$NON-NLS-1$
        registerChangeListener(control.partialRatingProperty(), "PARTIAL_RATING"); //$NON-NLS-1$
        // added to ensure clip is correctly calculated when control is first shown:
        registerChangeListener(control.boundsInLocalProperty(), "BOUNDS"); //$NON-NLS-1$
    }

    
    
    /***************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "RATING") { //$NON-NLS-1$
            updateRating();
        } else if (p == "MAX") { //$NON-NLS-1$
            recreateButtons();
        } else if (p == "ORIENTATION") { //$NON-NLS-1$
            recreateButtons();
        } else if (p == "PARTIAL_RATING") { //$NON-NLS-1$
            this.partialRating = getSkinnable().isPartialRating();
            recreateButtons();
        } else if (p == "UPDATE_ON_HOVER") { //$NON-NLS-1$
            this.updateOnHover = getSkinnable().isUpdateOnHover();
            recreateButtons();
        } else if (p == "BOUNDS") { //$NON-NLS-1$
        	if (this.partialRating) {
        		updateClip();
        	}
        }
    }
    
    private void recreateButtons() {
        backgroundContainer = null;
        foregroundContainer = null;
        
        backgroundContainer = isVertical() ? new VBox() : new HBox();
        backgroundContainer.getStyleClass().add("container"); //$NON-NLS-1$
        getChildren().setAll(backgroundContainer);
        
        if (updateOnHover || partialRating) {
            foregroundContainer = isVertical() ? new VBox() : new HBox();
            foregroundContainer.getStyleClass().add("container"); //$NON-NLS-1$
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
    
    // Calculate the rating based on a mouse position (in Scene coordinates).
    // If we support partial ratings, the value is calculated directly.
    // Otherwise the ceil of the value is computed.
    private double calculateRating(Point2D sceneLocation) {
        final Point2D b = backgroundContainer.sceneToLocal(sceneLocation);
        
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
        final Rating control = getSkinnable();
        final double h = control.getHeight() - (snappedTopInset() + snappedBottomInset());
        final double w = control.getWidth() - (snappedLeftInset() + snappedRightInset());
        
        if (isVertical()) {
        	final double y = h * rating / control.getMax() ;
            forgroundClipRect.relocate(0, h - y);
            forgroundClipRect.setWidth(control.getWidth());
            forgroundClipRect.setHeight(y);
        } else {
        	final double x = w * rating / control.getMax();        	
            forgroundClipRect.setWidth(x);
            forgroundClipRect.setHeight(control.getHeight());
        }
    	
    }
    
//    private double getSpacing() {
//        return (backgroundContainer instanceof HBox) ?
//                ((HBox)backgroundContainer).getSpacing() :
//                ((VBox)backgroundContainer).getSpacing();
//    }
    
    private Node createButton() {
        Region btn = new Region();
        btn.getStyleClass().add("button"); //$NON-NLS-1$
        
        btn.setOnMouseMoved(mouseMoveHandler);
        btn.setOnMouseClicked(mouseClickHandler);
        return btn;
    }
    
    // Update the skin based on a new value for the rating.
    // If we support partial ratings, updates the clip.
    // Otherwise, updates the style classes for the buttons.
    
    private void updateRating() {
    	
    	double newRating = getSkinnable().getRating();
    	    	
        if (newRating == rating) return;
        
        rating = Utils.clamp(0, newRating, getSkinnable().getMax());

        if (partialRating) {
        	updateClip();
        } else {
            updateButtonStyles();
        }
    }

	private void updateButtonStyles() {
		final int max = getSkinnable().getMax();

		// make a copy of the buttons list so that we can reverse the order if
		// the list is vertical (as the buttons are ordered bottom to top).
		List<Node> buttons = new ArrayList<>(backgroundContainer.getChildren());
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
    
    private boolean isVertical() {
        return getSkinnable().getOrientation() == Orientation.VERTICAL;
    }
    
    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
