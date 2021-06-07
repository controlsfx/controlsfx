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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionCheck;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.TableView2;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

/**
 * A custom action that can be added to the ContextMenu of a 
 * column header, allowing the user to fix or unfix the column.
 * 
 * This action has to be bound to a CheckMenuItem node.
 * 
 * <h3>Sample</h3>
 * 
 * <p>The following code snippet creates a column with a ContextMenu that contains
 * the fix action for the column:
 * 
 * <pre>
 * {@code
 * TableColumn2<Person,String> firstNameCol = new TableColumn2<>("First Name");
 * firstNameCol.setCellValueFactory(p -> p.getValue().firstNameProperty());
 * ContextMenu cm = ActionUtils.createContextMenu(Arrays.asList(new ColumnFixAction(firstNameColumn)));
 * firstNameColumn.setContextMenu(cm);
 * }</pre>
 * 
 * @see ActionUtils#createContextMenu(java.util.Collection) 
 */
@ActionCheck
public class ColumnFixAction extends Action {

    private InvalidationListener fixedColumnsListener;

    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param column The TableColumn to which the action is applied to
     */
    public ColumnFixAction(TableColumn column) {
        this(column, localize(asKey("tableview2.column.menu.fixed")));
    }

    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param column The TableColumn to which the action is applied to
     * @param name the string to display in the text property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() MenuItem}.
     */
    public ColumnFixAction(TableColumn column, String name) {
        this(column, name, null);
    }

    /**
     * Creates a fix action for a given column. When fired, the action will fix 
     * the column if is allowed and the column wasn't fixed yet, or unfix a fixed
     * column.
     * 
     * The action can be attached for instance to the {@link javafx.scene.control.ContextMenu}
     * of the column.
     * 
     * @param column The TableColumn to which the action is applied to
     * @param name the string to display in the text property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() MenuItem}.
     * @param image the node to display in the graphic property of controls such
     *      as {@link javafx.scene.control.CheckMenuItem#textProperty() CheckMenuItem}.
     */
    public ColumnFixAction(TableColumn column, String name, Node image) {
        super(name);
        
        setGraphic(image);
        if (column != null) {
            fixedColumnsListener = (Observable o) -> setSelected(isFixedColumn(column));
            
            final TableView tableView = column.getTableView();
            if (tableView != null && tableView instanceof TableView2) {
                initialize(column, (TableView2) tableView);
            }
            
            column.tableViewProperty().addListener((o, ov, nv) -> {
                if (ov != null && ov instanceof TableView2) {
                    reset((TableView2) ov);
                }
                if (nv != null && nv instanceof TableView2) {
                    initialize(column, (TableView2) nv);
                }
            });
        }
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return getText();
    }

    private void initialize(TableColumn column, TableView2 tableView) {
        tableView.getFixedColumns().addListener(fixedColumnsListener);
        disabledProperty().bind(tableView.columnFixingEnabledProperty().not()
                .or(column.parentColumnProperty().isNotNull()));
        setSelected(isFixedColumn(column));
        setEventHandler(e -> {
            if (!tableView.getFixedColumns().contains(column)) {
                tableView.getFixedColumns().add(column);
            } else {
                tableView.getFixedColumns().remove(column);
            }
        });
    }

    private void reset(TableView2 tableView) {
        tableView.getFixedColumns().removeListener(fixedColumnsListener);
        disabledProperty().unbind();
        setSelected(false);
        setEventHandler(null);
    }
    
    private boolean isFixedColumn(TableColumn column) {
        while (column.getParentColumn() != null) {
            column = (TableColumn) column.getParentColumn();
        }
        final TableView tableView = column.getTableView();
        return tableView != null && tableView instanceof TableView2 
                && ((TableView2) tableView).getFixedColumns().contains(column);
    } 
}
