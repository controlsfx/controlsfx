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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.Control;

public class ValidationResult {

	private List<ValidationMessage> errors   = new ArrayList<>();
	private List<ValidationMessage> warnings = new ArrayList<>();
	
	public ValidationResult() {}
	
	public static final ValidationResult fromMessageIf( Control target, String text, Severity severity, boolean condition ) {
		return new ValidationResult().addMessageIf(target, text, severity, condition);
	}
	
	public static final ValidationResult fromErrorIf( Control target, String text, boolean condition ) {
		return new ValidationResult().addErrorIf(target, text, condition);
	}
	
	public static final ValidationResult fromWarningIf( Control target, String text, boolean condition ) {
		return new ValidationResult().addWarningIf(target, text, condition);
	}
	
	public static final ValidationResult fromWarning( Control target, String text ) {
		return fromMessages( ValidationMessage.warning(target, text));
	}
	
	public static final ValidationResult fromMessages( ValidationMessage... messages ) {
		return new ValidationResult().addAll(messages);
	}
	
	public static final ValidationResult fromMessages( Collection<? extends ValidationMessage> messages ) {
		return new ValidationResult().addAll(messages);
	}
	
	public static final ValidationResult fromResults( ValidationResult... results ) {
		return new ValidationResult().combineAll(results);
	}

	public static final ValidationResult fromResults( Collection<ValidationResult> results ) {
		return new ValidationResult().combineAll(results);
	}
	
	public ValidationResult copy() {
		return ValidationResult.fromMessages(getMessages());
	}
	
	public ValidationResult add( ValidationMessage message ) {
		
		if ( message != null ) {
			switch( message.getSeverity() ) {
				case ERROR  : errors.add( message); break;
				case WARNING: warnings.add(message); break;
			}
		}
		
		return this;
	}
	
	public ValidationResult addMessageIf( Control target, String text, Severity severity, boolean condition) {
		return  condition? add( new SimpleValidationMessage(target, text, severity)): this;
	}
	
	public ValidationResult addErrorIf( Control target, String text, boolean condition) {
		return addMessageIf(target,text,Severity.ERROR,condition);
	}
	
	public ValidationResult addWarningIf( Control target, String text, boolean condition) {
		return addMessageIf(target,text,Severity.WARNING,condition);
	}
	
	public ValidationResult addAll( Collection<? extends ValidationMessage> messages ) {
		messages.stream().forEach( msg-> add(msg));
		return this;
	}
	
	public ValidationResult addAll( ValidationMessage... messages ) {
		return addAll(Arrays.asList(messages));
	}
	
	public ValidationResult combine( ValidationResult validationResult ) {
		return validationResult == null? copy(): copy().addAll(validationResult.getMessages());
	}
	
	public ValidationResult combineAll( Collection<ValidationResult> validationResults ) {
		return validationResults.stream().reduce(copy(), (x,r) -> {
			return r == null? x: x.addAll(r.getMessages());
		});
	}
	
	public ValidationResult combineAll( ValidationResult... validationResults ) {
		return combineAll( Arrays.asList(validationResults));
	}
	
	
	public Collection<? extends ValidationMessage> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	public Collection<? extends ValidationMessage> getWarnings() {
		return Collections.unmodifiableList(warnings);
	}
	
	public Collection<? extends ValidationMessage> getMessages() {
		List<ValidationMessage> messages = new ArrayList<>();
		messages.addAll(errors);
		messages.addAll(warnings);
		return Collections.unmodifiableList(messages);
	}
	
}
