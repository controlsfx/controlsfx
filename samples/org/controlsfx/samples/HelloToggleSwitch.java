package org.controlsfx.samples;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.control.ToggleSwitch;

public class HelloToggleSwitch extends Application {
    
     @Override public void start(Stage stage) throws Exception {
        stage.setTitle("ToggleSwitch Demo");
        ToggleSwitch sc = new ToggleSwitch();
        Scene scene = newScene();
        ((Group)scene.getRoot()).getChildren().add(sc);
        scene.getStylesheets().addAll("org/controlsfx/control/toggle-switch.css");
        scene.setFill(Color.LIGHTGRAY);
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    private static Scene newScene() {
        Group g = new Group();
        g.setLayoutX(100);
        g.setLayoutY(100);
        Scene scene = new Scene(g, 300, 300);
        return scene;
    }
    
}