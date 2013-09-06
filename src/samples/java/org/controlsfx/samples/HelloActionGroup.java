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

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;

import java.util.Arrays;
import java.util.Collection;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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
    
    private static final Image image = new Image("/org/controlsfx/samples/security-low.png");
    
    private Collection<? extends Action> actions = Arrays.asList(
        new ActionGroup("Group 1",  new DummyAction("Action 1.1", image), 
                                    new DummyAction("Action 1.2") ),
        new ActionGroup("Group 2",  new DummyAction("Action 2.1"), 
                                    ACTION_SEPARATOR,
                                    new ActionGroup("Action 2.2", new DummyAction("Action 2.2.1"), 
                                                                  new DummyAction("Action 2.2.2")),
                                    new DummyAction("Action 2.3") ),
        ACTION_SEPARATOR,                                    
        new DummyAction("Action 3", image),
        new ActionGroup("Group 4",  new DummyAction("Action 4.1", image), 
                                    new DummyAction("Action 4.2"))
    );
    
    static class DummyAction extends AbstractAction {

        public DummyAction(String name, Image image) {
            super(name);
            setGraphic(new ImageView(image));
        }
        
        public DummyAction( String name ) {
            super(name);
        }

        @Override public void execute(javafx.event.ActionEvent ae) {
            System.out.println( String.format("Action '%s' is executed", getText()));
        }
        
        @Override public String toString() {
            return getText();
        }

        
    }
    
    private ObservableList<Action> flatten( Collection<? extends Action> actions, ObservableList<Action> dest ) {
        
        for (Action a : actions) {
           if ( a == null || a == ActionUtils.ACTION_SEPARATOR ) continue;
           dest.add(a); 
           if ( a instanceof ActionGroup ) {
               flatten( ((ActionGroup)a).getActions(), dest);
           }
        }
        
        return dest;
    }
    
    
    @Override public String getSampleName() {
        return "Action Group";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/action/ActionGroup.html";
    }
    
    @Override public boolean includeInSamples() {
        return true;
    }
    
    @Override public Node getPanel(final Stage stage) {
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);
        
        Label overview = new Label("MenuBar, TaskBar and ContenxtMenu presented here are effortlesly built out of the same action tree. " +
        		"Action properties can be dynamically changed, triggering changes in all related controls");
        overview.setWrapText(true);
        root.getChildren().add(overview);
        
        HBox hbox = new HBox(10);
        final ComboBox<Action> cbActions = new ComboBox<Action>(  flatten( actions, FXCollections.<Action>observableArrayList()));
        cbActions.getSelectionModel().select(0);
        
        hbox.getChildren().add(new Label("Dynamically enable/disable action: "));
        hbox.getChildren().add(cbActions);
        
        Action toggleAction = new AbstractAction("Enable/Disable") {

            @Override public void execute(ActionEvent ae) {
               Action action = cbActions.getSelectionModel().getSelectedItem();
               if ( action != null ) {
                   BooleanProperty p = action.disabledProperty();
                   p.set(!p.get());
               }
            }
        };
        
        hbox.getChildren().add(ActionUtils.createButton(toggleAction));
        
        root.getChildren().add(hbox);
        root.getChildren().add( new Separator());

        root.getChildren().add(new Label("MenuBar"));
        MenuBar menuBar = ActionUtils.createMenuBar(actions);
        root.getChildren().add(menuBar);

        root.getChildren().add(new Label("ToolBar"));
        ToolBar toolBar = ActionUtils.createToolBar(actions);
        root.getChildren().add(toolBar);

        
        root.getChildren().add(new Label("ContextMenu"));
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