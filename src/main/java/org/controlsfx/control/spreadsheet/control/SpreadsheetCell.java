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
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.sponge.TableCellSkin;


/**
 *
 * The View cell that will be visible on screen.
 * It holds the DataRow and the DataCell.
 */
public class SpreadsheetCell extends TableCell<DataRow, DataCell<?>> {

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

		// Drag
		this.addEventHandler(MouseEvent.DRAG_DETECTED,new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				startFullDrag();
			}});

	}
	@Override
	protected Skin<?> createDefaultSkin() {
		return new TableCellSkin<>(this);
	}

	@Override
	public void startEdit() {
		if(!isEditable()) {
			return;
		}
		final int column = this.getTableView().getColumns().indexOf(this.getTableColumn());
		final int row = getIndex();
		//We start to edit only if the Cell is a normal Cell (aka visible).
		final SpreadsheetView spv = ((SpreadsheetRow)getTableRow()).getSpreadsheetView();
		if (spv.getSpanType(row, column) == SpreadsheetView.SpanType.NORMAL_CELL
				|| spv.getSpanType(row, column) == SpreadsheetView.SpanType.ROW_VISIBLE) {
			super.startEdit();
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			spv.getEditor(getItem(), this).startEdit();
		}
	}

	@Override
	public void commitEdit(DataCell<?> newValue) {
		if (! isEditing()) {
			return;
		}
		super.commitEdit(newValue);

		setContentDisplay(ContentDisplay.TEXT_ONLY);

		// TODO Modify because this CSS property is not in the model
		/*if(!getText().equals(newValue.getStr())){
			pseudoClassStateChanged(new PseudoClass() {
				@Override
				public String getPseudoClassName() {
					return "modified";
				}
			}, true);
		}*/
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

}
