package org.controlsfx.dialog;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

abstract class FXDialog {
    
    public abstract void show();
    
    public abstract void hide();
    
    
    // --- resizable
//    private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", false);
    
    abstract BooleanProperty resizableProperty();
    
    
    // --- focused
    private final BooleanProperty focused = new SimpleBooleanProperty(this, "focused", true);
    
    public final BooleanProperty focusedProperty() {
        return focused;
    }
    
    
    // --- title
//    private final StringProperty title = new SimpleStringProperty(this, "title");
    
    abstract StringProperty titleProperty();
    
    
    // --- content
    public abstract void setContentPane(Pane pane);
    
    // --- root
    public abstract Node getRoot();
    
    
    // --- width
    /**
     * Property representing the width of the dialog.
     */
    abstract ReadOnlyDoubleProperty widthProperty();
    
    
    // --- height
    /**
     * Property representing the height of the dialog.
     */
    abstract ReadOnlyDoubleProperty heightProperty();
    
    
    /**
     * Sets whether the dialog can be iconified (minimized)
     * @param iconifiable if dialog should be iconifiable
     */
    abstract void setIconifiable(boolean iconifiable);
    
    /**
     * Sets whether the dialog can be closed
     * @param iconifiable if dialog should be closable
     */
    abstract void setClosable( boolean closable );
}
