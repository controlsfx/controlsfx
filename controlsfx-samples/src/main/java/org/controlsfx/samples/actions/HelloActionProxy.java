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
import org.controlsfx.control.action.*;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.controlsfx.samples.Utils;

import java.util.Arrays;
import java.util.Collection;

import static org.controlsfx.control.action.ActionMap.action;
import static org.controlsfx.control.action.ActionMap.actions;
import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import static org.controlsfx.control.action.ActionUtils.ACTION_SPAN;

public class HelloActionProxy extends ControlsFXSample {
    
    private static final String imagePath = "/org/controlsfx/samples/security-low.png";
    private static final ImageView image = new ImageView(new Image(imagePath));
    
    private Collection<? extends Action> actions;
    
    public HelloActionProxy() {
		ActionMap.register(this);
		actions = Arrays.asList(
	        new ActionGroup("Group 1", image, actions("action11","action12") ),
			new ActionGroup("Group 2", image, actions("action21","---","action22", "action221","action222","action23") ),
            ACTION_SPAN,
            ACTION_SEPARATOR,
			action("action3"),
			new ActionGroup("Group 4",  image, actions("action41","action42"))
		);
	}
    
    @ActionProxy(text="Action 1.1", graphic=imagePath, accelerator="ctrl+shift+T")
    private void action11() {
    	 System.out.println( "Action 1.1 is executed");
    }

    @ActionCheck
    @ActionProxy(text="Action 1.2", graphic="http://icons.iconarchive.com/icons/custom-icon-design/mini-3/16/teacher-male-icon.png") 
    private void action12() {
    	 System.out.println( "Action 1.2 is executed");
    }
    
    @ActionProxy(text="Action 2.1", graphic=imagePath, factory="org.controlsfx.samples.actions.HelloCustomActionFactory")
    private void action21() {
    	 System.out.println( "Action 2.1 is executed (and used a custom action factory)");
    }
    
    @ActionProxy(text="Action 2.2", graphic=imagePath)
    private void action22( ActionEvent evt ) {
    	 System.out.println( "Action 2.2 is executed (and received an ActionEvent)");
    }
    
    @ActionProxy(text="Action 2.2.1", graphic=imagePath)
    private void action221( ActionEvent evt, Action action ) {
    	 System.out.println( "Action 2.2.1 is executed (and received both an ActionEvent and an Action)");
    }
    
    @ActionProxy(text="Action 2.2.2", graphic=imagePath)
    private void action222() {
    	 System.out.println( "Action 2.2.2 is executed");
    }   
    
    @ActionProxy(text="Action 2.3", graphic=imagePath)
    private void action23() {
    	 System.out.println( "Action 2.3 is executed");
    }

    @ActionCheck
    @ActionProxy(text="Action 3", graphic="font>FontAwesome|STAR")
    private void action3() {
    	 System.out.println( "Action 3 is executed");
    }    
    
    @ActionProxy(text="Action 4.1", graphic=imagePath)
    private void action41() {
    	 System.out.println( "Action 4.1 is executed");
    }
    
    @ActionProxy(text="Action 4.2", graphic=imagePath)
    private void action42() {
    	 System.out.println( "Action 4.2 is executed");
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
        return "Action Proxy";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/action/ActionProxy.html";
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