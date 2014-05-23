/**
 * Copyright (c) 2013, 2014 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.getString;
import static impl.org.controlsfx.i18n.Localization.localize;
import static org.controlsfx.dialog.Dialog.Actions.CANCEL;
import static org.controlsfx.dialog.Dialog.Actions.NO;
import static org.controlsfx.dialog.Dialog.Actions.OK;
import static org.controlsfx.dialog.Dialog.Actions.YES;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Effect;
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
import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.dialog.Dialog.ActionTrait;
import org.controlsfx.dialog.Dialog.Actions;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;


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
 * <h3>Styled Dialogs</h3>
 * 
 * <p>The ControlsFX dialogs API supports displaying dialogs with either a
 * consistent cross-platform titlebar area, or by using the titlebar of the users
 * operating system, or without a titlebar. All of the screenshots above are taken
 * using the cross-platform style, whereas the screenshots below are the
 * same dialog code being rendered using different dialog styles.</p>
 * 
 * <p>
 * A dialog has one of the following styles:
 * <ul>
 * <li>{@link DialogStyle#CROSS_PLATFORM_DARK} - a dialog with a cross-platform title bar.</li>
 * <li>{@link DialogStyle#NATIVE} - a dialog with a native title bar.</li>
 * <li>{@link DialogStyle#UNDECORATED} - a dialog without a title bar.</li>
 * </ul>
 * </p>
 * <p>If no style is specified, the dialogs will be rendered using the default
 * {@link DialogStyle#CROSS_PLATFORM_DARK} style.</p>
 * 
 * To enable this in the Dialogs fluent API, simply call {@link #style(DialogStyle)}
 * when creating the dialog. If you're using the {@link Dialog} class,
 * you can specify the {@code DialogStyle} you want to use as part of the 
 * {@link Dialog#Dialog(Object, String, boolean, DialogStyle)} constructor.
 * 
 * <p>Here are the screenshots of dialogs using different dialog styles:
 * 
 * <br/>
 * <table style="border: 1px solid gray;">
 *   <tr>
 *     <th><center><h3>Platform</h3></center></th>
 *     <th><center><h3>DialogStyle</h3></center></th>
 *     <th><center><h3>Screenshot</h3></center></th>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:center;"><strong>Cross-Platform (default)</strong></td>
 *     <td valign="center" style="text-align:right;"><strong>{@code DialogStyle.CROSS_PLATFORM_DARK}</strong></td>
 *     <td><center><img src="dialog-style/cross-platform.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:center;"><strong>Mac OS X</strong></td>
 *     <td valign="center" style="text-align:right;"><strong>{@code DialogStyle.NATIVE}</strong></td>
 *     <td><center><img src="dialog-style/mac-native-titlebar.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:center;"><strong>Windows 8</strong></td>
 *     <td valign="center" style="text-align:right;"><strong>{@code DialogStyle.NATIVE}</strong></td>
 *     <td><center><img src="dialog-style/windows-8-native-titlebar.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:center;"><strong>Linux (Ubuntu)</strong></td>
 *     <td valign="center" style="text-align:right;"><strong>{@code DialogStyle.NATIVE}</strong></td>
 *     <td><center><img src="dialog-style/linux-native-titlebar.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:center;"><strong>Linux (Ubuntu)</strong></td>
 *     <td valign="center" style="text-align:right;"><strong>{@code DialogStyle.UNDECORATED}</strong></td>
 *     <td><center><img src="dialog-style/linux-undecorated-dialog.png"></center></td>
 *   </tr>
 * </table>
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
 * <p>One limitation of lightweight dialogs is that it is not possible to use
 * the native titlebar feature. If you call both {@link #lightweight()} and
 * {@link #style(DialogStyle)} with {@link DialogStyle#NATIVE} option,
 * the call to enable lightweight takes precedence over the use of the
 * {@code DialogStyle}, so you will end up seeing what is shown in the screenshot
 * below (that is, a cross-platform-looking dialog that is lightweight).
 * 
 * <p>To make a dialog lightweight, you simply call {@link #lightweight()} when
 * constructing the dialog using this Dialogs API. If you are using the 
 * {@link Dialog} class instead, then the decision between heavyweight and 
 * lightweight dialogs must be made in the
 * {@link Dialog#Dialog(Object, String, boolean)} constructor.
 * 
 * <p>Shown below are screenshots of a lightweight dialog whose owner is the
 * Tab. This means that whilst the first tab is blocked for input until the
 * dialog is dismissed by the user, the rest of the UI (including going to other
 * tabs) remains interactive):
 * 
 * <h4>Lightweight dialog whose style is {@link DialogStyle#CROSS_PLATFORM_DARK}</h4>
 * <br>
 * <center><img src="dialog-style/windows-8-lightweight-cross-platform.png"></center>
 * 
 * <h4>Lightweight dialog whose style is {@link DialogStyle#UNDECORATED}</h4>
 * <br>
 * <center><img src="dialog-style/windows-8-lightweight-undecorated.png"></center>
 * 
 * <h3>Java 8 API elements</h3>
 * 
 * <p>Many methods in this class are taking advantage of {@link Optional}. There are many benefits of this, but the main one 
 * is that there no more checking for null and thus no possibility of NullPointerExceptions. Here some things you can do with
 * {@link Optional}s: <br>
 *  
 * <pre>{@code 
 *   optional.isPresent(value->{...}) //perform an operation only if there is a value 
 *   optional.orElse(default)         //get the value of optional with default if it does not exists
 *   optional.map( value -> {...} ).orElse(default) //convert to other type with default
 * }</pre>
 * and many more - check {@link Optional} API.<br>
 * {@link Optional} can be thought of as a stream collection of one or none elements, allowing for functional approach - 
 * chaining methods without keeping state. This especially beneficial in concurrent environments.  
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
 * @see Optional
 */
public final class Dialogs {

    /**
     * USE_DEFAULT can be passed in to {@link #title(String)} and {@link #masthead(String)} methods
     * to specify that the default text for the dialog should be used, where the default text is
     * specific to the type of dialog being shown.
     */
    public static final String USE_DEFAULT = "$$$";

    private Object owner;
    private String title = USE_DEFAULT;
    private String message;
    private String masthead;
    private boolean lightweight;
    private DialogStyle style;
    private Set<Action> actions = new LinkedHashSet<>();
    private Effect backgroundEffect;

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
     * @return dialog instance.
     */
    public Dialogs owner(final Object owner) {
        this.owner = owner;
        return this;
    }

    /**
     * Assigns dialog's title
     * @param title dialog title
     * @return dialog instance.
     */
    public Dialogs title(final String title) {
        this.title = title;
        return this;
    }

    
    /**
     * Assigns dialog's instructions
     * @param message dialog message
     * @return dialog instance.
     */
    public Dialogs message(final String message) {
        this.message = message;
        return this;
    }

    /**
     * Assigns dialog's masthead
     * @param masthead dialog masthead
     * @return dialog instance.
     */
    public Dialogs masthead(final String masthead) {
        this.masthead = masthead;
        return this;
    }
    
    /**
     * Completely replaces standard actions with provided ones.
     * @param actions new dialog actions
     * @return dialog instance.
     */
    public Dialogs actions( Collection<? extends Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }
    
    /**
     * Completely replaces standard actions with provided ones.  
     * @param actions new dialog actions
     * @return dialog instance.
     */
    public Dialogs actions( Action... actions) {
        return actions( Arrays.asList(actions));
    }
    
    /**
     * Sets the effect which should be applied to background components when dialog appears.
     * If not set or set to null, default semi-transparent panel will be used
     * Good example of effect to use is GaussianBlur 
     * Currently works only with lightweight dialogs.
     * @param effect to be applied
     * @return dialog instance
     */
    public Dialogs backgroundEffect(Effect effect) {
        this.backgroundEffect = effect;
        return this;
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
     * Specifies that the dialog should use the given {@code DialogStyle}
     * rather than the custom cross-platform rendering used by default.
     * Refer to the Dialogs class JavaDoc for more information.
     * 
     * @param style The {@code DialogStyle} of the dialog.
     * @return dialog instance.
     */
    public Dialogs style(DialogStyle style) {
        this.style = style;
        return this;
    }
    
    /**
     * Shows information dialog.
     */
    public void showInformation() {
        showSimpleContentDialog(Type.INFORMATION);
    }

    /**
     * Shows confirmation dialog.
     * @return action used to close dialog.
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
        dlg.setContent(message != null && ! message.isEmpty() ? message : exception.getMessage());
        dlg.setExpandableContent(buildExceptionDetails(exception));
        
        if ( !actions.isEmpty()) {
            dlg.getActions().clear();
            dlg.getActions().addAll(actions);
        }
        
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
        dlg.setContent(message != null && ! message.isEmpty() ? message : exception.getMessage());
        
        dlg.getActions().clear();
        
        Action openExceptionAction = new AbstractAction(localize(asKey("exception.button.label"))) {
            @Override public void handle(ActionEvent ae) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                exception.printStackTrace(pw);
                String moreDetails = sw.toString();
                new ExceptionDialog((Window)owner, moreDetails, style).show();
            }
        };
        ButtonBar.setType(openExceptionAction, ButtonType.HELP_2);
        dlg.getActions().add(openExceptionAction);
        
        if ( !actions.isEmpty()) {
            dlg.getActions().addAll(actions);
        }
        
        return dlg.show();
    }

    /**
     * Shows dialog with one text field
     * @param defaultValue text field default value 
     * @return Optional of text from input field if OK action is used otherwise Optional.EMPTY 
     */
    public Optional<String> showTextInput(String defaultValue) {
        Dialog dlg = buildDialog(Type.INPUT);
        final TextField textField = new TextField(defaultValue);
        dlg.setContent(buildInputContent(textField));
        
        return Optional.ofNullable( dlg.show() == OK ? textField.getText() : null );
    }

    /**
     * Shows dialog with one text field 
     * @return Optional of text from input field or Optional.EMPTY if dialog is cancelled 
     */
    public Optional<String> showTextInput() {
        return showTextInput("");
    }

    /**
     * Show a dialog with one combobox filled with provided choices. The combobox selection 
     * will be set to a default value if one is provided.
     * @param defaultChoice default combobox selection 
     * @param choices dialog choices
     * @return Optional of selected choice or Optional.EMPTY if dialog is cancelled.
     */
    public <T> Optional<T> showChoices(T defaultChoice, Collection<T> choices) {

        Dialog dlg = buildDialog(Type.INPUT);
        
        final double MIN_WIDTH = 150;
        SelectionModel<T> selectionModel=null;
        if (choices.size() > 10) {
            // use ComboBox
            ComboBox<T> comboBox = new ComboBox<T>();
            comboBox.setMinWidth(MIN_WIDTH);
            comboBox.getItems().addAll(choices);
            selectionModel = comboBox.getSelectionModel();
            dlg.setContent(buildInputContent(comboBox));
        } else {
            // use ChoiceBox
            ChoiceBox<T> choiceBox = new ChoiceBox<T>();
            choiceBox.setMinWidth(MIN_WIDTH);
            choiceBox.getItems().addAll(choices);
            selectionModel = choiceBox.getSelectionModel();
            dlg.setContent(buildInputContent(choiceBox));
        }
        if (defaultChoice==null) {
            selectionModel.selectFirst();
        } else {
            selectionModel.select(defaultChoice);
        }

        return Optional.ofNullable( dlg.show() == OK ? selectionModel.getSelectedItem() : null);

    }

    /**
     * Show a dialog with one combobox filled with provided choices 
     * @param choices dialog choices
     * @return Optional of selected choice or Optinal.EMPTY if dialog is cancelled
     */
    public <T> Optional<T> showChoices(Collection<T> choices) {
        return showChoices(null, choices);
    }

    /**
     * Show a dialog with one combobox filled with provided choices 
     * @param choices dialog choices
     * @return Optional of selected choice or Optional.EMPTY if dialog is cancelled
     */
    public <T> Optional<T> showChoices(@SuppressWarnings("unchecked") T... choices) {
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
                   commandLink.handle( new ActionEvent(dlg, ae.getTarget()));
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
     * Show font selection dialog, allowing to manipulate font name, style and size. 
     * 
     * @param font default font value 
     * @return Optional of selected font. Optional.Empty returned if dialog is cancelled.
     */
    public Optional<Font> showFontSelector(Font font) {
        FontPanel fontPanel = new FontPanel();
        fontPanel.setFont(font);
        title(Dialogs.USE_DEFAULT);
        Dialog dlg = buildDialog(Type.FONT);
        dlg.setIconifiable(false);
        dlg.setContent(fontPanel);
        return Optional.ofNullable( Dialog.Actions.OK == dlg.show() ? fontPanel.getFont(): null );
    }
    
    /**
     * Creates a progress bar {@link Dialog} which is attached to the given
     * {@link Worker} instance.  The worker will be observed permanently and
     * the attached dialog will be shown and hidden as the worker starts and
     * completes. If the worker's state is
     * {@link javafx.concurrent.Worker.State#SCHEDULED} or
     * {@link javafx.concurrent.Worker.State#RUNNING} the dialog will be visible.
     *
     * <h2><u>Expected Behavior</u></h2>
     *
     * If the worker has a state of {@link javafx.concurrent.Worker.State#READY},
     * {@link javafx.concurrent.Worker.State#SUCCEEDED},
     * {@link javafx.concurrent.Worker.State#FAILED}, or
     * {@link javafx.concurrent.Worker.State#CANCELLED}, the dialog will be shown
     * when the worker's state changes to SUCCEDED or RUNNING.
     *
     * If the worker has a state of SCHEDULED or RUNNING, the dialog will be
     * hidden when the worker's state changes to SUCCEEDED, FAILED, or CANCELLED.
     *
     * All other changes in worker state will not cause the visibility of the
     * attached dialog to change.  Note that if a worker is submitted with a
     * state of SCHEDULED or RUNNING the dialog will be shown immediately.
     *
     * <h2><u>Multiple Workers</u></h2>
     *
     * It's important to make sure that only one progress dialog is shown at a
     * time, especially when using {@link LightweightDialog}s.  If an attempt is
     * made to show more than one lightweight dialog at a time, an
     * {@link IllegalArgumentException} will be thrown.  If several workers have
     * dialogs attached, care should be taken to ensure only one of those workers
     * is SCHEDULED or RUNNING at any given time.
     *
     * <h2><u>Worker Completion Handling</u></h2>
     *
     * It's important to remember, especially when using lightweight dialogs, that
     * when a worker completes the attached dialog may still be visible.  If a
     * worker's completion handler attempts to show another dialog, the result may
     * be undesirable.  For heavyweight dialogs it's possible to end up with two
     * dialogs on the screen at once.  For lightweight dialogs it's possible for an
     * {@link IllegalArgumentException} to be thrown.  If a worker's completion
     * handler is going to cause another dialog to be shown, or is going to restart
     * a failed worker, {@link Platform#runLater(Runnable)} should be used to
     * ensure notification about the worker's completion is finished first.  Doing
     * so makes sure the worker's attached dialog has been hidden before any new
     * dialogs are shown during completion handling.
     *
     * <pre>{@code
    Service<Void> service = getService();

    service.setOnFailed(new EventHandler<WorkerStateEvent>() {
        public void handle(WorkerStateEvent event) {
            Platform.runLater(new Runnable() {
                public void run() {
                    // Make sure the service state is still failed.  If so, the
                    // call to shouldRetry() will show the user a dialog asking
                    // if they'd like to retry and will return true or false
                    // depending on the user's answer.
                    if(isFailed(service) && shouldRetry()) {
                        service.restart();
                    }
                }
            });
        }
    });
     * }</pre>
     *
     * The above code is much cleaner with Java 8 syntax:
     *
     * <pre>{@code
    Service<Void> service = getService();

    service.setOnFailed(event -> Platform.runLater(() -> {
        if(isFailed(service) && shouldRetry()) {
            service.restart();
        }
    }));
     * }</pre>
     */

    public void showWorkerProgress(final Worker<?> worker) {
        Dialog dlg = buildDialog(Type.PROGRESS);
        dlg.setClosable(false);
        
        final Label progressMessage = new Label();
        progressMessage.textProperty().bind(worker.messageProperty());

        final WorkerProgressPane content = new WorkerProgressPane(dlg);
        content.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(10, progressMessage, content);
        vbox.setMaxWidth(Double.MAX_VALUE);
        vbox.setPrefSize(300, 100);
        
        if (message != null) {
            vbox.getChildren().add(0, new Label(message));
        }
        dlg.setContent(vbox);
        content.setWorker(worker);
    }
    
    /**
     * The UserInfo class is used in relation to the pre-built login dialog 
     * available from {@link Dialogs#showLogin(UserInfo, Callback)}.
     */
    public static class UserInfo {
    	
    	private String userName;
    	private String password;
    	
    	/**
    	 * Creates a UserInfo instance with the given username and password.
    	 */
		public UserInfo(String userName, String password) {
			this.userName = userName == null? "": userName;
			this.password = password == null? "": password;
		}
		
		/**
		 * Returns the currently set username.
		 */
		public String getUserName() {
			return userName;
		}
		
		/**
		 * Sets the username to the given value.
		 */
		public void setUserName(String userName) {
			this.userName = userName;
		}
		
		/**
         * Returns the currently set password.
         */
		public String getPassword() {
			return password;
		}
		
		/**
         * Sets the password to the given value.
         */
		public void setPassword(String password) {
			this.password = password;
		}

		/** {@inheritDoc} */
		@Override public String toString() {
			return "UserInfo [userName=" + userName + "]";
		}

		/** {@inheritDoc} */
		@Override public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((password == null) ? 0 : password.hashCode());
			result = prime * result
					+ ((userName == null) ? 0 : userName.hashCode());
			return result;
		}

		/** {@inheritDoc} */
		@Override public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserInfo other = (UserInfo) obj;
			if (password == null) {
				if (other.password != null)
					return false;
			} else if (!password.equals(other.password))
				return false;
			if (userName == null) {
				if (other.userName != null)
					return false;
			} else if (!userName.equals(other.userName))
				return false;
			return true;
		}
    }
    
    
    
    /**
     * Creates a Login {@link Dialog} whith user name and password  fields
     * 
     * @param initialUserInfo user information initially shown in the dialog
     * @param authenticator callback represents actual authentication process. Exceptions coming from this callback are interpreted as
     * authentication errors and will be shown as error message. In case of an exception the dialog will not closed to give the user 
     * an opportunity to correct their information and try again
     * 
     * @return optional UserInfo. Optional.EMPTY returned in case of cancelled dialog otherwise Optional of UserInfo value with provided user name
     * and password
     *  
     */
    public Optional<UserInfo> showLogin( final UserInfo initialUserInfo, final Callback<UserInfo, Void> authenticator ) {
    	
    	CustomTextField txUserName = (CustomTextField) TextFields.createClearableTextField();// new CustomTextField();
    	txUserName.setLeft(new ImageView( DialogResources.getImage("login.user.icon")) );
    	
    	CustomPasswordField txPassword = (CustomPasswordField) TextFields.createClearablePasswordField();
    	txPassword.setLeft(new ImageView( DialogResources.getImage("login.password.icon")));
		
		Label lbMessage= new Label(""); 
		lbMessage.getStyleClass().addAll("message-banner");
		lbMessage.setVisible(false);
		lbMessage.setManaged(false);
		
		final VBox content = new VBox(10);
		content.getChildren().add(lbMessage);
		content.getChildren().add(txUserName);
		content.getChildren().add(txPassword);
		
//		NotificationPane notificationPane = new NotificationPane(content);
//		notificationPane.setShowFromTop(true);
		
		Action actionLogin = new DefaultDialogAction(getString("login.dlg.login.button"), ActionTrait.DEFAULT) {
			{
				ButtonBar.setType(this, ButtonType.OK_DONE);
			}
			
			@Override public void handle(ActionEvent ae) {
				Dialog dlg = (Dialog) ae.getSource();
				try {
					if ( authenticator != null ) {
						authenticator.call( new UserInfo(txUserName.getText(), txPassword.getText() ) );
					}
					lbMessage.setVisible(false);
					lbMessage.setManaged(false);
					dlg.hide();
					dlg.setResult(this);
				} catch( Throwable ex ) {
					//Platform.runLater( () -> notificationPane.show(ex.getMessage()) );
					lbMessage.setVisible(true);
					lbMessage.setManaged(true);
					lbMessage.setText(ex.getMessage());
					dlg.sizeToScene();
					dlg.shake();
					ex.printStackTrace();
				}
			}

			public String toString() {
				return "LOGIN";
			};
		};
		
		final Dialog dlg = buildDialog(Type.LOGIN);
        dlg.setContent(content);
        
        dlg.setResizable(false);
		dlg.setIconifiable(false);
		if ( dlg.getGraphic() == null ) { 
			dlg.setGraphic( new ImageView( DialogResources.getImage("login.icon")));
		}
		dlg.getActions().setAll(actionLogin, Dialog.Actions.CANCEL);
		String userNameCation = getString("login.dlg.user.caption");
		String passwordCaption = getString("login.dlg.pswd.caption");
		txUserName.setPromptText(userNameCation);
		txUserName.setText( initialUserInfo.getUserName());
		txPassword.setPromptText(passwordCaption);
		txPassword.setText(new String(initialUserInfo.getPassword()));

		ValidationSupport validationSupport = new ValidationSupport();
		Platform.runLater( () -> {
			String requiredFormat = "'%s' is required";
			validationSupport.registerValidator(txUserName, Validator.createEmptyValidator( String.format( requiredFormat, userNameCation )));
			validationSupport.registerValidator(txPassword, Validator.createEmptyValidator(String.format( requiredFormat, passwordCaption )));
			actionLogin.disabledProperty().bind(validationSupport.invalidProperty());
			txUserName.requestFocus();
	    } );

    	return Optional.ofNullable( 
    			dlg.show() == actionLogin? 
    					new UserInfo(txUserName.getText(), txPassword.getText()): 
    					null);
    }
    
    
    
    /***************************************************************************
     * 
     * Private API
     * 
     **************************************************************************/

    private Dialog buildDialog(final Type dlgType) {
        String actualTitle = title == null ? null : USE_DEFAULT.equals(title) ? dlgType.getDefaultTitle() : title;
        String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType.getDefaultMasthead() : masthead);
        Dialog dlg = new Dialog(owner, actualTitle, lightweight, style);
        dlg.setResizable(false);
        dlg.setIconifiable(false);
        Image image = dlgType.getImage();
        if (image != null) {
            dlg.setGraphic(new ImageView(image));
        }
        dlg.setMasthead(actualMasthead);
        dlg.getActions().addAll(dlgType.getActions());
        dlg.setBackgroundEffect(backgroundEffect);
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
        Label label = new Label( localize(asKey("exception.dlg.label")));

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
        Node view = graphic == null? new ImageView( DialogResources.getImage("command.link.icon")) : graphic;
        Pane graphicContainer = new Pane(view);
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
        ERROR("error.image",          asKey("error.dlg.title"),   asKey("error.dlg.masthead"), OK),
        INFORMATION("info.image",     asKey("info.dlg.title"),    asKey("info.dlg.masthead"), OK),
        WARNING("warning.image",      asKey("warning.dlg.title"), asKey("warning.dlg.masthead"), OK),
        CONFIRMATION("confirm.image", asKey("confirm.dlg.title"), asKey("confirm.dlg.masthead"), YES, NO, CANCEL),
        INPUT("confirm.image",        asKey("input.dlg.title"),   asKey("input.dlg.masthead"), OK, CANCEL),
        FONT( null,                   asKey("font.dlg.title"),    asKey("font.dlg.masthead"), OK, CANCEL),
        PROGRESS("info.image",        asKey("progress.dlg.title"), asKey("progress.dlg.masthead")),
        LOGIN("login.image",          asKey("login.dlg.title"),    asKey("login.dlg.masthead"), OK, CANCEL);

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
            return localize(defaultMasthead);
        }

        public String getDefaultTitle() {
            return localize(defaultTitle);
        }

        public Collection<Action> getActions() {
            return actions;
        }
    }
    
    
    /**
     * Command Link class.
     * Represents one command link in command links dialog. 
     */
    public static class CommandLink extends DefaultDialogAction {
        
        public CommandLink( Node graphic, String text, String longText ) {
            super(text);
            setLongText(longText);
            setGraphic(graphic);
        }
        
        public CommandLink( String message, String comment ) {
            this(null, message, comment);
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
        private final Text sample = new Text(localize(asKey("font.dlg.sample.text")));
        
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
            add( new Label(localize(asKey("font.dlg.font.label"))), 0, 0);
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

            add( new Label(localize(asKey("font.dlg.style.label"))), 1, 0);
//            postureSearch.setMinHeight(Control.USE_PREF_SIZE);
//            add( postureSearch, 1, 1);
            add(styleListView, 1, 1);
            styleListView.selectionModelProperty().get().selectedItemProperty().addListener(sampleRefreshListener);
            
            add( new Label(localize(asKey("font.dlg.size.label"))), 2, 0);
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
     * The WorkerProgressPane takes a {@link Dialog} and a {@link Worker}
     * and links them together so the dialog is shown or hidden depending
     * on the state of the worker.  The WorkerProgressPane also includes
     * a progress bar that is automatically bound to the progress property
     * of the worker.  The way in which the WorkerProgressPane shows and
     * hides its worker's dialog is consistent with the expected behavior
     * for {@link #showWorkerProgress(Worker)}.
     */
    private static class WorkerProgressPane extends Region {
        private Worker<?> worker;

        private boolean dialogVisible = false;
        private boolean cancelDialogShow = false;

        private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
            @Override public void changed(ObservableValue<? extends State> observable, State old, State value) {
                switch(value) {
                    case CANCELLED:
                    case FAILED:
                    case SUCCEEDED:
                        if(!dialogVisible) {
                            cancelDialogShow = true;
                        }
                        else if(old == State.SCHEDULED || old == State.RUNNING) {
                            end();
                        }
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

                worker = newWorker;

                if (newWorker != null) {
                    newWorker.stateProperty().addListener(stateListener);
                    if (newWorker.getState() == Worker.State.RUNNING || newWorker.getState() == Worker.State.SCHEDULED) {
                        // It is already running
                        begin();
                    }
                }
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
            // Platform.runLater needs to be used to show the dialog because
            // the call begin() is going to be occurring when the worker is
            // notifying state listeners about changes.  If Platform.runLater
            // is not used, the call to show() will cause the worker to get
            // blocked during notification and it will prevent the worker
            // from performing any additional notification for state changes.
            //
            // Sine the dialog is hidden as a result of a change in worker
            // state, calling show() without wrapping it in Platform.runLater
            // will cause the progress dialog to run forever when the dialog
            // is attached to workers that start out with a state of READY.
            //
            // This also creates a case where the worker's state can change
            // to finished before the dialog is shown, resulting in an
            // an attempt to hide the dialog before it is shown.  It's
            // necessary to track whether or not this occurs, so flags are
            // set to indicate if the dialog is visible and if if the call
            // to show should still be allowed.
            cancelDialogShow = false;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    if(!cancelDialogShow) {
                        progressBar.progressProperty().bind(worker.progressProperty());
                        dialogVisible = true;
                        dialog.show();
                    }
                }
            });
        }

        private void end() {
            progressBar.progressProperty().unbind();
            dialogVisible = false;
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
