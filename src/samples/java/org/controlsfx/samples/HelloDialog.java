package org.controlsfx.samples;

import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.DialogsAccessor;
import org.controlsfx.dialog.Dialogs.CommandLink;

public class HelloDialog extends Application implements Sample {

    private final CheckBox cbShowMasthead = new CheckBox("Show Masthead");
    private final CheckBox cbSetOwner = new CheckBox("Set Owner");
    
    @Override public String getSampleName() {
        return "Dialogs";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/dialogs/Dialogs.html";
    }
    
    private static final String WINDOWS = "Windows";
    private static final String MAC_OS = "Mac OS";
    private static final String LINUX = "Linux";
    
    
    private ToggleButton createToggle( final String caption ) {
        final ToggleButton btn = new ToggleButton(caption);
        btn.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
                DialogsAccessor.setMacOS(MAC_OS.equals(caption));
                DialogsAccessor.setWindows(WINDOWS.equals(caption));
                DialogsAccessor.setLinux(LINUX.equals(caption));
            }});
        return btn;
    }    
    
    
    @Override public Node getPanel(final Stage stage) {
     // VBox vbox = new VBox(10);
        // vbox.setAlignment(Pos.CENTER);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);

//        StackPane root = new StackPane();
//        root.getChildren().add(grid);
//        Scene scene = new Scene(root, 800, 300);
//        scene.setFill(Color.WHITE);

        int row = 0;

        // *******************************************************************
        // Information Dialog
        // *******************************************************************

        grid.add(createLabel("Operating system button placement: "), 0, 0);

        final ToggleButton windowsBtn = createToggle(WINDOWS);
        final ToggleButton macBtn = createToggle(MAC_OS);
        final ToggleButton linuxBtn = createToggle(LINUX);
        windowsBtn.selectedProperty().set(true);
        
        SegmentedButton operatingSystem = new SegmentedButton(FXCollections.observableArrayList(windowsBtn, macBtn, linuxBtn));
        
        grid.add(operatingSystem, 1, row, 2, 1);

        row++;
        grid.add(createLabel("Common Dialog attributes: "), 0, 1);
        grid.add(cbShowMasthead, 1, row);
        grid.add(cbSetOwner, 2, row);

        row++;

        // *******************************************************************
        // Information Dialog
        // *******************************************************************

        grid.add(createLabel("Information Dialog: "), 0, row);

        final Button Hyperlink2 = new Button("Show");
        Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Dialogs.create()
                   .owner(cbSetOwner.isSelected() ? stage : null)
                   .title("JavaFX")
                   .masthead(isMastheadVisible() ? "Wouldn't this be nice?" : null)
                   .message("A collection of pre-built JavaFX dialogs?\nSeems like a great idea to me...")
                   .showInformation();
            }
        });
        grid.add(Hyperlink2, 1, row);

        row++;

        // *******************************************************************
        // Confirmation Dialog
        // *******************************************************************

        grid.add(createLabel("Confirmation Dialog: "), 0, row);

        final Button Hyperlink3 = new Button("Show");
        Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Action response =  Dialogs.create()
                    .owner(cbSetOwner.isSelected() ? stage : null)
                    .title("You do want dialogs right?")
                    .masthead(isMastheadVisible() ? "Just Checkin'" : null)
                    .message( "I was a bit worried that you might not want them, so I wanted to double check.")
                    .showConfirm();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink3, 1, row);

        row++;

        // *******************************************************************
        // Warning Dialog
        // *******************************************************************

        grid.add(createLabel("Warning Dialog: "), 0, row);

        final Button Hyperlink6a = new Button("Show");
        Hyperlink6a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Action response = Dialogs.create()
                    .owner(cbSetOwner.isSelected() ? stage : null)
                   .title("I'm warning you!")
                   .masthead(isMastheadVisible() ? "I'm glad I didn't need to use this..." : null)
                   .message("This is a warning")
                   .showWarning();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink6a, 1, row);

        row++;

        // *******************************************************************
        // Error Dialog
        // *******************************************************************

        grid.add(createLabel("Error Dialog: "), 0, row);

        final Button Hyperlink7a = new Button("Show");
        Hyperlink7a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Action response = Dialogs.create()
                      .owner(cbSetOwner.isSelected() ? stage : null)
                      .title("It looks like you're making a bad decision")
                      .message("Exception Encountered")
                      .masthead( isMastheadVisible() ? "Better change your mind - this is really your last chance! Even longer text that should probably wrap" : null)
                      .showError();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink7a, 1, row);

        row++;

        // *******************************************************************
        // More Details Dialog
        // *******************************************************************

        grid.add(createLabel("'Exception' Dialog: "), 0, row);

        final Button Hyperlink5a = new Button("Show");
        Hyperlink5a.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                Action response = Dialogs.create()
                    .owner(cbSetOwner.isSelected() ? stage : null)
                    .title("It looks like you're making a bad decision")
                    .message("Better change your mind - this is really your last chance!")
                    .masthead(isMastheadVisible() ? "Exception Encountered" : null)
                    .showException(new RuntimeException("Pending Bad Decision Exception"));

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5a, 1, row);

        final Button Hyperlink5b = new Button("Open in new window");
        Hyperlink5b.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {

                Action response = Dialogs.create()
                        .owner(cbSetOwner.isSelected() ? stage : null)
                        .message("Better change your mind - this is really your last chance!")
                        .title("It looks like you're making a bad decision")
                        .masthead(isMastheadVisible() ? "Exception Encountered" : null)
                        .showExceptionInNewWindow(new RuntimeException("Pending Bad Decision Exception"));

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink5b, 2, row);

        row++;

        // *******************************************************************
        // Input Dialog (with masthead)
        // *******************************************************************

        grid.add(createLabel("Input Dialog (with Masthead): "), 0, row);

        final Button Hyperlink8 = new Button("TextField");
        Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.create()
                    .owner(cbSetOwner.isSelected() ? stage : null)
                    .title("Name Check")
                    .masthead(isMastheadVisible() ? "Please type in your name" : null)
                    .message("What is your name?")
                    .showTextInput();

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink8, 1, row);

        final Button Hyperlink9 = new Button("Initial Value Set");
        Hyperlink9.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                String response = Dialogs.create()
                        .owner(cbSetOwner.isSelected() ? stage : null)
                        .title("Name Guess")
                        .masthead(isMastheadVisible() ? "Name Guess" : null)
                        .message("Pick a name?")
                        .showTextInput("Jonathan");
                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink9, 2, row);

        final Button Hyperlink10 = new Button("Set Choices (< 10)");
        Hyperlink10.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {

                String response = Dialogs.create()
                      .owner(cbSetOwner.isSelected() ? stage : null)
                      .title("Name Guess")
                      .masthead(isMastheadVisible() ? "Name Guess" : null)
                      .message("Pick a name?")
                      .showChoices("Matthew", "Jonathan", "Ian", "Sue", "Hannah");

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink10, 3, row);

        final Button Hyperlink11 = new Button("Set Choices (>= 10)");
        Hyperlink11.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {

                String response = Dialogs.create()
                        .owner(cbSetOwner.isSelected() ? stage : null)
                        .title("Name Guess")
                        .masthead(isMastheadVisible() ? "Name Guess" : null)
                        .message("Pick a name?")
                        .showChoices("Matthew", "Jonathan", "Ian", "Sue", "Hannah", "Julia", "Denise", "Stephan",
                             "Sarah", "Ron", "Ingrid");

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink11, 4, row);

        row++;
        
        grid.add(createLabel("Command Links: "), 0, row);
        final Button Hyperlink12 = new Button("Show");
        Hyperlink12.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                
                List<CommandLink> links = Arrays.asList(
                     new CommandLink("Add a network that is in the range of this computer", 
                                     "This shows you a list of networks that are currently available and lets you connect to one."),
                     new CommandLink("Manually create a network profile", 
                                     "This creates a new network profile or locates an existing one and saves it on your computer"),
                     new CommandLink("Create an ad hoc network", 
                             "This creates a temporary network for sharing files or and Internet connection"));
                
                
                Action response = Dialogs.create()
                        .owner(cbSetOwner.isSelected() ? stage : null)
                        .title("Manually connect to wireless network")
                        .masthead(isMastheadVisible() ? "Manually connect to wireless network": null)
                        .message("How do you want to add a network?")
                        .showCommandLinks( links.get(1), links );

                System.out.println("response: " + response);
            }
        });
        grid.add(Hyperlink12, 1, row);
        
        return grid;
    }

    @Override public void start(final Stage stage) {
        // setUserAgentStylesheet(STYLESHEET_MODENA);

        stage.setTitle("Dialog Sample");

        Scene scene = new Scene((Parent)getPanel(stage), 800, 300);
        scene.setFill(Color.WHITE);

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

    private boolean isMastheadVisible() {
        return cbShowMasthead.isSelected();
    }
}
