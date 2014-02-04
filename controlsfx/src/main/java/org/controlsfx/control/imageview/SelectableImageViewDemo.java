package org.controlsfx.control.imageview;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class SelectableImageViewDemo extends Application {

    private static final Image image = new Image(
            "http://www.campusgifts.co.uk/media/catalog/product/cache/3/image/9df78eab33525d08d6e5fb8d27136e95/e/d/edward-monkton-coaster-the-random-hedgehog-750_4.jpg");

    @Override
    public void start(Stage stage) throws Exception {
        SelectableImageView customControl = new SelectableImageView(image);

        stage.setScene(new Scene(customControl));
        stage.setTitle("Custom Control");
        stage.setWidth(800);
        stage.setHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
