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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.scene.Node;

import org.controlsfx.control.GridCell;
import org.controlsfx.control.GridView;

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
        // TODO we can cache the cells locally like in TableRowSkinBase
        getChildren().clear();

        int startCellIndex = getSkinnable().indexProperty().get() * computeMaxCellsInRow();
        int endCellIndex = startCellIndex + computeMaxCellsInRow() - 1;

        if (getSkinnable().indexProperty().get() >= 0) {
            for (int cellIndex = startCellIndex; cellIndex <= endCellIndex; cellIndex++) {
                if (cellIndex < getSkinnable().gridViewProperty().get().getItems().size()) {
                    GridCell<T> cell = createCell();
//                    cell.setGridRow(getSkinnable());
                    cell.updateGridView(getSkinnable().getGridView());
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
    
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        GridView<T> gv = getSkinnable().gridViewProperty().get();
        return gv.getCellHeight() + gv.getVerticalCellSpacing() * 2;
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

        // This has been commented out as I removed the API from GridView until
        // a use case was created.
//        HPos currentHorizontalAlignment = getSkinnable().gridViewProperty().get().getHorizontalAlignment();
//        if (currentHorizontalAlignment != null) {
//            if (currentHorizontalAlignment.equals(HPos.CENTER)) {
//                xPos = (currentWidth % computeCellWidth()) / 2;
//            } else if (currentHorizontalAlignment.equals(HPos.RIGHT)) {
//                xPos = currentWidth % computeCellWidth();
//            }
//        }

        for (Node child : getChildren()) {
            child.relocate(xPos + horizontalCellSpacing, yPos + verticalCellSpacing);
            child.resize(cellWidth, cellHeight);
            xPos = xPos + horizontalCellSpacing + cellWidth + horizontalCellSpacing;
        }
    }
}
