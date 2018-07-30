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
package impl.org.controlsfx.tableview2.filter.parser.aggregate;

import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

public enum AggregatorsParser implements Aggregator {

    AND ("text.and") {
        @Override
        public <T> Predicate<T> aggregate(String lhs, String rhs, Parser<T> parser) {
            if (parser.isValid(lhs) && parser.isValid(rhs)) {
                final Predicate<T> parsedLhs = parser.parse(lhs);
                final Predicate<T> parsedRhs = parser.parse(rhs);
                if (parsedLhs != null && parsedRhs != null) {
                    return parsedLhs.and(parsedRhs);
                }
            }
            return null;
        }
    },

    OR ("text.or") {
        @Override
        public <T> Predicate<T> aggregate(String lhs, String rhs, Parser<T> parser) {
            if (parser.isValid(lhs) && parser.isValid(rhs)) {
                final Predicate<T> parsedLhs = parser.parse(lhs);
                final Predicate<T> parsedRhs = parser.parse(rhs);
                if (parsedLhs != null && parsedRhs != null) {
                    return parsedLhs.or(parsedRhs);
                }
            }
            return null;
        }
    };

    private final String type;

    AggregatorsParser(String type) {
        this.type = " " + i18nString(type) + " ";
    }

    public String get() {
        return type;
    }

    public static Stream<String> getStrings() {
        return Arrays.stream(values()).map(AggregatorsParser::get);
    }

    public static Optional<Aggregation> findParser(String text) {
        AggregatorsParser minAggregator= null;
        int indexOf = text.length();
        for (AggregatorsParser aggregatorsParser : values()) {
            int textIndexOf = text.indexOf(aggregatorsParser.get());
            if (textIndexOf != -1 && textIndexOf < indexOf) {
                indexOf = textIndexOf;
                minAggregator = aggregatorsParser;
            }
        }
        if (minAggregator != null) {
            String lhs = text.substring(0, indexOf);
            String rhs = text.substring(indexOf + minAggregator.get().length(), text.length());
            return Optional.of(new Aggregation(lhs.trim(), rhs.trim(), minAggregator));
        }
        return Optional.empty();
    }

    public static class Aggregation {

        private final String lhs;
        private final String rhs;
        private final AggregatorsParser parser;

        Aggregation(String lhs, String rhs, AggregatorsParser parser) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.parser = parser;
        }

        public String getLhs() {
            return lhs;
        }

        public String getRhs() {
            return rhs;
        }

        public AggregatorsParser getParser() {
            return parser;
        }
    }
    
    private static String i18nString(String key) {
        return localize(asKey("parser.text.operator." + key));
    }
}
