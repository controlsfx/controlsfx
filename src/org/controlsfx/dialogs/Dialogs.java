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
