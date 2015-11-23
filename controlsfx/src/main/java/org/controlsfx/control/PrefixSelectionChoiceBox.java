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
import javafx.scene.control.ChoiceBox;

/**
 * <p>A simple extension of the {@link ChoiceBox} which selects an entry of
 * its item list based on keyboard input. The user can type letters or 
 * digits on the keyboard and die {@link ChoiceBox} will attempt to
 * select the first item it can find with a matching prefix.
 *
 * <p>This feature is available natively on the Windows combo box control, so many
 * users have asked for it. There is a feature request to include this feature
 * into JavaFX (<a href="https://javafx-jira.kenai.com/browse/RT-18064">Issue RT-18064</a>). 
 * The class is published as part of ContorlsFX to allow testing and feedback.
 * 
 * <h3>Example</h3>
 * 
 * <p>Let's look at an example to clarify this. The choice box offers the items 
 * ["Aaaaa", "Abbbb", "Abccc", "Abcdd", "Abcde"]. The user now types "abc" in 
 * quick succession (and then stops typing). The choice box will select a new entry 
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
 * <p>If you want to modify an existing {@link ChoiceBox} you can use the
 * {@link PrefixSelectionCustomizer} directly to do this.
 * 
 * @see PrefixSelectionCustomizer
 */
public class PrefixSelectionChoiceBox<T> extends ChoiceBox<T> {

    /**
     * Create a non editable {@link ChoiceBox} with the "prefix selection"
     * feature installed.
     */
    public PrefixSelectionChoiceBox() {
        PrefixSelectionCustomizer.customize(this);
    }
    
}
