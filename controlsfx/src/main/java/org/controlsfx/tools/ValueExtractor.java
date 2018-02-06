/**
 * Copyright (c) 2014 ControlsFX
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
package org.controlsfx.tools;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class ValueExtractor {
	
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
        extractors.add(0, new ObservableValueExtractor(test, extract));
    }

    static {
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
	
	
	
    public static final Optional<Callback<Control, ObservableValue<?>>> getObservableValueExtractor(final Control c) {
        for( ObservableValueExtractor e: extractors ) {
            if ( e.applicability.test(c)) return Optional.of(e.extraction);
        }
        return Optional.empty();
    }
    
    
    private static class NodeValueExtractor {

        public final Predicate<Node> applicability;
		public final Callback<Node, Object> extraction;

        public NodeValueExtractor( Predicate<Node> applicability, Callback<Node, Object> extraction ) {
            this.applicability = Objects.requireNonNull(applicability);
            this.extraction    = Objects.requireNonNull(extraction);
        }

    }
    

    private static final List<NodeValueExtractor> valueExtractors = FXCollections.observableArrayList();
    
    static {
        addValueExtractor( n -> n instanceof CheckBox,         cb -> ((CheckBox)cb).isSelected());
        addValueExtractor( n -> n instanceof ChoiceBox,        cb -> ((ChoiceBox<?>)cb).getValue());
        addValueExtractor( n -> n instanceof ComboBox,         cb -> ((ComboBox<?>)cb).getValue());
        addValueExtractor( n -> n instanceof DatePicker,       dp -> ((DatePicker)dp).getValue());
        addValueExtractor( n -> n instanceof RadioButton,      rb -> ((RadioButton)rb).isSelected());
        addValueExtractor( n -> n instanceof Slider,           sl -> ((Slider)sl).getValue());
        addValueExtractor( n -> n instanceof TextInputControl, ta -> ((TextInputControl)ta).getText());
        
        addValueExtractor( n -> n instanceof ListView, lv -> {
            MultipleSelectionModel<?> sm = ((ListView<?>)lv).getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor( n -> n instanceof TreeView, tv -> {
            MultipleSelectionModel<?> sm = ((TreeView<?>)tv).getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor( n -> n instanceof TableView, tv -> {
            MultipleSelectionModel<?> sm = ((TableView<?>)tv).getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor( n -> n instanceof TreeTableView, tv -> {
            MultipleSelectionModel<?> sm = ((TreeTableView<?>)tv).getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
    }
    
    private ValueExtractor() {
        // no-op
    }
    
    public static void addValueExtractor(Predicate<Node> test, Callback<Node, Object> extractor) {
        valueExtractors.add(0, new NodeValueExtractor(test, extractor));
    }
    
    /**
     * Attempts to return a value for the given Node. This is done by checking
     * the map of value extractors, contained within this class. This
     * map contains value extractors for common UI controls, but more extractors
     * can be added by calling {@link #addObservableValueExtractor(Predicate, Callback)}.
     * 
     * @param n The node from whom a value will hopefully be extracted.
     * @return The value of the given node.
     */
    public static Object getValue(Node n) {
    	for( NodeValueExtractor nve: valueExtractors ) {
            if ( nve.applicability.test(n)) return nve.extraction.call(n);
        }
        return null;
    }
}