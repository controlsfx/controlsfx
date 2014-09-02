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

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * A strategy which enlarges an existing rectangle to the northeast.
 */
public class ToNortheastChangeStrategy extends AbstractFixedPointChangeStrategy {

    /*
     * The new rectangle will have the existing rectangle's southwestern corner as a fixed corner. The other corner will
     * always be the current point (modulo the ratio which will be respected if enforced), which is handled by the
     * superclass.
     */

    // ATTRIBUTES

    /**
     * The new rectangle's southwestern corner.
     */
    private final Point2D southwesternCorner;

    // CONSTRUCTOR

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
     * @param bounds
     *            the bounds within which the new rectangle must be contained
     */
    public ToNortheastChangeStrategy(Rectangle2D original, boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio, bounds);
        southwesternCorner = new Point2D(original.getMinX(), original.getMaxY());
    }

    // IMPLEMENTATION OF 'AbstractFixedPointChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point2D getFixedCorner() {
        return southwesternCorner;
    }

}
