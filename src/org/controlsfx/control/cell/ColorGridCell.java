package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.RectangleBuilder;



public class ColorGridCell extends GridCell<Color> {
	
	public ColorGridCell() {
		getStyleClass().add("color-grid-cell");
		itemProperty().addListener(new ChangeListener<Color>() {

			@Override
			public void changed(ObservableValue<? extends Color> arg0,
					Color arg1, Color arg2) {
				getChildren().clear();
				Rectangle colorRect = RectangleBuilder.create().fill(arg2)
						.stroke(Color.BLACK).build();
				colorRect.heightProperty().bind(heightProperty());
				colorRect.widthProperty().bind(widthProperty());				
				setGraphic(colorRect);
			}
		});
	}
}
