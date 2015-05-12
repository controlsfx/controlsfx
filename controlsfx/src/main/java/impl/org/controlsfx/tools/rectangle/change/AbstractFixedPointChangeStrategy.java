/**
 * Copyright (c) 2014, ControlsFX
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
package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Rectangles2D;

import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to those implementations of {@link Rectangle2DChangeStrategy} which compute their rectangle by
 * spanning it from a fixed point to the point given to {@link Rectangle2DChangeStrategy#continueChange(Point2D)
 * continueChange}. <br>
 * The point is fixed during the change but can be changed in between changes. Implemented such that a ratio is
 * respected if specified.
 */
abstract class AbstractFixedPointChangeStrategy extends AbstractRatioRespectingChangeStrategy {

    // ATTRIBUTES

    /**
     * A rectangle which defines the bounds within which the new rectangle must be contained.
     */
    private final Rectangle2D bounds;

    /**
     * The point which is fixed during the change. In {@link #doBegin(Point2D)} it is set to {@link #getFixedCorner()};
     * in {@link #doEnd(Point2D)} it is set to {@code null}.
     */
    private Point2D fixedCorner;

    // CONSTRUCTOR

    /**
     * Creates a fixed corner change strategy. It respects the specified {@code ratio} if {@code ratioFixed} is
     * {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     * @param bounds
     *            the bounds within which the new rectangle must be contained
     */
    protected AbstractFixedPointChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio);
        Objects.requireNonNull(bounds, "The argument 'bounds' must not be null."); //$NON-NLS-1$

        this.bounds = bounds;
    }

    // ABSTRACT METHODS

    /**
     * Returns the corner which is fixed during the change. Called once when the change begins.
     * 
     * @return the corner which is fixed during the change
     */
    protected abstract Point2D getFixedCorner();

    // IMPLEMENTATION OF 'do...'

    /**
     * Creates a new rectangle from the two corners defined by {@link #getFixedCorner()} and the specified point.
     * 
     * @param point
     *            the second corner
     * @return the rectangle defined the two corners
     */
    private final Rectangle2D createFromCorners(Point2D point) {
        Point2D pointInBounds = Rectangles2D.inRectangle(bounds, point);

        if (isRatioFixed()) {
            return Rectangles2D.forDiagonalCornersAndRatio(fixedCorner, pointInBounds, getRatio());
        } else {
            return Rectangles2D.forDiagonalCorners(fixedCorner, pointInBounds);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doBegin(Point2D point) {
        boolean startPointNotInBounds = !bounds.contains(point);
        if (startPointNotInBounds) {
            throw new IllegalArgumentException(
                    "The change's start point (" + point + ") must lie within the bounds (" + bounds + ")."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        fixedCorner = getFixedCorner();
        return createFromCorners(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doContinue(Point2D point) {
        return createFromCorners(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doEnd(Point2D point) {
        Rectangle2D newRectangle = createFromCorners(point);
        fixedCorner = null;
        return newRectangle;
    }

}
