package impl.org.controlsfx.skin;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridRow;
import org.controlsfx.control.cell.DefaultGridCell;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class GridRowSkin<T> extends CellSkinBase<GridRow<T>, CellBehaviorBase<GridRow<T>>> {

    public GridRowSkin(GridRow<T> control) {
        super(control, new CellBehaviorBase<GridRow<T>>(control));

        getSkinnable().dirtyProperty().addListener(new ChangeListener<Boolean>() {

            @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) {
                if (newValue != null && newValue.booleanValue()) {
                    updateCells();
                    getSkinnable().dirtyProperty().set(false);
                }
            }
        });

        if (getSkinnable().dirtyProperty().get()) {
            updateCells();
            getSkinnable().dirtyProperty().set(false);
        }

        getSkinnable().widthProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                updateCells();
            }
        });

        getSkinnable().heightProperty().addListener(new ChangeListener<Number>() {

            @Override public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
                updateCells();
            }
        });

        updateCells();
    }

    public void updateCells() {
        getChildren().clear();

        int startCellIndex = getSkinnable().indexProperty().get() * computeMaxCellsInRow();
        int endCellIndex = startCellIndex + computeMaxCellsInRow() - 1;

        if (getSkinnable().indexProperty().get() >= 0) {
            for (int cellIndex = startCellIndex; cellIndex <= endCellIndex; cellIndex++) {
                if (cellIndex < getSkinnable().gridView().get().getItems().size()) {
                    T item = getSkinnable().gridView().get().getItems().get(cellIndex);
                    GridCell<T> cell = getSkinnable().getCellCache().getCellIfCached(cellIndex);
                    if (cell == null) {
                        cell = createCell();
                        if (cell.isCacheable()) {
                            getSkinnable().getCellCache().addCellToCache(cellIndex, cell);
                        }
                    }
                    cell.setItem(item);
                    cell.updateIndex(cellIndex);
                    getChildren().add(cell);
                }
            }
        }

        getSkinnable().requestLayout();
    }

    private GridCell<T> createCell() {
        GridCell<T> cell;
        if (getSkinnable().gridView().get().getCellFactory() != null) {
            cell = getSkinnable().gridView().get().getCellFactory().call(getSkinnable().gridView().get());
        } else {
            cell = createDefaultCellImpl();
        }
        return cell;
    }

    private GridCell<T> createDefaultCellImpl() {
        return new DefaultGridCell<>();
    }

    @Override protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override protected double computePrefHeight(double width) {
        return getSkinnable().gridView().get().cellHeightProperty().doubleValue() + getSkinnable().gridView().get().verticalCellSpacingProperty().doubleValue() + getSkinnable().gridView().get().verticalCellSpacingProperty().doubleValue();
    }

    public int computeMaxCellsInRow() {
        return computeMaxCellsInRow(getSkinnable().getWidth());
    }

    protected int computeMaxCellsInRow(double width) {
        return Math.max((int) Math.floor(width / computeCellWidth()), 1);
    }

    protected double computeCellWidth() {
        return getSkinnable().gridView().get().cellWidthProperty().doubleValue() + getSkinnable().gridView().get().horizontalCellSpacingProperty().doubleValue() + getSkinnable().gridView().get().horizontalCellSpacingProperty().doubleValue();
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        double currentWidth = getSkinnable().getWidth();
        double cellWidth = getSkinnable().gridView().get().getCellWidth();
        double cellHeight = getSkinnable().gridView().get().getCellHeight();
        double horizontalCellSpacing = getSkinnable().gridView().get().getHorizontalCellSpacing();
        double verticalCellSpacing = getSkinnable().gridView().get().getVerticalCellSpacing();

        double xPos = 0;
        double yPos = 0;

        HPos currentHorizontalAlignment = getSkinnable().gridView().get().getHorizontalAlignment();
        if (currentHorizontalAlignment != null) {
            if (currentHorizontalAlignment.equals(HPos.CENTER)) {
                xPos = (currentWidth % computeCellWidth()) / 2;
            } else if (currentHorizontalAlignment.equals(HPos.RIGHT)) {
                xPos = currentWidth % computeCellWidth();
            }
        }

        for (Node child : getChildren()) {
            child.relocate(xPos + horizontalCellSpacing, yPos + verticalCellSpacing);
            child.resize(cellWidth, cellHeight);
            xPos = xPos + horizontalCellSpacing + cellWidth + horizontalCellSpacing;
        }
    }
}
