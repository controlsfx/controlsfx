/**
 * Copyright (c) 2013, 2015 ControlsFX
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.dialog.CommandLinksDialog;
import org.controlsfx.dialog.CommandLinksDialog.CommandLinksButtonType;
import org.controlsfx.dialog.WizardPane;
import org.controlsfx.dialog.ExceptionDialog;
import org.controlsfx.dialog.FontSelectorDialog;
import org.controlsfx.dialog.LoginDialog;
import org.controlsfx.dialog.ProgressDialog;
import org.controlsfx.dialog.Wizard;
import org.controlsfx.dialog.Wizard.LinearFlow;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class HelloDialogs extends ControlsFXSample {
    
	@Override
	public String getSampleName() {
		return "Dialogs";
	}

	@Override
	public String getJavaDocURL() {
//		return Utils.JAVADOC_BASE + "org/controlsfx/dialog/Dialogs.html";
	    return null;
	}

	@Override
	public String getSampleDescription() {
		return "";
	}

	private final ComboBox<StageStyle> styleCombobox = new ComboBox<>();
    private final ComboBox<Modality> modalityCombobox = new ComboBox<>();
    private final CheckBox cbUseBlocking = new CheckBox();
    private final CheckBox cbCloseDialogAutomatically = new CheckBox();
    private final CheckBox cbShowMasthead = new CheckBox();
    private final CheckBox cbSetOwner = new CheckBox();
    private final CheckBox cbCustomGraphic = new CheckBox();
    
    private Stage stage;

    @Override
    public Node getPanel(Stage stage) {
        this.stage = stage;

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        
        Label javafxDialogs = new Label("JavaFX Dialogs:");
        javafxDialogs.setFont(Font.font(25));
        grid.add(javafxDialogs, 0, row++, 2, 1);

        // *******************************************************************
        // Information Dialog
        // *******************************************************************

        grid.add(createLabel("Information Dialog: "), 0, row);

        final Button Hyperlink2 = new Button("Show");
        Hyperlink2.setOnAction( (ActionEvent e) -> {
        		
                Alert dlg = createAlert(AlertType.INFORMATION);
                dlg.setTitle("Custom title");
                String optionalMasthead = "Wouldn't this be nice?";
                dlg.getDialogPane().setContentText("A collection of pre-built JavaFX dialogs?\nSeems like a great idea to me...");
                configureSampleDialog(dlg, optionalMasthead);
                
                // lets get some output when events happen
                dlg.setOnShowing(evt -> System.out.println(evt));
                dlg.setOnShown(evt -> System.out.println(evt));
                dlg.setOnHiding(evt -> System.out.println(evt));
                dlg.setOnHidden(evt -> System.out.println(evt));
                
//              dlg.setOnCloseRequest(evt -> evt.consume());
                
                showDialog(dlg);
        });

        final Button Hyperlink2a = new Button("2 x Buttons (no cancel)");
        Hyperlink2a.setOnAction( (ActionEvent e) -> {
                Alert dlg = createAlert(AlertType.INFORMATION);
                dlg.setTitle("Custom title");
                String optionalMasthead = "Wouldn't this be nice?";
                dlg.getDialogPane().setContentText("A collection of pre-built JavaFX dialogs?\nSeems like a great idea to me...");
                configureSampleDialog(dlg, optionalMasthead);
                dlg.getButtonTypes().add(ButtonType.NEXT);

//              dlg.setOnCloseRequest(evt -> evt.consume());

                showDialog(dlg);
            
        });

        grid.add(new HBox(10, Hyperlink2, Hyperlink2a), 1, row);

        row++;

        // *******************************************************************
        // Confirmation Dialog
        // *******************************************************************

        grid.add(createLabel("Confirmation Dialog: "), 0, row);

        final CheckBox cbShowCancel = new CheckBox("Show Cancel Button");
        cbShowCancel.setSelected(true);

        final Button Hyperlink3 = new Button("Show");
        Hyperlink3.setOnAction(e -> {
            Alert dlg = createAlert(AlertType.CONFIRMATION);
            dlg.setTitle("You do want dialogs right?");
            String optionalMasthead = "Just Checkin'";
            dlg.getDialogPane().setContentText("I was a bit worried that you might not want them, so I wanted to double check.");

            if (!cbShowCancel.isSelected()) {
                dlg.getDialogPane().getButtonTypes().remove(ButtonType.CANCEL);
            }

            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });
        grid.add(new HBox(10, Hyperlink3, cbShowCancel), 1, row);

        row++;

        // *******************************************************************
        // Warning Dialog
        // *******************************************************************

        grid.add(createLabel("Warning Dialog: "), 0, row);

        final Button Hyperlink6a = new Button("Show");
        Hyperlink6a.setOnAction(e -> {
            Alert dlg = createAlert(AlertType.WARNING);
            dlg.setTitle("I'm warning you!");
            String optionalMasthead = "This is a warning";
            dlg.getDialogPane().setContentText("I'm glad I didn't need to use this...");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });
        grid.add(new HBox(10, Hyperlink6a), 1, row);

        row++;

        // *******************************************************************
        // Error Dialog
        // *******************************************************************

        grid.add(createLabel("Error Dialog: "), 0, row);

        final Button Hyperlink7a = new Button("Show");
        Hyperlink7a.setOnAction(e -> {
            Alert dlg = createAlert(AlertType.ERROR);
            dlg.setTitle("It looks like you're making a bad decision");
            String optionalMasthead = "Exception Encountered";
            dlg.getDialogPane().setContentText("Better change your mind - this is really your last chance! (Even longer text that should probably wrap)");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });
        grid.add(new HBox(10, Hyperlink7a), 1, row);

        row++;


        // *******************************************************************
        // Input Dialog (with header)
        // *******************************************************************

        grid.add(createLabel("Input Dialog: "), 0, row);

        final Button Hyperlink8 = new Button("TextField");
        Hyperlink8.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog("");
            dlg.setTitle("Name Check");
            String optionalMasthead = "Please type in your name";
            dlg.getDialogPane().setContentText("What is your name?");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });

        final Button Hyperlink9 = new Button("Initial Value Set");
        Hyperlink9.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog("Jonathan");
            dlg.setTitle("Name Guess");
            String optionalMasthead = "Name Guess";
            dlg.getDialogPane().setContentText("Pick a name?");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });

        final Button Hyperlink10 = new Button("Set Choices (< 10)");
        Hyperlink10.setOnAction(e -> {
            ChoiceDialog<String> dlg = new ChoiceDialog<>("Jonathan",
                                                          "Matthew", "Jonathan", "Ian", "Sue", "Hannah");
            dlg.setTitle("Name Guess");
            String optionalMasthead = "Name Guess";
            dlg.getDialogPane().setContentText("Pick a name?");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });

        final Button Hyperlink11 = new Button("Set Choices (>= 10)");
        Hyperlink11.setOnAction(e -> {
            ChoiceDialog<String> dlg = new ChoiceDialog<>("Jonathan",
                                                          "Matthew", "Jonathan", "Ian", "Sue",
                                                          "Hannah", "Julia", "Denise", "Stephan",
                                                          "Sarah", "Ron", "Ingrid");
            dlg.setTitle("Name Guess");
            String optionalMasthead = "Name Guess";
            dlg.getDialogPane().setContentText("Pick a name?");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });

        grid.add(new HBox(10, Hyperlink8, Hyperlink9, Hyperlink10, Hyperlink11), 1, row);
        row++;
        
        
        
        
        
        
        // ---------  ControlsFX-specific Dialogs
        
        Label controlsfxDialogs = new Label("ControlsFX Dialogs:");
        controlsfxDialogs.setFont(Font.font(25));
        grid.add(controlsfxDialogs, 0, row++, 2, 1);
        

        // *******************************************************************
        // Command links
        // *******************************************************************

        grid.add(createLabel("Pre-built dialogs: "), 0, row);
        final Button Hyperlink12 = new Button("Command Links");
        Hyperlink12.setOnAction(e -> {
            List<CommandLinksButtonType> links = Arrays
                    .asList(new CommandLinksButtonType(
                            "Add a network that is in the range of this computer",
                            "This shows you a list of networks that are currently available and lets you connect to one.", false),
                            new CommandLinksButtonType(
                            "Manually create a network profile",
                            "This creates a new network profile or locates an existing one and saves it on your computer",
                             true /*default*/),
                             new CommandLinksButtonType("Create an ad hoc network",
                            "This creates a temporary network for sharing files or and Internet connection", false));

            CommandLinksDialog dlg = new CommandLinksDialog(links);
            dlg.setTitle("Manually connect to wireless network");
            String optionalMasthead = "Manually connect to wireless network";
            dlg.getDialogPane().setContentText("How do you want to add a network?");
            configureSampleDialog(dlg, optionalMasthead);
            showDialog(dlg);
        });

        final Button Hyperlink12a = new Button("Font Selector");
        Hyperlink12a.setOnAction(e -> {
            FontSelectorDialog dlg = new FontSelectorDialog(null);
            configureSampleDialog(dlg, "Please select a font!");
            showDialog(dlg);
        });
        
        final Button Hyperlink12b = new Button("Progress");
        Hyperlink12b.setOnAction((ActionEvent e) -> {
                Task<Object> worker = new Task<Object>() {
                    @Override
                    protected Object call() throws Exception {
                        for (int i = 0; i <= 100; i++) {
                            updateProgress(i, 99);
                            updateMessage("progress: " + i);
                            System.out.println("progress: " + i);
                            Thread.sleep(100);
                        }
                        return null;
                    }
                };

                ProgressDialog dlg = new ProgressDialog(worker);
                configureSampleDialog(dlg, "");

                Thread th = new Thread(worker);
                th.setDaemon(true);
                th.start();
        });
        
        final Button Hyperlink12c = new Button("Login");
        Hyperlink12c.setOnAction((ActionEvent e) -> {
                LoginDialog dlg = new LoginDialog(null, null);
                configureSampleDialog(dlg, "");
                showDialog(dlg);
        });
        
        final Button Hyperlink12d = new Button("Exception");
        Hyperlink12d.setOnAction((ActionEvent e) -> {
                ExceptionDialog dlg = new ExceptionDialog(new Exception("ControlsFX is _too_ awesome!"));
                configureSampleDialog(dlg, "");
                showDialog(dlg);
        });

        grid.add(new HBox(10, Hyperlink12, Hyperlink12a, Hyperlink12b, Hyperlink12c, Hyperlink12d), 1, row);
        row++;

        
        // *******************************************************************
        // wizards
        // *******************************************************************

        grid.add(createLabel("Wizard: "), 0, row);
        final Button Hyperlink15a = new Button("Linear Wizard");
        Hyperlink15a.setOnAction(e -> showLinearWizard());
        
        final Button Hyperlink15b = new Button("Branching Wizard");
        Hyperlink15b.setOnAction(e -> showBranchingWizard());
        
        final Button Hyperlink15c = new Button("Validated Linear Wizard");
        Hyperlink15c.setOnAction(e -> showValidatedLinearWizard());
        
        grid.add(new HBox(10, Hyperlink15a, Hyperlink15b, Hyperlink15c), 1, row++);
        
        return grid;
    }

    private Alert createAlert(AlertType type) {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        Alert dlg = new Alert(type, "");
        dlg.initModality(modalityCombobox.getValue());
        dlg.initOwner(owner);
        return dlg;
    }
    
    private void configureSampleDialog(Dialog<?> dlg, String header) {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        if (header != null && cbShowMasthead.isSelected()) {
            dlg.getDialogPane().setHeaderText(header);
        }
        
        if (cbCustomGraphic.isSelected()) {
            dlg.getDialogPane().setGraphic(new ImageView(new Image(getClass().getResource("../controlsfx-logo.png").toExternalForm())));
        }
        
        dlg.initStyle(styleCombobox.getValue());
        dlg.initOwner(owner);
    }
    
    private void showDialog(Dialog<?> dlg) {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        if (cbCloseDialogAutomatically.isSelected()) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Attempting to close dialog now...");
                Platform.runLater(() -> dlg.close());
            }).start();
        }
        dlg.initOwner(owner);

        if (cbUseBlocking.isSelected()) {
            dlg.showAndWait().ifPresent(result -> System.out.println("Result is " + result));
        } else {
            dlg.show();
            dlg.resultProperty().addListener(o -> System.out.println("Result is: " + dlg.getResult()));
            System.out.println("This println is _after_ the show method - we're non-blocking!");
        }
    }

    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // stage style
        grid.add(createLabel("Style: ", "property"), 0, row);
        styleCombobox.getItems().setAll(StageStyle.values());
        styleCombobox.setValue(styleCombobox.getItems().get(0));
        grid.add(styleCombobox, 1, row);
        row++;

        // modality
        grid.add(createLabel("Modality: ", "property"), 0, row);
        modalityCombobox.getItems().setAll(Modality.values());
        modalityCombobox.setValue(modalityCombobox.getItems().get(Modality.values().length-1));
        grid.add(modalityCombobox, 1, row);
        row++;
        
        // use blocking
        cbUseBlocking.setSelected(true);
        grid.add(createLabel("Use blocking: ", "property"), 0, row);
        grid.add(cbUseBlocking, 1, row);
        row++;

        // close dialog automatically
        grid.add(createLabel("Close dialog after 2000ms: ", "property"), 0, row);
        grid.add(cbCloseDialogAutomatically, 1, row);
        row++;

        // show header
        grid.add(createLabel("Show custom header text: ", "property"), 0, row);
        grid.add(cbShowMasthead, 1, row);
        row++;

        // set owner
        grid.add(createLabel("Set dialog owner: ", "property"), 0, row);
        grid.add(cbSetOwner, 1, row);
        row++;
        
        // custom graphic
        grid.add(createLabel("Use custom graphic: ", "property"), 0, row);
        grid.add(cbCustomGraphic, 1, row);
        row++;

        return grid;
    }
    
//    private CommandLinksButtonType buildCommandLink( String text, String comment, boolean isDefault ) {
//            return new CommandLinksButtonType(text, comment, isDefault);
//    }


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

    private void showLinearWizard() {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        // define pages to show
        Wizard wizard = new Wizard(owner);
        wizard.setTitle("Linear Wizard");
        
        // --- page 1
        int row = 0;

        GridPane page1Grid = new GridPane();
        page1Grid.setVgap(10);
        page1Grid.setHgap(10);

        page1Grid.add(new Label("First Name:"), 0, row);
        TextField txFirstName = createTextField("firstName");
//        wizard.getValidationSupport().registerValidator(txFirstName, Validator.createEmptyValidator("First Name is mandatory"));  
        page1Grid.add(txFirstName, 1, row++);

        page1Grid.add(new Label("Last Name:"), 0, row);
        TextField txLastName = createTextField("lastName");
//        wizard.getValidationSupport().registerValidator(txLastName, Validator.createEmptyValidator("Last Name is mandatory"));
        page1Grid.add(txLastName, 1, row);

        WizardPane page1 = new WizardPane();
        page1.setHeaderText("Please Enter Your Details");
        page1.setContent(page1Grid);


        // --- page 2
        final WizardPane page2 = new WizardPane() {
            @Override public void onEnteringPage(Wizard wizard) {
                String firstName = (String) wizard.getSettings().get("firstName");
                String lastName = (String) wizard.getSettings().get("lastName");

                setContentText("Welcome, " + firstName + " " + lastName + "! Let's add some newlines!\n\n\n\n\n\n\nHello World!");
            }
        };
        page2.setHeaderText("Thanks For Your Details!");


        // --- page 3
        WizardPane page3 = new WizardPane();
        page3.setHeaderText("Goodbye!");
        page3.setContentText("Page 3, with extra 'help' button!");
        
        ButtonType helpDialogButton = new ButtonType("Help", ButtonData.HELP_2);
        page3.getButtonTypes().add(helpDialogButton);
        Button helpButton = (Button) page3.lookupButton(helpDialogButton);
        helpButton.addEventFilter(ActionEvent.ACTION, actionEvent -> {
            actionEvent.consume(); // stop hello.dialog from closing
            System.out.println("Help clicked!");
        });
                
                

        // create wizard
        wizard.setFlow(new LinearFlow(page1, page2, page3));
        
        System.out.println("page1: " + page1);
        System.out.println("page2: " + page2);
        System.out.println("page3: " + page3);

        // show wizard and wait for response
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });
    }
    
    private void showBranchingWizard() {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        // define pages to show.
        // Because page1 references page2, we need to declare page2 first.
        final WizardPane page2 = new WizardPane();
        page2.setContentText("Page 2");

        final CheckBox checkBox = new CheckBox("Skip the second page");
        checkBox.setId("skip-page-2");
        VBox vbox = new VBox(10, new Label("Page 1"), checkBox);
        final WizardPane page1 = new WizardPane() {
            // when we exit page 1, we will check the state of the 'skip page 2'
            // checkbox, and if it is true, we will remove page 2 from the pages list
            @Override public void onExitingPage(Wizard wizard) {
//                List<WizardPage> pages = wizard.getPages();
//                if (checkBox.isSelected()) {
//                    pages.remove(page2);
//                } else {
//                    if (! pages.contains(page2)) {
//                        pages.add(1, page2);
//                    }
//                }
            }
        };
        page1.setContent(vbox);

        final WizardPane page3 = new WizardPane();
        page3.setContentText("Page 3");

        // create wizard
        Wizard wizard = new Wizard(owner);
        wizard.setTitle("Branching Wizard");
        Wizard.Flow branchingFlow = new Wizard.Flow() {

            @Override
            public Optional<WizardPane> advance(WizardPane currentPage) {
                return Optional.of(getNext(currentPage));
            }

            @Override
            public boolean canAdvance(WizardPane currentPage) {
                return currentPage != page3;
            }
            
            private WizardPane getNext(WizardPane currentPage) {
                if ( currentPage == null ) {
                    return page1;
                } else if ( currentPage == page1) {
                    return checkBox.isSelected()? page3: page2;
                } else {
                    return page3;
                }
            }
            
        };
        
        //wizard.setFlow( new LinearWizardFlow( page1, page2, page3));
        wizard.setFlow( branchingFlow);

        // show wizard
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });
    }
    
    private void showValidatedLinearWizard() {
        Window owner = cbSetOwner.isSelected() ? stage : null;
        Wizard wizard = new Wizard(owner);
        wizard.setTitle("Validated Linear Wizard");
        
        // Page 1
        WizardPane page1 = new WizardPane() {
            ValidationSupport vs = new ValidationSupport();
            {
                vs.initInitialDecoration();
                
                int row = 0;

                GridPane page1Grid = new GridPane();
                page1Grid.setVgap(10);
                page1Grid.setHgap(10);

                page1Grid.add(new Label("Username:"), 0, row);
                TextField txUsername = createTextField("username");
                vs.registerValidator(txUsername, Validator.createEmptyValidator("EMPTY!"));
                page1Grid.add(txUsername, 1, row++);

                page1Grid.add(new Label("Full Name:"), 0, row);
                TextField txFullName = createTextField("fullName");
                page1Grid.add(txFullName, 1, row);
                
                setContent(page1Grid);
            }
            
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                wizard.invalidProperty().bind(vs.invalidProperty());
            }
        };
        
        // Page 2

        WizardPane page2 = new WizardPane() {
            ValidationSupport vs = new ValidationSupport();
            {
                vs.initInitialDecoration();
                
                int row = 0;

                GridPane page2Grid = new GridPane();
                page2Grid.setVgap(10);
                page2Grid.setHgap(10);

                page2Grid.add(new Label("ControlsFX is:"), 0, row);
                ComboBox<String> cbControlsFX = createComboBox("controlsfx");
                cbControlsFX.setItems(FXCollections.observableArrayList("Cool", "Great"));
                vs.registerValidator(cbControlsFX, Validator.createEmptyValidator("EMPTY!"));
                page2Grid.add(cbControlsFX, 1, row++);

                page2Grid.add(new Label("Where have you heard of it?:"), 0, row);
                TextField txWhere = createTextField("where");
                vs.registerValidator(txWhere, Validator.createEmptyValidator("EMPTY!"));
                page2Grid.add(txWhere, 1, row++);

                page2Grid.add(new Label("Free text:"), 0, row);
                TextField txFreeText = createTextField("freetext");
                page2Grid.add(txFreeText, 1, row);
                
                setContent(page2Grid);
            }
            
            @Override
            public void onEnteringPage(Wizard wizard) {
                wizard.invalidProperty().unbind();
                wizard.invalidProperty().bind(vs.invalidProperty());
            }
        };

        // create wizard
        wizard.setFlow(new LinearFlow(page1, page2));

        // show wizard and wait for response
        wizard.showAndWait().ifPresent(result -> {
            if (result == ButtonType.FINISH) {
                System.out.println("Wizard finished, settings: " + wizard.getSettings());
            }
        });
    }
    
    private TextField createTextField(String id) {
        TextField textField = new TextField();
        textField.setId(id);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        return textField;
    }
    
    private ComboBox<String> createComboBox(String id) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setId(id);
        GridPane.setHgrow(comboBox, Priority.ALWAYS);
        return comboBox;
    }

}
