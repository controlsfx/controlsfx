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
    
    
    
    
    @Override protected Skin<?> createDefaultSkin() {
        return new RatingSkin(this);
    }

    @Override protected String getUserAgentStylesheet() {
        return getClass().getResource("rating.css").toExternalForm();
    }
    
    
    

    
    // --- Rating
    private IntegerProperty rating = new SimpleIntegerProperty(this, "rating", 3);
    public final void setRating(int value) {
       ratingProperty().set(value);
    }

    public final int getRating() {
        return rating == null ? 3 : rating.get();
    }

    public final IntegerProperty ratingProperty() {
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
}
