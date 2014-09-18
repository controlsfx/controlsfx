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
        Objects.requireNonNull(point, "The specified point must not be null."); //$NON-NLS-1$
        if (!beforeBegin)
            throw new IllegalStateException(
                    "The change already began, so 'beginChange' must not be called again before 'endChange' was called."); //$NON-NLS-1$
        beforeBegin = false;

        beforeBeginHook(point);
        return doBegin(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rectangle2D continueChange(Point2D point) {
        Objects.requireNonNull(point, "The specified point must not be null."); //$NON-NLS-1$
        if (beforeBegin)
            throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'continueChange'."); //$NON-NLS-1$

        return doContinue(point);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Rectangle2D endChange(Point2D point) {
        Objects.requireNonNull(point, "The specified point must not be null."); //$NON-NLS-1$
        if (beforeBegin)
            throw new IllegalStateException("The change did not begin. Call 'beginChange' before 'endChange'."); //$NON-NLS-1$

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
