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
        Scene scene = new Scene(root, 700, 300);
        scene.setFill(Color.WHITE);

        
        
        // *******************************************************************
        // Information Dialog
        // *******************************************************************
        
        grid.add(createLabel("Information Dialog: "), 0, 0);
        
        final Hyperlink Hyperlink2 = new Hyperlink();
        Hyperlink2.setText("No Masthead");
        Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.showInformationDialog(stage, 
                        "A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...", 
                        "Wouldn't this be nice for JavaFX");
            }
        });
        grid.add(Hyperlink2, 1, 0);
        
        final Hyperlink Hyperlink2a = new Hyperlink();
        Hyperlink2a.setText("With Masthead");
        Hyperlink2a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.showInformationDialog(stage, 
                        "A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...",
                        "JavaFX",
                        "Wouldn't this be nice?");
            }
        });
        grid.add(Hyperlink2a, 2, 0);
        
        
        
        
        // *******************************************************************
        // Confirmation Dialog
        // *******************************************************************
        
        grid.add(createLabel("Confirmation Dialog: "), 0, 1);
        
        final Hyperlink Hyperlink3 = new Hyperlink();
        Hyperlink3.setText("No Masthead");
        Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showConfirmDialog(stage, 
                        "I was a bit worried that you might not want them, so I wanted to double check.",
                        "You do want dialogs right?");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink3, 1, 1);
        
        final Hyperlink Hyperlink3a = new Hyperlink();
        Hyperlink3a.setText("With Masthead");
        Hyperlink3a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showConfirmDialog(stage, 
                        "I was a bit worried that you might not want them, so I wanted to double check.",
                        "You do want dialogs right?",
                        "Just Checkin'");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink3a, 2, 1);
        
        
        
        
        // *******************************************************************
        // Warning Dialog
        // *******************************************************************
        
        grid.add(createLabel("Warning Dialog: "), 0, 2);
        
        final Hyperlink Hyperlink6 = new Hyperlink();
        Hyperlink6.setText("No Masthead");
        Hyperlink6.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showWarningDialog(stage, 
                        "This is a warning", 
                        "I'm warning you!");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink6, 1, 2);
        
        final Hyperlink Hyperlink6a = new Hyperlink();
        Hyperlink6a.setText("With Masthead");
        Hyperlink6a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showWarningDialog(stage, 
                        "This is a warning", 
                        "I'm warning you!", 
                        "I'm glad I didn't need to use this...");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink6a, 2, 2);
        
        
        
        
        // *******************************************************************
        // Error Dialog
        // *******************************************************************
        
        grid.add(createLabel("Error Dialog: "), 0, 3);
        
        final Hyperlink Hyperlink7 = new Hyperlink();
        Hyperlink7.setText("No Masthead");
        Hyperlink7.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Exception Encountered", 
                        "It looks like you're making a bad decision");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink7, 1, 3);
        
        final Hyperlink Hyperlink7a = new Hyperlink();
        Hyperlink7a.setText("With Masthead");
        Hyperlink7a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Exception Encountered", 
                        "It looks like you're making a bad decision", 
                        "Better change your mind - this is really your last chance!");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink7a, 2, 3);
        
        
        
        
        // *******************************************************************
        // Exception Dialog
        // *******************************************************************
        
        grid.add(createLabel("Exception Dialog: "), 0, 4);
        
        final Hyperlink Hyperlink5 = new Hyperlink();
        Hyperlink5.setText("No Masthead");
        Hyperlink5.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Better change your mind - this is really your last chance!",
                        "It looks like you're making a bad decision", 
                        null, 
                        new RuntimeException("Pending Bad Decision Exception"));
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5, 1, 4);
        
        final Hyperlink Hyperlink5a = new Hyperlink();
        Hyperlink5a.setText("With Masthead");
        Hyperlink5a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.DialogResponse response = Dialogs.showErrorDialog(stage, 
                        "Better change your mind - this is really your last chance!",
                        "It looks like you're making a bad decision", 
                        "Exception Encountered", 
                        new RuntimeException("Pending Bad Decision Exception"));
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5a, 2, 4);
        
        
        
        // *******************************************************************
        // Input Dialog (with masthead)
        // *******************************************************************
        
        grid.add(createLabel("Input Dialog (with Masthead): "), 0, 5);
        
        final Hyperlink Hyperlink8 = new Hyperlink();
        Hyperlink8.setText("TextField");
        Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "What is your name?",
                        "Name Check",
                        "Please type in your name");
                System.out.println("response: " + response);
            }
        });
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
        
        
        
        // *******************************************************************
        // Input Dialog (without masthead)
        // *******************************************************************
        
        grid.add(createLabel("Input Dialog (no Masthead): "), 0, 6);
        
        final Hyperlink Hyperlink20 = new Hyperlink();
        Hyperlink20.setText("TextField");
        Hyperlink20.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "What is your name?",
                        "Name Check");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink20, 1, 6);
        
        final Hyperlink Hyperlink21 = new Hyperlink();
        Hyperlink21.setText("Initial Value Set");
        Hyperlink21.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        null,
                        "Jonathan");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink21, 2, 6);
        
        final Hyperlink Hyperlink22 = new Hyperlink();
        Hyperlink22.setText("Set Choices (< 10)");
        Hyperlink22.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        null,
                        "Jonathan",
                        "Matthew", "Jonathan", "Ian", "Sue", "Hannah");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink22, 3, 6);
        
        final Hyperlink Hyperlink23 = new Hyperlink();
        Hyperlink23.setText("Set Choices (>= 10)");
        Hyperlink23.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.showInputDialog(stage, 
                        "Pick a name?",
                        "Name Guess",
                        null,
                        "Jonathan",
                        "Matthew", "Jonathan", "Ian", "Sue", "Hannah", 
                        "Julia", "Denise", "Stephan", "Sarah", "Ron", "Ingrid");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink23, 4, 6);
        
        
        
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
