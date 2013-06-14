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
package impl.org.controlsfx.skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.property.editor.PropertyEditor;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;

public class PropertySheetSkin extends BehaviorSkinBase<PropertySheet, BehaviorBase<PropertySheet>> {

    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/

    private static final int MIN_COLUMN_WIDTH = 100;
    
    /**************************************************************************
     * 
     * fields
     * 
     **************************************************************************/
    
    private final ScrollPane scroller = new ScrollPane();
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    public PropertySheetSkin(final PropertySheet control) {
        super(control, new BehaviorBase<>(control));
        
        scroller.setFitToWidth(true);
        getChildren().add(scroller);
              
        
        // setup listeners
        registerChangeListener(control.modeProperty(), "MODE");
        registerChangeListener(control.propertyEditorFactory(), "EDITOR-FACTORY");
        
        control.getItems().addListener( new ListChangeListener<Item>() {
            @Override public void onChanged(javafx.collections.ListChangeListener.Change<? extends Item> change) {
                refreshProperties();
            }
        });
        
        
        
    }


    /**************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        if (p == "MODE" || p == "EDITOR-FACTORY") {
            refreshProperties();
        }
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        scroller.resizeRelocate(x, y, w, h);
    }



    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/

    private void refreshProperties() {
        scroller.setContent(buildPropertySheetContainer());
    }
    
    private Node buildPropertySheetContainer() {
        switch( getSkinnable().modeProperty().get() ) {

            case CATEGORY: {
                
                // group by category
                Map<String, List<Item>> categoryMap = new TreeMap<>();
                for( Item p: getSkinnable().getItems()) {
                    String category = p.getCategory();
                    List<Item> list = categoryMap.get(category);
                    if ( list == null ) {
                        list = new ArrayList<>();
                        categoryMap.put( category, list);
                    }
                    list.add(p);
                }
                
                // create category-based accordion
                Accordion accordeon = new Accordion();
                for( String category: categoryMap.keySet() ) {
                    TitledPane pane = new TitledPane( category, new PropertyPane( categoryMap.get(category)));
                    pane.setExpanded(true);
                    accordeon.getPanes().add(pane);
                }
                if ( accordeon.getPanes().size() > 0 ) {
                    accordeon.setExpandedPane( accordeon.getPanes().get(0));
                }
                return accordeon;
            }
            
            default: return new PropertyPane(getSkinnable().getItems());
        }
    }


    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
    private class PropertyPane extends GridPane {
        
        public PropertyPane( List<Item> properties ) {
            setVgap(5);
            setHgap(5);
            setPadding(new Insets(5, 15, 5, 15));
            getStyleClass().add("property-pane");
            setItems(properties);
//            setGridLinesVisible(true);
        }
        
        public void setItems( List<Item> properties ) {
            getChildren().clear();
            int row = 0;
            for (Item p : getSkinnable().getItems()) {
                
                // setup property label
                Label label = new Label(p.getName());
                label.setMinWidth(MIN_COLUMN_WIDTH);
                
                // show description as a tooltip
                String description = p.getDescription();
                if ( description != null && !description.trim().isEmpty()) {
                    label.setTooltip( new Tooltip(description));
                }
                
                add(label, 0, row);

             // setup property editor
                Region editor = getEditor(p);
                editor.setMinWidth(MIN_COLUMN_WIDTH);
                editor.setMaxWidth(Double.MAX_VALUE);
                add(editor, 1, row);
                GridPane.setHgrow(editor, Priority.ALWAYS);
                
              //TODO add support for recursive properties
                
                row++;
                
            }
        }
        
        private Region getEditor( Item item ) {
            
            PropertyEditor editor = getSkinnable().getPropertyEditorFactory().getEditor(item);
            if ( editor != null ) {
                editor.setValue(item.getValue());
                return editor.asNode();
            } else {
                TextField message = new TextField("No suitable editor found");
                message.setEditable(false);
                message.setDisable(true);
                return message;
            }
            
        }
        
        
    }

}
