package org.controlsfx.samples;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.control.Rating;

public class HelloRating extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("Rating Demo");

        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        final Rating rating = new Rating();
        
        // controls
        HBox controls = new HBox(5);
        ChoiceBox<Orientation> orientation = new ChoiceBox<Orientation>(FXCollections.observableArrayList(Orientation.values()));
        orientation.getSelectionModel().select(Orientation.HORIZONTAL);
        rating.orientationProperty().bind(orientation.getSelectionModel().selectedItemProperty());
        
        ChoiceBox<Integer> ratingValue = new ChoiceBox<Integer>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        ratingValue.getSelectionModel().select(rating.getRating());
        rating.ratingProperty().bind(ratingValue.getSelectionModel().selectedItemProperty());
        
        ChoiceBox<Integer> maxValue = new ChoiceBox<Integer>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        maxValue.getSelectionModel().select(rating.getMax());
        rating.maxProperty().bind(maxValue.getSelectionModel().selectedItemProperty());
        
        controls.getChildren().addAll(orientation, ratingValue, maxValue);
        
        root.getChildren().addAll(controls, rating);
        
        rating.ratingProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                System.out.println("Rating = " + t1);
            }
        });
        
        Scene scene = new Scene(root, 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
