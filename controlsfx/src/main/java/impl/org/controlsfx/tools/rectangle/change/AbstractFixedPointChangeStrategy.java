package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Rectangles2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to those implementations of {@link Rectangle2DChangeStrategy} which computed their rectangle by spanning it
 * from a fixed point to the point given to {@link Rectangle2DChangeStrategy#continueChange(Point2D) continueChange}. <br>
 * The point is fixed during the change but can be changed in between changes. Implemented such that a ratio is respected
 * if specified.
 */
abstract class AbstractFixedPointChangeStrategy extends AbstractRatioRespectingChangeStrategy {

    // ATTRIBUTES

    /**
     * The point which is fixed during the change. In {@link #doBegin(Point2D)} it is set to {@link #getFixedCorner()}; in
     * {@link #doEnd(Point2D)} it is set to {@code null}.
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
     */
    protected AbstractFixedPointChangeStrategy(boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);
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
        if (isRatioFixed())
            return Rectangles2D.forDiagonalCornersAndRatio(fixedCorner, point, getRatio());
        else
            return Rectangles2D.forDiagonalCorners(fixedCorner, point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Rectangle2D doBegin(Point2D point) {
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
