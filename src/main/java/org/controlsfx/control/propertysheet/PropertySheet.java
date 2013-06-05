package org.controlsfx.control.propertysheet;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

public class PropertySheet extends BorderPane {

    private final GridPane pane = new GridPane();
    private final ObservableList<Property> properties = FXCollections.observableArrayList();
    
    public PropertySheet() {
        
        pane.setVgap(5);
        pane.setHgap(5);
        pane.setPadding(new Insets(5, 15, 5, 15));

        ScrollPane scroller = new ScrollPane(pane);
        scroller.setFitToWidth(true);
        setCenter(scroller);
        
        properties.addListener( new ListChangeListener<Property>() {

            @Override public void onChanged(javafx.collections.ListChangeListener.Change<? extends Property> change) {
                refreshProperties();
            }});
        
    }
    
    public ObservableList<Property> getItems() {
        return properties;
    }

    
    private void refreshProperties() {
        
        int row = 0;
        for (Property p : properties ) {

            Label label = new Label(p.getName());
            label.setMinWidth(100);
            pane.add(label, 0, row);

            Control editor = createEditor(p);
            editor.setMaxWidth(Double.MAX_VALUE);
            editor.setMinWidth(100);
            pane.add(editor, 1, row++);
            GridPane.setHgrow(editor, Priority.ALWAYS);
        }
        
    }
    
    private Control createEditor( Property p  ) {
        
        Object value = p.getValue();
        Class<?> type = p.getType();
        System.out.println(type);
        
        if ( type != null && type == String.class ) {
            return new TextField( value == null? "": value.toString()); 
        }

        if ( type != null && ( type == boolean.class || type == Boolean.class) ) {
            CheckBox cb = new CheckBox();
            cb.selectedProperty().set( value == null? false: Boolean.valueOf(value.toString()).booleanValue());
            return cb;
        }

        if ( type != null && type.isAssignableFrom(Color.class) ) {
            return new ColorPicker();
        }

        return new ComboBox<Object>(); 
        
    }
    
}
