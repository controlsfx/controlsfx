/**
 * Copyright (c) 2014, 2015 ControlsFX
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
package org.controlsfx.dialog;

import javafx.scene.control.DialogPane;

/**
 * WizardPane is the base class for all wizard pages. The API is essentially
 * the {@link DialogPane}, with the addition of convenience methods related
 * to {@link #onEnteringPage(Wizard) entering} and 
 * {@link #onExitingPage(Wizard) exiting} the page.
 */
public class WizardPane extends DialogPane {

    /**
     * Creates an instance of wizard pane.
     */
    public WizardPane() {
        getStylesheets().add(Wizard.class.getResource("wizard.css").toExternalForm());
        getStyleClass().add("wizard-pane");
    }

    /**
     * Called on entering a page. This is a good place to read values from wizard settings 
     * and assign them to controls on the page
     * @param wizard which page will be used on
     */
    public void onEnteringPage(Wizard wizard) {
        // no-op
    }
    
    /**
     * Called on existing the page. 
     * This is a good place to read values from page controls and store them in wizard settings
     * @param wizard which page was used on
     */
    public void onExitingPage(Wizard wizard) {
        // no-op
    }
}
