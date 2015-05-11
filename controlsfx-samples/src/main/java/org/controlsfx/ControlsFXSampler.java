package org.controlsfx;

import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import fxsampler.FXSampler;
import fxsampler.FXSamplerProject;
import fxsampler.model.WelcomePage;

public class ControlsFXSampler implements FXSamplerProject {

    /** {@inheritDoc} */
    @Override public String getProjectName() {
        return "ControlsFX";
    }
    
    /** {@inheritDoc} */
    @Override public String getSampleBasePackage() {
        return "org.controlsfx.samples";
    }
    
    /** {@inheritDoc} */
    @Override public WelcomePage getWelcomePage() {
        VBox vBox = new VBox();
        ImageView imgView = new ImageView();
        imgView.setStyle("-fx-image: url('org/controlsfx/samples/ControlsFX.png');");
        StackPane pane = new StackPane();
        pane.setPrefHeight(207);
        pane.setStyle("-fx-background-image: url('org/controlsfx/samples/bar.png');"
                + "-fx-background-repeat: repeat-x;");
        pane.getChildren().add(imgView);
        Label label = new Label();
        label.setWrapText(true);
        StringBuilder desc = new StringBuilder();
        desc.append("ControlsFX is an open source project for JavaFX that aims ");
        desc.append("to provide really high quality UI controls and other tools to ");
        desc.append("complement the core JavaFX distribution.");
        desc.append("\n\n");
        desc.append("Explore the available UI controls by clicking on the options to the left.");
        label.setText(desc.toString());
        label.setStyle("-fx-font-size: 1.5em; -fx-padding: 20 0 0 5;");
        vBox.getChildren().addAll(pane, label);
        WelcomePage wPage = new WelcomePage("Welcome to Controls FX!", vBox);
        return wPage;
    }
    
    public static void main(String[] args) {
        FXSampler.main(args);
    }
}
