package org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import org.controlsfx.tools.rectangle.Edge2D;
import org.controlsfx.tools.rectangle.Rectangles2D;

/**
 * Abstract superclass to those implementations of {@link Rectangle2DChangeStrategy} which computed their rectangle by spanning it
 * from a fixed edge to the parallel edge defined by the point given to {@link Rectangle2DChangeStrategy#continueChange(Point2D)
 * continueChange}. <br>
 * The edge is fixed during the change but can be changed in between changes. Implemented such that a ratio is respected
 * if specified.
 */
abstract class AbstractFixedEdgeChangeStrategy extends AbstractRatioRespectingChangeStrategy {

    // ATTRIBUTES

    /**
     * A rectangle which defines the bounds within which the previous rectangle can be moved.
     */
    private final Rectangle2D bounds;

    /**
     * The edge which is fixed during the change. In {@link #doBegin(Point2D)} it is set to {@link #getFixedEdge()}; in
     * {@link #doEnd(Point2D)} it is set to {@code null}.
     */
    private Edge2D fixedEdge;

    // CONSTRUCTOR

    /**
     * Creates a fixed edge change strategy. It respects the specified {@code ratio} if {@code ratioFixed} is
     * {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     * @param bounds
     *            the bounds within which the rectangle can be resized
     */
    protected AbstractFixedEdgeChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio);
        this.bounds = bounds;
    }

    // ABSTRACT METHODS

    /**
     * Returns the edge which is fixed during the change. Called once when the change begins.
     * 
     * @return the edge which is fixed during the change
     */
    protected abstract Edge2D getFixedEdge();

    // IMPLEMENTATION OF 'do...'

    /**
     * Creates a new rectangle from the two edges defined by {@link #fixedEdge} and its parallel through the
     * specified point.
     * 
     * @param point
     *            the point defining the parallel edge
     * @return the rectangle defined the two edges
     */
    private final Rectangle2D createFromEdges(Point2D point) {
        if (isRatioFixed())
            return Rectangles2D.forEdgeAndOpposingPointAndRatioWithinBounds(fixedEdge, point, getRatio(), bounds);
        else
            return Rectangles2D.forEdgeAndOpposingPoint(fixedEdge, point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doBegin(Point2D point) {
        fixedEdge = getFixedEdge();
        return createFromEdges(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Rectangle2D doContinue(Point2D point) {
        return createFromEdges(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doEnd(Point2D point) {
        Rectangle2D newRectangle = createFromEdges(point);
        fixedEdge = null;
        return newRectangle;
    }

}
