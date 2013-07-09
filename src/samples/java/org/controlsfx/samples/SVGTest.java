package org.controlsfx.samples;

import java.net.URL;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;

public class SVGTest extends Application implements Sample {
    
    private static final int IMAGE_HEIGHT = 175;
    private static final int IMAGE_SPACING = 0;
    
    private final String[] images = new String[] {
          "emblem-important.svg",
          "error.svg",
          "help-browser.svg"
  };

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "SVG Rendering";
    }
    
    @Override public String getJavaDocURL() {
        return null;
    }
    
    @Override public boolean includeInSamples() {
        return false;
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox container = new VBox(IMAGE_SPACING);
        
//        container.setStyle("-fx-background-color: lightblue");
        
        for (String svg : images) {
            try {
                loadImage(svg, container);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return container;
    }

    @Override public void start(Stage primaryStage) throws Exception {
        Scene scene = new Scene((Parent)getPanel(primaryStage));
        primaryStage.setScene(scene);
        primaryStage.setWidth(200);
        primaryStage.setHeight((IMAGE_HEIGHT + IMAGE_SPACING) * (images.length) + IMAGE_HEIGHT / 2.0);

        primaryStage.show();
    }
    
    private void loadImage(final String filename, final VBox container) throws Exception {
        final URL pathToFile = SVGTest.class.getResource(filename).toURI().toURL();

//        SVGLoader.loadSVGImage(pathToFile, IMAGE_HEIGHT, -1, new Callback<ImageView, Void>() {
//            @Override public Void call(ImageView image) {
//                container.getChildren().addAll(image);
//                return null;
//            }
//        });
    }
}
