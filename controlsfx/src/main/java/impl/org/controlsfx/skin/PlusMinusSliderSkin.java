package impl.org.controlsfx.skin;

import static javafx.scene.input.MouseEvent.MOUSE_PRESSED;
import static javafx.scene.input.MouseEvent.MOUSE_RELEASED;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import org.controlsfx.control.PlusMinusSlider;
import org.controlsfx.control.PlusMinusSlider.PlusMinusEvent;

import com.sun.javafx.css.StyleManager;

public class PlusMinusSliderSkin extends SkinBase<PlusMinusSlider> {
    
    static {
        // refer to ControlsFXControl for why this is necessary
        StyleManager.getInstance().addUserAgentStylesheet(
                PlusMinusSlider.class.getResource("plusminusslider.css").toExternalForm()); //$NON-NLS-1$
    }

	private SliderReader reader;

	private Slider slider;

	private Region plusRegion;

	private Region minusRegion;

	private BorderPane borderPane;

	public PlusMinusSliderSkin(PlusMinusSlider adjuster) {
		super(adjuster);

		/*
		 * We are not supporting any key events, yet. Adding this filter makes
		 * sure the user doesn't use the standard key bindings of the slider. In
		 * that case the thumb would not move itself back automatically (e.g.
		 * after pressing "arrow right").
		 */
		adjuster.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				event.consume();
			}
		});

		slider = new Slider(-1, 1, 0);

		slider.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				getSkinnable().getProperties().put("plusminusslidervalue", //$NON-NLS-1$
						newValue.doubleValue());
			}
		});

		slider.orientationProperty().bind(adjuster.orientationProperty());

		slider.addEventHandler(MOUSE_PRESSED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent evt) {
				reader = new SliderReader();
				reader.start();
			}
		});

		slider.addEventHandler(MOUSE_RELEASED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent evt) {
				if (reader != null) {
					reader.stop();
				}

				KeyValue keyValue = new KeyValue(slider.valueProperty(), 0);
				KeyFrame keyFrame = new KeyFrame(Duration.millis(100), keyValue);
				Timeline timeline = new Timeline(keyFrame);
				timeline.play();
			}
		});

		plusRegion = new Region();
		plusRegion.getStyleClass().add("adjust-plus"); //$NON-NLS-1$

		minusRegion = new Region();
		minusRegion.getStyleClass().add("adjust-minus"); //$NON-NLS-1$

		borderPane = new BorderPane();

		updateLayout(adjuster.getOrientation());

		getChildren().add(borderPane);

		adjuster.orientationProperty().addListener((observable, oldValue, newValue) -> updateLayout(newValue));
	}

	private void updateLayout(Orientation orientation) {
		borderPane.getChildren().clear();

		switch (orientation) {
		case HORIZONTAL:
			borderPane.setLeft(minusRegion);
			borderPane.setCenter(slider);
			borderPane.setRight(plusRegion);
			break;
		case VERTICAL:
			borderPane.setTop(plusRegion);
			borderPane.setCenter(slider);
			borderPane.setBottom(minusRegion);
			break;
		}
	}

	class SliderReader extends AnimationTimer {
		private long lastTime = System.currentTimeMillis();

		@Override
		public void handle(long now) {
			// max speed: 100 hundred times per second
			if (now - lastTime > 10000000) {
				lastTime = now;
				slider.fireEvent(new PlusMinusEvent(slider, slider,
						PlusMinusEvent.VALUE_CHANGED, slider.getValue()));
			}
		}
	}
}
