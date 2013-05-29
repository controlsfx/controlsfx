package org.controlsfx.samples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.SegmentedButton;

public class HelloSegmentedButton extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "SegmentedButton";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/SegmentedButton.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        // without segmented button
        grid.add(new Label("Without SegmentedButton (with 10px spacing): "), 0, 0);
        
        ToggleButton without_b1 = new ToggleButton("day"); 
        ToggleButton without_b2 = new ToggleButton("week");
        ToggleButton without_b3 = new ToggleButton("month");
        ToggleButton without_b4 = new ToggleButton("year");
        
        final ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(without_b1, without_b2, without_b3, without_b4);
        
        HBox toggleButtons = new HBox(without_b1, without_b2, without_b3, without_b4);
        toggleButtons.setSpacing(10);
        grid.add(toggleButtons, 1, 0);
        
        // with segmented button
        grid.add(new Label("With SegmentedButton: "), 0, 1);
        
        ToggleButton with_b1 = new ToggleButton("day");
        ToggleButton with_b2 = new ToggleButton("week");
        ToggleButton with_b3 = new ToggleButton("month");
        ToggleButton with_b4 = new ToggleButton("year");
        
        SegmentedButton segmentedButton = new SegmentedButton(with_b1, with_b2, with_b3, with_b4);    
        grid.add(segmentedButton, 1, 1);
        
        
        // Using modena pill button styling
        grid.add(new Label("SegmentedButton (with modena styling): "), 0, 2);
        
        ToggleButton pill_b1 = new ToggleButton("day");
        pill_b1.getStyleClass().add("left-pill");
        ToggleButton pill_b2 = new ToggleButton("week");
        pill_b2.getStyleClass().add("center-pill");
        ToggleButton pill_b3 = new ToggleButton("month");
        pill_b3.getStyleClass().add("center-pill");
        ToggleButton pill_b4 = new ToggleButton("year");
        pill_b4.getStyleClass().add("right-pill");
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(pill_b1, pill_b2, pill_b3, pill_b4);
        HBox pillBox = new HBox(pill_b1, pill_b2, pill_b3, pill_b4);
        grid.add(pillBox, 1, 2);
        

        return grid;
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
