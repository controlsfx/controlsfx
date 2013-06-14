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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;

import com.sun.javafx.Utils;
import com.sun.javafx.tk.Toolkit;

class LightweightDialog extends FXDialog {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    // the modal dialog has to be parented to something, which is either a
    // Parent or a Scene. Which one we use is dependent on what the owner is that
    // is passed into the constructor
    private Scene scene;
    private Parent owner;
    
    private Region opaqueLayer;
    private Pane dialogStack;
    private Parent originalParent;
    
    private BooleanProperty focused;
    private StringProperty title;
    private BooleanProperty resizable;
    
    private Effect effect;
    private Effect tempEffect;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    LightweightDialog(final String title, final Object incomingOwner) {
        super();
        
        Object _owner = incomingOwner;
        
        Pair<Scene, Parent> owners = org.controlsfx.dialog.DialogUtils.getOwners(_owner);
        this.scene = owners.getKey();
        this.owner = owners.getValue();
        
        
        // *** The rest is for adding window decorations ***
        init(title);
        lightweightDialog.getStyleClass().add("lightweight");
        
        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragDeltaX = lightweightDialog.getLayoutX() - event.getSceneX();
                mouseDragDeltaY = lightweightDialog.getLayoutY() - event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                final double w = lightweightDialog.getWidth();
                final double h = lightweightDialog.getHeight();
                
                // remove the drop shadow out of the width calculations
                final double DROP_SHADOW_SIZE = (lightweightDialog.getBoundsInParent().getWidth() - lightweightDialog.getLayoutBounds().getWidth()) / 2.0;
                final Insets padding = lightweightDialog.getPadding();
                final double rightPadding = padding.getRight();
                final double bottomPadding = padding.getBottom();
                
                double minX = 0;
                double maxX = owner == null ? scene.getWidth() : owner.getLayoutBounds().getWidth();
                double newX = event.getSceneX() + mouseDragDeltaX;
                newX = Utils.clamp(minX, newX, maxX - w + DROP_SHADOW_SIZE + rightPadding + minX);
                
                double minY = 0;
                double maxY = owner == null ? scene.getHeight() : owner.getLayoutBounds().getHeight();
                double newY = event.getSceneY() + mouseDragDeltaY;
                newY = Utils.clamp(0, newY, maxY - h + DROP_SHADOW_SIZE + bottomPadding + minY);
                
                lightweightDialog.setLayoutX(newX);
                lightweightDialog.setLayoutY(newY);
            }
        });

        // we don't support maximising or minimising lightweight dialogs, so we 
        // remove these from the toolbar
        minButton = null;
        maxButton = null;
        windowBtns.getChildren().setAll(closeButton);
        
        // add window resizing
        EventHandler<MouseEvent> resizeHandler = new EventHandler<MouseEvent>() {
            private double width;
            private double height;
            private Point2D dragAnchor;

            @Override public void handle(MouseEvent event) {
                EventType<? extends MouseEvent> type = event.getEventType();
                
                if (type == MouseEvent.MOUSE_PRESSED) {
                    width = lightweightDialog.getWidth();
                    height = lightweightDialog.getHeight();
                    dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
                } else if (type == MouseEvent.MOUSE_DRAGGED) {
                    lightweightDialog.setPrefWidth(Math.max(lightweightDialog.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    lightweightDialog.setPrefHeight(Math.max(lightweightDialog.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);
       
        // make focused by default
        lightweightDialog.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        lightweightDialog.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
    }
    
    

    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    public void setEffect(Effect e) {
        this.effect = e;
    }
    
    @Override public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(this, "title");
        }
        return title;
    }
    
    @Override public void show() {
        if (owner != null) {
            showInParent();
        } else if (scene != null) {
            showInScene();
        } 
        
        // This forces the lightweight dialog to be modal
        Object lock = owner != null ? owner : scene; 
        Toolkit.getToolkit().enterNestedEventLoop(lock);
    }
    
    @Override public void hide() {
        if (owner != null) {
            hideInParent();
        } else if (scene != null) {
            hideInScene();
        } 
        
        // stop the lightweight dialog from being modal (i.e. restart the 
        // execution after it paused with the dialog being shown)
        Object lock = owner != null ? owner : scene;
        Toolkit.getToolkit().exitNestedEventLoop(lock, null);
    }

    @Override BooleanProperty resizableProperty() {
        if (resizable == null) {
            resizable = new SimpleBooleanProperty(this, "resizable", false);
        }
        return resizable;
    }

    @Override public Node getRoot() {
        return lightweightDialog;
    }

    @Override ReadOnlyDoubleProperty widthProperty() {
        return lightweightDialog.widthProperty();
    }

    @Override ReadOnlyDoubleProperty heightProperty() {
        return lightweightDialog.heightProperty();
    }    
    
    @Override BooleanProperty focusedProperty() {
        if (focused == null) {
            focused = new SimpleBooleanProperty(this, "focused", true);
        }
        return focused;
    }
    
    @Override public void setContentPane(Pane pane) {
        root.setCenter(pane);        
    }
    
    @Override public void sizeToScene() {
        // no-op: This isn't needed when there is not stage...
    }
    
    @Override void setIconified(boolean iconified) {
        // no-op: We don't want to iconify lightweight dialogs
    }
    
    @Override boolean isIconified() {
        return false;
    }
    
    @Override public void setIconifiable(boolean iconifiable) {
        // no-op: We don't want to iconify lightweight dialogs
    }
    
    @Override public void setClosable( boolean closable ) {
        closeButton.setVisible( closable );
    }
    
    
    
    /**************************************************************************
     * 
     * Private implementation
     * 
     **************************************************************************/
    
    private void hideInScene() {
        // remove the opaque layer behind the dialog, if it was used
        if (opaqueLayer != null) {
            opaqueLayer.setVisible(false);
        }
        
        // reset the effect on the parent
        originalParent.setEffect(tempEffect);
        
        // hide the dialog
        lightweightDialog.setVisible(false);
        
        // reset the scene root
        dialogStack.getChildren().remove(originalParent);
        originalParent.getStyleClass().remove("root");
        
        scene.setRoot(originalParent);
    }
    
    private void hideInParent() {
        // remove the opaque layer behind the dialog, if it was used
        if (opaqueLayer != null) {
            opaqueLayer.setVisible(false);
        }
        
        // reset the effect on the parent
        if (originalParent != null) {
            originalParent.setEffect(tempEffect);
        }
        
        // hide the dialog
        lightweightDialog.setVisible(false);
        
        // reset the scenegraph
        DialogUtils.getChildren(owner.getParent()).setAll(owner);
        
        dialogStack = null;
    }
    
    private void showInScene() {
        installCSSInScene();
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        DialogUtils.buildOverlayPane(scene, originalParent, lightweightDialog, true);
        
        lightweightDialog.setVisible(true);
        scene.setRoot(dialogStack);
    }
    
    private void showInParent() {
        installCSSInScene();
        
        DialogUtils.injectIntoParent(owner, new Callback<Void, Node>() {
            @Override public Node call(Void v) {
                DialogUtils.buildOverlayPane(scene, owner, lightweightDialog, true);
                return dialogStack;
            }
        });
        
        lightweightDialog.setVisible(true);
    }
    
    private void installCSSInScene() {
        if (scene != null) {
            // install CSS
            scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        } else if (owner != null) {
            Scene _scene = owner.getScene();
            if (_scene != null) {
                // install CSS
                _scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
            }
        }
    }
}
