package impl.org.controlsfx.skin;

import static javafx.geometry.Orientation.VERTICAL;

import org.controlsfx.control.StatusBar;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class StatusBarSkin extends SkinBase<StatusBar> {

	private HBox leftBox;
	private HBox rightBox;
	private Label label;
	private ProgressBar progressBar;

	public StatusBarSkin(StatusBar statusBar) {
		super(statusBar);

		leftBox = new HBox();
		leftBox.getStyleClass().add("left-items");

		rightBox = new HBox();
		rightBox.getStyleClass().add("right-items");

		progressBar = new ProgressBar();
		progressBar.progressProperty().bind(statusBar.progressProperty());
		progressBar.visibleProperty().bind(
				Bindings.not(Bindings.equal(0, statusBar.progressProperty())));

		Separator separator = new Separator(VERTICAL);
		separator.visibleProperty().bind(
				Bindings.isNotEmpty(statusBar.getRightItems()));

		label = new Label();
		label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		label.textProperty().bind(statusBar.textProperty());
		label.graphicProperty().bind(statusBar.graphicProperty());
		label.getStyleClass().add("status-label");

		leftBox.getChildren().setAll(getSkinnable().getLeftItems());

		rightBox.getChildren().setAll(getSkinnable().getRightItems());

		statusBar.getLeftItems().addListener(
				(Observable evt) -> leftBox.getChildren().setAll(
						getSkinnable().getLeftItems()));

		statusBar.getRightItems().addListener(
				(Observable evt) -> rightBox.getChildren().setAll(
						getSkinnable().getRightItems()));

		GridPane gridPane = new GridPane();

		GridPane.setFillHeight(leftBox, true);
		GridPane.setFillHeight(rightBox, true);
		GridPane.setFillHeight(label, true);
		GridPane.setFillHeight(progressBar, true);
		GridPane.setFillHeight(separator, true);

		GridPane.setVgrow(leftBox, Priority.ALWAYS);
		GridPane.setVgrow(rightBox, Priority.ALWAYS);
		GridPane.setVgrow(label, Priority.ALWAYS);
		GridPane.setVgrow(progressBar, Priority.ALWAYS);
		GridPane.setVgrow(separator, Priority.ALWAYS);

		GridPane.setHgrow(label, Priority.ALWAYS);

		GridPane.setMargin(separator, new Insets(0, 8, 0, 8));

		gridPane.add(leftBox, 0, 0);
		gridPane.add(label, 1, 0);
		gridPane.add(progressBar, 2, 0);
		gridPane.add(separator, 3, 0);
		gridPane.add(rightBox, 4, 0);

		getChildren().add(gridPane);
	}
}
