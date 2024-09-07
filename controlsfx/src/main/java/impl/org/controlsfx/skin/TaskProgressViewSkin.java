/**
 * Copyright (c) 2014, 2015 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
package impl.org.controlsfx.skin;

import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import org.controlsfx.control.TaskProgressView;

import java.util.function.Consumer;

public class TaskProgressViewSkin<T extends Task<?>> extends
        SkinBase<TaskProgressView<T>> {

    private Boolean retainTasks;
    public TaskProgressViewSkin(TaskProgressView<T> monitor) {
        super(monitor);
        retainTasks = monitor.isRetainTasks();

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("box");

        // list view
        ListView<T> listView = new ListView<>();
        listView.setPrefSize(500, 400);
        listView.setPlaceholder(new Label("No tasks running"));

        if (retainTasks) {
            listView.setCellFactory(param -> new TaskCell().withRemoveFinishedTaskButton(task -> {
                        if (task.getState() == Worker.State.FAILED || task.getState() == Worker.State.SUCCEEDED || task.getState() == Worker.State.CANCELLED) {
                            monitor.getTasks().remove(task);
                        }
                    }
            ));
        } else {
            listView.setCellFactory(param -> new TaskCell());
        }
        listView.setFocusTraversable(false);

        Bindings.bindContent(listView.getItems(), monitor.getTasks());
        borderPane.setCenter(listView);

        getChildren().add(listView);
    }

    class TaskCell extends ListCell<T> {
        private final ProgressBar progressBar;
        private final Label titleText;
        private final Label messageText;
        private final Button cancelButton;
        private Button removeTaskButton;
        private final VBox buttonsContainer;

        private T task;
        private final BorderPane borderPane;

        public TaskCell() {
            titleText = new Label();
            titleText.getStyleClass().add("task-title");

            messageText = new Label();
            messageText.getStyleClass().add("task-message");

            progressBar = new ProgressBar();
            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setMaxHeight(8);
            progressBar.getStyleClass().add("task-progress-bar");

            cancelButton = new Button("Cancel");
            cancelButton.setAlignment(Pos.CENTER);
            cancelButton.getStyleClass().add("task-cancel-button");
            cancelButton.setTooltip(new Tooltip("Cancel Task"));
            cancelButton.setOnAction(evt -> {
                if (task != null) {
                    task.cancel();
                }
            });

            VBox vbox = new VBox();
            vbox.setSpacing(4);
            vbox.getChildren().add(titleText);
            vbox.getChildren().add(progressBar);
            vbox.getChildren().add(messageText);

            borderPane = new BorderPane();
            borderPane.setCenter(vbox);

            buttonsContainer = new VBox();
            buttonsContainer.prefHeightProperty().bind(progressBar.prefHeightProperty());
            buttonsContainer.setAlignment(Pos.CENTER);
            buttonsContainer.getChildren().add(cancelButton);
            borderPane.setRight(buttonsContainer);
            BorderPane.setAlignment(buttonsContainer, Pos.BOTTOM_CENTER);
            BorderPane.setMargin(buttonsContainer, new Insets(0, 0, 0, 4));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        private TaskCell withRemoveFinishedTaskButton(Consumer<Task<?>> onButtonPress) {
            Button customButton = getCustomRemoveFinishedTaskButton();
            customButton.setOnAction(evt -> onButtonPress.accept(task));
            buttonsContainer.getChildren().add(removeTaskButton);
            return this;
        }

        /**
         * Customize the button for removing finished tasks manually. Can be used in conjunction with retain task property
         *
         * @return Button
         */
        protected Button getCustomRemoveFinishedTaskButton() {
            removeTaskButton = new Button("X");
            removeTaskButton.setAlignment(Pos.CENTER);
            removeTaskButton.getStyleClass().add("task-remove-finished-button");
            removeTaskButton.setTooltip(new Tooltip("Remove finished"));

            return removeTaskButton;
        }

        @Override
        public void updateIndex(int index) {
            super.updateIndex(index);

            /*
             * I have no idea why this is necessary but it won't work without
             * it. Shouldn't the updateItem method be enough?
             */
            if (index == -1) {
                setGraphic(null);
                getStyleClass().setAll("task-list-cell-empty");
            }
        }

        @Override
        protected void updateItem(T task, boolean empty) {
            super.updateItem(task, empty);

            this.task = task;

            if (empty || task == null) {
                getStyleClass().setAll("task-list-cell-empty");
                setGraphic(null);
            } else {
                getStyleClass().setAll("task-list-cell");
                progressBar.progressProperty().bind(task.progressProperty());
                titleText.textProperty().bind(task.titleProperty());
                messageText.textProperty().bind(task.messageProperty());

                if (retainTasks) {
                    cancelButton.visibleProperty().bind(task.runningProperty());
                    cancelButton.managedProperty().bind(cancelButton.visibleProperty());

                    removeTaskButton.visibleProperty().bind(Bindings.not(task.runningProperty()));
                    removeTaskButton.managedProperty().bind(removeTaskButton.visibleProperty());
                } else {
                    cancelButton.disableProperty().bind(
                            Bindings.not(task.runningProperty()));
                }

                Callback<T, Node> factory = getSkinnable().getGraphicFactory();
                if (factory != null) {
                    Node graphic = factory.call(task);
                    if (graphic != null) {
                        BorderPane.setAlignment(graphic, Pos.CENTER);
                        BorderPane.setMargin(graphic, new Insets(0, 4, 0, 0));
                        borderPane.setLeft(graphic);
                    }
                } else {
                    /*
                     * Really needed. The application might have used a graphic
                     * factory before and then disabled it. In this case the border
                     * pane might still have an old graphic in the left position.
                     */
                    borderPane.setLeft(null);
                }

                setGraphic(borderPane);
            }
        }
    }
}
