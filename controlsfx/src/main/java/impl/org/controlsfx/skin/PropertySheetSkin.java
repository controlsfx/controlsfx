/**
 * Copyright (c) 2013, 2015 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.control.PropertySheet.Mode;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
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
    
    private final BorderPane content;
    private final ScrollPane scroller;
    private final ToolBar toolbar;
    private final SegmentedButton modeButton = ActionUtils.createSegmentedButton(
        new ActionChangeMode(Mode.NAME),
        new ActionChangeMode(Mode.CATEGORY)
    );
    private final TextField searchField = TextFields.createClearableTextField();
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/

    public PropertySheetSkin(final PropertySheet control) {
        super(control, new BehaviorBase<>(control, Collections.<KeyBinding> emptyList()));
        
        scroller = new ScrollPane();
        scroller.setFitToWidth(true);
        
        toolbar = new ToolBar();
        toolbar.managedProperty().bind(toolbar.visibleProperty());
        toolbar.setFocusTraversable(true);
        
        // property sheet mode
        modeButton.managedProperty().bind(modeButton.visibleProperty());
        modeButton.getButtons().get(getSkinnable().modeProperty().get().ordinal()).setSelected(true);
        toolbar.getItems().add(modeButton);
        
        // property sheet search
        searchField.setPromptText( localize(asKey("property.sheet.search.field.prompt"))); //$NON-NLS-1$
        searchField.setMinWidth(0);
        HBox.setHgrow(searchField, Priority.SOMETIMES);
        searchField.managedProperty().bind(searchField.visibleProperty());
        toolbar.getItems().add(searchField);
        
        // layout controls
        content = new BorderPane();
        content.setTop(toolbar);
        content.setCenter(scroller);
        getChildren().add(content);
              
        
        // setup listeners
        registerChangeListener(control.modeProperty(), "MODE"); //$NON-NLS-1$
        registerChangeListener(control.propertyEditorFactory(), "EDITOR-FACTORY"); //$NON-NLS-1$
        registerChangeListener(control.titleFilter(), "FILTER"); //$NON-NLS-1$
        registerChangeListener(searchField.textProperty(), "FILTER-UI"); //$NON-NLS-1$
        registerChangeListener(control.modeSwitcherVisibleProperty(), "TOOLBAR-MODE"); //$NON-NLS-1$
        registerChangeListener(control.searchBoxVisibleProperty(), "TOOLBAR-SEARCH"); //$NON-NLS-1$
        
        control.getItems().addListener((ListChangeListener<Item>) change -> refreshProperties());
        
        // initialize properly 
        refreshProperties(); 
        updateToolbar();
    }


    /**************************************************************************
     * 
     * Overriding public API
     * 
     **************************************************************************/

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if (p == "MODE" || p == "EDITOR-FACTORY" || p == "FILTER") { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            refreshProperties();
        } else if (p == "FILTER-UI") { //$NON-NLS-1$
            getSkinnable().setTitleFilter(searchField.getText());
        } else if (p == "TOOLBAR-MODE") { //$NON-NLS-1$
            updateToolbar();
        } else if (p == "TOOLBAR-SEARCH") { //$NON-NLS-1$
            updateToolbar();
        }
    }
    
    @Override protected void layoutChildren(double x, double y, double w, double h) {
        content.resizeRelocate(x, y, w, h);
    }



    /**************************************************************************
     * 
     * Implementation
     * 
     **************************************************************************/
    
    private void updateToolbar() {
        modeButton.setVisible(getSkinnable().isModeSwitcherVisible());
        searchField.setVisible(getSkinnable().isSearchBoxVisible());
        
        toolbar.setVisible(modeButton.isVisible() || searchField.isVisible());
    }

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
                Accordion accordion = new Accordion();
                for( String category: categoryMap.keySet() ) {
                	PropertyPane props = new PropertyPane( categoryMap.get(category));
                	// Only show non-empty categories 
                	if ( props.getChildrenUnmodifiable().size() > 0 ) {
                       TitledPane pane = new TitledPane( category, props );
                       pane.setExpanded(true);
                       accordion.getPanes().add(pane);
                    }
                }
                if ( accordion.getPanes().size() > 0 ) {
                    accordion.setExpandedPane(accordion.getPanes().get(0));
                }
                return accordion;
            }
            
            default: return new PropertyPane(getSkinnable().getItems());
        }
        
    }

    
    /**************************************************************************
     * 
     * Support classes / enums
     * 
     **************************************************************************/
    
    private class ActionChangeMode extends Action {
        
    	private final Image CATEGORY_IMAGE = new Image(PropertySheetSkin.class.getResource("/org/controlsfx/control/format-indent-more.png").toExternalForm()); //$NON-NLS-1$
    	private final Image NAME_IMAGE = new Image(PropertySheetSkin.class.getResource("/org/controlsfx/control/format-line-spacing-triple.png").toExternalForm()); //$NON-NLS-1$
    	
        public ActionChangeMode(PropertySheet.Mode mode) {
            super(""); //$NON-NLS-1$
            setEventHandler(ae -> getSkinnable().modeProperty().set(mode));
            
            if (mode == Mode.CATEGORY) {
                setGraphic( new ImageView(CATEGORY_IMAGE));
                setLongText(localize(asKey("property.sheet.group.mode.bycategory"))); //$NON-NLS-1$
            } else if (mode == Mode.NAME) {
                setGraphic(new ImageView(NAME_IMAGE));
                setLongText(localize(asKey("property.sheet.group.mode.byname"))); //$NON-NLS-1$
            } else {
                setText("???"); //$NON-NLS-1$
            }
        }

    }
    
    
    private class PropertyPane extends GridPane {
        
        public PropertyPane( List<Item> properties ) {
            this( properties, 0 );
        }
        
        public PropertyPane( List<Item> properties, int nestingLevel ) {
            setVgap(5);
            setHgap(5);
            setPadding(new Insets(5, 15, 5, 15 + nestingLevel*10 ));
            getStyleClass().add("property-pane"); //$NON-NLS-1$
            setItems(properties);
//            setGridLinesVisible(true);
        }
        
        public void setItems( List<Item> properties ) {
            getChildren().clear();
            
            String filter = getSkinnable().titleFilter().get();
            filter = filter == null? "": filter.trim().toLowerCase(); //$NON-NLS-1$

            int row = 0;
            
            for (Item item : properties) {

                // filter properties
                String title = item.getName();
               
                if ( !filter.isEmpty() && title.toLowerCase().indexOf( filter ) < 0) continue;
                
                // setup property label
                Label label = new Label(title);
                label.setMinWidth(MIN_COLUMN_WIDTH);
                
                // show description as a tooltip
                String description = item.getDescription();
                if ( description != null && !description.trim().isEmpty()) {
                    label.setTooltip(new Tooltip(description));
                }
                
                add(label, 0, row);

                // setup property editor
                Node editor = getEditor(item);
                
                if (editor instanceof Region) {
                    ((Region)editor).setMinWidth(MIN_COLUMN_WIDTH);
                    ((Region)editor).setMaxWidth(Double.MAX_VALUE);
                }
                label.setLabelFor(editor);
                add(editor, 1, row);
                GridPane.setHgrow(editor, Priority.ALWAYS);
                
                //TODO add support for recursive properties
                
                row++;
            }
            
        }
        
        @SuppressWarnings("unchecked")
        private Node getEditor(Item item) {
            @SuppressWarnings("rawtypes")
            PropertyEditor editor = getSkinnable().getPropertyEditorFactory().call(item);
            if (editor == null) {
                editor = new AbstractPropertyEditor<Object, TextField>(item, new TextField(), true) {
                    {
                        getEditor().setEditable(false);
                        getEditor().setDisable(true);
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    @Override protected ObservableValue<Object> getObservableValue() {
                        return (ObservableValue<Object>)(Object)getEditor().textProperty();
                    }
                    
                    /**
                     * {@inheritDoc}
                     */
                    @Override public void setValue(Object value) {
                        getEditor().setText(value == null? "": value.toString()); //$NON-NLS-1$
                    }
                };
            } else if (! item.isEditable()) {
                editor.getEditor().setDisable(true);
            }
            editor.setValue(item.getValue());
            return editor.getEditor();
        }
    }
}
