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

import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import org.controlsfx.control.tableview2.TableView2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

final class TableView2VirtualFlow<T extends IndexedCell<?>> extends VirtualFlow<T> {
    
    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final TableView2<?> tableView;
    private final TableView2Skin<?> skin;
    /**
     * Store the fixedRow in order to place them at the top when necessary.
     * That is to say, when the VirtualFlow has not already placed one.
     */
    private final ArrayList<T> myFixedCells = new ArrayList<>();
    public final List<Node> sheetChildren;
    
    private boolean adjusting;
    private final DoubleProperty adjustedPixels = new SimpleDoubleProperty() {
        @Override
        protected void invalidated() {
            if (! adjusting && get() > 0 && myFixedCells.size() < tableView.getFixedRows().size()) {
                /**
                * Force layout pass in the inner (outer) tableView, to avoid flickering 
                * under a fast initial scrolling in the outer (inner) tableView, 
                */
                layoutChildren();
            }
            adjusting = false;
        }
        
    };
    
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public TableView2VirtualFlow(TableView2Skin<?> skin) {
        super();
        this.skin = skin;
        this.tableView = (TableView2<?>) skin.getSkinnable();
        final ChangeListener<Number> listenerY = (ov, t, t1) -> layoutTotal();
        getVbar().valueProperty().addListener(listenerY);
        getHbar().valueProperty().addListener(hBarValueChangeListener);
        widthProperty().addListener(hBarValueChangeListener);
        
        sheetChildren = findSheetChildren();
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    public void init() {
        /**
         * The idea is to work-around
         * https://bugs.openjdk.java.net/browse/JDK-8090224 in order to have the
         * same behavior between the vertical scrollBar and the horizontal
         * scrollBar.
         */
        getHbar().maxProperty().addListener((observable, oldValue, newValue) -> {
            //We want to go page by page.
            getHbar().setBlockIncrement(getWidth());
            getHbar().setUnitIncrement(newValue.doubleValue()/20);
        });
       
        tableView.getFixedRows().addListener((Observable observable) -> {
            List<T> toRemove = new ArrayList<>();
            for (T cell : myFixedCells) {
                if (!tableView.getFixedRows().contains(cell.getIndex())) {
                    cell.setManaged(false);
                    cell.setVisible(false);
                    toRemove.add(cell);
                }
            }
            myFixedCells.removeAll(toRemove);
        });
    }

    /** {@inheritDoc} */
    @Override public void scrollTo(int index) {
        //If we have some fixedRows, we check if the selected row is not below them
        if (! getCells().isEmpty() && ! tableView.getFixedRows().isEmpty()) {
            double offset = skin.getFixedRowHeight();

            while (offset >= 0 && index > 0) {
                index--;
                offset -= skin.getRowHeight(index);
            }
        }
        super.scrollTo(index);

        layoutTotal();
        layoutFixedRows();
    }

    /** {@inheritDoc} */
    @Override public double scrollPixels(final double delta) {
        final double returnValue = super.scrollPixels(delta);
        adjusting = true;
        this.adjustedPixels.set(delta);
        
        layoutTotal();
        layoutFixedRows();

        return returnValue;
    }
    
    List<T> getFixedCells() {
        return myFixedCells;
    }

    // clears the list of fixed cells. On the next layout pass the list is built 
    // again
    void rebuildFixedCells() {
        myFixedCells.clear();
    }
    
    // double property with delta pixels adjusted while scrolling 
    DoubleProperty adjustedPixelsProperty() {
        return adjustedPixels;
    }
    
    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    /** {@inheritDoc} */
    @Override protected void layoutChildren() {
        /**
         * In fact, we must do a layout even when editing, because if the user
         * resize the window during edition, if we block layout, the view will
         * be in a wrong state.
         */
        if (tableView != null) {
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
       
        for (T cell : getCells()) {
            if (cell == null) {
                continue;
            }
            cell.getProperties().put("fixed", false);
            if (cell.getIndex() >= 0 && 
                    (!skin.hBarValue.get(cell.getIndex()) || skin.rowToLayout.get(cell.getIndex()))) {
                if (skin.rowToLayout.get(cell.getIndex())) {
                    cell.getProperties().put("fixed", true);
                }
                cell.requestLayout();
            }
        }
    }
    
    private <S> void removeDeportedCells() {
        /**
         * When we layout, we also remove the cell that have been deported into
         * other rows in order not to have some TableCell hanging out.
         *
         * When scrolling with mouse wheel, we will request the layout of all
         * rows, but only one row will be really called. Thus by wiping entirely
         * the deportedCell, all cells in fixedColumns are gone. So we must be
         * smarter.
         */
        ArrayList<TableRow2<S>> rowToRemove = new ArrayList<>();
        for (Object o : skin.deportedCells.entrySet()) {
            Entry<TableRow2<S>, Set<TableCell<S, ?>>> entry = (Entry<TableRow2<S>, Set<TableCell<S, ?>>>) o;
            ArrayList<TableCell<S, ?>> toRemove = new ArrayList<>();
            for (TableCell<S, ?> cell : entry.getValue()) {
                //If we're not editing and the TableRow of the cell is not contained anymore, we remove.
                if (!cell.isEditing() && !getCells().contains((T) cell.getTableRow())) {
                    entry.getKey().removeCell(cell);
                    toRemove.add((TableCell<S, ?>) cell);
                }
            }
            entry.getValue().removeAll(toRemove);
            if (entry.getValue().isEmpty()) {
                rowToRemove.add(entry.getKey());
            }
        }
        for (TableRow2<S> row : rowToRemove) {
            skin.deportedCells.remove(row);
        }
    }

    protected ScrollBar getVerticalBar() {
        return getVbar();
    }
    protected ScrollBar getHorizontalBar() {
        return getHbar();
    }

    /** {@inheritDoc} */
    @Override protected List<T> getCells() {
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
     * Layout the fixed rows to position them correctly
     */
    private void layoutFixedRows() {
		
        //We must have a cell in ViewPort because otherwise
        //we short-circuit the VirtualFlow.
        if (! tableView.getFixedRows().isEmpty() && tableView.isRowFixingEnabled() && getFirstVisibleCellWithinViewport() != null) {
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
            for (int i = tableView.getFixedRows().size() - 1; i >= 0; i--) {
                fixedRowIndex = tableView.getFixedRows().get(i);
                T lastCell = getLastVisibleCellWithinViewport();
                //If the fixed row is out of bounds
                if (lastCell != null && fixedRowIndex > lastCell.getIndex()) {
                    if (row != null) {
                        row.setVisible(false);
                        row.setManaged(false);
                        row.getProperties().put("fixed", false);
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
                            row.getProperties().put("fixed", false);
                            sheetChildren.remove(row);
                        }
                        
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
                    row = getCellFactory().call(this);
                    row.getProperties().put("newcell", null); //$NON-NLS-1$
                	 
                    setCellIndex(row, fixedRowIndex);
                    resizeCell(row);
                    myFixedCells.add(row);
                }
                
                if (! sheetChildren.contains(row)) {
                    sheetChildren.add(row);
                }
               
                row.setManaged(true);
                row.setVisible(true);
                row.getProperties().put("fixed", true);
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
        final List<T> temp = getCells();
        final List<T> tset = new ArrayList<>(temp);
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
        Collections.sort(tset, (i1, i2) -> i2.getIndex() - i1.getIndex());
        for (final T r : tset) {
            r.toFront();
        }
    }
    
    private final ChangeListener<Number> hBarValueChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
            skin.hBarValue.clear();
        }
    };
}

