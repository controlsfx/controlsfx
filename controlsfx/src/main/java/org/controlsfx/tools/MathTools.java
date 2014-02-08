package org.controlsfx.tools;

/**
 * Contains methods {@link Math} might also contain but doesn't.
 */
public class MathTools {

    /**
     * Checks whether the specified value lies in the closed interval defined by the specified bounds.
     * If it does, it is returned; otherwise the bound closer to the value will be returned.
     * @param lowerBound
     * the interval's lower bound; included in the interval
     * @param value
     * the value which will be checked
     * @param upperBound
     * the interval's upper bound; included in the interval
     * @return
     * {@code value} if {@code lowerBound} <= {@code value} <= {@code upperBound} <br>
     * {@code lowerBound} if {@code value} < {@code lowerBound} <br>
     * {@code upperBound} if {@code upperBound} < {@code value}
     */
    public static double inInterval(double lowerBound, double value, double upperBound) {
        if (value < lowerBound)
            return lowerBound;
        if (upperBound < value)
            return upperBound;
        return value;
    }

}
