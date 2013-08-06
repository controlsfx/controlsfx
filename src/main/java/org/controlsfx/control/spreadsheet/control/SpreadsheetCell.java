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
package org.controlsfx.control.spreadsheet.control;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.spreadsheet.control.SpreadsheetView.SpanType;
import org.controlsfx.control.spreadsheet.editor.Editor;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;
import com.sun.javafx.scene.control.skin.TableCellSkin;


/**
 *
 * The View cell that will be visible on screen.
 * It holds the {@link DataRow} and the {@link DataCell}.
 */
public class SpreadsheetCell extends TableCell<DataRow, DataCell<?>> {

	/***************************************************************************
     *                                                                         *
     * Static Fields                                                           *
     *                                                                         *
     **************************************************************************/
	private static final String ANCHOR_PROPERTY_KEY = "table.anchor";

	static TablePositionBase<?> getAnchor(Control table, TablePositionBase<?> focusedCell) {
		return hasAnchor(table) ?
				(TablePositionBase<?>) table.getProperties().get(ANCHOR_PROPERTY_KEY) :
					focusedCell;
	}
	static boolean hasAnchor(Control table) {
		return table.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
	}
	
	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public SpreadsheetCell() {

		hoverProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				final int row = getIndex();
				final SpreadsheetView spv = ((SpreadsheetRow)getTableRow()).getSpreadsheetView();

				if (getItem() == null) {
					getTableRow().requestLayout();
					// If we are not at the top of the Spanned Cell
				} else if (t1 && row != getItem().getRow()) {
					spv.hoverGridCell(getItem());
				} else if (!t1 && row != getItem().getRow()) {
					spv.unHoverGridCell();
				}
			}
		});
		//When we detect a drag, we start the Full Drag so that other event will be fired
		this.addEventHandler(MouseEvent.DRAG_DETECTED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				startFullDrag();
		}});
		
		
		setOnMouseDragEntered(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent arg0) {
				dragSelect(arg0);
		}});

	}
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	@Override
	public void startEdit() {
		if(!isEditable()) {
			return;
		}

		final int column = this.getTableView().getColumns().indexOf(this.getTableColumn());
		final int row = getIndex();
		//We start to edit only if the Cell is a normal Cell (aka visible).
		final SpreadsheetView spv = ((SpreadsheetRow)getTableRow()).getSpreadsheetView();
		final SpanType type = spv.getSpanType(row, column);
		if ( type == SpreadsheetView.SpanType.NORMAL_CELL || type == SpreadsheetView.SpanType.ROW_VISIBLE) {
			Editor editor = spv.getEditor(getItem(), this);
			if(editor != null){
				super.startEdit();
//				System.out.println("je start"+row+"/"+((SpreadsheetRow) getTableRow()).getIndexVirtualFlow()+"/"+column);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				editor.startEdit();
			}else{
				//TODO We got a problem right now because two lines are added.
			}
		}
	}

	@Override
	public void commitEdit(DataCell<?> newValue) {
		if (! isEditing()) {
			return;
		}
		super.commitEdit(newValue);

		setContentDisplay(ContentDisplay.TEXT_ONLY);

		updateItem(newValue, false);


	}

	@Override
	public void cancelEdit() {
		if (! isEditing()) {
			return;
		}

		super.cancelEdit();
		
		setContentDisplay(ContentDisplay.TEXT_ONLY);
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				getTableView().requestFocus();
			}
		};
		Platform.runLater(r);
	}

	@Override
	public void updateItem(final DataCell<?> item, boolean empty) {
		final boolean emptyRow = getTableView().getItems().size() < getIndex() + 1;

		/**
		 * don't call super.updateItem() because it will trigger cancelEdit() if
		 * the cell is being edited. It causes calling commitEdit() ALWAYS call
		 * cancelEdit as well which is undesired.
		 *
		 */
		if (!isEditing()) {
			super.updateItem(item, empty && emptyRow);
		}
		if (empty && isSelected()) {
			updateSelected(false);
		}
		if (empty && emptyRow) {
			setText(null);
			//do not nullify graphic here. Let the TableRow to control cell dislay
			//setGraphic(null);
			setContentDisplay(null);
		} else if (!isEditing() && item != null) {
			show(item);

		}
	}

	/**
	 * Set this SpreadsheetCell hoverProperty
	 *
	 * @param hover
	 */
	public void setHoverPublic(boolean hover) {
		this.setHover(hover);
		// We need to tell the SpreadsheetRow where this SpreadsheetCell is in to be in Hover
		//Otherwise it's will not be visible
		((SpreadsheetRow) this.getTableRow()).setHoverPublic(hover);
	}
	
	@Override
	public String toString(){
		return getItem().getRow()+"/"+getItem().getColumn();

	}
	/**
	 * Called in the gridRowSkinBase when doing layout
	 * This allow not to override opacity in the row and let the
	 * cell handle itself
	 */
	public void show(final DataCell<?> item){
		//We reset the settings
		setText(item.getStr());
		this.setEditable(true);
		if (item.getCellType().equals(DataCell.CellType.SPLITTER) || item.getCellType().equals(DataCell.CellType.READ_ONLY)){
			this.setEditable(false);
		}

		// Style
		final ObservableList<String> css = getStyleClass();
		if (css.size() == 1) {
			css.set(0, item.getStyleCss());
		}else{
			css.clear();
			css.add(item.getStyleCss());
		}

	}

	public void show(){
		if (getItem() != null){
			show(getItem());
		}
	}
	
	/***************************************************************************
     *                                                                         *
     * Protected Methods                                                          *
     *                                                                         *
     **************************************************************************/
	@Override
	protected Skin<?> createDefaultSkin() {
		return new TableCellSkin<>(this);
	}
	
	/***************************************************************************
     *                                                                         *
     * Private Methods                                                          *
     *                                                                         *
     **************************************************************************/
	/**
	 * Method that will select all the cells between the drag place and that cell.
	 * @param e
	 */
	private void dragSelect(MouseEvent e) {

		// If the mouse event is not contained within this tableCell, then
		// we don't want to react to it.
		if (! this.contains(e.getX(), e.getY())) {
			return;
		}

		final TableView<DataRow> tableView = getTableView();
		if (tableView == null) {
			return;
		}

		final int count = tableView.getItems().size();
		if (getIndex() >= count) {
			return;
		}

		final TableSelectionModel<DataRow> sm = tableView.getSelectionModel();
		if (sm == null) {
			return;
		}

		final int row = getIndex();
		final int column = tableView.getVisibleLeafIndex(getTableColumn());
		// For spanned Cells
		final DataCell<?> cell = (DataCell<?>) getItem();
		final int rowCell = cell.getRow()+cell.getRowSpan()-1;
		final int columnCell = cell.getColumn()+cell.getColumnSpan()-1;

		final TableViewFocusModel<?> fm = tableView.getFocusModel();
		if (fm == null) {
			return;
		}

		final TablePositionBase<?> focusedCell = fm.getFocusedCell();
		final MouseButton button = e.getButton();
		if (button == MouseButton.PRIMARY) {
			// we add all cells/rows between the current selection focus and
			// this cell/row (inclusive) to the current selection.
			final TablePositionBase<?> anchor = getAnchor(tableView, focusedCell);

			// and then determine all row and columns which must be selected
			int minRow = Math.min(anchor.getRow(), row);
			minRow = Math.min(minRow, rowCell);
			int maxRow = Math.max(anchor.getRow(), row);
			maxRow = Math.max(maxRow, rowCell);
			int minColumn = Math.min(anchor.getColumn(), column);
			minColumn = Math.min(minColumn, columnCell);
			int maxColumn = Math.max(anchor.getColumn(), column);
			maxColumn = Math.max(maxColumn, columnCell);

			// clear selection, but maintain the anchor
			sm.clearSelection();

			// and then perform the selection
			for (int _row = minRow; _row <= maxRow; _row++) {
				for (int _col = minColumn; _col <= maxColumn; _col++) {
					sm.select(_row, tableView.getVisibleLeafColumn(_col));
				}
			}
		}

	}
}
