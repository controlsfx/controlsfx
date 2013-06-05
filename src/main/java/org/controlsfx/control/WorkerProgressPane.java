package org.controlsfx.control;

import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * The WorkerProgressPane is automatically shown or hidden based on the status of the
 * Worker that is associated with it. You can specify the ProgressIndicator to be used,
 * as well as what kind of a delay the WorkerProgressPane should have before being shown,
 * and the speed at which it should be shown / hidden.
 *
 * The WorkerProgressPane will listen to the Worker as it changes state. When the Worker enters
 * the running state, the pane will wait for some short (configurable) period of time to determine
 * what the rate of progress is, and to figure out whether the task will be complete before the
 * (configurable) showTime is specified. When it is triggered, it will show the pane (it must have
 * previously been added to the parent) and when either the worker succeeds, or fails, or is cancelled,
 * the WorkerProgressPane will hide itself.
 */
public class WorkerProgressPane extends Region {
    private ObjectProperty<Duration> delay = new SimpleObjectProperty<>(this, "delay", Duration.seconds(.5));
    public final Duration getDelay() { return delay.get(); }
    public final void setDelay(Duration value) { delay.set(value); }
    public final ObjectProperty<Duration> delayProperty() { return delay; }

    private ObjectProperty<Duration> fadeInTime = new SimpleObjectProperty<>(this, "fadeInTime", Duration.seconds(1));
    public final Duration getFadeInTime() { return fadeInTime.get(); }
    public final void setFadeInTime(Duration value) { fadeInTime.set(value); }
    public final ObjectProperty<Duration> fadeInTimeProperty() { return fadeInTime; }

    private ObjectProperty<Duration> fadeOutTime = new SimpleObjectProperty<>(this, "fadeOutTime", Duration.seconds(1));
    public final Duration getFadeOutTime() { return fadeOutTime.get(); }
    public final void setFadeOutTime(Duration value) { fadeOutTime.set(value); }
    public final ObjectProperty<Duration> fadeOutTimeProperty() { return fadeOutTime; }

    private ChangeListener<Worker.State> stateListener = new ChangeListener<Worker.State>() {
        @Override public void changed(ObservableValue<? extends State> observable, State old, State value) {
            switch(value) {
                case CANCELLED:
                case FAILED:
                case SUCCEEDED:
                    end();
                    break;
                case SCHEDULED:
                    begin();
                    break;
            }
        }
    };
    private ObjectProperty<Worker> worker = new SimpleObjectProperty<Worker>(this, "worker") {
        private Worker old;
        @Override protected void invalidated() {
            Worker n = get();
            if (n != old) {
                if (old != null) {
                    old.stateProperty().removeListener(stateListener);
                    end();
                }
                if (n != null) {
                    n.stateProperty().addListener(stateListener);
                    if (n.getState() == Worker.State.RUNNING || n.getState() == Worker.State.SCHEDULED) {
                        // It is already running
                        begin();
                    }
                }
                old = n;
            }
        }
    };
    public final Worker getWorker() { return worker.get(); }
    public final void setWorker(Worker value) { worker.set(value); }
    public final ObjectProperty<Worker> workerProperty() { return worker; }

    // TODO SimpleObjectProperty constructor wouldn't take a ProgressBar, but should have.
    private ObjectProperty<ProgressIndicator> progressIndicator = new SimpleObjectProperty<ProgressIndicator>(this, "progressIndicator") {
        private ProgressIndicator old;

        @Override protected void invalidated() {
            ProgressIndicator n = get();
            if (n != old) {
                if (old != null) {
                    old.progressProperty().unbind();
                    getChildren().remove(old);
                }
                if (n != null && worker.get() != null) {
                    n.progressProperty().bind(worker.get().progressProperty());
                }
                if (n != null) {
                    getChildren().add(n);
                }
                old = n;
            }
        }
    };
    { progressIndicator.set(new ProgressBar()); }
    public final ProgressIndicator getProgressIndicator() { return progressIndicator.get(); }
    public final void setProgressIndicator(ProgressIndicator value) { progressIndicator.set(value); }
    public final ObjectProperty<ProgressIndicator> progressIndicatorProperty() { return progressIndicator; }

    // If the progress indicator changes, then we need to re-initialize
    // If the worker changes, we need to re-initialize

    // We don't care about changes to fadeInTime or fadeOutTime -- whatever the value is at the time I need it,
    // is what I will end up using. Same goes for delay.

    private FadeTransition fadeInTx;
    private FadeTransition fadeOutTx;

    {
        setVisible(false);
        setOpacity(0);
        setBackground(new Background(new BackgroundFill(Color.color(0, 0, 0, .8), null, null)));
    }

    private void begin() {
        final Worker w = worker.get();
        final ProgressIndicator p = progressIndicator.get();
        p.progressProperty().bind(w.progressProperty());

        if (fadeOutTx != null) {
            fadeOutTx.stop();
            fadeOutTx = null;
        }

        final Duration d = delay.get() == null ? Duration.ZERO : delay.get();
        final Duration fadeTime = fadeInTime.get() == null ? Duration.ZERO : fadeInTime.get();

        if (!isVisible()) {
            setVisible(true);
            setOpacity(0);
        }

        fadeInTx = new FadeTransition(fadeTime, this);
        fadeInTx.setFromValue(0);
        fadeInTx.setToValue(1);
        if (getOpacity() > 0) {
            // skip the delay and adjust the current time of this animation
            fadeInTx.jumpTo(fadeTime.multiply(getOpacity()));
        } else {
            fadeInTx.setDelay(d);
        }
        fadeInTx.play();
    }

    private void end() {
        final ProgressIndicator p = progressIndicator.get();
        p.progressProperty().unbind();

        if (fadeInTx != null) {
            fadeInTx.stop();
            fadeInTx = null;
        }

        final Duration fadeTime = fadeOutTime.get() == null ? Duration.ZERO : fadeOutTime.get();

        fadeOutTx = new FadeTransition(fadeTime, this);
        fadeOutTx.setFromValue(1);
        fadeOutTx.setToValue(0);
        if (getOpacity() < 1) {
            // adjust
            fadeOutTx.jumpTo(fadeTime.multiply(1 - getOpacity()));
        }
        fadeOutTx.setOnFinished(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                setVisible(false);
            }
        });
        fadeOutTx.play();
    }

    @Override protected void layoutChildren() {
        final ProgressIndicator p = progressIndicator.get();
        if (p != null) {
            Insets insets = getInsets();
            double w = getWidth() - insets.getLeft() - insets.getRight();
            double h = getHeight() - insets.getTop() - insets.getBottom();

            double prefW = p.prefWidth(-1);
            double prefH = p.prefHeight(-1);
            double x = insets.getLeft() + (w - prefW) / 2.0;
            double y = insets.getTop() + (h - prefH) / 2.0;

            p.resizeRelocate(x, y, prefW, prefH);
        }
    }
}
