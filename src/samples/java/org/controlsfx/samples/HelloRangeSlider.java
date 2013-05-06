package org.controlsfx.samples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.RangeSlider;

public class HelloRangeSlider extends Application implements Sample {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "RangeSlider";
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(15);
        
        Region horizontalRangeSlider = createHorizontalSlider();
        Region verticalRangeSlider = createVerticalSlider();
        root.getChildren().addAll(horizontalRangeSlider, verticalRangeSlider);
        
        return root;
    }
    
    @Override public void start(Stage stage) {
//        setUserAgentStylesheet(STYLESHEET_CASPIAN);
        stage.setTitle("RangeSlider Demo");

        Scene scene = new Scene((Parent)getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
    
    Region createHorizontalSlider() {
        final TextField minField = new TextField();
        minField.setPrefColumnCount(5);
        final TextField maxField = new TextField();
        maxField.setPrefColumnCount(5);

        final RangeSlider hSlider = new RangeSlider(0, 100, 10, 90);
        hSlider.setShowTickMarks(true);
        hSlider.setShowTickLabels(true);
        hSlider.setBlockIncrement(10);
        hSlider.setPrefWidth(200);

        minField.setText("" + hSlider.getLowValue());
        maxField.setText("" + hSlider.getHighValue());

        minField.setEditable(false);
        minField.setPromptText("Min");

        maxField.setEditable(false);
        maxField.setPromptText("Max");

        minField.textProperty().bind(hSlider.lowValueProperty().asString("%.2f"));
        maxField.textProperty().bind(hSlider.highValueProperty().asString("%.2f"));

        HBox box = new HBox(10);
        box.getChildren().addAll(minField, hSlider, maxField);
        box.setPadding(new Insets(20,0,0,20));
        box.setFillHeight(false);

        return box;
    }
    
    
    Region createVerticalSlider() {
        final TextField minField = new TextField();
        minField.setPrefColumnCount(5);
        final TextField maxField = new TextField();
        maxField.setPrefColumnCount(5);

        final RangeSlider vSlider = new RangeSlider(0, 200, 30, 150);
        vSlider.setOrientation(Orientation.VERTICAL);
        vSlider.setPrefHeight(200);
        vSlider.setBlockIncrement(10);
        vSlider.setShowTickMarks(true);
        vSlider.setShowTickLabels(true);

        minField.setText("" + vSlider.getLowValue());
        maxField.setText("" + vSlider.getHighValue());

        minField.setEditable(false);
        minField.setPromptText("Min");

        maxField.setEditable(false);
        maxField.setPromptText("Max");

        minField.textProperty().bind(vSlider.lowValueProperty().asString("%.2f"));
        maxField.textProperty().bind(vSlider.highValueProperty().asString("%.2f"));

        VBox box = new VBox(10);
        box.setPadding(new Insets(0,0,0, 20));
//        box.setAlignment(Pos.CENTER);
        box.setFillWidth(false);
        box.getChildren().addAll(minField, vSlider, maxField);
        return box;
    }
    
}
