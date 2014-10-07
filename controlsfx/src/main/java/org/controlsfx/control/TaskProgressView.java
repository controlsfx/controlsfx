package org.controlsfx.control;

import impl.org.controlsfx.skin.TaskProgressViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class TaskProgressView extends Control {

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
                        for (Task<?> task : c.getAddedSubList()) {
                            task.removeEventHandler(WorkerStateEvent.ANY,
                                    taskHandler);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TaskProgressViewSkin(this);
    }

    private final ObservableList<Task<?>> tasks = FXCollections
            .observableArrayList();

    public final ObservableList<Task<?>> getTasks() {
        return tasks;
    }

    private final StringProperty title = new SimpleStringProperty(this,
            "title", "Tasks");

    public final StringProperty titleProperty() {
        return title;
    }

    public final void setTitle(String title) {
        titleProperty().set(title);
    }

    public final String getTitle() {
        return titleProperty().get();
    }

    private final BooleanProperty showTitle = new SimpleBooleanProperty(this,
            "showTitle", true);

    public final BooleanProperty showTitleProperty() {
        return showTitle;
    }

    public final void setShowTitle(boolean show) {
        showTitleProperty().set(show);
    }

    public final boolean isShowTitle() {
        return showTitleProperty().get();
    }

    private final BooleanProperty showCancelAllButton = new SimpleBooleanProperty(
            this, "showCancelAllButton", true);

    public final BooleanProperty showCancelAllButtonProperty() {
        return showCancelAllButton;
    }

    public final void setShowCancelAllButton(boolean show) {
        showCancelAllButtonProperty().set(show);
    }

    public final boolean isShowCancelAllButton() {
        return showCancelAllButtonProperty().get();
    }

    private final BooleanProperty showTaskCount = new SimpleBooleanProperty(
            this, "showTaskCount", true);

    public final BooleanProperty showTaskCountProperty() {
        return showTaskCount;
    }

    public final void setShowTaskCount(boolean show) {
        showTaskCountProperty().set(show);
    }

    public final boolean isShowTaskCount() {
        return showTaskCountProperty().get();
    }
}
