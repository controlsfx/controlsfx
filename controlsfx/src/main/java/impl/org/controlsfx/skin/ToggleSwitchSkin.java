/**
 * Copyright (c) 2015, 2020, 2021, ControlsFX
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

package impl.org.controlsfx.skin;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.controlsfx.control.ToggleSwitch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic Skin implementation for the {@link ToggleSwitch}
 */
public class ToggleSwitchSkin extends SkinBase<ToggleSwitch>
{
    private final StackPane thumb;
    private final StackPane thumbArea;
    private final Label label;
    private final StackPane labelContainer;
    private final TranslateTransition transition;

    /**
     * Constructor for all ToggleSwitchSkin instances.
     *
     * @param control The ToggleSwitch for which this Skin should attach to.
     */
    public ToggleSwitchSkin(ToggleSwitch control) {
        super(control);

        thumb = new StackPane();
        thumbArea = new StackPane();
        label = new Label();
        labelContainer = new StackPane();
        transition = new TranslateTransition(Duration.millis(getThumbMoveAnimationTime()), thumb);
        transition.setFromX(0.0);

        label.textProperty().bind(control.textProperty());
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
        // Stop the transition if it was already running, has no effect otherwise.
        transition.stop();
        if (getSkinnable().isSelected()) {
            transition.setRate(1.0);
            transition.jumpTo(Duration.ZERO);
        } else {
            // If we are not selected, we need to go from right to left.
            transition.setRate(-1.0);
            transition.jumpTo(transition.getDuration());
        }
        transition.play();
    }

    private void mousePressedOnToggleSwitch(ToggleSwitch toggleSwitch) {
        toggleSwitch.setSelected(!toggleSwitch.isSelected());
    }


    /**
     * How many milliseconds it should take for the thumb to go from
     * one edge to the other
     */
    private DoubleProperty thumbMoveAnimationTime = null;

    private DoubleProperty thumbMoveAnimationTimeProperty() {
        if (thumbMoveAnimationTime == null) {
            thumbMoveAnimationTime = new StyleableDoubleProperty(200) {

                @Override
                public Object getBean() {
                    return ToggleSwitchSkin.this;
                }

                @Override
                public String getName() {
                    return "thumbMoveAnimationTime";
                }

                @Override
                public CssMetaData<ToggleSwitch,Number> getCssMetaData() {
                    return THUMB_MOVE_ANIMATION_TIME;
                }
           };
        }
        return thumbMoveAnimationTime;
    }

    private double getThumbMoveAnimationTime() {
        return thumbMoveAnimationTime == null ? 200 : thumbMoveAnimationTime.get();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        ToggleSwitch toggleSwitch = getSkinnable();
        double thumbWidth = snapSize(thumb.prefWidth(-1));
        double thumbHeight = snapSize(thumb.prefHeight(-1));
        thumb.resize(thumbWidth, thumbHeight);

        double thumbAreaWidth = snapSize(thumbArea.prefWidth(-1));
        double thumbAreaHeight = snapSize(thumbArea.prefHeight(-1));
        double thumbAreaY = snapPosition(contentY + (contentHeight / 2) - (thumbAreaHeight / 2));

        thumbArea.resize(thumbAreaWidth, thumbAreaHeight);
        thumbArea.setLayoutX(contentWidth - thumbAreaWidth);
        thumbArea.setLayoutY(thumbAreaY);

        labelContainer.resize(contentWidth - thumbAreaWidth, thumbAreaHeight);
        labelContainer.setLayoutY(thumbAreaY);

        // Layout the thumb on the "unselected" position
        thumb.setLayoutX(thumbArea.getLayoutX());
        thumb.setLayoutY(thumbAreaY + (thumbAreaHeight - thumbHeight) / 2);

        // Each time the layout is done, recompute the thumb "selected" position and apply it to the transition target.
        final double thumbTarget = thumbAreaWidth - thumbWidth;
        transition.setToX(thumbTarget);

        if (transition.getStatus() == Animation.Status.RUNNING) {
            // If the transition is running, it must be restarted for the value to be properly updated.
            final Duration currentTime = transition.getCurrentTime();
            transition.stop();
            transition.playFrom(currentTime);
        } else {
            // If the transition is not running, simply apply the translate value.
            thumb.setTranslateX(toggleSwitch.isSelected() ? thumbTarget : 0.0);
        }
    }


    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + label.prefWidth(-1) + thumbArea.prefWidth(-1) + rightInset;
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + Math.max(thumb.prefHeight(-1), label.prefHeight(-1)) + bottomInset;
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + label.prefWidth(-1) + 20 + thumbArea.prefWidth(-1) + rightInset;
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + Math.max(thumb.prefHeight(-1), label.prefHeight(-1)) + bottomInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }
    
    private static final CssMetaData<ToggleSwitch, Number> THUMB_MOVE_ANIMATION_TIME =
            new CssMetaData<ToggleSwitch, Number>("-thumb-move-animation-time",
                    SizeConverter.getInstance(), 200) {

                @Override
                public boolean isSettable(ToggleSwitch toggleSwitch) {
                    final ToggleSwitchSkin skin = (ToggleSwitchSkin) toggleSwitch.getSkin();
                    return skin.thumbMoveAnimationTime == null ||
                            !skin.thumbMoveAnimationTime.isBound();
                }

                @Override
                public StyleableProperty<Number> getStyleableProperty(ToggleSwitch toggleSwitch) {
                    final ToggleSwitchSkin skin = (ToggleSwitchSkin) toggleSwitch.getSkin();
                    return (StyleableProperty<Number>) (WritableValue<Number>) skin.thumbMoveAnimationTimeProperty();
                }
            };

    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    static {
        final List<CssMetaData<? extends Styleable, ?>> styleables =
                new ArrayList<CssMetaData<? extends Styleable, ?>>(SkinBase.getClassCssMetaData());
        styleables.add(THUMB_MOVE_ANIMATION_TIME);
        STYLEABLES = Collections.unmodifiableList(styleables);
    }

    /**
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
}

