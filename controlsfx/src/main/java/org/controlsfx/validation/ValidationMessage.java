/**
 * Copyright (c) 2014, 2019, ControlsFX
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

import java.util.Comparator;

import javafx.scene.control.Control;

/**
 * Interface to define basic contract for validation message  
 */
public interface ValidationMessage extends Comparable<ValidationMessage>{

	public static final Comparator<ValidationMessage> COMPARATOR = new Comparator<ValidationMessage>() {

		@Override
		public int compare(ValidationMessage vm1, ValidationMessage vm2) {
			if ( vm1 == vm2 ) return  0;
			if ( vm1 == null) return  1;
			if ( vm2 == null) return -1;
			return vm1.compareTo(vm2);
		}
	};
	
    /**
     * Message text
     * @return message text
     */
    public String getText();

    /**
     * Message {@link Severity} 
     * @return message severity
     */
    public Severity getSeverity();


    /**
     * Message target - {@link Control} which message is related to . 
     * @return message target
     */
    public Control getTarget();

    /**
     * Factory method to create a simple error message 
     * @param target message target
     * @param text message text 
     * @return error message
     */
    public static ValidationMessage error( Control target, String text ) {
        return new SimpleValidationMessage(target, text, Severity.ERROR);
    }

    /**
     * Factory method to create a simple warning message 
     * @param target message target
     * @param text message text 
     * @return warning message
     */
    public static ValidationMessage warning( Control target, String text ) {
        return new SimpleValidationMessage(target, text, Severity.WARNING);
    }
    
    /**
     * Factory method to create a simple info message 
     * @param target message target
     * @param text message text 
     * @return info message
     */
    public static ValidationMessage info( Control target , String text ) {
    	return new SimpleValidationMessage(target, text, Severity.INFO);
    }

    /**
     * Factory method to create a simple ok message 
     * @param target message target
     * @param text message text 
     * @return ok message
     */
    public static ValidationMessage ok( Control target , String text ) {
    	return new SimpleValidationMessage(target, text, Severity.OK);
    }

    /**
     * Factory method to create a simple ok message 
     * @param target message target
     * @return ok message
     */
    public static ValidationMessage ok( Control target ) {
    	return new SimpleValidationMessage(target, "", Severity.OK);
    }

    @Override default public int compareTo(ValidationMessage msg) {
        return msg == null || getTarget() != msg.getTarget() ? -1: getSeverity().compareTo(msg.getSeverity());
    }
}
