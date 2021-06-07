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
package org.controlsfx.control.tableview2.filter.filtermenubutton;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import java.util.function.Predicate;

/**
 * Abstract class for a MenuButton extension with operators and parse
 * operations
 */
public abstract class FilterMenuButton extends MenuButton {

    protected Label symbol;
    protected StringProperty buttonText = new SimpleStringProperty();
    
    public FilterMenuButton() {
        if (getSkin() != null) {
            insertSymbol();
        } else {
            skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (getSkin() != null) {
                        insertSymbol();
                        skinProperty().removeListener(this);
                    }
                }
            });
        }
    }
    
    public abstract Predicate<?> parse(String text);
    
    public abstract Predicate<?> parse(String text, StringConverter<?> converter);
    
    private void insertSymbol() {
        StackPane arrow = (StackPane) lookup(".arrow");
        if (arrow != null) {
            arrow.getStyleClass().setAll("operator");
            symbol = new Label();
            symbol.getStyleClass().add("symbol");
            arrow.getChildren().add(symbol);
            symbol.textProperty().bind(buttonText);
        }
    }
    
    public abstract String getErrorMessage();
    
}
