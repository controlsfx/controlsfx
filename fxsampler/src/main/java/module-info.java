module org.controlsfx.fxsampler {

    requires transitive javafx.controls;
    requires transitive javafx.web;

    exports fxsampler to 
            javafx.graphics,
            org.controlsfx.samples;
    exports fxsampler.model to org.controlsfx.samples;
    exports fxsampler.util to org.controlsfx.samples;

    uses fxsampler.FXSamplerConfiguration;
    uses fxsampler.FXSamplerProject;
}