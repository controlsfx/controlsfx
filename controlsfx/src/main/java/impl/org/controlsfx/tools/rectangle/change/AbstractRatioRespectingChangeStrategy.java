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
            throw new IllegalStateException("The ratio is not fixed.");
        return ratio;
    }

}
