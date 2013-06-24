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
package org.controlsfx.property.editor;

import javafx.beans.value.ObservableValue;
import javafx.scene.text.Font;

import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.dialog.Dialogs;

/**
 * A {@link PropertyEditor} that is suitable for use for editing font-related 
 * properties.
 */
public class FontEditor extends AbstractPropertyEditor<Font, AbstractObjectField<Font>> {

    /**
     * Creates a default FontEditor instance that will edit the given 
     * {@link item}.
     * 
     * @param item The item that this editor instance should be responsible for 
     *      editing.
     */
    public FontEditor(Item property) {
        super(property, new AbstractObjectField<Font>() {
            @Override protected Class<Font> getType() {
                return Font.class;
            }
            
            @Override protected String objectToString(Font font) {
                return font == null? "": String.format("%s, %.1f", font.getName(), font.getSize());
            }

            @Override protected Font edit(Font font) {
                return Dialogs.create().owner(this.getScene().getWindow()).showFontSelector(font);
            }
        });
    }
    
    protected ObservableValue<Font> getObservableValue() {
        return getEditor().getObjectProperty();
    }

    @SuppressWarnings("unchecked")
    public void setValue(Font value) {
        getEditor().getObjectProperty().set(value);
    }
}
