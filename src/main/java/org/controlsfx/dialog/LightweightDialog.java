package org.controlsfx.dialog;

import java.util.Iterator;

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
    
    private Scene scene;
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
    
    LightweightDialog(String title, Object owner) {
        super();
        
        // we need to determine the type of the owner, so that we can appropriately
        // show the dialog
        if (owner == null) {
            // lets just get the focused stage and show the dialog in there
            Iterator<Window> windows = Window.impl_getWindows();
            Window window = null;
            while (windows.hasNext()) {
                window = windows.next();
                if (window.isFocused()) {
                    break;
                }
            }
            owner = window;
        } 
        
        if (owner instanceof Scene) {
            this.scene = (Scene) owner;
        } else if (owner instanceof Stage) {
            this.scene = ((Stage) owner).getScene();
        } else {
            throw new IllegalArgumentException("Unknown owner: " + owner.getClass());
        }
        
        
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
                
                double newX = event.getSceneX() + mouseDragDeltaX;
                newX = Utils.clamp(0, newX, scene.getWidth() - w + DROP_SHADOW_SIZE + rightPadding);
                
                double newY = event.getSceneY() + mouseDragDeltaY;
                newY = Utils.clamp(0, newY, scene.getHeight() - h + DROP_SHADOW_SIZE + bottomPadding);
                
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
        // install CSS
        scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        dialogStack = new Pane(originalParent, lightweightDialog) {
            protected void layoutChildren() {
                final double w = dialogStack.getWidth();
                final double h = dialogStack.getHeight();
                
                if (opaqueLayer != null) {
                    opaqueLayer.resizeRelocate(0, 0, w, h);
                }
                
                final double dialogWidth = lightweightDialog.prefWidth(-1);
                final double dialogHeight = lightweightDialog.prefHeight(-1);
                
                double dialogX = lightweightDialog.getLayoutX();
                dialogX = dialogX == 0.0 ? w/2.0-dialogWidth/2.0 : dialogX;
                
                double dialogY = lightweightDialog.getLayoutY();
                dialogY = dialogY == 0.0 ? h/2.0-dialogHeight/2.0 : dialogY;
                
                lightweightDialog.relocate(snapPosition(dialogX), snapPosition(dialogY));
                lightweightDialog.resize(snapSize(dialogWidth), snapSize(dialogHeight));
            }
        };
        
        if (effect == null) {
            // opaque layer
            opaqueLayer = new Region();
            opaqueLayer.getStyleClass().add("lightweight-dialog-background");
            
            dialogStack.getChildren().add(1, opaqueLayer);
        } else {
            tempEffect = originalParent.getEffect();
            originalParent.setEffect(effect);
        }
        
        
        lightweightDialog.setVisible(true);
        scene.setRoot(dialogStack);
        
        // This forces the lightweight dialog to be modal
        Toolkit.getToolkit().enterNestedEventLoop(this);
    }
    
    @Override public void hide() {
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
        
        // stop the lightweight dialog from being modal (i.e. restart the 
        // execution after it paused with the dialog being shown)
        Toolkit.getToolkit().exitNestedEventLoop(this, null);
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
}
