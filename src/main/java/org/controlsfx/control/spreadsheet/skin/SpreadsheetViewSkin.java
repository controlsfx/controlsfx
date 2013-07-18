package org.controlsfx.control.spreadsheet.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import org.controlsfx.control.spreadsheet.control.SpreadsheetRow;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.sponge.TableHeaderRow;
import org.controlsfx.control.spreadsheet.sponge.TableViewSkin;
import org.controlsfx.control.spreadsheet.sponge.VirtualFlow;

public class SpreadsheetViewSkin<T> extends TableViewSkin<T> {

	protected RowHeader rowHeader;
	private final double rowHeaderWidth = 50;
	public double getRowHeaderWidth() {
		return rowHeaderWidth;
	}
	
	protected SpreadsheetView spreadsheetView;
	
	public SpreadsheetViewSkin(TableView tableView,
			SpreadsheetView spreadsheetView) {
		super(tableView);
		this.spreadsheetView = spreadsheetView;
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		spreadsheetView.getFixedRows().addListener(fixedRowsListener);
		spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);
		spreadsheetView.setHbar(getFlow().getHorizontalBar());
		spreadsheetView.setVbar(getFlow().getVerticalBar());
		final SpreadsheetView.RowAccessor<TableRow<T>> lcells = new SpreadsheetView.RowAccessor<TableRow<T>>() {
			@Override
			public TableRow<T> get(int index) {
				return (TableRow<T>) getFlow().getCells().get(index);
			}

			@Override
			public boolean isEmpty() {
				return getFlow().getCells().isEmpty();
			}

			@Override
			public int size() {
				return getFlow().getCells().size();
			}

		};

		spreadsheetView.setRows(lcells);
		/*****************************************************************
		 * 				END MODIFIED BY NELLARMONIA
		 *****************************************************************/
		init();
	}

	protected void init() {
		getFlow().getVerticalBar().valueProperty().addListener(vbarValueListener);
		rowHeader =new RowHeader(this,spreadsheetView, rowHeaderWidth);
		getChildren().addAll(rowHeader);
		
		rowHeader.init();
		((SpreadsheetHeaderRow)getTableHeaderRow()).init();
		getFlow().init(spreadsheetView);
	}

	@Override protected void layoutChildren( double x, double y,
			double w, final double h) {
		if(spreadsheetView == null)
			return;
		if(spreadsheetView.getRowHeader().get()){
			x+= rowHeaderWidth;
			w-=rowHeaderWidth;
		}
		super.layoutChildren(x, y, w, h);
		
		final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
		double tableHeaderRowHeight=0;

		if(spreadsheetView.getColumnHeader().get()){
			// position the table header
			tableHeaderRowHeight = getTableHeaderRow().prefHeight(-1);
			layoutInArea(getTableHeaderRow(), x, y, w, tableHeaderRowHeight, baselineOffset,
					HPos.CENTER, VPos.CENTER);
			y += tableHeaderRowHeight;
		}else{
			//TODO try to hide the columnHeader
		}

		if(spreadsheetView.getRowHeader().get()){
			layoutInArea(rowHeader, x-rowHeaderWidth, y-tableHeaderRowHeight, w, h, baselineOffset,
					HPos.CENTER, VPos.CENTER);
		}
	}
	final InvalidationListener vbarValueListener = new InvalidationListener() {
		@Override public void invalidated(Observable valueModel) {
			verticalScroll();
		}
	};

	protected void verticalScroll() {
		rowHeader.updateScrollY();
	}

	@Override
	protected void onFocusPreviousCell() {
		final TableFocusModel fm = getFocusModel();
		if (fm == null) {
			return;
		}
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		final int row = fm.getFocusedIndex();
		//We try to make visible the rows that may be hiden by Fixed rows
		if(!getFlow().getCells().isEmpty() && getFlow().getCell(0+getFlow().getFixedRows().size()).getIndex()> row && !getFlow().getFixedRows().contains(row)) {
			flow.scrollTo(row);
		}else{
			flow.show(row);
		}
		scrollHorizontally();
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}

	@Override
	protected void onFocusNextCell() {
		final TableFocusModel fm = getFocusModel();
		if (fm == null) {
			return;
		}
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		final int row = fm.getFocusedIndex();
		//We try to make visible the rows that may be hiden by Fixed rows
		if(!getFlow().getCells().isEmpty() && getFlow().getCell(0+getFlow().getFixedRows().size()).getIndex()> row && !getFlow().getFixedRows().contains(row)) {
			flow.scrollTo(row);
		}else{
			flow.show(row);
		}
		scrollHorizontally();
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}
	@Override
	protected void onSelectPreviousCell() {
		super.onFocusPreviousCell();
		scrollHorizontally();
	}

	@Override
	protected void onSelectNextCell() {
		super.onSelectNextCell();
		scrollHorizontally();
	}

	/**
	 * We listen on the FixedRows in order to do the modification in the VirtualFlow
	 */
	private final ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
		@Override
		public void onChanged(Change<? extends Integer> c) {
			while (c.next()) {
				for (final Integer remitem : c.getRemoved()) {
					getFlow().getFixedRows().remove(remitem);
				}
				for (final Integer additem : c.getAddedSubList()) {
					getFlow().getFixedRows().add(additem);
				}
			}
			//requestLayout() not responding immediately..
			getFlow().layoutTotal();
		}

	};

	/**
	 * We listen on the FixedColumns in order to do the modification in the VirtualFlow
	 */
	private final ListChangeListener<Integer> fixedColumnsListener = new ListChangeListener<Integer>() {
		@Override
		public void onChanged(Change<? extends Integer> c) {
			if(getFlow().getFixedColumns().size() > c.getList().size()){
				for(int i=0;i<getFlow().getCells().size();++i){
					((SpreadsheetRow) getFlow().getCells().get(i)).putFixedColumnToBack();
				}
			}

			while (c.next()) {
				for (final Integer remitem : c.getRemoved()) {
					getFlow().getFixedColumns().remove(remitem);
				}
				for (final Integer additem : c.getAddedSubList()) {
					getFlow().getFixedColumns().add(additem);
				}
			}
			//requestLayout() not responding immediately..
			getFlow().layoutTotal();
		}

	};

	@Override
	protected VirtualFlow<TableRow<T>> createVirtualFlow() {
		return new VirtualFlowSpreadsheet<TableRow<T>>();
	}
	
	protected TableHeaderRow createTableHeaderRow() {
		return new SpreadsheetHeaderRow(this);
	}
	
	BooleanProperty getTableMenuButtonVisibleProperty(){
		return tableMenuButtonVisibleProperty();
	}
	protected void scrollHorizontally(TableColumnBase col) {

		if (col == null || !col.isVisible()) {
			return;
		}

		// work out where this column header is, and it's width (start -> end)
		double start = 0;//scrollX;
		for (TableColumnBase c : getVisibleLeafColumns()) {
            if (c.equals(col)) break;
            start += c.getWidth();
        }

		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 * We modifed this function so that we ensure that any selected cells
		 * will not be below a fixed column. Because when there's some fixed columns,
		 * the "left border" is not the table anymore, but the right side of the last
		 * fixed columns.
		 *****************************************************************/
		// We add the fixed columns width
		final double fixedColumnWidth = getFixedColumnWidth();

		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
		final double end = start + col.getWidth();

		// determine the visible width of the table
		final double headerWidth = getSkinnable().getWidth() - snappedLeftInset() - snappedRightInset();

		// determine by how much we need to translate the table to ensure that
		// the start position of this column lines up with the left edge of the
		// tableview, and also that the columns don't become detached from the
		// right edge of the table
		final double pos = getFlow().getHorizontalBar().getValue();
		final double max = getFlow().getHorizontalBar().getMax();
		double newPos;

		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		if (start < pos+fixedColumnWidth && start >= 0 && start >= fixedColumnWidth) {
			newPos = start- fixedColumnWidth <0 ? start: start- fixedColumnWidth ;
		} else {
			final double delta = start < 0 || end > headerWidth ? start - pos -fixedColumnWidth : 0;
			newPos = pos + delta > max ? max : pos + delta ;
		}

		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/


		// FIXME we should add API in VirtualFlow so we don't end up going
		// direct to the hbar.
		// actually shift the flow - this will result in the header moving
		// as well
		getFlow().getHorizontalBar().setValue(newPos);
	}

	/**
	 * Calc the width of the fixed columns in order not to select
	 * cells that are hidden by the fixed columns
	 * @return
	 */
	private double getFixedColumnWidth() {
		double fixedColumnWidth = 0;
		if(!getFlow().getFixedColumns().isEmpty()){
			for (int i = 0, max = getFlow().getFixedColumns().size(); i < max; ++i){
				final TableColumnBase<DataRow,?> c = getVisibleLeafColumn(i);
				fixedColumnWidth += c.getWidth();
			}
		}
		return fixedColumnWidth;
	}
	
	private VirtualFlowSpreadsheet getFlow(){
		return (VirtualFlowSpreadsheet) flow;
	}

}
