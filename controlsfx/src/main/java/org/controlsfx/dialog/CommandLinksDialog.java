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
package org.controlsfx.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.controlsfx.DialogResources;

import com.sun.javafx.scene.control.skin.AccordionSkin;

import javafx.beans.binding.DoubleBinding;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class CommandLinksDialog extends Dialog<ButtonType> {
    
    private final static int gapSize = 10;
    
    private final List<Button> buttons = new ArrayList<>();
    
    private GridPane grid = new GridPane() {
        @Override protected double computePrefWidth(double height) {
            double pw = 0;

            for (int i = 0; i < buttons.size(); i++) {
                Button btn = buttons.get(i);
                pw = Math.min(pw, btn.prefWidth(-1));
            }
            return pw + gapSize;
        }

        @Override protected double computePrefHeight(double width) {
            double ph = getDialogPane().getHeader() == null ? 0 : 10;

            for (int i = 0; i < buttons.size(); i++) {
                Button btn = buttons.get(i);
                ph += btn.prefHeight(width) + gapSize;
            }
            return ph * 1.5;
        }
    };
    
    public CommandLinksDialog(ButtonType... links) {
        this(Arrays.asList(links));
    }
    
    public CommandLinksDialog(List<ButtonType> links) {
        this.grid.setHgap(gapSize);
        this.grid.setVgap(gapSize);
        
        final DialogPane dialogPane = new DialogPane() {
            @Override protected Node createButtonBar() {
                return null;
            }
        }; 
        setDialogPane(dialogPane);
        
        dialogPane.getStylesheets().add(getClass().getResource("commandlink.css").toExternalForm());

        setTitle(DialogResources.getString("Dialog.info.title"));

        // FIXME extract to CSS
        dialogPane.setGraphic(new ImageView(new Image("/com/sun/javafx/scene/control/skin/modena/dialog-confirm.png")));
        dialogPane.getButtonTypes().addAll(links);
        
        dialogPane.contentProperty().addListener(o -> updateGrid());

        updateGrid();
        dialogPane.getButtonTypes().addListener((ListChangeListener<? super ButtonType>)c -> {
            updateGrid();
        });
    }
    
    private void updateGrid() {
        final Node content = getDialogPane().getContent();
        final boolean dialogContentIsGrid = grid == content;
        
        if (! dialogContentIsGrid) {
            if (content != null) {
                content.getStyleClass().add("command-link-message");
                grid.add(content, 0, 0);
            }
        }
        
        grid.getChildren().removeAll(buttons);
        int row = 1;
        for (final ButtonType action : getDialogPane().getButtonTypes()) {
            if (action == null) continue; 
            
//            if (! (action instanceof DialogButton)) {
//                throw new IllegalArgumentException("All actions in CommandLinksDialog must be instances of DialogAction");
//            }
//            
//            DialogButton commandLink = (DialogButton) action;

//            //replace link's event handler with a proper one
//            commandLink.setOnAction(new EventHandler<ActionEvent>() {
//                @Override public void handle(ActionEvent ae) {
//                    setResult(commandLink);
//                }
//            });

            final Button button = buildCommandLinkButton(action);   
            final ButtonData buttonType = action.getButtonData();
            button.setDefaultButton(buttonType != null && buttonType.isDefaultButton());
//            button.setOnAction(commandLink);
            
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent ae) {
                    setResult(action);
                }
            });

            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);
            grid.add(button, 0, row++);
            buttons.add(button);
        }

        // last button gets some extra padding (hacky)
        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));

        if (! dialogContentIsGrid) {
            getDialogPane().setContent(grid);
        }
    }
    
    private Button buildCommandLinkButton(ButtonType commandLink) {
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button");
        button.setMaxHeight(Double.MAX_VALUE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        final Label titleLabel = new Label(commandLink.getText() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() {
            {
                bind(titleLabel.prefWidthProperty());
            }

            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1");
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        // TODO no support in DialogButton for long text or graphic
//        Label messageLabel = new Label(commandLink.getLongText() );
//        messageLabel.getStyleClass().addAll("line-2");
//        messageLabel.setWrapText(true);
//        messageLabel.setAlignment(Pos.TOP_LEFT);
//        messageLabel.setMaxHeight(Double.MAX_VALUE);
//        GridPane.setVgrow(messageLabel, Priority.SOMETIMES);
//
//        Image commandLinkImage = commandLink.getGraphic();
//        Node view = commandLinkImage == null ? 
//                new ImageView(getClass().getResource("arrow-green-right.png").toExternalForm()) : 
//                new ImageView(commandLinkImage);
//        Pane graphicContainer = new Pane(view);
//        graphicContainer.getStyleClass().add("graphic-container");
        ImageView arrow = new ImageView(CommandLinksDialog.class.getResource("arrow-green-right.png").toExternalForm());
        GridPane.setValignment(arrow, VPos.TOP);
        GridPane.setMargin(arrow, new Insets(0,10,0,0));

        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container");
        grid.add(arrow, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
//        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());

        return button;
    }    
}
