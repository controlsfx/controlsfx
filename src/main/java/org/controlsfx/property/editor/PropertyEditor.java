package org.controlsfx.property.editor;

import javafx.scene.control.Control;

public interface PropertyEditor {

    Control asControl();
    
    Object getValue();
    void setValue( Object value );
    
}
