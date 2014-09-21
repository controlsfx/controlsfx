/**
 * Copyright (c) 2014 ControlsFX
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
package org.controlsfx.dialog.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.controlsfx.dialog.wizard.Wizard.WizardPane;

/**
 * <p>Implementation of common linear wizard page flow.  
 * The flow simply traverses a collections of provided {@link WizardPane}s.</p>
 * <p>For example of usage see {@link Wizard} documentation</p>
 * 
 * @see Wizard
 * @see WizardPane 
 */
public class LinearWizardFlow implements Wizard.Flow {

    private final List<WizardPane> pages;

    public LinearWizardFlow( Collection<WizardPane> pages ) {
        this.pages = new ArrayList<>(pages);
    }

    public LinearWizardFlow( WizardPane... pages ) {
        this( Arrays.asList(pages));
    }

	@Override
	public Optional<WizardPane> advance(WizardPane currentPage) {
		int pageIndex = pages.indexOf(currentPage);
		return Optional.ofNullable( pages.get(++pageIndex) );
	}

	@Override
	public boolean canAdvance(WizardPane currentPage) {
		int pageIndex = pages.indexOf(currentPage);
		return pages.size()-1 > pageIndex; 
	}
	
	

}