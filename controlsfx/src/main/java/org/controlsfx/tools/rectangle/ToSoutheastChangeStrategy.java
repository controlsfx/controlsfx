package org.controlsfx.tools.rectangle;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A strategy which enlarges an existing rectangle to the southeast.
 */
public class ToSoutheastChangeStrategy extends AbstractFixedPointChangeStrategy {

    /*
     * The new rectangle will have the existing rectangle's northwestern corner as a fixed corner. The other corner will
     * always be the current point (modulo the ratio which will be respected if enforced), which is handled by the
     * superclass.
     */

    // ATTRIBUTES

    /**
     * The new rectangle's northwestern corner.
     */
    private final Point2D northwesternCorner;

    // CONSTRUCTOR

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the southeast. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original the original rectangle
     * @param ratioFixed indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio the possibly fixed ratio of the rectangle created by this strategy
     */
    public ToSoutheastChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);
        northwesternCorner = new Point2D(original.getMinX(), original.getMinY());
    }

    // IMPLEMENTATION OF 'AbstractFixedPointChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point2D getFixedCorner() {
        return northwesternCorner;
    }

}
