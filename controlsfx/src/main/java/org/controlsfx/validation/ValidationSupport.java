package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.util.Callback;

public class ValidationSupport {
	
	private ObservableMap<Control,ValidationResult> validationResults = 
			FXCollections.observableMap(new WeakHashMap<>());
	
	// this can probably be done better
	private static class ObservableValueExtractor {
		
		private Callback<Control, Boolean> check;
		private Callback<Control, ObservableValue<?>> extract;
		
		public ObservableValueExtractor( Callback<Control, Boolean> check, Callback<Control, ObservableValue<?>> extract ) {
			this.check = check;
			this.extract = extract;
		}
		
		public boolean isApplicable( Control c ) {
			return check.call(c);
		}
		
		public ObservableValue<?> extract( Control c ) {
			return extract.call(c);
		}
		
	}

	private static List<ObservableValueExtractor> extractors = new ArrayList<>();
	
	
	public static void addObservableValueExtractor( Callback<Control, Boolean> check, Callback<Control, ObservableValue<?>> extract ) {
		extractors.add( new ObservableValueExtractor(check, extract));
	}
	
	{
		addObservableValueExtractor( c -> c instanceof TextInputControl, c -> ((TextInputControl)c).textProperty());
		addObservableValueExtractor( c -> c instanceof ComboBox,         c -> ((ComboBox<?>)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof ChoiceBox,        c -> ((ChoiceBox<?>)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof CheckBox,         c -> ((CheckBox)c).selectedProperty());
		addObservableValueExtractor( c -> c instanceof Slider,           c -> ((Slider)c).valueProperty());
		addObservableValueExtractor( c -> c instanceof ColorPicker,      c -> ((ColorPicker)c).valueProperty());
		
		addObservableValueExtractor( c -> c instanceof ListView,         c -> ((ListView<?>)c).itemsProperty());
		addObservableValueExtractor( c -> c instanceof TableView,        c -> ((TableView<?>)c).itemsProperty());
		
		
		// FIXME: How to listen for TreeView changes???
		//addObservableValueExtractor( c -> c instanceof TreeView,         c -> ((TreeView<?>)c).Property());
	}
	
	public ValidationSupport() {
		
		validationResults.addListener( new MapChangeListener<Control, ValidationResult>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) {
				// TODO: lazy binding??
				validationResultProperty.set(ValidationResult.fromResults(validationResults.values()));
			}
		});
	}
	
	private ReadOnlyObjectWrapper<ValidationResult> validationResultProperty = 
			new ReadOnlyObjectWrapper<ValidationResult>();
	
	
	public ValidationResult getValidationResult() {
		return validationResultProperty.get();
	}
	
	public ReadOnlyObjectProperty<ValidationResult> validationResultProperty() {
		return validationResultProperty.getReadOnlyProperty();
	}
	
	private Optional<ObservableValueExtractor> getExtractor(final Control c) {
		for( ObservableValueExtractor e: extractors ) {
			if ( e.isApplicable(c)) return Optional.of(e);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T> boolean registerValidator( final Control c, boolean required, final Validator<T> validator  ) {
		
		return getExtractor(c).map( e -> {
			
			ObservableValue<T> ov = (ObservableValue<T>) e.extract(c);
			ValidationControlUtils.setRequired( c, required );
		
			ov.addListener(new ChangeListener<T>(){
				public void changed(ObservableValue<? extends T> o, T oldValue, T newValue) {
					validationResults.put(c, validator.validate(c, newValue));
				};
		    });
			validationResults.put(c, validator.validate(c, ov.getValue()));
			return e;
			
		}).isPresent();
	}
	
	public <T> boolean registerValidator( final Control c, final Validator<T> validator  ) {
		return registerValidator(c, true, validator);
	}

}
