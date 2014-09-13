/**
 * Copyright (c) 2014 ControlsFX
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
package org.controlsfx.dialog.wizard;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;

public class ImplUtils {

    private ImplUtils() {
        // no-op
    }
    
    public static List<Node> getChildren(Node n, boolean useReflection) {
        return n instanceof Parent ? getChildren((Parent)n, useReflection) : Collections.emptyList();
    }
    
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildren(Parent p, boolean useReflection) {
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
            children = s instanceof SkinBase ? ((SkinBase<?>)s).getChildren() : null;
        } else if (useReflection) {
            // we really want to avoid using this!!!!
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
        }
        
        if (useReflection && children == null) {
            throw new RuntimeException("Unable to get children for Parent of type " + p.getClass() +  //$NON-NLS-1$
                                       ". useReflection is set to true"); //$NON-NLS-1$
        }
        
        return children == null ? FXCollections.emptyObservableList() : children;
    }
}
