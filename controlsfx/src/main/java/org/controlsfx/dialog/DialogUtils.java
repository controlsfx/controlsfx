/**
 * Copyright (c) 2014 ControlsFX
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

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar.ButtonData;

// package scope
class DialogUtils {

    static void forcefullyHideDialog(javafx.scene.control.Dialog<?> dialog) {
        // for the dialog to be able to hide, we need a cancel button,
        // so lets put one in now and then immediately call hide, and then
        // remove the button again (if necessary).
        DialogPane dialogPane = dialog.getDialogPane();
        if (containsCancelButton(dialog)) {
            dialog.hide();
            return;
        }
        
        dialogPane.getButtonTypes().add(ButtonType.CANCEL); 
        dialog.hide();
        dialogPane.getButtonTypes().remove(ButtonType.CANCEL);
    }
    
    static boolean containsCancelButton(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        for (ButtonType type : dialogPane.getButtonTypes()) {
            if (type.getButtonData() == ButtonData.CANCEL_CLOSE) {
                return true;
            }
        }
        return false;
    }
}
