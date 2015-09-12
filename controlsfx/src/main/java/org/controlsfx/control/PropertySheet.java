/**
 * Copyright (c) 2013, 2015, ControlsFX
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

import java.util.Optional;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import org.controlsfx.property.BeanPropertyUtils;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

/**
 * The PropertySheet control is a powerful control designed to make it really
 * easy for developers to present to end users a list of properties that the 
 * end user is allowed to manipulate. Commonly a property sheet is used in
 * visual editors and other tools where a lot of properties exist.
 * 
 * <p>To better describe what a property sheet is, please refer to the picture
 * below:
 * 
 * <br>
 * <center><img src="propertySheet.PNG" alt="Screenshot of PropertySheet"></center>
 * 
 * <p>In this property sheet there exists two columns: the left column shows a 
 * label describing the property itself, whereas the right column provides a
 * {@link PropertyEditor} that allows the end user the means to manipulate the
 * property. In the screenshot you can see CheckEditor, 
 * ChoiceEditor, TextEditor and FontEditor, among the
 * many editors that are available in the {@link Editors}
 * package.
 * 
 * <p>To create a PropertySheet is simple: you firstly instantiate an instance
 * of PropertySheet, and then you pass in a list of {@link Item} instances,
 * where each Item represents a single property that is to be editable by the
 * end user.
 * 
 * <h3>Working with JavaBeans</h3>
 * Because a very common use case for a property sheet is editing properties on
 * a JavaBean, there is convenience API for making this interaction easier.
 * Refer to the {@link BeanPropertyUtils class}, in particular the
 * {@link BeanPropertyUtils#getProperties(Object)} method that will return a 
 * list of Item instances, one Item instance per property on the given JavaBean.
 * 
 * @see Item
 * @see Mode
 */
public class PropertySheet extends ControlsFXControl {
    
    
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
     * Specifies how the {@link PropertySheet} should be laid out. Refer to the 
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
     * 
     * @see PropertySheet
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
         * less than two words). This is used to explain to the end user what the
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
        
        /**
         * Returns the underlying ObservableValue, where one exists, that the editor 
         * can monitor for changes.
         */
        public Optional<ObservableValue<? extends Object>> getObservableValue();
        
        /**
         * Returns an Optional wrapping the class of the PropertyEditor that 
         * should be used for editing this item. The default implementation 
         * returns Optional.empty()
         * 
         * The class must have a constructor that can accept a single argument 
         * of type PropertySheet.Item
         */
        default public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return Optional.empty();
        }
        
        /**
         *  Indicates whether the PropertySheet should allow editing of this 
         * property, or whether it is read-only. The default implementation 
         * returns true.
         */
        default public boolean isEditable() {
            return true;
        }
   }
    
    
    /**************************************************************************
     * 
     * Private fields
     * 
     **************************************************************************/
    
    private final ObservableList<Item> items;
    
    
    
    /**************************************************************************
     * 
     * Constructors
     * 
     **************************************************************************/
    
    /**
     * Creates a default PropertySheet instance with no properties to edit.
     */
    public PropertySheet() {
        this(null);
    }
    
    /**
     * Creates a PropertySheet instance prepopulated with the items provided
     * in the items {@link ObservableList}.
     * 
     * @param items The items that should appear within the PropertySheet.
     */
    public PropertySheet(ObservableList<Item> items) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        
        this.items = items == null ? FXCollections.<Item>observableArrayList() : items;
    }
    
    
    
    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    
    /**
     * 
     * @return An ObservableList of properties that will be displayed to the user to allow for them
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
    
    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        return getUserAgentStylesheet(PropertySheet.class, "propertysheet.css");
    }

    
    /**************************************************************************
     * 
     * Properties
     * 
     **************************************************************************/
    
    // --- modeProperty 
    private final SimpleObjectProperty<Mode> modeProperty = 
            new SimpleObjectProperty<>(this, "mode", Mode.NAME); //$NON-NLS-1$
    
    /**
     * Used to represent how the properties should be laid out in
     * the PropertySheet. Refer to the {@link Mode} enumeration to better 
     * understand the available options.
     * @return A SimpleObjectproperty. 
     */
    public final SimpleObjectProperty<Mode> modeProperty() {
    	return modeProperty;
    }

    /**
     * @see Mode
     * @return how the properties should be laid out in
     * the PropertySheet.
     */
    public final Mode getMode() {
        return modeProperty.get();
    }

    /**
     * Set how the properties should be laid out in
     * the PropertySheet.
     * @param mode 
     */
    public final void setMode(Mode mode) {
        modeProperty.set(mode);
    }
  

    // --- propertyEditorFactory
    private final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory = 
            new SimpleObjectProperty<>(this, "propertyEditor", new DefaultPropertyEditorFactory()); //$NON-NLS-1$
    
    /**
     * The property editor factory is used by the PropertySheet to determine which
     * {@link PropertyEditor} to use for a given {@link Item}. By default the
     * {@link DefaultPropertyEditorFactory} is used, but this may be replaced
     * or extended by developers wishing to add in (or substitute) their own
     * property editors.
     * 
     * @return A SimpleObjectproperty.
     */
    public final SimpleObjectProperty<Callback<Item, PropertyEditor<?>>> propertyEditorFactory() {
        return propertyEditorFactory;
    }
    
    /**
     * 
     * @return The editor factory used by the PropertySheet to determine which
     * {@link PropertyEditor} to use for a given {@link Item}.
     */
    public final Callback<Item, PropertyEditor<?>> getPropertyEditorFactory() {
        return propertyEditorFactory.get();
    }
    
    /**
     * Sets a new editor factory used by the PropertySheet to determine which
     * {@link PropertyEditor} to use for a given {@link Item}.
     * @param factory 
     */
    public final void setPropertyEditorFactory( Callback<Item, PropertyEditor<?>> factory ) {
        propertyEditorFactory.set( factory == null? new DefaultPropertyEditorFactory(): factory );
    }
    
    
    // --- modeSwitcherVisible
    private final SimpleBooleanProperty modeSwitcherVisible = 
            new SimpleBooleanProperty(this, "modeSwitcherVisible", true); //$NON-NLS-1$
    
    /**
     * This property represents whether a visual option should be presented to
     * users to switch between the various {@link Mode modes} available. By
     * default this is true, so setting it to false will hide these buttons.
     * @return A SimpleBooleanproperty.
     */
    public final SimpleBooleanProperty modeSwitcherVisibleProperty() {
        return modeSwitcherVisible;
    }
    
    /**
     * 
     * @return whether a visual option is presented to
     * users to switch between the various {@link Mode modes} available.
     */
    public final boolean isModeSwitcherVisible() {
        return modeSwitcherVisible.get();
    }
    
    /**
     * Set whether a visual option should be presented to
     * users to switch between the various {@link Mode modes} available.
     * @param visible 
     */
    public final void setModeSwitcherVisible( boolean visible ) {
        modeSwitcherVisible.set(visible);
    }
    
    
    // --- toolbarSearchVisibleProperty
    private final SimpleBooleanProperty searchBoxVisible = 
            new SimpleBooleanProperty(this, "searchBoxVisible", true); //$NON-NLS-1$
    
    /**
     * 
     */
    /**
     * This property represents whether a text field should be presented to
     * users to allow for them to filter the properties in the property sheet to
     * only show ones matching the typed input. By default this is true, so 
     * setting it to false will hide this search field.
     * @return A SimpleBooleanProperty.
     */
    public final SimpleBooleanProperty searchBoxVisibleProperty() {
        return searchBoxVisible;
    }
    
    /**
     * 
     * @return whether a text field should be presented to
     * users to allow for them to filter the properties in the property sheet to
     * only show ones matching the typed input.
     */
    public final boolean isSearchBoxVisible() {
        return searchBoxVisible.get();
    }
    
    /**
     * Sets whether a text field should be presented to
     * users to allow for them to filter the properties in the property sheet to
     * only show ones matching the typed input.
     * @param visible 
     */
    public final void setSearchBoxVisible( boolean visible ) {
        searchBoxVisible.set(visible);
    }   
    
     
    // --- titleFilterProperty
    private final SimpleStringProperty titleFilterProperty = 
            new SimpleStringProperty(this, "titleFilter", ""); //$NON-NLS-1$ //$NON-NLS-2$
    
    /**
     * Regardless of whether the {@link #searchBoxVisibleProperty() search box}
     * is visible or not, it is possible to filter the options shown on screen
     * using this title filter property. If the search box is visible, it will
     * manipulate this property to contain whatever the user types.
     * @return A SimpleStringProperty.
     */
    public final SimpleStringProperty titleFilter() {
        return titleFilterProperty;
    }
    
    /**
     * @see #titleFilter() 
     * @return the filter for filtering the options shown on screen
     */
    public final String getTitleFilter() {
        return titleFilterProperty.get();
    }
    
    /**
     * Sets the filter for filtering the options shown on screen.
     * @param filter 
     * @see #titleFilter() 
     */
    public final void setTitleFilter( String filter ) {
        titleFilterProperty.set(filter);
    }
    
    
    
    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "property-sheet"; //$NON-NLS-1$
    
}
