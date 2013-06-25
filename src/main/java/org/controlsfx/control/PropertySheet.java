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

import impl.org.controlsfx.skin.PropertySheetSkin;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

public class PropertySheet extends Control {
    
    
    /**************************************************************************
     * 
     * Static fields
     * 
     **************************************************************************/
    
    
    
    /**************************************************************************
     * 
     * Static enumerations / interfaces
     * 
     **************************************************************************/
    
    /**
     * Specifies how the PropertySheet should be laid out. Refer to the 
     * enumeration values to learn what each one means.
     */
    public static enum Mode {
        /**
         * Simply displays the properties in the 
         * {@link PropertySheet#getItems() items list} in the order they are 
         * in the list. 
         */
        NAME,
        
        /**
         * Groups the properties in the 
         * {@link PropertySheet#getItems() items list} based on their
         * {@link Item#getCategory() category}.
         */
        CATEGORY
    }
    
    
    
    /**
     * A wrapper interface for a single property to be displayed in a
     * {@link PropertySheet} control.
     */
    public static interface Item {
        
        /**
         * Returns the class type of the property.
         */
        public Class<?> getType();

        /**
         * Returns a String representation of the category of the property. This
         * is relevant when the {@link PropertySheet} 
         * {@link PropertySheet#modeProperty() mode property} is set to
         * {@link Mode#CATEGORY} - as then all properties with the same category
         * will be grouped together visually. 
         */
        public String getCategory();
       
        /**
         * Returns the display name of the property, which should be short (i.e.
         * less than two wordS). This is used to explain to the end user what the
         * property represents and is displayed beside the {@link PropertyEditor}.
         * If you need to explain more detail to the user, consider placing it
         * in the {@link #getDescription()}.
         */
        public String getName();
        
        /**
         * A String that will be shown to the user as a tooltip. This allows for
         * a longer form of detail than what is possible with the {@link #getName()} 
         * method.
         */
        public String getDescription();
        
        /**
         * Returns the current value of the property.
         */
        public Object getValue();

        /**
         * Sets the current value of the property.
         */
        public void setValue(Object value);
   }
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    public PropertySheet() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * A list of properties that will be displayed to the user to allow for them
     * to be edited. 
     */
    public ObservableList<Item> getItems() {
        return items;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new PropertySheetSkin(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override protected String getUserAgentStylesheet() {
        return PropertySheet.class.getResource("propertysheet.css").toExternalForm();
    }
    
    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- modeProperty 
    private final SimpleObjectProperty<Mode> modeProperty = 
            new SimpleObjectProperty<>(this, "mode", Mode.NAME);
    
    /**
     * A property used to represent how the properties should be laid out in
     * the PropertySheet. Refer to the {@link Mode} enumeration to better 
     * understand the available options.
     */
    public final SimpleObjectProperty<Mode> modeProperty() {
    	return modeProperty;
    }

    // -- JavaDoc auto-generated
    public final Mode getMode() {
        return modeProperty.get();
    }

    // -- JavaDoc auto-generated
    public final void setMode(Mode mode) {
        modeProperty.set(mode);
    }
  

    // --- propertyEditorFactory
    private final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory = 
            new SimpleObjectProperty<Callback<Item, PropertyEditor<?>>>(this, "propertyEditor", new DefaultPropertyEditorFactory());
    
    /**
     * The property editor factory is used by the PropertySheet to determine which
     * {@link PropertyEditor} to use for a given {@link Item}. By default the
     * {@link DefaultPropertyEditorFactory} is used, but this may be replaced
     * or extended by developers wishing to add in (or substitute) their own
     * property editors.
     */
    public final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory() {
        return propertyEditorFactory;
    }
    
    // -- JavaDoc auto-generated
    public final Callback<Item, PropertyEditor<?>> getPropertyEditorFactory() {
        return propertyEditorFactory.get();
    }
    
    // -- JavaDoc auto-generated
    public final void setPropertyEditorFactory( Callback<Item, PropertyEditor<?>> factory ) {
        propertyEditorFactory.set( factory == null? new DefaultPropertyEditorFactory(): factory );
    }
    
    
    // --- toolbarVisibleProperty
    private final SimpleBooleanProperty toolbarVisibleProperty = 
            new SimpleBooleanProperty(this, "toolbarVisible", true);
    
    public final SimpleBooleanProperty toolbarVisibleProperty() {
        return toolbarVisibleProperty;
    }
    
    public final boolean isToolbarVisible() {
        return toolbarVisibleProperty.get();
    }
    
    public final void setToolbarVisible( boolean visible ) {
        toolbarVisibleProperty.set(visible);
    }
    
    
    // --- toolbarModeVisibleProperty
    private final SimpleBooleanProperty toolbarModeVisibleProperty = 
            new SimpleBooleanProperty(this, "toolbarModeVisible",true);
    
    public final SimpleBooleanProperty toolbarModeVisibleProperty() {
        return toolbarModeVisibleProperty;
    }
    
    public final boolean isToolbarModeVisible() {
        return toolbarModeVisibleProperty.get();
    }
    
    public final void setToolbarModeVisible( boolean visible ) {
        toolbarModeVisibleProperty.set(visible);
    }
    
    
    // --- toolbarSearchVisibleProperty
    private final SimpleBooleanProperty toolbarSearchVisibleProperty = 
            new SimpleBooleanProperty(this, "toolbarSearchVisible", true);
    
    public final SimpleBooleanProperty toolbarSearchVisibleProperty() {
        return toolbarSearchVisibleProperty;
    }
    
    public final boolean isToolbarSearchVisible() {
        return toolbarSearchVisibleProperty.get();
    }
    
    public final void setToolbarSearchVisible( boolean visible ) {
        toolbarSearchVisibleProperty.set(visible);
    }   
    
     
    // --- titleFilterProperty
    private final SimpleStringProperty titleFilterProperty = 
            new SimpleStringProperty(this, "titleFilter", "");
    
    public final SimpleStringProperty titleFilter() {
        return titleFilterProperty;
    }
    
    public final String getTitleFilter() {
        return titleFilterProperty.get();
    }
    
    public final void setTitleFilter( String filter ) {
        titleFilterProperty.set(filter);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "property-sheet";
    
}
