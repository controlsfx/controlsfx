/**
 * Copyright (c) 2013, ControlsFX
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

import impl.org.controlsfx.behavior.GridViewBehavior;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.util.Callback;

import org.controlsfx.control.GridView;

import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;

public class GridViewSkin<T> extends VirtualContainerBase<GridView<T>, GridViewBehavior<T>, GridRow<T>> {

    private final ListChangeListener<T> gridViewItemsListener = new ListChangeListener<T>() {
        @Override public void onChanged(ListChangeListener.Change<? extends T> change) {
            updateRowCount();
            getSkinnable().requestLayout();
        }
    };

    private final WeakListChangeListener<T> weakGridViewItemsListener = new WeakListChangeListener<T>(gridViewItemsListener);

    public GridViewSkin(GridView<T> control) {
        super(control, new GridViewBehavior<>(control));
        
        updateGridViewItems();

        flow.setId("virtual-flow");
        flow.setPannable(false);
        flow.setVertical(true);
        flow.setFocusTraversable(getSkinnable().isFocusTraversable());
        flow.setCreateCell(new Callback<VirtualFlow, GridRow<T>>() {
            @Override public GridRow<T> call(VirtualFlow flow) {
                return GridViewSkin.this.createCell();
            }
        });
        getChildren().add(flow);

        updateRowCount();

        // Register listeners
        registerChangeListener(control.itemsProperty(), "ITEMS");
        registerChangeListener(control.cellFactoryProperty(), "CELL_FACTORY");
        registerChangeListener(control.parentProperty(), "PARENT");
        registerChangeListener(control.cellHeightProperty(), "CELL_HEIGHT");
        registerChangeListener(control.cellWidthProperty(), "CELL_WIDTH");
        registerChangeListener(control.horizontalCellSpacingProperty(), "HORIZONZAL_CELL_SPACING");
        registerChangeListener(control.verticalCellSpacingProperty(), "VERTICAL_CELL_SPACING");
        registerChangeListener(control.widthProperty(), "WIDTH_PROPERTY");
    }

    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        if (p == "ITEMS") {
            updateGridViewItems();
        } else if (p == "CELL_FACTORY") {
            flow.recreateCells();
        } else if (p == "CELL_HEIGHT") {
            flow.recreateCells();
        } else if (p == "CELL_WIDTH") {
            updateRowCount();
            flow.recreateCells();
        } else if (p == "HORIZONZAL_CELL_SPACING") {
            updateRowCount();
            flow.recreateCells();
        } else if (p == "VERTICAL_CELL_SPACING") {
            flow.recreateCells();
        } else if (p == "PARENT") {
            if (getSkinnable().getParent() != null && getSkinnable().isVisible()) {
                getSkinnable().requestLayout();
            }
        } else if (p == "WIDTH_PROPERTY") {
            updateRowCount();
        }
    }

    public void updateGridViewItems() {
        if (getSkinnable().getItems() != null) {
            getSkinnable().getItems().removeListener(weakGridViewItemsListener);
        }

        if (getSkinnable().getItems() != null) {
            getSkinnable().getItems().addListener(weakGridViewItemsListener);
        }

        updateRowCount();

        getSkinnable().requestLayout();
    }

    @Override protected void updateRowCount() {
        if (flow == null)
            return;

        int oldCount = flow.getCellCount();
        int newCount = getItemCount();
        
        if (newCount != oldCount) {
            flow.setCellCount(newCount);
            flow.recreateCells();
        } else {
            flow.reconfigureCells();
        }
        updateRows(newCount);
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        double x1 = getSkinnable().getInsets().getLeft();
        double y1 = getSkinnable().getInsets().getTop();
        double w1 = getSkinnable().getWidth() - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight());
        double h1 = getSkinnable().getHeight() - (getSkinnable().getInsets().getTop() + getSkinnable().getInsets().getBottom());

        flow.resizeRelocate(x1, y1, w1, h1);
    }

    @Override public GridRow<T> createCell() {
        GridRow<T> row = new GridRow<>();
        row.updateGridView(getSkinnable());
        return row;
    }

    @Override public int getItemCount() {
        final GridView<?> gv = getSkinnable();
        final ObservableList<?> items = gv.getItems();
        return items == null ? 0 : (items.size() / computeMaxCellsInRow(gv.getWidth()));
    }

    public int computeMaxCellsInRow(double width) {
        return Math.max((int) Math.floor(width / computeCellWidth()), 1);
    }

    protected double computeCellWidth() {
        return getSkinnable().cellWidthProperty().doubleValue() + getSkinnable().horizontalCellSpacingProperty().doubleValue() + getSkinnable().horizontalCellSpacingProperty().doubleValue();
    }

    protected void updateRows(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            GridRow<T> row = flow.getVisibleCell(i);
            if (row != null) {
                // FIXME hacky - need to better understand what this is about
                row.updateIndex(-1);
                row.updateIndex(i);
            }
        }
    }

    protected boolean areRowsVisible() {
        if (flow == null)
            return false;

        if (flow.getFirstVisibleCell() == null)
            return false;

        if (flow.getLastVisibleCell() == null)
            return false;

        return true;
    }
    
    @Override protected double computeMinHeight(double height, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        return 0;
    }
}
