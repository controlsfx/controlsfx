package org.controlsfx.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
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
		
		public boolean isAppicable( Control c ) {
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
		addObservableValueExtractor( c -> c instanceof TextField, c -> ((TextField)c).textProperty());
		addObservableValueExtractor( c -> c instanceof ComboBox,  c -> ((ComboBox<?>)c).valueProperty());
	}
	
	public ValidationSupport() {
		
		
		validationResults.addListener( new MapChangeListener<Control, ValidationResult>() {
			@Override
			public void onChanged(MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) {
				// TODO: fire global "validation" event with global validationResults asParameter
			}
		});
	}
	
	public ValidationResult getValidationResult() {
		return new ValidationResult().addValidationResults(validationResults.values());
	}
	
	private Optional<ObservableValueExtractor> getExtractor(Control c) {
		for( ObservableValueExtractor e: extractors ) {
			if ( e.isAppicable(c)) return Optional.of(e);
		}
		return Optional.empty();
	}
	
	
	// TODO: Need weak listeners to avoid memory leaks
    // TODO: Should both old and new value be passed into a validator? 
    // TODO: Add 'required' flag
	public <T> void registerValidator( Control c, Callback<T, ValidationResult> validator  ) {
		
		getExtractor(c).ifPresent(e->{
			
			ObservableValue<T> ov = (ObservableValue<T>) e.extract(c);
		
			ov.addListener(new ChangeListener<T>(){
				public void changed(ObservableValue<? extends T> o, T oldValue, T newValue) {
					validationResults.put(c, validator.call(newValue));
				};
		    });
			validationResults.put(c, validator.call(ov.getValue()));
			
		});
		
		
	}

}
