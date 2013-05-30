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

import java.util.Arrays;
import java.util.Collection;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.Sample;
import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;

public class HelloActionGroup extends Application implements Sample {
    
    private Collection<? extends Action> actions = Arrays.asList(
        new ActionGroup("Group 1",  new DummyAction("Action 1.1"), 
                                    new DummyAction("Action 2.1") ),
        new ActionGroup("Group 2",  new DummyAction("Action 2.1"), 
                                    new ActionGroup("Action 2.2", new DummyAction("Action 2.2.1"), 
                                                                  new DummyAction("Action 2.2.2")),
                                    new DummyAction("Action 2.3") ),
        new ActionGroup("Group 3",  new DummyAction("Action 3.1"), 
                                    new DummyAction("Action 3.2") )
    );
    
    static class DummyAction extends AbstractAction {
        
        public DummyAction( String name ) {
            super(name);
        }

        @Override public void execute(javafx.event.ActionEvent ae) {
            System.out.println( String.format("Action '%s' is executed", getText()));
        }
        
    }
    
    
    @Override public String getSampleName() {
        return "Action Factory";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/action/ActionGroup.html";
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);
        
        MenuBar menuBar = ActionUtils.createMenuBar(actions);
        root.getChildren().add(menuBar);

        ToolBar toolBar = ActionUtils.createToolBar(actions);
        root.getChildren().add(toolBar);

        
        Label context = new Label("Right-click to see the context menu");
        context.setContextMenu( ActionUtils.createContextMenu(actions));  
        root.getChildren().add(context);
        VBox.setVgrow(context, Priority.ALWAYS);
        
        return root;
    }
    
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("Action Group Demo");
        
        Scene scene = new Scene((Parent)getPanel(stage), 1300, 300);
        scene.setFill(Color.WHITE);
        
        stage.setScene(scene);
        stage.show();
    }
     
    public static void main(String[] args) {
        launch(args);
    }
    
}