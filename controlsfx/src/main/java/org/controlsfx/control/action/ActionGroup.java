/**
 * Copyright (c) 2013, 2015, ControlsFX
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
package org.controlsfx.control.action;

import java.util.Arrays;
import java.util.Collection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;

/**
 * An ActionGroup (unsurprisingly) groups together zero or more {@link Action} 
 * instances, allowing for more complex controls like {@link ToolBar}, 
 * {@link MenuBar} and {@link ContextMenu} to be automatically generated from
 * the collection of actions inside the ActionGroup. For your convenience,
 * there are a number of utility methods that do precisely this in the 
 * {@link ActionUtils} class.
 * 
 * <h3>Code Examples</h3>
 * <p>Consider the following code example (note that DummyAction is a fake class
 * that extends from (and implements) {@link Action}):
 * 
 * <pre>
 * {@code
 * // Firstly, create a list of Actions
 * Collection<? extends Action> actions = Arrays.asList(
 *     new ActionGroup("Group 1",  new DummyAction("Action 1.1"), 
 *                                 new DummyAction("Action 2.1") ),
 *     new ActionGroup("Group 2",  new DummyAction("Action 2.1"), 
 *                                 new ActionGroup("Action 2.2", new DummyAction("Action 2.2.1"), 
 *                                                               new DummyAction("Action 2.2.2")),
 *                                 new DummyAction("Action 2.3") ),
 *     new ActionGroup("Group 3",  new DummyAction("Action 3.1"), 
 *                                 new DummyAction("Action 3.2") )
 *   );
 *   
 *   // Use the ActionUtils class to create UI controls from these actions, e.g:
 *   MenuBar menuBar = ActionUtils.createMenuBar(actions);
 *   
 *   ToolBar toolBar = ActionUtils.createToolBar(actions);
 *   
 *   Label context = new Label("Right-click to see the context menu");
 *   context.setContextMenu(ActionUtils.createContextMenu(actions));  
 * }</pre>
 * 
 * <p>The end result of running the code above is shown in the screenshots below
 * (hopefully it goes without saying that within the 'Group 1', 'Group 2' and 
 * 'Group 3' options are the 'Action 1.1', etc actions that have been specified
 * in the code above):
 *
 * <table border="0" summary="ActionGroup Screenshots">
 *   <tr>
 *     <td width="75" valign="center"><strong>MenuBar:</strong></td>
 *     <td><img src="actionGroup-menubar.png" alt="Screenshot of ActionGroup in a MenuBar"></td>
 *   </tr>
 *   <tr>
 *     <td width="75" valign="center"><strong>ToolBar:</strong></td>
 *     <td><img src="actionGroup-toolbar.png" alt="Screenshot of ActionGroup in a ToolBar"></td>
 *   </tr>
 *   <tr>
 *   <td width="75" valign="top"><strong>ContextMenu:</strong></td>
 *     <td><img src="actionGroup-contextmenu.png" alt="Screenshot of ActionGroup in a ContextMenu"></td>
 *   </tr>
 * </table>
 * 
 * @see Action
 * @see ActionUtils
 */
public class ActionGroup extends Action {
    
    /**
     * Creates an ActionGroup with the given text as the name of the {@link Action}, 
     * and zero or more Actions as members of this ActionGroup. Note that it is
     * legitimate to pass in zero Actions to this constructor, and to later 
     * set the actions directly into the {@link #getActions() actions} list.
     * 
     * @param text The {@link Action#textProperty() text} of this {@link Action}.
     * @param actions Zero or more actions to insert into this ActionGroup.
     */
    public ActionGroup(String text, Action... actions) {
    	 this(text, Arrays.asList(actions));
    }
    
    /**
     * Creates an ActionGroup with the given text as the name of the {@link Action}, 
     * and collection of Actions as members of this ActionGroup. 
     * 
     * @param text The {@link Action#textProperty() text} of this {@link Action}.
     * @param actions Collection of actions to insert into this ActionGroup.
     */
    public ActionGroup(String text, Collection<Action> actions) {
        super(text);
        getActions().addAll(actions);
    }
    
    /**
     * Creates an ActionGroup with the given text as the name of the {@link Action}, 
     * and zero or more Actions as members of this ActionGroup. Note that it is
     * legitimate to pass in zero Actions to this constructor, and to later 
     * set the actions directly into the {@link #getActions() actions} list.
     * 
     * @param text The {@link Action#textProperty() text} of this {@link Action}.
     * @param icon The {@link Action#graphicProperty() image} of this {@link Action}.
     * @param actions Zero or more actions to insert into this ActionGroup.
     */
    public ActionGroup(String text, Node icon, Action... actions) {
    	 this( text, icon, Arrays.asList(actions));
    }
    
    /**
     * Creates an ActionGroup with the given text as the name of the {@link Action},
     * and collection of Actions as members of this ActionGroup. .
     * 
     * @param text The {@link Action#textProperty() text} of this {@link Action}.
     * @param icon The {@link Action#graphicProperty() image} of this {@link Action}.
     * @param actions Collection of actions to insert into this ActionGroup.
     */
    public ActionGroup(String text, Node icon, Collection<Action> actions) {
        super(text);
        setGraphic(icon);
        getActions().addAll(actions);
    }

    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    /**
     * The list of {@link Action} instances that exist within this ActionGroup.
     * This list may be modified, as shown in the class documentation.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    @Override public String toString() {
        return getText();
    }
    
}
