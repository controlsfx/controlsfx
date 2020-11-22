/**
 * Copyright (c) 2018 ControlsFX
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
package org.controlsfx.control.tableview2.filter.popupfilter;

import impl.org.controlsfx.tableview2.filter.popupfilter.PopupFilterSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.filter.parser.Parser;

import java.util.List;

/**
 * A popup control containing a {@link javafx.scene.control.TextField} to filter data in a 
 * {@link org.controlsfx.control.tableview2.FilteredTableView}.
 * The control accepts a {@link FilteredTableColumn} on which the filter is to be
 * applied.
 * {@link #showPopup()} should be called to show the popup.
 *
 * @param <S> Type of the objects contained within the 
 *      {@link org.controlsfx.control.tableview2.FilteredTableView} items list.
 * @param <T> Type of the content to be filtered,
 *           which is similar to the type of cells contained in the 
 *      {@link FilteredTableColumn}.
 */
public abstract class PopupFilter<S, T> extends PopupControl {

    private final FilteredTableColumn<S, T> tableColumn;

    /**
     * Creates a new instance of PopupFilter.
     * @param tableColumn TableColumn associated with this PopupFilter.
     */
    public PopupFilter(FilteredTableColumn<S, T> tableColumn) {
        this.tableColumn = tableColumn;

        setAutoHide(true);
        setAutoFix(true);
        setHideOnEscape(true);
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    /**
     * The textual content of this PopupFilter.
     */
    protected final StringProperty text = new SimpleStringProperty(this, "text");
    public final StringProperty textProperty() {
       return text;
    }
    public final String getText() {
       return text.get();
    }
    public final void setText(String value) {
        text.set(value);
    }

    /**
     * Shows the pop up just below the column header.
     */
    public void showPopup() {
        Node node = tableColumn.getGraphic().getParent().getParent();

        if (node.getScene() == null || node.getScene().getWindow() == null) {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window."); //$NON-NLS-1$
        }
        
        if (isShowing()) {
            return;
        }

        Window parent = node.getScene().getWindow();
        this.show(
                parent,
                parent.getX() + node.localToScene(0, 0).getX() +
                        node.getScene().getX(),
                parent.getY() + node.localToScene(0, 0).getY() +
                        node.getScene().getY() + node.getLayoutBounds().getHeight());

    }

    /**
     * Returns a list of operations which can be performed on this PopupFilter.
     * @return A list of operations.
     */
    public abstract List<String> getOperations();

    /**
     * Returns a TextParser which is used to parse the text in the TextField
     * and filter the data.
     * @return A {@link Parser}.
     */
    public abstract Parser<T> getParser();

    /**
     * Returns the TableColumn associated with this PopupFilter.
     * @return TableColumn associated with this PopupFilter.
     */
    public FilteredTableColumn<S, T> getTableColumn() {
        return tableColumn;
    }

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new PopupFilterSkin<>(this);
    }

    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static final String DEFAULT_STYLE_CLASS = "pop-up-filter";

}
