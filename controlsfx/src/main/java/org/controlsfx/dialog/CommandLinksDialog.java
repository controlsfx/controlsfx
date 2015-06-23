/**
 * Copyright (c) 2014, 2015 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.getString;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.DoubleBinding;
import javafx.collections.ListChangeListener;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class CommandLinksDialog extends Dialog<ButtonType> {
    
    public static class CommandLinksButtonType {
        private final ButtonType buttonType;
        private final String longText;
        private final Node graphic;
        private boolean isHidden = false;
        
        public CommandLinksButtonType(String text, boolean isDefault ) {
            this(new ButtonType(text, buildButtonData(isDefault)), null);
        }
        
        public CommandLinksButtonType(String text, String longText, boolean isDefault) {
            this(new ButtonType(text, buildButtonData(isDefault)), longText, null);
        }
        
        public CommandLinksButtonType(String text, String longText, Node graphic, boolean isDefault) {
            this(new ButtonType(text, buildButtonData(isDefault)), longText, graphic);
        }
        
        private CommandLinksButtonType(ButtonType buttonType) {
            this(buttonType, null);
        }
        
        private CommandLinksButtonType(ButtonType buttonType, String longText) {
            this(buttonType, longText, null);
        }
        
        private CommandLinksButtonType(ButtonType buttonType, String longText, Node graphic) {
            this.buttonType = buttonType;
            this.longText = longText;
            this.graphic = graphic;
            
        }
        
        private static ButtonData buildButtonData( boolean isDeafault) {
        	return isDeafault? ButtonData.OK_DONE :ButtonData.OTHER;
        }
        
        private static CommandLinksButtonType buildHiddenCancelLink() {
            CommandLinksButtonType link = new CommandLinksButtonType(new ButtonType("",ButtonData.CANCEL_CLOSE));
            link.isHidden = true;
            return link;
        }
        
        public ButtonType getButtonType() {
            return buttonType;
        }
        
        public Node getGraphic() {
            return graphic;
        }
        
        public String getLongText() {
            return longText;
        }
    }
    
    
    private final static int gapSize = 10;
    
    private final Map<ButtonType, CommandLinksButtonType> typeMap;
    
    private Label contentTextLabel;
    
    private GridPane grid = new GridPane() {
        @Override protected double computePrefWidth(double height) {
            boolean isDefault = true;
            double pw = 0;

            for (ButtonType buttonType : getDialogPane().getButtonTypes()) {
                Button button = (Button) getDialogPane().lookupButton(buttonType);
                double buttonPrefWidth = button.getGraphic().prefWidth(-1);
                
                if (isDefault) {
                    pw = buttonPrefWidth;
                    isDefault = false;
                } else {
                    pw = Math.min(pw, buttonPrefWidth);
                }
            }
            return pw + gapSize;
        }

        @Override protected double computePrefHeight(double width) {
            double ph = getDialogPane().getHeader() == null ? 0 : 10;

            for (ButtonType buttonType : getDialogPane().getButtonTypes()) {
                Button button = (Button) getDialogPane().lookupButton(buttonType);
                ph += button.prefHeight(width) + gapSize;
            }
            
            // TODO remove magic number
            return ph * 1.2;
        }
    };
    
    public CommandLinksDialog(CommandLinksButtonType... links) {
        this(Arrays.asList(links));
    }
    
    public CommandLinksDialog(List<CommandLinksButtonType> links) {
        this.grid.setHgap(gapSize);
        this.grid.setVgap(gapSize);
        this.grid.getStyleClass().add("container"); //$NON-NLS-1$
        
        final DialogPane dialogPane = new DialogPane() {
            @Override protected Node createButtonBar() {
                return null;
            }
            
            @Override protected Node createButton(ButtonType buttonType) {
                return createCommandLinksButton(buttonType);
            }
        }; 
        setDialogPane(dialogPane);
        
        setTitle(getString("Dialog.info.title")); //$NON-NLS-1$
        dialogPane.getStyleClass().add("command-links-dialog"); //$NON-NLS-1$
        dialogPane.getStylesheets().add(getClass().getResource("dialogs.css").toExternalForm()); //$NON-NLS-1$
        dialogPane.getStylesheets().add(getClass().getResource("commandlink.css").toExternalForm()); //$NON-NLS-1$
        
        // create a map from ButtonType -> CommandLinkButtonType, and put the 
        // ButtonType values into the dialog pane
        
        typeMap = new HashMap<>();
        for (CommandLinksButtonType link : links) { 
            addLinkToDialog(dialogPane,link);
        }
        addLinkToDialog(dialogPane,CommandLinksButtonType.buildHiddenCancelLink());
        
        updateGrid();
        dialogPane.getButtonTypes().addListener((ListChangeListener<? super ButtonType>)c -> updateGrid());
        
        contentTextProperty().addListener(o -> updateContentText());
    }
    
    private void addLinkToDialog(DialogPane dialogPane, CommandLinksButtonType link) {
         typeMap.put(link.getButtonType(), link); 
         dialogPane.getButtonTypes().add(link.getButtonType()); 
    }
    
    private void updateContentText() {
        String contentText = getDialogPane().getContentText();
        grid.getChildren().remove(contentTextLabel);
        if (contentText != null && ! contentText.isEmpty()) {
            if (contentTextLabel != null) {
                contentTextLabel.setText(contentText);
            } else {
                contentTextLabel = new Label(getDialogPane().getContentText());
                contentTextLabel.getStyleClass().add("command-link-message"); //$NON-NLS-1$
            }
            grid.add(contentTextLabel, 0, 0);
        }
    }
    
    private void updateGrid() {
        grid.getChildren().clear();
        
        // add the message to the top of the dialog
        updateContentText();
        
        // then build all the buttons
        int row = 1;
        for (final ButtonType buttonType : getDialogPane().getButtonTypes()) {
            if (buttonType == null) continue; 

            final Button button = (Button)getDialogPane().lookupButton(buttonType);   

            GridPane.setHgrow(button, Priority.ALWAYS);
            GridPane.setVgrow(button, Priority.ALWAYS);
            grid.add(button, 0, row++);
        }

//        // last button gets some extra padding (hacky)
//        GridPane.setMargin(buttons.get(buttons.size() - 1), new Insets(0,0,10,0));

        getDialogPane().setContent(grid);
        getDialogPane().requestLayout();
    }
    
    private Button createCommandLinksButton(ButtonType buttonType) {
        // look up the CommandLinkButtonType for the given ButtonType
        CommandLinksButtonType commandLink = typeMap.getOrDefault(buttonType, new CommandLinksButtonType(buttonType));
        
        
        // put the content inside a button
        final Button button = new Button();
        button.getStyleClass().addAll("command-link-button"); //$NON-NLS-1$
        button.setMaxHeight(Double.MAX_VALUE);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        final ButtonData buttonData = buttonType.getButtonData();
        button.setDefaultButton(buttonData != null && buttonData.isDefaultButton());
        button.setOnAction(ae -> setResult(buttonType));

        final Label titleLabel = new Label(commandLink.getButtonType().getText() );
        titleLabel.minWidthProperty().bind(new DoubleBinding() {
            {
                bind(titleLabel.prefWidthProperty());
            }

            @Override protected double computeValue() {
                return titleLabel.getPrefWidth() + 400;
            }
        });
        titleLabel.getStyleClass().addAll("line-1"); //$NON-NLS-1$
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        GridPane.setVgrow(titleLabel, Priority.NEVER);

        Label messageLabel = new Label(commandLink.getLongText() );
        messageLabel.getStyleClass().addAll("line-2"); //$NON-NLS-1$
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(messageLabel, Priority.SOMETIMES);

        Node commandLinkImage = commandLink.getGraphic();
        Node view = commandLinkImage == null ? 
                new ImageView(CommandLinksDialog.class.getResource("arrow-green-right.png").toExternalForm()) :  //$NON-NLS-1$
                commandLinkImage;
        Pane graphicContainer = new Pane(view);
        graphicContainer.getStyleClass().add("graphic-container"); //$NON-NLS-1$
        GridPane.setValignment(graphicContainer, VPos.TOP);
        GridPane.setMargin(graphicContainer, new Insets(0,10,0,0));

        GridPane grid = new GridPane();
        grid.minWidthProperty().bind(titleLabel.prefWidthProperty());
        grid.setMaxHeight(Double.MAX_VALUE);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.getStyleClass().add("container"); //$NON-NLS-1$
        grid.add(graphicContainer, 0, 0, 1, 2);
        grid.add(titleLabel, 1, 0);
        grid.add(messageLabel, 1, 1);

        button.setGraphic(grid);
        button.minWidthProperty().bind(titleLabel.prefWidthProperty());
        
        if (commandLink.isHidden) {
            button.setVisible(false);
            button.setPrefHeight(1);
        }
        return button;
    }    
}
