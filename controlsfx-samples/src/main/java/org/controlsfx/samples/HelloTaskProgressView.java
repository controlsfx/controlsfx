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

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.FontAwesome.Glyph;

public class HelloTaskProgressView extends ControlsFXSample {

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private TaskProgressView<MyTask> taskProgressView;

    private FontAwesome fontAwesome = new FontAwesome();

    private Callback<MyTask, Node> factory;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public String getSampleName() {
        return "TaskProgressView";
    }

    @Override
    public String getJavaDocURL() {
        return Utils.JAVADOC_BASE
                + "org/controlsfx/control/TaskProgressView.html";
    }

    @Override
    public Node getPanel(Stage stage) {
        taskProgressView = new TaskProgressView<MyTask>();

        factory = task -> {

            org.controlsfx.glyphfont.Glyph result = null;
            switch (task.getType()) {
            case TYPE1:
                result = fontAwesome.create(Glyph.MOBILE_PHONE).size(24)
                        .color(Color.RED);
                break;
            case TYPE2:
                result = fontAwesome.create(Glyph.COMPASS).size(24)
                        .color(Color.GREEN);
                break;
            case TYPE3:
                result = fontAwesome.create(Glyph.APPLE).size(24)
                        .color(Color.BLUE);
                break;
            default:
            }

            if (result != null) {
                result.setEffect(new DropShadow(8, Color.GRAY));
                result.setAlignment(Pos.CENTER);

                /*
                 * We have to make sure all glyps have the same size. Otherwise
                 * the progress cells will not be aligned properly.
                 */
                result.setPrefSize(24, 24);
            }

            return result;
        };

        StackPane stackPane = new StackPane();
        stackPane.setStyle("-fx-border-color: black; -fx-border-insets: 40;");
        stackPane.getChildren().add(taskProgressView);
        StackPane.setAlignment(taskProgressView, Pos.CENTER);

        return stackPane;
    }

    @Override
    public String getSampleDescription() {
        return "The task progress view lists running tasks and displays their progress and status. "
                + "The view can have an optional title, a 'cancel all' button and a task counter.";
    }

    @Override
    public Node getControlPanel() {
        VBox box = new VBox();
        box.setSpacing(10);

        Button startTask = new Button("Start Task");
        startTask.setOnAction(evt -> startTask());
        box.getChildren().add(startTask);

        CheckBox useFactory = new CheckBox("Use Graphics Factory");
        useFactory.setOnAction(evt -> {
            /*
             * Cancel all tasks before changing the factory.
             */
            (new ArrayList<>(taskProgressView.getTasks())).forEach(task -> task.cancel());
            if (useFactory.isSelected()) {
                taskProgressView.setGraphicFactory(factory);
            } else {
                taskProgressView.setGraphicFactory(null);
            }
        });

        box.getChildren().add(useFactory);

        return box;
    }

    private int taskCounter;

    private void startTask() {
        taskCounter++;

        MyTask task = new MyTask("Task #" + taskCounter);

        // add to the UI
        taskProgressView.getTasks().add(task);

        // execute task
        executorService.submit(task);
    }

    enum TaskType {
        TYPE1, TYPE2, TYPE3;
    }

    class MyTask extends Task<Void> {
        private TaskType type;

        public MyTask(String title) {
            updateTitle(title);

            type = TaskType.values()[(int) (Math.random() * 3)];
        }

        public TaskType getType() {
            return type;
        }

        @Override
        protected Void call() throws Exception {

            if (Math.random() < .3) {
                updateMessage("First we sleep ....");
                Thread.sleep(2500);
            }

            int max = 10000000;
            for (int i = 0; i < max; i++) {
                if (isCancelled()) {
                    break;
                }
                updateMessage("Message " + i);
                updateProgress(i, max);
            }

            updateProgress(0, 0);
            done();
            return null;
        }
    }
}
