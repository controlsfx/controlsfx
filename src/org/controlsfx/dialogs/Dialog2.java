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

import static org.controlsfx.dialogs.DialogResources.getString;
import static org.controlsfx.dialogs.DialogTemplate2.DialogAction.CANCEL;
import static org.controlsfx.dialogs.DialogTemplate2.DialogAction.CLOSE;
import static org.controlsfx.dialogs.DialogTemplate2.DialogAction.NO;
import static org.controlsfx.dialogs.DialogTemplate2.DialogAction.OK;
import static org.controlsfx.dialogs.DialogTemplate2.DialogAction.YES;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.dialogs.DialogTemplate2.Action;

public final class Dialog2 {

    /**
     * USE_DEFAULT can be passed in to {@link #title(String)} and {@link #masthead(String)} methods
     * to specify that the default text for the dialog should be used, where the default text is
     * specific to the type of dialog being shown.
     */
    public static final String USE_DEFAULT = "$$$";

    private final Stage owner;
    private String title;
    private String message;
    private String masthead;

    // private String details;
    // private boolean openDetailsInNewWindow = false;
    /**
     * TODO delete me - this is just for testing!!
     */
    public static void setMacOS(boolean b) {
        DialogTemplate.setMacOS(b);
        DialogTemplate2.setMacOS(b);
    }

    public static void setWindows(boolean b) {
        DialogTemplate.setWindows(b);
        DialogTemplate2.setWindows(b);
    }

    public static Dialog2 build(final Stage owner) {
        return new Dialog2(owner);
    }

    private Dialog2(final Stage owner) {
        this.owner = owner;
    }

    public Dialog2 title(final String title) {
        this.title = title;
        return this;
    }

    public Dialog2 message(final String message) {
        this.message = message;
        return this;
    }

    public Dialog2 masthead(final String masthead) {
        this.masthead = masthead;
        return this;
    }

    // public Dialog2 details(final String details) {
    // this.details = details;
    // return this;
    // }

    // public Dialog2 openDetailsInNewWindow(final boolean openDetailsInNewWindow) {
    // this.openDetailsInNewWindow = openDetailsInNewWindow;
    // return this;
    // }

    // public Dialog2 details(final Throwable throwable) {
    // StringWriter sw = new StringWriter();
    // PrintWriter pw = new PrintWriter(sw);
    // throwable.printStackTrace(pw);
    // details(sw.toString());
    // return this;
    // }

    public void showInformation() {
        showSimpleContentDialog(Type.INFORMATION);
    }

    public Action showConfirm() {
        return showSimpleContentDialog(Type.CONFIRMATION);
    }

    public Action showWarning() {
        return showSimpleContentDialog(Type.WARNING);
    }

    public Action showError() {
        return showSimpleContentDialog(Type.ERROR);
    }

    // TODO: Has to be generalized to have details for any type of dialog
    public Action showException(Throwable exception) {
        DialogTemplate2 template = getDialogTemplate(Type.ERROR);
        template.setContent(exception.getMessage());
        template.setExpandableContent(buildExceptionDetails(exception));
        return showDialog(template);
    }

    public String showTextInput(String defaultValue) {
        DialogTemplate2 template = getDialogTemplate(Type.INPUT);
        final TextField textField = new TextField(defaultValue);
        template.setContent(buildInputContent(textField));
        return showDialog(template) == OK ? textField.getText() : null;
    }

    public String showTextInput() {
        return showTextInput("");
    }

    @SuppressWarnings("unchecked") public <T> T showChoices(T defaultValue, Collection<T> choices) {

        DialogTemplate2 template = getDialogTemplate(Type.INPUT);
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
            template.setContent(buildInputContent(comboBox));
        } else {
            // use ChoiceBox
            ChoiceBox<T> choiceBox = new ChoiceBox<T>();
            choiceBox.getItems().addAll(choices);
            choiceBox.getSelectionModel().select(defaultValue);
            choiceBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
            template.setContent(buildInputContent(choiceBox));
        }

        return showDialog(template) == OK ? (T) response[0] : null;

    }

    public <T> T showChoices(Collection<T> choices) {
        return showChoices(null, choices);
    }

    // public <T> T showChoices( T defaultValue, @SuppressWarnings("unchecked") T... choices ) {
    // return showInputChoices( defaultValue, Arrays.asList(choices));
    // }

    public <T> T showChoices(@SuppressWarnings("unchecked") T... choices) {
        return showChoices(Arrays.asList(choices));
    }

    /***************************************************************************
     * Private API
     **************************************************************************/

    private static enum Type {
        // TODO maybe introduce a MORE_DETAILS type, rather than use the ERROR
        // type?
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

    private DialogTemplate2 getDialogTemplate(final Type dlgType) {
        String actualTitle = title == null ? null : (USE_DEFAULT.equals(title) ? dlgType.getDefaultTitle() : title);
        String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType.getDefaultMasthead() : masthead);
        DialogTemplate2 template = new DialogTemplate2(owner, actualTitle);
        template.setResizable(false);
        template.setIcon(dlgType.getImage());
        template.setMasthead(actualMasthead);
        template.getActions().addAll(dlgType.getActions());
        return template;
    }

    private Action showSimpleContentDialog(final Type dlgType) {
        DialogTemplate2 template = getDialogTemplate(dlgType);
        template.setContent(message);
        template.show();
        return template.getResult();
    }

    private static Action showDialog(DialogTemplate2 template) {
        try {
            template.getDialog().centerOnScreen();
            template.show();
            return template.getResult();
        } catch (Throwable e) {
            return CLOSE;
        }
    }

    private Node buildInputContent(Control inputControl) {

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.BASELINE_LEFT);
        
        // hbox.setPrefWidth(MAIN_TEXT_WIDTH);

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
        VBox detailsPane = new VBox(10);

        Label label = new Label(getString("exception.dialog.label"));
        VBox.setVgrow(label, Priority.NEVER);

        detailsPane.getChildren().add(label);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        TextArea text = new TextArea(sw.toString());
        text.setEditable(false);
        text.setWrapText(true);
        text.setPrefWidth(60 * 8);
        text.setMaxHeight(Double.MAX_VALUE);
        detailsPane.getChildren().add(text);
        VBox.setVgrow(text, Priority.ALWAYS);

        return detailsPane;

    }

}
