package org.controlsfx.control.spreadsheet.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableRow;

import org.controlsfx.control.spreadsheet.control.SpreadsheetRow;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.sponge.VirtualFlow;
import org.controlsfx.control.spreadsheet.sponge.VirtualScrollBar;


public class VirtualFlowSpreadsheet<T extends IndexedCell> extends VirtualFlow<T>{

	SpreadsheetView spv;
	/**
	 * Count the number of fixed Cell added to the viewport
	 * If we added just a few cells in the addLeadingCell
	 * We need to add the remaining in the addTrailingCell
	 */
	private int cellFixedAdded;
	
//	double scrollY = 0;
	public VirtualFlowSpreadsheet(){
		super();
		final ChangeListener<Number> listenerY = new ChangeListener<Number>() {
			@Override public void changed(ObservableValue ov, Number t, Number t1) {
				layoutTotal();
			}
		};
		getVbar().valueProperty().addListener(listenerY);
		
	}
	public void init(SpreadsheetView spv){
		this.spv = spv;
	}
	@Override protected void layoutChildren() {
		/*int diff = (int) Math.ceil(Math.abs((scrollY - getVbar().getValue()))*getCellCount());
		scrollY = getVbar().getValue();*/
		
		//We don't want to layout everything in case we're editing because it has no sense
		if(spv != null && (spv.getEditingCell() == null || spv.getEditingCell().getRow() == -1)){
			sortHB();
			super.layoutChildren();
			layoutTotal();
			/*if(diff>10){
				layoutTotal();
			}else{
				layoutFirstRows(diff<2? 2:diff);
			}*/
			layoutFixedRows();
		}

	}
	@Override
	public void show(int index) {
		super.show(index);
		layoutTotal();
		layoutFixedRows();
	}

	@Override
	public void scrollTo(int index) {
		if(!getCells().isEmpty() && index < getCells().get(0+getFixedRows().size()).getIndex()){
			index -= getCells().size()-getFixedRows().size();
		}
		super.scrollTo(index);

		layoutTotal();
		layoutFixedRows();
	}

	@Override
	public double adjustPixels(final double delta) {
		final double returnValue = super.adjustPixels(delta);
		/*int diff = (int) Math.ceil(Math.abs((scrollY - getVbar().getValue()))*getCellCount());
		scrollY = getVbar().getValue();
		if(diff>10){
			layoutTotal();
		}else{
			layoutFirstRows(diff<2? 2:diff);
		}*/
		layoutTotal();
		layoutFixedRows();

		return returnValue;
	}

	/**
	 * Layout the fixed rows to position them correctly
	 */
	public void layoutFixedRows(){
		sortHB();
		if(!getCells().isEmpty() && !getFixedRows().isEmpty()){
			for(int i = getFixedRows().size()-1; i>= 0 ;--i){
				SpreadsheetRow cell = (SpreadsheetRow) getCells().get(i);
				if( cell != null && getFixedRows().contains(cell.getIndex())) {
					cell.toFront();
					cell.requestLayout();
				}
			}
		}
	}
	/**
	 * Layout all the visible rows
	 */
	public void layoutTotal(){
		sortHB();
		//FIXME When scrolling fast with fixed Rows, cells is empty and not recreated..
		if(getCells().isEmpty()){
			reconfigureCells();
			//recreateCells();
		}
		for (Cell cell : getCells()) {
			if (cell != null) {
				cell.requestLayout();
			}
		}
	}

	/**
	 * Sort the rows so that they stay in order for layout
	 */
	public void sortHB(){
		final List<SpreadsheetRow> temp = (List<SpreadsheetRow>) getCells();
		final List<SpreadsheetRow> tset = new ArrayList<>(temp);
		Collections.sort(tset, new Comparator<SpreadsheetRow>() {
			@Override
			public int compare(SpreadsheetRow o1, SpreadsheetRow o2) {
				final int lhs = o1.getIndex();
				final int rhs = o2.getIndex();
				return lhs < rhs ? -1  : +1;
			}
		});
		for (final TableRow<DataRow> r : tset) {
			r.toFront();
		}
	}

	public VirtualScrollBar getVerticalBar(){
		return getVbar();
	}
	public VirtualScrollBar getHorizontalBar(){
		return getHbar();
	}

	@Override
	public List<T> getCells() {
		return super.getCells();
	}

	/**
	 * Return the index for a given cell. This allows subclasses to customise
	 * how cell indices are retrieved.
	 */
	@Override
	protected int getCellIndex(T cell){
		return ((SpreadsheetRow)cell).getIndexVirtualFlow();
	}

	/**
	 * The list of Columns fixed.It only contains the
	 * number of the colums, sorted.
	 */
	private final ArrayList<Integer> fixedColumns = new ArrayList<Integer>();

	public ArrayList<Integer> getFixedColumns(){
		return fixedColumns;
	}
	
	public T getFirstVisibleCellWithinViewPort() {
		if (getCells().isEmpty() || getHeight() <= 0) return null;

        final boolean isVertical = isVertical();
        T cell;
        for (int i = 0+getFixedRows().size(); i < getCells().size(); i++) {
            cell = getCells().get(i);
            if (cell.isEmpty()) continue;

            if (isVertical && cell.getLayoutY() + cell.getHeight() > 0) {
                return cell;
            } else if (! isVertical && cell.getLayoutX() + cell.getWidth() > 0) {
                return cell;
            }
        }

        return null;
	}
	@Override
	protected void addLeadingCells(int currentIndex, double startOffset) {
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

		//First pass to know how many cells we will add
		int cellToAdd = 0;
		while (index >= 0 && (offset > 0 || first)) {
			if (first) {
				first = false;
			} else {
				// Careful here because I've seen that it could mess things up a bit
				// Maybe use directly "fixedCellSize" if we're sure..
				offset -= getCellLength(0);
			}
			--index;
			++cellToAdd;
		}

		//Now that we know how many cells we will add, we reset the variable
		offset = startOffset;
		index = currentIndex;
		first = true;
		cellFixedAdded = 0;

		while (index >= 0 && (offset > 0 || first)) {
			/*if(index >= getCellCount()){
				if (first) {
					first = false;
				}else {
					//					offset -= getCellLength(0);
				}
				--index;
				--cellToAdd;
			}else{*/
				// If the remaining cells to add are in the header
				if(!fixedRows.isEmpty() && cellToAdd <= fixedRows.size()){
					final int realIndex = fixedRows.get(cellToAdd-1);
//					System.out.println("JaddC"+realIndex);
					cell = getAvailableCell(realIndex); // We grab the right one
					setCellIndex(cell, realIndex); // the index is the real one
					setCellIndexVirtualFlow(cell, index); // But the index for the Virtual Flow remain his (not the real one)
					++cellFixedAdded;
				}else{
//									System.out.println("JaddC"+index);
					cell = getAvailableCell(index);
					setCellIndex(cell, index);
				}
				resizeCellSize(cell); // resize must be after config
				cells.addFirst(cell);


				// A little gross but better than alternatives because it reduces
				// the number of times we have to update a cell or compute its
				// size. The first time into this loop "offset" is actually the
				// top of the current index. On all subsequent visits, it is the
				// bottom of the current index.
				if (first) {
					first = false;
				} else {
					offset -= getCellLength(cell);
				}

				// Position the cell, and update the maxPrefBreadth variable as we go.
				positionCell(cell, offset);
				maxPrefBreadth = Math.max(maxPrefBreadth, getCellBreadth(cell));
				cell.setVisible(true);
				--index;
				--cellToAdd;
//			}
		}

		// There are times when after laying out the cells we discover that
		// the top of the first cell which represents index 0 is below the top
		// of the viewport. In these cases, we have to adjust the cells up
		// and reset the mapper position. This might happen when items got
		// removed at the top or when the viewport size increased.
		cell = cells.getFirst();
		final int firstIndex = getCellIndex(cell);
		final double firstCellPos = getCellPosition(cell);
		if (firstIndex == 0 && firstCellPos > 0) {
			setPosition(0.0f);
			offset = 0;
			for (int i = 0; i < cells.size(); i++) {
				cell = cells.get(i);
				positionCell(cell, offset);
				offset += getCellLength(cell);
			}
		}
	}
	@Override
	protected boolean addTrailingCells(boolean fillEmptyCells) {
		// If cells is empty then addLeadingCells bailed for some reason and
		// we're hosed, so just punt
		if (cells.isEmpty()) {
			return false;
		}

		// While we have not yet laid out so many cells that they would fall
		// off the flow, so we will continue to create and add cells. When the
		// offset becomes greater than the width/height of the flow, then we
		// know we cannot add any more cells.
		final T startCell = cells.getLast();
		double offset = getCellPosition(startCell) + getCellLength(startCell);
		int index = getCellIndex(startCell) + 1;
		boolean filledWithNonEmpty = index <= getCellCount();

		while (offset < viewportLength){// && index <getCellCount()) {
			if (index >= getCellCount()) {
				if (offset < viewportLength) {
					filledWithNonEmpty = false;
				}
				if (! fillEmptyCells) {
					return filledWithNonEmpty;
				}
			}
			T cell = null;
			// If we have a lot of rows in header, we need to add the remaining in the trailingCells
			//I Added fillEmptyCells because it appears that when AddtrailingCells is called from
			// adjustPixel, we add several time the rows in fixedHeader...
			if(!fixedRows.isEmpty() && cellFixedAdded < fixedRows.size() && fillEmptyCells){
				final int realIndex = fixedRows.get(cellFixedAdded);
//				System.out.println("JaddD"+realIndex);
				cell = getAvailableCell(realIndex); // We grab the right one
				setCellIndex(cell, realIndex); // the index is the real one
				setCellIndexVirtualFlow(cell, index); // But the index for the Virtual Flow remain his (not the real one)
				++cellFixedAdded;
			}else{
//								System.out.println("JaddD"+index);
				cell = getAvailableCell(index);
				setCellIndex(cell, index);
			}
			resizeCellSize(cell); // resize happens after config!
			cells.addLast(cell);


			// Position the cell and update the max pref
			positionCell(cell, offset);
			maxPrefBreadth = Math.max(maxPrefBreadth, getCellBreadth(cell));

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
		T firstCell = cells.getFirst();
		index = ((SpreadsheetRow)firstCell).getIndexVirtualFlow();
		final int realIndex = firstCell.getIndex();
		final T lastNonEmptyCell = getLastVisibleCell();
		double start = getCellPosition(firstCell);
		final double end = getCellPosition(lastNonEmptyCell) + getCellLength(lastNonEmptyCell);
		if ((index != 0 || index == 0 && start < 0) && fillEmptyCells &&
				lastNonEmptyCell != null && getCellIndex(lastNonEmptyCell) == getCellCount() - 1 && end < viewportLength) {

			//Quite impossible to add properly the FixedRows so I choose to rebuild the view
			if(!fixedRows.isEmpty()){
				final int currentIndex = computeCurrentIndex();
				addAllToPile();

				// The distance from the top of the viewport to the top of the
				// cell for the current index.
				final double offset2 = -computeViewportOffset(getPosition());
				// Add all the leading and trailing cells (the call to add leading
				// cells will add the current cell as well -- that is, the one that
				// represents the current position on the mapper).
				addLeadingCells(currentIndex, offset2);
				// Force filling of space with empty cells if necessary
				addTrailingCells(true);
			}else{


				double prospectiveEnd = end;
				final double distance = viewportLength - end;
				while (prospectiveEnd < viewportLength && index != 0 && -start < distance) {
					index--;
					//				System.out.println("JaddE"+realIndex);
					final T cell = getAvailableCell(realIndex);
					setCellIndex(cell, index);
					resizeCellSize(cell); // resize must be after config
//					System.out.println("JaddC"+realIndex);
					cells.addFirst(cell);


					final double cellLength = getCellLength(cell);
					start -= cellLength;
					prospectiveEnd += cellLength;
					positionCell(cell, start);
					maxPrefBreadth = Math.max(maxPrefBreadth, getCellBreadth(cell));
					cell.setVisible(true);
				}

				// The amount by which to translate the cells down
				firstCell = cells.getFirst();
				start = getCellPosition(firstCell);
				double delta = viewportLength - end;
				if (getCellIndex(firstCell) == 0 && delta > -start) {
					delta = -start;
				}
				// Move things
				for (int i = 0; i < cells.size(); i++) {
					final T cell = cells.get(i);
					positionCell(cell, getCellPosition(cell) + delta);
				}

				// Check whether the first cell, subsequent to our adjustments, is
				// now index #0 and aligned with the top. If so, change the position
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
		setCellIndexVirtualFlow(cell,index);
	}

	private void setCellIndexVirtualFlow(T cell, int index) {
		if (cell == null) {
			return;
		}

		((SpreadsheetRow)cell).setIndexVirtualFlow(index);
	}
	
	private final TreeSet<Integer> visibleRows = new TreeSet<Integer>();
	public TreeSet<Integer> getVisibleRows(){
		return visibleRows;
	}
	
	/**
	 * The list of Rows fixed. It only contains the
	 * number of the rows, sorted.
	 */
	private final static ArrayList<Integer> fixedRows = new ArrayList<Integer>();

	public ArrayList<Integer> getFixedRows(){
		return fixedRows;
	}
	
	
	/*****************************************************************
	 * 		METHOD NO LONGER IN USE BUT MAY BE NEEDED IN FUTURE
	 *****************************************************************/
	/**
	 * The first time "setLayoutFixedColumns" is called but we want
	 * to layout all the columns so we need that hack to prevent that
	 */
	/*private  boolean firstTimeColumns = false;

	 *//**
	 * Layout the fixed column when Hbar is touched
	 *//*
	public void layoutFixedColumn(){
		// because the first time we want to layout everything
		//TODO fix this because it's not optimum
		if(!firstTimeColumns){
			firstTimeColumns = true;
			return;
		}
		if(firstTimeColumns && !cells.isEmpty() && !fixedColumns.isEmpty()){
			for(int i = 0; i<cells.size(); ++i){
				final SpreadsheetRow rows = (SpreadsheetRow) cells.get(i);
				if(rows != null) {
					rows.setLayoutFixedColumns(true);
					rows.requestLayout();
				}
			}
		}
	}

	  *//**
	  * Layout the first NON-FIXED row if it needs to span correctly
	  */
	public void layoutFirstRows(int number){
		sortHB();
		if(!getCells().isEmpty()) {
			int beginning = getFirstVisibleCell().getIndex();
			for(int i = beginning ; i<=(number+beginning); ++i){
				final SpreadsheetRow rows = (SpreadsheetRow) getCell(i);
				if(rows != null) {
					System.out.println("je demande"+i);
					rows.requestLayout();
				}
			}
		}
	}
/*
	   *//**
	   * Layout the two first NON-FIXED rows
	   *//*
	public void layoutTwoFirstRows(){
		sortHB();
		if(!cells.isEmpty()){
			final T temp = cells.getNonFixed(0);

			if(temp != null) {
				//				System.out.println("je demande"+temp.getIndex());
				temp.requestLayout();
			}
			//			temp = cells.getNonFixed(1);
			if(temp != null) {
				//				System.out.println("je demande"+temp.getIndex());
				temp.requestLayout();
			}
		}
	}

	public void layoutDeltaFirstRows(double delta){
		sortHB();
		if(!cells.isEmpty()){
			if(delta<0){
				delta = Math.abs(delta);
				final int number = (int) Math.ceil(delta/fixedCellSize);
				final SpreadsheetRow temp;
				for(int i = number;i>=0;--i){
					//for(int i = 0;i<number;++i){
					//					temp = (SpreadsheetRow) cells.getNonFixed(i);
					if(temp != null) {
						//						System.out.println("JE demande"+temp.getIndex());
						temp.requestLayout();
					}
				}

			}else{
				delta = Math.abs(delta);
				final int number = (int) Math.ceil(delta/fixedCellSize);
				final SpreadsheetRow temp;
				for(int i = cells.size()-1;i>=cells.size()-1 - number;--i){
					//					temp = (SpreadsheetRow) cells.getNonFixed(i);
					if(temp != null) {
						//						System.out.println("JE demande"+temp.getIndex());
						temp.requestLayout();
					}
				}
			}
		}
	}*/
	
	/*T getLeadingAvailableCell(int index, double offset){
	if(cellToAdd <= 0 && !fixedRows.isEmpty()){
		cellToAdd = 0;
		int currentIndex = index;
		boolean first = true;
		//First pass to know how many cells we will add
		while (index >= 0 && (offset > 0 || first)) {
			if (first) {
				first = false;
			} else {
				// Careful here because I've seen that it could mess things up a bit
				// Maybe use directly "fixedCellSize" if we're sure..
				offset -= getCellLength(0);
			}
			--index;
			++cellToAdd;
		}
		index = currentIndex;
	}
	// If the remaining cells to add are in the header
	T cell;
	if(!fixedRows.isEmpty() && cellToAdd <= fixedRows.size()){
		final int realIndex = fixedRows.get(cellToAdd-1);
		cell = getAvailableCell(realIndex); // We grab the right one
		setCellIndex(cell, realIndex); // the index is the real one
		setCellIndexVirtualFlow(cell, index); // But the index for the Virtual Flow remain his (not the real one)
		++cellFixedAdded;
	}else{
		//				System.out.println("JaddC"+index);
		visibleRows.add(index);
		cell = getAvailableCell(index);
		setCellIndex(cell, index);
	}
	--cellToAdd;
	return cell;
}

T getTrailingAvailableCell(int index){
	// If the remaining cells to add are in the header
	T cell;
	if(!fixedRows.isEmpty() && cellFixedAdded < fixedRows.size()){
		final int realIndex = fixedRows.get(cellFixedAdded);
		cell = getAvailableCell(realIndex); // We grab the right one
		setCellIndex(cell, realIndex); // the index is the real one
		setCellIndexVirtualFlow(cell, index); // But the index for the Virtual Flow remain his (not the real one)
		++cellFixedAdded;
	}else{
		//				System.out.println("JaddC"+index);
		visibleRows.add(index);
		cell = getAvailableCell(index);
		setCellIndex(cell, index);
	}
	return cell;
}*/
}
