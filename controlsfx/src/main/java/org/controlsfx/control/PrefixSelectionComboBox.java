/**
 * Copyright (c) 2015, ControlsFX
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
package org.controlsfx.control;

import impl.org.controlsfx.tools.PrefixSelectionCustomizer;
import static impl.org.controlsfx.tools.PrefixSelectionCustomizer.DEFAULT_LOOKUP_COMBOBOX;
import static impl.org.controlsfx.tools.PrefixSelectionCustomizer.DEFAULT_TYPING_DELAY;
import java.util.Optional;
import java.util.function.BiFunction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;

/**
 * A simple extension of the {@link ComboBox} which selects an entry of
 * its item list based on keyboard input. The user can  type letters or 
 * digits on the keyboard and the control will attempt to
 * select the first item it can find with a matching prefix.
 * 
 * This will only be enabled, when the {@link ComboBox} is not editable, so
 * this class will be setup as non editable by default.
 *
 * <p>This feature is available natively on the Windows combo box control, so many
 * users have asked for it. There is a feature request to include this feature
 * into JavaFX (<a href="https://javafx-jira.kenai.com/browse/RT-18064">Issue RT-18064</a>). 
 * The class is published as part of ControlsFX to allow testing and feedback.
 * 
 * <h3>Example</h3>
 * 
 * <p>Let's look at an example to clarify this. The combo box offers the items 
 * ["Aaaaa", "Abbbb", "Abccc", "Abcdd", "Abcde"]. The user now types "abc" in 
 * quick succession (and then stops typing). The combo box will select a new entry 
 * on every key pressed. The first entry it will select is "Aaaaa" since it is the 
 * first entry that starts with an "a" (case ignored). It will then select "Abbbb", 
 * since this is the first entry that started with "ab" and will finally settle for 
 * "Abccc".
 * 
 * <ul><table>
 *   <tr><th>Keys typed</th><th>Element selected</th></tr>
 *   <tr><td>a</td><td>Aaaaa<td></tr>
 *   <tr><td>aaa</td><td>Aaaaa<td></tr>
 *   <tr><td>ab</td><td>Abbbb<td></tr>
 *   <tr><td>abc</td><td>Abccc<td></tr>
 *   <tr><td>xyz</td><td>-<td></tr>
 * </table></ul>
 * 
 * <p>If you want to modify an existing {@link ComboBox} you can use the
 * {@link PrefixSelectionCustomizer} directly to do this.
 * 
 * @see PrefixSelectionCustomizer
 */
public class PrefixSelectionComboBox<T> extends ComboBox<T> {
    
    private final ChangeListener<Boolean> focusedListener = (obs, ov, nv) -> {
            if (nv) {
                show();
            }
        };
    
    /**
     * Create a non editable {@link ComboBox} with the "prefix selection"
     * feature installed.
     */
    public PrefixSelectionComboBox() {
        setEditable(false);
        PrefixSelectionCustomizer.customize(this);
    }
    
    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    
    // --- displayOnFocusedEnabled
    /**
     * When enabled, the {@link ComboBox} will display its popup upon focus gained.
     * Default is false
     */
    private final BooleanProperty displayOnFocusedEnabled = new SimpleBooleanProperty(this, "displayOnFocusedEnabled", false) {
        @Override
        protected void invalidated() {
            if (get()) {
                focusedProperty().addListener(focusedListener);
            } else {
                focusedProperty().removeListener(focusedListener);
            }
        }
    };
    public final boolean isDisplayOnFocusedEnabled() { return displayOnFocusedEnabled.get(); }
    public final void setDisplayOnFocusedEnabled(boolean value) { displayOnFocusedEnabled.set(value); }
    public final BooleanProperty displayOnFocusedEnabledProperty() { return displayOnFocusedEnabled; }
    
    // --- backSpaceAllowed
    /**
     * When allowed, the user can press on the back space to clear the current 
     * selection.
     * Default is false
     */
    private final BooleanProperty backSpaceAllowed = new SimpleBooleanProperty(this, "backSpaceAllowed", false);
    public final boolean isBackSpaceAllowed() { return backSpaceAllowed.get(); }
    public final void setBackSpaceAllowed(boolean value) { backSpaceAllowed.set(value); }
    public final BooleanProperty backSpaceAllowedProperty() { return backSpaceAllowed; }
    
    // --- typingDelay
    /**
     * Allows setting the delay until the current selection is reset, in ms. 
     * Default is 500 ms
     */
    private final IntegerProperty typingDelay = new SimpleIntegerProperty(this, "typingDelay", DEFAULT_TYPING_DELAY);
    public final int getTypingDelay() { return typingDelay.get(); }
    public final void setTypingDelay(int value) { typingDelay.set(value); }
    public final IntegerProperty typingDelayProperty() { return typingDelay; }
    
    // --- lookup
    /**
     * Allows setting a custom search criteria, based on the control and the typed 
     * selection.
     * The default criteria searchs for the first matching item that starts with 
     * the typed selection, being case insenstitive.
     */
    private final ObjectProperty<BiFunction<ComboBox, String, Optional>> lookup = new SimpleObjectProperty<>(this, "lookup", DEFAULT_LOOKUP_COMBOBOX);
    public final BiFunction<ComboBox, String, Optional> getLookup() { return lookup.get(); }
    public final void setLookup(BiFunction<ComboBox, String, Optional> value) { lookup.set(value); }
    public final ObjectProperty<BiFunction<ComboBox, String, Optional>> lookupProperty() { return lookup; }
    
}
