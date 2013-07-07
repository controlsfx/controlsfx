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
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

abstract class FXDialog {
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    protected static final URL DIALOGS_CSS_URL = FXDialog.class.getResource("dialogs.css");
    protected static final int HEADER_HEIGHT = 28;
    
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    protected BorderPane root;
    protected HBox windowBtns;
    protected Button closeButton;
    protected Button minButton;
    protected Button maxButton;
    protected Rectangle resizeCorner;
    protected Label titleLabel;
    protected ToolBar toolBar;
    protected double mouseDragDeltaX = 0;
    protected double mouseDragDeltaY = 0;
    
    protected StackPane lightweightDialog;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    protected FXDialog() {
        // no-op, but we expect subclasses to call init(title) once they have
        // initialised their abstract property methods.
    }
     
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    protected final void init(String title) {
        titleProperty().set(title);
        
        resizableProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {
                resizeCorner.setVisible(resizableProperty().get());
                
                if (maxButton != null) {
                    maxButton.setVisible(resizableProperty().get());
    
                    if (resizableProperty().get()) {
                        if (! windowBtns.getChildren().contains(maxButton)) {
                            windowBtns.getChildren().add(1, maxButton);
                        }
                    } else {
                        windowBtns.getChildren().remove(maxButton);
                    }
                }
            }
        });
        
        root = new BorderPane();
        
        // *** The rest is for adding window decorations ***

        lightweightDialog = new StackPane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                if (resizeCorner != null) {
                    resizeCorner.relocate(getWidth() - 20, getHeight() - 20);
                }
            }
        };
        lightweightDialog.getChildren().add(root);
        lightweightDialog.getStyleClass().addAll("dialog", "decorated-root");
        
        focusedProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable valueModel) {                
                boolean active = ((ReadOnlyBooleanProperty)valueModel).get();
                lightweightDialog.pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, active);
            }
        });

        toolBar = new ToolBar();
        toolBar.getStyleClass().add("window-header");
        toolBar.setPrefHeight(HEADER_HEIGHT);
        toolBar.setMinHeight(HEADER_HEIGHT);
        toolBar.setMaxHeight(HEADER_HEIGHT);
        
        titleLabel = new Label();
        titleLabel.setMaxHeight(Double.MAX_VALUE);
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
                FXDialog.this.hide();
            }
        });
        minButton = new WindowButton("minimize");
        minButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                setIconified(isIconified());
            }
        });

        maxButton = new WindowButton("maximize");

        windowBtns = new HBox(3);
        windowBtns.getStyleClass().add("window-buttons");
        windowBtns.getChildren().addAll(minButton, maxButton, closeButton);

        toolBar.getItems().addAll(titleLabel, spacer, windowBtns);
        root.setTop(toolBar);

        resizeCorner = new Rectangle(10, 10);
        resizeCorner.getStyleClass().add("window-resize-corner");
        
        resizeCorner.setManaged(false);
        lightweightDialog.getChildren().add(resizeCorner);
    }
    
    
    
    /***************************************************************************
     * 
     * Abstract API
     * 
     **************************************************************************/
    
    public abstract void show();
    
    public abstract void hide();
    
    public abstract void sizeToScene();
    
    // --- resizable
    abstract BooleanProperty resizableProperty();
    
    
    // --- focused
    abstract ReadOnlyBooleanProperty focusedProperty();
    
    
    // --- title
    abstract StringProperty titleProperty();
    
    // --- content
    public abstract void setContentPane(Pane pane);
    
    // --- root
    public abstract Node getRoot();
    
    
    // --- width
    /**
     * Property representing the width of the dialog.
     */
    abstract ReadOnlyDoubleProperty widthProperty();
    
    
    // --- height
    /**
     * Property representing the height of the dialog.
     */
    abstract ReadOnlyDoubleProperty heightProperty();
    
    
    /**
     * Sets whether the dialog can be iconified (minimized)
     * @param iconifiable if dialog should be iconifiable
     */
    abstract void setIconifiable(boolean iconifiable);
    
    abstract void setIconified(boolean iconified);
    
    abstract boolean isIconified();
    
    /**
     * Sets whether the dialog can be closed
     * @param iconifiable if dialog should be closable
     */
    abstract void setClosable( boolean closable );
    
    abstract void setModal(boolean modal);
    
    abstract boolean isModal();
    
    
    
    /***************************************************************************
     *                                                                         
     * Support Classes                                                 
     *                                                                         
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
     *                                                                         
     * Stylesheet Handling                                                     
     *                                                                         
     **************************************************************************/
    protected static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

}
