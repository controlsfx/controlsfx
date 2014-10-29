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
package org.controlsfx.samples;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;

public class HelloNotificationPane extends ControlsFXSample {
    
    private NotificationPane notificationPane;
    private CheckBox cbUseDarkTheme;
    private CheckBox cbHideCloseBtn;
    private TextField textField;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Notification Pane";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationPane.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/notificationpane.css";
    }
    
    @Override public Node getPanel(Stage stage) {
        notificationPane = new NotificationPane();
        
        String imagePath = HelloNotificationPane.class.getResource("notification-pane-warning.png").toExternalForm();
        ImageView image = new ImageView(imagePath);
        notificationPane.setGraphic(image);
        
        notificationPane.getActions().addAll(new Action("Sync", ae -> {
                // do sync
                
                // then hide...
                notificationPane.hide();
        }));
        
        Button showBtn = new Button("Show / Hide");
        showBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                if (notificationPane.isShowing()) {
                    notificationPane.hide();
                } else {
                    notificationPane.show();
                }
            }
        });
        
        CheckBox cbSlideFromTop = new CheckBox("Slide from top");
        cbSlideFromTop.setSelected(true);
        notificationPane.showFromTopProperty().bind(cbSlideFromTop.selectedProperty());
        
        cbUseDarkTheme = new CheckBox("Use dark theme");
        cbUseDarkTheme.setSelected(false);
        cbUseDarkTheme.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                updateBar();
            }
        });
        
        cbHideCloseBtn = new CheckBox("Hide close button");
        cbHideCloseBtn.setSelected(false);
        cbHideCloseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                notificationPane.setCloseButtonVisible(!cbHideCloseBtn.isSelected());
            }
        });
        
        textField = new TextField();
        textField.setPromptText("Type text to display and press Enter");
        textField.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                notificationPane.show(textField.getText());
            }
        });
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(50, 0, 0, 10));
        root.getChildren().addAll(showBtn, cbSlideFromTop, cbUseDarkTheme, cbHideCloseBtn, textField);
        
        notificationPane.setContent(root);
        updateBar();
        
        return notificationPane;
    }
    
    private void updateBar() {
        boolean useDarkTheme = cbUseDarkTheme.isSelected();
        
        if (useDarkTheme) {
            notificationPane.setText("Hello World! Using the dark theme");
            notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        } else {
            notificationPane.setText("Hello World! Using the light theme");
            notificationPane.getStyleClass().remove(NotificationPane.STYLE_CLASS_DARK);
        }
    }
}
