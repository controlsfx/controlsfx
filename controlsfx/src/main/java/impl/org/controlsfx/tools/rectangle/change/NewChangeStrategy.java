package impl.org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;

/**
 * A strategy which creates a new rectangle.
 */
public class NewChangeStrategy extends AbstractFixedPointChangeStrategy {

    /*
     * The new selection will have the starting point as a fixed corner. The other corner will always be the current
     * point modulo the ratio which will be respected if enforced. Both is handled by the superclass.
     */

    // ATTRIBUTES

    /**
     * The starting point of this change.
     */
    private Point2D startingPoint;

    // CONSTRUCTOR

    /**
     * Creates a change strategy which creates a new rectangle. It respects the specified {@code ratio} if
     * {@code ratioFixed} is {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     */
    public NewChangeStrategy(boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeBeginHook(Point2D point) {
        startingPoint = point;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point2D getFixedCorner() {
        return startingPoint;
    }

}
