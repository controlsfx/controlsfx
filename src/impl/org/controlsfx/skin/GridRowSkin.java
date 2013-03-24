package impl.org.controlsfx.skin;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridRow;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import com.sun.javafx.scene.control.skin.CellSkinBase;

public class GridRowSkin<T> extends CellSkinBase<GridRow<T>, CellBehaviorBase<GridRow<T>>> {

    public GridRowSkin(GridRow<T> control) {
        super(control, new CellBehaviorBase<GridRow<T>>(control));

        updateCells();
        
        registerChangeListener(getSkinnable().indexProperty(), "INDEX");
        registerChangeListener(getSkinnable().widthProperty(), "WIDTH");
        registerChangeListener(getSkinnable().heightProperty(), "HEIGHT");
    }
    
    @Override protected void handleControlPropertyChanged(String p) {
        super.handleControlPropertyChanged(p);
        
        if ("INDEX".equals(p)) {
            updateCells();
        } else if ("WIDTH".equals(p)) {
            updateCells();
        } else if ("HEIGHT".equals(p)) {
            updateCells();
        }
    }

    public void updateCells() {
        getChildren().clear();

        int startCellIndex = getSkinnable().indexProperty().get() * computeMaxCellsInRow();
        int endCellIndex = startCellIndex + computeMaxCellsInRow() - 1;

        if (getSkinnable().indexProperty().get() >= 0) {
            for (int cellIndex = startCellIndex; cellIndex <= endCellIndex; cellIndex++) {
                if (cellIndex < getSkinnable().gridViewProperty().get().getItems().size()) {
                    T item = getSkinnable().gridViewProperty().get().getItems().get(cellIndex);
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
        if (getSkinnable().gridViewProperty().get().getCellFactory() != null) {
            cell = getSkinnable().gridViewProperty().get().getCellFactory().call(getSkinnable().gridViewProperty().get());
        } else {
            cell = createDefaultCellImpl();
        }
        return cell;
    }

    private GridCell<T> createDefaultCellImpl() {
        return new GridCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if(empty) {
                    setText("");
                } else {
                    setText(item.toString());
                }
            }
        };
    }

    @Override protected double computeMinHeight(double width) {
        return computePrefHeight(width);
    }

    @Override protected double computeMaxHeight(double width) {
        return Double.MAX_VALUE;
    }

    @Override protected double computePrefHeight(double width) {
        return getSkinnable().gridViewProperty().get().cellHeightProperty().doubleValue() + getSkinnable().gridViewProperty().get().verticalCellSpacingProperty().doubleValue() + getSkinnable().gridViewProperty().get().verticalCellSpacingProperty().doubleValue();
    }

    public int computeMaxCellsInRow() {
        return computeMaxCellsInRow(getSkinnable().getWidth());
    }

    protected int computeMaxCellsInRow(double width) {
        return Math.max((int) Math.floor(width / computeCellWidth()), 1);
    }

    protected double computeCellWidth() {
        return getSkinnable().gridViewProperty().get().cellWidthProperty().doubleValue() + getSkinnable().gridViewProperty().get().horizontalCellSpacingProperty().doubleValue() + getSkinnable().gridViewProperty().get().horizontalCellSpacingProperty().doubleValue();
    }

    @Override protected void layoutChildren(double x, double y, double w, double h) {
        double currentWidth = getSkinnable().getWidth();
        double cellWidth = getSkinnable().gridViewProperty().get().getCellWidth();
        double cellHeight = getSkinnable().gridViewProperty().get().getCellHeight();
        double horizontalCellSpacing = getSkinnable().gridViewProperty().get().getHorizontalCellSpacing();
        double verticalCellSpacing = getSkinnable().gridViewProperty().get().getVerticalCellSpacing();

        double xPos = 0;
        double yPos = 0;

        HPos currentHorizontalAlignment = getSkinnable().gridViewProperty().get().getHorizontalAlignment();
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
