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
package org.controlsfx.control.tableview2.actions;

import javafx.beans.Observable;
import javafx.scene.Node;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionCheck;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.TableView2;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

/**
 * A custom action that can be added to the ContextMenu of any row in the 
 * row header, allowing the user to fix or unfix the row.
 * 
 * This action has to be bound to a {@link javafx.scene.control.CheckMenuItem} node.
 * 
 * <h3>Sample</h3>
 * 
 * <p>The following code snippet provides a ContextMenu that contains
 * the fix action for each row in the row header:
 * 
 * <pre>
 * {@code
 * TableView2<Person> table = new TableView2<Person>();
 * table.setRowHeaderVisible(true);
 * table.setRowHeaderContextMenuFactory((i, person) -> 
 *     ActionUtils.createContextMenu(Arrays.asList(new RowFixAction(this, i))));
 * }</pre>
 * 
 * @see ActionUtils#createContextMenu(java.util.Collection) 
 */
@ActionCheck
public class RowFixAction extends Action {
        
    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param tableView The TableView2 to which the action is applied to
     * @param row The number of row
     */
    public RowFixAction(TableView2 tableView, Integer row) {
        this(tableView, row, localize(asKey("tableview2.rowheader.menu.fixed")));
    }

    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param tableView The TableView2 to which the action is applied to
     * @param row The number of row
     * @param name the string to display in the text property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() MenuItem}.
     */
    public RowFixAction(TableView2 tableView, Integer row, String name) {
        this(tableView, row, name, null);
    }

    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param tableView The TableView2 to which the action is applied to
     * @param row The number of row
     * @param name the string to display in the text property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() MenuItem}.
     * @param image the node to display in the graphic property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() CheckMenuItem}.
     */
    public RowFixAction(TableView2 tableView, Integer row, String name, Node image) {
        super(name);

        setGraphic(image);
        
        if (tableView != null && row != null) {
            disabledProperty().bind(tableView.rowFixingEnabledProperty().not());
            
            tableView.getFixedRows().addListener(((Observable o) -> {
                setSelected(tableView.getFixedRows().contains(row));
            }));
            setSelected(tableView.getFixedRows().contains(row));
            
            setEventHandler(e -> {
                if (! tableView.getFixedRows().contains(row)) {
                    tableView.getFixedRows().add(row);
                } else {
                    tableView.getFixedRows().remove(row);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return getText();
    }
    
}
