package org.controlsfx.samples;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.NotificationBar;
import org.controlsfx.control.Rating;
import org.controlsfx.dialog.Dialog.Actions;

public class HelloNotificationBar extends Application implements Sample {
    
    private NotificationBar notificationBar;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Notification Bar";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationBar.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(Stage stage) {
        VBox root = new VBox(20);
//        root.setPadding(new Insets(30, 30, 30, 30));
        
        notificationBar = new NotificationBar(null);
        notificationBar.getActions().add(Actions.OK);
        
        
        root.getChildren().add(notificationBar);
        
        Button showBtn = new Button("Show / Hide");
        showBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                if (notificationBar.isShowing()) {
                    notificationBar.hide();
                } else {
                    boolean useDarkTheme = ! notificationBar.getStyleClass().contains(NotificationBar.STYLE_CLASS_DARK);
                    
                    if (useDarkTheme) {
                        notificationBar.setText("Hello World! Using the dark theme");
                        notificationBar.getStyleClass().add(NotificationBar.STYLE_CLASS_DARK);
                    } else {
                        notificationBar.setText("Hello World! Using the light theme");
                        notificationBar.getStyleClass().remove(NotificationBar.STYLE_CLASS_DARK);
                    }
                    
                    notificationBar.show();
                }
            }
        });
        root.getChildren().add(showBtn);
        
        return root;
    }
    
    @Override public void start(Stage stage) {
        stage.setTitle("NotificationBar Demo");

        Scene scene = new Scene((Parent) getPanel(stage), 520, 360);

        stage.setScene(scene);
        stage.show();
    }
}
