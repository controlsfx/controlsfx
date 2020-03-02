module org.controlsfx.samples {

    requires java.desktop;
    requires org.controlsfx.controls;
    requires org.controlsfx.fxsampler;
    
    exports org.controlsfx.samples to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.actions to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.button to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.checked to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.dialogs to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.propertysheet to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.tablefilter to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.tableview to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.textfields to org.controlsfx.fxsampler;
    exports org.controlsfx.samples.spreadsheet to org.controlsfx.fxsampler;
    
    opens org.controlsfx.samples;
    opens org.controlsfx.samples.dialogs;
    opens org.controlsfx.samples.actions to org.controlsfx.controls;
    opens org.controlsfx.samples.tableview to javafx.base;
    opens org.controlsfx.samples.spreadsheet to javafx.graphics;
    
    provides fxsampler.FXSamplerProject with org.controlsfx.ControlsFXSampler;
}