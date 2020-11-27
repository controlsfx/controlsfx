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
package org.controlsfx.control.tableview2.filter.filtereditor;

import impl.org.controlsfx.tableview2.filter.filtermenubutton.DefaultFilterMenuButtonFactory;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.filtermenubutton.FilterMenuButton;

/**
 * A container that can be placed in the south node of a 
 * {@link FilteredTableColumn}. Includes a
 * {@link FilterEditor} allowing filtering the column by adding text to it or
 * by selecting an item from the popup list.
 *
 * It also includes a MenuButton that when displayed shows possible operators
 * that will be applied to perform the search
 *
 * See {@link org.controlsfx.control.tableview2.TableColumn2#southNodeProperty() }
 *
 * @param <S> Type of the objects contained within the
 *      {@link org.controlsfx.control.tableview2.FilteredTableView} items list.
 * @param <T> Type of the content to be filtered,
 *           which is similar to the type of cells contained in the
 *      {@link FilteredTableColumn}.
 */
public class SouthFilter<S, T> extends HBox {
    
    private final FilterEditor<S, T> filterEditor;
    private FilterMenuButton menuButton;
    private final FilteredTableColumn<S, T> tableColumn;
    private final Class<T> clazz;
    
    private final InvalidationListener buttonListener = (Observable o) -> {
            getChildren().remove(menuButton);
            menuButton = getFilterMenuButtonFactory();
            getChildren().add(menuButton);
        };

    /**
     * Creates a new SouthFilter instance
     * @param tableColumn TableColumn on which this SouthFilter will be attached
     * @param clazz The type of SouthFilter. By default, this helps filter menu button
     *             to populate a list of operation permitted by the filter.
     */
    public SouthFilter(FilteredTableColumn<S, T> tableColumn, Class<T> clazz) {
        this.tableColumn = tableColumn;
        this.clazz = clazz;
        filterEditor = new FilterEditor<>(tableColumn);
        HBox.setHgrow(filterEditor, Priority.ALWAYS);
        
        menuButton = getFilterMenuButtonFactory();
        filterMenuButtonFactoryProperty().addListener(new WeakInvalidationListener(buttonListener));
        
        getStyleClass().add("south-filter");
        getChildren().addAll(menuButton, filterEditor);
    }

    public FilterEditor<S, T> getFilterEditor() {
        return filterEditor;
    }

    public FilteredTableColumn<S, T> getTableColumn() {
        return tableColumn;
    }

    public FilterMenuButton getMenuButton() {
        return menuButton;
    }
    
    /**
     * This property allows setting a {@link FilterMenuButton} for the South 
     * Filter container.
     * 
     * By default a {@link FilterMenuButton} is provided for String, Object and 
     * Number types.
     */
    private SimpleObjectProperty<FilterMenuButton> filterMenuButtonFactory;
    public final SimpleObjectProperty<FilterMenuButton> filterMenuButtonFactoryProperty() {
        if (filterMenuButtonFactory == null) {
            filterMenuButtonFactory = new SimpleObjectProperty<>(this, "filterMenuButtonFactory", DefaultFilterMenuButtonFactory.forClass(clazz));
        }
        return filterMenuButtonFactory;
    }
    public final FilterMenuButton getFilterMenuButtonFactory() {
        return filterMenuButtonFactoryProperty().get();
    }
    public final void setFilterMenuButtonFactory(FilterMenuButton value) {
        filterMenuButtonFactoryProperty().set(value);
    }
}
