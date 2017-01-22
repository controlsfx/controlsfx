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
package org.controlsfx.samples.actions;

import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionCheck;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.samples.Utils;

import java.util.Arrays;
import java.util.Collection;

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import static org.controlsfx.control.action.ActionUtils.ACTION_SPAN;

public class HelloActionGroup extends ControlsFXSample {
    
    private static final ImageView image = new ImageView( new Image("/org/controlsfx/samples/security-low.png"));
    
    private Collection<? extends Action> actions = Arrays.asList(
        new ActionGroup("Group 1", image, new DummyAction("Action 1.1", image), 
                                          new CheckDummyAction("Action 1.2") ),
        new ActionGroup("Group 2", image, new DummyAction("Action 2.1"), 
                                          ACTION_SEPARATOR,
                                          new ActionGroup("Action 2.2", new DummyAction("Action 2.2.1"), 
                                                                        new CheckDummyAction("Action 2.2.2")),
                                          new DummyAction("Action 2.3") ),
        ACTION_SPAN,
        ACTION_SEPARATOR,
        new CheckDummyAction("Action 3", image),
        new ActionGroup("Group 4",  image, new DummyAction("Action 4.1", image), 
                                           new CheckDummyAction("Action 4.2"))
    );
    
    private static class DummyAction extends Action {
        public DummyAction(String name, Node image) {
            super(name);
            setGraphic(image);
            setEventHandler(ae -> String.format("Action '%s' is executed", getText()) );
        }
        
        public DummyAction( String name ) {
            super(name);
        }

        @Override public String toString() {
            return getText();
        }
    }

    @ActionCheck
    private static class CheckDummyAction extends Action {
        public CheckDummyAction(String name, Node image) {
            super(name);
            setGraphic(image);
            setEventHandler(ae -> String.format("Action '%s' is executed", getText()) );
        }

        public CheckDummyAction( String name ) {
            super(name);
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
    
    @Override public String getSampleDescription() {
        return "MenuBar, ToolBar and ContextMenu presented here are effortlessly built out of the same action tree. " +
                "Action properties can be dynamically changed, triggering changes in all related controls";
    }
    
    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));

        int row = 0;

        // Dynamically enable/disable action
        Label lblAddCrumb = new Label("Dynamically enable/disable action: ");
        lblAddCrumb.getStyleClass().add("property");
        grid.add(lblAddCrumb, 0, row);
        final ComboBox<Action> cbActions = new ComboBox<>(flatten( actions, FXCollections.<Action>observableArrayList()));
        cbActions.getSelectionModel().select(0);
        grid.add(cbActions, 1, row);
        Action toggleAction = new Action("Enable/Disable") {
            { setEventHandler(this::handleAction); }

            private void handleAction(ActionEvent ae) {
               Action action = cbActions.getSelectionModel().getSelectedItem();
               if ( action != null ) {
                   BooleanProperty p = action.disabledProperty();
                   p.set(!p.get());
               }
            }
        };
        grid.add(ActionUtils.createButton(toggleAction), 2, row++);
        
        return grid;
    }
    
    @Override public Node getPanel(final Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setMaxHeight(Double.MAX_VALUE);
        
        Insets topMargin = new Insets(7, 7, 0, 7);
        Insets margin = new Insets(0, 7, 7, 7);
        
        addWithMargin(root, new Label("MenuBar:"), topMargin ).setStyle("-fx-font-weight: bold;");
        addWithMargin(root, ActionUtils.createMenuBar(actions), margin);

        addWithMargin(root,new Label("ToolBar (with text on controls):"), topMargin).setStyle("-fx-font-weight: bold;");
        addWithMargin(root, ActionUtils.createToolBar(actions, ActionTextBehavior.SHOW), margin);

        addWithMargin(root,new Label("ToolBar (no text on controls):"), topMargin).setStyle("-fx-font-weight: bold;");
        addWithMargin(root, ActionUtils.createToolBar(actions, ActionTextBehavior.HIDE), margin);
        
        addWithMargin(root, new Label("ContextMenu:"), topMargin).setStyle("-fx-font-weight: bold;");
        Label context = new Label("Right-click to see the context menu");
        addWithMargin(root,context, margin);
        context.setContextMenu(ActionUtils.createContextMenu(actions)); 
        context.setStyle("-fx-background-color: #E0E0E0 ;-fx-border-color: black;-fx-border-style: dotted");
        context.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(context, Priority.ALWAYS);
        VBox.setVgrow(root, Priority.ALWAYS);
        
        return root;
    }
    
    private Control addWithMargin( VBox parent, Control control, Insets insets) {
    	parent.getChildren().add(control);
    	VBox.setMargin(control, insets);
    	return control;
    }
     
    public static void main(String[] args) {
        launch(args);
    }
}