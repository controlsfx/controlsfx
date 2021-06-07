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
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.TableView2;

/**
 * A cell column header.
 */
public class NestedTableColumnHeader2 extends NestedTableColumnHeader {

    int lastColumnResized = -1;
    private TableView2Skin<?> skin;
    private TableView2<?> tableView;
    
    public NestedTableColumnHeader2(TableColumnBase<?, ?> tc) {
        super(tc);
    }
    
    private void init() {
        this.skin = (TableView2Skin<?>) getTableSkin();
        this.tableView = (TableView2<?>) skin.getSkinnable();
        /**
         * Resolve https://bitbucket.org/controlsfx/controlsfx/issue/395
         * and https://bitbucket.org/controlsfx/controlsfx/issue/434
         */
        widthProperty().addListener((Observable observable) -> {
            this.skin.hBarValue.clear();
        });
        
        getColumnHeaders().addListener((Observable o) ->
            this.skin.getTableHeaderRow2().updateVisibleLeafColumnHeaders());
        tableView.getVisibleLeafColumns().addListener((Observable o) ->
            this.skin.getTableHeaderRow2().updateVisibleLeafColumnHeaders());
    }

    
    /** {@inheritDoc} */
    @Override protected TableColumnHeader createTableColumnHeader(final TableColumnBase col) {
        if (col == null || col.getColumns().isEmpty() || col == getTableColumn())  {
            final TableColumnHeader tableColumnHeader = new TableColumnHeader(col);
            addMousePressedListener(tableColumnHeader);
            addMouseReleasedListener(tableColumnHeader);
            return tableColumnHeader;
        } else {
            final NestedTableColumnHeader2 rootHeader = new NestedTableColumnHeader2(col);
            final ObservableList<Node> rootChildren = rootHeader.getChildren();
            rootChildren.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable o) {
                    if (rootChildren.size() > 0) {
                        final TableColumnHeader tableColumnHeader = (TableColumnHeader) rootChildren.get(0);
                        addMouseReleasedListener(tableColumnHeader);
                        rootChildren.removeListener(this);
                    }
                }
            });
            addMousePressedListener(rootHeader);
            return rootHeader;
        }
    }

    /** {@inheritDoc} */
    @Override protected void layoutChildren() {
        super.layoutChildren();
        layoutFixedColumns();
        updateDragRectangles();
    }

    /**
     * We want ColumnHeader to be fixed when we freeze some columns
     */
    public void layoutFixedColumns() {
        if (skin == null && tableView == null) {
            init();
        }
        
        if (skin == null || getChildren().isEmpty()) {
            return;
        }
        double h = getHeight() - snappedTopInset() - snappedBottomInset();
        double hbarValue = skin.getHBar().getValue();

        final int labelHeight = (int) getChildren().get(0).prefHeight(-1);
        double fixedColumnWidth = 0;
        double x = snappedLeftInset();
        int max = getColumnHeaders().size();
        max = max > tableView.getVisibleLeafColumns().size() ? tableView.getVisibleLeafColumns().size() : max;
        max = max > tableView.getColumns().size() ? tableView.getColumns().size() : max;
        for (int j = 0; j < max; j++) {
            final TableColumnHeader n = getColumnHeaders().get(j);
            if (! n.isVisible()) {
                continue;
            }
            
            final double prefWidth = snapSize(n.prefWidth(-1));
            n.resize(prefWidth, snapSize(h - labelHeight));
            //If the column is fixed
            TableColumn<?, ?> column = (TableColumn<?, ?>) n.getTableColumn();
            boolean isLeafColumn = column.getParentColumn() != null;
            while (column.getParentColumn() != null) {
                column = (TableColumn<?, ?>) column.getParentColumn();
            }
            if (tableView.isColumnFixingEnabled() && tableView.getFixedColumns().contains(column)) {
                double tableCellX = 0;
                //If the column is hidden we have to translate it
                if (hbarValue + fixedColumnWidth > x) {
                    
                    tableCellX = Math.abs((isLeafColumn ? 0 : hbarValue) + fixedColumnWidth - x);

                    n.toFront();
                    fixedColumnWidth += prefWidth;
                }
                n.relocate(x + tableCellX, labelHeight + snappedTopInset());
            }

            x += prefWidth;
        }
    }
    
    private void updateDragRectangles() {
        // remove drag rectangles from left border as sometimes they are placed 
        // at the origin and it doesn't make sense allowing resizing the row 
        // header or the first column from the left side
        getChildren().removeIf(r -> (r instanceof Rectangle) &&
                (r.getLayoutX() < 1 || (tableView.getParent() instanceof RowHeader)));
    }
    
    /**
     * If a dragging event to reorder the column is cancelled, when the mouse 
     * is released, the column is marked as reorderable so it can be reordered 
     * again.
     */
    private void addMouseReleasedListener(TableColumnHeader tableColumnHeader) {
        tableColumnHeader.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            TableColumnHeader reorderingRegion = skin.getTableHeaderRow2().getReorderingRegion();
            if (tableColumnHeader.equals(reorderingRegion)) {
                tableColumnHeader.getTableColumn().setReorderable(true);
            }
            if (e.getButton() != MouseButton.PRIMARY) {
                // consume event to avoid sorting after right-click
                e.consume();
            }
        });
    }
    
    // fix JDK-8161054
    private void addMousePressedListener(TableColumnHeader tableColumnHeader) {
        tableColumnHeader.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            ContextMenu menu = tableColumnHeader.getTableColumn().getContextMenu();
            if (menu != null && menu.isShowing()) {
                menu.hide();
            }  
        });
    }

}
