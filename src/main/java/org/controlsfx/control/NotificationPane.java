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
package org.controlsfx.control;

import impl.org.controlsfx.skin.NotificationPaneSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.web.WebView;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.control.action.Action;

/**
 * The NotificationPane control is a container control that, when prompted by
 * the {@link #show()} method, will show a non-modal message to the user. The
 * notification appears as a bar that will slide in to their application window,
 * either from the top or the bottom of the NotificationPane (based on 
 * {@link #showFromTopProperty()}) wherever that may be in the scenegraph.
 * 
 * <h3>Alternative Styling</h3>
 * <p>As is visible in the screenshots further down this documentation, 
 * there are two different styles supported by the NotificationPane control.
 * Firstly, there is the default style based on the JavaFX Modena look. The 
 * alternative style is what is currently referred to as the 'dark' look. To 
 * enable this functionality, simply do the following:
 * 
 * <pre>
 * {@code
 * NotificationPane notificationPane = new NotificationPane();   
 * notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
 * }</pre>
 * 
 * <h3>Screenshots</h3>
 * <p>To better explain NotificationPane, here is a table showing both the
 * default and dark look for the NotificationPane on a Windows machine (although
 * that shouldn't impact the visuals a great deal). Also, to show the difference
 * between top and bottom placement, these two modes are also captured in the
 * screenshots below:
 * 
 * <br>
 * <center>
 * <table style="border: 1px solid gray; max-width:750px">
 *   <tr>
 *     <th width="200"><center><h3>Setting</h3></center></th>
 *     <th width="520"><center><h3>Screenshot</h3></center></th>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Light theme from top:</strong></td>
 *     <td><center><img src="notication-pane-light-top.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Light theme from bottom:</strong></td>
 *     <td><center><img src="notication-pane-light-bottom.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Dark theme from top:</strong></td>
 *     <td><center><img src="notication-pane-dark-top.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Dark theme from bottom:</strong></td>
 *     <td><center><img src="notication-pane-dark-bottom.png"></center></td>
 *   </tr>
 * </table>
 * </center>
 * 
 * <h3>Code Examples</h3>
 * 
 * <p>NotificationPane is a conceptually very simple control - you simply create
 * your user interface as you normally would, and then wrap it inside the 
 * NotificationPane. You can then show a notification bar on top of your content
 * simply by calling {@link #show()} on the notification bar. Here is an example: 
 * 
 * <pre>
 * {@code
 * // Create a WebView
 * WebView webView = new WebView();
 * 
 * // Wrap it inside a NotificationPane
 * NotificationPane notificationPane = new NotificationPane(webView);
 * 
 * // and put the NotificationPane inside a Tab
 * Tab tab1 = new Tab("Tab 1");
 * tab1.setContent(notificationPane);
 * 
 * // and the Tab inside a TabPane. We just have one tab here, but of course 
 * // you can have more!
 * TabPane tabPane = new TabPane();
 * tabPane.getTabs().addAll(tab1);
 * }</pre>
 * 
 * <p>Now that the notification pane is installed inside the tab, at some point
 * later in you application lifecycle, you can do something like the following
 * to have the notification bar slide into view:
 * 
 * <pre>
 * {@code 
 * notificationPane.setText("Do you want to save your password?");
 * notificationPane.getActions().add(new AbstractAction("Save Password") {
 *     public void execute(ActionEvent ae) {
 *         // do save...
 *           
 *         // then hide...
 *         notificationPane.hide();
 *     }
 * }}</pre>
 * 
 * @see Action
 * @see AbstractAction
 */
public class NotificationPane extends Control {
    
    public static final String STYLE_CLASS_DARK = "dark";
    
    /***************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates an instance of NotificationPane with no 
     * {@link #contentProperty() content}, {@link #textProperty() text}, 
     * {@link #graphicProperty() graphic} properties set, and no 
     * {@link #getActions() actions} specified.
     */
    public NotificationPane() {
        this(null);
    }
    
    /**
     * Creates an instance of NotificationPane with the 
     * {@link #contentProperty() content} property set, but no 
     * {@link #textProperty() text} or
     * {@link #graphicProperty() graphic} property set, and no
     * {@link #getActions() actions} specified.
     * 
     * @param content The content to show in the NotificationPane behind where
     *      the notification bar will appear, that is, the content 
     *      <strong>will not</strong>appear in the notification bar. 
     */
    public NotificationPane(Node content) {
        this(content, "");
    }
    
    /**
     * Creates an instance of NotificationPane with the 
     * {@link #contentProperty() content} and {@link #textProperty() text} 
     * property set, but no {@link #graphicProperty() graphic} property set, and
     * no {@link #getActions() actions} specified.
     * 
     * @param content The content to show in the NotificationPane behind where
     *      the notification bar will appear, that is, the content 
     *      <strong>will not</strong>appear in the notification bar. 
     * @param text The text to show in the notification pane.
     */
    public NotificationPane(Node content, String text) {
        this(content, text, null);
    }
    
    /**
     * Creates an instance of NotificationPane with the 
     * {@link #contentProperty() content}, {@link #textProperty() text} and 
     * {@link #graphicProperty() graphic} properties set, but no 
     * {@link #getActions() actions} specified.
     * 
     * @param content The content to show in the NotificationPane behind where
     *      the notification bar will appear, that is, the content 
     *      <strong>will not</strong>appear in the notification bar. 
     * @param text The text to show in the notification pane.
     * @param graphic The node to show in the notification pane.
     */
    public NotificationPane(Node content, String text, Node graphic) {
        this(content, text, graphic, (Action[])null);
    }
    
    /**
     * Creates an instance of NotificationPane with the 
     * {@link #contentProperty() content}, {@link #textProperty() text} and 
     * {@link #graphicProperty() graphic} property set, and the provided actions 
     * copied into the {@link #getActions() actions} list.
     * 
     * @param content The content to show in the NotificationPane behind where
     *      the notification bar will appear, that is, the content 
     *      <strong>will not</strong>appear in the notification bar. 
     * @param text The text to show in the notification pane.
     * @param graphic The node to show in the notification pane.
     * @param actions The actions to show in the notification pane.
     */
    public NotificationPane(Node content, String text, Node graphic, Action... actions) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setContent(content);
        setText(text);
        setGraphic(graphic);
        if (actions != null) {
            for (Action action : actions) {
                if (action == null) continue;
                getActions().add(action);
            }
        }
    }
    
    
    
    /***************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/
    
    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new NotificationPaneSkin(this);
    }
    
    /** {@inheritDoc} */
    @Override protected String getUserAgentStylesheet() {
        return NotificationPane.class.getResource("notificationpane.css").toExternalForm();
    }
    
    
    
    /***************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- content
    private ObjectProperty<Node> content = new SimpleObjectProperty<Node>(this, "content");
    
    /**
     * The content property represents what is shown in the scene 
     * <strong>that is not within</strong> the notification bar. In other words,
     * it is what the notification bar should appear on top of. For example, in
     * the scenario where you are using a {@link WebView} to show to the user
     * websites, and you want to popup up a notification bar to save a password,
     * the content would be the {@link WebView}. Refer to the 
     * {@link NotificationPane} class documentation for more details.
     *  
     * @return A property representing the content of this NotificationPane.
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }
    public final void setContent(Node value) {
        this.content.set(value); 
    }
    public final Node getContent() {
        return content.get();
    }
    
    
    // --- text
    private StringProperty text = new SimpleStringProperty(this, "text");
    
    /**
     * The text property represents the text to show within the popup 
     * notification bar that appears on top of the 
     * {@link #contentProperty() content} that is within the NotificationPane.
     * 
     * @return A property representing the text shown in the notification bar.
     */
    public final StringProperty textProperty() {
        return text;
    }
    public final void setText(String value) {
        this.text.set(value); 
    }
    public final String getText() {
        return text.get();
    }
    
    
    // --- graphic
    private ObjectProperty<Node> graphic = new SimpleObjectProperty<Node>(this, "graphic");
    
    /**
     * The graphic property represents the {@link Node} to show within the popup 
     * notification bar that appears on top of the 
     * {@link #contentProperty() content} that is within the NotificationPane.
     * Despite the term 'graphic', this can be an arbitrarily complex scenegraph
     * in its own right.
     * 
     * @return A property representing the graphic shown in the notification bar.
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }
    public final void setGraphic(Node value) {
        this.graphic.set(value); 
    }
    public final Node getGraphic() {
        return graphic.get();
    }
    
    
    // --- showing
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");
    
    /**
     * A read-only property that represents whether the notification bar popup
     * should be showing to the user or not. To toggle visibility, use the
     * {@link #show()} and {@link #hide()} methods.
     * 
     * @return A property representing whether the notifications bar is currently showing.
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }
    private final void setShowing(boolean value) {
        this.showing.set(value); 
    }
    public final boolean isShowing() {
        return showing.get();
    }
    
    
    // --- show from top
    private BooleanProperty showFromTop = new SimpleBooleanProperty(this, "showFromTop", true) {
        protected void invalidated() {
            pseudoClassStateChanged(SHOW_FROM_TOP_PSEUDOCLASS_STATE,      isShowFromTop());
            pseudoClassStateChanged(SHOW_FROM_BOTTOM_PSEUDOCLASS_STATE, ! isShowFromTop());
        }
    };
    
    /**
     * A property representing whether the notification bar should appear from the
     * top  or the bottom of the NotificationPane area. By default it will appear 
     * from the top, but this can be changed by setting this property to false.
     * 
     * @return A property representing where the notification bar should appear from.
     */
    public final BooleanProperty showFromTopProperty() {
        return showFromTop;
    }
    public final void setShowFromTop(boolean value) {
        this.showFromTop.set(value); 
    }
    public final boolean isShowFromTop() {
        return showFromTop.get();
    }
    
    
    
    /***************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    // --- actions
    private final ObservableList<Action> actions = FXCollections.<Action> observableArrayList();

    /**
     * Observable list of actions used for the actions area of the notification 
     * bar. Modifying the contents of this list will change the actions available to
     * the user.
     * @return The {@link ObservableList} of actions available to the user.
     */
    public final ObservableList<Action> getActions() {
        return actions;
    }
    
    /**
     * Call this to make the notification bar appear on top of the 
     * {@link #contentProperty() content} of this {@link NotificationPane}.
     * If the notification bar is already showing this will be a no-op.
     */
    public void show() {
        setShowing(true);
    }
    
    /**
     * Call this to make the notification bar disappear from the 
     * {@link #contentProperty() content} of this {@link NotificationPane}.
     * If the notification bar is already hidden this will be a no-op.
     */
    public void hide() {
        setShowing(false);
    }
    
    
    
    /**************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
     
     private static final String DEFAULT_STYLE_CLASS = "notification-pane";
     
     private static final PseudoClass SHOW_FROM_TOP_PSEUDOCLASS_STATE =
             PseudoClass.getPseudoClass("top");
     private static final PseudoClass SHOW_FROM_BOTTOM_PSEUDOCLASS_STATE =
             PseudoClass.getPseudoClass("bottom");
}
