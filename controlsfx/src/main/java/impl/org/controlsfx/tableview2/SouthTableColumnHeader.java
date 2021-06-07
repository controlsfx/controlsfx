/**
 * Copyright (c) 2013, 2020 ControlsFX
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
package impl.org.controlsfx.tableview2;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

import static impl.org.controlsfx.tableview2.SouthTableHeaderRow.SOUTH_HEADER_STYLE;

public class SouthTableColumnHeader extends Region {

    private static final double ROW_HEIGHT = 20;
    private final TableViewSkinBase skin;
    private final TableColumn column;
    private TableColumn2 column2;
    private TableView2 tableView2;
    private final Rectangle clip;

    private final InvalidationListener tableColumnWidthListener = o -> requestLayout();
    private final WeakInvalidationListener weakTableColumnWidthListener =
            new WeakInvalidationListener(tableColumnWidthListener);

    private final InvalidationListener southNodeListener = o -> updateSouthNode();
    private final WeakInvalidationListener weakSouthNodeListener =
            new WeakInvalidationListener(southNodeListener);

    private final InvalidationListener southHeaderBlendedListener = o -> updateSouthHeaderColumnStyle();
    private final WeakInvalidationListener weakSouthHeaderBlendedListener =
            new WeakInvalidationListener(southHeaderBlendedListener);

    public SouthTableColumnHeader(final TableViewSkinBase skin, final TableColumnBase columnBase) {
        this.skin = skin;
        this.column = (TableColumn) columnBase;
        getStyleClass().setAll("column-header", "south-header");
        initTableView();
        if (tableView2 == null) {
            column.tableViewProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    initTableView();
                    column.tableViewProperty().removeListener(this);
                }
            });
        }
        if (column instanceof TableColumn2) {
            column2 = (TableColumn2) column;
            column2.southNodeProperty().addListener(weakSouthNodeListener);
            updateSouthNode();
        }
        columnBase.widthProperty().addListener(weakTableColumnWidthListener);
        
        setOnContextMenuRequested(e -> {
            TableColumnBase col = columnBase;
            ContextMenu cm = col.getContextMenu();
            if (cm == null) {
                while (col.getParentColumn() != null && cm == null) {
                    col = col.getParentColumn();
                    cm = col.getContextMenu();
                }
            }
            if (cm != null && e.getSource() instanceof Node) {
                cm.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
            }
        });
        
        clip = new Rectangle(0, 0, column.getWidth(), getHeight());
        clip.widthProperty().bind(column.widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
    }

    private void initTableView() {
        if (column.getTableView() instanceof TableView2) {
            tableView2 = (TableView2) column.getTableView();
            if (tableView2 != null) {
                tableView2.southHeaderBlendedProperty().addListener(weakSouthHeaderBlendedListener);
                updateSouthHeaderColumnStyle();
            }
        }
    }

    private void updateSouthNode() {
        final Node southNode = column2.getSouthNode();
        if (southNode != null) {
            getChildren().add(southNode);
        } else {
            getChildren().clear();
        }
    }
    
    private void updateSouthHeaderColumnStyle() {
        if (tableView2.isSouthHeaderBlended() && ! getStyleClass().contains(SOUTH_HEADER_STYLE)) {
            getStyleClass().add(SOUTH_HEADER_STYLE);
        } else if (! tableView2.isSouthHeaderBlended() && getStyleClass().contains(SOUTH_HEADER_STYLE)) {
            getStyleClass().remove(SOUTH_HEADER_STYLE);
        }
    }
    
    /** {@inheritDoc} */
    @Override protected void layoutChildren() {
        resize(column.getWidth(), getHeight());
        if (! getChildren().isEmpty()) {
            Node n = getChildren().get(0);
            n.resizeRelocate(0, 0, column.getWidth(), getHeight());
        }
    }
    
    /** {@inheritDoc} */
    @Override protected double computePrefWidth(double height) {
        return column.getWidth();
    }
    
    /** {@inheritDoc} */
    @Override protected double computePrefHeight(double width) {
        if (column == null || getChildren().isEmpty()) {
            return 0d;
        }
        Node n = getChildren().get(0);
        if (! n.isVisible() && ! n.isManaged()) {
            return 0d;
        }
        return Math.max(ROW_HEIGHT, n.prefHeight(-1)) + snappedTopInset() + snappedBottomInset();
    }
    
    void dispose() {
        if (column != null) {
            column.widthProperty().removeListener(weakTableColumnWidthListener);
        }
        if (column2 != null) {
            column2.southNodeProperty().removeListener(weakSouthNodeListener);
        }
    }
    
    public TableColumnBase getTableColumn() {
        return column; 
    }

}
