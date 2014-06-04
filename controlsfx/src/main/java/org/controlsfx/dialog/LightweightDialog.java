/**
 * Copyright (c) 2013, 2014 ControlsFX
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

import impl.org.controlsfx.ImplUtils;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.effect.Effect;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.Window;

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
    private Group dialogStack;
    private Parent originalParent;
    
    private BooleanProperty focused;
    private StringProperty title;
    private BooleanProperty resizable;
    
    private Effect effect;
    private Effect tempEffect;
    
    private boolean modal = true;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    LightweightDialog(final String title, final Object incomingOwner, final DialogStyle style) {
        super();
        
        Object _owner = incomingOwner;
        
        // we need to determine the type of the owner, so that we can appropriately
        // show the dialog
        if (_owner == null) {
            _owner = org.controlsfx.tools.Utils.getWindow(_owner);
        } 
        
        if (_owner instanceof Scene) {
            this.scene = (Scene) _owner;
        } else if (_owner instanceof Stage) {
            this.scene = ((Stage) _owner).getScene();
        } else if (_owner instanceof Tab) {
            // special case for people wanting to show a lightweight dialog inside
            // one tab whilst the rest of the TabPane remains responsive.
            // we keep going up until the styleclass is "tab-content-area"
            owner = (Parent) ((Tab)_owner).getContent();
        } else if (_owner instanceof Node) {
            owner = getFirstParent((Node)_owner);
        } else {
            throw new IllegalArgumentException("Unknown owner: " + _owner.getClass()); //$NON-NLS-1$
        }
        
        if (scene == null && owner != null) {
            this.scene = owner.getScene();
        }
        
        // Don't add window decorations if style is undecorated
        if (style == DialogStyle.UNDECORATED) {
            init(title, style);
            return;
        }
        
        // *** The rest is for adding window decorations ***
        init(title, DialogStyle.CROSS_PLATFORM_DARK);
        lightweightDialog.getStyleClass().addAll("lightweight", "custom-chrome"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragDeltaX = lightweightDialog.getLayoutX() - event.getSceneX();
                mouseDragDeltaY = lightweightDialog.getLayoutY() - event.getSceneY();
                lightweightDialog.setCache(true);
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
        toolBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                lightweightDialog.setCache(false);
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
                    initAnchor(event);
                } else if (type == MouseEvent.MOUSE_DRAGGED) {
                    if (null == dragAnchor) {
                        initAnchor(event);
                    }
                    lightweightDialog.setPrefWidth(Math.max(lightweightDialog.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    lightweightDialog.setPrefHeight(Math.max(lightweightDialog.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }

            private void initAnchor(MouseEvent event) {
                width = lightweightDialog.getWidth();
                height = lightweightDialog.getHeight();
                dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
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
    
    @Override public void setModal(boolean modal) {
        this.modal = modal;
    }
    
    @Override public boolean isModal() {
        return modal;
    }
    
    @Override 
    public ObservableList<String> getStylesheets(){
        return scene.getStylesheets();
    }
    
    @Override public void setEffect(Effect e) {
        this.effect = e;
    }
    
    @Override public StringProperty titleProperty() {
        if (title == null) {
            title = new SimpleStringProperty(this, "title"); //$NON-NLS-1$
        }
        return title;
    }
    
    @Override public void show() {
        if (owner != null && owner.getParent() != null) {
            showInParent();
        } else if (scene != null) {
            showInScene();
        } 
        
        if (isModal()) {
            // This forces the lightweight dialog to be modal
            Object lock = owner != null ? owner : scene; 
            Toolkit.getToolkit().enterNestedEventLoop(lock);
        }
    }
    
    @Override public void hide() {
        if (owner != null) {
            hideInParent();
        } else if (scene != null) {
            hideInScene();
        } 
        
        if (isModal()) {
            // stop the lightweight dialog from being modal (i.e. restart the 
            // execution after it paused with the dialog being shown)
            Object lock = owner != null ? owner : scene;
            Toolkit.getToolkit().exitNestedEventLoop(lock, null);
        }
    }

    @Override BooleanProperty resizableProperty() {
        if (resizable == null) {
            resizable = new SimpleBooleanProperty(this, "resizable", false); //$NON-NLS-1$
        }
        return resizable;
    }
    
    @Override public Window getWindow() {
        return scene.getWindow();
    }

    @Override public Node getRoot() {
        return lightweightDialog;
    }
    
    public double getX() {
        return lightweightDialog.getLayoutX();
    }
    
    public void setX(double x) {
        lightweightDialog.setLayoutX(x);
    }

    @Override ReadOnlyDoubleProperty widthProperty() {
        return lightweightDialog.widthProperty();
    }

    @Override ReadOnlyDoubleProperty heightProperty() {
        return lightweightDialog.heightProperty();
    }    
    
    @Override BooleanProperty focusedProperty() {
        if (focused == null) {
            focused = new SimpleBooleanProperty(this, "focused", true); //$NON-NLS-1$
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
        ImplUtils.stripRootPane(scene, originalParent, false);
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
        ImplUtils.getChildren(owner.getParent(), false).setAll(owner);
        
        dialogStack = null;
    }
    
    private void showInScene() {
        installCSSInScene();
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        buildDialogStack(originalParent);
        
        lightweightDialog.setVisible(true);
        ImplUtils.injectAsRootPane(scene, dialogStack, false);
        configureDialogStack(originalParent);
        lightweightDialog.requestFocus();
    }
    
    private void showInParent() {
        installCSSInScene();
        
        buildDialogStack(owner);
        ImplUtils.injectPane(owner, dialogStack, false);
        configureDialogStack(owner);
        lightweightDialog.setVisible(true);
        lightweightDialog.requestFocus();
    }
    
    private void installCSSInScene() {
        String dialogsCssUrl = DIALOGS_CSS_URL.toExternalForm();
        if (scene != null) {
            // install CSS
            if(!scene.getStylesheets().contains(dialogsCssUrl)){
                scene.getStylesheets().addAll(dialogsCssUrl);
            }
        } else if (owner != null) {
            Scene _scene = owner.getScene();
            if (_scene != null) {
                // install CSS
                if(!scene.getStylesheets().contains(dialogsCssUrl)){
                    _scene.getStylesheets().addAll(dialogsCssUrl);
                }
            }
        }
    }
    
    private void buildDialogStack(final Node parent) {
        dialogStack = new Group(lightweightDialog) {
            private boolean isFirstRun = true;
            
            protected void layoutChildren() {
                final double w = getOverlayWidth();
                final double h = getOverlayHeight();
                
                final double x = getOverlayX();
                final double y = getOverlayY();
                
                if (parent != null) {
                    parent.resizeRelocate(x, y, w, h);
                }
                
                if (opaqueLayer != null) {
                    opaqueLayer.resizeRelocate(x, y, w, h);
                }
                
                final double dialogWidth = lightweightDialog.prefWidth(-1);
                final double dialogHeight = lightweightDialog.prefHeight(-1);
                lightweightDialog.resize((int)(dialogWidth), (int)(dialogHeight));
                
                // hacky, but we only want to position the dialog the first time 
                // it is laid out - after that the only way it should move is if
                // the user moves it.
                if (isFirstRun) {
                    isFirstRun = false;
                    
                    double dialogX = lightweightDialog.getLayoutX();
                    dialogX = dialogX == 0.0 ? w / 2.0 - dialogWidth / 2.0 : dialogX;
                    
                    double dialogY = lightweightDialog.getLayoutY();
                    dialogY = dialogY == 0.0 ? h / 2.0 - dialogHeight / 2.0 : dialogY;
                    
                    lightweightDialog.relocate((int)(dialogX), (int)(dialogY));
                }
            }
        };
                
        dialogStack.setManaged(true);
    }
    
    private void configureDialogStack(Node parent) {
        // always add opaque layer, to block input events to parent scene
        opaqueLayer = new Region();
        dialogStack.getChildren().add(parent == null ? 0 : 1, opaqueLayer);
        
        if (effect == null) {
            opaqueLayer.getStyleClass().add("lightweight-dialog-background"); //$NON-NLS-1$
        } else {
            if (parent != null) {
                tempEffect = parent.getEffect();
                parent.setEffect(effect);
            }
        }
    }
    
    private double getOverlayWidth() {
        if (owner != null) {
            return owner.getLayoutBounds().getWidth();
        } else if (scene != null) {
            return scene.getWidth();
        } 
        
        return 0;
    }
    
    private double getOverlayHeight() {
        if (owner != null) {
            return owner.getLayoutBounds().getHeight();
        } else if (scene != null) {
            return scene.getHeight();
        } 
        
        return 0;
    }
    
    private double getOverlayX() {
        return 0;
    }
    
    private double getOverlayY() {
        return 0;
    }
    
    private Parent getFirstParent(Node n) {
        if (n == null) return null;
        return n instanceof Parent ? (Parent) n : getFirstParent(n.getParent());
    }
}
