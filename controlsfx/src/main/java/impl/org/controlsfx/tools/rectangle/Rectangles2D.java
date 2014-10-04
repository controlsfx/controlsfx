/**
 * Copyright (c) 2014, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package impl.org.controlsfx.tools.rectangle;

import impl.org.controlsfx.tools.MathTools;

import java.util.Objects;

import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Usability methods for rectangles.
 */
public class Rectangles2D {

    /*
     * CHECKS
     */

    /**
     * Indicates whether the specified rectangle contains the specified edge.
     * 
     * @param rectangle
     *            the rectangle to check
     * @param edge
     *            the edge to check
     * @return {@code true} if both end points of the edge are {@link Rectangle2D#contains(Point2D) contained} in the
     *         rectangle
     */
    public static boolean contains(Rectangle2D rectangle, Edge2D edge) {
        Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(edge, "The argument 'edge' must not be null."); //$NON-NLS-1$

        boolean edgeInBounds = rectangle.contains(edge.getUpperLeft()) && rectangle.contains(edge.getLowerRight());
        return edgeInBounds;
    }

    /*
     * POINT
     */

    /**
     * Moves the specified point into the specified rectangle. If the point is already with the rectangle, it is
     * returned. Otherwise the point in the rectangle which is closest to the specified one is returned.
     * 
     * @param rectangle
     *            the {@link Rectangle2D} into which the point should be moved
     * @param point
     *            the {@link Point2D} which is checked
     * @return either the specified {@code point} or the {@link Point2D} which is closest to it while still being
     *         contained on the {@code rectangle}
     */
    public static Point2D inRectangle(Rectangle2D rectangle, Point2D point) {
        Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(point, "The argument 'point' must not be null."); //$NON-NLS-1$

        if (rectangle.contains(point)) {
            return point;
        }

        // force the x and y coordinate into the rectangle
        double newX = MathTools.inInterval(rectangle.getMinX(), point.getX(), rectangle.getMaxX());
        double newY = MathTools.inInterval(rectangle.getMinY(), point.getY(), rectangle.getMaxY());
        return new Point2D(newX, newY);
    }

    /**
     * Returns the center of the specified rectangle as a point.
     * 
     * @param rectangle
     *            the {@link Rectangle2D} whose center point will be returned
     * @return the {@link Point2D} whose x/y coordinates lie at {@code (min + max) / 2}.
     */
    public static Point2D getCenterPoint(Rectangle2D rectangle) {
        Objects.requireNonNull(rectangle, "The argument 'rectangle' must not be null."); //$NON-NLS-1$

        double centerX = (rectangle.getMinX() + rectangle.getMaxX()) / 2;
        double centerY = (rectangle.getMinY() + rectangle.getMaxY()) / 2;
        return new Point2D(centerX, centerY);
    }

    /*
     * OTHER RECTANGLE
     */

    /**
     * Returns the rectangle which represents the intersection of the two specified rectangles.
     * 
     * @param a
     *            a {@link Rectangle2D}
     * @param b
     *            another {@link Rectangle2D}
     * @return a {@link Rectangle2D} which is the intersection of {@code a} and {@code b}; possible
     *         {@link Rectangle2D#EMPTY}.
     */
    public static Rectangle2D intersection(Rectangle2D a, Rectangle2D b) {
        Objects.requireNonNull(a, "The argument 'a' must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(b, "The argument 'b' must not be null."); //$NON-NLS-1$

        if (a.intersects(b)) {
            double intersectionMinX = Math.max(a.getMinX(), b.getMinX());
            double intersectionMaxX = Math.min(a.getMaxX(), b.getMaxX());
            double intersectionWidth = intersectionMaxX - intersectionMinX;
            double intersectionMinY = Math.max(a.getMinY(), b.getMinY());
            double intersectionMaxY = Math.min(a.getMaxY(), b.getMaxY());
            double intersectionHeight = intersectionMaxY - intersectionMinY;
            return new Rectangle2D(intersectionMinX, intersectionMinY, intersectionWidth, intersectionHeight);
        } else {
            return Rectangle2D.EMPTY;
        }
    }

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
        Objects.requireNonNull(oneCorner, "The specified corner must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(diagonalCorner, "The specified diagonal corner must not be null."); //$NON-NLS-1$

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
     *            the ratio the returned rectangle must have; must be non-negative
     * @return the {@link Rectangle2D} which is defined by the {@code fixedCorner}, the x- or y-parallel of the
     *         {@code diagonalCorner} and the {@code ratio}
     */
    public static Rectangle2D forDiagonalCornersAndRatio(Point2D fixedCorner, Point2D diagonalCorner, double ratio) {
        Objects.requireNonNull(fixedCorner, "The specified fixed corner must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(diagonalCorner, "The specified diagonal corner must not be null."); //$NON-NLS-1$
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // the coordinate differences - note that they can be negative
        double xDifference = diagonalCorner.getX() - fixedCorner.getX();
        double yDifference = diagonalCorner.getY() - fixedCorner.getY();

        // the following calls will only change one of the two differences:
        // the one whose value is too large compared to what it should be based on the other difference and the ratio;
        // its value will instead be the other difference time or divided by the ratio
        double xDifferenceByRatio = correctCoordinateDifferenceByRatio(xDifference, yDifference, ratio);
        double yDifferenceByRatio = correctCoordinateDifferenceByRatio(yDifference, xDifference, 1 / ratio);

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
    private static double correctCoordinateDifferenceByRatio(double difference, double otherDifference,
            double ratioAsMultiplier) {
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
        if (difference < 0) {
            return fixedCoordinate + difference;
        }

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
        Objects.requireNonNull(centerPoint, "The specified center point must not be null."); //$NON-NLS-1$

        double absoluteWidth = Math.abs(width);
        double absoluteHeight = Math.abs(height);
        double minX = centerPoint.getX() - absoluteWidth / 2;
        double minY = centerPoint.getY() - absoluteHeight / 2;

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
        Objects.requireNonNull(original, "The specified original rectangle must not be null."); //$NON-NLS-1$
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

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
        Objects.requireNonNull(original, "The specified original rectangle must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null."); //$NON-NLS-1$
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return createWithFixedRatioWithinBounds(original, ratio, bounds);
    }

    /**
     * Creates a new rectangle with the same center point and area as the specified {@code original} rectangle with the
     * specified {@code ratio} and respecting the specified {@code bounds} (if not-{@code null}).
     * 
     * @param original
     *            the original rectangle
     * @param ratio
     *            the new ratio
     * @param bounds
     *            the bounds within which the new rectangle will be located; might be {@code null}
     * @return a new {@link Rectangle2D} with the same center point as the {@code original} and the specified
     *         {@code ratio}; it has the same area as the {@code original} unless this would violate the bounds; in this
     *         case it is as large as possible while still staying within the bounds
     * @throws IllegalArgumentException
     *             if the {@code original} rectangle's center point is out of the {@code bounds}
     */
    private static Rectangle2D createWithFixedRatioWithinBounds(Rectangle2D original, double ratio, Rectangle2D bounds) {
        Point2D centerPoint = getCenterPoint(original);

        boolean centerPointInBounds = bounds == null || bounds.contains(centerPoint);
        if (!centerPointInBounds) {
            throw new IllegalArgumentException("The center point " + centerPoint //$NON-NLS-1$
                    + " of the original rectangle is out of the specified bounds."); //$NON-NLS-1$
        }

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
    public static Rectangle2D forCenterAndAreaAndRatio(Point2D centerPoint, double area, double ratio) {
        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null."); //$NON-NLS-1$
        if (area < 0) {
            throw new IllegalArgumentException("The specified area " + area + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

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
    public static Rectangle2D forCenterAndAreaAndRatioWithinBounds(
            Point2D centerPoint, double area, double ratio, Rectangle2D bounds) {

        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null."); //$NON-NLS-1$
        boolean centerPointInBounds = bounds.contains(centerPoint);
        if (!centerPointInBounds) {
            throw new IllegalArgumentException("The center point " + centerPoint //$NON-NLS-1$
                    + " of the original rectangle is out of the specified bounds."); //$NON-NLS-1$
        }
        if (area < 0) {
            throw new IllegalArgumentException("The specified area " + area + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

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
    private static Rectangle2D createForCenterAreaAndRatioWithinBounds(
            Point2D centerPoint, double area, double ratio, Rectangle2D bounds) {

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

        Objects.requireNonNull(centerPoint, "The specified center point of the new rectangle must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(bounds, "The specified bounds for the new rectangle must not be null."); //$NON-NLS-1$
        boolean centerPointInBounds = bounds.contains(centerPoint);
        if (!centerPointInBounds) {
            throw new IllegalArgumentException("The center point " + centerPoint //$NON-NLS-1$
                    + " of the original rectangle is out of the specified bounds."); //$NON-NLS-1$
        }
        if (width < 0) {
            throw new IllegalArgumentException("The specified width " + width + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (height < 0) {
            throw new IllegalArgumentException("The specified height " + height + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

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

    /*
     * EDGES
     */

    /**
     * Returns a rectangle that has the specified edge and has its opposing edge on the parallel axis defined by the
     * specified point's X or Y coordinate (depending on the edge's orientation).
     * 
     * @param edge
     *            the edge which will be contained in the returned rectangle
     * @param point
     *            the point whose X or Y coordinate defines the other edge
     * @return a rectangle
     */
    public static Rectangle2D forEdgeAndOpposingPoint(Edge2D edge, Point2D point) {
        double otherDimension = edge.getOrthogonalDifference(point);
        return createForEdgeAndOtherDimension(edge, otherDimension);
    }

    /**
     * Returns a rectangle that is principally defined by the specified edge and point. It should have the specified
     * edge as one of its own and its parallel edge should contain the point. While this would already well-define the
     * rectangle (compare {@link #forEdgeAndOpposingPoint(Edge2D, Point2D) forEdgeAndOpposingPoint}) the additionally
     * specified ratio and bounds have precedence over these arguments:<br>
     * The returned rectangle will have the ratio and will be within the bounds. If the bounds make it possible, the
     * specified point will lie on the edge parallel to the specified one. In order to maintain the ratio, this will
     * make it necessary to not use the specified edge but instead one with a different length. The new edge will have
     * the same center point as the specified one.<br>
     * This results on the following behavior: As the point is moved closer to or further away from the edge, the
     * resulting rectangle shrinks and grows while being anchored to the specified edge's center point and keeping the
     * ratio. This is limited by the bounds.
     * 
     * @param edge
     *            the edge which defines the center point and orientation of one of the rectangle's edges; must be
     *            within the specified {@code bounds}
     * @param point
     *            the point to which the rectangle spans if ratio and bounds allow it
     * @param ratio
     *            the ratio the new rectangle must have
     * @param bounds
     *            the bounds within which the new rectangle must lie
     * @return a rectangle
     */
    public static Rectangle2D forEdgeAndOpposingPointAndRatioWithinBounds(
            Edge2D edge, Point2D point, double ratio, Rectangle2D bounds) {

        Objects.requireNonNull(edge, "The specified edge must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(point, "The specified point must not be null."); //$NON-NLS-1$
        Objects.requireNonNull(bounds, "The specified bounds must not be null."); //$NON-NLS-1$

        boolean edgeInBounds = contains(bounds, edge);
        if (!edgeInBounds) {
            throw new IllegalArgumentException(
                    "The specified edge " + edge + " is not entirely contained on the specified bounds."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (ratio < 0) {
            throw new IllegalArgumentException("The specified ratio " + ratio + " must be larger than zero."); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /*
         * 1. move the point into the bounds
         * 2. create an edge whose length matches the distance to the point and the ratio
         * 3. correct that edge so that it lies within the bounds
         * 4. create a new rectangle from that edge and the ratio
         */

        Point2D boundedPoint = movePointIntoBounds(point, bounds);
        Edge2D unboundedEdge = resizeEdgeForDistanceAndRatio(edge, boundedPoint, ratio);
        Edge2D boundedEdge = resizeEdgeForBounds(unboundedEdge, bounds);

        // when computing the other dimension, note that the sign of the original difference between edge and point is
        // important; otherwise the "direction" of the resize is wrong
        double otherDimension = Math.signum(boundedEdge.getOrthogonalDifference(boundedPoint));
        if (boundedEdge.isHorizontal()) {
            // edge horizontal -> width fixed -> use length to compute height
            otherDimension *= boundedEdge.getLength() / ratio;
        } else {
            // edge vertical -> height fixed -> use length to compute width
            otherDimension *= boundedEdge.getLength() * ratio;
        }

        return createForEdgeAndOtherDimension(boundedEdge, otherDimension);
    }

    /**
     * Returns either the specified point if if the specified bounds {@link Rectangle2D#contains(Point2D) contain} it or
     * a point whose X and/or Y coordinates are moved into the bounds.
     * 
     * @param point
     *            the point to move into the {@code bounds}
     * @param bounds
     *            the bounds into which the {@code point} will be moved
     * @return either {@code point} or a new {@link Point2D} whose coordinates were changed so that it lies within the
     *         {@code bounds}
     */
    private static Point2D movePointIntoBounds(Point2D point, Rectangle2D bounds) {
        if (bounds.contains(point)) {
            return point;
        } else {
            double boundedPointX = MathTools.inInterval(bounds.getMinX(), point.getX(), bounds.getMaxX());
            double boundedPointY = MathTools.inInterval(bounds.getMinY(), point.getY(), bounds.getMaxY());
            return new Point2D(boundedPointX, boundedPointY);
        }
    }

    /**
     * Returns an edge with the same center point and orientation as the specified edge. Its length has the specified
     * ratio to the distance of the edge and the specified point.
     * 
     * @param edge
     *            the edge whose center point and orientation defines the returned edge's center point and orientation
     * @param point
     *            the point to which the distance is measured
     * @param ratio
     *            the ratio between the distance to the {@code point} and the returned edge's length
     * @return an {@link Edge2D}
     */
    private static Edge2D resizeEdgeForDistanceAndRatio(Edge2D edge, Point2D point, double ratio) {
        double distance = Math.abs(edge.getOrthogonalDifference(point));
        if (edge.isHorizontal()) {
            // a horizontal edge's length lies in the X axis; the distance lies in the Y axis: x = y * ratio
            double xLength = distance * ratio;
            return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), xLength);
        } else {
            // a vertical edge's length lies in the Y axis; the distance lies in the X axis: y = x / ratio
            double yLength = distance / ratio;
            return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), yLength);
        }
    }

    /**
     * Returns an edge with the same center point and orientation as the specified edge. If necessary, its length is
     * reduced to fit within the bounds.
     * 
     * @param edge
     *            the edge whose center point and orientation defines the returned edge's center point and orientation;
     *            the center point must be within the bounds or an {@link IllegalArgumentException} will be thrown
     * @param bounds
     *            the bounds within which the returned edge must be contained
     * @return either the specified {@code edge} if it is with in the {@code bounds} or one with a corrected length
     * @throws IllegalArgumentException
     *             if the {@code edge}'s center point is out of {@code bounds}
     */
    private static Edge2D resizeEdgeForBounds(Edge2D edge, Rectangle2D bounds) {
        // return the same edge if it is in the bounds
        boolean edgeInBounds = contains(bounds, edge);
        if (edgeInBounds) {
            return edge;
        }

        // make sure the bounds contain the edge's center point
        boolean centerPointInBounds = bounds.contains(edge.getCenterPoint());
        if (!centerPointInBounds) {
            throw new IllegalArgumentException(
                    "The specified edge's center point (" + edge + ") is out of the specified bounds (" + bounds + ")."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        if (edge.isHorizontal()) {
            // compute the length bounds for the left and right part of the edge
            double leftPartLengthBound = Math.abs(bounds.getMinX() - edge.getCenterPoint().getX());
            double rightPartLengthBound = Math.abs(bounds.getMaxX() - edge.getCenterPoint().getX());
            // compute the length of the left and right parts of the edge
            double leftPartLength = MathTools.inInterval(0, edge.getLength() / 2, leftPartLengthBound);
            double rightPartLength = MathTools.inInterval(0, edge.getLength() / 2, rightPartLengthBound);
            // compute the total length as double of the smaller length
            double horizontalLength = Math.min(leftPartLength, rightPartLength) * 2;
            return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), horizontalLength);
        } else {
            // compute the length bounds for the lower and upper part of the edge
            double lowerPartLengthBound = Math.abs(bounds.getMinY() - edge.getCenterPoint().getY());
            double upperPartLengthBound = Math.abs(bounds.getMaxY() - edge.getCenterPoint().getY());
            // compute the length of the lower and upper part of the edge
            double lowerPartLength = MathTools.inInterval(0, edge.getLength() / 2, lowerPartLengthBound);
            double upperPartLength = MathTools.inInterval(0, edge.getLength() / 2, upperPartLengthBound);
            // compute the total length as double of the smaller length
            double verticalLength = Math.min(lowerPartLength, upperPartLength) * 2;
            return new Edge2D(edge.getCenterPoint(), edge.getOrientation(), verticalLength);
        }
    }

    /**
     * Returns a rectangle that has the specified edge and a height or width (depending on the edge's orientation) as
     * specified by {@code otherDimension}.
     * 
     * @param edge
     *            the edge which will be contained in the returned rectangle
     * @param otherDimension
     *            if the edge's orientation is {@link Orientation#HORIZONTAL horizontal}, this is interpreted as the
     *            height; if the edge's orientation is {@link Orientation#VERTICAL vertical}, this is interpreted as the
     *            width
     * @return a rectangle
     */
    private static Rectangle2D createForEdgeAndOtherDimension(Edge2D edge, double otherDimension) {
        if (edge.isHorizontal()) {
            return createForHorizontalEdgeAndHeight(edge, otherDimension);
        } else {
            return createForVerticalEdgeAndWidth(edge, otherDimension);
        }
    }

    /**
     * Returns a rectangle that has the specified horizontal edge and height. Depending on whether the width is positive
     * or negative, the specified edge will be the upper or lower edge of the returned rectangle.
     * 
     * @param horizontalEdge
     *            the horizontal edge which will be contained in the returned rectangle
     * @param height
     *            the returned rectangle's height
     * @return a rectangle
     */
    private static Rectangle2D createForHorizontalEdgeAndHeight(Edge2D horizontalEdge, double height) {
        Point2D leftEdgeEndPoint = horizontalEdge.getUpperLeft();
        double upperLeftX = leftEdgeEndPoint.getX();
        // if the height is negative, reduce the Y coordinate by that amount
        double upperLeftY = leftEdgeEndPoint.getY() + Math.min(0, height);

        double absoluteWidth = Math.abs(horizontalEdge.getLength());
        double absoluteHeight = Math.abs(height);

        return new Rectangle2D(upperLeftX, upperLeftY, absoluteWidth, absoluteHeight);
    }

    /**
     * Returns a rectangle that has the specified horizontal edge and width. Depending on whether the width is positive
     * or negative, the specified edge will be the left or right edge of the returned rectangle.
     * 
     * @param verticalEdge
     *            the vertical edge which will be contained in the returned rectangle
     * @param width
     *            the returned rectangle's height
     * @return a rectangle
     */
    private static Rectangle2D createForVerticalEdgeAndWidth(Edge2D verticalEdge, double width) {
        Point2D upperEdgeEndPoint = verticalEdge.getUpperLeft();
        // if the width is negative, reduce the X coordinate by that amount
        double upperLeftX = upperEdgeEndPoint.getX() + Math.min(0, width);
        double upperLeftY = upperEdgeEndPoint.getY();

        double absoluteWidth = Math.abs(width);
        double absoluteHeight = Math.abs(verticalEdge.getLength());

        return new Rectangle2D(upperLeftX, upperLeftY, absoluteWidth, absoluteHeight);
    }

    /*
     * MISC
     */

    /**
     * Returns a rectangle with the same coordinates as the specified bounds.
     * 
     * @param bounds
     *            the {@link Bounds} for which the rectangle will be created
     * @return a {@link Rectangle2D} with the same minX-, minY-, maxX- and maxY-coordiantes as the specified bounds
     */
    public static Rectangle2D fromBounds(Bounds bounds) {
        return new Rectangle2D(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
    }

}
