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
