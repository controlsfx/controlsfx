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
package impl.org.controlsfx.tableview2.filter.parser.string;

import impl.org.controlsfx.tableview2.filter.parser.Operation;
import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import javafx.util.StringConverter;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

public class StringParser<T> implements Parser<T> {

    private StringConverter<T> converter;
    private boolean caseSensitive;
    private String errorString = "";
    private final List<Operation<T, String>> operations = Arrays.asList(
            new BeginsWith(), new EndsWith(), new Contains(), new EqualsTo(), new NotEqualsTo());

    public StringParser() {}

    public StringParser(boolean caseSensitive) {
        this (caseSensitive, null);
    }

    public StringParser(boolean caseSensitive, StringConverter<T> converter) {
        this.caseSensitive = caseSensitive;
        this.converter = converter;
    }

    public StringConverter<T> getConverter() {
        return converter;
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public Predicate<T> parse(String text) {
        errorString = "";
        Predicate<T> aggregation = aggregate(text);
        if (aggregation == null) {
            Optional<Operation<T, String>> optionalOperation = operations.stream()
                    .filter(opr -> text.startsWith(opr.get()))
                    .filter(opr -> text.length() > opr.length())
                    .findFirst();
            if (optionalOperation.isPresent()) {
                Operation<T, String> opr = optionalOperation.get();
                String trimText = trim(text, opr.length());
                if (trimText.isEmpty()) return null;
                return opr.operate(trimText);
            } else {
                errorString = localize(asKey("parser.text.error.start.operator"));
                return null;
            }
        }
        return aggregation;
    }

    @Override
    public boolean isValid(String text) {
        parse(text);
        return errorString.isEmpty();
    }

    @Override
    public String getErrorMessage() {
        return errorString;
    }

    private String trim(String text, int typeLength) {
        String trimmedText = text.substring(typeLength, text.length()).trim();
        if (!trimmedText.startsWith("\"")) {
            errorString = trimmedText + " " + localize(asKey("parser.text.error.string.start"));
        } else if (!trimmedText.endsWith("\"")) {
            errorString = trimmedText.substring(1) + " " + localize(asKey("parser.text.error.string.end"));
        } else if (trimmedText.length() > 2) {
            return trimmedText.substring(1, trimmedText.length() - 1);
        }
        return "";
    }

    @Override
    public List<String> operators() {
        return Stream.concat(
                operations.stream().map(Operation::get),
                AggregatorsParser.getStrings()
        ).collect(Collectors.toList());
    }
    
    @Override
    public String getSymbol(String text) {
        return operations.stream()
                .filter(op -> op.get().equals(text))
                .map(Operation::getSymbol)
                .findFirst()
                .orElse(i18nString("symbol.default"));
    }

    private String casedString(String string) {
        return caseSensitive ? string : string.toUpperCase();
    }

    class BeginsWith implements Operation<T, String> {

        @Override
        public String get() {
            return i18nString("text.beginswith");
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public Predicate<T> operate(String text) {
            if (converter != null) {
                return t -> t != null && casedString(converter.toString(t)).startsWith(casedString(text));
            }
            return t -> casedString(String.valueOf(t)).startsWith(casedString(text));
        }

        @Override
        public String getSymbol() {
            return i18nString("symbol.beginswith." + (caseSensitive ? "sensitive" : "insensitive"));
        }
        
    }

    class EndsWith implements Operation<T, String> {

        @Override
        public String get() {
            return i18nString("text.endswith");
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public Predicate<T> operate(String text) {
            if (converter != null) {
                return t -> t != null && casedString(converter.toString(t)).endsWith(casedString(text));
            }
            return t -> casedString(String.valueOf(t)).endsWith(casedString(text));
        }
        
        @Override
        public String getSymbol() {
            return i18nString("symbol.endswith." + (caseSensitive ? "sensitive" : "insensitive"));
        }
    }

    class Contains implements Operation<T, String> {

        @Override
        public String get() {
            return i18nString("text.contains");
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public Predicate<T> operate(String text) {
            if (converter != null) {
                return t -> t != null && casedString(converter.toString(t)).contains(casedString(text));
            }
            return t -> casedString(String.valueOf(t)).contains(casedString(text));
        }
        
        @Override
        public String getSymbol() {
            return i18nString("symbol.contains." + (caseSensitive ? "sensitive" : "insensitive"));
        }
    }
    
    class EqualsTo implements Operation<T, String> {

        @Override
        public String get() {
            return i18nString("text.equalsto");
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public Predicate<T> operate(String text) {
            if (converter != null) {
                return t -> t != null && casedString(converter.toString(t)).equals(casedString(text));
            }
            return t -> casedString(String.valueOf(t)).equals(casedString(text));
        }
        
        @Override
        public String getSymbol() {
            return i18nString("symbol.equalsto." + (caseSensitive ? "sensitive" : "insensitive"));
        }
    }
    
    class NotEqualsTo implements Operation<T, String> {

        @Override
        public String get() {
            return i18nString("text.notequalsto");
        }

        @Override
        public int length() {
            return get().length();
        }

        @Override
        public Predicate<T> operate(String text) {
            if (converter != null) {
                return t -> t != null && ! casedString(converter.toString(t)).equals(casedString(text));
            }
            return t -> ! casedString(String.valueOf(t)).equals(casedString(text));
        }
        
        @Override
        public String getSymbol() {
            return i18nString("symbol.notequalsto." + (caseSensitive ? "sensitive" : "insensitive"));
        }
    }
    
    private static String i18nString(String key) {
        return localize(asKey("parser.text.operator." + key));
    }
}
