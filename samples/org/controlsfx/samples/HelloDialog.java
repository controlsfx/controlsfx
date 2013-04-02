package org.controlsfx.samples;

import java.util.Arrays;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.controlsfx.dialogs.Dialog;

public class HelloDialog extends Application {

    @Override
    public void start(final Stage stage) {
        setUserAgentStylesheet(STYLESHEET_MODENA);

        stage.setTitle("Dialog Sample");

        // VBox vbox = new VBox(10);
        // vbox.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(15);
        grid.setVgap(15);

        StackPane root = new StackPane();
        root.getChildren().add(grid);
        Scene scene = new Scene(root, 800, 300);
        scene.setFill(Color.WHITE);

        int row = 0;

        // *******************************************************************
        // Information Dialog
        // *******************************************************************

        grid.add(createLabel("Operating system button placement: "), 0, 0);

        final String WINDOWS_UNIX = "Windows / Unix";
        final String MAC_OS = "Mac OS";
        final ChoiceBox<String> operatingSystem = new ChoiceBox<>(
                FXCollections.observableArrayList(WINDOWS_UNIX, MAC_OS));
        operatingSystem.getSelectionModel().select(WINDOWS_UNIX);
        operatingSystem.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(
                    ObservableValue<? extends String> observable,
                    String oldValue, String newValue) {
                String os = operatingSystem.getSelectionModel()
                        .getSelectedItem();
                Dialog.setMacOS(MAC_OS.equals(os));
                Dialog.setWindows(WINDOWS_UNIX.equals(os));
            }
        });
        grid.add(operatingSystem, 1, row);

        row++;

        // *******************************************************************
        // Information Dialog
        // *******************************************************************

        grid.add(createLabel("Information Dialog: "), 0, row);

        final Hyperlink Hyperlink2 = new Hyperlink();
        Hyperlink2.setText("No Masthead");
        Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.build(stage)
                .message(
                        "A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...")
                        .title("JavaFX").showInformationDialog();
            }
        });
        grid.add(Hyperlink2, 1, row);

        final Hyperlink Hyperlink2a = new Hyperlink();
        Hyperlink2a.setText("With Masthead");
        Hyperlink2a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.build(stage)
                .message(
                        "A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...")
                        .title("JavaFX").masthead("Wouldn't this be nice?")
                        .showInformationDialog();
            }
        });
        grid.add(Hyperlink2a, 2, row);

        row++;

        // *******************************************************************
        // Confirmation Dialog
        // *******************************************************************

        grid.add(createLabel("Confirmation Dialog: "), 0, row);

        final Hyperlink Hyperlink3 = new Hyperlink();
        Hyperlink3.setText("No Masthead");
        Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message(
                                "I was a bit worried that you might not want them, so I wanted to double check.")
                                .title("You do want dialogs right?")
                                .showConfirmDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink3, 1, row);

        final Hyperlink Hyperlink3a = new Hyperlink();
        Hyperlink3a.setText("With Masthead");
        Hyperlink3a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message(
                                "I was a bit worried that you might not want them, so I wanted to double check.")
                                .title("You do want dialogs right?")
                                .masthead("Just Checkin'").showConfirmDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink3a, 2, row);

        row++;

        // *******************************************************************
        // Warning Dialog
        // *******************************************************************

        grid.add(createLabel("Warning Dialog: "), 0, row);

        final Hyperlink Hyperlink6 = new Hyperlink();
        Hyperlink6.setText("No Masthead");
        Hyperlink6.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog.build(stage)
                        .message("This is a warning").title("I'm warning you!")
                        .showWarningDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink6, 1, row);

        final Hyperlink Hyperlink6a = new Hyperlink();
        Hyperlink6a.setText("With Masthead");
        Hyperlink6a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog.build(stage)
                        .message("This is a warning").title("I'm warning you!")
                        .masthead("I'm glad I didn't need to use this...")
                        .showWarningDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink6a, 2, row);

        row++;

        // *******************************************************************
        // Error Dialog
        // *******************************************************************

        grid.add(createLabel("Error Dialog: "), 0, row);

        final Hyperlink Hyperlink7 = new Hyperlink();
        Hyperlink7.setText("No Masthead");
        Hyperlink7.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog.build(stage)
                        .message("Exception Encountered")
                        .title("It looks like you're making a bad decision")
                        .showErrorDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink7, 1, row);

        final Hyperlink Hyperlink7a = new Hyperlink();
        Hyperlink7a.setText("With Masthead");
        Hyperlink7a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message("Exception Encountered")
                        .title("It looks like you're making a bad decision")
                        .masthead(
                                "Better change your mind - this is really your last chance!")
                                .showErrorDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink7a, 2, row);

        row++;

        // *******************************************************************
        // More Details Dialog
        // *******************************************************************

        grid.add(createLabel("'More Details' Dialog: "), 0, row);

        final Hyperlink Hyperlink5 = new Hyperlink();
        Hyperlink5.setText("No Masthead");
        Hyperlink5.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message(
                                "Better change your mind - this is really your last chance!")
                                .title("It looks like you're making a bad decision")
                                .details(
                                        new RuntimeException(
                                                "Pending Bad Decision Exception"))
                                                .showMoreDetailsDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5, 1, row);

        final Hyperlink Hyperlink5a = new Hyperlink();
        Hyperlink5a.setText("With Masthead");
        Hyperlink5a.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message(
                                "Better change your mind - this is really your last chance!")
                                .title("It looks like you're making a bad decision")
                                .masthead("Exception Encountered")
                                .details(
                                        new RuntimeException(
                                                "Pending Bad Decision Exception"))
                                                .showMoreDetailsDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5a, 2, row);

        final Hyperlink Hyperlink5b = new Hyperlink();
        Hyperlink5b.setText("Open in new window");
        Hyperlink5b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Dialog.Response response = Dialog
                        .build(stage)
                        .message(
                                "Better change your mind - this is really your last chance!")
                                .title("It looks like you're making a bad decision")
                                .masthead("Exception Encountered")
                                .details(
                                        new RuntimeException(
                                                "Pending Bad Decision Exception"))
                                                .openDetailsInNewWindow(true).showMoreDetailsDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5b, 3, row);

        row++;

        // *******************************************************************
        // Input Dialog (with masthead)
        // *******************************************************************

        grid.add(createLabel("Input Dialog (with Masthead): "), 0, row);

        final Hyperlink Hyperlink8 = new Hyperlink();
        Hyperlink8.setText("TextField");
        Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog.<String> build(stage)
                        .message("What is your name?").title("Name Check")
                        .masthead("Please type in your name").showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink8, 1, row);

        final Hyperlink Hyperlink9 = new Hyperlink();
        Hyperlink9.setText("Initial Value Set");
        Hyperlink9.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog.<String> build(stage)
                        .message("Pick a name?").title("Name Guess")
                        .masthead("Name Guess").inputInitialValue("Jonathan")
                        .showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink9, 2, row);

        final Hyperlink Hyperlink10 = new Hyperlink();
        Hyperlink10.setText("Set Choices (< 10)");
        Hyperlink10.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog
                        .<String> build(stage)
                        .message("Pick a name?")
                        .title("Name Guess")
                        .masthead("Name Guess")
                        .inputInitialValue("Jonathan")
                        .inputChoices(
                                Arrays.asList("Matthew", "Jonathan", "Ian",
                                        "Sue", "Hannah")).showInputDialog();
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink10, 3, row);

        final Hyperlink Hyperlink11 = new Hyperlink();
        Hyperlink11.setText("Set Choices (>= 10)");
        Hyperlink11.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog
                        .<String> build(stage)
                        .message("Pick a name?")
                        .title("Name Guess")
                        .masthead("Name Guess")
                        .inputInitialValue("Jonathan")
                        .inputChoices(
                                Arrays.asList("Matthew", "Jonathan", "Ian",
                                        "Sue", "Hannah", "Julia", "Denise",
                                        "Stephan", "Sarah", "Ron", "Ingrid"))
                                        .showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink11, 4, row);

        row++;

        // *******************************************************************
        // Input Dialog (without masthead)
        // *******************************************************************

        grid.add(createLabel("Input Dialog (no Masthead): "), 0, row);

        final Hyperlink Hyperlink20 = new Hyperlink();
        Hyperlink20.setText("TextField");
        Hyperlink20.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog.<String> build(stage)
                        .message("What is your name?").title("Name Check")
                        .showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink20, 1, row);

        final Hyperlink Hyperlink21 = new Hyperlink();
        Hyperlink21.setText("Initial Value Set");
        Hyperlink21.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog.<String> build(stage)
                        .message("Pick a name?").title("Name Guess")
                        .inputInitialValue("Jonathan").showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink21, 2, row);

        final Hyperlink Hyperlink22 = new Hyperlink();
        Hyperlink22.setText("Set Choices (< 10)");
        Hyperlink22.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog
                        .<String> build(stage)
                        .message("Pick a name?")
                        .title("Name Guess")
                        .inputInitialValue("Jonathan")
                        .inputChoices(
                                Arrays.asList("Matthew", "Jonathan", "Ian",
                                        "Sue", "Hannah")).showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink22, 3, row);

        final Hyperlink Hyperlink23 = new Hyperlink();
        Hyperlink23.setText("Set Choices (>= 10)");
        Hyperlink23.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String response = Dialog
                        .<String> build(stage)
                        .message("Pick a name?")
                        .title("Name Guess")
                        .inputInitialValue("Jonathan")
                        .inputChoices(
                                Arrays.asList("Matthew", "Jonathan", "Ian",
                                        "Sue", "Hannah", "Julia", "Denise",
                                        "Stephan", "Sarah", "Ron", "Ingrid"))
                                        .showInputDialog();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink23, 4, row);

        row++;

        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args
     *            the command line arguments
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
