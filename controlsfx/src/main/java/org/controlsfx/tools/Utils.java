/**
 * Copyright (c) 2014, 2020, ControlsFX
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
package org.controlsfx.tools;

import java.util.List;

import javafx.scene.Node;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public class Utils {

    /**
     * Will return a {@link Window} from an object if any can be found. {@code null}
     * value can be given, the program will then try to find the focused window
     * among those available.
     * 
     * @param owner the object whose window is to be found.
     * @return the window of the given object.
     */
    public static Window getWindow(Object owner) throws IllegalArgumentException {
        if (owner == null) {
            Window window = null;
            // lets just get the focused stage and show the dialog in there
            List<Window> windows = Window.getWindows();
            for (Window w : windows) {
                window = w;
                if (window.isFocused() && !(window instanceof PopupWindow)) {
                    return window;
                }
            }
            return window;
        } else if (owner instanceof Window) {
            return (Window) owner;
        } else if (owner instanceof Node) {
            return ((Node) owner).getScene().getWindow();
        } else {
            throw new IllegalArgumentException("Unknown owner: " + owner.getClass()); //$NON-NLS-1$
        }
    }
    
    /**
     * Return a letter (just like Excel) associated with the number. When the
     * number is under 26, a simple letter is returned. When the number is
     * superior, concatenated letters are returned.
     * 
     * 
     * For example: 0 -&gt; A 1 -&gt; B 26 -&gt; AA 32 -&gt; AG 45 -&gt; AT
     * 
     * 
     * @param number the number whose Excel Letter is to be found.
     * @return a letter (like) associated with the number.
     */
    public static final String getExcelLetterFromNumber(int number) {
        String letter = ""; //$NON-NLS-1$
        // Repeatedly divide the number by 26 and convert the
        // remainder into the appropriate letter.
        while (number >= 0) {
            final int remainder = number % 26;
            letter = (char) (remainder + 'A') + letter;
            number = number / 26 - 1;
        }

        return letter;
    }
	
    /**
     * Simple utility function which clamps the given value to be strictly
     * between the min and max values.
     */
    public static double clamp(double min, double value, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Utility function which returns either {@code less} or {@code more}
     * depending on which {@code value} is closer to. If {@code value}
     * is perfectly between them, then either may be returned.
     */
    public static double nearest(double less, double value, double more) {
        double lessDiff = value - less;
        double moreDiff = more - value;
        if (lessDiff < moreDiff) return less;
        return more;
    }
}
