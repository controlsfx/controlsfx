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
import javafx.scene.control.ComboBox;

/**
 * A simple extension of the {@link ComboBox} which selects an entry of
 * its item list based on keyboard input. The user can  type letters or 
 * digits on the keyboard and die ChoiceBox will attempt to
 * select the first item it can find with a matching prefix.
 * 
 * This will only be enabled, when the {@link ComboBox} is not editable, so
 * this class will be setup as noneditable by default.
 *
 * This feature has been available on the Windows ComboBox control, so many
 * users have asked for it. There is a feature request to include this feature
 * into JavaFX directly. The class is published as part of ContorlsFX to
 * allow testing and feedback.
 * 
 * See: https://javafx-jira.kenai.com/browse/RT-18064
 * 
 * If you want to modify an existing {@link ComboBox} you can use the
 * {@link PrefixSelectionCustomizer} directly to do this.
 */
public class PrefixSelectionComboBox<T> extends ComboBox<T> {

    public PrefixSelectionComboBox() {
        setEditable(false);
        PrefixSelectionCustomizer.customize(this);
    }
    
}
