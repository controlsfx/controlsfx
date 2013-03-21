package org.controlsfx.samples;

import org.controlsfx.dialogs.Dialogs;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class HelloDialog extends Application {

    @Override public void start(final Stage stage) {
        stage.setTitle("Dialog Sample");

//        VBox vbox = new VBox(10);
//        vbox.setAlignment(Pos.CENTER);
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(15);
        grid.setVgap(15);
        
        StackPane root = new StackPane();
        root.getChildren().add(grid);
        Scene scene = new Scene(root, 600, 300);
        scene.setFill(Color.WHITE);

        final Hyperlink Hyperlink2 = new Hyperlink();
        Hyperlink2.setText("Basic");
        Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.showInformationDialog(stage, 
                        "A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...", 
                        "Wouldn't this be nice?", 
                        "JavaFX 2.1");
            }
        });
        grid.add(createLabel("Information Dialog: "), 0, 0);
        grid.add(Hyperlink2, 1, 0);
        
        final Hyperlink Hyperlink3 = new Hyperlink();
        Hyperlink3.setText("Basic");
        Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showConfirmDialog(stage, 
                        "I was a bit worried that you might not want them, so I wanted to double check.",
                        "You do want dialogs right?",
                        "Just Checkin'");
                System.out.println("response: " + response);
            }
        });
        grid.add(createLabel("Confirmation Dialog: "), 0, 1);
        grid.add(Hyperlink3, 1, 1);
        
        final Hyperlink Hyperlink6 = new Hyperlink();
        Hyperlink6.setText("Basic");
        Hyperlink6.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showWarningDialog(stage, 
                        "This is a warning", 
                        "I'm warning you!", 
                        "I'm glad I didn't need to use this...");
                System.out.println("response: " + response);
            }
        });
        grid.add(createLabel("Warning Dialog: "), 0, 2);
        grid.add(Hyperlink6, 1, 2);
        
//        final Hyperlink Hyperlink4 = new Hyperlink();
//        Hyperlink4.setText("Scrollable Content Dialog");
//        Hyperlink4.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(ActionEvent e) {
//                        "Scrollable Content Dialog", 
//                        "A nice long message....\n\n\nThat\ncan\neasily\nbe\nsplit\nover\nmultiple\nlines....");
//                System.out.println("response: " + response);
//            }
//        });
//        vbox.getChildren().add(Hyperlink4);
        
//        final Hyperlink Hyperlink4 = new Hyperlink();
//        Hyperlink4.setText("List Dialog");
//        Hyperlink4.setOnAction(new EventHandler<ActionEvent>() {
//            @Override public void handle(ActionEvent e) {
//                System.out.println("response: " + response);
//            }
//        });
//        vbox.getChildren().add(Hyperlink4);
        
        final Hyperlink Hyperlink7 = new Hyperlink();
        Hyperlink7.setText("Basic");
        Hyperlink7.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Exception Encountered", 
                        "It looks like you're making a bad decision", 
                        "Better change your mind - this is really your last chance!");
                System.out.println("response: " + response);
            }
        });
        grid.add(createLabel("Error Dialog: "), 0, 3);
        grid.add(Hyperlink7, 1, 3);
        
        final Hyperlink Hyperlink5 = new Hyperlink();
        Hyperlink5.setText("Basic");
        Hyperlink5.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Better change your mind - this is really your last chance!",
                        "It looks like you're making a bad decision", 
                        "Exception Encountered", 
                        new RuntimeException("Pending Bad Decision Exception"));
                System.out.println("response: " + response);
            }
        });
        grid.add(createLabel("Exception Dialog: "), 0, 4);
        grid.add(Hyperlink5, 1, 4);
        
        
        // Input dialog tests
        final Hyperlink Hyperlink8 = new Hyperlink();
        Hyperlink8.setText("TextField");
        Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "What is your name?",
                        "Name Check");
                System.out.println("response: " + response);
            }
        });
        grid.add(createLabel("Input Dialog: "), 0, 5);
        grid.add(Hyperlink8, 1, 5);
        
        final Hyperlink Hyperlink9 = new Hyperlink();
        Hyperlink9.setText("Initial Value Set");
        Hyperlink9.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        "Name Guess",
                        "Jonathan");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink9, 2, 5);
        
        final Hyperlink Hyperlink10 = new Hyperlink();
        Hyperlink10.setText("Set Choices (< 10)");
        Hyperlink10.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        "Name Guess",
                        "Jonathan",
                        "Matthew", "Jonathan", "Ian", "Sue", "Hannah");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink10, 3, 5);
        
        final Hyperlink Hyperlink11 = new Hyperlink();
        Hyperlink11.setText("Set Choices (>= 10)");
        Hyperlink11.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        "Name Guess",
                        "Jonathan",
                        "Matthew", "Jonathan", "Ian", "Sue", "Hannah", 
                        "Julia", "Denise", "Stephan", "Sarah", "Ron", "Ingrid");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink11, 4, 5);
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Application.launch(args);
    }
    
    private Node createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Amble, Arial", 13));
        label.setTextFill(Color.BLUE);
        return label;
    }
}
