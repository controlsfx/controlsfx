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

import impl.org.controlsfx.tools.MathTools;

import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Moves the rectangle around.
 */
public class MoveChangeStrategy extends AbstractPreviousRectangleChangeStrategy {

    /*
     * The previous rectangle will be moved around using a vector computed from the start to the current point.
     * The moved rectangle will be forced within defined bounds.
     */

    // ATTRIBUTES

    /**
     * A rectangle which defines the bounds within which the previous rectangle can be moved.
     */
    private final Rectangle2D bounds;

    /**
     * The starting point of the selection change. The move will be computed relative to this point.
     */
    private Point2D startingPoint;

    // CONSTRUCTORS

    /**
     * Creates a new change strategy which moves the specified rectangle within the specified bounds.
     * 
     * @param previous
     *            the previous rectangle this move is based on
     * @param bounds
     *            the bounds within which the rectangle can be moved
     */
    public MoveChangeStrategy(Rectangle2D previous, Rectangle2D bounds) {
        super(previous, false, 0);
        Objects.requireNonNull(bounds, "The specified bounds must not be null."); //$NON-NLS-1$
        this.bounds = bounds;
    }

    /**
     * Creates a new change strategy which moves the specified rectangle within the specified bounds defined by the
     * rectangle from {@code (0, 0)} to {@code (maxX, maxY)}.
     * 
     * @param previous
     *            the previous rectangle this move is based on
     * @param maxX
     *            the maximal x-coordinate of the right edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's width
     * @param maxY
     *            the maximal y-coordinate of the lower edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's height
     */
    public MoveChangeStrategy(Rectangle2D previous, double maxX, double maxY) {
        super(previous, false, 0);
        if (maxX < previous.getWidth()) {
            throw new IllegalArgumentException(
                    "The specified maximal x-coordinate must be greater than or equal to the previous rectangle's width."); //$NON-NLS-1$
        }
        if (maxY < previous.getHeight()) {
            throw new IllegalArgumentException(
                    "The specified maximal y-coordinate must be greater than or equal to the previous rectangle's height."); //$NON-NLS-1$
        }

        bounds = new Rectangle2D(0, 0, maxX, maxY);
    }

    // IMPLEMENTATION OF 'do...'

    /**
     * Moves the previous rectangle to the specified point relative to the {@link #startingPoint}.
     * 
     * @param point
     *            the vector from the {@link #startingPoint} to this point defines the movement
     * @return the moved rectangle
     */
    private final Rectangle2D moveRectangleToPoint(Point2D point) {

        /*
         * The computation makes sure that no part of the rectangle can be moved out the bounds.
         * To achieve this, the coordinates of the future rectangle's upper left corner are forced into the intervals
         *  - [boundsMinX, boundsMaxX - previousRectangleWidth],
         *  - [boundsMinY, boundsMaxY - previousRectangleHeight] respectively.
         */

        // vector from starting to specified point
        double xMove = point.getX() - startingPoint.getX();
        double yMove = point.getY() - startingPoint.getY();

        // upper left corner
        double upperLeftX = getPrevious().getMinX() + xMove;
        double upperLeftY = getPrevious().getMinY() + yMove;

        // upper bounds for upper left corner
        double maxX = bounds.getMaxX() - getPrevious().getWidth();
        double maxY = bounds.getMaxY() - getPrevious().getHeight();

        // corrected upper left corner
        double correctedUpperLeftX = MathTools.inInterval(bounds.getMinX(), upperLeftX, maxX);
        double correctedUpperLeftY = MathTools.inInterval(bounds.getMinY(), upperLeftY, maxY);

        // rectangle from corrected upper left corner with the previous rectangle's width and height
        return new Rectangle2D(
                correctedUpperLeftX, correctedUpperLeftY,
                getPrevious().getWidth(), getPrevious().getHeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doBegin(Point2D point) {
        this.startingPoint = point;
        return getPrevious();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doContinue(Point2D point) {
        return moveRectangleToPoint(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doEnd(Point2D point) {
        return moveRectangleToPoint(point);
    }

}
