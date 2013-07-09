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
package org.controlsfx.control.spreadsheet.behavior;

import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.spreadsheet.model.DataCell;

import com.sun.javafx.scene.control.behavior.TableCellBehavior;
/**
 * The basic behavior of a SpreadsheetCell, especially some listener to handle the selection
 * with the mouse or the keyboard.
 * 
 *
 * @param <S>
 * @param <T>
 */
public class GridCellBehavior<S,T> extends TableCellBehavior<S,T>{

	public GridCellBehavior(TableCell control) {
		super(control);

		control.setOnMouseDragEntered(new EventHandler<MouseEvent>(){
			@Override
			public void handle(MouseEvent arg0) {
				dragSelect(arg0);
			}});
	}

	/** @{@inheritDoc} */
	@Override protected int getVisibleLeafIndex(TableColumnBase tc) {
		return getControl().getTableView().getVisibleLeafColumns().indexOf(tc);
	}
	protected void dragSelect(MouseEvent e) {
		// Note that table.select will reset selection
		// for out of bounds indexes. So, need to check
		final IndexedCell<?> tableCell = getControl();

		// If the mouse event is not contained within this tableCell, then
		// we don't want to react to it.
		if (! tableCell.contains(e.getX(), e.getY())) {
			return;
		}

		final Control tableView = getTableControl();
		if (tableView == null) {
			return;
		}

		final int count = getItemCount();
		if (tableCell.getIndex() >= count) {
			return;
		}

		final TableSelectionModel sm = getSelectionModel();
		if (sm == null) {
			return;
		}

		final int row = tableCell.getIndex();
		final int column = getColumn();
		// For spanned Cells
		final DataCell<?> cell = (DataCell<?>) tableCell.getItem();
		final int rowCell = cell.getRow()+cell.getRowSpan()-1;
		final int columnCell = cell.getColumn()+cell.getColumnSpan()-1;

		final TableFocusModel<?, ?> fm = getFocusModel();
		if (fm == null) {
			return;
		}

		final TablePositionBase<?> focusedCell = getFocusedCell();
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
					sm.select(_row, getVisibleLeafColumn(_col));
				}
			}
		}

	}
	/** @{@inheritDoc} */
	TableView getTableControl() {
		return getControl().getTableView();
	}

	/** @{@inheritDoc} */
	TableColumn getTableColumn() {
		return getControl().getTableColumn();
	}

	/** @{@inheritDoc} */
	int getItemCount() {
		return getTableControl().getItems().size();
	}

	/** @{@inheritDoc} */
	TableView.TableViewSelectionModel getSelectionModel() {
		return getTableControl().getSelectionModel();
	}

	/** @{@inheritDoc} */
	TableViewFocusModel getFocusModel() {
		return getTableControl().getFocusModel();
	}

	/** @{@inheritDoc} */
	TablePositionBase getFocusedCell() {
		return getTableControl().getFocusModel().getFocusedCell();
	}

	/** @{@inheritDoc} */
	boolean isTableRowSelected() {
		return getControl().getTableRow().isSelected();
	}

	/** @{@inheritDoc} */
	TableColumnBase<?, ?> getVisibleLeafColumn(int index) {
		return getTableControl().getVisibleLeafColumn(index);
	}



	/** @{@inheritDoc} */
	void focus(int row, TableColumnBase tc) {
		getFocusModel().focus(row, (TableColumn)tc);
	}

	/** @{@inheritDoc} */
	void edit(int row, TableColumnBase tc) {
		getTableControl().edit(row, (TableColumn)tc);
	}

	private int getColumn() {
		if (getSelectionModel().isCellSelectionEnabled()) {
			final TableColumnBase<?, ?> tc = getTableColumn();
			return getVisibleLeafIndex(tc);
		}

		return -1;
	}

	private static final String ANCHOR_PROPERTY_KEY = "table.anchor";

	static TablePositionBase<?> getAnchor(Control table, TablePositionBase<?> focusedCell) {
		return hasAnchor(table) ?
				(TablePositionBase<?>) table.getProperties().get(ANCHOR_PROPERTY_KEY) :
					focusedCell;
	}
	static boolean hasAnchor(Control table) {
		return table.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
	}
}
