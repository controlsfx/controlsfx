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
package org.controlsfx.validation.decoration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javafx.scene.control.Control;

import org.controlsfx.control.decoration.Decoration;
import org.controlsfx.control.decoration.StyleClassDecoration;
import org.controlsfx.validation.ValidationMessage;

/**
 * Validation decorator to decorate component validation state using two
 * CSS classes for errors and warnings.
 * Here is example of such decoration 
 * <br> <br>
 * <img src="StyleClassValidationDecoration.png" alt="Screenshot of StyleClassValidationDecoration">  
 */
public class StyleClassValidationDecoration extends AbstractValidationDecoration {

    private final String errorClass;
    private final String warningClass;
    private final String infoClass;
    private final String okClass;

    /**
     * Creates a default instance of a decorator
     */
    public StyleClassValidationDecoration() {
        this(null,null);
    }

    /**
     * Creates an instance of validator using custom class names
     * @param errorClass class name for error decoration
     * @param warningClass class name for warning decoration
     */
    public StyleClassValidationDecoration(String errorClass, String warningClass) {
        this(errorClass, warningClass, null, null);
    }

    /**
     * Creates an instance of validator using custom class names
     * @param errorClass class name for error decoration
     * @param warningClass class name for warning decoration
     * @param infoClass class name for info decoration
     * @param okClass class name for ok decoration
     */
    public StyleClassValidationDecoration(String errorClass, String warningClass, String infoClass, String okClass) {
        this.errorClass = errorClass != null? errorClass : "error"; //$NON-NLS-1$
        this.warningClass = warningClass != null? warningClass : "warning";	 //$NON-NLS-1$
        this.infoClass = infoClass != null? infoClass : "info";	 //$NON-NLS-1$
        this.okClass = okClass != null? okClass : "ok";	 //$NON-NLS-1$
    }

    @Override
    protected Collection<Decoration> createValidationDecorations(ValidationMessage message) {
        String validationClass = infoClass;
        switch (message.getSeverity()) {
        case ERROR:
            validationClass = errorClass;
            break;
        case WARNING:
            validationClass = warningClass;
            break;
        case OK:
            validationClass = okClass;
            break;
        default:
            validationClass = infoClass;
            break;
        }
        return Arrays.asList(new StyleClassDecoration( validationClass ));
    }

	@Override
	protected Collection<Decoration> createRequiredDecorations(Control target) {
		return Collections.emptyList();
	}
    
}
