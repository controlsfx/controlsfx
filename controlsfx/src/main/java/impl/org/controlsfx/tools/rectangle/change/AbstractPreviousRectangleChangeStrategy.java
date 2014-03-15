package impl.org.controlsfx.tools.rectangle.change;

import java.util.Objects;

import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to most implementations of {@link Rectangle2DChangeStrategy}. Stores a ratio and is based on a previous
 * rectangle. Both behaviors are determined during construction.
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

        Objects.requireNonNull(previous, "The previous rectangle must not be null.");
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
