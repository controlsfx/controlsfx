package org.controlsfx.dialogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import static org.controlsfx.dialogs.DialogResources.*;
import static org.controlsfx.dialogs.Dialogs.DialogResponse.*;

/**
 * A class containing a number of pre-built JavaFX modal dialogs.
 */
public class Dialogs {
    
    // NOT PUBLIC API
    static enum DialogType {
        ERROR(DialogOptions.OK,"error.image") {
            @Override public String getDefaultMasthead() { return "Error"; }  
        },
        INFORMATION(DialogOptions.OK, "info.image") {
            @Override public String getDefaultMasthead() { return "Message"; }
        },
        WARNING(DialogOptions.OK,"warning.image") {
            @Override public String getDefaultMasthead() { return "Warning"; }
        },
        CONFIRMATION(DialogOptions.YES_NO_CANCEL, "confirm.image") {
            @Override public String getDefaultMasthead() { return "Select an Option"; }
        },
        INPUT(DialogOptions.OK_CANCEL, "confirm.image") {
            @Override public String getDefaultMasthead() { return "Select an Option"; }
        };
        
        private final DialogOptions defaultOptions;
        private final String imageResource;
        private ImageView imageView;
        
        
        DialogType(DialogOptions defaultOptions, String imageResource) {
            this.defaultOptions = defaultOptions;
            this.imageResource = imageResource;
        }
        
        public ImageView getImage() {
            if (imageView == null) {
                imageView = getIcon(imageResource);
            }
            return imageView;
        }

        public String getDefaultTitle() {
            return getDefaultMasthead();
        }
        
        public abstract String getDefaultMasthead();

        public DialogOptions getDefaultOptions() {
            return defaultOptions;
        }
    }
    
    /**
     * An enumeration used to specify the response provided by the user when
     * interacting with a dialog.
     */
    public static enum DialogResponse {
        YES,
        NO,
        CANCEL,
        OK,
        CLOSED
    }
    
    /**
     * An enumeration used to specify which buttons to show to the user in a 
     * Dialog.
     */
    public static enum DialogOptions {
        YES_NO,
        YES_NO_CANCEL,
        OK,
        OK_CANCEL;
    }
    
    
    /***************************************************************************
     * Confirmation Dialogs
     **************************************************************************/    
    
    /**
     * Brings up a dialog with the options Yes, No and Cancel; with the title, 
     * <b>Select an Option</b>. 
     * 
     * @param owner
     * @param message
     * @return 
     */
    public static DialogResponse showConfirmDialog(final Stage owner, 
                                                  final String message) {
        return showConfirmDialog(owner, 
                                    message, 
                                    DialogType.CONFIRMATION.getDefaultMasthead());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead) {
        return showConfirmDialog(owner, 
                                    message, 
                                    masthead, 
                                    DialogType.CONFIRMATION.getDefaultTitle());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead, 
                                                   final String title) {
        return showConfirmDialog(owner, 
                                    message, 
                                    masthead, 
                                    title, 
                                    DialogType.CONFIRMATION.getDefaultOptions());
    }
    
    public static DialogResponse showConfirmDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead, 
                                                   final String title, 
                                                   final DialogOptions options) {
        return showSimpleContentDialog(owner, 
                                    title,
                                    masthead, 
                                    message, 
                                    DialogType.CONFIRMATION,
                                    options);
    }
    
    

    /***************************************************************************
     * Information Dialogs
     **************************************************************************/
    
    public static void showInformationDialog(final Stage owner,
                                             final String message) {
        showInformationDialog(owner, 
                                    message, 
                                    DialogType.INFORMATION.getDefaultMasthead());
    }
    
    public static void showInformationDialog(final Stage owner, 
                                             final String message,
                                             final String masthead){
        showInformationDialog(owner, 
                                    message, 
                                    masthead,
                                    DialogType.INFORMATION.getDefaultTitle());
    }
    
    /*
     * Info message string displayed in the masthead
     * Info icon 48x48 displayed in the masthead
     * "OK" button at the bottom.
     *
     * text and title strings are already translated strings.
     */
    public static void showInformationDialog(final Stage owner, 
                                             final String message,
                                             final String masthead, 
                                             final String title){
        showSimpleContentDialog(owner, 
                                    title,
                                    masthead, 
                                    message, 
                                    DialogType.INFORMATION,
                                    DialogType.INFORMATION.getDefaultOptions());
    }
    
    
    
    
    
    /***************************************************************************
     * Warning Dialogs
     **************************************************************************/
    
    /**
     * showWarningDialog - displays warning icon instead of "Java" logo icon
     *                     in the upper right corner of masthead.  Has masthead
     *                     and message that is displayed in the middle part
     *                     of the dialog.  No bullet is displayed.
     *
     *
     * @param  owner           - Component to parent the dialog to
     * @param  appInfo         - AppInfo object
     * @param  masthead        - masthead in the top part of the dialog
     * @param  message         - question to display in the middle part
     * @param  title           - dialog title string from resource bundle
     *
     */
    public static DialogResponse showWarningDialog(final Stage owner, 
                                                   final String message) {
        return showWarningDialog(owner, 
                                message, 
                                DialogType.WARNING.getDefaultMasthead());
    }
    
    public static DialogResponse showWarningDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead) {
        return showWarningDialog(owner, 
                                message, 
                                masthead,
                                DialogType.WARNING.getDefaultTitle());
    }
                                        
    public static DialogResponse showWarningDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead, 
                                                   final String title) {
        return showWarningDialog(owner, 
                                message, 
                                masthead,
                                title,
                                DialogType.WARNING.getDefaultOptions());
    }
                                        
    public static DialogResponse showWarningDialog(final Stage owner, 
                                                   final String message,
                                                   final String masthead, 
                                                   final String title,
                                                   final DialogOptions options) {
        return showSimpleContentDialog(owner, 
                                title,
                                masthead, 
                                message, 
                                DialogType.WARNING, 
                                options);
    }


    
    /***************************************************************************
     * Exception / Error Dialogs
     **************************************************************************/

    public static DialogResponse showErrorDialog(final Stage owner, 
                                                 final String message) {
        return showErrorDialog(owner, 
                                message, 
                                DialogType.ERROR.getDefaultMasthead());
    }
    
    public static DialogResponse showErrorDialog(final Stage owner,
                                                 final String message,
                                                 final String masthead) {
        return showErrorDialog(owner, 
                                message, 
                                masthead,
                                masthead);
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, 
                                                 final String message,
                                                 final String masthead, 
                                                 final String title) {
        return showErrorDialog(owner, 
                                message, 
                                masthead,
                                title,
                                DialogType.ERROR.getDefaultOptions());
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, 
                                                 final String message,
                                                 final String masthead, 
                                                 final String title,
                                                 final DialogOptions options) {
        return showSimpleContentDialog(owner, 
                title,
                masthead, 
                message, 
                DialogType.ERROR, 
                options);
    }
    
    public static DialogResponse showErrorDialog(final Stage owner, 
                                                 final String message,
                                                 final String masthead, 
                                                 final String title, 
                                                 final Throwable throwable) {
        DialogTemplate template = new DialogTemplate(owner, title, masthead, null);
        template.setErrorContent(message, throwable);
        return showDialog(template);
    }
    
    
    
    
    /***************************************************************************
     * User Input Dialogs
     **************************************************************************/
    
    public static String showInputDialog(final Stage owner, 
                                         final String message) {
        return showInputDialog(owner, message, "Masthead");
    }
    
    public static String showInputDialog(final Stage owner, 
                                         final String message,
                                         final String masthead) {
        return showInputDialog(owner, message, masthead, "Title");
    }
    
    public static String showInputDialog(final Stage owner, 
                                         final String message,
                                         final String masthead, 
                                         final String title) {
        return showInputDialog(owner, message, masthead, title, null);
    }
    
    public static String showInputDialog(final Stage owner, 
                                         final String message,
                                         final String masthead, 
                                         final String title,
                                         final String initialValue) {
        return showInputDialog(owner, message, masthead, title, initialValue, Collections.<String>emptyList());
    }
    
    public static <T> T showInputDialog(final Stage owner, 
                                        final String message,
                                        final String masthead, 
                                        final String title,
                                        final T initialValue, 
                                        final T... choices) {
        return showInputDialog(owner, message, masthead, title, initialValue, Arrays.asList(choices));
    }
    
    public static <T> T showInputDialog(final Stage owner, 
                                        final String message,
                                        final String masthead, 
                                        final String title,
                                        final T initialValue, 
                                        final List<T> choices) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, title, masthead, null);
        template.setInputContent(message, initialValue, choices);
        return showUserInputDialog(template);
    }
    
    
    
    /***************************************************************************
     * Private API
     **************************************************************************/
    private static <T> DialogResponse showSimpleContentDialog(final Stage owner,
                                                          final String title, 
                                                          final String masthead, 
                                                          final String message, 
                                                          final DialogType dialogType,
                                                          final DialogOptions options) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, title, masthead, options);
        template.setSimpleContent(message, dialogType);
        return showDialog(template);
    }
    
    private static <T> DialogResponse showDialog(DialogTemplate<T> template) {
        try {
            template.getDialog().centerOnScreen();
            template.show();
            return template.getResponse();
        } catch (Throwable e) {
            return CLOSED;
        }
    }
    
    private static <T> T showUserInputDialog(DialogTemplate<T> template) {
        template.getDialog().centerOnScreen();
        template.show();
        return template.getInputResponse();
    }
}
