/**
 * Copyright (c) 2014, 2021, ControlsFX
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
package impl.org.controlsfx;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

public class ImplUtils {

    private ImplUtils() {
        // no-op
    }

    public static void injectAsRootPane(Scene scene, Parent injectedParent, boolean useReflection) {
        Parent originalParent = scene.getRoot();
        scene.setRoot(injectedParent);

        // Begin Fix Issue #847, see
        // https://bitbucket.org/controlsfx/controlsfx/issues/847/validationsupport-messes-up-layout
        if (injectedParent instanceof Region) {
            Region region = (Region) injectedParent;

            region.setMaxWidth(Double.MAX_VALUE);
            region.setMaxHeight(Double.MAX_VALUE);
        }
        // End Fix Issue #847

        if (originalParent != null) {
            getChildren(injectedParent, useReflection).add(0, originalParent);

            // copy in layout properties, etc, so that the dialogStack displays
            // properly in (hopefully) whatever layout the owner node is in
            injectedParent.getProperties().putAll(originalParent.getProperties());
        }
    }

    // parent is where we want to inject the injectedParent. We then need to
    // set the child of the injectedParent to include parent.
    // The end result is that we've forced in the injectedParent node above parent.
    public static void injectPane(Parent parent, Parent injectedParent, boolean useReflection) {
        if (parent == null) {
            throw new IllegalArgumentException("parent can not be null"); //$NON-NLS-1$
        }

        List<Node> ownerParentChildren = getChildren(parent.getParent(), useReflection);

        // we've got the children list, now we need to insert a temporary
        // layout container holding our dialogs and opaque layer / effect
        // in place of the owner (the owner will become a child of the dialog
        // stack)
        int ownerPos = ownerParentChildren.indexOf(parent);
        ownerParentChildren.remove(ownerPos);
        ownerParentChildren.add(ownerPos, injectedParent);

        // now we install the parent as a child of the injectedParent
        getChildren(injectedParent, useReflection).add(0, parent);

        // copy in layout properties, etc, so that the dialogStack displays
        // properly in (hopefully) whatever layout the owner node is in
        injectedParent.getProperties().putAll(parent.getProperties());
    }

    public static void stripRootPane(Scene scene, Parent originalParent, boolean useReflection) {
        Parent oldParent = scene.getRoot();
        getChildren(oldParent, useReflection).remove(originalParent);
        originalParent.getStyleClass().remove("root"); //$NON-NLS-1$
        scene.setRoot(originalParent);
    }

    public static List<Node> getChildren(Node n, boolean useReflection) {
        return n instanceof Parent ? getChildren((Parent)n, useReflection) : Collections.emptyList();
    }

    public static List<Node> getChildren(Parent p, boolean useReflection) {
        ObservableList<Node> children = null;

        // previously we used reflection immediately, now we try to avoid reflection
        // by checking the type of the Parent. Still not great...
        if (p instanceof Pane) {
            // This should cover the majority of layout containers, including
            // AnchorPane, FlowPane, GridPane, HBox, Pane, StackPane, TilePane, VBox
            children = ((Pane)p).getChildren();
        } else if (p instanceof Group) {
            children = ((Group)p).getChildren();
        } else if (p instanceof Control) {
            Control c = (Control) p;
            Skin<?> s = c.getSkin();
            children = s instanceof SkinBase ? ((SkinBase<?>)s).getChildren() : getChildrenReflectively(p);
        } else if (useReflection) {
            // we really want to avoid using this!!!!
            children = getChildrenReflectively(p);
        }

        if (children == null) {
            throw new RuntimeException("Unable to get children for Parent of type " + p.getClass() +  //$NON-NLS-1$
                                       ". useReflection is set to " + useReflection); //$NON-NLS-1$
        }

        return children == null ? FXCollections.emptyObservableList() : children;
    }

    @SuppressWarnings("unchecked")
	public static ObservableList<Node> getChildrenReflectively(Parent p) {
        ObservableList<Node> children = null;

        try {
            Method getChildrenMethod = Parent.class.getDeclaredMethod("getChildren"); //$NON-NLS-1$

            if (getChildrenMethod != null) {
                if (! getChildrenMethod.isAccessible()) {
                    getChildrenMethod.setAccessible(true);
                }
                children = (ObservableList<Node>) getChildrenMethod.invoke(p);
            } else {
                // uh oh, trouble
            }
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new RuntimeException("Unable to get children for Parent of type " + p.getClass(), e); //$NON-NLS-1$
        }

        return children;
    }
}
