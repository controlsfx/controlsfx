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
package org.controlsfx.control.breadcrumbs;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * Represents a BreadCrumb Button
 * 
 * <code>
 * ----------
 *  \         \
 *  /         /
 * ----------
 * </code>
 * 
 * 
 */
public class BreadCrumbButton extends Button {

    private final double arrowWidth = 5;
    private final double arrowHeight = 20;

    /**
     * Create a BreadCrumbButton
     * 
     * @param text Buttons text
     * @param first Is this the first / home button?
     */
    public BreadCrumbButton(String text, boolean first){
        this(text, null, first);
    }

    /**
     * Create a BreadCrumbButton
     * @param text Buttons text
     * @param gfx Gfx of the Button
     * @param first Is this the first / home button?
     */
    public BreadCrumbButton(String text, Node gfx, boolean first){
        super(text, gfx);
        // set path as button shape
        this.setShape(createButtonShape(first));
    }

    public double getArrowWidth(){
        return arrowWidth;
    }

    /**
     * Create an arrow path
     * 
     * Based upon Uwe / Andy Till code snippet found here:
     * @see http://ustesis.wordpress.com/2013/11/04/implementing-breadcrumbs-in-javafx/
     * @param first
     * @return
     */
    private Path createButtonShape(boolean first){
        // build the following shape (or home without left arrow)

        //   --------
        //  \         \
        //  /         /
        //   --------
        Path path = new Path();

        // begin in the upper left corner
        MoveTo e1 = new MoveTo(0, 0);
        path.getElements().add(e1);

        // draw a horizontal line that defines the width of the shape
        HLineTo e2 = new HLineTo();
        // bind the width of the shape to the width of the button
        e2.xProperty().bind(this.widthProperty().subtract(arrowWidth));
        path.getElements().add(e2);

        // draw upper part of right arrow
        LineTo e3 = new LineTo();
        // the x endpoint of this line depends on the x property of line e2
        e3.xProperty().bind(e2.xProperty().add(arrowWidth));
        e3.setY(arrowHeight / 2.0);
        path.getElements().add(e3);

        // draw lower part of right arrow
        LineTo e4 = new LineTo();
        // the x endpoint of this line depends on the x property of line e2
        e4.xProperty().bind(e2.xProperty());
        e4.setY(arrowHeight);
        path.getElements().add(e4);

        // draw lower horizontal line
        HLineTo e5 = new HLineTo(0);
        path.getElements().add(e5);

        if(!first){
            // draw lower part of left arrow
            // we simply can omit it for the first Button
            LineTo e6 = new LineTo(arrowWidth, arrowHeight / 2.0);
            path.getElements().add(e6);
        }else{
            // draw an arc for the first bread crumb
            ArcTo arcTo = new ArcTo();
            arcTo.setSweepFlag(true);
            arcTo.setX(0);
            arcTo.setY(0);
            arcTo.setRadiusX(15.0f);
            arcTo.setRadiusY(15.0f);
            path.getElements().add(arcTo);
        }

        // close path
        ClosePath e7 = new ClosePath();
        path.getElements().add(e7);
        // this is a dummy color to fill the shape, it won't be visible
        path.setFill(Color.BLACK);
        return path;
    }
}
