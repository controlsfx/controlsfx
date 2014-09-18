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
package org.controlsfx.dialog.wizard;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.util.Callback;

public class ValueExtractor {

    @SuppressWarnings("rawtypes")
    private static final Map<Class, Callback> valueExtractors = new HashMap<>();
    static {
        addValueExtractor(CheckBox.class,       cb -> cb.isSelected());
        addValueExtractor(ChoiceBox.class,      cb -> cb.getValue());
        addValueExtractor(ComboBox.class,       cb -> cb.getValue());
        addValueExtractor(DatePicker.class,     dp -> dp.getValue());
        addValueExtractor(PasswordField.class,  pf -> pf.getText());
        addValueExtractor(RadioButton.class,    rb -> rb.isSelected());
        addValueExtractor(Slider.class,         sl -> sl.getValue());
        addValueExtractor(TextArea.class,       ta -> ta.getText());
        addValueExtractor(TextField.class,      tf -> tf.getText());
        
        addValueExtractor(ListView.class, lv -> {
            MultipleSelectionModel<?> sm = lv.getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor(TreeView.class, tv -> {
            MultipleSelectionModel<?> sm = tv.getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor(TableView.class, tv -> {
            MultipleSelectionModel<?> sm = tv.getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
        addValueExtractor(TreeTableView.class, tv -> {
            MultipleSelectionModel<?> sm = tv.getSelectionModel();
            return sm.getSelectionMode() == SelectionMode.MULTIPLE ? sm.getSelectedItems() : sm.getSelectedItem();
        });
    }
    
    private ValueExtractor() {
        // no-op
    }
    
    public static <T> void addValueExtractor(Class<T> clazz, Callback<T, Object> extractor) {
        valueExtractors.put(clazz, extractor);
    }
    
    /**
     * Attempts to return a value for the given Node. This is done by checking
     * the map of value extractors, contained within this class. This
     * map contains value extractors for common UI controls, but more extractors
     * can be added by calling {@link #addValueExtractor(Class, javafx.util.Callback)}.
     * 
     * @param n The node from whom a value will hopefully be extracted.
     * @return The value of the given node.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Object getValue(Node n) {
        Object value = null;
        
        if (valueExtractors.containsKey(n.getClass())) {
            Callback callback = valueExtractors.get(n.getClass());
            value = callback.call(n);
        }
        
        return value;
    }
}