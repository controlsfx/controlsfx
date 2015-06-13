/**
 * Copyright (c) 2013, 2015, ControlsFX
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.Skin;

/**
 * A control for allowing users to provide a rating. This control supports
 * {@link #partialRatingProperty() partial ratings} (i.e. not whole numbers and
 * dependent upon where the user clicks in the control) and 
 * {@link #updateOnHoverProperty() updating the rating on hover}. Read on for
 * more examples!
 * 
 * <h3>Examples</h3>
 * It can be hard to appreciate some of the features of the Rating control, so
 * hopefully the following screenshots will help. Firstly, here is what the 
 * standard (horizontal) Rating control looks like when it has five stars, and a 
 * rating of two:
 * 
 * <br>
 * <center>
 * <img src="rating-horizontal.png" alt="Screenshot of horizontal Rating">
 * </center>
 * 
 * <p>To create a Rating control that looks like this is simple:
 * 
 * <pre>
 * {@code 
 * final Rating rating = new Rating();}</pre>
 * 
 * <p>This creates a default horizontal rating control. To create a vertical 
 * Rating control, simply change the orientation, as such:
 * 
 * <pre>
 * {@code 
 * final Rating rating = new Rating();
 * rating.setOrientation(Orientation.VERTICAL);}</pre>
 * 
 * <p>The end result of making this one line change is shown in the screenshot
 * below:
 * 
 * <br>
 * <center>
 * <img src="rating-vertical.png" alt="Screenshot of vertical Rating">
 * </center>
 * 
 * <p>One of the features of the Rating control is that it doesn't just allow
 * for 'integer' ratings: it also allows for the user to click anywhere within
 * the rating area to set a 'float' rating. This is hard to describe, but easy 
 * to show in a picture:
 * 
 * <br>
 * <center>
 * <img src="rating-partial.png" alt="Screenshot of partial Rating">
 * </center>
 * 
 * <p>In essence, in the screenshot above, the user clicked roughly in the 
 * middle of the third star. This results in a rating of approximately 2.44.
 * To enable {@link #partialRatingProperty() partial ratings}, simply do the
 * following when instantiating the Rating control:
 * 
 * <pre>
 * {@code 
 * final Rating rating = new Rating();
 * rating.setPartialRating(true);}</pre>
 * 
 * <p>So far all of the Rating controls demonstrated above have
 * required the user to click on the stars to register their rating. This may not
 * be the preferred user interaction - often times the preferred approach is to
 * simply allow for the rating to be registered by the user hovering their mouse
 * over the rating stars. This mode is also supported by the Rating control,
 * using the {@link #updateOnHoverProperty() update on hover} property, as such:
 * 
 * <pre>
 * {@code 
 * final Rating rating = new Rating();
 * rating.setUpdateOnHover(true);}</pre>
 * 
 * <p>It is also allowable to have a Rating control that both updates on hover
 * and allows for partial values: the 'golden fill' of the default graphics will
 * automatically follow the users mouse as they move it along the Rating scale.
 * To enable this, just set both properties to true.
 */
public class Rating extends ControlsFXControl {
    
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
        getStyleClass().setAll("rating"); //$NON-NLS-1$
        
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
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(Rating.class, "rating.css");
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
    private DoubleProperty rating = new SimpleDoubleProperty(this, "rating", 3); //$NON-NLS-1$
    
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
    private IntegerProperty max = new SimpleIntegerProperty(this, "max", 5); //$NON-NLS-1$
    
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
            orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL); //$NON-NLS-1$
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
    private BooleanProperty partialRating = new SimpleBooleanProperty(this, "partialRating", false); //$NON-NLS-1$
    
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
    private BooleanProperty updateOnHover = new SimpleBooleanProperty(this, "updateOnHover", false); //$NON-NLS-1$
    
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
