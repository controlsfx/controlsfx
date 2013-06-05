package org.controlsfx.samples;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.WorkerProgressPane;

/**
 * User: rbair
 * Date: 6/3/13
 * Time: 10:49 AM
 */
public class HelloWorkerProgressPane extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "Worker Progress Pane";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/WorkerProgressPane.html";
    }
    
    
    @Override public Node getPanel(Stage stage) {
        StackPane root = new StackPane();
        Button b = new Button("Press Me");
        final WorkerProgressPane pane = new WorkerProgressPane();
        root.getChildren().addAll(b, pane);

//        Scene scene = new Scene(root, 640, 480);
//        stage.setScene(scene);
//        stage.setTitle("Hello WorkerProgressPane");
//        stage.show();

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
        
        return root;
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Hello WorkerProgressPane");
        
        Scene scene = new Scene((Parent)getPanel(stage), 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
