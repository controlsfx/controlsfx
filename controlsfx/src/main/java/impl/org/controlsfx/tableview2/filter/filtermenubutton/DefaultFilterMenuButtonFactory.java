/**
 * Copyright (c) 2013, 2018 ControlsFX
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
package impl.org.controlsfx.tableview2.filter.filtermenubutton;

import impl.org.controlsfx.tableview2.filter.parser.aggregate.AggregatorsParser;
import impl.org.controlsfx.tableview2.filter.parser.number.NumberParser;
import impl.org.controlsfx.tableview2.filter.parser.string.StringParser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import org.controlsfx.control.tableview2.filter.filtermenubutton.FilterMenuButton;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

/**
 * Two possible implementations of FilterMenuButton, one for Numbers, one for
 * String (or Objects with StringConverter)
 */
public class DefaultFilterMenuButtonFactory {

    
    public static FilterMenuButton forClass(Class<?> clazz) {
        if (isNumber(clazz)) {
            return new FilterNumberMenuButton();
        } 
        return new FilterStringMenuButton();
    }
    
    private static class FilterStringMenuButton extends FilterMenuButton {

        private final List<MenuItem> items;

        private StringParser<?> operator = new StringParser<>();
        private String operatorString = "";
        private final CheckBox caseSensitive;

        public FilterStringMenuButton() {
            super();
            getStyleClass().add("filter-menu-button");

            caseSensitive = new CheckBox(localize(asKey("southfilter.menubutton.checkbox.sensitive")));
            final CustomMenuItem customMenuItem = new CustomMenuItem(caseSensitive);
            customMenuItem.setHideOnClick(false);
            getItems().add(customMenuItem);
            getItems().add(new SeparatorMenuItem());
            ToggleGroup group = new ToggleGroup();
            items = operator.operators().stream()
                    .filter(opr -> AggregatorsParser.getStrings().noneMatch(s -> s.equals(opr)))
                    .map(op -> {
                        RadioButton radioButton = new RadioButton(op);
                        radioButton.setToggleGroup(group);
                        return new CustomMenuItem(radioButton);
                    })
                    .collect(Collectors.toList());
            getItems().addAll(items);
            group.selectedToggleProperty().addListener((obs, ov, nv) -> 
                updateSymbol((RadioButton) nv));
            group.getToggles().get(0).setSelected(true);
            caseSensitive.selectedProperty().addListener((obs, ov, nv) -> 
                updateSymbol((RadioButton) group.getSelectedToggle()));
        }

        private void updateSymbol(RadioButton radioButton) {
            operator.setCaseSensitive(caseSensitive.isSelected());
            operatorString = radioButton.getText();
            buttonText.set(operator.getSymbol(radioButton.getText()));
            setTooltip(new Tooltip(operatorString + " (" + 
                    localize(asKey("southfilter.menubutton.sensitive." + 
                            (caseSensitive.isSelected() ? "enabled" : "disabled"))) + ")"));
        }

        @Override
        public Predicate parse(String text) {
            return parse(text, null);
        }

        @Override
        public Predicate<?> parse(String text, StringConverter converter) {
            operator.setCaseSensitive(caseSensitive.isSelected());
            operator.setConverter(converter);
            return operator.parse(operatorString + " \"" + text + "\"");
        }

        @Override
        public String getErrorMessage() {
            return operator.getErrorMessage();
        }

    }

    private static class FilterNumberMenuButton extends FilterMenuButton {

        private final List<MenuItem> items;

        private NumberParser<? extends Number> operator = new NumberParser<>();
        private String operatorString = "";

        public FilterNumberMenuButton() {
            super();
            getStyleClass().add("filter-menu-button");

            ToggleGroup group = new ToggleGroup();
            items = operator.operators().stream()
                    .filter(opr -> AggregatorsParser.getStrings().noneMatch(s -> s.equals(opr)))
                    .map(op -> {
                        RadioButton radioButton = new RadioButton(op);
                        radioButton.setToggleGroup(group);
                        return new CustomMenuItem(radioButton);
                    })
                    .collect(Collectors.toList());
            getItems().addAll(items);
            group.selectedToggleProperty().addListener((obs, ov, nv) -> {
                final RadioButton radioButton = (RadioButton) nv;
                    operatorString = radioButton.getText();
                    buttonText.set(operator.getSymbol(operatorString));
            });
            group.getToggles().get(0).setSelected(true);
        }

        @Override
        public Predicate<? extends Number> parse(String text) {
            return operator.parse(operatorString + " " + text);
        }

        @Override
        public Predicate<?> parse(String text, StringConverter<?> converter) {
            return parse(text);
        }
        
        @Override
        public String getErrorMessage() {
            return operator.getErrorMessage();
        }
    }

    private static final List<Class<?>> PRIMITIVE_NUMERIC_TYPES = Arrays.asList(
            byte.class, short.class, int.class,
            long.class, float.class, double.class);

    private static boolean isNumber(Class<?> type) {
        if ( type == null ) return false; 
        return Number.class.isAssignableFrom(type) ||
                PRIMITIVE_NUMERIC_TYPES.stream().anyMatch(cls -> type == cls);
    }
    
}
