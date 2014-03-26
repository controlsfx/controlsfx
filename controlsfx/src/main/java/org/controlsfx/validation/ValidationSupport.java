/**
 * Copyright (c) 2014, ControlsFX
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
package org.controlsfx.validation;

import static org.controlsfx.control.decoration.Decorator.addDecoration;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.util.Callback;

import org.controlsfx.control.decoration.Decorator;

/**
 * Provides validation support for UI components. The idea is create an instance of this class the component group, usually a panel.<br>
 * Once created, {@link Validator}s can be registered for components, to provide the validation:
 * 
 *     <pre>
 *        ValidationSupport validationSupport = new ValidationSupport();
 *        validationSupport.registerValidator(textField, Validator.createEmptyValidator("Text is required"));
 *        validationSupport.registerValidator(combobox, Validator.createEmptyValidator( "ComboBox Selection required"));
 *        validationSupport.registerValidator(checkBox, (Control c, Boolean newValue) -> 
 *        	    ValidationResult.fromErrorIf( c, "Checkbox should be checked", !newValue)
 *         );
 *     </pre>
 *     
 *  validationResultProperty provides an ability to react on overall validation result changes:
 *  <pre>
 *     validationSupport.validationResultProperty().addListener( (o, oldValue, newValue) ->
        	 messageList.getItems().setAll(newValue.getMessages()));
 *  </pre>   
 *  
 *  Standard JavaFX UI controls are supported out of the box. There is also an ability to add support for custom controls.
 *  To do that "observable value extractor" should be added for specific controls. Such "extractor" consists of two functional interfaces:
 *  a {@link Predicate} to check the applicability of the control and a {@link Callback} to extract control's observable value. 
 *  Here is an sample of internal registration of such "extractor" for  a few  controls :
 *  <pre>
 *     addObservableValueExtractor( c -> c instanceof TextInputControl, c -> ((TextInputControl)c).textProperty());
 *     addObservableValueExtractor( c -> c instanceof ComboBox,         c -> ((ComboBox<?>)c).valueProperty());
 *  </pre>
 *   
 */
public class ValidationSupport {
	
	private static class ObservableValueExtractor {
		
		public final Predicate<Control> applicability;
		public final Callback<Control, ObservableValue<?>> extraction;
		
		public ObservableValueExtractor( Predicate<Control> applicability, Callback<Control, ObservableValue<?>> extraction ) {
			this.applicability = Objects.requireNonNull(applicability);
			this.extraction    = Objects.requireNonNull(extraction);
		}
		
	}

	private static List<ObservableValueExtractor> extractors = FXCollections.observableArrayList(); 
	
	/**
	 * Add "obervable value extractor" for custom controls.
	 * @param test applicability test
	 * @param extract extraction of observable value
	 */
	public static void addObservableValueExtractor( Predicate<Control> test, Callback<Control, ObservableValue<?>> extract ) {
		extractors.add( new ObservableValueExtractor(test, extract));
	}
	
	{
		addObservableValueExtractor( c -> c instanceof TextInputControl, c -> ((TextInputControl)c).textProperty());
		addObservableValueExtractor( c -> c instanceof ComboBox,         c -> ((ComboBox<?>)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof ChoiceBox,        c -> ((ChoiceBox<?>)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof CheckBox,         c -> ((CheckBox)c).selectedProperty());
		addObservableValueExtractor( c -> c instanceof Slider,           c -> ((Slider)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof ColorPicker,      c -> ((ColorPicker)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof DatePicker,       c -> ((DatePicker)c).valueProperty());
		
		addObservableValueExtractor( c -> c instanceof ListView,         c -> ((ListView<?>)c).itemsProperty());
		addObservableValueExtractor( c -> c instanceof TableView,        c -> ((TableView<?>)c).itemsProperty());
		
		// FIXME: How to listen for TreeView changes???
		//addObservableValueExtractor( c -> c instanceof TreeView,         c -> ((TreeView<?>)c).Property());
	}
	
	private static String CTRL_REQUIRED_FLAG = "controlsfx..validation.required";
	
	/**
	 * Set control's required flag
	 * @param c control
	 * @param required flag
	 */
	public static void setRequired( Control c, boolean required ) {
		c.getProperties().put(CTRL_REQUIRED_FLAG, required );
	}
	
	/**
	 * Check control's required flag
	 * @param c control
	 * @return true if required 
	 */
	public static boolean isRequired( Control c ) {
		Object value = c.getProperties().get(CTRL_REQUIRED_FLAG);
		return value instanceof Boolean? (Boolean)value: false;
	}
	
	private ObservableSet<Control> controls = FXCollections.observableSet();
	private ObservableMap<Control,ValidationResult> validationResults = 
			FXCollections.observableMap(new WeakHashMap<>());
	
	/**
	 * Creates validation support instance
	 */
	public ValidationSupport() {
		
		// notify validation result observers
		validationResults.addListener( (MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) ->
			validationResultProperty.set(ValidationResult.fromResults(validationResults.values()))
		);
		
		// validation decoration
		// TODO needs optimizaion
		validationResultProperty().addListener( (o, oldValue, validationResult) -> {
			ValidationDecorator decorator = getValidationDecorator();
			if ( decorator != null ) {
	        	for( Control target: getKnownControls()) {
	        		try {
		        		Decorator.removeAllDecorations(target);
	 	        		getHighestMessage(target).ifPresent( msg -> {
			        		addDecoration(target, decorator.createDecoration(msg));
		        		});
	        		} catch ( Throwable ex ) {
	        			// FIXME Decorator throws an exception on the first run
	        			ex.printStackTrace();
	        		}
	        	}
        	}
        }
        );
	}
	
	private ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = 
			new ReadOnlyObjectWrapper<ValidationResult>();
	
	
	/**
	 * Retrieves current validation result
	 * @return validation result
	 */
	public ValidationResult getValidationResult() {
		return validationResultProperty.get();
	}
	
	/**
	 * Validation result property. Can be used to track validation result changes 
	 * @return
	 */
	public ReadOnlyObjectProperty<ValidationResult> validationResultProperty() {
		return validationResultProperty.getReadOnlyProperty();
	}
	
	private ObjectProperty<ValidationDecorator> validationDecoratorProperty =
			new SimpleObjectProperty<>(new IconValidationDecorator());
	
	public ObjectProperty<ValidationDecorator> validationDecoratorProperty() {
		return validationDecoratorProperty;
	}
	
	public ValidationDecorator getValidationDecorator() {
		return validationDecoratorProperty.get();
	}
	
	public void setValidationDecorator( ValidationDecorator decorator ) {
		if ( getValidationDecorator() != null && decorator == null ) {
			for( Control target: getKnownControls()) {
	           Decorator.removeAllDecorations(target);
			}
		}
		validationDecoratorProperty.set(decorator);
	}
	
	private Optional<ObservableValueExtractor> getExtractor(final Control c) {
		for( ObservableValueExtractor e: extractors ) {
			if ( e.applicability.test(c)) return Optional.of(e);
		}
		return Optional.empty();
	}

	/**
	 * Registers {@link Validator} for specified control with additional possiblity to mark control as required or not.
	 * @param c control to validate
	 * @param required true if controls should be required
	 * @param validator {@link Validator} to be used
	 * @return true if registration is successful
	 */
	@SuppressWarnings("unchecked")
	public <T> boolean registerValidator( final Control c, boolean required, final Validator<T> validator  ) {
		
		return getExtractor(c).map( e -> {
			
			ObservableValue<T> observable = (ObservableValue<T>) e.extraction.call(c);
			setRequired( c, required );
			
			Consumer<T> updateResults = value -> validationResults.put(c, validator.apply(c, value));
			
			controls.add(c);
			observable.addListener( (o,oldValue,newValue) -> updateResults.accept(newValue));
			updateResults.accept(observable.getValue());
			
			return e;
			
		}).isPresent();
	}
	
	public Set<Control> getKnownControls() {
		return Collections.unmodifiableSet(controls);
	}
	
	public Optional<ValidationMessage> getHighestMessage(Control target) {
		return Optional.ofNullable(validationResults.get(target)).map( result -> 
			result.getMessages().stream().max( ValidationMessage.COMPARATOR).orElse(null)
		);
	}
	
	/**
	 * Registers {@link Validator} for specified control and makes control required
	 * @param c control to validate
	 * @param validator {@link Validator} to be used
	 * @return true if registration is successful
	 */
	public <T> boolean registerValidator( final Control c, final Validator<T> validator  ) {
		return registerValidator(c, true, validator);
	}

}
