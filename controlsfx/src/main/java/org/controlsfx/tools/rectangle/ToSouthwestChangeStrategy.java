package org.controlsfx.tools.rectangle;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A strategy which enlarges an existing rectangle to the southwest.
 * 
 * @author pan
 */
public class ToSouthwestChangeStrategy extends AbstractFixedPointChangeStrategy {

    /*
     * The new rectangle will have the existing rectangle's northeastern corner as a fixed corner. The other corner will
     * always be the current point (modulo the ratio which will be respected if enforced), which is handled by the
     * superclass.
     */

    // ATTRIBUTES

    /**
     * The new rectangle's northeastern corner.
     */
    private final Point2D northeasternCorner;

    // CONSTRUCTOR

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the southwest. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original the original rectangle
     * @param ratioFixed indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio the possibly fixed ratio of the rectangle created by this strategy
     */
    public ToSouthwestChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);
        northeasternCorner = new Point2D(original.getMaxX(), original.getMinY());
    }

    // IMPLEMENTATION OF 'AbstractFixedPointChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point2D getFixedCorner() {
        return northeasternCorner;
    }

}
