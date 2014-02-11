package org.controlsfx.samples;

import java.util.Random;

import org.controlsfx.control.NotificationPopup;
import org.controlsfx.control.NotificationPopup.Notification;
import org.controlsfx.control.NotificationPopup.Notifications;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class HelloNotificationPopup extends Application {
    
    private Notification[] notifications = new Notification[] {
        Notifications.create().text("Hello World!").build(),
        Notifications.create().text("Top-right notification").position(Pos.TOP_RIGHT).build()
    };

    public static void main(String[] args) {
        launch(args);
    }
    
    public void start(final Stage stage) {
        Button showBtn = new Button("Show Notification");
        showBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Random r = new Random();
                new NotificationPopup().show(stage, notifications[r.nextInt(notifications.length)]);
            }
        });
        
        VBox vbox = new VBox(10, showBtn);
        
        Scene scene = new Scene(vbox, 200, 200);
        stage.setScene(scene);
        stage.show();
    }
}
