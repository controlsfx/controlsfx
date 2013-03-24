package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;

public class ColorGridCell extends GridCell<Color> {
	
	private Rectangle colorRect;

    public ColorGridCell() {
		getStyleClass().add("color-grid-cell");
		
		colorRect = RectangleBuilder.create().stroke(Color.BLACK).build();
		colorRect.heightProperty().bind(heightProperty());
		colorRect.widthProperty().bind(widthProperty());   
		setGraphic(colorRect);
	}
	
	@Override protected void updateItem(Color item, boolean empty) {
	    super.updateItem(item, empty);
	    
	    if (empty) {
	        setGraphic(null);
	    } else {
	        colorRect.setFill(item);
	        setGraphic(colorRect);
	    }
	}
}
