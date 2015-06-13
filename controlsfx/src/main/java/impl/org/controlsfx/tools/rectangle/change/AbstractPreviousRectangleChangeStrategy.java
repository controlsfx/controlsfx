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

import java.util.Objects;

import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to those implementations of {@link Rectangle2DChangeStrategy}, which are based on a previous
 * rectangle. Respects a ratio if one is set.
 */
abstract class AbstractPreviousRectangleChangeStrategy extends AbstractRatioRespectingChangeStrategy {

    // ATTRIBUTES

    /**
     * The rectangle these changes are based on.
     */
    private final Rectangle2D previous;

    // CONSTRUCTOR

    /**
     * Creates a change strategy which is based on the specified {@code previous} rectangle and respects the specified
     * {@code ratio} if {@code ratioFixed} is {@code true}.
     * 
     * @param previous
     *            the previous rectangle this change is based on
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     */
    protected AbstractPreviousRectangleChangeStrategy(Rectangle2D previous, boolean ratioFixed, double ratio) {
        super(ratioFixed, ratio);

        Objects.requireNonNull(previous, "The previous rectangle must not be null."); //$NON-NLS-1$
        this.previous = previous;
    }

    // ATTRIBUTE ACCESS

    /**
     * The previous rectangle this change is based on.
     * 
     * @return the previous rectangle
     */
    protected final Rectangle2D getPrevious() {
        return previous;
    }

}
