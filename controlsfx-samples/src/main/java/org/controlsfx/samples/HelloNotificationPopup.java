/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.samples;

import java.util.Random;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.NotificationPopup;
import org.controlsfx.control.NotificationPopup.Notification;
import org.controlsfx.control.NotificationPopup.Notifications;
import org.controlsfx.control.cell.ColorGridCell;


public class HelloNotificationPopup extends ControlsFXSample {
    
    private static final Image SMALL_GRAPHIC = 
            new Image(HelloNotificationPane.class.getResource("notification-pane-warning.png").toExternalForm());
    
    private Stage stage;
    private NotificationPopup notifier;
    
    private int count = 0;
    
    private CheckBox showTitleChkBox;
    private CheckBox showCloseButtonChkBox;
    private Slider fadeDelaySlider;
    protected String graphicMode = "";
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "NotificationPopup";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/NotificationPopup.html";
    }
    
    @Override public Node getPanel(Stage stage) {
        this.stage = stage;
        this.notifier = new NotificationPopup();
        
        Button topLeftBtn = new Button("Top-left\nnotification");
        topLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_LEFT);
            }
        });
        AnchorPane.setTopAnchor(topLeftBtn, 0.0);
        AnchorPane.setLeftAnchor(topLeftBtn, 0.0);
        
        Button topRightBtn = new Button("Top-right\nnotification");
        topRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_RIGHT);
            }
        });
        AnchorPane.setTopAnchor(topRightBtn, 0.0);
        AnchorPane.setRightAnchor(topRightBtn, 0.0);
        
        Button bottomLeftBtn = new Button("Bottom-left\nNotification");
        bottomLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_LEFT);
            }
        });
        AnchorPane.setBottomAnchor(bottomLeftBtn, 0.0);
        AnchorPane.setLeftAnchor(bottomLeftBtn, 0.0);
        
        Button bottomRightBtn = new Button("Bottom-right\nNotification");
        bottomRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_RIGHT);
            }
        });
        AnchorPane.setBottomAnchor(bottomRightBtn, 0.0);
        AnchorPane.setRightAnchor(bottomRightBtn, 0.0);
        
        AnchorPane pane = new AnchorPane(topLeftBtn, topRightBtn, bottomLeftBtn, bottomRightBtn);
        
        return pane;
    }
    
    @Override public String getSampleDescription() {
        return "Unlike the NotificationPane, the NotificationPopup is designed to"
                + "show popup warnings outside your application.";
    }
    
    @Override public Node getControlPanel() {
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.setPadding(new Insets(30, 30, 0, 30));
        
        int row = 0;
        
        // --- show title
        Label showTitleLabel = new Label("Show Title: ");
        showTitleLabel.getStyleClass().add("property");
        grid.add(showTitleLabel, 0, row);
        showTitleChkBox = new CheckBox();
        showTitleChkBox.setSelected(true);
        grid.add(showTitleChkBox, 1, row++);
        
        // --- show close button
        Label showCloseButtonLabel = new Label("Show Close Button: ");
        showCloseButtonLabel.getStyleClass().add("property");
        grid.add(showCloseButtonLabel, 0, row);
        showCloseButtonChkBox = new CheckBox();
        showCloseButtonChkBox.setSelected(true);
        grid.add(showCloseButtonChkBox, 1, row++);
        
        // --- graphic
        Label graphicLabel = new Label("Graphic Options: ");
        graphicLabel.getStyleClass().add("property");
        grid.add(graphicLabel, 0, row);
        final ChoiceBox<String> graphicOptions = new ChoiceBox<>(
                FXCollections.observableArrayList("No graphic", "Small graphic", "Total-replacement graphic"));
        graphicOptions.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(graphicOptions, Priority.ALWAYS);
        final SelectionModel<String> sm = graphicOptions.getSelectionModel();
        sm.selectedItemProperty().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable o) {
                graphicMode = sm.getSelectedItem();
            }
        });
        sm.select(1);
        grid.add(graphicOptions, 1, row++);
        
        // --- fade duration
        Label fadeDurationLabel = new Label("Fade delay (seconds): ");
        fadeDurationLabel.getStyleClass().add("property");
        grid.add(fadeDurationLabel, 0, row);
        fadeDelaySlider = new Slider(1, 20, 5);
        fadeDelaySlider.setShowTickMarks(true);
        fadeDelaySlider.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(fadeDelaySlider, Priority.ALWAYS);
        grid.add(fadeDelaySlider, 1, row++);
        
        
        return grid;
    }
    
    private void notification(Pos pos) {
        String text = "Hello World " + (count++) + "!";
        
        Node graphic = null;
        switch (graphicMode) {
            default:
            case "No graphic": 
                break;
            case "Small graphic":
                graphic = new ImageView(SMALL_GRAPHIC);
                break;
            case "Total-replacement graphic": 
                text = null;
                graphic = buildTotalReplacementGraphic();
                break;
        }
        
        Notifications notificationBuilder = Notifications.create()
                .title(showTitleChkBox.isSelected() ? "Title Text" : "")
                .text(text)
                .graphic(graphic)
                .fadeAfter(Duration.seconds(fadeDelaySlider.getValue()))
                .position(pos);
        
        if (! showCloseButtonChkBox.isSelected()) {
            notificationBuilder.hideCloseButton();
        }
        
        Notification actualNotification = notificationBuilder.build();
        notifier.show(stage, actualNotification);
    }
    
    private Node buildTotalReplacementGraphic() {
        final ObservableList<Color> list = FXCollections.<Color>observableArrayList();

        GridView<Color> colorGrid = new GridView<>(list);

        colorGrid.setCellFactory(new Callback<GridView<Color>, GridCell<Color>>() {
            @Override public GridCell<Color> call(GridView<Color> arg0) {
                return new ColorGridCell();
            }
        });
        Random r = new Random(System.currentTimeMillis());
        for(int i = 0; i < 500; i++) {
            list.add(new Color(r.nextDouble(), r.nextDouble(), r.nextDouble(), 1.0));
        }
        return colorGrid;
    }
}
