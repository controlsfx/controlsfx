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

/**
 * A control for allowing users to provide a rating. This control supports
 * {@link #partialRatingProperty() partial ratings} (i.e. not whole numbers and
 * dependent upon where the user clicks in the control) and 
 * {@link #updateOnHoverProperty() updating the rating on hover}.
 */
public class Rating extends Control {
    
    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a default instance with a minimum rating of 0 and a maximum 
     * rating of 5.
     */
    public Rating() {
        this(5);
    }
    
    /**
     * Creates a default instance with a minimum rating of 0 and a maximum rating
     * as provided by the argument.
     * 
     * @param max The maximum allowed rating value.
     */
    public Rating(int max) {
        this(max, -1);
    }
    
    /**
     * Creates a Rating instance with a minimum rating of 0, a maximum rating
     * as provided by the {@code max} argument, and a current rating as provided
     * by the {@code rating} argument.
     * 
     * @param max The maximum allowed rating value.
     */
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
    /**
     * The current rating value.
     */
    public final DoubleProperty ratingProperty() {
        return rating;
    }
    private DoubleProperty rating = new SimpleDoubleProperty(this, "rating", 3);
    
    /**
     * Sets the current rating value.
     */
    public final void setRating(double value) {
       ratingProperty().set(value);
    }
    
    /**
     * Returns the current rating value.
     */
    public final double getRating() {
        return rating == null ? 3 : rating.get();
    }

    
    // --- Max
    /**
     * The maximum-allowed rating value.
     */
    public final IntegerProperty maxProperty() {
        return max;
    }
    private IntegerProperty max = new SimpleIntegerProperty(this, "max", 5);
    
    /**
     * Sets the maximum-allowed rating value.
     */
    public final void setMax(int value) {
       maxProperty().set(value);
    }

    /**
     * Returns the maximum-allowed rating value.
     */
    public final int getMax() {
        return max == null ? 5 : max.get();
    }
    
    
    // --- Orientation
    /**
     * The {@link Orientation} of the {@code Rating} - this can either be 
     * horizontal or vertical.
     */
    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new SimpleObjectProperty<Orientation>(this, "orientation", Orientation.HORIZONTAL);
        }
        return orientation;
    }
    private ObjectProperty<Orientation> orientation;
    
    /**
     * Sets the {@link Orientation} of the {@code Rating} - this can either be 
     * horizontal or vertical.
     */
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    };
    
    /**
     * Returns the {@link Orientation} of the {@code Rating} - this can either 
     * be horizontal or vertical.
     */
    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    
    // --- partial rating
    /**
     * If true this allows for users to set a rating as a floating point value.
     * In other words, the range of the rating 'stars' can be thought of as a
     * range between [0, max], and whereever the user clicks will be calculated
     * as the new rating value. If this is false the more typical approach is used
     * where the selected 'star' is used as the rating.
     */
    public final BooleanProperty partialRatingProperty() {
        return partialRating;
    }
    private BooleanProperty partialRating = new SimpleBooleanProperty(this, "partialRating", false);
    
    /**
     * Sets whether {@link #partialRatingProperty() partial rating} support is
     * enabled or not.
     */
    public final void setPartialRating(boolean value) {
        partialRatingProperty().set(value);
    }
    
    /**
     * Returns whether {@link #partialRatingProperty() partial rating} support is
     * enabled or not.
     */
    public final boolean isPartialRating() {
        return partialRating == null ? false : partialRating.get();
    }

    
    // --- update on hover
    /**
     * If true this allows for the {@link #ratingProperty() rating property} to
     * be updated simply by the user hovering their mouse over the control. If
     * false the user is required to click on their preferred rating to register
     * the new rating with this control.
     */
    public final BooleanProperty updateOnHoverProperty() {
        return updateOnHover;
    }
    private BooleanProperty updateOnHover = new SimpleBooleanProperty(this, "updateOnHover", false);
    
    /**
     * Sets whether {@link #updateOnHoverProperty() update on hover} support is
     * enabled or not.
     */
    public final void setUpdateOnHover(boolean value) {
        updateOnHoverProperty().set(value);
    }
    
    /**
     * Returns whether {@link #updateOnHoverProperty() update on hover} support is
     * enabled or not.
     */
    public final boolean isUpdateOnHover() {
        return updateOnHover == null ? false : updateOnHover.get();
    }
}
