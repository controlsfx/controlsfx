package impl.org.controlsfx.skin;

import impl.org.controlsfx.behavior.GridViewBehavior;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.util.Callback;

import org.controlsfx.control.GridRow;
import org.controlsfx.control.GridView;

import com.sun.javafx.scene.control.skin.VirtualContainerBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;

public class GridViewSkin<T> extends VirtualContainerBase<GridView<T>, GridViewBehavior<T>, GridRow<T>> {

    private final ListChangeListener<T> gridViewItemsListener = new ListChangeListener<T>() {
        @Override public void onChanged(ListChangeListener.Change<? extends T> change) {
            updateRowCount();

            // TODO: only removed the changed once
            cellCache.clear();

            getSkinnable().requestLayout();
        }
    };

    private final WeakListChangeListener<T> weakGridViewItemsListener = new WeakListChangeListener<T>(gridViewItemsListener);

    private GridCellCache<T> cellCache;

    public GridViewSkin(GridView<T> control) {
        super(control, new GridViewBehavior<>(control));
        
        cellCache = new GridCellCache<T>(this, flow);

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
        cellCache.clear();

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
        updateVisibleRows();
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        double x1 = getSkinnable().getInsets().getLeft();
        double y1 = getSkinnable().getInsets().getTop();
        double w1 = getSkinnable().getWidth() - (getSkinnable().getInsets().getLeft() + getSkinnable().getInsets().getRight());
        double h1 = getSkinnable().getHeight() - (getSkinnable().getInsets().getTop() + getSkinnable().getInsets().getBottom());

        flow.resizeRelocate(x1, y1, w1, h1);
    }

    @Override public GridRow<T> createCell() {
        GridRow<T> row = new GridRow<>(cellCache);
        row.updateGridView(getSkinnable());
        return row;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.javafx.scene.control.skin.VirtualContainerBase#getItemCount() Returns the row
     * count
     */
    @Override public int getItemCount() {
        return getSkinnable().getItems() == null ? 0 : (getSkinnable().getItems().size() / computeMaxCellsInRow(getSkinnable().getWidth()));
    }

    public int computeMaxCellsInRow(double width) {
        return Math.max((int) Math.floor(width / computeCellWidth()), 1);
    }

    protected double computeCellWidth() {
        return getSkinnable().cellWidthProperty().doubleValue() + getSkinnable().horizontalCellSpacingProperty().doubleValue() + getSkinnable().horizontalCellSpacingProperty().doubleValue();
    }

    protected void updateRows(int rowStartIndex, int rowEndIndex) {
        for (int currentRowIndex = rowStartIndex; currentRowIndex <= rowEndIndex; currentRowIndex++) {
            // Generics in VirtualFlow?? <- Ask Jonathan / compare with JDK8
            GridRow<T> row = (GridRow<T>) flow.getVisibleCell(currentRowIndex);
            if (row != null) {
                // FIXME hacky - need to better understand what this is about
                int index = row.getIndex();
                row.updateIndex(-1);
                row.updateIndex(index);
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

    public void updateRow(int rowIndex) {
        if (!areRowsVisible())
            return;

        int rowStartIndex = flow.getFirstVisibleCell().indexProperty().get();
        int rowEndIndex = flow.getLastVisibleCell().indexProperty().get();

        updateRows(rowStartIndex, rowEndIndex);
    }

    public void updateFromRow(int rowIndex) {
        if (!areRowsVisible())
            return;

        int rowStartIndex = Math.max(flow.getFirstVisibleCell().indexProperty().get(), rowIndex);
        int rowEndIndex = flow.getLastVisibleCell().indexProperty().get();

        updateRows(rowStartIndex, rowEndIndex);
    }

    public void updateVisibleRows() {
        if (!areRowsVisible())
            return;

        int rowStartIndex = flow.getFirstVisibleCell().indexProperty().get();
        int rowEndIndex = flow.getLastVisibleCell().indexProperty().get();

        updateRows(rowStartIndex, rowEndIndex);
    }
}
