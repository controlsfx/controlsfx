package org.controlsfx.samples;

import java.net.URL;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.tools.SVGLoader;

public class SVGTest extends Application {

    /**
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) throws Exception {
        final URL pathToFile = SVGTest.class.getResource("error.svg").toURI().toURL();

        final StackPane root = new StackPane();
        
        final Rectangle rect = new Rectangle(400, 400, Color.GREEN);
        
        SVGLoader.loadSVGImage(pathToFile, 350, -1, new Callback<ImageView, Void>() {
            @Override public Void call(ImageView image) {
                root.getChildren().setAll(rect, image);
                return null;
            }
        });

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setWidth(400);
        primaryStage.setHeight(400);

        primaryStage.show();
    }
}
