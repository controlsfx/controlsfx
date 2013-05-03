package org.controlsfx.samples;

import java.util.Arrays;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.tools.ButtonBar;

public class HelloButtonBar extends Application {
    
     @Override public void start(Stage stage) throws Exception {
        stage.setTitle("ButtonBar Demo");
        
        
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        
        ButtonBar bar = new ButtonBar();
        bar.getButtons().addAll(Arrays.asList(
                createButton("OK", 'O'),
                createButton("Cancel", 'C'),
                createButton("Left 1", 'L'),
                createButton("Left 2", 'L'),
                createButton("Left 3", 'L'),
                createButton("Right 1", 'R'),
                createButton("Unknown 1", '*'),
                createButton("Help(R)", 'H'),
                createButton("Help(L)", 'E'),
                createButton("Unknown 2", '*'),
                createButton("Yes", 'Y'),
                createButton("No", 'N'),
                createButton("Next", 'X'),
                createButton("Unknown 3", '*'),
                createButton("Back", 'B'),
                createButton("Right 2", 'R'),
                createButton("Finish", 'I'),
                createButton("Right 3", 'R'),
                createButton("Apply", 'A')
                
        ));
        root.setBottom( bar );
        
        Scene scene = new Scene(root, 800, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    private Button createButton( String title, char type) {
        Button button = new Button(title);
        ButtonBar.setType(button, type);
        return button;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}