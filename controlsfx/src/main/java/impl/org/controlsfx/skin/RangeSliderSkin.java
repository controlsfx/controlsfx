/**
 * Copyright (c) 2013, 2016 ControlsFX
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

import static impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild.HIGH_THUMB;
import static impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild.LOW_THUMB;
import static impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild.NONE;
import static impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild.RANGE_BAR;

import java.util.List;

import impl.org.controlsfx.behavior.RangeSliderBehavior;
import impl.org.controlsfx.behavior.RangeSliderBehavior.FocusedChild;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import org.controlsfx.control.RangeSlider;

import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;

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
    
    private final RangeSliderBehavior rangeSliderBehavior;

    public RangeSliderSkin(final RangeSlider rangeSlider) {
        super(rangeSlider);
        orientation = getSkinnable().getOrientation();
        rangeSliderBehavior = new RangeSliderBehavior(rangeSlider);
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
        lowThumb.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (hasFocus) {
                    currentFocus = LOW_THUMB;
                }
            }
        });
        highThumb.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (hasFocus) {
                    currentFocus = HIGH_THUMB;
                }
            }
        });
        rangeBar.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (hasFocus) {
                    currentFocus = RANGE_BAR;
                }
            }
        });
        rangeSlider.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean hasFocus) {
                if (hasFocus) {
                    lowThumb.setFocus(true);
                } else {
                    lowThumb.setFocus(false);
                    highThumb.setFocus(false);
                    currentFocus = NONE;
                }
            }
        });

        EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
            /** The algorithm to be passed to
             * {@link ParentTraversalEngine}{@code 's} constructor. */
            private final Algorithm algorithm = new Algorithm() {
                
                @Override
                public Node selectLast(final TraversalContext context) {
                    final List<Node> focusTraversableNodes =
                            context.getAllTargetNodes();
                    
                    return focusTraversableNodes.get(
                            focusTraversableNodes.size() - 1);
                }
                
                
                /**
                 * Method is not used in current implementation. But in case of
                 * using may return RangeSlider object itself instead of
                 * {@link #lowThumb}, if RangeSlider is the first
                 * focus traversable Node on the root pane.
                 */
                @Override
                public Node selectFirst(final TraversalContext context) {
                    return context.getAllTargetNodes().get(0);
                }
                
                
                /**
                 * In case of passing {@link Direction#LEFT},
                 * {@link Direction#PREVIOUS} or {@link Direction#UP} returns
                 * Node <u>before</u> previous.
                 * This method is called when pressing Shift + Tab on
                 * the focused {@link #lowThumb}. The Node before 'lowThumb' is
                 * RangeSlider object itself, so visually focus is lost and
                 * focus traversing becomes distrupted.
                 */
                @Override
                public Node select(
                        final Node owner,
                        final Direction dir,
                        final TraversalContext context) {
                    /* Determines focus moving direction to next
                     * focus traversable node */
                    final int direction;
                    
                    switch (dir)
                    {
                    case DOWN:
                    case RIGHT:
                    case NEXT:
                    case NEXT_IN_LINE:
                        direction = 1;
                        
                        break;
                        
                        
                    case LEFT:
                    case PREVIOUS:
                    case UP:
                        direction = -2;
                        
                        break;
                        
                        
                    default:
                        throw new EnumConstantNotPresentException(
                                dir.getClass(), dir.name());
                    }
                    
                    final List<Node> focusTraversableNodes =
                            context.getAllTargetNodes();
                    final int focusReceiverIndex =
                            focusTraversableNodes.indexOf(owner) + direction;
                    
                    if (focusReceiverIndex < 0) {
                        return focusTraversableNodes.get(
                                focusTraversableNodes.size() - 1);
                    }
                    if (focusReceiverIndex == focusTraversableNodes.size()) {
                        return focusTraversableNodes.get(0);
                    }
                    
                    return focusTraversableNodes.get(focusReceiverIndex);
                }
            };
            
            @Override public void handle(KeyEvent event) {
                if (KeyCode.TAB.equals(event.getCode())) {
                    if (lowThumb.isFocused()) {
                        if (event.isShiftDown()) {
                            lowThumb.setFocus(false);
                            new ParentTraversalEngine(
                                    rangeSlider.getScene().getRoot(), algorithm)
                            .select(lowThumb, Direction.PREVIOUS)
                            .requestFocus();
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
                            highThumb.setFocus(false);
                            new ParentTraversalEngine(
                                    rangeSlider.getScene().getRoot(), algorithm)
                            .select(highThumb, Direction.NEXT)
                            .requestFocus();
                        }
                        event.consume();
                    }
                }
            }
        };
        getSkinnable().addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);  
        // set up a callback on the behavior to indicate which thumb is currently 
        // selected (via enum).
        rangeSliderBehavior.setSelectedValue(new Callback<Void, FocusedChild>() {
            @Override public FocusedChild call(Void v) {
                return currentFocus;
            }
        });
    }
    
    private void initFirstThumb() {
        lowThumb = new ThumbPane();
        lowThumb.getStyleClass().setAll("low-thumb"); //$NON-NLS-1$
        lowThumb.setFocusTraversable(true);
        track = new StackPane();
        track.getStyleClass().setAll("track"); //$NON-NLS-1$

        getChildren().clear();
        getChildren().addAll(track, lowThumb);
        setShowTickMarks(getSkinnable().isShowTickMarks(), getSkinnable().isShowTickLabels());
        track.setOnMousePressed( new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                if (!lowThumb.isPressed() && !highThumb.isPressed()) {
                    if (isHorizontal()) {
                        rangeSliderBehavior.trackPress(me, (me.getX() / trackLength));
                    } else {
                        rangeSliderBehavior.trackPress(me, (me.getY() / trackLength));
                    }
                }
            }
        });

        track.setOnMouseReleased( new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                 //Nothing being done with the second param in sliderBehavior
                //So, passing a dummy value
                rangeSliderBehavior.trackRelease(me, 0.0f);
            }
        });

        lowThumb.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                highThumb.setFocus(false);
                lowThumb.setFocus(true);
                rangeSliderBehavior.lowThumbPressed(me, 0.0f);
                preDragThumbPoint = lowThumb.localToParent(me.getX(), me.getY());
                preDragPos = (getSkinnable().getLowValue() - getSkinnable().getMin()) /
                        (getMaxMinusMinNoZero());
            }
        });

        lowThumb.setOnMouseReleased(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                rangeSliderBehavior.lowThumbReleased(me);
            }
        });

        lowThumb.setOnMouseDragged(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override public void handle(javafx.scene.input.MouseEvent me) {
                Point2D cur = lowThumb.localToParent(me.getX(), me.getY());
                double dragPos = (isHorizontal())?
                    cur.getX() - preDragThumbPoint.getX() : -(cur.getY() - preDragThumbPoint.getY());
                rangeSliderBehavior.lowThumbDragged(me, preDragPos + dragPos / trackLength);
            }
        });
    }

    private void initSecondThumb() {
        highThumb = new ThumbPane();
        highThumb.getStyleClass().setAll("high-thumb"); //$NON-NLS-1$
        highThumb.setFocusTraversable(true);
        if (!getChildren().contains(highThumb)) {
            getChildren().add(highThumb);
        }

        highThumb.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                lowThumb.setFocus(false);
                highThumb.setFocus(true);
                rangeSliderBehavior.highThumbPressed(e, 0.0D);
                preDragThumbPoint = highThumb.localToParent(e.getX(), e.getY());
                preDragPos = (((RangeSlider) getSkinnable()).getHighValue() - ((RangeSlider) getSkinnable()).getMin()) / 
                            (getMaxMinusMinNoZero());
            }
        }
        );
        highThumb.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                rangeSliderBehavior.highThumbReleased(e);
            }
        }
        );
        highThumb.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                boolean orientation = ((RangeSlider) getSkinnable()).getOrientation() == Orientation.HORIZONTAL;
                double trackLength = orientation ? track.getWidth() : track.getHeight();

                Point2D point2d = highThumb.localToParent(e.getX(), e.getY());
                double d = ((RangeSlider) getSkinnable()).getOrientation() != Orientation.HORIZONTAL ? -(point2d.getY() - preDragThumbPoint.getY()) : point2d.getX() - preDragThumbPoint.getX();
                rangeSliderBehavior.highThumbDragged(e, preDragPos + d / trackLength);
            }
        });
    }
    
    private void initRangeBar() {
        rangeBar = new StackPane();
        rangeBar.cursorProperty().bind(new ObjectBinding<Cursor>() {
            { bind(rangeBar.hoverProperty()); }

            @Override protected Cursor computeValue() {
                return rangeBar.isHover() ? Cursor.HAND : Cursor.DEFAULT;
            }
        });
        rangeBar.getStyleClass().setAll("range-bar"); //$NON-NLS-1$
        
        rangeBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                rangeBar.requestFocus();
                preDragPos = isHorizontal() ? e.getX() : -e.getY();
            }
        });
        
        rangeBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                double delta = (isHorizontal() ? e.getX() : -e.getY()) - preDragPos;
                rangeSliderBehavior.moveRange(delta);
            }
        });
        
         rangeBar.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent e) {
                rangeSliderBehavior.confirmRange();
            }
        });
        
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
            double totalWidthNeeded = trackAreaWidth  + ((showTickMarks) ? trackToTickGap+tickLineWidth : 0) ;
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
        return 2*lowThumb.prefWidth(-1);
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

    private static class ThumbPane extends StackPane {
        public void setFocus(boolean value) {
            setFocused(value);
        }
    }
}
