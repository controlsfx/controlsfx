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
