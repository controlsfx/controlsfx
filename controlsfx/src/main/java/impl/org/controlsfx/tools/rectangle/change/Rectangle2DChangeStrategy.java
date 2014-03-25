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
 * A {@code Rectangle2DChangeStrategy} creates instances of {@link Rectangle2D} based on the coordinates of the begin,
 * continuation and end of an action. The behavior is undefined if these three methods are not called on the following
 * order:<br>
 * ({@link #beginChange(Point2D) begin} -> {@link #continueChange(Point2D) continue}* -> {@link #endChange(Point2D) end}
 * )* <br>
 * <br>
 * Most implementations will be creating new rectangles based on an existing one. If the created ones constantly replace
 * the original, this effectively "changes" the rectangle's appearance (note that {@link Rectangle2D} instances
 * themselves are immutable !). This interface and its implementations were created to easily allow a GUI user to change
 * an existing rectangle by typical resize and move operations.
 */
public interface Rectangle2DChangeStrategy {

    /**
     * Begins the change at the specified point.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D beginChange(Point2D point);

    /**
     * Continues the change to the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D continueChange(Point2D point);

    /**
     * Ends the change at the specified point. Must not be called before a call to {@link #beginChange}.
     * 
     * @param point
     *            a point
     * @return the new rectangle
     */
    Rectangle2D endChange(Point2D point);

}
