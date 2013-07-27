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
package org.controlsfx.dialog;

import static org.controlsfx.dialog.Dialog.Actions.CANCEL;
import static org.controlsfx.dialog.Dialog.Actions.NO;
import static org.controlsfx.dialog.Dialog.Actions.OK;
import static org.controlsfx.dialog.Dialog.Actions.YES;
import static org.controlsfx.dialog.DialogResources.getString;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Callback;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog.Actions;


/**
 * A simple (yet flexible) API for showing the most common forms of (modal) UI 
 * dialogs. This class contains a fluent API to make building and customizing
 * the pre-built dialogs really easy, but for those developers who want complete
 * control, you may be interested in instead using the {@link Dialog} class
 * (which is what all of these pre-built dialogs use as well).
 * 
 * <p>A dialog consists of a number of sections, and the pre-built dialogs in 
 * this class modify these sections as required. Refer to the {@link Dialog} 
 * class documentation for more detail, but a brief overview is provided in 
 * the following section.
 * 
 * <h3>Anatomy of a Dialog</h3>
 * 
 * <p>A dialog consists of the following sections:
 * 
 * <ul>
 *   <li>Title,
 *   <li>System buttons (min, max, close),
 *   <li>Masthead, 
 *   <li>Content, 
 *   <li>Expandable content,
 *   <li>Button bar
 * </ul>
 * 
 * <p>This is more easily demonstrated in the diagram shown below:
 * 
 * <br>
 * <center><img src="dialog-overview.png"></center>
 * 
 * <br>
 * The system buttons are hidden, only "close" button is visible by default. 
 * "Maximize" button is only available when dialog becomes resizable.
 * This happens for example with the Exception dialog when details are expanded. 
 * 
 * <h3>Screenshots</h3>
 * <p>To better explain the dialogs, here is a table showing the default look
 * of all available pre-built dialogs when run on Windows (the button placement
 * in dialogs uses the {@link ButtonBar} control, so the buttons vary in order
 * based on the operating system in which the dialog is shown):
 * 
 * <br>
 * <table style="border: 1px solid gray;">
 *   <tr>
 *     <th></th>
 *     <th><center><h3>Without Masthead</h3></center></th>
 *     <th><center><h3>With Masthead</h3></center></th>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Information</strong></td>
 *     <td><center><img src="dialog-information-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-information-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Confirmation</strong></td>
 *     <td><center><img src="dialog-confirmation-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-confirmation-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Warning</strong></td>
 *     <td><center><img src="dialog-warning-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-warning-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Error</strong></td>
 *     <td><center><img src="dialog-error-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-error-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Exception</strong></td>
 *     <td><center><img src="dialog-exception-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-exception-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Exception (Expanded)</strong></td>
 *     <td><center><img src="dialog-exception-expanded-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-exception-expanded-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Exception (new window)</strong></td>
 *     <td colspan="2"><center><img src="dialog-exception-new-window.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Text Input</strong></td>
 *     <td><center><img src="dialog-text-input-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-text-input-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Choice Input<br>(ChoiceBox/ComboBox)</strong></td>
 *     <td><center><img src="dialog-choicebox-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-choicebox-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Command Link</strong></td>
 *     <td><center><img src="dialog-commandlink-no-masthead.png"></center></td>
 *     <td><center><img src="dialog-commandlink-masthead.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Font Selector</strong></td>
 *     <td colspan="2"><center><img src="dialog-font-selector.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Progress</strong></td>
 *     <td colspan="2"><center><img src="dialog-progress.png"></center></td>
 *   </tr>
 * </table>
 * 
 * 
 * <h3>Heavyweight vs Lightweight Dialogs</h3>
 * 
 * <p>The ControlsFX dialogs API supports a distinction between heavyweight and
 * lightweight dialogs. In short, a heavyweight dialog is rendered in its own
 * JavaFX window, allowing for it to appear outside the bounds of the application.
 * This is the most common style of dialog, and is therefore the default behavior
 * when creating dialogs in ControlsFX. However, in some case, lightweight dialogs
 * make more sense, so the following paragraphs will detail when you might want
 * to use lightweight dialogs.
 * 
 * <p>Lightweight dialogs are rendered within the scenegraph (and can't leave 
 * the window). Other than this limitation, lightweight dialogs otherwise render
 * exactly the same as heavyweight dialogs (except a lightweight dialog normally
 * inserts an opaque overlay into the scene so that the dialog sticks out 
 * visually). Lightweight dialogs are commonly useful in environments where a 
 * windowing system is unavailable (e.g. tablet devices), and also when you only 
 * want to block execution (and access to) a portion of your user interface. For 
 * example, you could create a lightweight dialog with an owner of a single 
 * {@link Tab} in a {@link TabPane}, and this will only block on that one tab - 
 * all other tabs will continue to be interactive and execute as per usual.
 * 
 * <p>To make a dialog lightweight, you simply call {@link #lightweight()} when
 * constructing the dialog using this Dialogs API. If you are using the 
 * {@link Dialog} class instead, then the decision between heavyweight and 
 * lightweight dialogs must be made in the
 * {@link Dialog#Dialog(Object, String, boolean)} constructor.
 * 
 * <p>Shown below is a screenshot of a lightweight dialog whose owner is the
 * Tab. This means that whilst the first tab is blocked for input until the
 * dialog is dismissed by the user, the rest of the UI (including going to other
 * tabs) remains interactive):
 * 
 * <br>
 * <center><img src="lightweight.png"></center>
 * 
 * <h3>Code Examples</h3>
 * 
 * <p>The code below will setup and show a confirmation dialog:
 * 
 * <pre>{@code
 *  Action response = Dialogs.create()
 *      .owner( isOwnerSelected ? stage : null)
 *      .title("You do want dialogs right?")
 *      .masthead(isMastheadVisible() ? "Just Checkin'" : null)
 *      .message( "I was a bit worried that you might not want them, so I wanted to double check.")
 *      .showConfirm();}</pre>
 *      
 * <p>The most important point to note about the dialogs is that they are modal,
 * which means that they stop the application code from progressing until the
 * dialog is closed. Because of this, it is very easy to retrieve the users input:
 * when the user closes the dialog (e.g. by clicking on one of the buttons), the 
 * dialog will be hidden, and their response will be returned from the 
 * show method that was called to bring the dialog up in the 
 * first place. In other words, following on from the code sample above, you 
 * might do the following:
 * 
 * <pre>
 * {@code 
 * if (response == Dialog.Actions.OK) {
 *     // ... submit user input
 * } else {
 *     // ... user cancelled, reset form to default
 * }}</pre>
 * 
 * <p>The following code is an example of setting up a CommandLink dialog:
 * 
 * <pre>{@code 
 *   List<CommandLink> links = Arrays.asList(
 *        new CommandLink("Add a network that is in the range of this computer", 
 *                        "This shows you a list of networks that are currently available and lets you connect to one."),
 *        new CommandLink("Manually create a network profile", 
 *                        "This creates a new network profile or locates an existing one and saves it on your computer"),
 *        new CommandLink("Create an ad hoc network", 
 *                "This creates a temporary network for sharing files or and Internet connection"));
 *   
 *   Action response = Dialogs.create()
 *           .owner(cbSetOwner.isSelected() ? stage : null)
 *           .title("Manually connect to wireless network")
 *           .masthead(isMastheadVisible() ? "Manually connect to wireless network": null)
 *           .message("How do you want to add a network?")
 *           .showCommandLinks( links.get(1), links );}</pre>
 *
 * @see Dialog
 * @see Action
 * @see Actions
 * @see AbstractAction
 */
public final class Dialogs {

    /**
     * USE_DEFAULT can be passed in to {@link #title(String)} and {@link #masthead(String)} methods
     * to specify that the default text for the dialog should be used, where the default text is
     * specific to the type of dialog being shown.
     */
    public static final String USE_DEFAULT = "$$$";

    private Object owner;
    private String title;
    private String message;
    private String masthead;
    private boolean lightweight;
    private boolean nativeChrome;
    private Set<Action> actions = new HashSet<>();

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
     * @return dialog instance
     */
    public Dialogs owner(final Object owner) {
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
     * Completely replaces standard actions with provided ones.
     * @param actions new dialog actions
     * @return dialog instance
     */
    public Dialogs actions( Collection<? extends Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }
    
    /**
     * Completely replaces standard actions with provided ones.  
     * @param actions new dialog actions
     * @return dialog instance
     */
    public Dialogs actions( Action... actions) {
        return actions( Arrays.asList(actions));
    }
    
    /**
     * Specifies that the dialog should become lightweight, which means it is
     * rendered within the scene graph (and can't leave the window). Lightweight 
     * dialogs are commonly useful in environments where a windowing system is unavailable
     * (e.g. tablet devices), and also when you only want to block execution
     * (and access to) a portion of your user interface. For example, you could
     * create a lightweight dialog with an owner of a single {@link Tab} in a 
     * {@link TabPane}, and this will only block on that one tab - all other 
     * tabs will continue to be interactive and execute as per usual.
     * 
     * @return dialog instance.
     */
    public Dialogs lightweight() {
        this.lightweight = true;
        return this;
    }
    
    /**
     * Specifies that the dialog should use the native 'chrome' of the users 
     * operating system rather than the custom cross-platform rendering used by 
     * default. Refer to the Dialogs class JavaDoc for more information.
     * 
     * @return Dialog instance.
     */
    public Dialogs nativeChrome() {
        this.nativeChrome = true;
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
                new ExceptionDialog((Window)owner, moreDetails, nativeChrome).show();
            }
        };
        ButtonBar.setType(openExceptionAction, ButtonType.HELP_2);
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

    /**
     * Show a dialog with one combobox filled with provided choices. The combobox selection 
     * will be set to a default value if one is provided.
     * @param defaultChoice default combobox selection 
     * @param choices dialog choices
     * @return selected choice or null if dialog is cancelled
     */
    @SuppressWarnings("unchecked") public <T> T showChoices(T defaultChoice, Collection<T> choices) {

        Dialog dlg = buildDialog(Type.INPUT);
        // Workaround: need final variable without custom change listener
        final Object[] response = new Object[1];
        ChangeListener<T> changeListener = new ChangeListener<T>() {
            @Override public void changed(ObservableValue<? extends T> ov, T t, T t1) {
                response[0] = t1;
            }
        };
        
        final double MIN_WIDTH = 150;
        if (choices.size() > 10) {
            // use ComboBox
            ComboBox<T> comboBox = new ComboBox<T>();
            comboBox.setMinWidth(MIN_WIDTH);
            comboBox.getItems().addAll(choices);
            comboBox.getSelectionModel().select(defaultChoice);
            comboBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
            dlg.setContent(buildInputContent(comboBox));
        } else {
            // use ChoiceBox
            ChoiceBox<T> choiceBox = new ChoiceBox<T>();
            choiceBox.setMinWidth(MIN_WIDTH);
            choiceBox.getItems().addAll(choices);
            choiceBox.getSelectionModel().select(defaultChoice);
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
    public Action showCommandLinks(CommandLink defaultCommandLink, List<CommandLink> links) {
        final Dialog dlg = buildDialog(Type.INFORMATION);
        dlg.setContent(message);
        
        Node messageNode = dlg.getContent();
        messageNode.getStyleClass().add("command-link-message");
        
        final int gapSize = 10;
        final List<Button> buttons = new ArrayList<>(links.size());
        
        GridPane content = new GridPane() {
            @Override protected double computePrefWidth(double height) {
                double pw = 0;
                
                for (int i = 0; i < buttons.size(); i++) {
                    Button btn = buttons.get(i);
                    pw = Math.min(pw, btn.prefWidth(-1));
                }
                return pw + gapSize;
            }
            
            @Override protected double computePrefHeight(double width) {
                double ph = masthead == null || masthead.isEmpty() ? 0 : 10;
                
                for (int i = 0; i < buttons.size(); i++) {
                    Button btn = buttons.get(i);
                    ph += btn.prefHeight(width) + gapSize;
                }
                return ph * 1.5;
            }
        };
        content.setHgap(gapSize);
        content.setVgap(gapSize);
        
        int row = 0;
        Node message = dlg.getContent();
        if (message != null) {
            content.add(message, 0, row++);
        }
        
        for (final CommandLink commandLink : links) {
            if (commandLink == null) continue; 
            
            final Button button = buildCommandLinkButton(commandLink);            
            button.setDefaultButton(commandLink == defaultCommandLink);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent ae) {
                   commandLink.execute( new ActionEvent(dlg, ae.getTarget()));
                }
            });
                    
            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);
            content.add(button, 0, row++);
            buttons.add(button);
        }
        
        // last button gets some extra padding (hacky)
        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));
        
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
     * Show font selection dialog, allowing to manipulate font name, style and size 
     * 
     * @param font Default font value 
     * @return selected font or null if the dialog is canceled
     */
    public Font showFontSelector(Font font) {
        FontPanel fontPanel = new FontPanel();
        fontPanel.setFont(font);
        title(Dialogs.USE_DEFAULT);
        Dialog dlg = buildDialog(Type.FONT);
        dlg.setIconifiable(false);
        dlg.setContent(fontPanel);
        return Dialog.Actions.OK == dlg.show() ? fontPanel.getFont(): null;
    }
    
    /**
     * Creates a progress bar dialog which is attached to the given {@link Worker}
     * instance, so that as the worker starts and completes, so too does the
     * dialog show and hide automatically. 
     * 
     * @param worker The worker that the progress dialog is watching.
     */
    public void showWorkerProgress(final Worker<?> worker) {
        Dialog dlg = buildDialog(Type.PROGRESS);
        dlg.setClosable(false);
        
        final WorkerProgressPane content = new WorkerProgressPane(dlg);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setWorker(worker);

        VBox vbox = new VBox(10, new Label(message == null ? "Progress:" : message), content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        
        vbox.setPrefSize(300, 100);
        
        dlg.setContent(vbox);
    }
    
    
    
    /***************************************************************************
     * 
     * Private API
     * 
     **************************************************************************/

    private Dialog buildDialog(final Type dlgType) {
        String actualTitle = title == null ? null : (USE_DEFAULT.equals(title) ? dlgType.getDefaultTitle() : title);
        String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType.getDefaultMasthead() : masthead);
        Dialog dlg = new Dialog(owner, actualTitle, lightweight, nativeChrome);
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        Image image = dlgType.getImage();
        if (image != null) {
            dlg.setGraphic(new ImageView(image));
        }
        dlg.setMasthead(actualMasthead);
        dlg.getActions().addAll(dlgType.getActions());
        return dlg;
    }

    private Action showSimpleContentDialog(final Type dlgType) {
        Dialog dlg = buildDialog(dlgType);
        if ( !actions.isEmpty()) {
            dlg.getActions().clear();
            dlg.getActions().addAll(actions);
        }
        dlg.setContent(message);
        return dlg.show();
    }

    private Node buildInputContent(final Control inputControl) {
    	GridPane grid = new GridPane();
    	grid.setHgap(10);
    	grid.setMaxWidth(Double.MAX_VALUE);
    	
        if (message != null && !message.isEmpty()) {
            Label label = new Label(message);
            GridPane.setHgrow(label, Priority.NEVER);
            grid.add(label, 0, 0);
        }

        if (inputControl != null) {
            inputControl.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(inputControl, Priority.ALWAYS);
            grid.add(inputControl, 1, 0);
        }
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                inputControl.requestFocus();
            }
        });

        return grid;
    }

    private Node buildExceptionDetails(Throwable exception) {
        Label label = new Label(getString("exception.dialog.label"));

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane root = new GridPane();
        root.setMaxWidth(Double.MAX_VALUE);
        root.add(label, 0, 0);
        root.add(textArea, 0, 1);

        return root;
    }
    
    private Button buildCommandLinkButton(CommandLink commandLink) {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
        button.setMaxHeight(Double.MAX_VALUE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        final Label titleLabel = new Label(commandLink.getText() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() {
            {
                bind(titleLabel.prefWidthProperty());
            }
            
            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.getStyleClass().addAll("line-2");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(messageLabel, Priority.SOMETIMES);
        
        Node graphic = commandLink.getGraphic();
        graphic = graphic == null? new ImageView(DialogResources.getImage("command.link.icon")) : graphic;
        Pane graphicContainer = new Pane(graphic);
        graphicContainer.getStyleClass().add("graphic-container");
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,10,0,0));
        
        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container");
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());
        
        return button;
    }
    
    
    
    /***************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/
    
    private static enum Type {
        ERROR("error.image", "Error", "Error", OK),
        INFORMATION("info.image", "Message", "Message", OK),
        WARNING("warning.image", "Warning", "Warning", OK),
        CONFIRMATION("confirm.image", "Select an option", "Select an option", YES, NO, CANCEL),
        INPUT("confirm.image", "Select an option", "Select an option", OK, CANCEL),
        FONT( null, "Select Font", "Select Font", OK, CANCEL),
        PROGRESS("info.image", "Progress", "Progress");

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
            if (image == null && imageResource != null ) {
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
    
    /**
     * Font style as combination of font weight and font posture. 
     * Weight does not have to be there (represented by null)
     * Posture is required, null posture is converted to REGULAR
     */
    private static class FontStyle implements Comparable<FontStyle> {
        
        private FontPosture posture; 
        private FontWeight weight;
        
        public FontStyle( FontWeight weight, FontPosture posture ) {
            this.posture = posture == null? FontPosture.REGULAR: posture;
            this.weight = weight;
        }
        
        public FontStyle() {
            this( null, null);
        }
        
        public FontStyle(String styles) {
            this();
            String[] fontStyles = (styles == null? "": styles.trim().toUpperCase()).split(" ");
            for ( String style: fontStyles) {
                FontWeight w = FontWeight.findByName(style);
                if ( w != null ) {
                    weight = w;
                } else {
                    FontPosture p = FontPosture.findByName(style);
                    if ( p != null ) posture = p;
                }
            }
        }
        
        public FontStyle(Font font) {
            this( font.getStyle());
        }
        
        public FontPosture getPosture() {
            return posture;
        }
        
        public FontWeight getWeight() {
            return weight;
        }
        
        
        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((posture == null) ? 0 : posture.hashCode());
            result = prime * result + ((weight == null) ? 0 : weight.hashCode());
            return result;
        }

        @Override public boolean equals(Object that) {
            if (this == that)
                return true;
            if (that == null)
                return false;
            if (getClass() != that.getClass())
                return false;
            FontStyle other = (FontStyle) that;
            if (posture != other.posture)
                return false;
            if (weight != other.weight)
                return false;
            return true;
        }
        
        private static String makePretty(Object o) {
            String s = o == null? "": o.toString();
            if ( !s.isEmpty()) { 
                s = s.replace("_", " ");
                s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            }
            return s;
        }

        @Override public String toString() {
            return String.format("%s %s", makePretty(weight), makePretty(posture) ).trim();
        }
        
        private <T extends Enum<T>> int compareEnums( T e1, T e2) {
            if ( e1 == e2 ) return 0;
            if ( e1 == null ) return -1;
            if ( e2 == null ) return 1;
            return e1.compareTo(e2);
        }

        @Override public int compareTo(FontStyle fs) {
            int result = compareEnums(weight,fs.weight);
            return ( result != 0 )? result: compareEnums(posture,fs.posture);
        }
        
    }
    
    private static class FontPanel extends GridPane {
        private static final double HGAP = 10;
        private static final double VGAP = 5;
        
        private static final Predicate<Object> MATCH_ALL = new Predicate<Object>() {
            @Override public boolean test(Object t) {
                return true;
            }
        };
        
        private static final Double[] fontSizes = new Double[] {8d,9d,11d,12d,14d,16d,18d,20d,22d,24d,26d,28d,36d,48d,72d};
        
        private static List<FontStyle> getFontStyles( String fontFamily ) {
            Set<FontStyle> set = new HashSet<FontStyle>();
            for (String f : Font.getFontNames(fontFamily)) {
                set.add(new FontStyle(f.replace(fontFamily, "")));
            }
            
            List<FontStyle> result =  new ArrayList<FontStyle>(set);
            Collections.sort(result);
            return result;
            
        }
        
        
        private final FilteredList<String> filteredFontList = new FilteredList<>(FXCollections.observableArrayList(Font.getFamilies()), MATCH_ALL);
        private final FilteredList<FontStyle> filteredStyleList = new FilteredList<>(FXCollections.<FontStyle>observableArrayList(), MATCH_ALL);
        private final FilteredList<Double> filteredSizeList = new FilteredList<>(FXCollections.observableArrayList(fontSizes), MATCH_ALL);
        
        private final ListView<String> fontListView = new ListView<String>(filteredFontList);
        private final ListView<FontStyle> styleListView = new ListView<FontStyle>(filteredStyleList);
        private final ListView<Double> sizeListView = new ListView<Double>(filteredSizeList);
        private final Text sample = new Text("Sample");
        
        public FontPanel() {
            setHgap(HGAP);
            setVgap(VGAP);
            setPrefSize(500, 300);
            setMinSize(500, 300);
            
            ColumnConstraints c0 = new ColumnConstraints();
            c0.setPercentWidth(60);
            ColumnConstraints c1 = new ColumnConstraints();
            c1.setPercentWidth(25);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setPercentWidth(15);
            getColumnConstraints().addAll(c0, c1, c2);
            
            RowConstraints r0 = new RowConstraints();
            r0.setVgrow(Priority.NEVER);
            RowConstraints r1 = new RowConstraints();
            r1.setVgrow(Priority.NEVER);
            RowConstraints r2 = new RowConstraints();
            r2.setFillHeight(true);
            r2.setVgrow(Priority.NEVER);
            RowConstraints r3 = new RowConstraints();
            r3.setPrefHeight(250);
            r3.setVgrow(Priority.NEVER);
            getRowConstraints().addAll(r0, r1, r2, r3);
            
//            // set up filtering
//            fontSearch.textProperty().addListener(new InvalidationListener() {
//                @Override public void invalidated(Observable arg0) {
//                    filteredFontList.setPredicate(new Predicate<String>() {
//                        @Override public boolean test(String t) {
//                            return t.isEmpty() || t.toLowerCase().contains(fontSearch.getText().toLowerCase());
//                        }
//                    });
//                }
//            });
//            postureSearch.textProperty().addListener(new InvalidationListener() {
//                @Override public void invalidated(Observable arg0) {
//                    filteredStyleList.setPredicate(new Predicate<FontPosture>() {
//                        @Override public boolean test(FontPosture t) {
//                            return t == null || t.toString().toLowerCase().startsWith(postureSearch.getText().toLowerCase());
//                        }
//                    });
//                }
//            });
//            
//            // FIXME buggy due to use of NumericField
//            sizeSearch.valueProperty().addListener(new InvalidationListener() {
//                @Override public void invalidated(Observable arg0) {
//                    filteredSizeList.setPredicate(new Predicate<Double>() {
//                        @Override public boolean test(Double t) {
//                            return t == sizeSearch.valueProperty().get();
//                        }
//                    });
//                }
//            });
            
            // layout dialog
            add( new Label("Font"), 0, 0);
//            fontSearch.setMinHeight(Control.USE_PREF_SIZE);
//            add( fontSearch, 0, 1);
            add(fontListView, 0, 1);
            fontListView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
                @Override public ListCell<String> call(ListView<String> listview) {
                    return new ListCell<String>() {
                        @Override protected void updateItem(String family, boolean empty) {
                            super.updateItem(family, empty);
                            
                            if (! empty) {
                                setFont(Font.font(family));
                                setText(family);
                            } else {
                                setText(null);
                            }
                        }
                    };
                }
            });
            
            
            ChangeListener<Object> sampleRefreshListener = new ChangeListener<Object>() {
                @Override public void changed(ObservableValue<? extends Object> arg0, Object arg1, Object arg2) {
                    refreshSample();
                }
            };
            
            fontListView.selectionModelProperty().get().selectedItemProperty().addListener( new ChangeListener<String>() {

                @Override public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
                    String fontFamily = listSelection(fontListView);
                    styleListView.setItems(FXCollections.<FontStyle>observableArrayList(getFontStyles(fontFamily)));       
                    refreshSample();
                }});

            add( new Label("Style"), 1, 0);
//            postureSearch.setMinHeight(Control.USE_PREF_SIZE);
//            add( postureSearch, 1, 1);
            add(styleListView, 1, 1);
            styleListView.selectionModelProperty().get().selectedItemProperty().addListener(sampleRefreshListener);
            
            add( new Label("Size"), 2, 0);
//            sizeSearch.setMinHeight(Control.USE_PREF_SIZE);
//            add( sizeSearch, 2, 1);
            add(sizeListView, 2, 1);
            sizeListView.selectionModelProperty().get().selectedItemProperty().addListener(sampleRefreshListener);
            
            final double height = 45;
            final DoubleBinding sampleWidth = new DoubleBinding() {
                {
                    bind(fontListView.widthProperty(), styleListView.widthProperty(), sizeListView.widthProperty());
                }
                
                @Override protected double computeValue() {
                    return fontListView.getWidth() + styleListView.getWidth() + sizeListView.getWidth() + 3 * HGAP;
                }
            };
            StackPane sampleStack = new StackPane(sample);
            sampleStack.setAlignment(Pos.CENTER_LEFT);
            sampleStack.setMinHeight(height);
            sampleStack.setPrefHeight(height);
            sampleStack.setMaxHeight(height);
            sampleStack.prefWidthProperty().bind(sampleWidth);
            Rectangle clip = new Rectangle(0, height);
            clip.widthProperty().bind(sampleWidth);
            sampleStack.setClip(clip);
            add(sampleStack, 0, 3, 1, 3);
        }
        
        public void setFont(final Font font) {
            final Font _font = font == null ? Font.getDefault() : font;
            if (_font != null) {
                selectInList( fontListView,  _font.getFamily() );
                selectInList( styleListView, new FontStyle(_font));
                selectInList( sizeListView, _font.getSize() );
            }
        }
        
        public Font getFont() {
            try {
                FontStyle style = listSelection(styleListView);
                if ( style == null ) {
                    return Font.font(
                            listSelection(fontListView),
                            listSelection(sizeListView));
                    
                } else { 
                    return Font.font(
                            listSelection(fontListView),
                            style.getWeight(),
                            style.getPosture(),
                            listSelection(sizeListView));
                }
                
            } catch( Throwable ex ) {
                return null;
            }
        }
        
        private void refreshSample() {
            System.out.println(getFont());
            sample.setFont(getFont());
        }
        
        private <T> void selectInList( final ListView<T> listView, final T selection ) {
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    listView.scrollTo(selection);
                    listView.getSelectionModel().select(selection);
                }
            });
        }
        
        private <T> T listSelection(final ListView<T> listView) {
            return listView.selectionModelProperty().get().getSelectedItem();
        }
    }    
    
    
    
    /**
     * The WorkerProgressPane is automatically shown or hidden based on the status of the
     * Worker that is associated with it. You can specify the ProgressIndicator to be used,
     * as well as what kind of a delay the WorkerProgressPane should have before being shown,
     * and the speed at which it should be shown / hidden.
     *
     * The WorkerProgressPane will listen to the Worker as it changes state. When the Worker enters
     * the running state, the pane will wait for some short (configurable) period of time to determine
     * what the rate of progress is, and to figure out whether the task will be complete before the
     * (configurable) showTime is specified. When it is triggered, it will show the pane (it must have
     * previously been added to the parent) and when either the worker succeeds, or fails, or is cancelled,
     * the WorkerProgressPane will hide itself.
     */
    private static class WorkerProgressPane extends Region {
        private Worker<?> worker;
        
        private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
            @Override public void changed(ObservableValue<? extends State> observable, State old, State value) {
                switch(value) {
                    case CANCELLED:
                    case FAILED:
                    case SUCCEEDED:
                        end();
                        break;
                    case SCHEDULED:
                        begin();
                        break;
                    default: //no-op     
                }
            }
        };

        public final void setWorker(final Worker<?> newWorker) { 
            if (newWorker != worker) {
                if (worker != null) {
                    worker.stateProperty().removeListener(stateListener);
                    end();
                }
                if (newWorker != null) {
                    newWorker.stateProperty().addListener(stateListener);
                    if (newWorker.getState() == Worker.State.RUNNING || newWorker.getState() == Worker.State.SCHEDULED) {
                        // It is already running
                        begin();
                    }
                }
                worker = newWorker;
            }
        }

        // If the progress indicator changes, then we need to re-initialize
        // If the worker changes, we need to re-initialize

        private final Dialog dialog;
        private final ProgressBar progressBar;
        
        public WorkerProgressPane(Dialog dialog) {
            this.dialog = dialog;
            
            this.progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            getChildren().add(progressBar);
            
            if (worker != null) {
                progressBar.progressProperty().bind(worker.progressProperty());
            }
        }

        private void begin() {
            progressBar.progressProperty().bind(worker.progressProperty());
            dialog.show();
        }

        private void end() {
            progressBar.progressProperty().unbind();
            dialog.hide();
        }

        @Override protected void layoutChildren() {
            if (progressBar != null) {
                Insets insets = getInsets();
                double w = getWidth() - insets.getLeft() - insets.getRight();
                double h = getHeight() - insets.getTop() - insets.getBottom();

                double prefH = progressBar.prefHeight(-1);
                double x = insets.getLeft() + (w - w) / 2.0;
                double y = insets.getTop() + (h - prefH) / 2.0;

                progressBar.resizeRelocate(x, y, w, prefH);
            }
        }
    }
}
