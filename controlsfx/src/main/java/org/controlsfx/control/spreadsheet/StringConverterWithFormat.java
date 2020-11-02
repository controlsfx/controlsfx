/**
 * Copyright (c) 2013, 2015, ControlsFX
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
package org.controlsfx.control.spreadsheet;

import javafx.util.StringConverter;

/**
 * This class is used by some of the {@link SpreadsheetCellType} in order to use
 * a specific format.<br>
 * 
 * Since the format is specified in the {@link SpreadsheetCell}, we need a
 * converter which provide a runtime method {@link #toStringFormat(Object, String)}.<br>
 * 
 * This class provide two constructors:
 * <ul>
 * <li>A default one where you implement the three abstract methods.</li>
 * <li>Another one which takes another StringConverter. This is useful when you just want to implement 
 * the {@link #toStringFormat(Object, String)} and let the other converter handle the other methods.</li>
 * </ul>
 * 
 * @see SpreadsheetCellType
 * 
 * @param <T>
 */
public abstract class StringConverterWithFormat<T> extends StringConverter<T> {

    protected StringConverter<T> myConverter;

    /**
     * Default constructor.
     */
    public StringConverterWithFormat() {
        super();
    }

    /**
     * This constructor allow to use another StringConverter. 
     * @param specificStringConverter
     */
    public StringConverterWithFormat(StringConverter<T> specificStringConverter) {
        myConverter = specificStringConverter;
    }

    /**
     * Converts the object provided into its string form with the specified format.
     * @param value
     * @param format
     * @return a string containing the converted value with the specified format.
     */
    public String toStringFormat(T value, String format) {
        return toString(value);
    }
}
