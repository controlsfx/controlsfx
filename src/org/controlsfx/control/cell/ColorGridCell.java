package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.scene.control.ContentDisplay;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

public class ColorGridCell extends GridCell<Color> {
	
	private Rectangle colorRect;
	
	private static final boolean debug = false;

    public ColorGridCell() {
		getStyleClass().add("color-grid-cell");
		
		colorRect = RectangleBuilder.create().stroke(Color.BLACK).build();
		colorRect.heightProperty().bind(heightProperty());
		colorRect.widthProperty().bind(widthProperty());   
		setGraphic(colorRect);
		
		if (debug) {
		    setContentDisplay(ContentDisplay.TEXT_ONLY);
		}
	}
	
	@Override protected void updateItem(Color item, boolean empty) {
	    super.updateItem(item, empty);
	    
	    if (empty) {
	        setGraphic(null);
	    } else {
	        colorRect.setFill(item);
	        setGraphic(colorRect);
	    }
	    
	    if (debug) {
	        setText(getIndex() + "");
	    }
	}
}
