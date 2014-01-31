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
package org.controlsfx.control;

import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Callback;

import com.sun.javafx.event.EventHandlerManager;

/**
 * Represents a bread crumb bar. 
 * This control is useful to visualize and navigate a hierarchical path structure, such as file systems.
 * 
 */
public class BreadCrumbBar<T> extends Control {

    private final ObjectProperty<TreeItem<T>> selectedCrumb = new SimpleObjectProperty<TreeItem<T>>(this, "selectedCrumb");
    private final ObjectProperty<Boolean> autoNavigationEnabled = new SimpleObjectProperty<Boolean>(this, "autoNavigationEnabled");
    private final ObjectProperty<Callback<TreeItem<T>, Button>> crumbFactory = new SimpleObjectProperty<Callback<TreeItem<T>, Button>>(this, "crumbFactory");
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);


    /**
     * Represents an Event which is fired when a bread crumb was activated.
     *
     * @param <TE>
     */
    @SuppressWarnings("serial")
    public static class BreadCrumbActionEvent<TE> extends Event {
        @SuppressWarnings("rawtypes")
        public static final EventType<BreadCrumbActionEvent> CRUMB_ACTION = new EventType<BreadCrumbActionEvent>("CRUMB_ACTION");

        private final TreeItem<TE> selectedCrumb;

        public BreadCrumbActionEvent(TreeItem<TE> selectedCrumb) {
            super(CRUMB_ACTION);
            this.selectedCrumb = selectedCrumb;
        }

        /**
         * Returns the crumb which was the action target
         * @return
         */
        public TreeItem<TE> getSelectedCrumb() {
            return selectedCrumb;
        }
    }


    public final ObjectProperty<EventHandler<BreadCrumbBar.BreadCrumbActionEvent<T>>> onCrumbActionProperty() { return onCrumbAction; }
    public final void setOnCrumbAction(EventHandler<BreadCrumbBar.BreadCrumbActionEvent<T>> value) { onCrumbActionProperty().set(value); }
    public final EventHandler<BreadCrumbBar.BreadCrumbActionEvent<T>> getOnCrumbAction() { return onCrumbActionProperty().get(); }
    private ObjectProperty<EventHandler<BreadCrumbBar.BreadCrumbActionEvent<T>>> onCrumbAction = new ObjectPropertyBase<EventHandler<BreadCrumbBar.BreadCrumbActionEvent<T>>>() {
        @SuppressWarnings("rawtypes")
        @Override protected void invalidated() {
            eventHandlerManager.setEventHandler(BreadCrumbActionEvent.CRUMB_ACTION, (EventHandler<BreadCrumbActionEvent>)(Object)get());
        }

        @Override
        public Object getBean() {
            return BreadCrumbBar.this;
        }

        @Override
        public String getName() {
            return "onCrumbAction";
        }
    };


    /**
     * Default crumb node factory. This factory is used when no custom factory is specified by the user.
     */
    private final Callback<TreeItem<T>, Button> defaultCrumbNodeFactory = new Callback<TreeItem<T>, Button>(){
        @Override
        public Button call(TreeItem<T> crumb) {
            return new BreadCrumbBarSkin.BreadCrumbButton(crumb.getValue() != null ? crumb.getValue().toString() : "");
        }
    };

    /**
     * Creates an empty bread crumb bar
     */
    public BreadCrumbBar(){
        this(null);
    }

    /**
     * Creates a bread crumb bar with the given initial model
     * @param pathTarget
     */
    public BreadCrumbBar(TreeItem<T> pathTarget) {
        autoNavigationEnabled.set(true); // by default, auto navigation is enabled
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setSelectedCrumb(pathTarget);
        setCrumbFactory(defaultCrumbNodeFactory);
    }

    /** {@inheritDoc} */
    @Override public EventDispatchChain buildEventDispatchChain(EventDispatchChain tail) {
        return tail.prepend(eventHandlerManager);
    }


    /**
     * Get the current target path
     */
    public final TreeItem<T> getSelectedCrumb() {
        return selectedCrumb.get();
    }

    /**
     * Set the bottom most path node (the node on the most-right side in terms of the bread crumb bar). 
     * The full path is then being constructed using getParent() of the tree-items.
     * 
     * <p>
     * Consider the following hierarchy:
     * [Root] > [Folder] > [SubFolder] > [myfile.txt]
     * 
     * To show the above bread crumb bar, you have to set the [myfile.txt] tree-node as selected crumb.
     * 
     * @param selectedCrumb
     */
    public final void setSelectedCrumb(TreeItem<T> selectedCrumb){
        this.selectedCrumb.set(selectedCrumb);
    }

    public final ObjectProperty<TreeItem<T>> selectedCrumbProperty() {
        return selectedCrumb;
    }


    public final ObjectProperty<Callback<TreeItem<T>, Button>> crumbFactoryProperty() {
        return crumbFactory;
    }

    /**
     * Enable or disable auto navigation (default is enabled).
     * If auto navigation is enabled, it will automatically navigate to the crumb which was clicked by the user.
     * @param enabled
     */
    public void setAutoNavigationEnabled(boolean enabled) {
        autoNavigationEnabled.set(enabled);
    }

    /**
     * Checks if auto navigation is currently enabled.
     * If auto navigation is enabled, it will automatically navigate to the crumb which was clicked by the user.
     * @return
     */
    public boolean isAutoNavigationEnabled() {
        return autoNavigationEnabled.get();
    }

    public  ObjectProperty<Boolean> autoNavigationProperty(){
        return autoNavigationEnabled;
    }


    /**
     * Sets the crumb factory to create (custom) {@link BreadCrumbButton} instances.
     * <code>null</code> is not allowed and will result in a fall back to the default factory.
     */
    public final void setCrumbFactory(Callback<TreeItem<T>, Button> value) {
        if(value == null){
            value = defaultCrumbNodeFactory;
        }
        crumbFactoryProperty().set(value);
    }

    /**
     * Returns the cell factory that will be used to create {@link BreadCrumbButton} 
     * instances
     */
    public final Callback<TreeItem<T>, Button> getCrumbFactory() {
        return crumbFactory.get();
    }

    //
    // Style sheet handling
    //

    private static final String DEFAULT_STYLE_CLASS = "bread-crumb-bar";

    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new BreadCrumbBarSkin<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return BreadCrumbBar.class.getResource("breadcrumbbar.css").toExternalForm();
    }


    //
    // Static helper methods
    //

    /**
     * Construct a tree model from the flat list which then can be set 
     * as selectedCrumb node to be shown 
     * @param crumbs
     */
    public static <T> TreeItem<T> buildTreeModel(T... crumbs){
        TreeItem<T> subRoot = null;
        for (T crumb : crumbs) {
            TreeItem<T> currentNode = new TreeItem<T>(crumb);
            if(subRoot == null){
                subRoot = currentNode; 
            }else{
                subRoot.getChildren().add(currentNode);
                subRoot = currentNode;
            }
        }
        return subRoot;
    }

    
    
    
    
}
