/**
 * Copyright (c) 2013, 2021, ControlsFX
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

import impl.org.controlsfx.ImplUtils;
import javafx.beans.binding.ObjectBinding;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.tools.Utils;

import static impl.org.controlsfx.skin.RangeSliderSkin.FocusedChild.*;

public class RangeSliderSkin extends SkinBase<RangeSlider> {

    /** Track if slider is vertical/horizontal and cause re layout */
    private NumberAxis tickLine = null;
    private double trackToTickGap = 2;

    private boolean showTickMarks;
    private double thumbWidth;
    private double thumbHeight;

    private Orientation orientation;

    private StackPane track;
    private double trackStart;
    private double trackLength;
    private double lowThumbPos;
    private double rangeEnd;
    private double rangeStart;
    private ThumbPane lowThumb;
    private ThumbPane highThumb;
    private StackPane rangeBar; // the bar between the two thumbs, can be dragged

    // temp fields for mouse drag handling
    private double preDragPos;          // used as a temp value for low and high thumbs
    private Point2D preDragThumbPoint;  // in skin coordinates

    private FocusedChild currentFocus = LOW_THUMB;

    public RangeSliderSkin(final RangeSlider rangeSlider) {
        super(rangeSlider);
        orientation = getSkinnable().getOrientation();
        initFirstThumb();
        initSecondThumb();
        initRangeBar();
        registerChangeListener(rangeSlider.lowValueProperty(), e -> {
            positionLowThumb();
            rangeBar.resizeRelocate(rangeStart, rangeBar.getLayoutY(), 
            rangeEnd - rangeStart, rangeBar.getHeight());
        });
        registerChangeListener(rangeSlider.highValueProperty(), e -> {
            positionHighThumb();
            rangeBar.resize(rangeEnd-rangeStart, rangeBar.getHeight());
        });
        registerChangeListener(rangeSlider.minProperty(), e -> {
            if (showTickMarks && tickLine != null) {
                tickLine.setLowerBound(getSkinnable().getMin());
            }
            getSkinnable().requestLayout();
        });
        registerChangeListener(rangeSlider.maxProperty(), e -> {
            if (showTickMarks && tickLine != null) {
                tickLine.setUpperBound(getSkinnable().getMax());
            }
            getSkinnable().requestLayout();
        });
        registerChangeListener(rangeSlider.orientationProperty(), e -> {
            orientation = getSkinnable().getOrientation();
            if (showTickMarks && tickLine != null) {
                tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
            }
            getSkinnable().requestLayout();
        });
        registerChangeListener(rangeSlider.showTickMarksProperty(), e -> {
            setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        });
        registerChangeListener(rangeSlider.showTickLabelsProperty(), e -> {
            setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        });
        registerChangeListener(rangeSlider.majorTickUnitProperty(), e -> {
            if (tickLine != null) {
                tickLine.setTickUnit(getSkinnable().getMajorTickUnit());
                getSkinnable().requestLayout();
            }
        });
        registerChangeListener(rangeSlider.minorTickCountProperty(), e -> {
            if (tickLine != null) {
                tickLine.setMinorTickCount(Math.max(getSkinnable().getMinorTickCount(),0) + 1);
                getSkinnable().requestLayout();
            }
        });
        lowThumb.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (hasFocus) {
                currentFocus = LOW_THUMB;
            }
        });
        highThumb.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (hasFocus) {
                currentFocus = HIGH_THUMB;
            }
        });
        rangeBar.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (hasFocus) {
                currentFocus = RANGE_BAR;
            }
        });
        rangeSlider.focusedProperty().addListener((ov, t, hasFocus) -> {
            if (hasFocus) {
                lowThumb.setFocus(true);
            } else {
                lowThumb.setFocus(false);
                highThumb.setFocus(false);
                currentFocus = NONE;
            }
        });

        EventHandler<KeyEvent> keyPressEventHandler = event -> {
            if (KeyCode.TAB.equals(event.getCode())) {
                if (lowThumb.isFocused()) {
                    if (event.isShiftDown()) {
                        ImplUtils.focusPreviousSibling(getSkinnable());
                    } else {
                        lowThumb.setFocus(false);
                        highThumb.setFocus(true);
                    }
                    event.consume();
                } else if (highThumb.isFocused()) {
                    if(event.isShiftDown()) {
                        highThumb.setFocus(false);
                        lowThumb.setFocus(true);
                    } else {
                        ImplUtils.focusNextSibling(getSkinnable());
                    }
                    event.consume();
                }
            } else if (KeyCode.LEFT.equals(event.getCode()) || KeyCode.KP_LEFT.equals(event.getCode())) {
                if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
                    rtl(getSkinnable(), this::incrementValue, this::decrementValue);
                }
            } else if (KeyCode.RIGHT.equals(event.getCode()) || KeyCode.KP_RIGHT.equals(event.getCode())) {
                if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
                    rtl(getSkinnable(), this::decrementValue, this::incrementValue);
                }
            } else if (KeyCode.DOWN.equals(event.getCode()) || KeyCode.KP_DOWN.equals(event.getCode())) {
                if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
                    decrementValue();
                }
            } else if (KeyCode.UP.equals(event.getCode()) || KeyCode.KP_UP.equals(event.getCode())) {
                if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
                    incrementValue();
                }
            }
            event.consume();
        };
        EventHandler<KeyEvent> keyReleaseEventHandler = event -> {
            if (KeyCode.HOME.equals(event.getCode())) {
                home();
            } else if (KeyCode.END.equals(event.getCode())) {
                end();
            }
            event.consume();
        };
        getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, keyPressEventHandler);
        getSkinnable().addEventHandler(KeyEvent.KEY_RELEASED, keyReleaseEventHandler);
        // set up a callback to indicate which thumb is currently selected (via enum).
        setSelectedValue(v -> currentFocus);
    }
    
    private void initFirstThumb() {
        lowThumb = new ThumbPane();
        lowThumb.getStyleClass().setAll("low-thumb"); //$NON-NLS-1$
        lowThumb.setFocusTraversable(true);
        track = new StackPane();
        track.setFocusTraversable(false);
        track.getStyleClass().setAll("track"); //$NON-NLS-1$

        getChildren().clear();
        getChildren().addAll(track, lowThumb);
        setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        track.setOnMousePressed(me -> {
            if (!lowThumb.isPressed() && !highThumb.isPressed()) {
                if (isHorizontal()) {
                    trackPress(me, (me.getX() / trackLength));
                } else {
                    trackPress(me, (me.getY() / trackLength));
                }
            }
        });

        track.setOnMouseReleased(me -> {
            // Nothing being done with the second param in sliderBehavior
            // So, passing a dummy value
            trackRelease(me, 0.0f);
        });

        lowThumb.setOnMousePressed(me -> {
            highThumb.setFocus(false);
            lowThumb.setFocus(true);
            lowThumbPressed(me, 0.0f);
            preDragThumbPoint = lowThumb.localToParent(me.getX(), me.getY());
            preDragPos = (getSkinnable().getLowValue() - getSkinnable().getMin()) /
                    (getMaxMinusMinNoZero());
        });

        lowThumb.setOnMouseReleased(me -> lowThumbReleased(me));

        lowThumb.setOnMouseDragged(me -> {
            Point2D cur = lowThumb.localToParent(me.getX(), me.getY());
            double dragPos = (isHorizontal())?
                cur.getX() - preDragThumbPoint.getX() : -(cur.getY() - preDragThumbPoint.getY());
            lowThumbDragged(me, preDragPos + dragPos / trackLength);
        });
    }

    private void initSecondThumb() {
        highThumb = new ThumbPane();
        highThumb.getStyleClass().setAll("high-thumb"); //$NON-NLS-1$
        if (!getChildren().contains(highThumb)) {
            getChildren().add(highThumb);
        }
        highThumb.setOnMousePressed(e -> {
            lowThumb.setFocus(false);
            highThumb.setFocus(true);
            highThumbPressed(e, 0.0D);
            preDragThumbPoint = highThumb.localToParent(e.getX(), e.getY());
            preDragPos = (getSkinnable().getHighValue() - getSkinnable().getMin()) / (getMaxMinusMinNoZero());
        });
        highThumb.setOnMouseReleased(e -> highThumbReleased(e));
        highThumb.setOnMouseDragged(e -> {
            boolean orientation = getSkinnable().getOrientation() == Orientation.HORIZONTAL;
            double trackLength = orientation ? track.getWidth() : track.getHeight();

            Point2D point2d = highThumb.localToParent(e.getX(), e.getY());
            double d = getSkinnable().getOrientation() != Orientation.HORIZONTAL ? -(point2d.getY() - preDragThumbPoint.getY()) : point2d.getX() - preDragThumbPoint.getX();
            highThumbDragged(e, preDragPos + d / trackLength);
        });
    }

    private void initRangeBar() {
        rangeBar = new StackPane();
        rangeBar.setFocusTraversable(false);
        rangeBar.cursorProperty().bind(new ObjectBinding<>() {
            {
                bind(rangeBar.hoverProperty());
            }

            @Override
            protected Cursor computeValue() {
                return rangeBar.isHover() ? Cursor.HAND : Cursor.DEFAULT;
            }
        });
        rangeBar.getStyleClass().setAll("range-bar"); //$NON-NLS-1$

        rangeBar.setOnMousePressed(e -> {
            rangeBar.requestFocus();
            preDragPos = isHorizontal() ? e.getX() : -e.getY();
        });

        rangeBar.setOnMouseDragged(e -> {
            double delta = (isHorizontal() ? e.getX() : -e.getY()) - preDragPos;
            moveRange(delta);
        });

        rangeBar.setOnMouseReleased(e -> confirmRange());
        getChildren().add(rangeBar);
    }

    /**
     * When ticks or labels are changing of visibility, we compute the new
     * visibility and add the necessary objets. After this method, we must be
     * sure to add the high Thumb and the rangeBar.
     *
     * @param ticksVisible
     * @param labelsVisible
     */
    private void setShowTickMarks(boolean ticksVisible, boolean labelsVisible) {
        showTickMarks = (ticksVisible || labelsVisible);
        RangeSlider rangeSlider = getSkinnable();
        if (showTickMarks) {
            if (tickLine == null) {
                tickLine = new NumberAxis();
                tickLine.setFocusTraversable(false);
                tickLine.tickLabelFormatterProperty().bind(getSkinnable().labelFormatterProperty());
                tickLine.setAnimated(false);
                tickLine.setAutoRanging(false);
                tickLine.setSide(isHorizontal() ? Side.BOTTOM : Side.RIGHT);
                tickLine.setUpperBound(rangeSlider.getMax());
                tickLine.setLowerBound(rangeSlider.getMin());
                tickLine.setTickUnit(rangeSlider.getMajorTickUnit());
                tickLine.setTickMarkVisible(ticksVisible);
                tickLine.setTickLabelsVisible(labelsVisible);
                tickLine.setMinorTickVisible(ticksVisible);
                // add 1 to the slider minor tick count since the axis draws one
                // less minor ticks than the number given.
                tickLine.setMinorTickCount(Math.max(rangeSlider.getMinorTickCount(),0) + 1);
                getChildren().clear();
                getChildren().addAll(tickLine, track, lowThumb);
            } else {
                tickLine.setTickLabelsVisible(labelsVisible);
                tickLine.setTickMarkVisible(ticksVisible);
                tickLine.setMinorTickVisible(ticksVisible);
            }
        } 
        else  {
            getChildren().clear();
            getChildren().addAll(track, lowThumb);
//            tickLine = null;
        }

        getSkinnable().requestLayout();
    }

    /**
     *
     * @return the difference between max and min, but if they have the same
     * value, 1 is returned instead of 0 because otherwise the division where it
     * can be used will return Nan.
     */
    private double getMaxMinusMinNoZero() {
        RangeSlider s = getSkinnable();
        return s.getMax() - s.getMin() == 0 ? 1 : s.getMax() - s.getMin();
    }
    
    /**
     * Called when ever either min, max or lowValue changes, so lowthumb's layoutX, Y is recomputed.
     */
    private void positionLowThumb() {
        RangeSlider s = getSkinnable();
        boolean horizontal = isHorizontal();
        double lx = (horizontal) ? trackStart + (((trackLength * ((s.getLowValue() - s.getMin()) /
                (getMaxMinusMinNoZero()))) - thumbWidth/2)) : lowThumbPos;
        double ly = (horizontal) ? lowThumbPos :
            getSkinnable().getInsets().getTop() + trackLength - (trackLength * ((s.getLowValue() - s.getMin()) /
                (getMaxMinusMinNoZero()))); //  - thumbHeight/2
        lowThumb.setLayoutX(lx);
        lowThumb.setLayoutY(ly);
        if (horizontal) rangeStart = lx + thumbWidth; else rangeEnd = ly;
    }

    /**
     * Called when ever either min, max or highValue changes, so highthumb's layoutX, Y is recomputed.
     */
    private void positionHighThumb() {
        RangeSlider slider = (RangeSlider) getSkinnable();
        boolean orientation = ((RangeSlider) getSkinnable()).getOrientation() == Orientation.HORIZONTAL;

        double thumbWidth = lowThumb.getWidth();
        double thumbHeight = lowThumb.getHeight();
        highThumb.resize(thumbWidth, thumbHeight);

        double pad = 0;//track.impl_getBackgroundFills() == null || track.impl_getBackgroundFills().length <= 0 ? 0.0D : track.impl_getBackgroundFills()[0].getTopLeftCornerRadius();
        double trackStart = orientation ? track.getLayoutX() : track.getLayoutY();
        trackStart += pad;
        double trackLength = orientation ? track.getWidth() : track.getHeight();
        trackLength -= 2 * pad;

        double x = orientation ? trackStart + (trackLength * ((slider.getHighValue() - slider.getMin()) / (getMaxMinusMinNoZero())) - thumbWidth / 2D) : lowThumb.getLayoutX();
        double y = orientation ? lowThumb.getLayoutY() : (getSkinnable().getInsets().getTop() + trackLength) - trackLength * ((slider.getHighValue() - slider.getMin()) / (getMaxMinusMinNoZero()));
        highThumb.setLayoutX(x);
        highThumb.setLayoutY(y);
        if (orientation) rangeEnd = x; else rangeStart = y + thumbWidth;
    }
    
    @Override protected void layoutChildren(final double x, final double y,
            final double w, final double h) {
        // resize thumb to preferred size
        thumbWidth = lowThumb.prefWidth(-1);
        thumbHeight = lowThumb.prefHeight(-1);
        lowThumb.resize(thumbWidth, thumbHeight);
        // we are assuming the is common radius's for all corners on the track
        double trackRadius = track.getBackground() == null ? 0 : track.getBackground().getFills().size() > 0 ?
                track.getBackground().getFills().get(0).getRadii().getTopLeftHorizontalRadius() : 0;
      
        if (isHorizontal()) {
            double tickLineHeight =  (showTickMarks) ? tickLine.prefHeight(-1) : 0;
            double trackHeight = track.prefHeight(-1);
            double trackAreaHeight = Math.max(trackHeight,thumbHeight);
            double totalHeightNeeded = trackAreaHeight  + ((showTickMarks) ? trackToTickGap+tickLineHeight : 0);
            double startY = y + ((h - totalHeightNeeded)/2); // center slider in available height vertically
            trackLength = w - thumbWidth;
            trackStart = x + (thumbWidth/2);
            double trackTop = (int)(startY + ((trackAreaHeight-trackHeight)/2));
            lowThumbPos = (int)(startY + ((trackAreaHeight-thumbHeight)/2));
            
            positionLowThumb();
            // layout track
            track.resizeRelocate(trackStart - trackRadius, trackTop , trackLength + trackRadius + trackRadius, trackHeight);
            positionHighThumb();
            // layout range bar
            rangeBar.resizeRelocate(rangeStart, trackTop, rangeEnd - rangeStart, trackHeight);
            // layout tick line
            if (showTickMarks) {
                tickLine.setLayoutX(trackStart);
                tickLine.setLayoutY(trackTop+trackHeight+trackToTickGap);
                tickLine.resize(trackLength, tickLineHeight);
                tickLine.requestAxisLayout();
            } else {
                if (tickLine != null) {
                    tickLine.resize(0,0);
                    tickLine.requestAxisLayout();
                }
                tickLine = null;
            }
        } else {
            double tickLineWidth = (showTickMarks) ? tickLine.prefWidth(-1) : 0;
            double trackWidth = track.prefWidth(-1);
            double trackAreaWidth = Math.max(trackWidth,thumbWidth);
            double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap + tickLineWidth : 0) ;
            double startX = x + ((w - totalWidthNeeded)/2); // center slider in available width horizontally
            trackLength = h - thumbHeight;
            trackStart = y + (thumbHeight/2);
            double trackLeft = (int)(startX + ((trackAreaWidth-trackWidth)/2));
            lowThumbPos = (int)(startX + ((trackAreaWidth-thumbWidth)/2));

            positionLowThumb();
            // layout track
            track.resizeRelocate(trackLeft, trackStart - trackRadius, trackWidth, trackLength + trackRadius + trackRadius);
            positionHighThumb();
            // layout range bar
            rangeBar.resizeRelocate(trackLeft, rangeStart, trackWidth, rangeEnd - rangeStart);
            // layout tick line
            if (showTickMarks) {
                tickLine.setLayoutX(trackLeft+trackWidth+trackToTickGap);
                tickLine.setLayoutY(trackStart);
                tickLine.resize(tickLineWidth, trackLength);
                tickLine.requestAxisLayout();
            } else {
                if (tickLine != null) {
                    tickLine.resize(0,0);
                    tickLine.requestAxisLayout();
                }
                tickLine = null;
            }
        }
    }
    
    private double minTrackLength() {
        return 2 * lowThumb.prefWidth(-1);
    }
    
    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isHorizontal()) {
            return (leftInset + minTrackLength() + lowThumb.minWidth(-1) + rightInset);
        } else {
            return (leftInset + lowThumb.prefWidth(-1) + rightInset);
        }
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
         if (isHorizontal()) {
            return (topInset + lowThumb.prefHeight(-1) + bottomInset);
        } else {
            return (topInset + minTrackLength() + lowThumb.prefHeight(-1) + bottomInset);
        }
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isHorizontal()) {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefWidth(-1));
            } else {
                return 140;
            }
        } else {
            //return (padding.getLeft()) + Math.max(thumb.prefWidth(-1), track.prefWidth(-1)) + padding.getRight();
            return leftInset + Math.max(lowThumb.prefWidth(-1), track.prefWidth(-1)) +
            ((showTickMarks) ? (trackToTickGap+tickLine.prefWidth(-1)) : 0) + rightInset;
        }
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isHorizontal()) {
            return getSkinnable().getInsets().getTop() + Math.max(lowThumb.prefHeight(-1), track.prefHeight(-1)) +
             ((showTickMarks) ? (trackToTickGap+tickLine.prefHeight(-1)) : 0)  + bottomInset;
        } else {
            if(showTickMarks) {
                return Math.max(140, tickLine.prefHeight(-1));
            } else {
                return 140;
            }
        }
    }

    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isHorizontal()) {
            return Double.MAX_VALUE;
        } else {
            return getSkinnable().prefWidth(-1);
        }
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (isHorizontal()) {
            return getSkinnable().prefHeight(width);
        } else {
            return Double.MAX_VALUE;
        }
    }
    
    private boolean isHorizontal() {
        return orientation == null || orientation == Orientation.HORIZONTAL;
    }

    /**************************************************************************
     *                         State and Functions                            *
     *************************************************************************/

    private Callback<Void, FocusedChild> selectedValue;
    public void setSelectedValue(Callback<Void, FocusedChild> c) {
        selectedValue = c;
    }

    // when high thumb is released, highValueChanging is set to false.
    private void highThumbReleased(MouseEvent e) {
        RangeSlider slider = getSkinnable();
        slider.setHighValueChanging(false);
        if (slider.isSnapToTicks())
            slider.setHighValue(snapValueToTicks(slider.getHighValue()));
    }

    private void highThumbPressed(MouseEvent e, double position) {
        RangeSlider slider = getSkinnable();
        if (!slider.isFocused())
            slider.requestFocus();
        slider.setHighValueChanging(true);
    }

    private void highThumbDragged(MouseEvent e, double position) {
        RangeSlider slider = getSkinnable();
        slider.setHighValue(Utils.clamp(slider.getMin(), position * (slider.getMax() - slider.getMin()) + slider.getMin(), slider.getMax()));
    }

    private void moveRange(double position) {
        RangeSlider slider = getSkinnable();
        final double min = slider.getMin();
        final double max = slider.getMax();
        final double lowValue = slider.getLowValue();
        final double newLowValue = Utils.clamp(min, lowValue + position *(max-min) /
                (slider.getOrientation() == Orientation.HORIZONTAL? slider.getWidth(): slider.getHeight()), max);
        final double highValue = slider.getHighValue();
        final double newHighValue = Utils.clamp(min, highValue + position*(max-min) /
                (slider.getOrientation() == Orientation.HORIZONTAL? slider.getWidth(): slider.getHeight()), max);

        if (newLowValue <= min || newHighValue >= max) return;
        slider.setLowValueChanging(true);
        slider.setHighValueChanging(true);
        slider.setLowValue(newLowValue);
        slider.setHighValue(newHighValue);
    }

    private void confirmRange() {
        RangeSlider slider = getSkinnable();

        slider.setLowValueChanging(false);
        if (slider.isSnapToTicks()) {
            slider.setLowValue(snapValueToTicks(slider.getLowValue()));
        }
        slider.setHighValueChanging(false);
        if (slider.isSnapToTicks()) {
            slider.setHighValue(snapValueToTicks(slider.getHighValue()));
        }
    }

    /**
     * Invoked by the RangeSlider {@link Skin} implementation whenever a mouse press
     * occurs on the "track" of the slider. This will cause the thumb to be
     * moved by some amount.
     *
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    private void trackPress(MouseEvent e, double position) {
        // determine the percentage of the way between min and max
        // represented by this mouse event
        final RangeSlider rangeSlider = getSkinnable();
        // If not already focused, request focus
        if (!rangeSlider.isFocused()) {
            rangeSlider.requestFocus();
        }
        if (selectedValue != null) {
            double newPosition;
            if (rangeSlider.getOrientation().equals(Orientation.HORIZONTAL)) {
                newPosition = position * (rangeSlider.getMax() - rangeSlider.getMin()) + rangeSlider.getMin();
            } else {
                newPosition = (1 - position) * (rangeSlider.getMax() - rangeSlider.getMin()) + rangeSlider.getMin();
            }

            /**
             * If the position is inferior to the current LowValue, this means
             * the user clicked on the track to move the low thumb. If not, then
             * it means the user wanted to move the high thumb.
             */
            if (newPosition < rangeSlider.getLowValue()) {
                rangeSlider.adjustLowValue(newPosition);
            } else {
                rangeSlider.adjustHighValue(newPosition);
            }
        }
    }

    public void trackRelease(MouseEvent e, double position) {
    }

    /**
     * @param position The mouse position on track with 0.0 being beginning of
     *       track and 1.0 being the end
     */
    public void lowThumbPressed(MouseEvent e, double position) {
        // If not already focused, request focus
        final RangeSlider rangeSlider = getSkinnable();
        if (!rangeSlider.isFocused())  rangeSlider.requestFocus();
        rangeSlider.setLowValueChanging(true);
    }

    /**
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    public void lowThumbDragged(MouseEvent e, double position) {
        final RangeSlider rangeSlider = getSkinnable();
        double newValue = Utils.clamp(rangeSlider.getMin(),
                (position * (rangeSlider.getMax() - rangeSlider.getMin())) + rangeSlider.getMin(),
                rangeSlider.getMax());
        rangeSlider.setLowValue(newValue);
    }

    /**
     * When lowThumb is released lowValueChanging should be set to false.
     */
    public void lowThumbReleased(MouseEvent e) {
        final RangeSlider rangeSlider = getSkinnable();
        rangeSlider.setLowValueChanging(false);
        // RT-15207 When snapToTicks is true, slider value calculated in drag
        // is then snapped to the nearest tick on mouse release.
        if (rangeSlider.isSnapToTicks()) {
            rangeSlider.setLowValue(snapValueToTicks(rangeSlider.getLowValue()));
        }
    }

    void home() {
        RangeSlider slider = (RangeSlider) getNode();
        slider.adjustHighValue(slider.getMin());
    }

    private void decrementValue() {
        RangeSlider slider = getSkinnable();
        if (selectedValue != null) {
            if (selectedValue.call(null) == FocusedChild.HIGH_THUMB) {
                if (slider.isSnapToTicks())
                    slider.adjustHighValue(slider.getHighValue() - computeIncrement());
                else
                    slider.decrementHighValue();
            } else {
                if (slider.isSnapToTicks())
                    slider.adjustLowValue(slider.getLowValue() - computeIncrement());
                else
                    slider.decrementLowValue();
            }
        }
    }

    private void end() {
        RangeSlider slider = (RangeSlider) getNode();
        slider.adjustHighValue(slider.getMax());
    }

    private void incrementValue() {
        RangeSlider slider = getSkinnable();
        if (selectedValue != null) {
            if (selectedValue.call(null) == FocusedChild.HIGH_THUMB) {
                if (slider.isSnapToTicks())
                    slider.adjustHighValue(slider.getHighValue() + computeIncrement());
                else
                    slider.incrementHighValue();
            } else {
                if (slider.isSnapToTicks())
                    slider.adjustLowValue(slider.getLowValue() + computeIncrement());
                else
                    slider.incrementLowValue();
            }
        }
    }

    private double computeIncrement() {
        RangeSlider rangeSlider = getSkinnable();
        double d;
        if (rangeSlider.getMinorTickCount() != 0)
            d = rangeSlider.getMajorTickUnit() / (double) (Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
        else
            d = rangeSlider.getMajorTickUnit();
        if (rangeSlider.getBlockIncrement() > 0.0D && rangeSlider.getBlockIncrement() < d)
            return d;
        else
            return rangeSlider.getBlockIncrement();
    }

    private void rtl(RangeSlider node, Runnable rtlMethod, Runnable nonRtlMethod) {
        if (node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            rtlMethod.run();
        } else {
            nonRtlMethod.run();
        }
    }

    private double snapValueToTicks(double d) {
        RangeSlider rangeSlider = getSkinnable();
        double d1 = d;
        double d2;
        if (rangeSlider.getMinorTickCount() != 0)
            d2 = rangeSlider.getMajorTickUnit() / (double) (Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
        else
            d2 = rangeSlider.getMajorTickUnit();
        int i = (int) ((d1 - rangeSlider.getMin()) / d2);
        double d3 = (double) i * d2 + rangeSlider.getMin();
        double d4 = (double) (i + 1) * d2 + rangeSlider.getMin();
        d1 = Utils.nearest(d3, d1, d4);
        return Utils.clamp(rangeSlider.getMin(), d1, rangeSlider.getMax());
    }

    private static class ThumbPane extends StackPane {
        public void setFocus(boolean value) {
            setFocused(value);
        }
    }

    public enum FocusedChild {
        LOW_THUMB,
        HIGH_THUMB,
        RANGE_BAR,
        NONE
    }
}
