package org.controlsfx.control.spreadsheet.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
		if(!getCells().isEmpty() && index < getCells().get(index+getFixedRows().size()).getIndex()){
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
	/*****************************************************************
	 * 				MODIFIED BY NELLARMONIA
	 *****************************************************************/
	/*****************************************************************
	 * 				END OF MODIFIED BY NELLARMONIA
	 *****************************************************************/
}
