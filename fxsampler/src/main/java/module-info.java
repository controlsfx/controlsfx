module org.controlsfx.fxsampler {

    requires transitive javafx.controls;
    requires transitive javafx.web;

    exports fxsampler;
    exports fxsampler.model;

    uses fxsampler.FXSamplerProject;
    uses fxsampler.FXSamplerConfiguration;
}