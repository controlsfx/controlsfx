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
package impl.org.controlsfx.tools.rectangle.change;

import impl.org.controlsfx.tools.rectangle.Edge2D;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A strategy which enlarges an existing rectangle to the west.
 */
public class ToWestChangeStrategy extends AbstractFixedEdgeChangeStrategy {

    /*
     * The new rectangle will have the existing rectangle's eastern edge as a fixed edge. The parallel edge will
     * be defined by the current point (modulo the ratio which will be respected if enforced), which is handled by the
     * superclass.
     */

    // ATTRIBUTES

    /**
     * The new rectangle's eastern edge.
     */
    private final Edge2D easternEdge;

    // CONSTRUCTOR

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the west. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original
     *            the original rectangle
     * @param ratioFixed
     *            indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio
     *            the possibly fixed ratio of the rectangle created by this strategy
     * @param bounds
     *            the bounds within which the rectangle can be resized
     */
    public ToWestChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio, bounds);
        Point2D edgeCenterPoint = new Point2D(original.getMaxX(), (original.getMinY() + original.getMaxY()) / 2);
        easternEdge = new Edge2D(edgeCenterPoint, Orientation.VERTICAL, original.getMaxY() - original.getMinY());
    }

    /**
     * Creates a new change strategy which enlarges the specified {@code original} rectangle to the northeast. The given
     * {@code ratio} is enforced when indicated by {@code ratioFixed}.
     * 
     * @param original
     *            the original rectangle
     * @param ratioFixed
     *            indicates whether the rectangle's ratio will be fixed to the {@code ratio}
     * @param ratio
     *            the possibly fixed ratio of the rectangle created by this strategy
     * @param maxX
     *            the maximal x-coordinate of the right edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's width
     * @param maxY
     *            the maximal y-coordinate of the lower edge of the created rectangles; must be greater than or equal to
     *            the previous rectangle's height
     */
    public ToWestChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, double maxX, double maxY) {
        this(original, ratioFixed, ratio, new Rectangle2D(0, 0, maxX, maxY));
    }

    // IMPLEMENTATION OF 'AbstractFixedEdgeChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    protected Edge2D getFixedEdge() {
        return easternEdge;
    }

}
