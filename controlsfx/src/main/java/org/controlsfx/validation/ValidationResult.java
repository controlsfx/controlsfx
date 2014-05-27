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

/**
 * Validation result. Can generally be thought of a collection of validation messages.
 * Allows for quick an painless accumulation of the messages. 
 * Also provides ability to combine validation results 
 */
public class ValidationResult {

    private List<ValidationMessage> errors   = new ArrayList<>();
    private List<ValidationMessage> warnings = new ArrayList<>();

    /**
     * Creates empty validation result
     */
    public ValidationResult() {}

    /**
     * Factory method to create validation result out of one message
     * @param target validation target 
     * @param text message text 
     * @param severity message severity
     * @param condition condition on which message will be added to validation result
     * @return New instance of validation result
     */
    public static final ValidationResult fromMessageIf( Control target, String text, Severity severity, boolean condition ) {
        return new ValidationResult().addMessageIf(target, text, severity, condition);
    }

    /**
     * Factory method to create validation result out of one error
     * @param target validation target 
     * @param text message text 
     * @param condition condition on which message will be added to validation result
     * @return New instance of validation result
     */
    public static final ValidationResult fromErrorIf( Control target, String text, boolean condition ) {
        return new ValidationResult().addErrorIf(target, text, condition);
    }

    /**
     * Factory method to create validation result out of one warning
     * @param target validation target 
     * @param text message text 
     * @param condition condition on which message will be added to validation result
     * @return New instance of validation result
     */
    public static final ValidationResult fromWarningIf( Control target, String text, boolean condition ) {
        return new ValidationResult().addWarningIf(target, text, condition);
    }

    /**
     * Factory method to create validation result out of one error
     * @param target validation target 
     * @param text message text 
     * @return New instance of validation result
     */
    public static final ValidationResult fromError( Control target, String text ) {
        return fromMessages( ValidationMessage.error(target, text));
    }

    /**
     * Factory method to create validation result out of one warning
     * @param target validation target 
     * @param text message text 
     * @return New instance of validation result
     */
    public static final ValidationResult fromWarning( Control target, String text ) {
        return fromMessages( ValidationMessage.warning(target, text));
    }

    /**
     * Factory method to create validation result out of several messages
     * @param messages
     * @return New instance of validation result
     */
    public static final ValidationResult fromMessages( ValidationMessage... messages ) {
        return new ValidationResult().addAll(messages);
    }

    /**
     * Factory method to create validation result out of collection of messages
     * @param messages
     * @return New instance of validation result
     */
    public static final ValidationResult fromMessages( Collection<? extends ValidationMessage> messages ) {
        return new ValidationResult().addAll(messages);
    }

    /**
     * Factory method to create validation result out of several validation results
     * @param results results
     * @return New instance of validation result, combining all into one
     */
    public static final ValidationResult fromResults( ValidationResult... results ) {
        return new ValidationResult().combineAll(results);
    }

    /**
     * Factory method to create validation result out of collection of validation results
     * @param results results
     * @return New instance of validation result, combining all into one
     */
    public static final ValidationResult fromResults( Collection<ValidationResult> results ) {
        return new ValidationResult().combineAll(results);
    }

    /**
     * Creates a copy of validation result
     * @return copy of validation result
     */
    public ValidationResult copy() {
        return ValidationResult.fromMessages(getMessages());
    }

    /**
     * Add one message to validation result
     * @param message validation message
     * @return updated validation result
     */
    public ValidationResult add( ValidationMessage message ) {

        if ( message != null ) {
            switch( message.getSeverity() ) {
                case ERROR  : errors.add( message); break;
                case WARNING: warnings.add(message); break;
            }
        }

        return this;
    }

    /**
     * Add one message to validation result with condition
     * @param target validation target
     * @param text message text
     * @param severity message severity
     * @param condition condition on which message will be added 
     * @return updated validation result
     */
    public ValidationResult addMessageIf( Control target, String text, Severity severity, boolean condition) {
        return  condition? add( new SimpleValidationMessage(target, text, severity)): this;
    }

    /**
     * Add one error to validation result with condition
     * @param target validation target
     * @param text message text
     * @param condition condition on which error will be added 
     * @return updated validation result
     */
    public ValidationResult addErrorIf( Control target, String text, boolean condition) {
        return addMessageIf(target,text,Severity.ERROR,condition);
    }

    /**
     * Add one warning to validation result with condition
     * @param target validation target
     * @param text message text
     * @param condition condition on which warning will be added 
     * @return updated validation result
     */
    public ValidationResult addWarningIf( Control target, String text, boolean condition) {
        return addMessageIf(target,text,Severity.WARNING,condition);
    }

    /**
     * Add collection of validation messages
     * @param messages
     * @return updated validation result
     */
    public ValidationResult addAll( Collection<? extends ValidationMessage> messages ) {
        messages.stream().forEach( msg-> add(msg));
        return this;
    }

    /**
     * Add several validation messages
     * @param messages
     * @return updated validation result
     */
    public ValidationResult addAll( ValidationMessage... messages ) {
        return addAll(Arrays.asList(messages));
    }

    /**
     * Combine validation result with another. This will create a new instance of combined validation result
     * @param validationResult
     * @return new instance of combined validation result
     */
    public ValidationResult combine( ValidationResult validationResult ) {
        return validationResult == null? copy(): copy().addAll(validationResult.getMessages());
    }

    /**
     * Combine validation result with others. This will create a new instance of combined validation result
     * @param validationResults
     * @return new instance of combined validation result
     */
    public ValidationResult combineAll( Collection<ValidationResult> validationResults ) {
        return validationResults.stream().reduce(copy(), (x,r) -> {
            return r == null? x: x.addAll(r.getMessages());
        });
    }

    /**
     * Combine validation result with others. This will create a new instance of combined validation result
     * @param validationResults
     * @return new instance of combined validation result
     */
    public ValidationResult combineAll( ValidationResult... validationResults ) {
        return combineAll( Arrays.asList(validationResults));
    }

    /**
     * Retrieve errors represented by validation result
     * @return collection of errors
     */
    public Collection<ValidationMessage> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /**
     * Retrieve warnings represented by validation result
     * @return collection of warnings
     */
    public Collection<ValidationMessage> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    /**
     * Retrieve all messages represented by validation result
     * @return collection of messages
     */
    public Collection<ValidationMessage> getMessages() {
        List<ValidationMessage> messages = new ArrayList<>();
        messages.addAll(errors);
        messages.addAll(warnings);
        return Collections.unmodifiableList(messages);
    }

}
