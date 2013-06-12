package org.controlsfx.samples;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.Rating;

public class HelloRating extends Application implements Sample {
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Rating";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/Rating.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        final Rating rating = new Rating();
        
        // controls, row 1
        HBox controls_row1 = new HBox(5);
        ChoiceBox<Orientation> orientation = new ChoiceBox<Orientation>(FXCollections.observableArrayList(Orientation.values()));
        orientation.getSelectionModel().select(Orientation.HORIZONTAL);
        rating.orientationProperty().bind(orientation.getSelectionModel().selectedItemProperty());
        
        ChoiceBox<Double> ratingValue = new ChoiceBox<Double>(FXCollections.observableArrayList(0D, 1D, 2D, 3D, 4D, 5D, 6D, 7D, 8D, 9D, 10D));
        ratingValue.getSelectionModel().select(rating.getRating());
//        rating.ratingProperty().bind(ratingValue.getSelectionModel().selectedItemProperty());
        
        ChoiceBox<Integer> maxValue = new ChoiceBox<Integer>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        maxValue.getSelectionModel().select(rating.getMax());
        rating.maxProperty().bind(maxValue.getSelectionModel().selectedItemProperty());
        
        controls_row1.getChildren().addAll(orientation, ratingValue, maxValue);
        
        // controls, row 2
        CheckBox partialRating = new CheckBox("Allow partial ratings");
        partialRating.selectedProperty().bindBidirectional(rating.partialRatingProperty());
        
        // controls, row 3
        CheckBox updateOnHover = new CheckBox("Update rating on hover");
        updateOnHover.selectedProperty().bindBidirectional(rating.updateOnHoverProperty());
        
        root.getChildren().addAll(controls_row1, partialRating, updateOnHover, rating);
        
        rating.ratingProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                System.out.println("Rating = " + t1);
            }
        });
        
        return root;
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("Rating Demo");

        Scene scene = new Scene((Parent) getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
