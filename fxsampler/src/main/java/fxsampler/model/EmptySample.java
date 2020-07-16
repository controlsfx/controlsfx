/**
 * Copyright (c) 2013, 2020, ControlsFX
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
package fxsampler.model;

import javafx.scene.Node;
import javafx.stage.Stage;
import fxsampler.Sample;

public class EmptySample implements Sample {
    private final String name;

    public EmptySample(String name) {
        this.name = name;
    }

    @Override public String getSampleName() {
        return name;
    }

    @Override public String getSampleDescription() {
        return null;
    }
    
    @Override public String getProjectName() {
        return null;
    }

	@Override
	public String getProjectVersion() {
		return null;
	}
	
    @Override public Node getPanel(Stage stage) {
        return null;
    }

    @Override public String getJavaDocURL() {
        return null;
    }
    
    @Override public String getSampleSourceURL() {
        return null;
    }

    @Override public boolean isVisible() {
        return true;
    }

    @Override public Node getControlPanel() {
        return null;
    }

    @Override
    public void dispose() {}

    public double getControlPanelDividerPosition() {
    	return 0.6;
    }

	@Override
	public String getControlStylesheetURL() {
		return null;
	}

}