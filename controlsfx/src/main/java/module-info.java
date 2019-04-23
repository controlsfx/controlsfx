module org.controlsfx.controls {

    requires java.desktop;

    requires transitive javafx.controls;
    requires static javafx.web;
    requires static javafx.media;

    exports org.controlsfx.control;
    exports org.controlsfx.control.action;
    exports org.controlsfx.control.cell;
    exports org.controlsfx.control.decoration;
    exports org.controlsfx.control.table;
    exports org.controlsfx.control.textfield;
    exports org.controlsfx.dialog;
    exports org.controlsfx.glyphfont;
    exports org.controlsfx.property;
    exports org.controlsfx.property.editor;
    exports org.controlsfx.tools;
    exports org.controlsfx.validation;
    exports org.controlsfx.validation.decoration;

    // opens org.controlsfx.glyphfont to org.controlsfx.fxsampler;
    
    uses org.controlsfx.glyphfont.GlyphFont;
    provides org.controlsfx.glyphfont.GlyphFont with org.controlsfx.glyphfont.FontAwesome;
}