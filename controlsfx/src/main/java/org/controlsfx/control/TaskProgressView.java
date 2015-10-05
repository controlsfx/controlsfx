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
package org.controlsfx.control;

import impl.org.controlsfx.skin.TaskProgressViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.util.Callback;

/**
 * The task progress view is used to visualize the progress of long running
 * tasks. These tasks are created via the {@link Task} class. This view
 * manages a list of such tasks and displays each one of them with their
 * name, progress, and update messages.<p>
 * An optional graphic factory can be set to place a graphic in each row.
 * This allows the user to more easily distinguish between different types
 * of tasks.
 *
 * <h3>Screenshots</h3>
 * The picture below shows the default appearance of the task progress view
 * control:
 * <center><img src="task-monitor.png" alt="Screenshot of TaskProgressView"></center>
 *
 * <h3>Code Sample</h3>
 * <pre>
 * TaskProgressView&lt;MyTask&gt; view = new TaskProgressView&lt;&gt;();
 * view.setGraphicFactory(task -&gt; return new ImageView("db-access.png"));
 * view.getTasks().add(new MyTask());
 * </pre>
 */
public class TaskProgressView<T extends Task<?>> extends ControlsFXControl {

    /**
     * Constructs a new task progress view.
     */
    public TaskProgressView() {
        getStyleClass().add("task-progress-view");

        EventHandler<WorkerStateEvent> taskHandler = evt -> {
            if (evt.getEventType().equals(
                    WorkerStateEvent.WORKER_STATE_SUCCEEDED)
                    || evt.getEventType().equals(
                            WorkerStateEvent.WORKER_STATE_CANCELLED)
                    || evt.getEventType().equals(
                            WorkerStateEvent.WORKER_STATE_FAILED)) {
                getTasks().remove(evt.getSource());
            }
        };

        getTasks().addListener(new ListChangeListener<Task<?>>() {
            @Override
            public void onChanged(Change<? extends Task<?>> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        for (Task<?> task : c.getAddedSubList()) {
                            task.addEventHandler(WorkerStateEvent.ANY,
                                    taskHandler);
                        }
                    } else if (c.wasRemoved()) {
                        for (Task<?> task : c.getRemoved()) {
                            task.removeEventHandler(WorkerStateEvent.ANY,
                                    taskHandler);
                        }
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(TaskProgressView.class, "taskprogressview.css");
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TaskProgressViewSkin<>(this);
    }

    private final ObservableList<T> tasks = FXCollections
            .observableArrayList();

    /**
     * Returns the list of tasks currently monitored by this view.
     *
     * @return the monitored tasks
     */
    public final ObservableList<T> getTasks() {
        return tasks;
    }

    private ObjectProperty<Callback<T, Node>> graphicFactory;

    /**
     * Returns the property used to store an optional callback for creating
     * custom graphics for each task.
     *
     * @return the graphic factory property
     */
    public final ObjectProperty<Callback<T, Node>> graphicFactoryProperty() {
        if (graphicFactory == null) {
            graphicFactory = new SimpleObjectProperty<Callback<T, Node>>(
                    this, "graphicFactory");
        }

        return graphicFactory;
    }

    /**
     * Returns the value of {@link #graphicFactoryProperty()}.
     *
     * @return the optional graphic factory
     */
    public final Callback<T, Node> getGraphicFactory() {
        return graphicFactory == null ? null : graphicFactory.get();
    }

    /**
     * Sets the value of {@link #graphicFactoryProperty()}.
     *
     * @param factory an optional graphic factory
     */
    public final void setGraphicFactory(Callback<T, Node> factory) {
        graphicFactoryProperty().set(factory);
    }
}
