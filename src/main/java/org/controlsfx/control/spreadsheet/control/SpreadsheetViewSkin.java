package org.controlsfx.control.spreadsheet.control;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import org.controlsfx.control.spreadsheet.sponge.TableViewSkin;
import org.controlsfx.control.spreadsheet.sponge.VirtualFlow;

public class SpreadsheetViewSkin<T> extends TableViewSkin<T> {

	public SpreadsheetViewSkin(TableView tableView,
			SpreadsheetView spreadsheetView) {
		super(tableView, spreadsheetView);

		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		spreadsheetView.getFixedRows().addListener(fixedRowsListener);
		spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);
		spreadsheetView.setVisibleRows(flow.getVisibleRows());
		spreadsheetView.setHbar(((VirtualFlowSpreadsheet)flow).getHorizontalBar());
		spreadsheetView.setVbar(((VirtualFlowSpreadsheet)flow).getVerticalBar());
		final SpreadsheetView.RowAccessor<TableRow<T>> lcells = new SpreadsheetView.RowAccessor<TableRow<T>>() {
			@Override
			public TableRow<T> get(int index) {
				return (TableRow<T>) ((VirtualFlowSpreadsheet)flow).getCells().get(index);
			}

			@Override
			public boolean isEmpty() {
				return ((VirtualFlowSpreadsheet)flow).getCells().isEmpty();
			}
		};

		spreadsheetView.setRows(lcells);
		/*****************************************************************
		 * 				END MODIFIED BY NELLARMONIA
		 *****************************************************************/
		init();
	}

	protected void init() {
		((VirtualFlowSpreadsheet)flow).getVerticalBar().valueProperty().addListener(vbarValueListener);
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
		if(!flow.getVisibleRows().isEmpty() && flow.getVisibleRows().first()> row && !flow.getFixedRows().contains(row)) {
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
		if(!flow.getVisibleRows().isEmpty() && flow.getVisibleRows().first()> row && !flow.getFixedRows().contains(row)) {
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
					flow.getFixedRows().remove(remitem);
				}
				for (final Integer additem : c.getAddedSubList()) {
					flow.getFixedRows().add(additem);
				}
			}
			//requestLayout() not responding immediately..
			((VirtualFlowSpreadsheet)flow).layoutTotal();
		}

	};

	/**
	 * We listen on the FixedColumns in order to do the modification in the VirtualFlow
	 */
	private final ListChangeListener<Integer> fixedColumnsListener = new ListChangeListener<Integer>() {
		@Override
		public void onChanged(Change<? extends Integer> c) {
			if(flow.getFixedColumns().size() > c.getList().size()){
				for(int i=0;i<((VirtualFlowSpreadsheet)flow).getCells().size();++i){
					((SpreadsheetRow) ((VirtualFlowSpreadsheet)flow).getCells().get(i)).putFixedColumnToBack();
				}
			}

			while (c.next()) {
				for (final Integer remitem : c.getRemoved()) {
					flow.getFixedColumns().remove(remitem);
				}
				for (final Integer additem : c.getAddedSubList()) {
					flow.getFixedColumns().add(additem);
				}
			}
			//requestLayout() not responding immediately..
			((VirtualFlowSpreadsheet)flow).layoutTotal();
		}

	};

	@Override
	protected VirtualFlow<TableRow<T>> createVirtualFlow() {
		return new VirtualFlowSpreadsheet<TableRow<T>>();
	}

}
