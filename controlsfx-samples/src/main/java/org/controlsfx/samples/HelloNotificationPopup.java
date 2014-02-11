/**
 * Copyright (c) 2014, ControlsFX
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
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.NotificationPopup;
import org.controlsfx.control.NotificationPopup.Notification;
import org.controlsfx.control.NotificationPopup.Notifications;


public class HelloNotificationPopup extends ControlsFXSample {
    
    private Stage stage;
    
    private NotificationPopup notifier;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "NotificationPopup";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationPopup.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        this.stage = stage;
        this.notifier = new NotificationPopup();
        
        Button topLeftBtn = new Button("Top-left\nnotification");
        topLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_LEFT);
            }
        });
        AnchorPane.setTopAnchor(topLeftBtn, 0.0);
        AnchorPane.setLeftAnchor(topLeftBtn, 0.0);
        
        Button topRightBtn = new Button("Top-right\nnotification");
        topRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_RIGHT);
            }
        });
        AnchorPane.setTopAnchor(topRightBtn, 0.0);
        AnchorPane.setRightAnchor(topRightBtn, 0.0);
        
        Button bottomLeftBtn = new Button("Bottom-left\nNotification");
        bottomLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_LEFT);
            }
        });
        AnchorPane.setBottomAnchor(bottomLeftBtn, 0.0);
        AnchorPane.setLeftAnchor(bottomLeftBtn, 0.0);
        
        Button bottomRightBtn = new Button("Bottom-right\nNotification");
        bottomRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_RIGHT);
            }
        });
        AnchorPane.setBottomAnchor(bottomRightBtn, 0.0);
        AnchorPane.setRightAnchor(bottomRightBtn, 0.0);
        
        AnchorPane pane = new AnchorPane(topLeftBtn, topRightBtn, bottomLeftBtn, bottomRightBtn);
        
        return pane;
    }
    
    @Override public String getSampleDescription() {
        return "Unlike the NotificationPane, the NotificationPopup is designed to"
                + "show popup warnings outside your application.";
    }
    
    private void notification(Pos pos) {
        Notification n = Notifications.create().text("Hello World!").position(pos).build();
        notifier.show(stage, n);
    }
}
