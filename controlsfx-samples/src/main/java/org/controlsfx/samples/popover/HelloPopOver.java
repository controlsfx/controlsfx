package org.controlsfx.samples.popover;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import org.controlsfx.ControlsFXSample;
import org.controlsfx.control.popover.PopOver;
import org.controlsfx.control.popover.PopOverController;
import org.controlsfx.control.popover.PopOverHeader;
import org.controlsfx.control.popover.PopOverTitledPane;
import org.controlsfx.samples.Utils;

public class HelloPopOver extends ControlsFXSample {

	private PopOverController<PopOver, Button> controller = new MyController();

	@Override
	public Node getPanel(Stage stage) {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER);
		grid.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evt) {
				controller.hidePopOver();
			}
		});

		for (int i = 0; i < 10; i++) {
			final Button button = new Button("Button " + i);
			grid.add(button, i % 2, i / 2);

			button.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent evt) {
					controller.hidePopOver();

					if (evt.getClickCount() == 2) {
						controller.setupPopOver(button);
						controller.showPopOver(button, evt.getScreenX(),
								evt.getScreenY());
					}
				}
			});
		}

		return grid;
	}

	class MyController extends PopOverController<PopOver, Button> {

		@Override
		protected PopOver createPopOver(final Button button) {
			PopOver editor = new PopOver();

			PopOverHeader<?> header = (PopOverHeader<?>) editor.getHeader();
			header.setTitle(button.getText() + " (edit me)");
			header.setSubtitle("Just some random controls (edit me)");

			editor.setDetachedTitle(button.getText());

			editor.getPanes().add(createTitledPane("Start Time & Duration"));
			editor.getPanes().add(createTitledPane("Dependencies"));
			editor.getPanes().add(createTitledPane("Priority"));
			editor.getPanes().add(createTitledPane("Assignments / Resources"));
			editor.setExpandedPane(editor.getPanes().get(0));
			editor.setFooter(new Footer());

			ColorPicker picker = (ColorPicker) header.getExtra();
			picker.valueProperty().addListener(new ChangeListener<Color>() {
				@Override
				public void changed(ObservableValue<? extends Color> value,
						Color oldColor, Color newColor) {
					button.setBackground(new Background(new BackgroundFill(
							newColor, CornerRadii.EMPTY, Insets.EMPTY)));
				}
			});

			return editor;
		}
	}

	private TitledPane createTitledPane(String title) {
		VBox box = new VBox(5);
		box.getChildren().add(new Button("Test"));
		box.getChildren().add(new Slider());

		ListView<String> view = new ListView<>();
		view.setPrefHeight(100);
		box.getChildren().add(view);
		final TitledPane pane = new PopOverTitledPane(title, box);
		pane.setTextAlignment(TextAlignment.LEFT);

		Pane connectivityArrow = (Pane) pane.lookup(".arrow");
		if (connectivityArrow != null) {
			connectivityArrow.translateXProperty().bind(
					pane.widthProperty().subtract(
							connectivityArrow.widthProperty().multiply(2)));
		}

		return pane;
	}

	class Footer extends FlowPane {

		public Footer() {
			super(Orientation.HORIZONTAL);

			setAlignment(Pos.CENTER_RIGHT);

			Button delete = new Button("Delete");
			getChildren().add(delete);
			delete.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent evt) {
				}
			});
		}
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public String getSampleName() {
		return "PopOver";
	}

	@Override
	public String getJavaDocURL() {
		return Utils.JAVADOC_BASE
				+ "org/controlsfx/control/popover/PopOver.html";
	}

	@Override
	public String getSampleDescription() {
		return "An implementation of a pop over control as used by Apple for its iCal application. A pop over allows"
				+ "the user to see and edit an objects properties. The pop over gets displayed in its own popup window and"
				+ "can be torn off in order to create several instances of it.";
	}
}
