package org.controlsfx.property.editor;

import org.controlsfx.control.PropertySheet.Item;

public interface PropertyEditorFactory {

    PropertyEditor getEditor( Item propertySheetItem  );
    
}
