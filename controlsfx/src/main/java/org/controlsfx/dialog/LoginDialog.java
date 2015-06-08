/**
 * Copyright (c) 2014, 2015 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.getString;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.Pair;

import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

public class LoginDialog extends Dialog<Pair<String,String>> {
    
    private final ButtonType loginButtonType;
    private final CustomTextField txUserName;
    private final CustomPasswordField txPassword;

    @SuppressWarnings("deprecation")
    public LoginDialog(final Pair<String,String> initialUserInfo, final Callback<Pair<String,String>, Void> authenticator) {
        final DialogPane dialogPane = getDialogPane();
        
        setTitle(getString("login.dlg.title")); //$NON-NLS-1$
        dialogPane.setHeaderText(getString("login.dlg.header")); //$NON-NLS-1$
        dialogPane.getStyleClass().add("login-dialog"); //$NON-NLS-1$
        dialogPane.getStylesheets().add(LoginDialog.class.getResource("dialogs.css").toExternalForm()); //$NON-NLS-1$
        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL);
        
        
        
        
        
        txUserName = (CustomTextField) TextFields.createClearableTextField();
        
        txUserName.setLeft(new ImageView(LoginDialog.class.getResource("/org/controlsfx/dialog/user.png").toExternalForm())); //$NON-NLS-1$
        
        txPassword = (CustomPasswordField) TextFields.createClearablePasswordField();
        txPassword.setLeft(new ImageView(LoginDialog.class.getResource("/org/controlsfx/dialog/lock.png").toExternalForm())); //$NON-NLS-1$
        
        Label lbMessage= new Label("");  //$NON-NLS-1$
        lbMessage.getStyleClass().addAll("message-banner"); //$NON-NLS-1$
        lbMessage.setVisible(false);
        lbMessage.setManaged(false);
        
        final VBox content = new VBox(10);
        content.getChildren().add(lbMessage);
        content.getChildren().add(txUserName);
        content.getChildren().add(txPassword);
        
        dialogPane.setContent(content);
        
        loginButtonType = new javafx.scene.control.ButtonType(getString("login.dlg.login.button"), ButtonData.OK_DONE); //$NON-NLS-1$
        dialogPane.getButtonTypes().addAll(loginButtonType);
        Button loginButton = (Button) dialogPane.lookupButton(loginButtonType);
        loginButton.setOnAction(actionEvent -> {
            try {
                if (authenticator != null ) {
                    authenticator.call(new Pair<>(txUserName.getText(), txPassword.getText()));
                }
                lbMessage.setVisible(false);
                lbMessage.setManaged(false);
                hide();
//                dlg.setResult(this);
            } catch( Throwable ex ) {
                lbMessage.setVisible(true);
                lbMessage.setManaged(true);
                lbMessage.setText(ex.getMessage());
//                sizeToScene();
//                dlg.shake();
                ex.printStackTrace();
            }
        });
        
//        final Dialog dlg = buildDialog(Type.LOGIN);
//        dlg.setContent(content);
        
//        dlg.setResizable(false);
//        dlg.setIconifiable(false);
//        if ( dlg.getGraphic() == null ) { 
//            dlg.setGraphic( new ImageView( DialogResources.getImage("login.icon")));
//        }
//        dlg.getActions().setAll(actionLogin, ACTION_CANCEL);
        String userNameCation = getString("login.dlg.user.caption"); //$NON-NLS-1$
        String passwordCaption = getString("login.dlg.pswd.caption"); //$NON-NLS-1$
        txUserName.setPromptText(userNameCation);
        txUserName.setText(initialUserInfo == null ? "" : initialUserInfo.getKey()); //$NON-NLS-1$
        txPassword.setPromptText(passwordCaption);
        txPassword.setText(new String(initialUserInfo == null ? "" : initialUserInfo.getValue())); //$NON-NLS-1$

        ValidationSupport validationSupport = new ValidationSupport();
        Platform.runLater( () -> {
            String requiredFormat = "'%s' is required"; //$NON-NLS-1$
            validationSupport.registerValidator(txUserName, Validator.createEmptyValidator( String.format( requiredFormat, userNameCation )));
            validationSupport.registerValidator(txPassword, Validator.createEmptyValidator(String.format( requiredFormat, passwordCaption )));
//            loginButton.disabledProperty().bind(validationSupport.invalidProperty());
            txUserName.requestFocus();
        } );
        
        
        setResultConverter(dialogButton -> dialogButton == loginButtonType ? 
                new Pair<>(txUserName.getText(), txPassword.getText()) : null);

//        return Optional.ofNullable( 
//                dlg.show() == actionLogin? 
//                        new Pair<String,String>(txUserName.getText(), txPassword.getText()): 
//                        null);
    }
    

    
    /**************************************************************************
     * 
     * Support classes
     * 
     **************************************************************************/

}
