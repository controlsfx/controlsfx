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
 * A strategy which creates a new rectangle.
 */
public class NewChangeStrategy extends AbstractFixedPointChangeStrategy {

    /*
     * The new selection will have the starting point as a fixed corner. The other corner will always be the current
     * point modulo the ratio which will be respected if enforced. Both is handled by the superclass.
     */

    // ATTRIBUTES

    /**
     * The starting point of this change.
     */
    private Point2D startingPoint;

    // CONSTRUCTOR

    /**
     * Creates a change strategy which creates a new rectangle. It respects the specified {@code ratio} if
     * {@code ratioFixed} is {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     * @param bounds
     *            the bounds within which the new rectangle must be contained
     */
    public NewChangeStrategy(boolean ratioFixed, double ratio, Rectangle2D bounds) {
        super(ratioFixed, ratio, bounds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void beforeBeginHook(Point2D point) {
        startingPoint = point;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Point2D getFixedCorner() {
        return startingPoint;
    }

}
