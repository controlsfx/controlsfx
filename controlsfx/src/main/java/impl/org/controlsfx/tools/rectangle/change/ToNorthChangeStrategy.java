package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Edge2D;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A strategy which enlarges an existing rectangle to the north.
 */
public class ToNorthChangeStrategy extends AbstractFixedEdgeChangeStrategy {

    /*
     * The new rectangle will have the existing rectangle's southern edge as a fixed edge. The parallel edge will
     * be defined by the current point (modulo the ratio which will be respected if enforced), which is handled by the
     * superclass.
     */

    // ATTRIBUTES

    /**
     * The new rectangle's southern edge.
     */
    private final Edge2D southernEdge;

    // CONSTRUCTOR

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the north. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original
     *            the original rectangle
     * @param ratioFixed
     *            indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio
     *            the possibly fixed ratio of the rectangle created by this strategy
     * @param bounds
     *            the bounds within which the rectangle can be resized
     */
    public ToNorthChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio, bounds);
        Point2D edgeCenterPoint = new Point2D((original.getMinX() + original.getMaxX()) / 2, original.getMaxY());
        southernEdge = new Edge2D(edgeCenterPoint, Orientation.HORIZONTAL, original.getMaxX() - original.getMinX());
    }

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the northeast. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original
     *            the original rectangle
     * @param ratioFixed
     *            indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio
     *            the possibly fixed ratio of the rectangle created by this strategy
     * @param maxX
     *            the maximal x-coordinate of the right edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's width
     * @param maxY
     *            the maximal y-coordinate of the lower edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's height
     */
    public ToNorthChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, double maxX, double maxY) {
        this(original, ratioFixed, ratio, new Rectangle2D(0, 0, maxX, maxY));
    }

    // IMPLEMENTATION OF 'AbstractFixedEdgeChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    protected Edge2D getFixedEdge() {
        return southernEdge;
    }

}
