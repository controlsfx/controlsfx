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

import java.net.URL;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

// Not public API (class is package-protected), so no JavaDoc is required.
class HeavyweightDialog extends FXDialog {

    private static final URL DIALOGS_CSS_URL = HeavyweightDialog.class.getResource("dialogs.css");  
    
    private final Stage stage;

    private BorderPane root;
    private StackPane decoratedRoot;
    private HBox windowBtns;
    private Button closeButton;
    private Button minButton;
    private Button maxButton;
    private Rectangle resizeCorner;
    private double mouseDragOffsetX = 0;
    private double mouseDragOffsetY = 0;
    protected Label titleLabel;

    private static final int HEADER_HEIGHT = 28;

    HeavyweightDialog(String title, Window owner, boolean modal) {
        this(title, owner, modal, StageStyle.TRANSPARENT);
    }

    private HeavyweightDialog(String title, Window owner, boolean modal, StageStyle stageStyle) {
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
        stage.setTitle(title);

        if (owner != null) {
            stage.initOwner(owner);
        }

        if (modal) {
            if (owner != null) {
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
        }

        resizableProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
                resizeCorner.setVisible(stage.isResizable());
                maxButton.setVisible(stage.isResizable());

                if (stage.isResizable()) {
                    windowBtns.getChildren().add(1, maxButton);
                } else {
                    windowBtns.getChildren().remove(maxButton);
                }
            }
        });

        root = new BorderPane();

        Scene scene;



        // *** The rest is for adding window decorations ***

        decoratedRoot = new StackPane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                if (resizeCorner != null) {
                    resizeCorner.relocate(getWidth() - 20, getHeight() - 20);
                }
            }
        };
        decoratedRoot.getChildren().add(root);
        scene = new Scene(decoratedRoot);

        scene.getStylesheets().addAll(DIALOGS_CSS_URL.toExternalForm());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        decoratedRoot.getStyleClass().addAll("dialog", "decorated-root");

        stage.focusedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {                
                boolean active = ((ReadOnlyBooleanProperty)valueModel).get();
                decoratedRoot.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, active);
            }
        });

        ToolBar toolBar = new ToolBar();
        toolBar.getStyleClass().add("window-header");
        toolBar.setPrefHeight(HEADER_HEIGHT);
        toolBar.setMinHeight(HEADER_HEIGHT);
        toolBar.setMaxHeight(HEADER_HEIGHT);

        // add window dragging
        toolBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                mouseDragOffsetX = event.getSceneX();
                mouseDragOffsetY = event.getSceneY();
            }
        });
        toolBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - mouseDragOffsetX);
                stage.setY(event.getScreenY() - mouseDragOffsetY);
            }
        });

        titleLabel = new Label();
        titleLabel.getStyleClass().add("window-title");
        titleLabel.setText(titleProperty().get());

        titleProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
                titleLabel.setText(titleProperty().get());
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // add close min max
        closeButton = new WindowButton("close");
        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                HeavyweightDialog.this.hide();
            }
        });
        minButton = new WindowButton("minimize");
        minButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                stage.setIconified(!stage.isIconified());
            }
        });

        maxButton = new WindowButton("maximize");
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

        windowBtns = new HBox(3);
        windowBtns.getStyleClass().add("window-buttons");
        windowBtns.getChildren().addAll(minButton, maxButton, closeButton);

        toolBar.getItems().addAll(titleLabel, spacer, windowBtns);
        root.setTop(toolBar);

        resizeCorner = new Rectangle(10, 10);
        resizeCorner.getStyleClass().add("window-resize-corner");

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
                    stage.setWidth(Math.max(decoratedRoot.minWidth(-1),   width  + (event.getSceneX() - dragAnchor.getX())));
                    stage.setHeight(Math.max(decoratedRoot.minHeight(-1), height + (event.getSceneY() - dragAnchor.getY())));
                }
            }
        };
        resizeCorner.setOnMousePressed(resizeHandler);
        resizeCorner.setOnMouseDragged(resizeHandler);

        resizeCorner.setManaged(false);
        decoratedRoot.getChildren().add(resizeCorner);
    }
    
    @Override public void setContentPane(Pane pane) {
        root.setCenter(pane);
    }

    public void setIconifiable(boolean iconifiable) {
        minButton.setVisible(iconifiable);
    }
    
    public void setClosable( boolean closable ) {
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
    
    @Override public void sizeToScene() {
        stage.sizeToScene();
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Support classes                                                         *
     *                                                                         *
     **************************************************************************/

    private static class WindowButton extends Button {
        WindowButton(String name) {
            getStyleClass().setAll("window-button");
            getStyleClass().add("window-"+name+"-button");
            StackPane graphic = new StackPane();
            graphic.getStyleClass().setAll("graphic");
            setGraphic(graphic);
            setMinSize(17, 17);
            setPrefSize(17, 17);
        }
    }

    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

}

