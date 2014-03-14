package org.controlsfx.tools.rectangle.change;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A {@code Rectangle2DChangeStrategy} creates instances of {@link Rectangle2D} based on the coordinates of the begin,
 * continuation and end of an action. The behavior is undefined if these three methods are not called on the following
 * order:<br>
 * ({@link #beginChange(Point2D) begin} -> {@link #continueChange(Point2D) continue}* -> {@link #endChange(Point2D) end}
 * )* <br>
 * <br>
 * Most implementations will be creating new rectangles based on an existing one. If the created ones constantly replace
 * the original, this effectively "changes" the rectangle's appearance (note that {@link Rectangle2D} instances
 * themselves are immutable !). This interface and its implementations were created to easily allow a GUI user to change
 * an existing rectangle by typical resize and move operations.
 */
public interface Rectangle2DChangeStrategy {

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
