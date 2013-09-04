package impl.org.controlsfx.skin;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;

import javafx.scene.control.TableColumn;

public class SpreadsheetColumn extends TableColumn<DataRow, DataCell<?>>{

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private boolean fixed=false;
	private SpreadsheetView spreadsheetView;
	
	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public SpreadsheetColumn(String equivColumn, SpreadsheetView spreadsheetView) {
		super(equivColumn);
		this.spreadsheetView = spreadsheetView;
		this.setPrefWidth(100);
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
		return fixed;
	}
	
	/**
	 * Verify that you can fix this column
	 * Right now, only a column without any cell spanning in column
	 * can be fixed.
	 * @return
	 */
	public boolean canFix(){
		final int indexColumn = getTableView().getColumns().indexOf(this);
		for (DataRow row : spreadsheetView.getGrid().getRows()) {
			if(row.get(indexColumn).getColumnSpan()>1)
				return false;
		}
		return true;
	}
	
	/**
	 * Fix this column to the left (if possible)
	 * @param fixed
	 */
	public void setFixed(boolean fixed) {
		if(canFix()){
			final int indexColumn = getTableView().getColumns().indexOf(this);
			this.fixed = fixed;
			//FIXME Decide about a visual confirmation
			//Just a visual confirmation, will be removed
			if(fixed){
				setText(getText()+"-fixed");
				spreadsheetView.getFixedColumns().add(indexColumn);
			}else{
				setText(getText().replace("-fixed", ""));
				spreadsheetView.getFixedColumns().remove(Integer.valueOf(indexColumn));
			}
		}
	}

}
