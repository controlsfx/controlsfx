/**
 * Copyright (c) 2013, ControlsFX
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
package impl.org.controlsfx.behavior;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.END;
import static javafx.scene.input.KeyCode.F4;
import static javafx.scene.input.KeyCode.HOME;
import static javafx.scene.input.KeyCode.KP_DOWN;
import static javafx.scene.input.KeyCode.KP_LEFT;
import static javafx.scene.input.KeyCode.KP_RIGHT;
import static javafx.scene.input.KeyCode.KP_UP;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;

import java.util.ArrayList;
import java.util.List;

import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import org.controlsfx.control.RangeSlider;
import org.controlsfx.tools.Utils;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.behavior.OrientedKeyBinding;

public class RangeSliderBehavior extends BehaviorBase<RangeSlider> {
    
     /**************************************************************************
     *                          Setup KeyBindings                             *
     *                                                                        *
     * We manually specify the focus traversal keys because Slider has        *
     * different usage for up/down arrow keys.                                *
     *************************************************************************/
    private static final List<KeyBinding> RANGESLIDER_BINDINGS = new ArrayList<>();
    static {
        RANGESLIDER_BINDINGS.add(new KeyBinding(F4, "TraverseDebug").alt().ctrl().shift()); //$NON-NLS-1$

        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(LEFT, "DecrementValue")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_LEFT, "DecrementValue")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(UP, "IncrementValue").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_UP, "IncrementValue").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(RIGHT, "IncrementValue")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_RIGHT, "IncrementValue")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(DOWN, "DecrementValue").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_DOWN, "DecrementValue").vertical()); //$NON-NLS-1$

        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(LEFT, "TraverseLeft").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_LEFT, "TraverseLeft").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(UP, "TraverseUp")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_UP, "TraverseUp")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(RIGHT, "TraverseRight").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_RIGHT, "TraverseRight").vertical()); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(DOWN, "TraverseDown")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new RangeSliderKeyBinding(KP_DOWN, "TraverseDown")); //$NON-NLS-1$

        RANGESLIDER_BINDINGS.add(new KeyBinding(HOME, KEY_RELEASED, "Home")); //$NON-NLS-1$
        RANGESLIDER_BINDINGS.add(new KeyBinding(END, KEY_RELEASED, "End")); //$NON-NLS-1$
    }
    
    public RangeSliderBehavior(RangeSlider slider) {
        super(slider, RANGESLIDER_BINDINGS);
    }

    @Override protected void callAction(String s) {
        if ("Home".equals(s) || "Home2".equals(s)) home(); //$NON-NLS-1$ //$NON-NLS-2$
        else if ("End".equals(s) || "End2".equals(s)) end(); //$NON-NLS-1$ //$NON-NLS-2$
        else if ("IncrementValue".equals(s) || "IncrementValue2".equals(s)) incrementValue(); //$NON-NLS-1$ //$NON-NLS-2$
        else if ("DecrementValue".equals(s) || "DecrementValue2".equals(s)) decrementValue(); //$NON-NLS-1$ //$NON-NLS-2$
        else super.callAction(s);
    }
     
    /**************************************************************************
     *                         State and Functions                            *
     *************************************************************************/

    private Callback<Void, FocusedChild> selectedValue;
    public void setSelectedValue(Callback<Void, FocusedChild> c) {
        selectedValue = c;
    }
    /**
     * Invoked by the RangeSlider {@link Skin} implementation whenever a mouse press
     * occurs on the "track" of the slider. This will cause the thumb to be
     * moved by some amount.
     *
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    public void trackPress(MouseEvent e, double position) {
        // determine the percentage of the way between min and max
        // represented by this mouse event
        final RangeSlider rangeSlider = getControl();
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

    /**
     */
    public void trackRelease(MouseEvent e, double position) {
    }
    
     /**
     * @param position The mouse position on track with 0.0 being beginning of
      *       track and 1.0 being the end
     */
    public void lowThumbPressed(MouseEvent e, double position) {
        // If not already focused, request focus
        final RangeSlider rangeSlider = getControl();
        if (!rangeSlider.isFocused())  rangeSlider.requestFocus();
        rangeSlider.setLowValueChanging(true);
    }

    /**
     * @param position The mouse position on track with 0.0 being beginning of
     *        track and 1.0 being the end
     */
    public void lowThumbDragged(MouseEvent e, double position) {
        final RangeSlider rangeSlider = getControl();
        double newValue = Utils.clamp(rangeSlider.getMin(), 
                (position * (rangeSlider.getMax() - rangeSlider.getMin())) + rangeSlider.getMin(), 
                rangeSlider.getMax());
        rangeSlider.setLowValue(newValue);
    }
    
    /**
     * When lowThumb is released lowValueChanging should be set to false.
     */
    public void lowThumbReleased(MouseEvent e) {
        final RangeSlider rangeSlider = getControl();
        rangeSlider.setLowValueChanging(false);
        // RT-15207 When snapToTicks is true, slider value calculated in drag
        // is then snapped to the nearest tick on mouse release.
        if (rangeSlider.isSnapToTicks()) {
            rangeSlider.setLowValue(snapValueToTicks(rangeSlider.getLowValue()));
        }
    }
    
    void home() {
        RangeSlider slider = (RangeSlider) getControl();
        slider.adjustHighValue(slider.getMin());
    }

    void decrementValue() {
        RangeSlider slider = (RangeSlider) getControl();
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

    void end() {
        RangeSlider slider = (RangeSlider) getControl();
        slider.adjustHighValue(slider.getMax());
    }

    void incrementValue() {
        RangeSlider slider = (RangeSlider) getControl();
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

    double computeIncrement() {
        RangeSlider rangeSlider = (RangeSlider) getControl();
        double d = 0.0D;
        if (rangeSlider.getMinorTickCount() != 0)
            d = rangeSlider.getMajorTickUnit() / (double) (Math.max(rangeSlider.getMinorTickCount(), 0) + 1);
        else
            d = rangeSlider.getMajorTickUnit();
        if (rangeSlider.getBlockIncrement() > 0.0D && rangeSlider.getBlockIncrement() < d)
            return d;
        else
            return rangeSlider.getBlockIncrement();
    }

    private double snapValueToTicks(double d) {
        RangeSlider rangeSlider = (RangeSlider) getControl();
        double d1 = d;
        double d2 = 0.0D;
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

    // when high thumb is released, highValueChanging is set to false.
    public void highThumbReleased(MouseEvent e) {
        RangeSlider slider = (RangeSlider) getControl();
        slider.setHighValueChanging(false);
        if (slider.isSnapToTicks())
            slider.setHighValue(snapValueToTicks(slider.getHighValue()));
    }

    public void highThumbPressed(MouseEvent e, double position) {
        RangeSlider slider = (RangeSlider) getControl();
        if (!slider.isFocused())
            slider.requestFocus();
        slider.setHighValueChanging(true);
    }

    public void highThumbDragged(MouseEvent e, double position) {
        RangeSlider slider = (RangeSlider) getControl();
        slider.setHighValue(Utils.clamp(slider.getMin(), position * (slider.getMax() - slider.getMin()) + slider.getMin(), slider.getMax()));
    }
    
    public void moveRange(double position) {
        RangeSlider slider = (RangeSlider) getControl();
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
    
      public void confirmRange() {
        RangeSlider slider = (RangeSlider) getControl();

        slider.setLowValueChanging(false);
        if (slider.isSnapToTicks()) {
            slider.setLowValue(snapValueToTicks(slider.getLowValue()));
        }
        slider.setHighValueChanging(false);
        if (slider.isSnapToTicks()) {
            slider.setHighValue(snapValueToTicks(slider.getHighValue()));
        }

    }
    
    public static class RangeSliderKeyBinding extends OrientedKeyBinding {
        public RangeSliderKeyBinding(KeyCode code, String action) {
            super(code, action);
        }

        public RangeSliderKeyBinding(KeyCode code, EventType<KeyEvent> type, String action) {
            super(code, type, action);
        }

        public @Override boolean getVertical(Control control) {
            return ((RangeSlider)control).getOrientation() == Orientation.VERTICAL;
        }
    }
     
    public enum FocusedChild {
        LOW_THUMB,
        HIGH_THUMB,
        RANGE_BAR,
        NONE
    }
}

