package impl.org.controlsfx.skin;

import com.sun.javafx.scene.control.skin.LabeledText;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.tools.Utils;

/**
 * Created by pedro_000 on 8/26/2015.
 */
public class ToggleSwitchSkin extends SkinBase<ToggleSwitch>
{
    StackPane thumb;
    StackPane thumbArea;
    LabeledText label;
    StackPane labelContainer;

    /**
     * Constructor for all SkinBase instances.
     *
     * @param control The control for which this Skin should attach to.
     */
    public ToggleSwitchSkin(ToggleSwitch control) {
        super(control);

        thumb = new StackPane();
        thumbArea = new StackPane();
        label = new LabeledText(control);
        labelContainer = new StackPane();

        updateLabel(control);
        getChildren().addAll(labelContainer, thumbArea, thumb);
        labelContainer.getChildren().addAll(label);
        StackPane.setAlignment(label, Pos.CENTER_LEFT);

        thumb.getStyleClass().setAll("thumb");
        thumbArea.getStyleClass().setAll("thumb-area");

        thumbArea.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
        thumb.setOnMouseReleased(event -> mousePressedOnToggleSwitch(control));
        control.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue() != oldValue.booleanValue())
                selectedStateChanged();
        });
    }

    private void selectedStateChanged() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(100), thumb);
        double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
        double thumbWidth = snapSize(thumb.prefWidth(-1));

        if (!getSkinnable().isSelected())
            transition.setByX(-(thumbAreaWidth - thumbWidth));
        else {
            transition.setByX(thumbAreaWidth - thumbWidth);
        }
        transition.setCycleCount(1);
        transition.play();
    }

    private void mousePressedOnToggleSwitch(ToggleSwitch toggleSwitch) {
        toggleSwitch.setSelected(!toggleSwitch.isSelected());
    }

    private void updateLabel(ToggleSwitch skinnable) {
        label.setText(skinnable.isSelected() ? skinnable.getTurnOnText() : skinnable.getTurnOffText());
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        ToggleSwitch toggleSwitch = getSkinnable();

        double thumbWidth = snapSize(thumb.prefWidth(-1));
        double thumbHeight = snapSize(thumb.prefHeight(-1));
        thumb.resize(thumbWidth, thumbHeight);

        double thumbAreaY = snapPosition(contentY);
        double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
        double thumbAreaHeight = snapSize(thumbArea.prefHeight(-1));

        thumbArea.resize(thumbAreaWidth, thumbAreaHeight);
        thumbArea.setLayoutX(contentWidth - thumbAreaWidth);
        thumbArea.setLayoutY(thumbAreaY);

        labelContainer.resize(contentWidth - thumbAreaWidth, thumbAreaHeight);
        labelContainer.setLayoutY(thumbAreaY);

        if (!toggleSwitch.isSelected())
        {
            thumb.setLayoutX(thumbArea.getLayoutX());
            thumb.setLayoutY(thumbAreaY + (thumbAreaHeight - thumbHeight) / 2);
        } else
        {
            thumb.setLayoutX(thumbArea.getLayoutX() + thumbAreaWidth - thumbWidth);
            thumb.setLayoutY(thumbAreaY + (thumbAreaHeight - thumbHeight) / 2);
        }
    }


    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final String labelText = label.getText();
        final Font font = label.getFont();
        double textWidth = Utils.computeTextWidth(font, labelText, 0);

        return leftInset + textWidth + thumbArea.prefWidth(-1) + rightInset;
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        final Font font = label.getFont();
        final String labelText = label.getText();
        final double textHeight = Utils.computeTextHeight(font, labelText, 0, label.getLineSpacing(), label.getBoundsType());

        return topInset + Math.max(thumb.prefHeight(-1), textHeight) + bottomInset;
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final String labelText = label.getText();
        final Font font = label.getFont();
        double textWidth = Utils.computeTextWidth(font, labelText, 0);

        return leftInset + textWidth + 20 + thumbArea.prefWidth(-1) + rightInset;
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset)
    {
        final Font font = label.getFont();
        final String labelText = label.getText();
        final double textHeight = Utils.computeTextHeight(font, labelText, 0, label.getLineSpacing(), label.getBoundsType());

        return topInset + Math.max(thumb.prefHeight(-1), textHeight) + bottomInset;
    }
}

