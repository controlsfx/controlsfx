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
package org.controlsfx.control.breadcrumbs;

import impl.org.controlsfx.skin.BreadCrumbBarSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;

/**
 * Represents a bread crumb bar
 * 
 */
public class BreadCrumbBar<T> extends Control {

    private final ObjectProperty<TreeItem<T>> pathTarget = new SimpleObjectProperty<TreeItem<T>>(this, "pathTarget");
    private final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactory = new SimpleObjectProperty<BreadCrumbNodeFactory<T>>(this, "crumbFactory");


    @SuppressWarnings("serial")
    public static class BreadCrumbActionEvent<TE> extends Event {
        @SuppressWarnings("rawtypes")
        public static final EventType<BreadCrumbActionEvent> CRUMB_ACTION = new EventType<BreadCrumbActionEvent>("CRUMB_ACTION");

        private final TreeItem<TE> crumbModel;

        public BreadCrumbActionEvent(TreeItem<TE> crumbModel) {
            super(CRUMB_ACTION);
            this.crumbModel = crumbModel;
        }

        public TreeItem<TE> getCrumbModel() {
            return crumbModel;
        }
    }


    private final BreadCrumbNodeFactory<T> defaultCrumbNodeFactory = new BreadCrumbNodeFactory<T>(){
        @Override
        public BreadCrumbButton createBreadCrumbButton(TreeItem<T> crumb, int index) {
            return new BreadCrumbButton(crumb.getValue() != null ? crumb.getValue().toString() : "", index == 0);
        }
    };

    public BreadCrumbBar(){
        this(null);
    }

    public BreadCrumbBar(TreeItem<T> pathTarget) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        setPathTarget(pathTarget);
        setCrumbFactory(defaultCrumbNodeFactory);
    }

    /**
     * Register an action event handler which is invoked when a bread crumb is activated
     * @param handler
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setOnBreadCrumbAction(EventHandler<BreadCrumbActionEvent<T>> handler){
        addEventHandler(BreadCrumbActionEvent.CRUMB_ACTION, (EventHandler<BreadCrumbActionEvent>)(Object)handler);
    }


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

    /**
     * Get the current target path
     */
    public final TreeItem<T> getPathTarget() {
        return pathTarget.get();
    }

    /**
     * Set the bottom most path node.
     * <p>
     * Consider the following hierarchy:
     * [Root] > [Folder] > [SubFolder] > [myfile.txt]
     * 
     * You have to set the [myfile.txt] tree node as path target.
     * 
     * @param pathTarget
     */
    public final void setPathTarget(TreeItem<T> pathTarget){
        this.pathTarget.set(pathTarget);
    }

    public final ObjectProperty<TreeItem<T>> pathTargetProperty() {
        return pathTarget;
    }

    /**
     * Append the given crumbs to the path
     * @param crumbs
     */
    public final void appendCrumbs(T... crumbs){
        TreeItem<T> subRoot = getPathTarget();
        for (T crumb : crumbs) {
            TreeItem<T> currentNode = new TreeItem<T>(crumb);
            if(subRoot == null){
                subRoot = currentNode; 
            }else{
                subRoot.getChildren().add(currentNode);
                subRoot = currentNode;
            }
        }
        setPathTarget(subRoot);
    }


    // --- crumb factory
    public final ObjectProperty<BreadCrumbNodeFactory<T>> crumbFactoryProperty() {
        return crumbFactory;
    }


    /**
     * Sets the crumb factory to create (custom) {@link BreadCrumbButton} instances.
     * <code>null</code> is not allowed and will result in a fall back to the default factory.
     */
    public final void setCrumbFactory(BreadCrumbNodeFactory<T> value) {
        if(value == null){
            value = defaultCrumbNodeFactory;
        }
        crumbFactoryProperty().set(value);
    }

    /**
     * Returns the cell factory that will be used to create {@link BreadCrumbButton} 
     * instances
     */
    public final BreadCrumbNodeFactory<T> getCrumbFactory() {
        return crumbFactory.get();
    }

    // Style sheet handling

    private static final String DEFAULT_STYLE_CLASS = "bread-crumb-bar";
}
