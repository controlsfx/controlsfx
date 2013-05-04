package org.controlsfx.samples;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.ToggleSwitch;

public class HelloToggleSwitch extends Application implements Sample {

    @Override public String getSampleName() {
        return "ToggleSwitch";
    }

    @Override public Node getPanel(Stage stage) {
        ToggleSwitch sc = new ToggleSwitch();
        
        Group g = new Group();
        g.setLayoutX(100);
        g.setLayoutY(100);
        
        g.getChildren().add(sc);
        
        return g;
    }

    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("ToggleSwitch Demo");
        
        Scene scene = new Scene((Parent)getPanel(stage), 300, 300);
        scene.setFill(Color.LIGHTGRAY);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}