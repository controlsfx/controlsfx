/**
 * Copyright (c) 2015 ControlsFX
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Callback;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 * A default implementation of the {@link Callback} type required by the
 * {@link PropertySheet} 
 * {@link PropertySheet#propertyEditorFactory() property editor factory}. By 
 * default this is the implementation used by PropertySheet, but developers may
 * choose to provide their own, or more likely, extend this implementation
 * and override the {@link DefaultPropertyEditorFactory#call(org.controlsfx.control.PropertySheet.Item) } method to 
 * add in support for additional editor types.
 *
 * @see PropertySheet
 */
public class DefaultPropertyEditorFactory implements Callback<Item, PropertyEditor<?>> {
    
    @Override public PropertyEditor<?> call(Item item) {
        Class<?> type = item.getType();
        
        //TODO: add support for char and collection editors
        
        if (item.getPropertyEditorClass().isPresent()) {
            Optional<PropertyEditor<?>> ed = Editors.createCustomEditor(item);
            if (ed.isPresent()) return ed.get();
        }
        
        if (/*type != null &&*/ type == String.class) {
            return Editors.createTextEditor(item);  
        }

        if (/*type != null &&*/ isNumber(type)) {
            return Editors.createNumericEditor(item);
        }
        
        if (/*type != null &&*/(type == boolean.class || type == Boolean.class)) {
            return Editors.createCheckEditor(item);
        }

        if (/*type != null &&*/type == LocalDate.class) {
            return Editors.createDateEditor(item);
        }
        
        if (/*type != null &&*/type == Color.class || type == Paint.class) {
            return Editors.createColorEditor(item);
        }

        if (type != null && type.isEnum()) {
            return Editors.createChoiceEditor(item, Arrays.<Object>asList(type.getEnumConstants()));
        }
        
        if (/*type != null &&*/type == Font.class) {
            return Editors.createFontEditor(item);
        }
        
        return null; 
    }
    
    private static Class<?>[] numericTypes = new Class[]{
        byte.class, Byte.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        BigInteger.class, BigDecimal.class
    };    
    
    // there should be better ways to do this
    private static boolean isNumber(Class<?> type)  {
        if ( type == null ) return false;
        for (Class<?> cls : numericTypes) {
            if ( type == cls ) return true;
        }
        return false;
    }
}
