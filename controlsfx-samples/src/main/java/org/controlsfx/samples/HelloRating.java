/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.samples;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.Rating;

public class HelloRating extends ControlsFXSample {
    
    private Rating rating;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Rating";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/Rating.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/rating.css";
    }
    
    @Override public String getSampleDescription() {
        return "TODO";
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        rating = new Rating();
        root.getChildren().addAll(rating);
        
        rating.ratingProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                System.out.println("Rating = " + t1);
            }
        });
        
        return root;
    }
    
    @Override public Node getControlPanel() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30, 30, 30, 30));
        
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
        
        root.getChildren().addAll(controls_row1, partialRating, updateOnHover);
        
        return root;
    }
}
