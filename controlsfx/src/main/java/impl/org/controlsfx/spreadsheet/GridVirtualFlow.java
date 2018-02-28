/**
 * Copyright (c) 2013, 2016 ControlsFX
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
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javafx.beans.Observable;
import javafx.beans.binding.When;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

final class GridVirtualFlow<T extends IndexedCell<?>> extends VirtualFlow<T> {
    
    /**
     * With that comparator we can lay out our rows in the reverse order. That
     * is to say from the bottom to the very top. In that manner we are sure
     * that our spanning cells will COVER the cell below so we don't have any
     * problems with missing hovering, the editor jammed etc.
     * <br/>
     *
     * The only problem is for the fixed column but the {@link #getTopRow(int) }
     * now returns the very first row and allow us to put some privileged
     * TableCell in it if they feel the need to be on top in term of z-order.
     *
     * FIXME The best would be to put a TreeList of something like that in order
     * not to sort the rows everytime, need investigation..
     */
    private static final Comparator<GridRow> ROWCMP = new Comparator<GridRow>() {
        @Override
        public int compare(GridRow firstRow, GridRow secondRow) {
            //o1.getIndex() < o2.getIndex() ? -1 : +1;
            return secondRow.getIndex() - firstRow.getIndex();
        }
    };

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private SpreadsheetView spreadSheetView;
    private final GridViewSkin gridViewSkin;
    /**
     * Store the fixedRow in order to place them at the top when necessary.
     * That is to say, when the VirtualFlow has not already placed one.
     */
    private final ArrayList<T> myFixedCells = new ArrayList<>();
    public final List<Node> sheetChildren;
    private StackPane corner;
    private Scale scale;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public GridVirtualFlow(GridViewSkin gridViewSkin) {
        super();
        this.gridViewSkin = gridViewSkin;
        final ChangeListener<Number> listenerY = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                layoutTotal();
            }
        };
        getVbar().valueProperty().addListener(listenerY);
        getHbar().valueProperty().addListener(hBarValueChangeListener);
        widthProperty().addListener(hBarValueChangeListener);
        
        sheetChildren = findSheetChildren();
        findCorner();
        //When we click outside of the grid, we want to deselect all cells.
        addEventHandler(MouseEvent.MOUSE_PRESSED, (MouseEvent event) -> {
            if (event.getTarget().getClass() == GridRow.class) {
                spreadSheetView.getSelectionModel().clearSelection();
            }
        });
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    public void init(SpreadsheetView spv) {
        /**
         * The idea is to work-around
         * https://javafx-jira.kenai.com/browse/RT-36396 in order to have the
         * same behavior between the vertical scrollBar and the horizontal
         * scrollBar.
         */
        getHbar().maxProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //We want to go page by page.
                getHbar().setBlockIncrement(getWidth());
                getHbar().setUnitIncrement(newValue.doubleValue()/20);
            }
        });
        scale = new Scale(1 / spv.getZoomFactor(), 1 / spv.getZoomFactor());
        scale.setPivotX(getHbar().getWidth() / 2);
        getHbar().getTransforms().add(scale);
        getVbar().getTransforms().add(scale);
        corner.getTransforms().add(scale);

        this.spreadSheetView = spv;
        
        spreadSheetView.zoomFactorProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            scale.setX(1 / newValue.doubleValue());
            scale.setY(1 / newValue.doubleValue());
        });

       
        //We clip the rectangle selection with a rectangle, inception style.
        Rectangle rec = new Rectangle();
        rec.widthProperty().bind(widthProperty().subtract(new When(getVbar().visibleProperty()).then(getVbar().widthProperty()).otherwise(0)));
        rec.heightProperty().bind(heightProperty().subtract(new When(getHbar().visibleProperty()).then(getHbar().heightProperty()).otherwise(0)));
        gridViewSkin.rectangleSelection.setClip(rec);
        
        getChildren().add(gridViewSkin.rectangleSelection);
        
        spv.getFixedRows().addListener((Observable observable) -> {
            List<T> toRemove = new ArrayList<>();
            for (T cell : myFixedCells) {
                if (!spv.getFixedRows().contains(spreadSheetView.getFilteredSourceIndex(cell.getIndex()))) {
                    cell.setManaged(false);
                    cell.setVisible(false);
                    toRemove.add(cell);
                }
            }
            myFixedCells.removeAll(toRemove);
        });
    }

    @Override
    public void show(int index) {
        super.show(index);
        layoutTotal();
        layoutFixedRows();
    }

    @Override
    public void scrollTo(int index) {
        //If we have some fixedRows, we check if the selected row is not below them
        if (!getCells().isEmpty() && !VerticalHeader.isFixedRowEmpty(spreadSheetView)) {
            double offset = gridViewSkin.getFixedRowHeight();

            while (offset >= 0 && index > 0) {
                index--;
                offset -= gridViewSkin.getRowHeight(index);
            }
        }
        super.scrollTo(index);

        layoutTotal();
        layoutFixedRows();
                }

    @Override
    public double adjustPixels(final double delta) {
        final double returnValue = super.adjustPixels(delta);

        layoutTotal();
        layoutFixedRows();

        return returnValue;
    }
    
    List<T> getFixedCells(){
        return myFixedCells;
    }
    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    /**
     * We need to return here the very top row in term of "z-order". Because we
     * will add in this row the TableCell that are in fixedColumn and which
     * needs to be drawn on top of all others.
     *
     * @return
     */
    GridRow getTopRow() {
        if (!sheetChildren.isEmpty()) {
            /**
             * When scrolling with mouse wheel, some row are present but will
             * not be lay out. Thus we only consider the row with children as
             * really available.
             */
            try {
                int i = sheetChildren.size() - 1;
                while (i >= sheetChildren.size() || (((GridRow) sheetChildren.get(i)).getChildrenUnmodifiable().isEmpty() && i > 0)) {
                    --i;
                }
                return (GridRow) sheetChildren.get(i);
                //We may have some IndexOutOfBoundsException sometimes...
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    protected void layoutChildren() {
        /**
         * In fact, we must do a layout even when editing, because if the user
         * resize the window during edition, if we block layout, the view will
         * be in a wrong state.
         */
        if (spreadSheetView != null
                /*&& (spreadSheetView.getEditingCell() == null || spreadSheetView
                        .getEditingCell().getRow() == -1)*/) {
            sortRows();
            super.layoutChildren();
            layoutTotal();
            layoutFixedRows();
            
            /**
             * Sometimes, the visible amount is not computed when we have few
             * big rows. If we detect that case, we must compute it manually
             * otherwise the Vbar is wrongly set.
             */
            if (getVbar().getVisibleAmount() == 0.0
                    && getVbar().isVisible()
                    && getCells().size() != getCellCount()) {
                getVbar().setMax(1);
                getVbar().setVisibleAmount(getCells().size() / (float) getCellCount());
            }
        }
        /**
         * If we have modify the Scale, the scrollBars will be smaller or
         * bigger. But we want to have them the same size as before, so we
         * reverse the effect of the scale applied, and place the bar to the
         * proper space.
         */
        Pos pos = Pos.TOP_LEFT;
        double width = getWidth();
        double height = getHeight();
        double top = getInsets().getTop();
        double right = getInsets().getRight();
        double left = getInsets().getLeft();
        double bottom = getInsets().getBottom();
        double scaleX = scale.getX();
        double shift = 1 - scaleX;
        double contentWidth = (width / scaleX) - left - right - getVbar().getWidth();
        double contentHeight = (height / scaleX) - top - bottom - getHbar().getHeight();

        //HBAR
        /**
         * Magic numbers coming out of nowhere but I don't understand why
         * the bar are shifting away when zooming...
         */
        layoutInArea(getHbar(), 0 - shift * 10,
                height - (getHbar().getHeight() * scaleX),
                contentWidth, contentHeight,
                0, null,
                pos.getHpos(),
                pos.getVpos());
        //VBAR
        layoutInArea(getVbar(), width - getVbar().getWidth() + shift,
                0,
                contentWidth, contentHeight,
                0, null,
                pos.getHpos(),
                pos.getVpos());

        //CORNER
        if (corner != null) {
            layoutInArea(corner, width - getVbar().getWidth() + shift,
                    getHeight() - (getHbar().getHeight() * scaleX),
                    corner.getWidth(), corner.getHeight(),
                    0, null,
                    pos.getHpos(),
                    pos.getVpos());
        }
    }

    /**
     * Layout all the visible rows
     */
    protected void layoutTotal() {
        sortRows();
        removeDeportedCells();

        // When scrolling fast with fixed Rows, cells is empty and not recreated..
        if (getCells().isEmpty()) {
            reconfigureCells();
        } 
       
        for (GridRow cell : (List<GridRow>)getCells()) {
            if (cell != null && cell.getIndex() >= 0 && (!gridViewSkin.hBarValue.get(cell.getIndex()) || gridViewSkin.rowToLayout.get(cell.getIndex()))) {
                cell.requestLayout();
            }
        }
    }
    
    private void removeDeportedCells() {
        /**
         * When we layout, we also remove the cell that have been deported into
         * other rows in order not to have some TableCell hanging out.
         *
         * When scrolling with mouse wheel, we will request the layout of all
         * rows, but only one row will be really called. Thus by wiping entirely
         * the deportedCell, all cells in fixedColumns are gone. So we must be
         * smarter.
         */
        ArrayList<GridRow> rowToRemove = new ArrayList<>();
        for (Entry<GridRow, Set<CellView>> entry : gridViewSkin.deportedCells.entrySet()) {
            ArrayList<CellView> toRemove = new ArrayList<>();
            for (CellView cell : entry.getValue()) {
                //If we're not editing and the TableRow of the cell is not contained anymore, we remove.
                if (!cell.isEditing() && !getCells().contains(cell.getTableRow())) {
                    entry.getKey().removeCell(cell);
                    toRemove.add(cell);
                }
            }
            entry.getValue().removeAll(toRemove);
            if (entry.getValue().isEmpty()) {
                rowToRemove.add(entry.getKey());
            }
        }
        for (GridRow row : rowToRemove) {
            gridViewSkin.deportedCells.remove(row);
        }
    }

    protected ScrollBar getVerticalBar() {
        return getVbar();
    }
    protected ScrollBar getHorizontalBar() {
        return getHbar();
    }

    @Override
    protected List<T> getCells() {
        return super.getCells();
    }

    /***************************************************************************
     * * Private Methods * *
     **************************************************************************/

    /**
     * WARNING : This is bad but no other options right now. This will find the
     * sheetChildren of the VirtualFlow, aka where the cells are kept and
     * clipped. See layoutFixedRows() or getTopRow() for use.
     *
     * @return
     */
    private List<Node> findSheetChildren(){
        if(!getChildren().isEmpty()){
            if(getChildren().get(0) instanceof Region){
                Region region = (Region) getChildren().get(0);
                if(!region.getChildrenUnmodifiable().isEmpty()){
                    if(region.getChildrenUnmodifiable().get(0) instanceof Group){
                        return ((Group)region.getChildrenUnmodifiable().get(0)).getChildren();
                    }
                }
            }
        }
        return new ArrayList<>();
    }
    
    /**
     * WARNING : This is bad but no other options right now. This will find the
     * corner where the two scrollBars are joigning.
     */
    private void findCorner() {
        if (!getChildren().isEmpty()) {
            for (Node node : getChildren()) {
                if (node instanceof StackPane) {
                    corner = (StackPane) node;
                }
            }
        }
    }
    
    /**
     * Layout the fixed rows to position them correctly
     */
    private void layoutFixedRows() {

		//We must have a cell in ViewPort because otherwise
        //we short-circuit the VirtualFlow.
        if (!VerticalHeader.isFixedRowEmpty(spreadSheetView) && getFirstVisibleCellWithinViewPort() != null) {
            sortRows();
            /**
             * What I do is just going after the VirtualFlow in order to ADD
             * (not replace like before) new rows at the top.
             *
             * If the VirtualFlow has the row, then I will hide mine and let him
             * handle. But if the row is missing, then I must show mine in order
             * to have the fixed row.
             */
            T row = null;
            Integer fixedRowIndex;
            
            rows:
            for (int i = spreadSheetView.getFixedRows().size() - 1; i >= 0; i--) {
                fixedRowIndex = spreadSheetView.getFixedRows().get(i);
                if(spreadSheetView.isRowHidden(i)){
                    continue;
                }
                //Changing the index to viewRow.
                fixedRowIndex = spreadSheetView.getFilteredRow(fixedRowIndex);
                T lastCell = getLastVisibleCellWithinViewPort();
                //If the fixed row is out of bounds
                if (lastCell != null && fixedRowIndex > lastCell.getIndex()) {
                    if (row != null) {
                        row.setVisible(false);
                        row.setManaged(false);
                        sheetChildren.remove(row);
                    }
                    continue;
                }

                //We see if the row is laid out by the VirtualFlow
                for (T virtualFlowCells : getCells()) {
                    if (virtualFlowCells.getIndex() > fixedRowIndex) {
                        break;
                    } else if (virtualFlowCells.getIndex() == fixedRowIndex) {
                        row = containsRows(fixedRowIndex);
                        if (row != null) {
                            row.setVisible(false);
                            row.setManaged(false);
                            sheetChildren.remove(row);
                        }
                        /**
                         * OLD COMMENT : We must push to Front only if the row
                         * is at the very top and has a risk to be recovered.
                         * This is happening only if this row is translated.
                         *
                         * NEW COMMENT: I'm not sure about this.. Since the
                         * fixedColumn are not in the special top row, we don't
                         * care if the row is pushed to front.. need
                         * investigation
                         */
                        virtualFlowCells.toFront();
                        continue rows;
                    }
                }
                
                row = containsRows(fixedRowIndex);
                if (row == null) {
                    /**
                     * getAvailableCell is not added our cell to the ViewPort in some cases.
                     * So we need to instantiate it ourselves.
                     */
                    row = getCreateCell().call(this);
                    row.getProperties().put("newcell", null); //$NON-NLS-1$
                	 
                    setCellIndex(row, fixedRowIndex);
                    resizeCellSize(row);
                    myFixedCells.add(row);
                }
                
                /**
                 * Sometime, when we set a new Grid on a SpreadsheetView without recreating it,
                 * we can end up with some rows not being added to the ViewPort.
                 * So we must be sure it's in and add it ourself otherwise.
                 */
                if(!sheetChildren.contains(row)){
                    sheetChildren.add(row);
                }
               
                row.setManaged(true);
                row.setVisible(true);
                row.toFront();
                row.requestLayout();
                    }
                }
                }

    /**
     * Verify if the row has been added to myFixedCell
     *
     * @param i
     * @return
     */
    private T containsRows(int i) {
        for (T cell : myFixedCells) {
            if (cell.getIndex() == i) {
                return cell;
            }
        }
        return null;
    }
    /**
     * Sort the rows so that they stay in order for layout
     */
    private void sortRows() {
        final List<GridRow> temp = (List<GridRow>) getCells();
        final List<GridRow> tset = new ArrayList<>(temp);
        Collections.sort(tset, ROWCMP);
        for (final TableRow<ObservableList<SpreadsheetCell>> r : tset) {
            r.toFront();
        }
    }
    
    private final ChangeListener<Number> hBarValueChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            gridViewSkin.hBarValue.clear();
        }
    };
}

