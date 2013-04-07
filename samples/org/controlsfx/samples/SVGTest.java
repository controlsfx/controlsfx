package org.controlsfx.samples;

import java.net.URL;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.tools.SVGLoader;

public class SVGTest extends Application {
    
    private static final int IMAGE_HEIGHT = 175;
    private static final int IMAGE_SPACING = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) throws Exception {
        VBox container = new VBox(IMAGE_SPACING);
        
        container.setStyle("-fx-background-color: lightblue");
        
        final String[] images = new String[] {
//                "emblem-important.svg",
//                "error.svg",
                "help-browser.svg"
        };
        
        for (String svg : images) {
            loadImage(svg, container);
        }

        Scene scene = new Scene(container);
        primaryStage.setScene(scene);
        primaryStage.setWidth(200);
        primaryStage.setHeight((IMAGE_HEIGHT + IMAGE_SPACING) * (images.length) + IMAGE_HEIGHT / 2.0);

        primaryStage.show();
    }
    
    private void loadImage(final String filename, final VBox container) throws Exception {
        final URL pathToFile = SVGTest.class.getResource(filename).toURI().toURL();

        SVGLoader.loadSVGImage(pathToFile, IMAGE_HEIGHT, -1, new Callback<ImageView, Void>() {
            @Override public Void call(ImageView image) {
                container.getChildren().addAll(image);
                return null;
            }
        });
    }
}
