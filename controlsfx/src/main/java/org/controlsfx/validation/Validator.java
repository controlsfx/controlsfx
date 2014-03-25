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

import javafx.scene.control.Control;

public interface Validator<T> extends BiFunction<Control, T, ValidationResult> {
	
	static <T> Validator<T> createEmptyValidator(final String message, final Severity severity ) {
		return new Validator<T>() {

			@Override
			public ValidationResult apply( Control c, T value) {
				return ValidationResult
				  .fromMessageIf(c, message, severity,
				     value instanceof String? value.toString().trim().isEmpty(): value == null);
			}
			
		};
	}
	
	static <T> Validator<T> createEmptyValidator(final String message ) {
		return createEmptyValidator(message, Severity.ERROR);
	}
	
	static <T> Validator<T> createEqualsValidator(final String message, final Severity severity, final Collection<T> values ) {
		
		return new Validator<T>() {

			@Override
			public ValidationResult apply( Control c, T value) {
				return ValidationResult.fromMessageIf(c,message,severity, !values.contains(value)); 
			}
			
		};
	}
	
	
    static <T> Validator<T> createEqualsValidator(final String message, final Collection<T> values ) {
		return createEqualsValidator(message, Severity.ERROR, values);
	}
	
}
