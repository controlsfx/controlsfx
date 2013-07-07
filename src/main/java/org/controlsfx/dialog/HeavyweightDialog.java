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
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

// Not public API (class is package-protected), so no JavaDoc is required.
class HeavyweightDialog extends FXDialog {

    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final Stage stage;
    private final Window owner;
    
    private boolean modal;

    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    HeavyweightDialog(String title, Window owner, boolean modal) {
        this(title, owner, modal, StageStyle.TRANSPARENT);
    }

    private HeavyweightDialog(String title, Window owner, boolean modal, StageStyle stageStyle) {
        super();
        this.owner = owner;
        
        stage = new Stage(stageStyle) {
            @Override public void showAndWait() {
                Window owner = getOwner();
                if (owner != null) {
                    // because Stage does not seem to centre itself over its owner, we
                    // do it here.
                    final double x = owner.getX() + (owner.getWidth() / 2.0) - (root.prefWidth(-1) / 2.0);
                    final double y = owner.getY() + (owner.getHeight() / 2.0) - (root.prefHeight(-1)) / 2.0 - 50;
                    setX(x);
                    setY(y);
                }
                super.showAndWait();
            }
        };

        if (owner != null) {
            stage.initOwner(owner);
        }

        setModal(modal);

        // *** The rest is for adding window decorations ***
        init(title);
        lightweightDialog.getStyleClass().add("heavyweight");

        Scene scene = new Scene(lightweightDialog);
        scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        

        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragDeltaX = event.getSceneX();
                mouseDragDeltaY = event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - mouseDragDeltaX);
                stage.setY(event.getScreenY() - mouseDragDeltaY);
            }
        });

        // support maximising the dialog
        maxButton.setOnAction(new EventHandler<ActionEvent>() {
            private double restoreX;
            private double restoreY;
            private double restoreW;
            private double restoreH;

            @Override public void handle(ActionEvent event) {
                Screen screen = Screen.getPrimary(); // todo something more sensible
                double minX = screen.getVisualBounds().getMinX();
                double minY = screen.getVisualBounds().getMinY();
                double maxW = screen.getVisualBounds().getWidth();
                double maxH = screen.getVisualBounds().getHeight();

                if (restoreW == 0 || stage.getX() != minX || stage.getY() != minY || stage.getWidth() != maxW || stage.getHeight() != maxH) {
                    restoreX = stage.getX();
                    restoreY = stage.getY();
                    restoreW = stage.getWidth();
                    restoreH = stage.getHeight();
                    stage.setX(minX);
                    stage.setY(minY);
                    stage.setWidth(maxW);
                    stage.setHeight(maxH);
                } else {
                    stage.setX(restoreX);
                    stage.setY(restoreY);
                    stage.setWidth(restoreW);
                    stage.setHeight(restoreH);
                }
            }
        });

        // add window resizing
        EventHandler<MouseEvent> resizeHandler = new EventHandler<MouseEvent>() {
            private double width;
            private double height;
            private Point2D dragAnchor;

            @Override public void handle(MouseEvent event) {
                EventType<? extends MouseEvent> type = event.getEventType();

                if (type == MouseEvent.MOUSE_PRESSED) {
                    width = stage.getWidth();
                    height = stage.getHeight();
                    dragAnchor = new Point2D(event.getSceneX(), event.getSceneY());
                } else if (type == MouseEvent.MOUSE_DRAGGED) {
                    stage.setWidth(Math.max(lightweightDialog.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    stage.setHeight(Math.max(lightweightDialog.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    @Override public void setModal(boolean modal) {
        this.modal = modal;
        
        if (modal) {
            if (owner != null) {
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
        } else {
            stage.initModality(Modality.NONE);
        }
    }
    
    @Override public boolean isModal() {
        return modal;
    }
    
    @Override public void setContentPane(Pane pane) {
        root.setCenter(pane);
    }

    @Override public void setIconifiable(boolean iconifiable) {
        minButton.setVisible(iconifiable);
    }
    
    @Override public void setClosable(boolean closable) {
        closeButton.setVisible( closable );
    }
    
    @Override public void show() {
        stage.centerOnScreen();
        stage.showAndWait();
    }
    
    @Override public void hide() {
        stage.hide();
    }
    
    @Override public Node getRoot() {
        return stage.getScene().getRoot();
    }
    
    @Override ReadOnlyDoubleProperty heightProperty() {
        return stage.heightProperty();
    }
    
    @Override ReadOnlyDoubleProperty widthProperty() {
        return stage.widthProperty();
    }
    
    @Override BooleanProperty resizableProperty() {
        return stage.resizableProperty();
    }
    
    @Override StringProperty titleProperty() {
        return stage.titleProperty();
    }
    
    @Override ReadOnlyBooleanProperty focusedProperty() {
        return stage.focusedProperty();
    }
    
    @Override public void sizeToScene() {
        stage.sizeToScene();
    }
    
    @Override void setIconified(boolean iconified) {
        stage.setIconified(iconified);
    }
    
    @Override boolean isIconified() {
        return stage.isIconified();
    }

    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

}

