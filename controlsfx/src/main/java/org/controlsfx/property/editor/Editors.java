/**
 * Copyright (c) 2015 ControlsFX
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
package org.controlsfx.property.editor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.controlsfx.dialog.FontSelectorDialog;

@SuppressWarnings("deprecation")
public class Editors {

    private Editors() {
        // no op
    }
    
    public static final PropertyEditor<?> createTextEditor( Item property ) {
    
        return new AbstractPropertyEditor<String, TextField>(property, new TextField()) {

            { enableAutoSelectAll(getEditor()); } 
            
            @Override protected StringProperty getObservableValue() {
                return getEditor().textProperty();
            }
    
            @Override public void setValue(String value) {
                getEditor().setText(value);
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    public static final PropertyEditor<?> createNumericEditor( Item property ) {

    	return new AbstractPropertyEditor<Number, NumericField>(property, new NumericField( (Class<? extends Number>) property.getType())) {

			private Class<? extends Number> sourceClass = (Class<? extends Number>) property.getType(); //Double.class;
            
            { enableAutoSelectAll(getEditor()); }

            @Override protected ObservableValue<Number> getObservableValue() {
                return getEditor().valueProperty();
            }

            @Override public Number getValue() {
                try {
                    return sourceClass.getConstructor(String.class).newInstance(getEditor().getText());
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override public void setValue(Number value) {
                sourceClass = (Class<? extends Number>) value.getClass();
                getEditor().setText(value.toString());
            }
            
        };   
    }
    
    public static final PropertyEditor<?> createCheckEditor( Item property ) {
     
        return new AbstractPropertyEditor<Boolean, CheckBox>(property, new CheckBox()) {

            @Override protected BooleanProperty getObservableValue() {
                return getEditor().selectedProperty();
            }
            
            @Override public void setValue(Boolean value) {
                getEditor().setSelected((Boolean)value);
            }
        };
        
    }
    
    public static final <T> PropertyEditor<?> createChoiceEditor( Item property, final Collection<T> choices ) {
         
        return new AbstractPropertyEditor<T, ComboBox<T>>(property, new ComboBox<T>()) {
            
            { getEditor().setItems(FXCollections.observableArrayList(choices)); }
            
            @Override protected ObservableValue<T> getObservableValue() {
                return getEditor().getSelectionModel().selectedItemProperty();
            }

            @Override public void setValue(T value) {
                getEditor().getSelectionModel().select(value);
            }
        };
    }
    
    public static final PropertyEditor<?> createColorEditor( Item property ) {
        return new AbstractPropertyEditor<Color, ColorPicker>(property, new ColorPicker()) {

            @Override protected ObservableValue<Color> getObservableValue() {
                return getEditor().valueProperty();
            }

            @Override public void setValue(Color value) {
                getEditor().setValue((Color) value);
            }
        };
    }
    
    
    public static final PropertyEditor<?> createDateEditor( Item property ) {
        return new AbstractPropertyEditor<LocalDate, DatePicker>(property, new DatePicker()) {
            
            //TODO: Provide date picker customization support
            
            @Override protected ObservableValue<LocalDate> getObservableValue() {
                return getEditor().valueProperty();
            }

            @Override public void setValue(LocalDate value) {
                getEditor().setValue((LocalDate) value);
            }
        };
    }    
    
    public static final PropertyEditor<?> createFontEditor( Item property ) {
        
        return new AbstractPropertyEditor<Font, AbstractObjectField<Font>>(property, new AbstractObjectField<Font>() {
                    @Override protected Class<Font> getType() {
                        return Font.class;
                    }
                    
                    @Override protected String objectToString(Font font) {
                        return font == null? "": String.format("%s, %.1f", font.getName(), font.getSize()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
        
                    @Override protected Font edit(Font font) {
                        FontSelectorDialog dlg = new FontSelectorDialog(font);
                        Optional<Font> optionalFont = dlg.showAndWait();
                        return optionalFont.orElse(null);
                    }
                }) {

            @Override protected ObservableValue<Font> getObservableValue() {
                return getEditor().getObjectProperty();
            }

            @Override public void setValue(Font value) {
                getEditor().getObjectProperty().set(value);
            }
        };
        
    }
    
    /**
     * Static method used to create an instance of the custom editor returned 
     * via a call to {@link Item#getPropertyEditorClass() } 
     * 
     * The class returned must declare a constructor that takes a single 
     * parameter of type PropertySheet.Item into which the parameter supplied 
     * to this method will be passed.
     * 
     * @param property The {@link Item} that this editor will be 
     * associated with.
     * @return The {@link PropertyEditor} wrapped in an {@link Optional}
     */
    public static final Optional<PropertyEditor<?>> createCustomEditor(final Item property ) {
        return property.getPropertyEditorClass().map(cls -> {
            try {
                Constructor<?> cn = cls.getConstructor(PropertySheet.Item.class);
                if (cn != null) {
                    return (PropertyEditor<?>) cn.newInstance(property);
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return null;
        });
    }
    
    private static void enableAutoSelectAll(final TextInputControl control) {
        control.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                Platform.runLater(() -> {
                    control.selectAll();
                });
            }
        });
    }
    
}
