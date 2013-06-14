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
package org.controlsfx.dialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Pair;

class DialogUtils {

    public static Pair<Scene, Parent> getOwners(Object _owner) {
        Scene scene = null;
        Parent owner = null;
        
        // we need to determine the type of the owner, so that we can appropriately
        // show the dialog
        if (_owner == null) {
            // lets just get the focused stage and show the dialog in there
            Iterator<Window> windows = Window.impl_getWindows();
            Window window = null;
            while (windows.hasNext()) {
                window = windows.next();
                if (window.isFocused()) {
                    break;
                }
            }
            _owner = window;
        } 
        
        if (_owner instanceof Scene) {
            scene = (Scene) _owner;
        } else if (_owner instanceof Stage) {
            scene = ((Stage) _owner).getScene();
        } else if (_owner instanceof Tab) {
            // special case for people wanting to show a lightweight dialog inside
            // one tab whilst the rest of the TabPane remains responsive.
            // we keep going up until the styleclass is "tab-content-area"
            owner = (Parent) ((Tab)_owner).getContent();
        } else if (_owner instanceof Node) {
            owner = getFirstParent((Node)_owner);
        } else {
            throw new IllegalArgumentException("Unknown owner: " + _owner.getClass());
        }
        
        if (scene == null && owner != null) {
            scene = owner.getScene();
        }
        
        return new Pair<Scene, Parent>(scene, owner);
    }
    
    private static Parent getFirstParent(Node n) {
        if (n == null) return null;
        return n instanceof Parent ? (Parent) n : getFirstParent(n.getParent());
    }
    
    @SuppressWarnings("unchecked")
    public static ObservableList<Node> getChildren(Parent p) {
        ObservableList<Node> children = null;
        
        try {
            Method getChildrenMethod = Parent.class.getDeclaredMethod("getChildren");
            
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
    
    public static void injectIntoParent(Parent owner, Callback<Void, Node> buildCallback) {
        ObservableList<Node> ownerParentChildren = DialogUtils.getChildren(owner.getParent());
        
        // we've got the children list, now we need to insert a temporary
        // layout container holding our dialogs and opaque layer / effect
        // in place of the owner (the owner will become a child of the dialog
        // stack)
        int ownerPos = ownerParentChildren.indexOf(owner);
        ownerParentChildren.remove(ownerPos);
        Node injectedNode = buildCallback.call(null);
        ownerParentChildren.add(ownerPos, injectedNode);
    }
    
    public static Pane buildOverlayPane(final Scene scene, final Parent parent, final Node content, final boolean showBackground) {
        final Region opaqueLayer = new Region();
        
        Pane dialogStack = new Pane(content) {
            private boolean isFirstRun = true;
            
            protected void layoutChildren() {
                final double w = getOverlayWidth(scene, parent);
                final double h = getOverlayHeight(scene, parent);
                
                final double x = 0;
                final double y = 0;
                
                if (parent != null) {
                    parent.resizeRelocate(x, y, w, h);
                }
                
                if (showBackground) {
                    opaqueLayer.resizeRelocate(x, y, w, h);
                }
                
                final double dialogWidth = content.prefWidth(-1);
                final double dialogHeight = content.prefHeight(-1);
                content.resize(snapSize(dialogWidth), snapSize(dialogHeight));
                
                // hacky, but we only want to position the dialog the first time 
                // it is laid out - after that the only way it should move is if
                // the user moves it.
                if (isFirstRun) {
                    isFirstRun = false;
                    
                    double dialogX = content.getLayoutX();
                    dialogX = dialogX == 0.0 ? w / 2.0 - dialogWidth / 2.0 : dialogX;
                    
                    double dialogY = content.getLayoutY();
                    dialogY = dialogY == 0.0 ? h / 2.0 - dialogHeight / 2.0 : dialogY;
                    
                    content.relocate(snapPosition(dialogX), snapPosition(dialogY));
                }
            }
        };
        dialogStack.setManaged(true);
        
        if (parent != null) {
            dialogStack.getChildren().add(0, parent);
            
            // copy in layout properties, etc, so that the dialogStack displays
            // properly in (hopefully) whatever layout the owner node is in
            dialogStack.getProperties().putAll(parent.getProperties());
        }
        
        if (showBackground) {
            opaqueLayer.getStyleClass().add("lightweight-dialog-background");
            
            dialogStack.getChildren().add(parent == null ? 0 : 1, opaqueLayer);
        }
        
        return dialogStack;
    }
    
    public static double getOverlayWidth(final Scene scene, final Parent owner) {
        if (owner != null) {
            return owner.getLayoutBounds().getWidth();
        } else if (scene != null) {
            return scene.getWidth();
        } 
        
        return 0;
    }
    
    public static double getOverlayHeight(final Scene scene, final Parent owner) {
        if (owner != null) {
            return owner.getLayoutBounds().getHeight();
        } else if (scene != null) {
            return scene.getHeight();
        } 
        
        return 0;
    }
}
