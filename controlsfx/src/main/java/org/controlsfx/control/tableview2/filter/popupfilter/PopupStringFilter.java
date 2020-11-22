/**
 * Copyright (c) 2018 ControlsFX
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
package org.controlsfx.control.tableview2.filter.popupfilter;

import impl.org.controlsfx.tableview2.filter.parser.string.StringParser;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.StringConverter;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.List;

/**
 * PopupFilter to be used on {@link org.controlsfx.control.tableview2.FilteredTableView}
 * containing cells of String type.
 *
 * @param <S> Type of the objects contained within the 
 *      {@link org.controlsfx.control.tableview2.FilteredTableView} items list.
 * @param <T> Type of the content to be filtered,
 *           which is similar to the type of cells contained in the 
 *      {@link FilteredTableColumn}. Should be
 *      a String or an Object, in case a {@link StringConverter} is provided.
*/
public class PopupStringFilter<S, T> extends PopupFilter<S, T> {

    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/

    private static <T> StringConverter<T> defaultStringConverter() {
        return new StringConverter<T>() {
            @Override public String toString(T t) {
                return t == null ? null : t.toString();
            }

            @Override public T fromString(String string) {
                return (T) string;
            }
        };
    }
    
    private final StringParser<T> stringParser;

    /**
     * Creates a new instance of PopupStringFilter.
     * @param tableColumn TableColumn associated with PopupFilter
     */
    public PopupStringFilter(FilteredTableColumn<S, T> tableColumn) {
        super(tableColumn);
        stringParser = new StringParser<>(caseSensitive.get(), getConverter());
        
        text.addListener((obs, ov, nv) -> {
            if (nv == null || nv.isEmpty()) {
                tableColumn.setPredicate(null);
            } else {
                tableColumn.setPredicate(getParser().parse(nv));
            }
        });
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public List<String> getOperations() {
        return stringParser.operators();
    }

    /**
     *{@inheritDoc}
     */
    @Override
    public Parser<T> getParser() {
        return stringParser;
    }

    private final BooleanProperty caseSensitive = new SimpleBooleanProperty(this, "caseSensitive", true) {
        @Override
        protected void invalidated() {
            stringParser.setCaseSensitive(get());
        }
    };
    public final BooleanProperty caseSensitiveProperty() {
        return caseSensitive;
    }
    
    // --- string converter
    /**
     * Converts the user-typed input (when the PopupStringFilter to an object of 
     * type T.
     */
    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter", PopupStringFilter.defaultStringConverter()) {
        @Override
        protected void invalidated() {
            stringParser.setConverter(get());
        }
    };
    public final ObjectProperty<StringConverter<T>> converterProperty() { return converter; }
    public final void setConverter(StringConverter<T> value) { converterProperty().set(value); }
    public final StringConverter<T> getConverter() { return converterProperty().get(); }

}
