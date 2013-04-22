package org.controlsfx.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.controlsfx.dialogs.DialogTemplate2.Action;
import org.controlsfx.dialogs.DialogTemplate2.DialogAction;

public final class Dialog2 {
    
    /**
     * USE_DEFAULT can be passed in to {@link #title(String)} and
     * {@link #masthead(String)} methods to specify that the default text for 
     * the dialog should be used, where the default text is specific to the type
     * of dialog being shown.  
     */
    public static final String USE_DEFAULT = "$$$";

    private final Stage owner;
    private String title;
    private String message;
    private String masthead;
//    private Options options;
    private String details;
    private boolean openDetailsInNewWindow = false;
//    private T inputInitialValue = null;
//    private List<T> inputChoices = null;

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

    // NOT PUBLIC API
    static enum Type {
        // TODO maybe introduce a MORE_DETAILS type, rather than use the ERROR
        // type?
        ERROR( "error.image", "Error", "Error", DialogAction.OK),
        INFORMATION("info.image", "Message", "Message", DialogAction.OK),
        WARNING("warning.image", "Warning", "Warning", DialogAction.OK),
        CONFIRMATION("confirm.image", "Select an option", "Select an option", 
                      DialogAction.YES, DialogAction.NO, DialogAction.CANCEL),
        INPUT("confirm.image", "Select an option", "Select an option", 
                      DialogAction.OK, DialogAction.CANCEL);


        private final String defaultTitle; 
        private final String defaultMasthead;
        private final Collection<Action> actions;
        private final String imageResource;
        private Image image;

        Type(String imageResource, String defaultTitle, String defaultMasthead, Action...  actions) {
//            this.defaultOptions = defaultOptions;
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

    /**
     * An enumeration used to specify which buttons to show to the user in a
     * Dialog.
     */
//    public static enum Options {
//        YES_NO, YES_NO_CANCEL, OK, OK_CANCEL;
//    }

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
    
//    public Dialog2 actions(Actions) {
//        this.options = options;
//        return this;
//    }

    public Dialog2 details(final String details) {
        this.details = details;
        return this;
    }

    public Dialog2 openDetailsInNewWindow(final boolean openDetailsInNewWindow) {
        this.openDetailsInNewWindow = openDetailsInNewWindow;
        return this;
    }

    public Dialog2 details(final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        details(sw.toString());
        return this;
    }

//    public Dialog2 inputInitialValue(final T initialValue) {
//        this.inputInitialValue = initialValue;
//        return this;
//    }

//    public <T> Dialog2 inputChoices(final List<T> choices) {
//        this.inputChoices = choices;
//        return this;
//    }
//
//    public Dialog2<T> inputChoices(
//            @SuppressWarnings("unchecked") final T... choices) {
//        return inputChoices(Arrays.asList(choices));
//    }

    public void showInformationDialog() {
        showSimpleContentDialog(Type.INFORMATION);
    }

    public Action showConfirmDialog() {
        return showSimpleContentDialog(Type.CONFIRMATION);
    }

    public Action showWarningDialog() {
        return showSimpleContentDialog(Type.WARNING);
    }

    public Action showErrorDialog() {
        return showSimpleContentDialog(Type.ERROR);
    }

    // TODO: Has to be generalized to have details for any type of dialog
    public Action showMoreDetailsDialog() {
        DialogTemplate2 template = new DialogTemplate2(owner, title);
        template.setMasthead(masthead);
        // null);
//      template.setMoreDetailsContent(message, details, openDetailsInNewWindow);
        return showDialog(template);
    }

//    public T showInputDialog() {
//        DialogTemplate<T> template = new DialogTemplate<>(owner, title, masthead, null);
//        template.setInputContent(message, inputInitialValue, inputChoices);
//        template.getDialog().centerOnScreen();
//        template.show();
//        return template.getInputResponse();
//    }

    /***************************************************************************
     * Private API
     **************************************************************************/

    private DialogTemplate2 getDialogTemplate(final Type dlgType) {
        String actualTitle = title == null ? null : (USE_DEFAULT.equals(title) ? dlgType.getDefaultTitle() : title);
        String actualMasthead = masthead == null ? null : (USE_DEFAULT.equals(masthead) ? dlgType.getDefaultMasthead() : masthead);
        DialogTemplate2 template = new DialogTemplate2(owner, actualTitle);
        template.setIcon( dlgType.getImage() );
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
            return DialogAction.CLOSE;
        }
    }

}
