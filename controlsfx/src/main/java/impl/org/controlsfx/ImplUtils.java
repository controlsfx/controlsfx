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
package impl.org.controlsfx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class ImplUtils {

    private ImplUtils() {
        // no-op
    }
    
    public static void injectAsRootPane(Scene scene, Parent injectedParent) {
        Parent originalParent = scene.getRoot();
        scene.setRoot(injectedParent);
        
        if (originalParent != null) {
            getChildren(injectedParent).add(0, originalParent);
            
            // copy in layout properties, etc, so that the dialogStack displays
            // properly in (hopefully) whatever layout the owner node is in
            injectedParent.getProperties().putAll(originalParent.getProperties());
        }
    }
    
    public static void injectPane(Parent parent, Parent injectedParent) {
        ObservableList<Node> ownerParentChildren = getChildren(parent.getParent());
        
        // we've got the children list, now we need to insert a temporary
        // layout container holding our dialogs and opaque layer / effect
        // in place of the owner (the owner will become a child of the dialog
        // stack)
        int ownerPos = ownerParentChildren.indexOf(parent);
        ownerParentChildren.remove(ownerPos);
        ownerParentChildren.add(ownerPos, injectedParent);
        
        if (parent != null) {
            getChildren(injectedParent).add(0, parent);
            
            // copy in layout properties, etc, so that the dialogStack displays
            // properly in (hopefully) whatever layout the owner node is in
            injectedParent.getProperties().putAll(parent.getProperties());
        }
    }
    
    public static void stripRootPane(Scene scene, Parent originalParent) {
        Parent oldParent = scene.getRoot();
        getChildren(oldParent).remove(originalParent);
        originalParent.getStyleClass().remove("root"); //$NON-NLS-1$
        scene.setRoot(originalParent);        
    }
    
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildren(Parent p) {
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
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        
        return children;
    }
}
