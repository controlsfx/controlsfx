/**
 * Copyright (c) 2023 ControlsFX
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
package impl.org.controlsfx.tableview2.filter.parser.comparable;

import impl.org.controlsfx.tableview2.filter.parser.Operation;
import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import javafx.util.StringConverter;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

public class ComparableParser<T extends Comparable<T>> implements Parser<T> {
    private final ComparableOperation EQUALS = new ComparableOperation("text.equals", "symbol.equals") {
        @Override
        public Predicate<T> operate(T obj) {
            return obj::equals;
        }
    };

    private final ComparableOperation NOT_EQUALS = new ComparableOperation("text.notequals", "symbol.notequals") {
        @Override
        public Predicate<T> operate(T obj) {
            return t -> !obj.equals(t);
        }
    };

    private final ComparableOperation GREATER_THAN_EQUALS = new ComparableOperation("text.greaterthanequals", "symbol.greaterthanequals") {
        @Override
        public Predicate<T> operate(T obj) {
            return t -> t != null && t.compareTo(obj) >= 0;
        }
    };

    private final ComparableOperation GREATER_THAN = new ComparableOperation("text.greaterthan", "symbol.greaterthan") {
        @Override
        public Predicate<T> operate(T obj) {
            return t -> t != null && t.compareTo(obj) > 0;
        }
    };

    private final ComparableOperation LESS_THAN_EQUALS = new ComparableOperation("text.lessthanequals", "symbol.lessthanequals") {
        @Override
        public Predicate<T> operate(T obj) {
            return t -> t != null && t.compareTo(obj) <= 0;
        }
    };

    private final ComparableOperation LESS_THAN = new ComparableOperation("text.lessthan", "symbol.lessthan") {
        @Override
        public Predicate<T> operate(T obj) {
            return t -> t != null && t.compareTo(obj) < 0;
        }
    };

    private final List<ComparableOperation> operations = List.of(EQUALS, NOT_EQUALS, GREATER_THAN_EQUALS, GREATER_THAN, LESS_THAN_EQUALS, LESS_THAN);

    private final StringConverter<T> stringConverter;

    private String errorString = "";

    public ComparableParser(StringConverter<T> stringConverter) {
        this.stringConverter = stringConverter;
    }

    @Override
    public Predicate<T> parse(String text) {
        errorString = "";
        Predicate<T> aggregation = aggregate(text);
        if (aggregation == null) {
            Optional<ComparableOperation> optionalOperation = operations.stream()
                    .filter(opr -> text.startsWith(opr.get()))
                    .filter(opr -> text.length() > opr.length())
                    .findFirst();
            if (optionalOperation.isPresent()) {
                ComparableOperation operation = optionalOperation.get();
                String objText = text.substring(operation.length()).trim();
                T obj = stringConverter.fromString(objText);
                if (obj == null) {
                    errorString = localize(asKey("parser.text.error.comparable.input"));
                    return null;
                }
                return operation.operate(obj);
            }
        }
        return aggregation;
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
        return operations.stream().filter(op -> op.get().equals(text)).map(Operation::getSymbol).findFirst().orElse(i18nString("symbol.default"));
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

    private abstract class ComparableOperation implements Operation<T, T> {
        private final String opr;
        private final String symbol;

        ComparableOperation(String opr, String symbol) {
            this.opr = i18nString(opr);
            this.symbol = i18nString(symbol);
        }

        @Override
        public int length() {
            return opr.length();
        }

        @Override
        public String get() {
            return opr;
        }

        @Override
        public String getSymbol() {
            return symbol;
        }
    }

    private static String i18nString(String key) {
        return localize(asKey("parser.text.operator." + key));
    }
}
