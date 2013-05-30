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
package org.controlsfx.samples;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.ActionUtils;

public class HelloAction extends Application implements Sample {
    
    @Override public String getSampleName() {
        return "Actions";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/action/Action.html";
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);

        AbstractAction action = new AbstractAction("Text") {
            @Override public void execute(ActionEvent ae) {
                System.out.println(ae);
            }
        };
        action.setLongText("Tooltip text");
        Button btn = ActionUtils.createButton(action);

        root.getChildren().add(btn);
        
        return root;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("Actions Demo");
        
        Scene scene = new Scene((Parent)getPanel(stage), 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    public static void main(String[] args) {
        launch(args);
    }
    
}