/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control;

import org.controlsfx.property.Property;

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
        
        pane.getChildren().clear();
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
