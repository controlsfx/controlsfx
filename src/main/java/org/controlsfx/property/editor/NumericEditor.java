/**
 * Copyright (c) 2013, ControlsFX All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: * Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. * Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the names of its contributors may
 * be used to endorse or promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.property.editor;

import java.lang.reflect.InvocationTargetException;

import javafx.beans.value.ObservableValue;

import org.controlsfx.control.PropertySheet.Item;

/**
 * A {@link PropertyEditor} that is suitable for use for editing numeric properties.
 */
public class NumericEditor extends AbstractPropertyEditor<Number, NumericField> {

    private Class<? extends Number> sourceClass = Double.class;

    /**
     * Creates a default NumericEditor instance that will edit the given 
     * {@link item}.
     * 
     * @param item The item that this editor instance should be responsible for 
     *      editing.
     */
    public NumericEditor(Item item) {
        super(item, new NumericField());
        EditorUtils.enableAutoSelectAll(control);
    }

    /**
     * {@inheritDoc}
     */
    @Override protected ObservableValue<Number> getObservableValue() {
        return control.valueProperty();
    }

    /**
     * {@inheritDoc}
     */
    @Override public Number getValue() {
        try {
            return sourceClass.getConstructor(String.class).newInstance(control.getText());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked") @Override public void setValue(Number value) {
        sourceClass = (Class<? extends Number>) value.getClass();
        control.setText(value.toString());
    }
}
