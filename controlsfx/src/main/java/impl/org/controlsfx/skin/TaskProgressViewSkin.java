package impl.org.controlsfx.skin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import org.controlsfx.control.TaskProgressView;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;

import com.sun.javafx.css.StyleManager;

public class TaskProgressViewSkin extends SkinBase<TaskProgressView> {

    static {
        StyleManager.getInstance().addUserAgentStylesheet(
                TaskProgressView.class
                        .getResource("taskmonitor.css").toExternalForm()); //$NON-NLS-1$
    }

    private static FontAwesome fontAwesome = new FontAwesome();

    public TaskProgressViewSkin(TaskProgressView monitor) {
        super(monitor);

        BorderPane borderPane = new BorderPane();
        borderPane.getStyleClass().add("box");

        // list view
        ListView<Task<?>> listView = new ListView<>();
        listView.setPrefSize(500, 400);
        listView.setPlaceholder(new Label("No tasks running"));
        listView.setCellFactory(param -> new TaskCell());
        listView.setFocusTraversable(false);
        // listView.setStyle("-fx-background-color: transparent");

        Bindings.bindContent(listView.getItems(), monitor.getTasks());
        borderPane.setCenter(listView);

        // title
        Label title = new Label();
        title.textProperty().bind(monitor.titleProperty());
        title.visibleProperty().bind(monitor.showTitleProperty());
        title.setTextAlignment(TextAlignment.CENTER);
        title.setMaxWidth(Double.MAX_VALUE);
        borderPane.setTop(title);

        // cancel all button
        Button cancelButton = new Button("Cancel All");
        cancelButton.getStyleClass().add("cancel-all-button");
        BorderPane.setAlignment(cancelButton, Pos.CENTER_RIGHT);
        BorderPane.setMargin(cancelButton, new Insets(10));
        cancelButton.visibleProperty().bind(
                monitor.showCancelAllButtonProperty());
        cancelButton.setOnAction(evt -> {
            List<Task<?>> copy = new ArrayList<>(monitor.getTasks());
            for (Task<?> task : copy) {
                task.cancel();
            }
        });

        BorderPane footerPane = new BorderPane();
        footerPane.visibleProperty().bind(
                Bindings.or(monitor.showCancelAllButtonProperty(),
                        monitor.showTaskCountProperty()));
        footerPane.setRight(cancelButton);

        final Label countLabel = new Label();
        countLabel.visibleProperty().bind(
                Bindings.and(monitor.showTaskCountProperty(),
                        Bindings.isNotEmpty(monitor.getTasks())));

        BorderPane.setMargin(countLabel, new Insets(10, 10, 10, 30));
        monitor.getTasks().addListener(
                (Observable it) -> {

                    int size = monitor.getTasks().size();
                    countLabel.setText(MessageFormat.format(
                            "Number of tasks: {0}", size));
                });

        footerPane.setLeft(countLabel);

        borderPane.setBottom(footerPane);

        getChildren().add(borderPane);
    }

    class TaskCell extends ListCell<Task<?>> {
        private ProgressIndicator progressIndicator;
        private Label titleText;
        private Label messageText;
        private Button cancelButton;

        private Task<?> task;
        private BorderPane borderPane;

        public TaskCell() {
            titleText = new Label();
            titleText.setFont(Font.font("Helvetica", FontWeight.BOLD, 12));

            messageText = new Label();
            messageText.setFont(Font.font("Helvetica", FontWeight.NORMAL, 12));

            progressIndicator = new ProgressIndicator();

            cancelButton = new Button();
            Glyph glyph = fontAwesome.create(FontAwesome.Glyph.STOP)
                    .color(Color.RED);
            cancelButton.setGraphic(glyph);
            cancelButton.getStyleClass().add("task-cancel-button");
            cancelButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            cancelButton.setTooltip(new Tooltip("Cancel Task"));
            cancelButton.setOnAction(evt -> {
                if (task != null) {
                    task.cancel();
                }
            });

            VBox vbox = new VBox();
            vbox.setSpacing(10);
            vbox.getChildren().add(titleText);
            vbox.getChildren().add(messageText);

            borderPane = new BorderPane();
            borderPane.setLeft(progressIndicator);
            borderPane.setCenter(vbox);
            borderPane.setRight(cancelButton);

            BorderPane.setMargin(progressIndicator, new Insets(5));
            BorderPane.setMargin(cancelButton, new Insets(5));
            BorderPane.setMargin(vbox, new Insets(5));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
        protected void updateItem(Task<?> task, boolean empty) {
            this.task = task;
            if (empty || task == null) {
                getStyleClass().setAll("task-list-cell-empty");
                setGraphic(null);
            } else if (task != null) {
                getStyleClass().setAll("task-list-cell");
                progressIndicator.progressProperty().bind(
                        task.progressProperty());
                titleText.textProperty().bind(task.titleProperty());
                messageText.textProperty().bind(task.messageProperty());
                cancelButton.disableProperty().bind(
                        Bindings.not(task.runningProperty()));

                setGraphic(borderPane);
            }
        }
    }
}
