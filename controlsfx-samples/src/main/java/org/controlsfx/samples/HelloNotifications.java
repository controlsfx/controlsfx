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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.cell.ColorGridCell;

public class HelloNotifications extends ControlsFXSample {
    
    private static final Image SMALL_GRAPHIC = 
            new Image(HelloNotificationPane.class.getResource("controlsfx-logo.png").toExternalForm());
    
    private Stage stage;
    private Pane pane;
    
    private int count = 0;
    
    private CheckBox showTitleChkBox;
    private CheckBox showCloseButtonChkBox;
    private CheckBox darkStyleChkBox;
    private CheckBox ownerChkBox;
    private Slider fadeDelaySlider, thresholdSlider;
    protected String graphicMode = "";
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override public String getSampleName() {
        return "Notifications";
    }
    
    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/Notifications.html";
    }
    
    
    @Override
    public String getControlStylesheetURL() {
    	return "/org/controlsfx/control/notificationpopup.css";
    }
    
    @Override public Node getPanel(Stage stage) {
        this.stage = stage;
        
        pane = new Pane() {
            @Override protected void layoutChildren() {
                super.layoutChildren();
                updatePane();
            }
        };
        createPaneChildren();
        updatePane();
        pane.setPadding(new Insets(10));
        
        return pane;
    }
    
    private void createPaneChildren() {
        Button topLeftBtn = new Button("Top-left\nnotification");
        topLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_LEFT);
            }
        });
        
        Button topCenterBtn = new Button("Top-center\nnotification");
        topCenterBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_CENTER);
            }
        });
        
        Button topRightBtn = new Button("Top-right\nnotification");
        topRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.TOP_RIGHT);
            }
        });
        
        Button centerLeftBtn = new Button("Center-left\nNotification");
        centerLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.CENTER_LEFT);
            }
        });
        
        Button centerBtn = new Button("Center\nnotification");
        centerBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.CENTER);
            }
        });
        
        Button centerRightBtn = new Button("Center-right\nNotification");
        centerRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.CENTER_RIGHT);
            }
        });
        
        Button bottomLeftBtn = new Button("Bottom-left\nNotification");
        bottomLeftBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_LEFT);
            }
        });
        
        Button bottomCenterBtn = new Button("Bottom-center\nnotification");
        bottomCenterBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_CENTER);
            }
        });
        
        Button bottomRightBtn = new Button("Bottom-right\nNotification");
        bottomRightBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                notification(Pos.BOTTOM_RIGHT);
            }
        });
        
        pane.getChildren().addAll(topLeftBtn,    topCenterBtn,    topRightBtn,
                                  centerLeftBtn, centerBtn,       centerRightBtn,
                                  bottomLeftBtn, bottomCenterBtn, bottomRightBtn);
    }
    
    private void updatePane() {
        final double paneWidth = pane.getWidth();
        final double paneHeight = pane.getHeight();
        
        final double halfWidth = paneWidth / 2.0;
        final double halfHeight = paneHeight / 2.0;
        
        int row = 0;
        int col = 0;
        
        for (Node node : pane.getChildren()) {
            final double nodeWidth = node.prefWidth(-1);
            final double nodeHeight = node.prefHeight(-1);
            
            double layoutX = col == 0 ? 0 : 
                             col == 1 ? halfWidth - nodeWidth / 2.0 :
                           /*col == 2*/ paneWidth - nodeWidth;
            
            double layoutY = row == 0 ? 0 : 
                             row == 1 ? halfHeight - nodeHeight / 2.0 :
                           /*row == 2*/ paneHeight - nodeHeight;
            
            node.setLayoutX(layoutX);
            node.setLayoutY(layoutY);
            
            col++;
            if (col == 3) {
                row++;
                col = 0;
            }
        }
    }

    @Override public String getSampleDescription() {
        return "Unlike the NotificationPane, the Notifications class is designed to"
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
        
        // --- dark style
        Label darkStyleLabel = new Label("Use Dark Style: ");
        darkStyleLabel.getStyleClass().add("property");
        grid.add(darkStyleLabel, 0, row);
        darkStyleChkBox = new CheckBox();
        grid.add(darkStyleChkBox, 1, row++);
        
        // --- owner
        Label owner = new Label("Set Owner: ");
        owner.getStyleClass().add("property");
        grid.add(owner, 0, row);
        ownerChkBox = new CheckBox();
        grid.add(ownerChkBox, 1, row++);
        
        // --- graphic
        Label graphicLabel = new Label("Graphic Options: ");
        graphicLabel.getStyleClass().add("property");
        grid.add(graphicLabel, 0, row);
        final ChoiceBox<String> graphicOptions = new ChoiceBox<>(
                FXCollections.observableArrayList(
                        "No graphic", 
                        "Warning graphic",
                        "Information graphic",
                        "Confirm graphic",
                        "Error graphic",
                        "Custom graphic",
                        "Total-replacement graphic"));
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

        // threshold slider
        final Label thresholdLabel = new Label("Threshold: ");
        thresholdLabel.getStyleClass().add("property");
        grid.add(thresholdLabel, 0, row);
        thresholdSlider = new Slider(0, 10, 0);
        thresholdSlider.setMajorTickUnit(1);
        thresholdSlider.setMinorTickCount(0);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setSnapToTicks(true);
        thresholdSlider.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(thresholdSlider, Priority.ALWAYS);
        grid.add(thresholdSlider, 1, row++);

        return grid;
    }
    
    private void notification(Pos pos) {
        String text = "Hello World " + (count++) + "!";
        
        Node graphic = null;
        switch (graphicMode) {
            default:
            case "No graphic": 
            case "Warning graphic":
            case "Information graphic":
            case "Confirm graphic":
            case "Error graphic":
                break;
            case "Custom graphic":
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
                .hideAfter(Duration.seconds(fadeDelaySlider.getValue()))
                .position(pos)
                .onAction(e -> System.out.println("Notification clicked on!"))
                .threshold((int) thresholdSlider.getValue(),
                        Notifications.create().title("Threshold Notification"));

        if (ownerChkBox.isSelected()) {
            notificationBuilder.owner(stage);
        }

        if (!showCloseButtonChkBox.isSelected()) {
            notificationBuilder.hideCloseButton();
        }
        
        if (darkStyleChkBox.isSelected()) {
            notificationBuilder.darkStyle();
        }
        
        switch (graphicMode) {
            case "Warning graphic":     notificationBuilder.showWarning(); break;
            case "Information graphic": notificationBuilder.showInformation(); break;
            case "Confirm graphic":     notificationBuilder.showConfirm(); break;
            case "Error graphic":       notificationBuilder.showError(); break;
            default: notificationBuilder.show(); 
        }
    }
    
    private Node buildTotalReplacementGraphic() {
        final ObservableList<Color> list = FXCollections.<Color>observableArrayList();

        GridView<Color> colorGrid = new GridView<>(list);
        colorGrid.setPrefSize(300, 300);
        colorGrid.setMaxSize(300, 300);

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
