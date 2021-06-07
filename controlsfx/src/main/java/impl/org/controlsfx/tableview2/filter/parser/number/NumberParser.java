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
package impl.org.controlsfx.tableview2.filter.parser.number;

import impl.org.controlsfx.tableview2.filter.parser.Operation;
import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

public class NumberParser<T extends Number> implements Parser<T> {

    private String errorString = "";

    @Override
    public Predicate<T> parse(String text) {
        errorString = "";
        Predicate<T> aggregation = aggregate(text);
        if (aggregation == null) {
            Optional<NumberOperation> optionalOperation = Stream.of(NumberOperation.values())
                    .filter(opr -> text.startsWith(opr.get()))
                    .filter(opr -> text.length() > opr.length())
                    .findFirst();
            if (optionalOperation.isPresent()) {
                NumberOperation operation = optionalOperation.get();
                String numText = trim(text, operation.length());
                if (!isNumeric(numText)) {
                    errorString = localize(asKey("parser.text.error.number.input"));
                    return null;
                }
                return (Predicate<T>) operation.operate(convert(numText));
            } else {
                errorString = localize(asKey("parser.text.error.start.operator"));
                return null;
            }
        }
        return aggregation;
    }

    @Override
    public List<String> operators() {
        return Stream.concat(
                Arrays.stream(NumberOperation.values()).map(Operation::get),
                AggregatorsParser.getStrings()
        ).collect(Collectors.toList());
    }

    @Override
    public String getSymbol(String text) {
        return Arrays.stream(NumberOperation.values())
                .filter(op -> op.get().equals(text))
                .map(Operation::getSymbol)
                .findFirst()
                .orElse(i18nString("symbol.default"));
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

    private boolean isNumeric(String str) {
        return !str.isEmpty() && str.matches("-?\\d+(\\.\\d+)?");
    }

    private String trim(String text, int startIndex) {
        return text.substring(startIndex, text.length()).trim();
    }

    private double convert(String numText) {
        return Double.parseDouble(numText);
    }

    private enum NumberOperation implements Operation<Number, Number> {

        EQUALS("text.equals", "symbol.equals") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() == num.doubleValue();
            }
        },
        NOT_EQUALS("text.notequals", "symbol.notequals") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() != num.doubleValue();
            }
        },
        GREATER_THAN_EQUALS("text.greaterthanequals", "symbol.greaterthanequals") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() >= num.doubleValue();
            }
        },
        GREATER_THAN("text.greaterthan", "symbol.greaterthan") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() > num.doubleValue();
            }
        },
        LESS_THAN_EQUALS("text.lessthanequals", "symbol.lessthanequals") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() <= num.doubleValue();
            }
        },
        LESS_THAN("text.lessthan", "symbol.lessthan") {
            @Override
            public Predicate<Number> operate(Number num) {
                return t -> t != null && t.doubleValue() < num.doubleValue();
            }
        };

        private final String opr;
        private final String symbol;

        NumberOperation(String opr, String symbol) {
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
