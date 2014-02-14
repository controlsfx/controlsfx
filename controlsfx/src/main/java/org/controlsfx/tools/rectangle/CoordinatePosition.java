package org.controlsfx.tools.rectangle;

/**
 * Enumerates all possible positions coordinates can have relative to a rectangle.
 */
public enum CoordinatePosition {

    /**
     * The coordinates are inside the rectangle.
     */
    IN_RECTANGLE,

    /**
     * The coordinates are outside of the rectangle.
     */
    OUT_OF_RECTANGLE,

    /**
     * The coordinates are close to the northern edge of the rectangle.
     */
    NORTH_EDGE,

    /**
     * The coordinates are close to the northern and the eastern edge of the rectangle.
     */
    NORTHEAST_EDGE,

    /**
     * The coordinates are close to the eastern edge of the rectangle.
     */
    EAST_EDGE,

    /**
     * The coordinates are close to the southern and eastern edge of the rectangle.
     */
    SOUTHEAST_EDGE,

    /**
     * The coordinates are close to the southern edge of the rectangle.
     */
    SOUTH_EDGE,

    /**
     * The coordinates are close to the southern and western edge of the rectangle.
     */
    SOUTHWEST_EDGE,

    /**
     * The coordinates are close to the western edge of the rectangle.
     */
    WEST_EDGE,

    /**
     * The coordinates are close to the northern and the western edge of the rectangle.
     */
    NORTHWEST_EDGE,

}
