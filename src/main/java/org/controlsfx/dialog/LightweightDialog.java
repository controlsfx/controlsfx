package org.controlsfx.dialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

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
    private Pane dialogStack;
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
    
    LightweightDialog(final String title, final Object incomingOwner) {
        super();
        
        Object _owner = incomingOwner;
        
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
            throw new IllegalArgumentException("Unknown owner: " + _owner.getClass());
        }
        
        if (scene == null && owner != null) {
            this.scene = owner.getScene();
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
    
    @Override public void setModal(boolean modal) {
        this.modal = modal;
    }
    
    @Override public boolean isModal() {
        return modal;
    }
    
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
        getChildren(owner.getParent()).setAll(owner);
        
        dialogStack = null;
    }
    
    private void showInScene() {
        installCSSInScene();
        
        // modify scene root to install opaque layer and the dialog
        originalParent = scene.getRoot();
        buildDialogStack(originalParent);
        
        lightweightDialog.setVisible(true);
        scene.setRoot(dialogStack);
    }
    
    private void showInParent() {
        installCSSInScene();
        
        ObservableList<Node> ownerParentChildren = getChildren(owner.getParent());
        
        // we've got the children list, now we need to insert a temporary
        // layout container holding our dialogs and opaque layer / effect
        // in place of the owner (the owner will become a child of the dialog
        // stack)
        int ownerPos = ownerParentChildren.indexOf(owner);
        ownerParentChildren.remove(ownerPos);
        buildDialogStack(owner);
        ownerParentChildren.add(ownerPos, dialogStack);
        
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
    
    private void buildDialogStack(final Node parent) {
        dialogStack = new Pane(lightweightDialog) {
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
                lightweightDialog.resize(snapSize(dialogWidth), snapSize(dialogHeight));
                
                // hacky, but we only want to position the dialog the first time 
                // it is laid out - after that the only way it should move is if
                // the user moves it.
                if (isFirstRun) {
                    isFirstRun = false;
                    
                    double dialogX = lightweightDialog.getLayoutX();
                    dialogX = dialogX == 0.0 ? w / 2.0 - dialogWidth / 2.0 : dialogX;
                    
                    double dialogY = lightweightDialog.getLayoutY();
                    dialogY = dialogY == 0.0 ? h / 2.0 - dialogHeight / 2.0 : dialogY;
                    
                    lightweightDialog.relocate(snapPosition(dialogX), snapPosition(dialogY));
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
        
        if (effect == null) {
            // opaque layer
            opaqueLayer = new Region();
            opaqueLayer.getStyleClass().add("lightweight-dialog-background");
            
            dialogStack.getChildren().add(parent == null ? 0 : 1, opaqueLayer);
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
    
    @SuppressWarnings("unchecked")
    private ObservableList<Node> getChildren(Parent p) {
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
}
