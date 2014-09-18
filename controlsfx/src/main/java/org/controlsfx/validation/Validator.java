/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.validation;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.Control;

/**
 * Interface defining the contract for validation of specific component
 * This interface is a {@link BiFunction} which when given the control and its current value 
 * computes the validation result 
 * 
 * @param <T> type of the controls value
 */
public interface Validator<T> extends BiFunction<Control, T, ValidationResult> {
    
    /**
     * Combines the given validators into a single Validator instance. 
     * @param validators the validators to combine
     * @return a Validator instance
     */
    @SafeVarargs
    static <T> Validator<T> combine(Validator<T>... validators) {
        return (control, value) -> Stream.of(validators)
            .map(validator -> validator.apply(control, value))
            .collect(Collectors.reducing(new ValidationResult(), ValidationResult::combine));
    }

    /**
     * Factory method to create a validator, which checks if value exists. 
     * @param message text of a message to be created if value is invalid
     * @param severity severity of a message to be created if value is invalid
     * @return new validator
     */
    public static <T> Validator<T> createEmptyValidator(final String message, final Severity severity) {
        return (c, value) -> {
            boolean condition = value instanceof String ? value.toString().trim().isEmpty() : value == null;
            return ValidationResult.fromMessageIf(c, message, severity, condition);
        };
    }

    /**
     * Factory method to create a validator, which checks if value exists. 
     * Error is created if not if value does not exist 
     * @param message of a error to be created if value is invalid
     * @return new validator
     */
    public static <T> Validator<T> createEmptyValidator(final String message) {
        return createEmptyValidator(message, Severity.ERROR);
    }

    /**
     * Factory method to create a validator, which if value exists in the provided collection. 
     * @param values text of a message to be created if value is not found
     * @param severity severity of a message to be created if value is found
     * @return new validator
     */
    public static <T> Validator<T> createEqualsValidator(final String message, final Severity severity, final Collection<T> values) {
        return (c, value) -> ValidationResult.fromMessageIf(c,message,severity, !values.contains(value));
    }

    /**
     * Factory method to create a validator, which checks if value exists in the provided collection. 
     * Error is created if not found 
     * @param message text of a error to be created if value is not found
     * @param values
     * @return new validator
     */
    public static <T> Validator<T> createEqualsValidator(final String message, final Collection<T> values) {
        return createEqualsValidator(message, Severity.ERROR, values);
    }
    
    /**
     * Factory method to create a validator, which evaluates the value validity with a given predicate.
     * Error is created if the evaluation is <code>false</code>.
     * @param message text of a message to be created if value is invalid
     * @param predicate the predicate to be used for the value validity evaluation.
     * @return new validator
     */
    static <T> Validator<T> createPredicateValidator(Predicate<T> predicate, String message) {
        return createPredicateValidator(predicate, message, Severity.ERROR);
    }
    
    /**
     * Factory method to create a validator, which evaluates the value validity with a given predicate.
     * Error is created if the evaluation is <code>false</code>.
     * @param message text of a message to be created if value is invalid
     * @param predicate the predicate to be used for the value validity evaluation.
     * @param severity severity of a message to be created if value is invalid
     * @return new validator
     */
    static <T> Validator<T> createPredicateValidator(Predicate<T> predicate, String message, Severity severity) {
        return (control, value) -> ValidationResult.fromMessageIf(
            control, message,
            severity,
            predicate.test(value) == false);
    }

    /**
     * Factory method to create a validator, which checks the value against a given regular expression.
     * Error is created if the value is <code>null</code> or the value does not match the pattern.
     * @param message text of a message to be created if value is invalid
     * @param regex the regular expression the value has to match
     * @param severity severity of a message to be created if value is invalid
     * @return new validator
     */
    public static Validator<String> createRegexValidator(final String message, final String regex, final Severity severity) {
        return (c, value) -> {
            boolean condition = value == null ? true : !Pattern.matches(regex, value);
            return ValidationResult.fromMessageIf(c, message, severity, condition);
        };
    }
    
    /**
     * Factory method to create a validator, which checks the value against a given regular expression.
     * Error is created if the value is <code>null</code> or the value does not match the pattern.
     * @param message text of a message to be created if value is invalid
     * @param regex the regular expression the value has to match
     * @param severity severity of a message to be created if value is invalid
     * @return new validator
     */
    public static Validator<String> createRegexValidator(final String message, final Pattern regex, final Severity severity) {
        return (c, value) -> {
            boolean condition = value == null ? true : !regex.matcher(value).matches();
            return ValidationResult.fromMessageIf(c, message, severity, condition);
        };
    }
}
