package org.controlsfx.samples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.ToggleSwitch;

public class HelloToggleSwitch extends Application implements Sample {

    @Override public String getSampleName() {
        return "ToggleSwitch";
    }

    @Override public Node getPanel(Stage stage) {
        
        Button btn1 = new Button("Button 1");
        Button btn2 = new Button("Button 2");
        ToggleSwitch sc = new ToggleSwitch();
        
//        sc.setStyle("-fx-border-color: green");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 10));
        grid.add(sc, 0, 0);
        grid.add(btn1, 1, 0);
        grid.add(btn2, 0, 1);
        
//        Group g = new Group();
//        g.setLayoutX(100);
//        g.setLayoutY(100);
//        g.getChildren().add(sc);
        
        return grid;
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