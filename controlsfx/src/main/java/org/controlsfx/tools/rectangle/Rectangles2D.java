package org.controlsfx.tools.rectangle;

import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import org.controlsfx.tools.MathTools;

/**
 * Usability methods for rectangles.
 */
public class Rectangles2D {

    /*
     * TWO CORNERS
     */

    /**
     * Creates a new rectangle with the two specified corners. The two corners will be interpreted as being diagonal of
     * each other.
     * 
     * @param oneCorner
     *            one corner
     * @param diagonalCorner
     *            another corner, diagonal from the first
     * @return the {@link Rectangle2D} which is defined by the two corners
     */
    public static Rectangle2D forDiagonalCorners(Point2D oneCorner, Point2D diagonalCorner) {
        double minX = Math.min(oneCorner.getX(), diagonalCorner.getX());
        double minY = Math.min(oneCorner.getY(), diagonalCorner.getY());
        double width = Math.abs(oneCorner.getX() - diagonalCorner.getX());
        double height = Math.abs(oneCorner.getY() - diagonalCorner.getY());

        return new Rectangle2D(minX, minY, width, height);
    }

    /*
     * CORNER AND SIZE
     */

    /**
     * Creates a new rectangle with the specified {@code upperLeft} corner and the specified {@code width} and
     * {@code height}.
     * 
     * @param upperLeft
     *            one corner
     * @param width
     *            the new rectangle's width
     * @param height
     *            the new rectangle's height
     * @return the {@link Rectangle2D} which is defined by the specified upper left corner and width and height
     */
    public static Rectangle2D forUpperLeftCornerAndSize(Point2D upperLeft, double width, double height) {
        return new Rectangle2D(upperLeft.getX(), upperLeft.getY(), width, height);
    }

    /*
     * CORNER AND RATIO
     */

    /**
     * Creates a new rectangle with the two specified corners. The two corners will be interpreted as being diagonal of
     * each other. The returned rectangle will have the specified {@code fixedCorner} as its corner. The other one will
     * either be on the same x- or y-parallel as the {@code diagonalCorner} but will be such that the rectangle has the
     * specified {@code ratio}.
     * 
     * @param fixedCorner
     *            one corner
     * @param diagonalCorner
     *            another corner, diagonal from the first
     * @param ratio
     *            the ratio the returned rectangle must have
     * @return the {@link Rectangle2D} which is defined by the {@code fixedCorner}, the x- or y-parallel of the
     *         {@code diagonalCorner} and the {@code ratio}
     */
    public static Rectangle2D forDiagonalCornersAndRatio(Point2D fixedCorner, Point2D diagonalCorner, double ratio) {
        // the coordinate differences - note that they can be negative
        double xDifference = diagonalCorner.getX() - fixedCorner.getX();
        double yDifference = diagonalCorner.getY() - fixedCorner.getY();

        // the following calls will only change one of the two differences:
        // the one whose value is too large compared to what it should be based on the other difference and the ratio;
        // its value will instead be the other difference time or divided by the ratio
        double xDifferenceByRatio = correctDifferenceByRatio(xDifference, yDifference, ratio);
        double yDifferenceByRatio = correctDifferenceByRatio(yDifference, xDifference, 1 / ratio);

        // these are the coordinates of the upper left corner of the future rectangle
        double minX = getMinCoordinate(fixedCorner.getX(), xDifferenceByRatio);
        double minY = getMinCoordinate(fixedCorner.getY(), yDifferenceByRatio);

        double width = Math.abs(xDifferenceByRatio);
        double height = Math.abs(yDifferenceByRatio);

        return new Rectangle2D(minX, minY, width, height);
    }

    /**
     * Returns the difference with the following properties:<br>
     * - it has the same sign as the specified difference <br>
     * - its absolute value is the minimum of the absolute values of ...<br>
     * ... the specified difference and <br>
     * ... the product of the specified ratio and the other specified difference <br>
     * 
     * @param difference
     *            the difference to check
     * @param otherDifference
     *            the other difference
     * @param ratioAsMultiplier
     *            the ratio as a multiplier for the other difference
     * @return the corrected difference
     */
    private static double correctDifferenceByRatio(double difference, double otherDifference, double ratioAsMultiplier) {
        double differenceByRatio = otherDifference * ratioAsMultiplier;
        double correctedDistance = Math.min(Math.abs(difference), Math.abs(differenceByRatio));

        return correctedDistance * Math.signum(difference);
    }

    /**
     * Returns the minimum coordinate such that a rectangle starting from that coordinate will contain the fixed
     * coordinate as a corner.
     * 
     * @param fixedCoordinate
     *            the coordinate which must be a corner
     * @param difference
     *            the difference in the computed coordinate
     * @return fixedCoordinate + difference; if difference < 0 <br>
     *         fixedCoordinate; else
     */
    private static double getMinCoordinate(double fixedCoordinate, double difference) {
        if (difference < 0)
            return fixedCoordinate + difference;

        return fixedCoordinate;
    }

    /*
     * CENTER AND SIZE
     */

    /**
     * Creates a new rectangle with the specified center and the specified width and height.
     * 
     * @param centerPoint
     *            the center point o the new rectangle
     * @param width
     *            the width of the new rectangle
     * @param height
     *            the height of the new rectangle
     * @return a rectangle with the specified center and size
     */
    public static Rectangle2D forCenterAndSize(Point2D centerPoint, double width, double height) {
        double minX = centerPoint.getX() - width / 2;
        double minY = centerPoint.getY() - height / 2;

        return new Rectangle2D(minX, minY, width, height);
    }

    /*
     * ORIGINAL, AREA AND RATIO
     */

    /**
     * Creates a new rectangle with the same center point and area as the specified {@code original} rectangle with the
     * specified {@code ratio}.
     * 
     * @param original
     *            the original rectangle
     * @param ratio
     *            the new ratio
     * @return a new {@link Rectangle2D} with the same center point as the {@code original} and the specified
     *         {@code ratio}; it has the same area as the {@code original}
     * @throws NullPointerException
     *             if the {@code original} rectangle is null
     */
    public static Rectangle2D fixRatio(Rectangle2D original, double ratio) {
        Objects.requireNonNull(original, "The specified original rectangle must not be null.");

        return createWithFixedRatioWithinBounds(original, ratio, null);
    }

    /**
     * Creates a new rectangle with the same center point and area (if possible) as the specified {@code original}
     * rectangle with the specified {@code ratio} and respecting the specified {@code bounds}.
     * 
     * @param original
     *            the original rectangle
     * @param ratio
     *            the new ratio
     * @param bounds
     *            the bounds within which the new rectangle will be located
     * @return a new {@link Rectangle2D} with the same center point as the {@code original} and the specified
     *         {@code ratio}; it has the same area as the {@code original} unless this would violate the bounds; in this
     *         case it is as large as possible while still staying within the bounds
     * @throws NullPointerException
     *             if the {@code original} or {@code bounds} rectangle is null
     * @throws IllegalArgumentException
     *             if the {@code original} rectangle's center point is out of the bounds
     */
    public static Rectangle2D fixRatioWithinBounds(Rectangle2D original, double ratio, Rectangle2D bounds) {
        Objects.requireNonNull(original, "The specified original rectangle must not be null.");
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");

        return createWithFixedRatioWithinBounds(original, ratio, bounds);
    }

    /**
     * Creates a new rectangle with the same center point and area as the specified {@code original} rectangle with the
     * specified {@code ratio} and respecting the specified {@code bounds} (if not-null).
     * 
     * @param original
     *            the original rectangle
     * @param ratio
     *            the new ratio
     * @param bounds
     *            the bounds within which the new rectangle will be located
     * @return a new {@link Rectangle2D} with the same center point as the {@code original} and the specified
     *         {@code ratio}; it has the same area as the {@code original} unless this would violate the bounds; in this
     *         case it is as large as possible while still staying within the bounds
     * @throws IllegalArgumentException
     *             if the {@code original} rectangle's center point is out of the {@code bounds}
     */
    private static Rectangle2D createWithFixedRatioWithinBounds(Rectangle2D original, double ratio, Rectangle2D bounds) {
        double centerX = original.getMinX() + original.getWidth() / 2;
        double centerY = original.getMinY() + original.getHeight() / 2;
        Point2D centerPoint = new Point2D(centerX, centerY);

        boolean centerPointInBounds = bounds == null || bounds.contains(centerPoint);
        if (!centerPointInBounds)
            throw new IllegalArgumentException("The center point " + centerPoint
                    + " of the original rectangle is out of the specified bounds.");

        double area = original.getWidth() * original.getHeight();

        return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, bounds);
    }

    /*
     * CENTER, AREA AND RATIO
     */

    /**
     * Creates a new rectangle with the specified {@code centerPoint}, {@code area} and {@code ratio}.
     * 
     * @param centerPoint
     *            the new rectangle's center point
     * @param area
     *            the new rectangle's area
     * @param ratio
     *            the new ratio
     * @return a new {@link Rectangle2D} with the specified {@code centerPoint}, {@code area} and {@code ratio}
     * @throws IllegalArgumentException
     *             if the {@code centerPoint} is out of the {@code bounds}
     */
    public static Rectangle2D forCenterAreaAndRatio(Point2D centerPoint, double area, double ratio) {
        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
        if (area < 0)
            throw new IllegalArgumentException("The specified area " + area + " must be larger than zero.");
        if (ratio < 0)
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");

        return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, null);
    }

    /**
     * Creates a new rectangle with the specified {@code centerPoint}, {@code area} (if possible) and {@code ratio},
     * respecting the specified {@code bounds}.
     * 
     * @param centerPoint
     *            the new rectangle's center point
     * @param area
     *            the new rectangle's area (if possible without violating the bounds)
     * @param ratio
     *            the new ratio
     * @param bounds
     *            the bounds within which the new rectangle will be located
     * @return a new {@link Rectangle2D} with the specified {@code centerPoint} and {@code ratio}; it has the specified
     *         {@code area} unless this would violate the {@code bounds}; in this case it is as large as possible while
     *         still staying within the bounds
     * @throws IllegalArgumentException
     *             if the {@code centerPoint} is out of the {@code bounds}
     */
    public static Rectangle2D forCenterAreaAndRatioWithinBounds(Point2D centerPoint, double area, double ratio,
            Rectangle2D bounds) {
        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");
        boolean centerPointInBounds = bounds.contains(centerPoint);
        if (!centerPointInBounds)
            throw new IllegalArgumentException("The center point " + centerPoint
                    + " of the original rectangle is out of the specified bounds.");
        if (area < 0)
            throw new IllegalArgumentException("The specified area " + area + " must be larger than zero.");
        if (ratio < 0)
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero.");

        return createForCenterAreaAndRatioWithinBounds(centerPoint, area, ratio, bounds);
    }

    /**
     * Creates a new rectangle with the specified {@code centerPoint}, {@code area} (if possible) and {@code ratio},
     * respecting the specified {@code bounds} (if not-null).
     * 
     * @param centerPoint
     *            the new rectangle's center point
     * @param area
     *            the new rectangle's area (if possible without violating the bounds)
     * @param ratio
     *            the new ratio
     * @param bounds
     *            the bounds within which the new rectangle will be located
     * @return a new {@link Rectangle2D} with the specified {@code centerPoint} and {@code ratio}; it has the specified
     *         {@code area} unless this would violate the {@code bounds}; in this case it is as large as possible while
     *         still staying within the bounds
     * @throws IllegalArgumentException
     *             if the {@code centerPoint} is out of the {@code bounds}
     */
    private static Rectangle2D createForCenterAreaAndRatioWithinBounds(Point2D centerPoint, double area, double ratio,
            Rectangle2D bounds) {

        double newWidth = Math.sqrt(area * ratio);
        double newHeight = area / newWidth;

        boolean boundsSpecified = bounds != null;
        if (boundsSpecified) {
            double reductionFactor = lengthReductionToStayWithinBounds(centerPoint, newWidth, newHeight, bounds);
            newWidth *= reductionFactor;
            newHeight *= reductionFactor;
        }

        return Rectangles2D.forCenterAndSize(centerPoint, newWidth, newHeight);
    }

    /**
     * Computes the factor by which the specified width and height must be multiplied to keep a rectangle with their
     * ratio and the specified center point within the specified bounds.
     * 
     * @param centerPoint
     *            the center point of the new rectangle
     * @param width
     *            the original width which might be too large
     * @param height
     *            the original height which might be too large
     * @param bounds
     *            the bounds within which the new rectangle will be located
     * @return the factor with which the width and height must be multiplied to stay within the bounds; always in the
     *         closed interval [0; 1]
     * @throws IllegalArgumentException
     *             if the {@code centerPoint} is out of the {@code bounds}; if {@code width} or {@code height} are not
     *             larger than zero
     */
    private static double lengthReductionToStayWithinBounds(
            Point2D centerPoint, double width, double height, Rectangle2D bounds) {

        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null.");
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null.");
        boolean centerPointInBounds = bounds.contains(centerPoint);
        if (!centerPointInBounds)
            throw new IllegalArgumentException("The center point " + centerPoint
                    + " of the original rectangle is out of the specified bounds.");
        if (width < 0)
            throw new IllegalArgumentException("The specified width " + width + " must be larger than zero.");
        if (height < 0)
            throw new IllegalArgumentException("The specified height " + height + " must be larger than zero.");

        /*
         * Compute the center point's distance to all edges. The width and height must be reduced (by the returned
         * factor) such that their halves (!) are not greater than those distances. This can be done by finding the
         * minimum ratio between the distance and the halved width/height.
         */

        double distanceToEast = Math.abs(centerPoint.getX() - bounds.getMinX());
        double distanceToWest = Math.abs(centerPoint.getX() - bounds.getMaxX());
        double distanceToNorth = Math.abs(centerPoint.getY() - bounds.getMinY());
        double distanceToSouth = Math.abs(centerPoint.getY() - bounds.getMaxY());

        // the returned factor must not be greater than one; otherwise the size would increase
        return MathTools.min(1,
                distanceToEast / width * 2, distanceToWest / width * 2,
                distanceToNorth / height * 2, distanceToSouth / height * 2);
    }

}
