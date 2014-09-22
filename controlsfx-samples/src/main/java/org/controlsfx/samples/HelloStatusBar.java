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

import static javafx.geometry.Orientation.VERTICAL;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.StatusBar;

public class HelloStatusBar extends ControlsFXSample {
    private StatusBar statusBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override public String getSampleName() {
        return "StatusBar";
    }

    @Override public String getJavaDocURL() {
        return Utils.JAVADOC_BASE + "org/controlsfx/control/StatusBar.html";
    }

    @Override public Node getPanel(Stage stage) {
        statusBar = new StatusBar();

        BorderPane borderPane = new BorderPane();
        borderPane.setBottom(statusBar);

        return borderPane;
    }

    @Override public String getSampleDescription() {
        return "The StatusBar control can be used to display various application-specific status fields. This "
                + "can be plain text, the progress of a long running task, or any other type of information.";
    }

    @Override public Node getControlPanel() {
        VBox box = new VBox();
        box.setSpacing(10);

        TextField statusTextField = new TextField();
        statusTextField.setPromptText("Status Text");
        statusTextField.textProperty().bindBidirectional(statusBar.textProperty());

        box.getChildren().add(statusTextField);

        Button simulateTask = new Button("Start Task");
        simulateTask.setOnAction(evt -> startTask());
        box.getChildren().add(simulateTask);

        Button addLeftItem = new Button("Add Left Item");
        addLeftItem.setOnAction(evt -> addItem(true));
        box.getChildren().add(addLeftItem);

        Button addLeftSeparator = new Button("Add Left Separator");
        addLeftSeparator.setOnAction(evt -> addSeparator(true));
        box.getChildren().add(addLeftSeparator);

        Button addRightItem = new Button("Add Right Item");
        addRightItem.setOnAction(evt -> addItem(false));
        box.getChildren().add(addRightItem);

        Button addRightSeparator = new Button("Add Right Separator");
        addRightSeparator.setOnAction(evt -> addSeparator(false));
        box.getChildren().add(addRightSeparator);

        return box;
    }

    private int itemCounter;

    private void addItem(boolean left) {
        itemCounter++;
        Button button = new Button(Integer.toString(itemCounter));
        button.setBackground(new Background(new BackgroundFill(Color.ORANGE,
                new CornerRadii(2), new Insets(4))));
        if (left) {
            statusBar.getLeftItems().add(button);
        } else {
            statusBar.getRightItems().add(button);
        }
    }

    private void addSeparator(boolean left) {
        if (left) {
            statusBar.getLeftItems().add(new Separator(VERTICAL));
        } else {
            statusBar.getRightItems().add(new Separator(VERTICAL));
        }
    }

    private void startTask() {
        Task<Void> task = new Task<Void>() {
            @Override protected Void call() throws Exception {
                updateMessage("First we sleep ....");

                Thread.sleep(2500);

                int max = 100000000;
                for (int i = 0; i < max; i++) {
                    updateMessage("Message " + i);
                    updateProgress(i, max);
                }

                updateProgress(0, 0);
                done();
                return null;
            }
        };
        
        statusBar.textProperty().bind(task.messageProperty());
        statusBar.progressProperty().bind(task.progressProperty());
        
        // remove bindings again
        task.setOnSucceeded(event -> {
            statusBar.textProperty().unbind();    
            statusBar.progressProperty().unbind();
        });

        new Thread(task).start();
    }
}
