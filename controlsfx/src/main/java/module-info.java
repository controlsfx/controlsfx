module org.controlsfx.controls {

    requires java.desktop;

    requires transitive javafx.controls;
    requires static javafx.media;
    requires java.logging;
    requires javafx.base;

    exports org.controlsfx.control;
    exports org.controlsfx.control.action;
    exports org.controlsfx.control.cell;
    exports org.controlsfx.control.decoration;
    exports org.controlsfx.control.table;
    exports org.controlsfx.control.textfield;
    exports org.controlsfx.control.spreadsheet;
    exports org.controlsfx.dialog;
    exports org.controlsfx.glyphfont;
    exports org.controlsfx.property;
    exports org.controlsfx.property.editor;
    exports org.controlsfx.tools;
    exports org.controlsfx.validation;
    exports org.controlsfx.validation.decoration;

    exports impl.org.controlsfx.skin to org.controlsfx.samples;

    // Required for reading CSS files for the CSS Tab
    opens org.controlsfx.control to org.controlsfx.fxsampler;

    uses org.controlsfx.glyphfont.GlyphFont;
    provides org.controlsfx.glyphfont.GlyphFont with org.controlsfx.glyphfont.FontAwesome;
}