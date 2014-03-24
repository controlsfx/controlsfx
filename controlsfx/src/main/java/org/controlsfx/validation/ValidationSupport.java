package org.controlsfx.validation;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.function.Consumer;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
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
	
	private static String CTRL_REQUIRED_FLAG = "controlsfx.required.control";
	
	public static void setRequired( Control c, boolean required ) {
		c.getProperties().put(CTRL_REQUIRED_FLAG, required );
	}
	
	public static boolean isRequired( Control c ) {
		Object value = c.getProperties().get(CTRL_REQUIRED_FLAG);
		return value instanceof Boolean? (Boolean)value: false;
	}
	
	
	private ObservableMap<Control,ValidationResult> validationResults = 
			FXCollections.observableMap(new WeakHashMap<>());
	
	public ValidationSupport() {
		validationResults.addListener( (MapChangeListener.Change<? extends Control, ? extends ValidationResult> change) ->
			validationResultProperty.set(ValidationResult.fromResults(validationResults.values()))
		);
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
			if ( e.applicability.test(c)) return Optional.of(e);
		}
		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	public <T> boolean registerValidator( final Control c, boolean required, final Validator<T> validator  ) {
		
		return getExtractor(c).map( e -> {
			
			ObservableValue<T> observable = (ObservableValue<T>) e.extraction.call(c);
			setRequired( c, required );
			
			Consumer<T> updateResults = value -> validationResults.put(c, validator.apply(c, value));
			
			observable.addListener( (o,oldValue,newValue) -> updateResults.accept(newValue));
			updateResults.accept(observable.getValue());
			
			return e;
			
		}).isPresent();
	}
	
	public <T> boolean registerValidator( final Control c, final Validator<T> validator  ) {
		return registerValidator(c, true, validator);
	}

}
