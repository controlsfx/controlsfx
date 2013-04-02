package org.controlsfx.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.stage.Stage;

/**
 * A class containing a number of pre-built JavaFX modal dialogs.
 */
@Deprecated()
public class Dialogs {

    // /**
    // * TODO delete me - this is just for testing!!
    // */
    // public static void setMacOS(boolean b) {
    // DialogTemplate.setMacOS(b);
    // }
    //
    // public static void setWindows(boolean b) {
    // DialogTemplate.setWindows(b);
    // }

    // // NOT PUBLIC API
    // static enum Dialog.Type {
    // // TODO maybe introduce a MORE_DETAILS type, rather than use the ERROR
    // type?
    // ERROR(Dialog.Options.OK,"error.image") {
    // @Override public String getDefaultTitle() { return "Error"; }
    // @Override public String getDefaultMasthead() { return "Error"; }
    // },
    // INFORMATION(Dialog.Options.OK, "info.image") {
    // @Override public String getDefaultTitle() { return "Message"; }
    // @Override public String getDefaultMasthead() { return "Message"; }
    // },
    // WARNING(Dialog.Options.OK,"warning.image") {
    // @Override public String getDefaultTitle() { return "Warning"; }
    // @Override public String getDefaultMasthead() { return "Warning"; }
    // },
    // CONFIRMATION(Dialog.Options.YES_NO_CANCEL, "confirm.image") {
    // @Override public String getDefaultTitle() { return "Select an option"; }
    // @Override public String getDefaultMasthead() { return "Select an option";
    // }
    // },
    // INPUT(Dialog.Options.OK_CANCEL, "confirm.image") {
    // @Override public String getDefaultTitle() { return "Select an option"; }
    // @Override public String getDefaultMasthead() { return "Select an option";
    // }
    // };
    //
    // private final Dialog.Options defaultOptions;
    // private final String imageResource;
    // private Image image;
    //
    //
    // Dialog.Type(Dialog.Options defaultOptions, String imageResource) {
    // this.defaultOptions = defaultOptions;
    // this.imageResource = imageResource;
    // }
    //
    // public Image getImage() {
    // if (image == null) {
    // image = DialogResources.getImage(imageResource);
    // }
    // return image;
    // }
    //
    // public abstract String getDefaultMasthead();
    //
    // public abstract String getDefaultTitle();
    //
    // public Dialog.Options getDefaultOptions() {
    // return defaultOptions;
    // }
    // }
    //
    // /**
    // * An enumeration used to specify the response provided by the user when
    // * interacting with a dialog.
    // */
    // public static enum Dialog.Response {
    // YES,
    // NO,
    // CANCEL,
    // OK,
    // CLOSED
    // }
    //
    // /**
    // * An enumeration used to specify which buttons to show to the user in a
    // * Dialog.
    // */
    // public static enum Dialog.Options {
    // YES_NO,
    // YES_NO_CANCEL,
    // OK,
    // OK_CANCEL;
    // }

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
    public static Dialog.Response showConfirmDialog(final Stage owner,
            final String message) {
        return showConfirmDialog(owner, message,
                Dialog.Type.CONFIRMATION.getDefaultTitle());
    }

    public static Dialog.Response showConfirmDialog(final Stage owner,
            final String message, final String title) {
        return showConfirmDialog(owner, message, title, null);
    }

    public static Dialog.Response showConfirmDialog(final Stage owner,
            final String message, final String title, final String masthead) {
        return showConfirmDialog(owner, message, title, masthead,
                Dialog.Type.CONFIRMATION.getDefaultOptions());
    }

    public static Dialog.Response showConfirmDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final Dialog.Options options) {
        return showSimpleContentDialog(owner, title, masthead, message,
                Dialog.Type.CONFIRMATION, options);
    }

    /***************************************************************************
     * Information Dialogs
     **************************************************************************/

    public static void showInformationDialog(final Stage owner,
            final String message) {
        showInformationDialog(owner, message,
                Dialog.Type.INFORMATION.getDefaultTitle());
    }

    public static void showInformationDialog(final Stage owner,
            final String message, final String title) {
        showInformationDialog(owner, message, title, null);
    }

    /*
     * Info message string displayed in the masthead Info icon 48x48 displayed
     * in the masthead "OK" button at the bottom.
     * 
     * text and title strings are already translated strings.
     */
    public static void showInformationDialog(final Stage owner,
            final String message, final String title, final String masthead) {
        showSimpleContentDialog(owner, title, masthead, message,
                Dialog.Type.INFORMATION,
                Dialog.Type.INFORMATION.getDefaultOptions());
    }

    /***************************************************************************
     * Warning Dialogs
     **************************************************************************/

    /**
     * showWarningDialog - displays warning icon instead of "Java" logo icon in
     * the upper right corner of masthead. Has masthead and message that is
     * displayed in the middle part of the dialog. No bullet is displayed.
     * 
     * 
     * @param owner
     *            - Component to parent the dialog to
     * @param appInfo
     *            - AppInfo object
     * @param masthead
     *            - masthead in the top part of the dialog
     * @param message
     *            - question to display in the middle part
     * @param title
     *            - dialog title string from resource bundle
     * 
     */
    public static Dialog.Response showWarningDialog(final Stage owner,
            final String message) {
        return showWarningDialog(owner, message,
                Dialog.Type.WARNING.getDefaultTitle());
    }

    public static Dialog.Response showWarningDialog(final Stage owner,
            final String message, final String title) {
        return showWarningDialog(owner, message, title, null);
    }

    public static Dialog.Response showWarningDialog(final Stage owner,
            final String message, final String title, final String masthead) {
        return showWarningDialog(owner, message, title, masthead,
                Dialog.Type.WARNING.getDefaultOptions());
    }

    public static Dialog.Response showWarningDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final Dialog.Options options) {
        return showSimpleContentDialog(owner, title, masthead, message,
                Dialog.Type.WARNING, options);
    }

    /***************************************************************************
     * Error Dialogs
     **************************************************************************/

    public static Dialog.Response showErrorDialog(final Stage owner,
            final String message) {
        return showErrorDialog(owner, message,
                Dialog.Type.ERROR.getDefaultTitle());
    }

    public static Dialog.Response showErrorDialog(final Stage owner,
            final String message, final String title) {
        return showErrorDialog(owner, message, title, null);
    }

    public static Dialog.Response showErrorDialog(final Stage owner,
            final String message, final String title, final String masthead) {
        return showErrorDialog(owner, message, title, masthead,
                Dialog.Type.ERROR.getDefaultOptions());
    }

    public static Dialog.Response showErrorDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final Dialog.Options options) {
        return showSimpleContentDialog(owner, title, masthead, message,
                Dialog.Type.ERROR, options);
    }

    /***************************************************************************
     * 'More Details' Dialogs FIXME: Need better name
     **************************************************************************/

    public static Dialog.Response showMoreDetailsDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String moreDetails = sw.toString();

        return showMoreDetailsDialog(owner, message, title, masthead,
                moreDetails, false);
    }

    public static Dialog.Response showMoreDetailsDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final Throwable throwable, final boolean openInNewWindow) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String moreDetails = sw.toString();

        return showMoreDetailsDialog(owner, message, title, masthead,
                moreDetails, openInNewWindow);
    }

    public static Dialog.Response showMoreDetailsDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final String moreDetails) {
        return showMoreDetailsDialog(owner, message, title, masthead,
                moreDetails, false);
    }

    public static Dialog.Response showMoreDetailsDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final String moreDetails, final boolean openInNewWindow) {
        DialogTemplate<Void> template = new DialogTemplate<>(owner, title,
                masthead, null);
        template.setMoreDetailsContent(message, moreDetails, openInNewWindow);
        return showDialog(template);
    }

    /***************************************************************************
     * User Input Dialogs
     **************************************************************************/

    public static String showInputDialog(final Stage owner, final String message) {
        return showInputDialog(owner, message,
                Dialog.Type.ERROR.getDefaultTitle());
    }

    public static String showInputDialog(final Stage owner,
            final String message, final String title) {
        return showInputDialog(owner, message, title, null);
    }

    public static String showInputDialog(final Stage owner,
            final String message, final String title, final String masthead) {
        return showInputDialog(owner, message, title, masthead, null);
    }

    public static String showInputDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final String initialValue) {
        return showInputDialog(owner, message, title, masthead, initialValue,
                Collections.<String> emptyList());
    }

    public static <T> T showInputDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final T initialValue, final T... choices) {
        return showInputDialog(owner, message, title, masthead, initialValue,
                Arrays.asList(choices));
    }

    public static <T> T showInputDialog(final Stage owner,
            final String message, final String title, final String masthead,
            final T initialValue, final List<T> choices) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, title,
                masthead, null);
        template.setInputContent(message, initialValue, choices);
        return showUserInputDialog(template);
    }

    /***************************************************************************
     * Private API
     **************************************************************************/
    private static <T> Dialog.Response showSimpleContentDialog(
            final Stage owner, final String title, final String masthead,
            final String message, final Dialog.Type dialogType,
            final Dialog.Options options) {
        DialogTemplate<T> template = new DialogTemplate<T>(owner, title,
                masthead, options);
        template.setSimpleContent(message, dialogType);
        return showDialog(template);
    }

    private static <T> Dialog.Response showDialog(DialogTemplate<T> template) {
        try {
            template.getDialog().centerOnScreen();
            template.show();
            return template.getResponse();
        } catch (Throwable e) {
            return Dialog.Response.CLOSED;
        }
    }

    private static <T> T showUserInputDialog(DialogTemplate<T> template) {
        template.getDialog().centerOnScreen();
        template.show();
        return template.getInputResponse();
    }
}
