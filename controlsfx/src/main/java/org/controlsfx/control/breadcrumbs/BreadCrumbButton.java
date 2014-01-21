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
 * 	\         \
 * 	/         /
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
