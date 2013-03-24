package org.controlsfx.control.cell;

import org.controlsfx.control.GridCell;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.ImageViewBuilder;

public class ImageGridCell extends GridCell<Image> {
	
    private final ImageView imageView;
    
	public ImageGridCell() {
		getStyleClass().add("image-grid-cell");
		
		imageView = ImageViewBuilder.create().build();
        imageView.fitHeightProperty().bind(heightProperty());
        imageView.fitWidthProperty().bind(widthProperty());
	}
	
	@Override protected void updateItem(Image item, boolean empty) {
	    super.updateItem(item, empty);
	    
	    getChildren().clear();
	    
	    if (empty) {
	        setGraphic(null);
	    } else {
	        imageView.setImage(item);
	        setGraphic(imageView);
	    }
	}
}