package org.controlsfx;

import fxsampler.FXSamplerProject;

public class ControlsFXSampler implements FXSamplerProject {

    /** {@inheritDoc} */
    @Override public String getProjectName() {
        return "ControlsFX";
    }
    
    /** {@inheritDoc} */
    @Override public String getSampleBasePackage() {
        return "org.controlsfx.samples";
    }
}
