package org.controlsfx.control;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by pedro_000 on 8/26/2015.
 */
public class ToggleSwitchTest extends Application
{
    static final String RESOURCE = "ToggleSwitch.fxml";


    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource(RESOURCE));
        root.getStylesheets().add(getClass().getResource("toggleswitchtest.css").toExternalForm());
        primaryStage.setTitle("Toggle Switch");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

//        ScenicView.show(scene);
    }


    public static void main(String[] args) {
        launch(args);
    }
}

