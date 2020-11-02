/**
 * Copyright (c) 2016 ControlsFX
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
package impl.org.controlsfx.skin;

import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.Node;
import javafx.scene.control.TableRow;
import org.controlsfx.control.table.TableRowExpanderColumn;

/**
 * This skin is installed when you assign a {@link org.controlsfx.control.table.TableRowExpanderColumn} to a TableView.
 * The skin will render the expanded node produced by the
 * {@link org.controlsfx.control.table.TableRowExpanderColumn#expandedNodeCallback} whenever the expanded state is
 * changed to true for a certain row.
 *
 * @param <S> The type of items in the TableRow
 */
public class ExpandableTableRowSkin<S> extends TableRowSkin<S> {
    private final TableRow<S> tableRow;
    private TableRowExpanderColumn<S> expander;
    private Double tableRowPrefHeight = -1D;

    /**
     * Create the ExpandableTableRowSkin and listen to changes for the item this table row represents. When the
     * item is changed, the old expanded node, if any, is removed from the children list of the TableRow.
     *
     * @param tableRow The table row to apply this skin for
     * @param expander The expander column, used to retrieve the expanded node when this row is expanded
     */
    public ExpandableTableRowSkin(TableRow<S> tableRow, TableRowExpanderColumn<S> expander) {
        super(tableRow);
        this.tableRow = tableRow;
        this.expander = expander;
        tableRow.itemProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                Node expandedNode = this.expander.getExpandedNode(oldValue);
                if (expandedNode != null) getChildren().remove(expandedNode);
            }
        });
    }

    /**
     * Create the expanded content node that should represent the current table row.
     *
     * If the expanded content node is not currently in the children list of the TableRow it is automatically added.
     *
     * @return The expanded content Node
     */
    private Node getContent() {
        Node node = expander.getOrCreateExpandedNode(tableRow);
        if (!getChildren().contains(node)) getChildren().add(node);
        return node;
    }

    /**
     * Check if the current node is expanded. This is done by checking that there is an item for the current row,
     * and that the expanded property for the row is true.
     *
     * @return A boolean indicating the expanded state of this row
     */
    private Boolean isExpanded() {
        return getSkinnable().getItem() != null && expander.getCellData(getSkinnable().getIndex());
    }

    /**
     * Add the preferred height of the expanded Node whenever the expanded flag is true.
     *
     * @return The preferred height of the TableRow, appended with the preferred height of the expanded node
     * if this row is currently expanded.
     */
    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        tableRowPrefHeight = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        return isExpanded() ? tableRowPrefHeight + getContent().prefHeight(width) : tableRowPrefHeight;
    }

    /**
     * Lay out the columns of the TableRow, then add the expanded content node below if this row is currently expanded.
     */
    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        if (isExpanded()) getContent().resizeRelocate(0.0, tableRowPrefHeight, w, h - tableRowPrefHeight);
    }
}
