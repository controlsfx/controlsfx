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
package org.controlsfx.control.tableview2.filter.parser;

import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser.Aggregation;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A parser accepts a string and returns a {@link Predicate} depending on its
 * implementation.
 *
 * @param <T> Type of input to the Parser
 */
public interface Parser<T> {

    Predicate<T> parse(String text);

    /**
     * Returns the list of operators which can be used with this parser.
     * @return A list of operators.
     */
    List<String> operators();

    /**
     * Return the symbol related to an operator text which can be used 
     * with this parser.
     * 
     * @param text The operator text
     * @return A symbol.
     */
    String getSymbol(String text);

    /**
     * Checks if the supplied text is valid or not.
     * @param text The text to be validated.
     * @return true if text is valid, else returns false.
     */
    boolean isValid(String text);

    /**
     * A string with the error message if the parser fails to parse the supplied string.
     * @return An error message if the parse fails.
     */
    String getErrorMessage();

    /**
     * Aggregates a text i.e. performs aggregation before parsing the text.
     * @param text The text on which aggregation is to be performed
     * @return A predicated by aggregating the text or null if the text cannot be aggregated.
     */
    default Predicate<T> aggregate(String text) {
        Optional<Aggregation> optionalAggregation = AggregatorsParser.findParser(text);
        if (optionalAggregation.isPresent()) {
            Aggregation aggregation = optionalAggregation.get();
            return aggregation.getParser().aggregate(aggregation.getLhs(), aggregation.getRhs(), this);
        }
        return null;
    }
}
