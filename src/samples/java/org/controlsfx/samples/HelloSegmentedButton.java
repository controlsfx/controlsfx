package org.controlsfx.samples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.SegmentedButton;

public class HelloSegmentedButton extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "SegmentedButton";
    }
    
    @Override public Node getPanel(Stage stage) {
        ToggleButton b1 = new ToggleButton("day");
        ToggleButton b2 = new ToggleButton("week");
        ToggleButton b3 = new ToggleButton("month");
        ToggleButton b4 = new ToggleButton("year");
        
        SegmentedButton pillBox = new SegmentedButton();    
        pillBox.getButtons().addAll(b1, b2, b3, b4);
        Button b = new Button("Submit");

        HBox container = new HBox(20);
        container.setPadding(new Insets(30, 30, 0, 30));
        container.getChildren().addAll(pillBox);
        
        return container;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("SegmentedButton Demo");
        
        
        Scene scene = new Scene((Parent) getPanel(stage), 350, 150);
//        scene.getStylesheets().addAll(SegmentedButton.class.getResource("segmentedbutton.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
