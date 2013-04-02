package org.controlsfx.samples;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.controlsfx.dialogs.Dialog;

public class HelloDialog extends Application {

	CheckBox cbShowMasthead = new CheckBox("Show Masthead");

	@Override
	public void start(final Stage stage) {
		// setUserAgentStylesheet(STYLESHEET_MODENA);

		stage.setTitle("Dialog Sample");

		// VBox vbox = new VBox(10);
		// vbox.setAlignment(Pos.CENTER);

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setHgap(10);
		grid.setVgap(10);

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
		final ChoiceBox<String> operatingSystem = new ChoiceBox<>(FXCollections.observableArrayList(WINDOWS_UNIX,
		        MAC_OS));
		operatingSystem.getSelectionModel().select(WINDOWS_UNIX);
		operatingSystem.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				String os = operatingSystem.getSelectionModel().getSelectedItem();
				Dialog.setMacOS(MAC_OS.equals(os));
				Dialog.setWindows(WINDOWS_UNIX.equals(os));
			}
		});
		grid.add(operatingSystem, 1, row);

		row++;
		grid.add(createLabel("Common Dialog attributes: "), 0, 1);
		grid.add(cbShowMasthead, 1, row);

		row++;

		// *******************************************************************
		// Information Dialog
		// *******************************************************************

		grid.add(createLabel("Information Dialog: "), 0, row);

		final Button Hyperlink2 = new Button();
		Hyperlink2.setText("Show");
		Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<?> dlg = Dialog.build(stage)
				        .message("A collection of pre-built JavaFX dialogs?\n\nSeems like a great idea to me...")
				        .title("JavaFX");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Wouldn't this be nice?");
				dlg.showInformationDialog();
			}
		});
		grid.add(Hyperlink2, 1, row);

		row++;

		// *******************************************************************
		// Confirmation Dialog
		// *******************************************************************

		grid.add(createLabel("Confirmation Dialog: "), 0, row);

		final Button Hyperlink3 = new Button();
		Hyperlink3.setText("Show");
		Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<?> dlg = Dialog.build(stage)
				        .message("I was a bit worried that you might not want them, so I wanted to double check.")
				        .title("You do want dialogs right?");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Just Checkin'");
				Dialog.Response response = dlg.showConfirmDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink3, 1, row);

		row++;

		// *******************************************************************
		// Warning Dialog
		// *******************************************************************

		grid.add(createLabel("Warning Dialog: "), 0, row);

		final Button Hyperlink6a = new Button();
		Hyperlink6a.setText("Shpw");
		Hyperlink6a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<?> dlg = Dialog.build(stage).message("This is a warning").title("I'm warning you!");
				if (cbShowMasthead.isSelected())
					dlg.masthead("I'm glad I didn't need to use this...");
				Dialog.Response response = dlg.showWarningDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink6a, 1, row);

		row++;

		// *******************************************************************
		// Error Dialog
		// *******************************************************************

		grid.add(createLabel("Error Dialog: "), 0, row);

		final Button Hyperlink7a = new Button();
		Hyperlink7a.setText("Show");
		Hyperlink7a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<?> dlg = Dialog.build(stage).message("Exception Encountered")
				        .title("It looks like you're making a bad decision");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Better change your mind - this is really your last chance!");
				Dialog.Response response = dlg.showErrorDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink7a, 1, row);

		row++;

		// *******************************************************************
		// More Details Dialog
		// *******************************************************************

		grid.add(createLabel("'More Details' Dialog: "), 0, row);

		final Button Hyperlink5a = new Button();
		Hyperlink5a.setText("Show in the same window");
		Hyperlink5a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<?> dlg = Dialog.build(stage)
				        .message("Better change your mind - this is really your last chance!")
				        .title("It looks like you're making a bad decision")
				        .details(new RuntimeException("Pending Bad Decision Exception"));
				if (cbShowMasthead.isSelected())
					dlg.masthead("Exception Encountered");
				Dialog.Response response = dlg.showMoreDetailsDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink5a, 1, row);

		final Button Hyperlink5b = new Button();
		Hyperlink5b.setText("Open in new window");
		Hyperlink5b.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				Dialog<?> dlg = Dialog.build(stage)
				        .message("Better change your mind - this is really your last chance!")
				        .title("It looks like you're making a bad decision")
				        .details(new RuntimeException("Pending Bad Decision Exception")).openDetailsInNewWindow(true);
				if (cbShowMasthead.isSelected())
					dlg.masthead("Exception Encountered");
				Dialog.Response response = dlg.showMoreDetailsDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink5b, 2, row);

		row++;

		// *******************************************************************
		// Input Dialog (with masthead)
		// *******************************************************************

		grid.add(createLabel("Input Dialog (with Masthead): "), 0, row);

		final Button Hyperlink8 = new Button();
		Hyperlink8.setText("TextField");
		Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<String> dlg = Dialog.<String> build(stage).message("What is your name?").title("Name Check");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Please type in your name");
				String response = dlg.showInputDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink8, 1, row);

		final Button Hyperlink9 = new Button();
		Hyperlink9.setText("Initial Value Set");
		Hyperlink9.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<String> dlg = Dialog.<String> build(stage).message("Pick a name?").title("Name Guess")
				        .inputInitialValue("Jonathan");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Name Guess");
				String response = dlg.showInputDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink9, 2, row);

		final Button Hyperlink10 = new Button();
		Hyperlink10.setText("Set Choices (< 10)");
		Hyperlink10.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<String> dlg = Dialog.<String> build(stage).message("Pick a name?").title("Name Guess")
				        .inputInitialValue("Jonathan").inputChoices("Matthew", "Jonathan", "Ian", "Sue", "Hannah");
				if (cbShowMasthead.isSelected())
					dlg.masthead("Name Guess");
				String response = dlg.showInputDialog();
				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink10, 3, row);

		final Button Hyperlink11 = new Button();
		Hyperlink11.setText("Set Choices (>= 10)");
		Hyperlink11.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Dialog<String> dlg = Dialog
				        .<String> build(stage)
				        .message("Pick a name?")
				        .title("Name Guess")
				        .inputInitialValue("Jonathan")
				        .inputChoices("Matthew", "Jonathan", "Ian", "Sue", "Hannah", "Julia", "Denise", "Stephan",
				                "Sarah", "Ron", "Ingrid");

				if (cbShowMasthead.isSelected())
					dlg.masthead("Name Guess");
				String response = dlg.showInputDialog();

				System.out.println("response: " + response);
			}
		});
		grid.add(Hyperlink11, 4, row);

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
