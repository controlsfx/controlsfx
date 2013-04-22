package org.controlsfx.control;

import impl.org.controlsfx.skin.RatingSkin;
import javafx.beans.property.*;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class Rating extends Control {
    
    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    public Rating() {
        this(5);
    }
    
    public Rating(int max) {
        this(max, -1);
    }
    
    public Rating(int max, int rating) {
        getStyleClass().setAll("rating");
        
        setMax(max);
        setRating(rating == -1 ? (int) Math.floor(max / 2.0) : rating);
    }
    
    
    
    /***************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new RatingSkin(this);
    }

    /** {@inheritDoc} */
    @Override protected String getUserAgentStylesheet() {
        return getClass().getResource("rating.css").toExternalForm();
    }
    
    
    

    /***************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- Rating
    private DoubleProperty rating = new SimpleDoubleProperty(this, "rating", 3);
    public final void setRating(double value) {
       ratingProperty().set(value);
    }

    public final double getRating() {
        return rating == null ? 3 : rating.get();
    }

    public final DoubleProperty ratingProperty() {
        return rating;
    }
    
    
    // --- Max
    private IntegerProperty max = new SimpleIntegerProperty(this, "max", 5);
    public final void setMax(int value) {
       maxProperty().set(value);
    }

    public final int getMax() {
        return max == null ? 5 : max.get();
    }

    public final IntegerProperty maxProperty() {
        return max;
    }
    
    
    // --- Orientation
    private ObjectProperty<Orientation> orientation;
    
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    };
    
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }
    
    /**
     * The orientation of the {@code Rating} - this can either be horizontal
     * or vertical.
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new SimpleObjectProperty<Orientation>(this, "orientation", Orientation.HORIZONTAL);
        }
        return orientation;
    }
    
    
    // --- partial rating
    /**
     * If true this allows for users to set a rating as a floating point value.
     * In other words, the range of the rating 'stars' can be thought of as a
     * range between [0, max], and whereever the user clicks will be calculated
     * as the new rating value. If this is false the more typical approach is used
     * where the selected 'star' is used as the rating.
     */
    private BooleanProperty partialRating = new SimpleBooleanProperty(this, "partialRating", false);
    public final BooleanProperty partialRatingProperty() {
        return partialRating;
    }
    public final void setPartialRating(boolean value) {
        partialRatingProperty().set(value);
    }
    public final boolean isPartialRating() {
        return partialRating == null ? false : partialRating.get();
    }

    
    // --- update on hover
    /**
     * 
     */
    private BooleanProperty updateOnHover = new SimpleBooleanProperty(this, "updateOnHover", false);
    public final BooleanProperty updateOnHoverProperty() {
        return updateOnHover;
    }
    public final void setUpdateOnHover(boolean value) {
        updateOnHoverProperty().set(value);
    }
    public final boolean isUpdateOnHover() {
        return updateOnHover == null ? false : updateOnHover.get();
    }
}
