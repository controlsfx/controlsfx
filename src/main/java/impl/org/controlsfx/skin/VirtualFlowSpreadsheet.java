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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableRow;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

final class VirtualFlowSpreadsheet<T extends IndexedCell<?>> extends VirtualFlow<T> {
    
    private static final Comparator<SpreadsheetRowImpl> ROWCMP = new Comparator<SpreadsheetRowImpl>() {
        @Override
        public int compare(SpreadsheetRowImpl o1, SpreadsheetRowImpl o2) {
            final int lhs = o1.getIndex();
            final int rhs = o2.getIndex();
            return lhs < rhs ? -1 : +1;
        }
    };

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private SpreadsheetView spreadSheetView;
    /**
     * Count the number of fixed Cell added to the viewport If we added just a
     * few cells in the addLeadingCell We need to add the remaining in the
     * addTrailingCell
     */
    private int cellFixedAdded = 0;
    private boolean cellIndexCall = false;

    /**
     * The list of Rows fixed. It only contains the number of the rows, sorted.
     */
//    private final ArrayList<Integer> fixedRows = new ArrayList<Integer>();
    // private double scrollY = 0;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public VirtualFlowSpreadsheet() {
        super();
        final ChangeListener<Number> listenerY = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                layoutTotal();
            }
        };
        getVbar().valueProperty().addListener(listenerY);

    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    public void init(SpreadsheetView spv) {
        this.spreadSheetView = spv;
    }

    @Override
    public void show(int index) {
        super.show(index);
        layoutTotal();
        layoutFixedRows();
    }

    @Override
    public void scrollTo(int index) {
        if (!getCells().isEmpty()
                && index < getCells().get(0 + getFixedRows().size()).getIndex()) {
            index -= getCells().size() - getFixedRows().size();
        }
        super.scrollTo(index);

        layoutTotal();
        layoutFixedRows();
    }

    @Override
    public double adjustPixels(final double delta) {
        cellIndexCall = true;
        final double returnValue = super.adjustPixels(delta);

        /*
         * int diff = (int) Math.ceil(Math.abs((scrollY -
         * getVbar().getValue()))*getCellCount()); scrollY =
         * getVbar().getValue(); if(diff>10){ layoutTotal(); }else{
         * layoutFirstRows(diff<2? 2:diff); }
         */
        layoutTotal();
        layoutFixedRows();

        return returnValue;
    }

    public ObservableList<Integer> getFixedRows() {
        return spreadSheetView.getFixedRows();
    }

    public T getFirstVisibleCellWithinViewPort() {
        if (getCells().isEmpty() || getHeight() <= 0) return null;

        final boolean isVertical = isVertical();
        T cell;
        for (int i = 0 + getFixedRows().size(); i < getCells().size(); i++) {
            cell = getCells().get(i);
            if (cell.isEmpty()) continue;

            if (isVertical && cell.getLayoutY() + cell.getHeight() > 0) {
                return cell;
            } else if (!isVertical && cell.getLayoutX() + cell.getWidth() > 0) { return cell; }
        }

        return null;
    }

    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    @Override
    protected void layoutChildren() {
        /*
         * int diff = (int) Math.ceil(Math.abs((scrollY -
         * getVbar().getValue()))*getCellCount()); scrollY =
         * getVbar().getValue();
         */

        // We don't want to layout everything in case we're editing because it
        // has no sense
        if (spreadSheetView != null
                && (spreadSheetView.getEditingCell() == null || spreadSheetView
                        .getEditingCell().getRow() == -1)) {
            sortRows();
            super.layoutChildren();
            layoutTotal();
            /*
             * if(diff>10){ layoutTotal(); }else{ layoutFirstRows(diff<2?
             * 2:diff); }
             */
            layoutFixedRows();
        }

    }

    protected T getAvailableCell(int prefIndex) {
        cellIndexCall = true;
        T tmp = super.getAvailableCell(prefIndex);
        return tmp;
    }

    /**
     * Layout all the visible rows
     */
    protected void layoutTotal() {
        sortRows();
        // FIXME When scrolling fast with fixed Rows, cells is empty and not
        // recreated..
        if (getCells().isEmpty()) {
            reconfigureCells();
            // recreateCells();
        }
        for (Cell<?> cell : getCells()) {
            if (cell != null) {
                cell.requestLayout();
            }
        }
    }

    protected VirtualScrollBar getVerticalBar() {
        return getVbar();
    }
    protected VirtualScrollBar getHorizontalBar() {
        return getHbar();
    }

    @Override
    protected List<T> getCells() {
        return super.getCells();
    }

    /**
     * Return the index for a given cell. This allows subclasses to customize
     * how cell indices are retrieved.
     */
    @Override
    protected int getCellIndex(T cell) {
        if (cellIndexCall) {
            cellIndexCall = false;
            return cell.getIndex();
        } else {
            return ((SpreadsheetRowImpl) cell).getIndexVirtualFlow();
        }
    }

    @Override
    protected void addLeadingCells(int currentIndex, double startOffset) {
        addLeadingCells(new Helper<T>(this), currentIndex, startOffset);
    }

    private void addLeadingCells(Helper<T> ch, int currentIndex,
            double startOffset) {
        // The offset will keep track of the distance from the top of the
        // viewport to the top of the current index. We will increment it
        // as we lay out leading cells.
        double offset = startOffset;
        // The index is the absolute index of the cell being laid out
        int index = currentIndex;
        
        // Offset should really be the bottom of the current index
        boolean first = true; // first time in, we just fudge the offset and let
        // it be the top of the current index then redefine
        // it as the bottom of the current index thereafter
        // while we have not yet laid out so many cells that they would fall
        // off the flow, we will continue to create and add cells. The
        // offset is our indication of whether we can lay out additional
        // cells. If the offset is ever < 0, except in the case of the very
        // first cell, then we must quit.
        T cell = null;

        // First pass to know how many cells we will add
        int cellToAdd = 0;
        while (index >= 0 && (offset > 0 || first)) {
            if (first) {
                first = false;
            } else {
                // Careful here because I've seen that it could mess things up a
                // bit
                // Maybe use directly "fixedCellSize" if we're sure..
                offset -= getCellLength(0);
            }
            --index;
            ++cellToAdd;
        }

        // Now that we know how many cells we will add, we reset the variable
        offset = startOffset;
        index = currentIndex;
        first = true;
        cellFixedAdded = 0;
        int numberOfCellsBelowIndex = 0;
        boolean flag = false; // We only want to compute once
        
        while (index >= 0 && (offset > 0 || first)) {
            if (index >= getCellCount()) {
                if (first) {
                    first = false;
                } else {
                    // offset -= getCellLength(0);
                }
                --index;
                --cellToAdd;
            } else {
            	if(!flag && !getFixedRows().isEmpty() && index>=getFixedRows().get(cellFixedAdded)){
            		flag = true;
            		while(numberOfCellsBelowIndex < getFixedRows().size() && index >= getFixedRows().get(numberOfCellsBelowIndex) && numberOfCellsBelowIndex < cellToAdd){
            			numberOfCellsBelowIndex++;
            		}
            		numberOfCellsBelowIndex--;
            	}
            	
                // If the number of remaining cells to add is the same of the fixedRows size
                if (!getFixedRows().isEmpty() && cellToAdd <= getFixedRows().size()) {
                	/**
                	 * We now try to determine if the current index is superior of any
                	 * index of our FixedRows. If so, we compute how many FixedRow index 
                	 * are below the current index.
                	 */
                	
                	if(flag && cellToAdd<= (numberOfCellsBelowIndex+1)){
                		
	                    final int realIndex = getFixedRows().get(numberOfCellsBelowIndex);
	                    cell = getAvailableCell(realIndex); // We grab the right one
	                    setCellIndex(cell, realIndex); // the index is the real one
	                    setCellIndexVirtualFlow(cell, index); // But the index for
	                                                          // the Virtual Flow
	                                                          // remain his (not the
	                	                                   // real one)
	                    --numberOfCellsBelowIndex;
	                    ++cellFixedAdded;
	                }else{
	                	if(numberOfCellsBelowIndex >=0 && index == getFixedRows().get(numberOfCellsBelowIndex)){
	                		--numberOfCellsBelowIndex;
		                    ++cellFixedAdded;
	                	}
	                	cell = getAvailableCell(index);
	                    setCellIndex(cell, index);
	                }
                }else {
                	if(!getFixedRows().isEmpty() && numberOfCellsBelowIndex >=0 && index == getFixedRows().get(numberOfCellsBelowIndex)){
                		--numberOfCellsBelowIndex;
	                    ++cellFixedAdded;
                	}
                    // System.out.println("JaddC"+index);
                    cell = getAvailableCell(index);
                    setCellIndex(cell, index);
                }
                resizeCellSize(cell); // resize must be after config
                ch.addFirst(cell);

                // A little gross but better than alternatives because it
                // reduces
                // the number of times we have to update a cell or compute its
                // size. The first time into this loop "offset" is actually the
                // top of the current index. On all subsequent visits, it is the
                // bottom of the current index.
                if (first) {
                    first = false;
                } else {
                    offset -= getCellLength(cell);
                }

                // Position the cell, and update the maxPrefBreadth variable as
                // we go.
                positionCell(cell, offset);
                setMaxPrefBreadth(Math.max(ch.getMaxPrefBreadth(this),
                        getCellBreadth(cell)));
                cell.setVisible(true);
                --index;
                --cellToAdd;
            }
        }

        // There are times when after laying out the cells we discover that
        // the top of the first cell which represents index 0 is below the top
        // of the viewport. In these cases, we have to adjust the cells up
        // and reset the mapper position. This might happen when items got
        // removed at the top or when the viewport size increased.
        cell = ch.getFirst();
        final int firstIndex = getCellIndex(cell);
        final double firstCellPos = getCellPosition(cell);
        if (firstIndex == 0 && firstCellPos > 0) {
            setPosition(0.0f);
            offset = 0;
            for (int i = 0; i < getCells().size(); i++) {
                cell = getCells().get(i);
                positionCell(cell, offset);
                offset += getCellLength(cell);
            }
        }
    }

    @Override
    protected boolean addTrailingCells(boolean fillEmptyCells) {
        return addTrailingCells(new Helper<T>(this), fillEmptyCells);
    }

    private boolean addTrailingCells(Helper<T> ch, boolean fillEmptyCells) {
        // If cells is empty then addLeadingCells bailed for some reason and
        // we're hosed, so just punt
        if (getCells().isEmpty()) { return false; }

        // While we have not yet laid out so many cells that they would fall
        // off the flow, so we will continue to create and add cells. When the
        // offset becomes greater than the width/height of the flow, then we
        // know we cannot add any more cells.
        final T startCell = ch.getLast();
        double offset = getCellPosition(startCell) + getCellLength(startCell);
        int index = getCellIndex(startCell) + 1;
        boolean filledWithNonEmpty = index <= getCellCount();
        
        final double viewportLength = getViewportLength();
        while (offset < viewportLength) {// && index <getCellCount()) {
            if (index >= getCellCount()) {
                if (offset < viewportLength) {
                    filledWithNonEmpty = false;
                }
                if (!fillEmptyCells) { return filledWithNonEmpty; }
            }
            T cell = null;
            /**
             * If we have a lot of rows in header, we need to add the remaining
             * in the trailingCells. I Added fillEmptyCells because it appears 
             * that when AddtrailingCells is called from adjustPixel,
             * we add several time the rows in fixedHeader...
             */
            if (!getFixedRows().isEmpty() && cellFixedAdded < getFixedRows().size()
                    && fillEmptyCells ) {
            	if(index>=getFixedRows().get(cellFixedAdded)){
	                final int realIndex = getFixedRows().get(cellFixedAdded);
	                // System.out.println("JaddD"+realIndex);
	                cell = getAvailableCell(realIndex); // We grab the right one
	                setCellIndex(cell, realIndex); // the index is the real one
	                setCellIndexVirtualFlow(cell, index); // But the index for the
	                                                      // Virtual Flow remain his
	                                                      // (not the real one)
	                ++cellFixedAdded;
            	}else{
            		cell = getAvailableCell(index);
                    setCellIndex(cell, index);
            	}
            } else {
                // System.out.println("JaddD"+index);
                cell = getAvailableCell(index);
                setCellIndex(cell, index);
            }
            resizeCellSize(cell); // resize happens after config!
            ch.addLast(cell);

            // Position the cell and update the max pref
            positionCell(cell, offset);
            setMaxPrefBreadth(Math.max(ch.getMaxPrefBreadth(this),
                    getCellBreadth(cell)));

            offset += getCellLength(cell);
            cell.setVisible(true);
            ++index;
        }

        // Discover whether the first cell coincides with index #0. If after
        // adding all the trailing cells we find that a) the first cell was
        // not index #0 and b) there are trailing cells, then we have a
        // problem. We need to shift all the cells down and add leading cells,
        // one at a time, until either the very last non-empty cells is aligned
        // with the bottom OR we have laid out cell index #0 at the first
        // position.
        T firstCell = ch.getFirst();
        index = getCellIndex(firstCell);
        final T lastNonEmptyCell = getLastVisibleCell();
        double start = getCellPosition(firstCell);
        final double end = getCellPosition(lastNonEmptyCell)
                + getCellLength(lastNonEmptyCell);
        if ((index != 0 || index == 0 && start < 0) && fillEmptyCells
                && lastNonEmptyCell != null
                && getCellIndex(lastNonEmptyCell) == getCellCount() - 1
                && end < viewportLength) {

            // Quite impossible to add properly the FixedRows so I choose to
            // rebuild the view
            if (!getFixedRows().isEmpty()) {
                final int currentIndex = (int) (getPosition() * getCellCount());
                ch.addAllToPile(this);

                // The distance from the top of the viewport to the top of the
                // cell for the current index.
                final double p = com.sun.javafx.Utils
                        .clamp(0, getPosition(), 1);
                final double fractionalPosition = p * getCellCount();
                final int cellIndex = (int) fractionalPosition;
                final double fraction = fractionalPosition - cellIndex;
                final double cellSize = getCellLength(cellIndex);
                final double pixelOffset = cellSize * fraction;
                final double viewportOffset = getViewportLength() * p;

                final double offset2 = pixelOffset - viewportOffset;// -computeViewportOffset(getPosition());
                // Add all the leading and trailing cells (the call to add
                // leading
                // cells will add the current cell as well -- that is, the one
                // that
                // represents the current position on the mapper).
                addLeadingCells(currentIndex, -offset2);
                // Force filling of space with empty cells if necessary
                addTrailingCells(true);
            } else {

                double prospectiveEnd = end;
                double distance = viewportLength - end;
                while (prospectiveEnd < viewportLength && index != 0
                        && (-start) < distance) {
                    index--;
                    T cell = getAvailableCell(index);
                    setCellIndex(cell, index);
                    resizeCellSize(cell); // resize must be after config
                    ch.addFirst(cell);
                    double cellLength = getCellLength(cell);
                    start -= cellLength;
                    prospectiveEnd += cellLength;
                    positionCell(cell, start);
                    setMaxPrefBreadth(Math.max(ch.getMaxPrefBreadth(this),
                            getCellBreadth(cell)));
                    cell.setVisible(true);
                }

                // The amount by which to translate the cells down
                firstCell = ch.getFirst();
                start = getCellPosition(firstCell);
                double delta = viewportLength - end;
                if (getCellIndex(firstCell) == 0 && delta > (-start)) {
                    delta = (-start);
                }
                // Move things
                for (int i = 0; i < ch.size(); i++) {
                    T cell = ch.get(i);
                    positionCell(cell, getCellPosition(cell) + delta);
                }

                // Check whether the first cell, subsequent to our adjustments,
                // is
                // now index #0 and aligned with the top. If so, change the
                // position
                // to be at 0 instead of 1.
                start = getCellPosition(firstCell);
                if (getCellIndex(firstCell) == 0 && start == 0) {
                    setPosition(0);
                } else if (getPosition() != 1) {
                    setPosition(1);
                }
            }

        }

        return filledWithNonEmpty;
    }

    protected void setCellIndex(T cell, int index) {
        super.setCellIndex(cell, index);
        setCellIndexVirtualFlow(cell, index);
    }

    /***************************************************************************
     * * Private Methods * *
     **************************************************************************/

    private void setCellIndexVirtualFlow(T cell, int index) {
        if (cell == null) { return; }

        ((SpreadsheetRowImpl) cell).setIndexVirtualFlow(index);
    }

    /**
     * Layout the fixed rows to position them correctly
     */
    private void layoutFixedRows() {
        sortRows();
        if (!getCells().isEmpty() && !getFixedRows().isEmpty()) {
            for (int i = getFixedRows().size() - 1; i >= 0; --i) {
                SpreadsheetRowImpl cell = (SpreadsheetRowImpl) getCells().get(i);
                if (cell != null && getFixedRows().contains(cell.getIndex())) {
                    cell.toFront();
                    cell.requestLayout();
                }
            }
        }
    }

    /**
     * Sort the rows so that they stay in order for layout
     */
    private void sortRows() {
        final List<SpreadsheetRowImpl> temp = (List<SpreadsheetRowImpl>) getCells();
        final List<SpreadsheetRowImpl> tset = new ArrayList<>(temp);
        Collections.sort(tset, ROWCMP);
        for (final TableRow<ObservableList<SpreadsheetCell<?>>> r : tset) {
            r.toFront();
        }
    }

    /**
     * Layout the first NON-FIXED row if it needs to span correctly
     */
    /*
     * private void layoutFirstRows(int number){ sortHB();
     * if(!getCells().isEmpty()) { int beginning =
     * getFirstVisibleCell().getIndex(); for(int i = beginning ;
     * i<=(number+beginning); ++i){ final SpreadsheetRow rows = (SpreadsheetRow)
     * getCell(i); if(rows != null) { System.out.println("je demande"+i);
     * rows.requestLayout(); } } } }
     */
}

/**
 * Helper class to workaround RT-31692
 */
class Helper<T extends IndexedCell<?>> {
    Object cells;
    static Field fcells;
    static Method getFirst;
    static Method getLast;
    static Method get;
    static Method addFirst;
    static Method addLast;
    static Method size;
    static Method getMaxPrefBreadth;
    static Method addAllToPile;
    static {
        try {
            Class<?> vfc = VirtualFlow.class;
            getMaxPrefBreadth = vfc.getDeclaredMethod("getMaxPrefBreadth");
            getMaxPrefBreadth.setAccessible(true);
            addAllToPile = vfc.getDeclaredMethod("addAllToPile");
            addAllToPile.setAccessible(true);
            fcells = vfc.getDeclaredField("cells");
            fcells.setAccessible(true);
            Class<?> ccells = fcells.getType();
            getFirst = ccells.getDeclaredMethod("getFirst");
            getFirst.setAccessible(true);;
            getLast = ccells.getDeclaredMethod("getLast");
            getLast.setAccessible(true);
            get = ccells.getDeclaredMethod("get", new Class<?>[]{int.class});
            get.setAccessible(true);
            addFirst = ccells.getDeclaredMethod("addFirst",
                    new Class<?>[]{Object.class});
            addFirst.setAccessible(true);
            addLast = ccells.getDeclaredMethod("addLast",
                    new Class<?>[]{Object.class});
            addLast.setAccessible(true);
            size = ccells.getDeclaredMethod("size");
            size.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException
                | NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    Helper(VirtualFlow<?> vf) {
        try {
            cells = fcells.get(vf);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            cells = null;
        }
    }

    void addAllToPile(VirtualFlow<?> vf) {
        try {
            addAllToPile.invoke(vf);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    double getMaxPrefBreadth(VirtualFlow<?> vf) {
        try {
            return (Double) getMaxPrefBreadth.invoke(vf);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    T get(int index) {
        try {
            return (T) get.invoke(cells, index);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    T getFirst() {
        try {
            return (T) getFirst.invoke(cells);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    T getLast() {
        try {
            return (T) getLast.invoke(cells);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    int size() {
        try {
            Integer isize = (Integer) size.invoke(cells);
            return isize.intValue();
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    void addFirst(T cell) {
        try {
            addFirst.invoke(cells, cell);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void addLast(T cell) {
        try {
            addLast.invoke(cells, cell);
        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
