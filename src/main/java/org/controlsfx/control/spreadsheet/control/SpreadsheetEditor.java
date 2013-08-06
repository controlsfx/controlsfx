package org.controlsfx.control.spreadsheet.control;

import org.controlsfx.control.spreadsheet.model.DataCell;

public class SpreadsheetEditor {
	
	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private SpreadsheetRow original;
	private DataCell<?> cell;
	private SpreadsheetCell gc;
	private boolean isMoved;
	private SpreadsheetView spreadsheetView;
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	public void begin(DataCell<?> cell, SpreadsheetCell bc, SpreadsheetView t) {
		this.cell = cell;
		this.gc = bc;
		this.spreadsheetView = t;
	}

	public void startEdit() {
		//Case when RowSpan if larger and we're not on the last row
				if(cell.getRowSpan()>1 && cell.getRow() != spreadsheetView.getVirtualFlowCellSize()-1){
					original = (SpreadsheetRow) gc.getTableRow();
					
					final double temp = gc.getLocalToSceneTransform().getTy();
					isMoved = spreadsheetView.addCell(gc);
					if(isMoved){
						gc.setTranslateY(temp - gc.getLocalToSceneTransform().getTy());
						original.putFixedColumnToBack();
					}
				}
	}

	public void end() {
		if(cell != null && cell.getRowSpan() >1){
			gc.setTranslateY(0);
			if(isMoved){
				original.addCell(gc);
				original.putFixedColumnToBack();
			}
		}
	}
}
