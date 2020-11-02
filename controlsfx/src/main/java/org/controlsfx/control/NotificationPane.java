/**
 * Copyright (c) 2013, 2020, ControlsFX
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
import java.util.UUID;
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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Skin;
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
 * <table style="border: 1px solid gray; max-width:750px" summary="NotificationPane Screenshots">
 *   <tr>
 *     <th width="200"><center><h3>Setting</h3></center></th>
 *     <th width="520"><center><h3>Screenshot</h3></center></th>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Light theme from top:</strong></td>
 *     <td><center><img src="notication-pane-light-top.png" alt="Screenshot of NotificationPane - Light theme from top"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Light theme from bottom:</strong></td>
 *     <td><center><img src="notication-pane-light-bottom.png" alt="Screenshot of NotificationPane - Light theme from bottom"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Dark theme from top:</strong></td>
 *     <td><center><img src="notication-pane-dark-top.png" alt="Screenshot of NotificationPane - Dark theme from top"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="top" style="text-align:center;"><strong>Dark theme from bottom:</strong></td>
 *     <td><center><img src="notication-pane-dark-bottom.png" alt="Screenshot of NotificationPane - Dark theme from bottom"></center></td>
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
 */
public class NotificationPane extends ControlsFXControl {
    
    /***************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    public static final String STYLE_CLASS_DARK = "dark"; //$NON-NLS-1$
    
    /**
     * Called when the NotificationPane <b>will</b> be shown.
     */
    public static final EventType<Event> ON_SHOWING = newEventType("NOTIFICATION_PANE_ON_SHOWING"); //$NON-NLS-1$

    /**
     * Called when the NotificationPane shows.
     */
    public static final EventType<Event> ON_SHOWN = newEventType("NOTIFICATION_PANE_ON_SHOWN"); //$NON-NLS-1$

    /**
     * Called when the NotificationPane <b>will</b> be hidden.
     */
    public static final EventType<Event> ON_HIDING = newEventType("NOTIFICATION_PANE_ON_HIDING"); //$NON-NLS-1$

    /**
     * Called when the NotificationPane is hidden.
     */
    public static final EventType<Event> ON_HIDDEN = newEventType("NOTIFICATION_PANE_ON_HIDDEN"); //$NON-NLS-1$
    
    /**
     * Static factory method for NotificationPane EventTypes.
     * @param name the name of the new EventType to create
     * @return the new EventType instance
     */
    private static EventType<Event> newEventType(String name) {
        return new EventType<>(Event.ANY, name + UUID.randomUUID().toString());
    }
    
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
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setContent(content);
        
        updateStyleClasses();
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
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(NotificationPane.class, "notificationpane.css");
    }
    
    /***************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- content
    private ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$
    
    /**
     * The content property represents what is shown in the scene 
     * <strong>that is not within</strong> the notification bar. In other words,
     * it is what the notification bar should appear on top of. For example, in
     * the scenario where you are using a WebView to show to the user
     * websites, and you want to popup up a notification bar to save a password,
     * the content would be the WebView. Refer to the 
     * {@link NotificationPane} class documentation for more details.
     *  
     * @return A property representing the content of this NotificationPane.
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }
    
    /**
     * Set the content to be shown in the scene, 
     * <strong>that is not within</strong> the notification bar.
     * @param value 
     */
    public final void setContent(Node value) {
        this.content.set(value); 
    }
    
    /**
     * 
     * @return The content shown in the scene.
     */
    public final Node getContent() {
        return content.get();
    }
    
    
    // --- text
    private StringProperty text = new SimpleStringProperty(this, "text"); //$NON-NLS-1$
    
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
    
    /**
     * Sets the text to show within the popup 
     * notification bar that appears on top of the 
     * {@link #contentProperty() content}.
     * @param value 
     */
    public final void setText(String value) {
        this.text.set(value); 
    }
    
    /**
     * 
     * @return the text showing within the popup 
     * notification bar that appears on top of the 
     * {@link #contentProperty() content}.
     */
    public final String getText() {
        return text.get();
    }
    
    
    // --- graphic
    private ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic"); //$NON-NLS-1$
    
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
    
    /**
     * Sets the {@link Node} to show within the popup 
     * notification bar.
     * @param value 
     */
    public final void setGraphic(Node value) {
        this.graphic.set(value); 
    }
    
    /**
     * 
     * @return the {@link Node} to show within the popup 
     * notification bar.
     */
    public final Node getGraphic() {
        return graphic.get();
    }
    
    
    // --- showing
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing"); //$NON-NLS-1$
    
    /**
     * A read-only property that represents whether the notification bar popup
     * should be showing to the user or not. To toggle visibility, use the
     * {@link #show()} and {@link #hide()} methods.
     * 
     * @return A property representing whether the notification bar is currently showing.
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }
    private final void setShowing(boolean value) {
        this.showing.set(value); 
    }
    /**
     * 
     * @return whether the notification bar is currently showing.
     */
    public final boolean isShowing() {
        return showing.get();
    }
    
    
    // --- show from top
    private BooleanProperty showFromTop = new SimpleBooleanProperty(this, "showFromTop", true) { //$NON-NLS-1$
        @Override protected void invalidated() {
            updateStyleClasses();
        }
    };
    
    /**
     * A property representing whether the notification bar should appear from the
     * top or the bottom of the NotificationPane area. By default it will appear 
     * from the top, but this can be changed by setting this property to false.
     * 
     * @return A property representing where the notification bar should appear from.
     */
    public final BooleanProperty showFromTopProperty() {
        return showFromTop;
    }
    
    /**
     * Sets whether the notification bar should appear from the
     * top or the bottom of the NotificationPane area.
     * @param value 
     */
    public final void setShowFromTop(boolean value) {
        this.showFromTop.set(value); 
    }
    
    /**
     * @return whether the notification bar is appearing from the
     * top or the bottom of the NotificationPane area.
     */
    public final boolean isShowFromTop() {
        return showFromTop.get();
    }
    
    
    // --- On Showing
    public final ObjectProperty<EventHandler<Event>> onShowingProperty() { return onShowing; }
    /**
     * Called just prior to the {@code NotificationPane} being shown.
     */
    public final void setOnShowing(EventHandler<Event> value) { onShowingProperty().set(value); }
    public final EventHandler<Event> getOnShowing() { return onShowingProperty().get(); }
    private ObjectProperty<EventHandler<Event>> onShowing = new SimpleObjectProperty<EventHandler<Event>>(this, "onShowing") { //$NON-NLS-1$
        @Override protected void invalidated() {
            setEventHandler(ON_SHOWING, get());
         }
     };


    // -- On Shown
    public final ObjectProperty<EventHandler<Event>> onShownProperty() { return onShown; }
    /**
     * Called just after the {@link NotificationPane} is shown.
     */
    public final void setOnShown(EventHandler<Event> value) { onShownProperty().set(value); }
    public final EventHandler<Event> getOnShown() { return onShownProperty().get(); }
    private ObjectProperty<EventHandler<Event>> onShown = new SimpleObjectProperty<EventHandler<Event>>(this, "onShown") { //$NON-NLS-1$
        @Override protected void invalidated() {
            setEventHandler(ON_SHOWN, get());
        }
    };


    // --- On Hiding
    public final ObjectProperty<EventHandler<Event>> onHidingProperty() { return onHiding; }
    /**
     * Called just prior to the {@link NotificationPane} being hidden.
     */
    public final void setOnHiding(EventHandler<Event> value) { onHidingProperty().set(value); }
    public final EventHandler<Event> getOnHiding() { return onHidingProperty().get(); }
    private ObjectProperty<EventHandler<Event>> onHiding = new SimpleObjectProperty<EventHandler<Event>>(this, "onHiding") { //$NON-NLS-1$
        @Override protected void invalidated() {
            setEventHandler(ON_HIDING, get());
        }
    };


    // --- On Hidden
    public final ObjectProperty<EventHandler<Event>> onHiddenProperty() { return onHidden; }
    /**
     * Called just after the {@link NotificationPane} has been hidden.
     */
    public final void setOnHidden(EventHandler<Event> value) { onHiddenProperty().set(value); }
    public final EventHandler<Event> getOnHidden() { return onHiddenProperty().get(); }
    private ObjectProperty<EventHandler<Event>> onHidden = new SimpleObjectProperty<EventHandler<Event>>(this, "onHidden") { //$NON-NLS-1$
        @Override protected void invalidated() {
            setEventHandler(ON_HIDDEN, get());
        }
    };
    
    // --- close button visibility
    private BooleanProperty closeButtonVisible = new SimpleBooleanProperty(this, "closeButtonVisible", true); //$NON-NLS-1$
        
    /**
     * A property representing whether the close button in the {@code NotificationPane} should be visible or not.
     * By default it will appear but this can be changed by setting this property to false.
     * 
     * @return A property representing whether the close button in the {@code NotificationPane} should be visible.
     */
    public final BooleanProperty closeButtonVisibleProperty() {
        return closeButtonVisible;
    }
    
    /**
     * Sets whether the close button in {@code NotificationPane} should be visible.
     * 
     * @param value 
     */
    public final void setCloseButtonVisible(boolean value) {
        this.closeButtonVisible.set(value);
    }
    
    /**
     * @return whether the close button in {@code NotificationPane} is visible.
     */
    public final boolean isCloseButtonVisible() {
        return closeButtonVisible.get();
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
     * Shows the NotificationPane with the 
     * {@link #contentProperty() content} and {@link #textProperty() text} 
     * property set, but no {@link #graphicProperty() graphic} property set, and
     * no {@link #getActions() actions} specified.
     * 
     * @param text The text to show in the notification pane.
     */
    public void show(final String text) {
        hideAndThen(new Runnable() {
            @Override public void run() {
                setText(text);
                setShowing(true);
            }
        });
    }
    
    /**
     * Shows the NotificationPane with the 
     * {@link #contentProperty() content}, {@link #textProperty() text} and 
     * {@link #graphicProperty() graphic} properties set, but no 
     * {@link #getActions() actions} specified.
     * 
     * @param text The text to show in the notification pane.
     * @param graphic The node to show in the notification pane.
     */
    public void show(final String text, final Node graphic) {
        hideAndThen(new Runnable() {
            @Override public void run() {
                setText(text);
                setGraphic(graphic);
                setShowing(true);
            }
        });
    }
    
    /**
     * Shows the NotificationPane with the 
     * {@link #contentProperty() content}, {@link #textProperty() text} and 
     * {@link #graphicProperty() graphic} property set, and the provided actions 
     * copied into the {@link #getActions() actions} list.
     * 
     * @param text The text to show in the notification pane.
     * @param graphic The node to show in the notification pane.
     * @param actions The actions to show in the notification pane.
     */
    public void show(final String text, final Node graphic, final Action... actions) {
        hideAndThen(new Runnable() {
            @Override public void run() {
                setText(text);
                setGraphic(graphic);

                if (actions == null) {
                    getActions().clear();
                } else {
                    for (Action action : actions) {
                        if (action == null) continue;
                        getActions().add(action);
                    }
                }
                
                setShowing(true);
            }
        });
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
     * Private Implementation                                                  *
     *                                                                         *
     **************************************************************************/
    
    private void updateStyleClasses() {
        getStyleClass().removeAll("top", "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
        getStyleClass().add(isShowFromTop() ? "top" : "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    private void hideAndThen(final Runnable r) {
        if (isShowing()) {
            final EventHandler<Event> eventHandler = new EventHandler<Event>() {
                @Override public void handle(Event e) {
                    r.run();
                    removeEventHandler(NotificationPane.ON_HIDDEN, this);
                }
            };
            addEventHandler(NotificationPane.ON_HIDDEN, eventHandler);
            hide();
        } else {
            r.run();
        }
    }
    
    
    
    /**************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
     
     private static final String DEFAULT_STYLE_CLASS = "notification-pane"; //$NON-NLS-1$
}
