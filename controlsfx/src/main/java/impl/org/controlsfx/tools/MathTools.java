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
package impl.org.controlsfx.tools;

import java.util.Objects;

/**
 * Contains methods {@link Math} might also contain but doesn't.
 */
public class MathTools {

    /**
     * Checks whether the specified value lies in the closed interval defined by the specified bounds.
     * 
     * @param lowerBound
     *            the interval's lower bound; included in the interval
     * @param value
     *            the value which will be checked
     * @param upperBound
     *            the interval's upper bound; included in the interval
     * @return {@code true} if {@code lowerBound} <= {@code value} <= {@code upperBound} <br>
     *         {@code false} otherwise
     */
    public static boolean isInInterval(double lowerBound, double value, double upperBound) {
        return lowerBound <= value && value <= upperBound;
    }

    /**
     * Checks whether the specified value lies in the closed interval defined by the specified bounds. If it does, it is
     * returned; otherwise the bound closer to the value will be returned.
     * 
     * @param lowerBound
     *            the interval's lower bound; included in the interval
     * @param value
     *            the value which will be checked
     * @param upperBound
     *            the interval's upper bound; included in the interval
     * @return {@code value} if {@code lowerBound} <= {@code value} <= {@code upperBound} <br>
     *         {@code lowerBound} if {@code value} < {@code lowerBound} <br>
     *         {@code upperBound} if {@code upperBound} < {@code value}
     */
    public static double inInterval(double lowerBound, double value, double upperBound) {
        if (value < lowerBound)
            return lowerBound;
        if (upperBound < value)
            return upperBound;
        return value;
    }

    /**
     * Returns the smallest value in the specified array according to {@link Math#min(double, double)}.
     * 
     * @param values
     *            a non-null, non-empty array of double values
     * @return a value from the array which is smaller then or equal to any other value from the array
     * @throws NullPointerException
     *             if the values array is {@code null}
     * @throws IllegalArgumentException
     *             if the values array is empty (i.e. has {@code length} 0)
     */
    public static double min(double... values) {
        Objects.requireNonNull(values, "The specified value array must not be null."); //$NON-NLS-1$
        if (values.length == 0)
            throw new IllegalArgumentException("The specified value array must contain at least one element."); //$NON-NLS-1$

        double min = Double.MAX_VALUE;
        for (double value : values)
            min = Math.min(value, min);
        return min;
    }

}
