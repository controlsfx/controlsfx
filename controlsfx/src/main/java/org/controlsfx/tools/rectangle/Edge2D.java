package org.controlsfx.tools.rectangle;

import java.util.Objects;

import javafx.geometry.Orientation;
import javafx.geometry.Point2D;

/**
 * The edge of a rectangle, i.e. a vertical or horizontal line segment.
 */
public class Edge2D {

    /*
     * ATTRIBUTES
     */

    /**
     * The edge's center point.
     */
    private final Point2D centerPoint;

    /**
     * The edge's orientation.
     */
    private final Orientation orientation;

    /**
     * The edge's length.
     */
    private final double length;

    /*
     * ATTRIBUTES
     */

    /**
     * Creates a new edge which is specified by its center point, orientation and length.
     * 
     * @param centerPoint
     *            the edge's center point
     * @param orientation
     *            the edge's orientation
     * @param length
     *            the edge's length; must be non-negative.
     */
    public Edge2D(Point2D centerPoint, Orientation orientation, double length) {
        Objects.requireNonNull(centerPoint, "The specified center point must not be null.");
        Objects.requireNonNull(orientation, "The specified orientation must not be null.");
        if (length < 0)
            throw new IllegalArgumentException(
                    "The length must not be negative, i.e. zero or a positive value is alowed.");

        this.centerPoint = centerPoint;
        this.orientation = orientation;
        this.length = length;
    }

    /*
     * CORNERS AND DISTANCES
     */

    /**
     * Returns the edge's upper left end point. It has ({@link #getLength() length} / 2) distance from the center point
     * and depending on the edge's orientation either the same X (for {@link Orientation#HORIZONTAL}) or Y (for
     * {@link Orientation#VERTICAL}) coordinate.
     * 
     * @return the edge's upper left point
     */
    public Point2D getUpperLeft() {
        if (isHorizontal()) {
            // horizontal
            double cornersX = centerPoint.getX() - (length / 2);
            double edgesY = centerPoint.getY();
            return new Point2D(cornersX, edgesY);
        } else {
            // vertical
            double edgesX = centerPoint.getX();
            double cornersY = centerPoint.getY() - (length / 2);
            return new Point2D(edgesX, cornersY);
        }
    }

    /**
     * Returns the edge's lower right end point. It has ({@link #getLength() length} / 2) distance from the center point
     * and depending on the edge's orientation either the same X (for {@link Orientation#HORIZONTAL}) or Y (for
     * {@link Orientation#VERTICAL}) coordinate.
     * 
     * @return the edge's lower right point
     */
    public Point2D getLowerRight() {
        if (isHorizontal()) {
            // horizontal
            double cornersX = centerPoint.getX() + (length / 2);
            double edgesY = centerPoint.getY();
            return new Point2D(cornersX, edgesY);
        } else {
            // vertical
            double edgesX = centerPoint.getX();
            double cornersY = centerPoint.getY() + (length / 2);
            return new Point2D(edgesX, cornersY);
        }
    }

    /**
     * Returns the distance of the specified point to the edge in terms of the dimension orthogonal to the edge's
     * orientation. The sign denotes whether on which side of the edge, the point lies.<br>
     * So e.g. if the edge is horizontal, only the Y coordinate's difference between the specified point and the edge is
     * considered. If the point lies to the right of the edge, the returned value is positive.
     * 
     * @param otherPoint
     *            the point to where the distance is computed
     * @return the distance
     */
    public double getOrthogonalDifference(Point2D otherPoint) {
        Objects.requireNonNull(otherPoint, "The other point must nt be null.");

        if (isHorizontal())
            // horizontal -> subtract y coordinates
            return otherPoint.getY() - centerPoint.getY();
        else
            // vertical-> subtract x coordinates
            return otherPoint.getX() - centerPoint.getX();
    }

    /*
     * ATTRIBUTE ACCESS
     */

    /**
     * @return the edge's center point
     */
    public Point2D getCenterPoint() {
        return centerPoint;
    }

    /**
     * Returns this edge's orientation. Note that the orientation can also be checked with {@link #isHorizontal()} and
     * {@link #isVertical()}.
     * 
     * @return the edge's orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Indicates whether this is a {@link Orientation#HORIZONTAL horizontal} edge.
     * 
     * @return true if {@link #getOrientation()} returns {@link Orientation#HORIZONTAL}
     */
    public boolean isHorizontal() {
        return orientation == Orientation.HORIZONTAL;
    }

    /**
     * Indicates whether this is a {@link Orientation#VERTICAL horizontal} edge.
     * 
     * @return true if {@link #getOrientation()} returns {@link Orientation#VERTICAL}
     */
    public boolean isVertical() {
        return orientation == Orientation.VERTICAL;
    }

    /**
     * @return the edge's length
     */
    public double getLength() {
        return length;
    }

    /*
     * EQUALS, HASHCODE & TOSTRING
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((centerPoint == null) ? 0 : centerPoint.hashCode());
        long temp;
        temp = Double.doubleToLongBits(length);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge2D other = (Edge2D) obj;
        if (centerPoint == null) {
            if (other.centerPoint != null)
                return false;
        } else if (!centerPoint.equals(other.centerPoint))
            return false;
        if (Double.doubleToLongBits(length) != Double.doubleToLongBits(other.length))
            return false;
        if (orientation != other.orientation)
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Edge2D [centerX = " + centerPoint.getX() + ", centerY = " + centerPoint.getY()
                + ", orientation = " + orientation + ", length = " + length + "]";
    }

}
