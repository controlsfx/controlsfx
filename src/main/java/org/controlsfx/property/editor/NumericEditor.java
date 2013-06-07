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
import javafx.scene.control.IndexRange;
import javafx.scene.control.TextField;

import org.controlsfx.property.PropertyDescriptor;



public class NumericEditor extends AbstractPropertyEditor<NumericField> {

    private Class<? extends Number> sourceClass = Double.class;

    public NumericEditor(PropertyDescriptor property) {
        super(property, new NumericField());
        EditorUtils.enableAutoSelectAll(control);
    }

    @Override protected ObservableValue<?> getObservableValue() {
        return control.textProperty();
    }

    @Override public Object getValue() {
        try {
            return sourceClass.getConstructor(String.class).newInstance(control.getText());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked") 
    @Override public void setValue(Object value) {
        if (value instanceof Number) {
            sourceClass = (Class<? extends Number>) value.getClass();
            control.setText(value.toString());
        }
    }

}

// TODO: Expose as new control?
class NumericField extends TextField {

    private static String regex = "[-+]?[0-9]*\\.?[0-9]+";

    @Override public void replaceText(int start, int end, String text) {
        if (replaceMatches(start, end, text)) {
            super.replaceText(start, end, text);
        }
    }

    @Override public void replaceSelection(String text) {
        IndexRange range = getSelection();
        if (replaceMatches(range.getStart(), range.getEnd(), text)) {
            super.replaceSelection(text);
        }
    }

    private Boolean replaceMatches(int start, int end, String fragment) {
        return (getText().substring(0, start) + fragment + getText().substring(end)).matches(regex);
    }

}
