module org.controlsfx.fxsampler {

    requires transitive javafx.controls;
    requires transitive javafx.web;

    opens org.controlsfx.fxsampler;
    exports org.controlsfx.fxsampler.model;
    exports org.controlsfx.fxsampler;

    uses org.controlsfx.fxsampler.FXSamplerProject;
    uses org.controlsfx.fxsampler.FXSamplerConfiguration;
}