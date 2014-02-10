package impl.org.controlsfx.skin;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.ButtonBar;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

public abstract class NotificationBar extends Region {

    private static final double MIN_HEIGHT = 40;

    final Label label;
    ButtonBar actionsBar;
    final Button closeBtn;

    private final GridPane pane;
    
    DoubleProperty transition = new SimpleDoubleProperty() {
        @Override protected void invalidated() {
            requestContainerLayout();
        }
    };
    
    
    public abstract void requestContainerLayout();
    public abstract String getText();
    public abstract Node getGraphic();
    public abstract ObservableList<Action> getActions();
    public abstract void hide();
    public abstract boolean isShowing();
    public abstract boolean isShowFromTop();
    
    

    public NotificationBar() {
        pane = new GridPane();
        pane.getStyleClass().add("notification-bar");
        pane.setAlignment(Pos.BASELINE_LEFT);
        pane.setVisible(isShowing());
        getChildren().setAll(pane);

        // initialise label area
        label = new Label();
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setVgrow(label, Priority.ALWAYS);
        GridPane.setHgrow(label, Priority.ALWAYS);

        label.setText(getText());
        label.setGraphic(getGraphic());
        label.opacityProperty().bind(transition);

        // initialise actions area
        getActions().addListener(new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                updatePane();
            }
        });

        // initialise close button area
        closeBtn = new Button();
        closeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent arg0) {
                hide();
            }
        });
        closeBtn.getStyleClass().setAll("close-button");
        StackPane graphic = new StackPane();
        graphic.getStyleClass().setAll("graphic");
        closeBtn.setGraphic(graphic);
        closeBtn.setMinSize(17, 17);
        closeBtn.setPrefSize(17, 17);
        closeBtn.opacityProperty().bind(transition);
        GridPane.setMargin(closeBtn, new Insets(0, 0, 0, 8));

        // put it all together
        updatePane();
    }

    private void updatePane() {
        actionsBar = ActionUtils.createButtonBar(getActions());
        actionsBar.opacityProperty().bind(transition);
        GridPane.setHgrow(actionsBar, Priority.SOMETIMES);
        pane.getChildren().clear();
        pane.add(label, 0, 0);
        pane.add(actionsBar, 1, 0);
        pane.add(closeBtn, 2, 0);
    }

    @Override protected void layoutChildren() {
        final double w = getWidth();
        final double h = computePrefHeight(-1);
        pane.resize(w, h);
    }

    @Override protected double computeMinHeight(double width) {
        return super.computePrefHeight(width);
    }

    @Override protected double computePrefHeight(double width) {
        if (isShowFromTop()) {
            return MIN_HEIGHT; 
        } else {
            return Math.max(pane.prefHeight(width), MIN_HEIGHT) * transition.get();
        }
    }

    public void doShow() {
        transitionStartValue = 0;
        doAnimationTransition();
    }

    public void doHide() {
        transitionStartValue = 1;
        doAnimationTransition();
    }



    // --- animation timeline code
    private final Duration TRANSITION_DURATION = new Duration(350.0);
    private Timeline timeline;
    private double transitionStartValue;
    private void doAnimationTransition() {
        Duration duration;

        if (timeline != null && (timeline.getStatus() != Status.STOPPED)) {
            duration = timeline.getCurrentTime();

            // fix for #70 - the notification pane freezes up as it has zero
            // duration to expand / contract
            duration = duration == Duration.ZERO ? TRANSITION_DURATION : duration;
            transitionStartValue = transition.get();
            // --- end of fix

            timeline.stop();
        } else {
            duration = TRANSITION_DURATION;
        }

        timeline = new Timeline();
        timeline.setCycleCount(1);

        KeyFrame k1, k2;

        if (isShowing()) {
            k1 = new KeyFrame(
                    Duration.ZERO,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // start expand
                            pane.setCache(true);
                            pane.setVisible(true);

                            pane.fireEvent(new Event(NotificationPane.ON_SHOWING));
                        }
                    },
                    new KeyValue(transition, transitionStartValue)
                    );

            k2 = new KeyFrame(
                    duration,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // end expand
                            pane.setCache(false);

                            pane.fireEvent(new Event(NotificationPane.ON_SHOWN));
                        }
                    },
                    new KeyValue(transition, 1, Interpolator.EASE_OUT)

                    );
        } else {
            k1 = new KeyFrame(
                    Duration.ZERO,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // Start collapse
                            pane.setCache(true);

                            pane.fireEvent(new Event(NotificationPane.ON_HIDING));
                        }
                    },
                    new KeyValue(transition, transitionStartValue)
                    );

            k2 = new KeyFrame(
                    duration,
                    new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent event) {
                            // end collapse
                            pane.setCache(false);
                            pane.setVisible(false);

                            pane.fireEvent(new Event(NotificationPane.ON_HIDDEN));
                        }
                    },
                    new KeyValue(transition, 0, Interpolator.EASE_IN)
                    );
        }

        timeline.getKeyFrames().setAll(k1, k2);
        timeline.play();
    }
}