package org.controlsfx.tools.rectangle.change;

import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Abstract superclass to implementations of {@link Rectangle2DChangeStrategy}. Checks whether the specified points are not-null
 * and the "begin-continue-end"-contract.
 */
abstract class AbstractBeginEndCheckingChangeStrategy implements Rectangle2DChangeStrategy {

    // ATTRIBUTES

    /**
     * Indicates whether {@link #beginChange(Point2D) beginChange} was called.
     */
    private boolean beforeBegin;

    // CONSTRUCTOR

    /**
     * Creates a change strategy which checks whether begin and end are correctly called.
     */
    protected AbstractBeginEndCheckingChangeStrategy() {
        beforeBegin = true;
    }

    // IMPLEMENTATION OF 'ChangeStrategy'

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rectangle2D beginChange(Point2D point) {
        Objects.requireNonNull(point, "The specified point must not be null.");
        if (!beforeBegin)
            throw new IllegalStateException(
                    "The change already began, so 'beginChange' must not be called again before 'endChange' was called.");
        beforeBegin = false;

        beforeBeginHook(point);
        return doBegin(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rectangle2D continueChange(Point2D point) {
        Objects.requireNonNull(point, "The specified point must not be null.");
        if (beforeBegin)
            throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'continueChange'.");

        return doContinue(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rectangle2D endChange(Point2D point) {
        Objects.requireNonNull(point, "The specified point must not be null.");
        if (beforeBegin)
            throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'endChange'.");

        Rectangle2D finalRectangle = doEnd(point);
        afterEndHook(point);
        beforeBegin = true;
        return finalRectangle;
    }

    //ABSTRACT METHODS

    /**
     * Called before the change begins at the specified point.
     * 
     * @param point
     *            a point
     */
    protected void beforeBeginHook(Point2D point) {
        // can be overridden by subclasses
    }

    /**
     * Begins the change at the specified point.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    protected abstract Rectangle2D doBegin(Point2D point);

    /**
     * Continues the change to the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    protected abstract Rectangle2D doContinue(Point2D point);

    /**
     * Ends the change at the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    protected abstract Rectangle2D doEnd(Point2D point);

    /**
     * Called after the change ends at the specified point.
     * 
     * @param point
     *            a point
     */
    protected void afterEndHook(Point2D point) {
        // can be overridden by subclasses
    }

}
