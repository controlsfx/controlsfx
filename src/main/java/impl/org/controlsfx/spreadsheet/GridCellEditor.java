package impl.org.controlsfx.spreadsheet;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellEditor;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class GridCellEditor {

	/***************************************************************************
	 * * Protected/Private Fields * *
	 **************************************************************************/

	private final SpreadsheetHandle handle;
	// transient properties - these fields will change based on the current
	// cell being edited.
	private SpreadsheetCell modelCell;
	private CellView viewCell;

	// private internal fields
	private SpreadsheetEditor spreadsheetEditor;
	private InvalidationListener editorListener;
	private InvalidationListener il;
	private boolean editing = false;
	private SpreadsheetCellEditor<?> spreadsheetCellEditor;
    private CellView lastHover = null;

	/***************************************************************************
	 * * Constructor * *
	 **************************************************************************/

	/**
	 * Construct the SpreadsheetCellEditor.
	 */
	public GridCellEditor(SpreadsheetHandle handle) {
		this.handle = handle;
		this.spreadsheetEditor = new SpreadsheetEditor();
	}

	/***************************************************************************
	 * * Public Methods * *
	 **************************************************************************/
	/**
	 * Update the internal {@link SpreadsheetCell}.
	 * @param cell
	 */
	public void updateDataCell(SpreadsheetCell cell) {
		this.modelCell = cell;
	}

	/**
	 * Update the internal {@link CellView}
	 * @param cell
	 */
	public void updateSpreadsheetCell(CellView cell) {
		this.viewCell = cell;
	}

	/**
	 * Update the SpreadsheetCellEditor
	 * @param spreadsheetCellEditor2
	 */
	public void updateSpreadsheetCellEditor(final SpreadsheetCellEditor<?> spreadsheetCellEditor2) {
		this.spreadsheetCellEditor = spreadsheetCellEditor2;
	}
    
    public CellView getLastHover() {
    	return lastHover;
    }
    
    public void setLastHover(CellView lastHover) {
    	this.lastHover = lastHover;
    }
    
	/**
	 * Whenever you want to stop the edition, you call that method.<br/>
	 * True means you're trying to commit the value, then {@link #validateEdit()}
	 * will be called in order to verify that the value is correct.<br/>
	 * False means you're trying to cancel the value and it will be follow by {@link #end()}.<br/>
	 * See SpreadsheetCellEditor description
	 * @param b true means commit, false means cancel
	 */
	public void endEdit(boolean b){
		if(b){
			final SpreadsheetView view = handle.getView();
			Object value = modelCell.getCellType().convertValue(spreadsheetCellEditor.getControlValue());
			if(value != null && viewCell != null){
				//We update the modified cells
				if(!modelCell.getItem().equals(value) && !view.getModifiedCells().contains(modelCell))
					view.getModifiedCells().add(modelCell);

				modelCell.setItem(value);
				viewCell.commitEdit(modelCell);
				end();
				spreadsheetCellEditor.end();
			}
		}else if(viewCell != null){
			viewCell.cancelEdit();
			end();
			spreadsheetCellEditor.end();
		}
	}


	/**
	 * Return if this editor is currently being used.
	 * @return if this editor is being used.
	 */
	public boolean isEditing() {
		return editing;
	}

	public SpreadsheetCell getModelCell() {
		return modelCell;
	}

	/***************************************************************************
	 * * Protected/Private Methods * *
	 **************************************************************************/
	void startEdit() {
		editing = true;
		spreadsheetEditor.startEdit();

		// If the SpreadsheetCell is deselected, we commit.
		// Sometimes, when you you touch the scrollBar when editing,
		// this is called way
		// too late and the SpreadsheetCell is null, so we need to be
		// careful.
		il = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				endEdit(false);
			}
		};

		viewCell.selectedProperty().addListener(il);

		// In ANY case, we stop when something move in scrollBar Vertical
		editorListener = new InvalidationListener() {
			@Override
			public void invalidated(Observable arg0) {
				endEdit(false);
			}
		};
		handle.getCellsViewSkin().getVBar().valueProperty().addListener(editorListener);
		//FIXME We need to REALLY find a way to stop edition when anything happen
		// This is one way but it will need further investigation
		handle.getView().disabledProperty().addListener(editorListener);

		viewCell.setGraphic(spreadsheetCellEditor.getEditor());
		
		//Then we call the user editor in order for it to be ready
		Object value = modelCell.getItem();
		spreadsheetCellEditor.startEdit(value);
	}


	private void end() {
		editing = false;
		spreadsheetEditor.end();
		if (viewCell != null) {
			viewCell.selectedProperty().removeListener(il);
		}
		il = null;

		handle.getCellsViewSkin().getVBar().valueProperty().removeListener(editorListener);
		handle.getView().disabledProperty().removeListener(editorListener);
		editorListener = null;
		this.modelCell = null;
		this.viewCell = null;
	}


	private class SpreadsheetEditor {

		/***********************************************************************
		 * * Private Fields * *
		 **********************************************************************/
		private GridRow original;
		private boolean isMoved;

		private int getCellCount() {
			return handle.getCellsViewSkin().getCellsSize();
		}

		private boolean addCell(CellView cell){
			GridRow temp = handle.getCellsViewSkin().getRow(getCellCount()-1-handle.getView().getFixedRows().size());
			if(temp != null){
				temp.addCell(cell);
				return true;
			}
			return false;
		}
		/***********************************************************************
		 * * Public Methods * *
		 **********************************************************************/

		/**
		 * In case the cell is spanning in rows. We want the cell to be fully
		 * accessible so we need to remove it from its tableRow and add it to the
		 * last row possible. Then we translate the cell so that it's invisible for
		 * the user.
		 */
		public void startEdit() {
			// Case when RowSpan if larger and we're not on the last row
			if (modelCell != null && modelCell.getRowSpan() > 1
					&& modelCell.getRow() != getCellCount() - 1) {
				original = (GridRow) viewCell.getTableRow();

				final double temp = viewCell.getLocalToSceneTransform().getTy();
				isMoved = addCell(viewCell);
				if (isMoved) {
					viewCell.setTranslateY(temp
							- viewCell.getLocalToSceneTransform().getTy());
					original.putFixedColumnToBack();
				}
			}
		}

		/**
		 * When we have finish editing. We put the cell back to its right TableRow.
		 */
		public void end() {
			if (modelCell != null && modelCell.getRowSpan() > 1) {
				viewCell.setTranslateY(0);
				if (isMoved) {
					original.addCell(viewCell);
					original.putFixedColumnToBack();
				}
			}
		}
	}
}
