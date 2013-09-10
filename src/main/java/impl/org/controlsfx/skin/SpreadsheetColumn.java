package impl.org.controlsfx.skin;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;

public class SpreadsheetColumn{

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private SpreadsheetView spreadsheetView;
	private TableColumn<DataRow, DataCell<?>> column;
	private boolean canFix;
	private Integer indexColumn;
	
	/***************************************************************************
     *                                                                         *
     * Package protected Fields                                                *
     *                                                                         *
     **************************************************************************/
	/**
	 * Indicate whether or not this column is currently fixed on the left.
	 * The column can be fixed but not moved because we have not exceed it.
	 * It is needed for the HoverProperty. See {@link SpreadsheetRow} and {@link SpreadsheetRowSkin} implementation.
	 */
	Boolean currentlyFixed = false;

	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public SpreadsheetColumn(TableColumn<DataRow, DataCell<?>> column, SpreadsheetView spreadsheetView, Integer indexColumn) {
		this.spreadsheetView = spreadsheetView;
		this.column = column;
		column.setPrefWidth(100);
		this.indexColumn = indexColumn;
		canFix = canFix();
	}
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	/**
	 * Return a boolean indicating whether this column is fixed or not.
	 * @return
	 */
	public boolean isFixed() {
		return column.impl_isFixed();
	}
	
	/**
	 * Verify that you can fix this column.
	 * Right now, only a column without any cell spanning 
	 * can be fixed.
	 * 
	 * @return
	 */
	public boolean canFix(){
		//FIXME Listen to change on model, then re-compute 
		for (DataRow row : spreadsheetView.getGrid().getRows()) {
			if(row.get(indexColumn).getColumnSpan()>1 || row.get(indexColumn).getRowSpan()>1)
				return false;
		}
		return true;
	}
	
	/**
	 * Fix this column to the left (if possible)
	 * @param fixed
	 */
	public void setFixed(boolean fixed) {
		if(canFix){
			column.impl_setFixed(fixed);
			
			
			//FIXME Decide about a visual confirmation
			//Just a visual confirmation, will be removed
			if(column.impl_isFixed()){
				column.setText(column.getText()+"-fixed");
				spreadsheetView.getFixedColumns().add(indexColumn);
			}else{
				column.setText(column.getText().replace("-fixed", ""));
				spreadsheetView.getFixedColumns().remove(Integer.valueOf(indexColumn));
			}
			FXCollections.sort(spreadsheetView.getFixedColumns());
		}
	}
	
	
	public void setPrefWidth(double arg0){
		column.setPrefWidth(arg0);
	}
	/***************************************************************************
     *                                                                         *
     * Package protected Methods                                               *
     *                                                                         *
     **************************************************************************/
	Boolean getCurrentlyFixed() {
		return currentlyFixed;
	}

	void setCurrentlyFixed(Boolean currentlyFixed) {
		this.currentlyFixed = currentlyFixed;
	}
}
