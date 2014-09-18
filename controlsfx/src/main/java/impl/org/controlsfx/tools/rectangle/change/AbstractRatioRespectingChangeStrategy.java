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

/**
 * Abstract superclass to implementations of {@link Rectangle2DChangeStrategy}, which might be parameterized such that only
 * rectangles of a defined ratio are created. This parameterization happens during construction. Subclasses must
 * implement the ratio handling themselves! This class only holds the parameters.
 */
abstract class AbstractRatioRespectingChangeStrategy extends AbstractBeginEndCheckingChangeStrategy {

    // ATTRIBUTES

    /**
     * Indicates whether the current selection must have a fixed ratio. If so, 'ratio' can be used.
     */
    private final boolean ratioFixed;

    /**
     * The currently used ratio. Should only be used if 'ratioFixed' is true.
     */
    private final double ratio;

    // CONSTRUCTOR

    /**
     * Creates a change strategy which respects the specified {@code ratio} if {@code ratioFixed} is {@code true}.
     * 
     * @param ratioFixed
     *            indicates whether the ratio will be fixed
     * @param ratio
     *            defines the fixed ratio
     */
    protected AbstractRatioRespectingChangeStrategy(boolean ratioFixed, double ratio) {
        super();
        this.ratioFixed = ratioFixed;
        this.ratio = ratio;
    }

    // Attribute Access

    /**
     * Indicates whether the ratio is fixed. If so, the ratio can be accessed with {@link #getRatio()}.
     * 
     * @return true if the ratio is fixed; false otherwise
     */
    protected final boolean isRatioFixed() {
        return ratioFixed;
    }

    /**
     * The current ratio. Can only be called without exception when {@link #isRatioFixed()} returns true.
     * 
     * @return the current ratio
     */
    protected final double getRatio() {
        if (!ratioFixed)
            throw new IllegalStateException("The ratio is not fixed."); //$NON-NLS-1$
        return ratio;
    }

}
