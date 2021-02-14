/**
 * Copyright (c) 2013, 2021, ControlsFX
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

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import org.controlsfx.control.GridView;

public class GridViewSkin<T> extends VirtualContainerBase<GridView<T>, GridRow<T>> {

    private VirtualFlow<GridRow<T>> flow;

    private final ListChangeListener<T> gridViewItemsListener = change -> {
        updateItemCount();
        getSkinnable().requestLayout();
    };

    private final WeakListChangeListener<T> weakGridViewItemsListener = new WeakListChangeListener<>(gridViewItemsListener);

    @SuppressWarnings("rawtypes")
    public GridViewSkin(GridView<T> control) {
        super(control);

        flow = getVirtualFlow();
        updateGridViewItems();

        flow.setId("virtual-flow"); //$NON-NLS-1$
        flow.setPannable(false);
        flow.setVertical(true);
        flow.setFocusTraversable(getSkinnable().isFocusTraversable());
        flow.setCellFactory(param -> createCell());
        getChildren().add(flow);

        updateItemCount();

        // Register listeners
        registerChangeListener(control.itemsProperty(), e -> updateGridViewItems());
        registerChangeListener(control.cellFactoryProperty(), e ->  getFlow().recreateCells());
        registerChangeListener(control.parentProperty(), e -> {
            if (getSkinnable().getParent() != null && getSkinnable().isVisible()) {
                getSkinnable().requestLayout();
            }
        });
        registerChangeListener(control.cellHeightProperty(), e -> getFlow().recreateCells());
        registerChangeListener(control.cellWidthProperty(), e -> {
            updateItemCount();
            getFlow().recreateCells();
        });
        registerChangeListener(control.horizontalCellSpacingProperty(), e -> {
            updateItemCount();
            getFlow().recreateCells();
        });
        registerChangeListener(control.verticalCellSpacingProperty(), e -> getFlow().recreateCells());
        registerChangeListener(control.widthProperty(), e ->  updateItemCount());
        registerChangeListener(control.heightProperty(), e ->  updateItemCount());
    }

    @Override
    protected VirtualFlow<GridRow<T>> createVirtualFlow() {
        return new GridVirtualFlow();
    }

    public void updateGridViewItems() {
        if (getSkinnable().getItems() != null) {
            getSkinnable().getItems().removeListener(weakGridViewItemsListener);
        }

        if (getSkinnable().getItems() != null) {
            getSkinnable().getItems().addListener(weakGridViewItemsListener);
        }

        updateItemCount();
        getFlow().recreateCells();
        getSkinnable().requestLayout();
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        double x1 = getSkinnable().getInsets().getLeft();
        double y1 = getSkinnable().getInsets().getTop();
        double w1 = getSkinnable().getWidth() - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight());
        double h1 = getSkinnable().getHeight() - (getSkinnable().getInsets().getTop() + getSkinnable().getInsets().getBottom());

        flow.resizeRelocate(x1, y1, w1, h1);
    }

    /**
     *  Returns the number of row needed to display the whole set of cells
     *  @return GridView row count
     */
    @Override public int getItemCount() {
        final ObservableList<?> items = getSkinnable().getItems();
        // Fix for #98 : int division should be cast to get the result as
        // double and ceiled to get the max int of it (as we are looking for
        // the max number of necessary row)
        return items == null ? 0 : (int)Math.ceil((double)items.size() / computeMaxCellsInRow());
    }

    @Override
    protected void updateItemCount() {
        if (flow == null)
            return;

        int oldCount = flow.getCellCount();
        int newCount = getItemCount();

        if (newCount != oldCount) {
            flow.setCellCount(newCount);
            getFlow().rebuildCells();
        } else {
            getFlow().reconfigureCells();
        }
        updateRows(newCount);
        getSkinnable().requestLayout();
    }

    /**
     *  Returns the max number of cell per row
     *  @return Max cell number per row 
     */
    public int computeMaxCellsInRow() {
        return Math.max((int) Math.floor(computeRowWidth() / computeCellWidth()), 1);
    }

    /**
     *  Returns the width of a row
     *  (should be GridView.width - GridView.Scrollbar.width)
     *  @return Computed width of a row 
     */
    protected double computeRowWidth() {
        // Fix for #98 : width calculation should take the scrollbar size
        // into account
        
        // TODO: need to figure out how to get the real scrollbar width and
        // replace the 18 value
        return getSkinnable().getWidth() - 18;
    }

    /**
     *  Returns the width of a cell
     *  @return Computed width of a cell 
     */
    protected double computeCellWidth() {
        return getSkinnable().cellWidthProperty().doubleValue() + (getSkinnable().horizontalCellSpacingProperty().doubleValue() * 2);
    }

    protected void updateRows(int rowCount) {
        for (int i = 0; i < rowCount; i++) {
            GridRow<T> row = flow.getVisibleCell(i);
            if (row != null) {
                // We do not have to force a change of the index by setting the index to -1
                // before setting it to its actual value. GridRow will update its cells every
                // time updateIndex is called even if the index did not change.
                row.updateIndex(i);
            }
        }
    }

    @Override protected double computeMinHeight(double height, double topInset, double rightInset, double bottomInset,
            double leftInset) {
        return 0;
    }

    private GridRow<T> createCell() {
        GridRow<T> row = new GridRow<>();
        row.updateGridView(getSkinnable());
        return row;
    }

    private GridVirtualFlow getFlow() {
        return (GridVirtualFlow) getVirtualFlow();
    }

    /**
     * Custom VirtualFlow to grant access to protected methods.
     */
    private class GridVirtualFlow extends VirtualFlow<GridRow<T>> {

        public void recreateCells(){
            super.recreateCells();
        }

        public void rebuildCells(){
            super.rebuildCells();
        }

        public void reconfigureCells(){
            super.reconfigureCells();
        }
    }
}
