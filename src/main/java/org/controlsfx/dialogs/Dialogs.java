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
package org.controlsfx.dialogs;

import static org.controlsfx.dialogs.Dialog.Actions.CANCEL;
import static org.controlsfx.dialogs.Dialog.Actions.NO;
import static org.controlsfx.dialogs.Dialog.Actions.OK;
import static org.controlsfx.dialogs.Dialog.Actions.YES;
import static org.controlsfx.dialogs.DialogResources.getString;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;


/**
 * Simplifies building commonly used dialogs
 * Uses fluent API for flexibility
 */
public final class Dialogs {

    /**
     * USE_DEFAULT can be passed in to {@link #title(String)} and {@link #masthead(String)} methods
     * to specify that the default text for the dialog should be used, where the default text is
     * specific to the type of dialog being shown.
     */
    public static final String USE_DEFAULT = "$$$";

    private Window owner;
    private String title;
    private String message;
    private String masthead;

    /**
     * Creates the initial dialog
     * @return dialog instance
     */
    public static Dialogs create() {
        return new Dialogs();
    }

    private Dialogs() {}
    
    /**
     * Assigns the owner of the dialog. If an owner is specified, the dialog will
     * block input to the owner and all parent owners. If no owner is specified,
     * or if owner is null, the dialog will block input to the entire application.
     * 
     * @param owner The dialog owner.
     * @return 
     */
    public Dialogs owner(final Window owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Assigns dialog's title
     * @param title dialog title
     * @return dialog instance
     */
    public Dialogs title(final String title) {
        this.title = title;
        return this;
    }

    
    /**
     * Assigns dialog's instructions
     * @param message dialog message
     * @return dialog instance
     */
    public Dialogs message(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Assigns dialog's masthead
     * @param masthead dialog masthead
     * @return dialog instance
     */
    public Dialogs masthead(final String masthead) {
        this.masthead = masthead;
        return this;
    }

    /**
     * Shows information dialog
     */
    public void showInformation() {
        showSimpleContentDialog(Type.INFORMATION);
    }

    /**
     * Shows confirmation dialog
     * @return action used to close dialog
     */
    public Action showConfirm() {
        return showSimpleContentDialog(Type.CONFIRMATION);
    }

    /**
     * Shows warning dialog
     * @return action used to close dialog
     */
    public Action showWarning() {
        return showSimpleContentDialog(Type.WARNING);
    }

    /**
     * Show error dialog 
     * @return action used to close dialog
     */
    public Action showError() {
        return showSimpleContentDialog(Type.ERROR);
    }

    /**
     * Shows exception dialog with expandable stack trace.
     * @param exception exception to present
     * @return action used to close dialog
     */
    public Action showException(Throwable exception) {
        Dialog dlg = buildDialog(Type.ERROR);
        dlg.setContent(exception.getMessage());
        dlg.setExpandableContent(buildExceptionDetails(exception));
        return dlg.show();
    }
    
    /**
     * Shows exception dialog with a button to open the exception text in a 
     * new window.
     * @param exception exception to present
     * @return action used to close dialog
     */
    public Action showExceptionInNewWindow(final Throwable exception) {
        Dialog dlg = buildDialog(Type.ERROR);
        dlg.setContent(exception.getMessage());
        
        Action openExceptionAction = new AbstractAction("Open Exception") {
            @Override public void execute(ActionEvent ae) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                String moreDetails = sw.toString();
                new ExceptionDialog(owner, moreDetails).show();
            }
        };
        dlg.getActions().add(openExceptionAction);
        
        return dlg.show();
    }

    /**
     * Shows dialog with one text field
     * @param defaultValue text field default value 
     * @return text from input field if OK action is used otherwise null 
     */
    public String showTextInput(String defaultValue) {
        Dialog dlg = buildDialog(Type.INPUT);
        final TextField textField = new TextField(defaultValue);
        dlg.setContent(buildInputContent(textField));
        return dlg.show() == OK ? textField.getText() : null;
    }

    /**
     * Shows dialog with one text field 
     * @return text from input field or null if dialog is cancelled 
     */
    public String showTextInput() {
        return showTextInput("");
    }

    @SuppressWarnings("unchecked") public <T> T showChoices(T defaultValue, Collection<T> choices) {

        Dialog dlg = buildDialog(Type.INPUT);
        // Workaround: need final variable without custom change listener
        final Object[] response = new Object[1];
        ChangeListener<T> changeListener = new ChangeListener<T>() {
            @Override public void changed(ObservableValue<? extends T> ov, T t, T t1) {
                response[0] = t1;
            }
        };
        if (choices.size() > 10) {
            // use ComboBox
            ComboBox<T> comboBox = new ComboBox<T>();
            comboBox.getItems().addAll(choices);
            comboBox.getSelectionModel().select(defaultValue);
            comboBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
            dlg.setContent(buildInputContent(comboBox));
        } else {
            // use ChoiceBox
            ChoiceBox<T> choiceBox = new ChoiceBox<T>();
            choiceBox.getItems().addAll(choices);
            choiceBox.getSelectionModel().select(defaultValue);
            choiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
            dlg.setContent(buildInputContent(choiceBox));
        }

        return dlg.show() == OK ? (T) response[0] : null;

    }

    /**
     * Show a dialog with one combobox filled with provided choices 
     * @param choices dialog choices
     * @return selected choice or null if dialog is cancelled
     */
    public <T> T showChoices(Collection<T> choices) {
        return showChoices(null, choices);
    }

    /**
     * Show a dialog with one combobox filled with provided choices 
     * @param choices dialog choices
     * @return selected choice or null if dialog is cancelled
     */
    public <T> T showChoices(@SuppressWarnings("unchecked") T... choices) {
        return showChoices(Arrays.asList(choices));
    }

    /**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent
     * a set of available 'radio' buttons
     * @param defaultCommandLink command is set to be default. Null means no default
     * @param links list of command links presented in specified sequence
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */
    public Action showCommandLinks( CommandLink defaultCommandLink, List<CommandLink> links ) {
        final Dialog dlg = buildDialog(Type.INFORMATION );
        dlg.setContent(message);
        
        Node messageNode = dlg.getContent();
        messageNode.getStyleClass().add("command-link-message");
        
        final VBox content = new VBox(10);
        Node message = dlg.getContent();
        if ( message != null ) {
            content.getChildren().add(message);
        }
        
        for (final CommandLink commandLink : links) {
            
            if ( commandLink == null ) continue; 
            
            final Button button = buildCommandLinkButton(commandLink);
            
            DoubleBinding dialogWidthBinding = new DoubleBinding() {
                {
                    bind(dlg.widthProperty());
                }
                
                @Override protected double computeValue() {
                    // FIXME 100 is just plucked out of the air to get good
                    // word wrapping in most circumstances
                    double width = dlg.widthProperty().get();
                    return width - 100;
                }
            };
            button.prefWidthProperty().bind(dialogWidthBinding);
            button.maxWidthProperty().bind(dialogWidthBinding);
            
            button.setDefaultButton(commandLink == defaultCommandLink);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent ae) {
                   commandLink.execute( new ActionEvent(dlg, ae.getTarget()));
                }
            });
                    
            VBox.setVgrow(button, Priority.SOMETIMES);
            content.getChildren().add( button );
        }
        dlg.setContent(content);
        dlg.getActions().clear();
        
        return dlg.show();
    }
    
    /**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent
     * a set of available 'radio' buttons
     * @param links list of command links presented in specified sequence
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */    
    public Action showCommandLinks( List<CommandLink> links ) {
        return showCommandLinks( null, links);
    }
    
    /**
     * Show a dialog filled with provided command links. Command links are used instead of button bar and represent
     * a set of available 'radio' buttons
     * @param defaultCommandLink command is set to be default. Null means no default
     * @param links command links presented in specified sequence
     * @return action used to close dialog (it is either one of command links or CANCEL) 
     */
    public Action showCommandLinks( CommandLink defaultCommandLink, CommandLink... links ) {
        return showCommandLinks( defaultCommandLink, Arrays.asList(links));
    }
    
    /**
     * Command Link class.
     * Represents one command link in command links dialog. 
     */
    public static class CommandLink extends AbstractAction {
        
        public CommandLink( Node graphic, String text, String longText ) {
            super(text);
            setLongText(longText);
            setGraphic(graphic);
        }
        
        public CommandLink( String message, String comment ) {
            this(null, message, comment);
        }

        @Override public final void execute(ActionEvent ae) {
            Dialog dlg = (Dialog)ae.getSource();
            dlg.result = this;
            dlg.hide();
        }

        @Override public String toString() {
            return "CommandLink [text=" + getText() + ", longText=" + getLongText() + "]";
        }
        
    }    
    
    /***************************************************************************
     * Private API
     **************************************************************************/

    private static enum Type {
        ERROR("error.image", "Error", "Error", OK),
        INFORMATION("info.image", "Message", "Message", OK),
        WARNING("warning.image", "Warning", "Warning", OK),
        CONFIRMATION("confirm.image", "Select an option", "Select an option", YES, NO, CANCEL),
        INPUT("confirm.image", "Select an option", "Select an option", OK, CANCEL);

        private final String defaultTitle;
        private final String defaultMasthead;
        private final Collection<Action> actions;
        private final String imageResource;
        private Image image;

        Type(String imageResource, String defaultTitle, String defaultMasthead, Action... actions) {
            this.actions = Arrays.asList(actions);
            this.imageResource = imageResource;
            this.defaultTitle = defaultTitle;
            this.defaultMasthead = defaultMasthead;
        }

        public Image getImage() {
            if (image == null) {
                image = DialogResources.getImage(imageResource);
            }
            return image;
        }

        public String getDefaultMasthead() {
            return defaultMasthead;
        }

        public String getDefaultTitle() {
            return defaultTitle;
        }

        public Collection<Action> getActions() {
            return actions;
        }
    }

    private Dialog buildDialog(final Type dlgType) {
        String actualTitle = title == null ? null : (USE_DEFAULT.equals(title) ? dlgType.getDefaultTitle() : title);
        String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType.getDefaultMasthead() : masthead);
        Dialog dlg = new Dialog(owner, actualTitle);
        dlg.setResizable(false);
        dlg.setGraphic(new ImageView(dlgType.getImage()));
        dlg.setMasthead(actualMasthead);
        dlg.getActions().addAll(dlgType.getActions());
        return dlg;
    }

    private Action showSimpleContentDialog(final Type dlgType) {
        Dialog dlg = buildDialog(dlgType);
        dlg.setContent(message);
        return dlg.show();
    }

    private Node buildInputContent(Control inputControl) {

        HBox hbox = new HBox(10);
        hbox.setMaxWidth(Double.MAX_VALUE);
        hbox.setAlignment(Pos.BASELINE_LEFT);
        
        if (message != null && !message.isEmpty()) {
            Label label = new Label(message);
            HBox.setHgrow(label, Priority.NEVER);
            hbox.getChildren().add(label);
        }

        if (inputControl != null) {
            inputControl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(inputControl, Priority.ALWAYS);
            hbox.getChildren().add(inputControl);
        }

        return hbox;

    }

    private Node buildExceptionDetails(Throwable exception) {
        Label label = new Label(getString("exception.dialog.label"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        TextArea text = new TextArea(sw.toString());
        text.setEditable(false);
        text.setWrapText(true);
        
        // FIXME without this, the text area will not grow when the expandable
        // dialog is manually resized with the mouse. There is bound to be a 
        // better solution, but I'm not entirely sure what it is right now.
        text.setPrefWidth(3000);
        
        text.setMaxWidth(Double.MAX_VALUE);
        text.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(text, Priority.ALWAYS);
        GridPane.setHgrow(text, Priority.ALWAYS);
        
        GridPane root = new GridPane();
        root.setMaxWidth(Double.MAX_VALUE);
        root.add(label, 0, 0);
        root.add(text, 0, 1);

        return root;
    }
    
    private Button buildCommandLinkButton( CommandLink commandLink ) {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
//        button.setPrefWidth(500);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMaxHeight(Region.USE_PREF_SIZE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        
        Label titleLabel = new Label(commandLink.getText() );
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        titleLabel.maxWidthProperty().bind(button.maxWidthProperty());
        GridPane.setVgrow(titleLabel, Priority.ALWAYS);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.getStyleClass().addAll("line-2");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.maxWidthProperty().bind(button.maxWidthProperty());
        GridPane.setVgrow(messageLabel, Priority.ALWAYS);
        
        Node graphic = commandLink.getGraphic();
        graphic = graphic == null? new ImageView(DialogResources.getImage("command.link.icon")) : graphic;
        Pane graphicContainer = new Pane(graphic);
        graphicContainer.getStyleClass().add("graphic-container");
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,10,0,0));
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("container");
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        
        return button;
    }
}
