package org.controlsfx.tools.rectangle;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A {@code Rectangle2DChangeStrategy} creates instances of {@link Rectangle2D} based on the coordinates of the begin,
 * continuation and end of an action. <br>
 * <br>
 */
public interface ChangeStrategy {

    /*
     * TODO Write better class comment:
     * - justification for the name 'change'
     * - proposed usage
     * - contract for begin -> continue -> end
     */

    /**
     * Begins the change at the specified point.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D beginChange(Point2D point);

    /**
     * Continues the change to the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D continueChange(Point2D point);

    /**
     * Ends the change at the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D endChange(Point2D point);

}
