package org.controlsfx.tools.rectangle;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to those implementations of {@link ChangeStrategy} which computed their rectangle by spanning it
 * from a fixed point to the point given to {@link ChangeStrategy#continueChange(Point2D) continueChange}. <br>
 * Implemented such that a ratio is respected if specified.
 */
abstract class AbstractFixedPointChangeStrategy extends AbstractRatioRespectingChangeStrategy {

    // CONSTRUCTOR

    /**
     * Creates a fixed corner change strategy. It respects the specified {@code ratio} if {@code ratioFixed} is
     * {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     */
    protected AbstractFixedPointChangeStrategy(boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);
    }

    // ABSTRACT METHODS

    /**
     * Returns the corner which is fixed during the change.
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
        if (isRatioFixed())
            return Rectangles2D.forDiagonalCornersAndRatio(getFixedCorner(), point, getRatio());
        else
            return Rectangles2D.forDiagonalCorners(getFixedCorner(), point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doBegin(Point2D point) {
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
        return createFromCorners(point);
    }

}
