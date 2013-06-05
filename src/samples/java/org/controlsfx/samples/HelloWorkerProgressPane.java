package org.controlsfx.samples;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.controlsfx.control.WorkerProgressPane;

/**
 * User: rbair
 * Date: 6/3/13
 * Time: 10:49 AM
 */
public class HelloWorkerProgressPane extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        StackPane root = new StackPane();
        Button b = new Button("Press Me");
        final WorkerProgressPane pane = new WorkerProgressPane();
        root.getChildren().addAll(b, pane);

        Scene scene = new Scene(root, 640, 480);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Hello WorkerProgressPane");
        primaryStage.show();

        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Task t = new Task() {
                    @Override protected Object call() throws Exception {
                        for (int i=0; i<100; i++) {
                            updateProgress(i, 99);
                            Thread.sleep(100);
                        }
                        return null;
                    }
                };
                pane.setWorker(t);
                Thread th = new Thread(t);
                th.setDaemon(true);
                th.start();
    
                System.out.println("Started");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
