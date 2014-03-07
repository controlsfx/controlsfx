/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.samples.dialogs;

import static org.controlsfx.dialog.Dialog.Actions.NO;
import static org.controlsfx.dialog.Dialog.Actions.YES;
import impl.org.controlsfx.i18n.Localization;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.AbstractDialogAction;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialog.ActionTrait;
import org.controlsfx.dialog.Dialogs;
import org.controlsfx.dialog.Dialogs.CommandLink;
import org.controlsfx.dialog.Dialogs.UserInfo;
import org.controlsfx.dialog.DialogsAccessor;
import org.controlsfx.samples.Utils;

public class HelloDialog extends ControlsFXSample {

	private final CheckBox cbUseLightweightDialog = new CheckBox(
			"Use Lightweight Dialogs");
	private final CheckBox cbUseNativeTitleBar = new CheckBox(
			"Use Native TitleBar");
	private final CheckBox cbShowMasthead = new CheckBox("Show Masthead");
	private final CheckBox cbSetOwner = new CheckBox("Set Owner");

	@Override
	public String getSampleName() {
		return "Dialogs";
	}

	@Override
	public String getJavaDocURL() {
		return Utils.JAVADOC_BASE + "org/controlsfx/dialog/Dialogs.html";
	}

	@Override
	public String getSampleDescription() {
		return "The Dialogs class is a simple (yet flexible) API for showing the most common forms of "
				+ "(modal) UI dialogs. This class contains a fluent API to make "
				+ "building and customizing the pre-built dialogs really easy, "
				+ "but for those developers who want complete control, you may "
				+ "be interested in instead using the Dialog class (which "
				+ "is what all of these pre-built dialogs use as well).";
	}

	private static final String WINDOWS = "Windows";
	private static final String MAC_OS = "Mac OS";
	private static final String LINUX = "Linux";

	private Stage stage;

	private ToggleButton createToggle(final String caption) {
		final ToggleButton btn = new ToggleButton(caption);
		btn.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean oldValue, Boolean newValue) {
				DialogsAccessor.setMacOS(MAC_OS.equals(caption));
				DialogsAccessor.setWindows(WINDOWS.equals(caption));
				DialogsAccessor.setLinux(LINUX.equals(caption));
			}
		});
		return btn;
	}

	private boolean includeOwner() {
		return cbSetOwner.isSelected() || cbUseLightweightDialog.isSelected();
	}

	@Override
	public Node getPanel(final Stage stage) {
		this.stage = stage;
		// VBox vbox = new VBox(10);
		// vbox.setAlignment(Pos.CENTER);

		GridPane grid = new GridPane();
		grid.setPadding(new Insets(10, 10, 10, 10));
		grid.setHgap(10);
		grid.setVgap(10);

		int row = 0;

		// *******************************************************************
		// Information Dialog
		// *******************************************************************

		grid.add(createLabel("Information Dialog: "), 0, row);

		final Button Hyperlink2 = new Button("Show");
		Hyperlink2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				configureSampleDialog(
						Dialogs.create()
								.title("JavaFX")
								.masthead(
										isMastheadVisible() ? "Wouldn't this be nice?"
												: null)
								.message(
										"A collection of pre-built JavaFX dialogs?\nSeems like a great idea to me..."))
						.showInformation();
			}
		});
		grid.add(new HBox(10, Hyperlink2), 1, row);

		row++;

		// *******************************************************************
		// Confirmation Dialog
		// *******************************************************************

		grid.add(createLabel("Confirmation Dialog: "), 0, row);

		final CheckBox cbShowCancel = new CheckBox("Show Cancel Button");
		cbShowCancel.setSelected(true);

		final Button Hyperlink3 = new Button("Show");
		Hyperlink3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Action response = configureSampleDialog(
						Dialogs.create()
								.title("You do want dialogs right?")
								.masthead(
										isMastheadVisible() ? "Just Checkin'"
												: null)
								.message(
										"I was a bit worried that you might not want them, so I wanted to double check."))
						.actions(
								!cbShowCancel.isSelected() ? new Action[] {
										YES, NO } : new Action[0])
						.showConfirm();

				System.out.println("response: " + response);
			}
		});
		grid.add(new HBox(10, Hyperlink3, cbShowCancel), 1, row);

		row++;

		// *******************************************************************
		// Warning Dialog
		// *******************************************************************

		grid.add(createLabel("Warning Dialog: "), 0, row);

		final Button Hyperlink6a = new Button("Show");
		Hyperlink6a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Action response = configureSampleDialog(
						Dialogs.create()
								.title("I'm warning you!")
								.masthead(
										isMastheadVisible() ? "I'm glad I didn't need to use this..."
												: null)
								.message("This is a warning")).showWarning();

				System.out.println("response: " + response);
			}
		});
		grid.add(new HBox(10, Hyperlink6a), 1, row);

		row++;

		// *******************************************************************
		// Error Dialog
		// *******************************************************************

		grid.add(createLabel("Error Dialog: "), 0, row);

		final Button Hyperlink7a = new Button("Show");
		Hyperlink7a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Action response = configureSampleDialog(
						Dialogs.create()
								.title("It looks like you're making a bad decision")
								.message("Exception Encountered")
								.masthead(
										isMastheadVisible() ? "Better change your mind - this is really your last chance! Even longer text that should probably wrap"
												: null)).showError();

				System.out.println("response: " + response);
			}
		});
		grid.add(new HBox(10, Hyperlink7a), 1, row);

		row++;

		// *******************************************************************
		// More Details Dialog
		// *******************************************************************

		grid.add(createLabel("'Exception' Dialog: "), 0, row);

		final Button Hyperlink5a = new Button("Show");
		Hyperlink5a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Action response = configureSampleDialog(
						Dialogs.create()
								.title("It looks like you're making a bad decision")
								.message(
										"Better change your mind - this is really your last chance!")
								.masthead(
										isMastheadVisible() ? "Exception Encountered"
												: null)).showException(
						new RuntimeException("Pending Bad Decision Exception"));

				System.out.println("response: " + response);
			}
		});

		final Button Hyperlink5b = new Button("Open in new window");
		Hyperlink5b.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Action response = configureSampleDialog(
						Dialogs.create()
								.message(
										"Better change your mind - this is really your last chance!")
								.title("It looks like you're making a bad decision")
								.masthead(
										isMastheadVisible() ? "Exception Encountered"
												: null))
						.showExceptionInNewWindow(
								new RuntimeException(
										"Pending Bad Decision Exception"));

				System.out.println("response: " + response);
			}
		});

		grid.add(new HBox(10, Hyperlink5a, Hyperlink5b), 1, row);
		row++;

		// *******************************************************************
		// Input Dialog (with masthead)
		// *******************************************************************

		grid.add(createLabel("Input Dialog: "), 0, row);

		final Button Hyperlink8 = new Button("TextField");
		Hyperlink8.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String response = configureSampleDialog(
						Dialogs.create()
								.title("Name Check")
								.masthead(
										isMastheadVisible() ? "Please type in your name"
												: null)
								.message("What is your name?")).showTextInput();

				System.out.println("response: " + response);
			}
		});

		final Button Hyperlink9 = new Button("Initial Value Set");
		Hyperlink9.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String response = configureSampleDialog(
						Dialogs.create()
								.title("Name Guess")
								.masthead(
										isMastheadVisible() ? "Name Guess"
												: null).message("Pick a name?"))
						.showTextInput("Jonathan");
				System.out.println("response: " + response);
			}
		});

		final Button Hyperlink10 = new Button("Set Choices (< 10)");
		Hyperlink10.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String response = configureSampleDialog(
						Dialogs.create()
								.title("Name Guess")
								.masthead(
										isMastheadVisible() ? "Name Guess"
												: null).message("Pick a name?"))
						.showChoices("Matthew", "Jonathan", "Ian", "Sue",
								"Hannah");

				System.out.println("response: " + response);
			}
		});

		final Button Hyperlink11 = new Button("Set Choices (>= 10)");
		Hyperlink11.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String response = configureSampleDialog(
						Dialogs.create()
								.title("Name Guess")
								.masthead(
										isMastheadVisible() ? "Name Guess"
												: null).message("Pick a name?"))
						.showChoices("Matthew", "Jonathan", "Ian", "Sue",
								"Hannah", "Julia", "Denise", "Stephan",
								"Sarah", "Ron", "Ingrid");

				System.out.println("response: " + response);
			}
		});

		grid.add(
				new HBox(10, Hyperlink8, Hyperlink9, Hyperlink10, Hyperlink11),
				1, row);
		row++;

		// *******************************************************************
		// Command links
		// *******************************************************************

		grid.add(createLabel("Other pre-built dialogs: "), 0, row);
		final Button Hyperlink12 = new Button("Command Links");
		Hyperlink12.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				List<CommandLink> links = Arrays
						.asList(new CommandLink(
								"Add a network that is in the range of this computer",
								"This shows you a list of networks that are currently available and lets you connect to one."),
								new CommandLink(
										"Manually create a network profile",
										"This creates a new network profile or locates an existing one and saves it on your computer"),
								new CommandLink("Create an ad hoc network",
										"This creates a temporary network for sharing files or and Internet connection"));

				Action response = configureSampleDialog(
						Dialogs.create()
								.title("Manually connect to wireless network")
								.masthead(
										isMastheadVisible() ? "Manually connect to wireless network"
												: null)
								.message("How do you want to add a network?"))
						.showCommandLinks(links.get(1), links);

				System.out.println("response: " + response);
			}
		});

		final Button Hyperlink12a = new Button("Font Chooser");
		Hyperlink12a.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Font response = configureSampleDialog(Dialogs.create())
						.showFontSelector(null);

				System.out.println("font: " + response);
			}
		});

		final Button Hyperlink12b = new Button("Progress");
		Hyperlink12b.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Task<Object> worker = new Task<Object>() {
					@Override
					protected Object call() throws Exception {
						for (int i = 0; i < 100; i++) {
							updateProgress(i, 99);
							updateMessage("progress: " + i);
							System.out.println("progress: " + i);
							Thread.sleep(100);
						}
						return null;
					}
				};

				configureSampleDialog(
						Dialogs.create()
								.title("Progress")
								.masthead(
										isMastheadVisible() ? "Please wait whilst the install completes..."
												: null)
								.message("Now Loading...")).showWorkerProgress(
						worker);

				Thread th = new Thread(worker);
				th.setDaemon(true);
				th.start();
			}
		});
		
		final Button Hyperlink12c = new Button("Login");
		Hyperlink12c.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				Optional<UserInfo> response = configureSampleDialog(Dialogs.create())
						.showLogin(new UserInfo("user", "password"), info -> {
							if ( !"controlsfx".equalsIgnoreCase(info.getUserName())) {
								throw new RuntimeException("Service is not available... try again later!"); 
							};
							return null;
							}
						 );

				System.out.println("User info: " + response);
			}
		});

		grid.add(new HBox(10, Hyperlink12, Hyperlink12a, Hyperlink12b, Hyperlink12c), 1, row);
		row++;

		// *******************************************************************
		// Custom dialogs
		// *******************************************************************

		grid.add(createLabel("Custom Dialog: "), 0, row);
		final Button Hyperlink14 = new Button("Show");
		Hyperlink14.setOnAction(new EventHandler<ActionEvent>() {

			final TextField txUserName = new TextField();
			final PasswordField txPassword = new PasswordField();
			final Action actionLogin = new AbstractDialogAction("Login",
					ActionTrait.CLOSING, ActionTrait.DEFAULT) {

				{
					ButtonBar.setType(this, ButtonType.OK_DONE);
				}

				@Override
				public void execute(ActionEvent ae) {
					Dialog dlg = (Dialog) ae.getSource();
					// real login code here
					dlg.setResult(this);
				}

				public String toString() {
					return "LOGIN";
				};
			};

			private void validate() {
				actionLogin.disabledProperty().set(
						txUserName.getText().trim().isEmpty()
								|| txPassword.getText().trim().isEmpty());
			}

			@Override
			public void handle(ActionEvent arg0) {
				Dialog dlg = new Dialog(includeOwner() ? stage : null,
						"Login Dialog", cbUseLightweightDialog.isSelected(),
						cbUseNativeTitleBar.isSelected());
				if (cbShowMasthead.isSelected()) {
					dlg.setMasthead("Login to ControlsFX");
				}

				ChangeListener<String> changeListener = new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						validate();
					}
				};

				txUserName.textProperty().addListener(changeListener);
				txPassword.textProperty().addListener(changeListener);

				final GridPane content = new GridPane();
				content.setHgap(10);
				content.setVgap(10);

				content.add(new Label("User name"), 0, 0);
				content.add(txUserName, 1, 0);
				GridPane.setHgrow(txUserName, Priority.ALWAYS);
				content.add(new Label("Password"), 0, 1);
				content.add(txPassword, 1, 1);
				GridPane.setHgrow(txPassword, Priority.ALWAYS);

				dlg.setResizable(false);
				dlg.setIconifiable(false);
				dlg.setGraphic(new ImageView(HelloDialog.class.getResource(
						"login.png").toString()));
				dlg.setContent(content);
				dlg.getActions().addAll(actionLogin, Dialog.Actions.CANCEL);
				validate();

				Platform.runLater( () -> txUserName.requestFocus() );

				Action response = dlg.show();
				System.out.println("response: " + response);
			}
		});

		grid.add(new HBox(10, Hyperlink14), 1, row);

		return grid;
	}

	private Dialogs configureSampleDialog(Dialogs dialog) {
		if (cbSetOwner.isSelected()) {
			dialog.owner(includeOwner() ? stage : null);
		}

		if (cbUseLightweightDialog.isSelected()) {
			dialog.lightweight();
		}

		if (cbUseNativeTitleBar.isSelected()) {
			dialog.nativeTitleBar();
		}

		return dialog;
	}

	@Override
	public Node getControlPanel() {
		GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setPadding(new Insets(30, 30, 0, 30));

		int row = 0;

		grid.add(createLabel("Locale: ", "property"), 0, row);
		final ComboBox<Locale> localeCombobox = new ComboBox<Locale>();
		localeCombobox.getItems()
				.addAll(Locale.ENGLISH, new Locale("ru", "RU"));
		localeCombobox.valueProperty().addListener(
				new ChangeListener<Locale>() {
					@Override
					public void changed(ObservableValue<? extends Locale> ov,
							Locale oldValue, Locale newValue) {
						Localization.setLocale(newValue);
					}
				});
		localeCombobox.setValue(localeCombobox.getItems().get(0));
		grid.add(localeCombobox, 1, row);
		row++;

		// operating system button order
		grid.add(createLabel("Operating system button order: ", "property"), 0,
				row);
		final ToggleButton windowsBtn = createToggle(WINDOWS);
		final ToggleButton macBtn = createToggle(MAC_OS);
		final ToggleButton linuxBtn = createToggle(LINUX);
		windowsBtn.selectedProperty().set(true);
		SegmentedButton operatingSystem = new SegmentedButton(
				FXCollections.observableArrayList(windowsBtn, macBtn, linuxBtn));
		grid.add(operatingSystem, 1, row);
		row++;

		// use lightweight dialogs
		grid.add(createLabel("Lightweight dialogs: ", "property"), 0, row);
		grid.add(cbUseLightweightDialog, 1, row);
		row++;

		// use native titlebar
		grid.add(createLabel("Native titlebar: ", "property"), 0, row);
		grid.add(cbUseNativeTitleBar, 1, row);
		row++;

		// show masthead
		grid.add(createLabel("Show masthead: ", "property"), 0, row);
		grid.add(cbShowMasthead, 1, row);
		row++;

		// set owner
		grid.add(createLabel("Set dialog owner: ", "property"), 0, row);
		grid.add(cbSetOwner, 1, row);
		row++;

		return grid;
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	private Node createLabel(String text, String... styleclass) {
		Label label = new Label(text);

		if (styleclass == null || styleclass.length == 0) {
			label.setFont(Font.font(13));
		} else {
			label.getStyleClass().addAll(styleclass);
		}
		return label;
	}

	private boolean isMastheadVisible() {
		return cbShowMasthead.isSelected();
	}

}
