package org.controlsfx.property.editor;

import javafx.scene.layout.Region;


public interface PropertyEditor {

    Region asNode();
    
    Object getValue();
    void setValue( Object value );
    
}
