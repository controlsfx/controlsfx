/**
 * Copyright (c) 2013, 2026, ControlsFX
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

import impl.org.controlsfx.control.GridViewSelectionModel;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import org.controlsfx.control.GridView;
import org.controlsfx.control.GridCell;

public class GridViewSkin<T> extends VirtualContainerBase<GridView<T>, GridRow<T>> {

    private VirtualFlow<GridRow<T>> flow;
    private ObservableList<T> observedItems;
    private int anchor = -1;
    private final EventHandler<MouseEvent> mouseHandler = this::mousePressed;
    private final EventHandler<KeyEvent> keyHandler = this::keyPressed;

    private final ListChangeListener<T> gridViewItemsListener = change -> {
        while (change.next()) anchor = GridViewSelectionModel.mapIndex(anchor, change);
        updateItemCount();
        getSkinnable().requestLayout();
    };

    private final WeakListChangeListener<T> weakGridViewItemsListener = new WeakListChangeListener<>(gridViewItemsListener);

    @SuppressWarnings("rawtypes")
    public GridViewSkin(GridView<T> control) {
        super(control);

        flow = getVirtualFlow();
        flow.setId("virtual-flow"); //$NON-NLS-1$
        flow.setPannable(false);
        flow.setVertical(true);
        flow.setFocusTraversable(false);
        flow.setCellFactory(param -> createCell());
        getChildren().add(flow);

        updateGridViewItems();
        updateItemCount();

        // A skin can be constructed without being installed. Only the active
        // skin should handle input (not another candidate created during CSS).
        registerChangeListener(control.skinProperty(), e -> updateInputHandlers());
        registerChangeListener(control.selectionModelProperty(), e -> anchor = -1);

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
        if (observedItems != null) observedItems.removeListener(weakGridViewItemsListener);
        observedItems = getSkinnable().getItems();
        if (observedItems != null) observedItems.addListener(weakGridViewItemsListener);
        anchor = -1;

        updateItemCount();
        getFlow().recreateCells();
        getSkinnable().requestLayout();
    }

    private void updateInputHandlers() {
        GridView<T> view = getSkinnable();
        view.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        view.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        if (view.getSkin() == this) {
            view.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
            view.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        }
    }

    private void mousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY && event.getButton() != MouseButton.SECONDARY) return;
        Node target = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
        while (target != null && !(target instanceof GridCell) && target != getSkinnable()) {
            target = target.getParent();
        }
        if (!(target instanceof GridCell)) return;
        GridCell<?> cell = (GridCell<?>) target;
        if (cell.getGridView() != getSkinnable() || cell.isEmpty()) return;
        MultipleSelectionModel<T> selection = getSkinnable().getSelectionModel();
        if (selection == null) return;
        int index = cell.getIndex();
        getSkinnable().requestFocus();
        if (event.getButton() == MouseButton.SECONDARY) {
            if (!selection.isSelected(index)) selection.clearAndSelect(index);
            anchor = index;
        } else {
            select(index, event.isShiftDown(), event.isShortcutDown(), true);
        }
        focus(index);
    }

    private void focus(int index) {
        FocusModel<T> model = getSkinnable().getFocusModel();
        if (model != null) model.focus(index);
    }

    private void select(int index, boolean shift, boolean shortcut, boolean toggle) {
        MultipleSelectionModel<T> model = getSkinnable().getSelectionModel();
        if (model == null) return;
        if (shift && model.getSelectionMode() == SelectionMode.MULTIPLE) {
            if (anchor < 0) {
                FocusModel<T> focus = getSkinnable().getFocusModel();
                anchor = focus == null || focus.getFocusedIndex() < 0 ? index : focus.getFocusedIndex();
            }
            if (!shortcut) model.clearSelection();
            model.selectRange(anchor, index >= anchor ? index + 1 : index - 1);
        } else if (shortcut && toggle && model.getSelectionMode() == SelectionMode.MULTIPLE) {
            if (model.isSelected(index)) model.clearSelection(index);
            else model.select(index);
            anchor = index;
        } else if (!shortcut) {
            model.clearAndSelect(index);
            anchor = index;
        }
    }

    private void keyPressed(KeyEvent event) {
        GridView<T> view = getSkinnable();
        if (event.getTarget() != view || view.getItems() == null || view.getItems().isEmpty()) return;
        MultipleSelectionModel<T> selection = view.getSelectionModel();
        FocusModel<T> focus = view.getFocusModel();
        if (selection == null || focus == null || event.isAltDown()) return;
        int count = view.getItems().size();
        int current = focus.getFocusedIndex();
        int index = current;
        int columns = computeMaxCellsInRow();
        boolean rtl = view.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        switch (event.getCode()) {
            case LEFT: index = current < 0 ? 0 : current + (rtl ? 1 : -1); break;
            case RIGHT: index = current < 0 ? 0 : current + (rtl ? -1 : 1); break;
            case UP: index = current < 0 ? 0 : current - columns; break;
            case DOWN: index = current < 0 ? 0 : current + columns; break;
            case HOME: index = 0; break;
            case END: index = count - 1; break;
            case SPACE:
                if (current >= 0) select(current, event.isShiftDown(), event.isShortcutDown(), true);
                event.consume();
                return;
            case A:
                if (!event.isShortcutDown()) return;
                selection.selectAll();
                event.consume();
                return;
            default: return;
        }
        index = Math.max(0, Math.min(count - 1, index));
        select(index, event.isShiftDown(), event.isShortcutDown(), false);
        focus(index);
        flow.scrollTo(index / columns);
        event.consume();
    }

    @Override public void dispose() {
        GridView<T> view = getSkinnable();
        view.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        view.removeEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        if (observedItems != null) observedItems.removeListener(weakGridViewItemsListener);
        super.dispose();
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
