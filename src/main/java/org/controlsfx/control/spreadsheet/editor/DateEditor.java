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
package org.controlsfx.control.spreadsheet.editor;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DateCell;


/**
 *
 * Specialization of the {@link Editor} Class.
 * It displays a {@link DatePicker}.
 */
public class DateEditor extends Editor {

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private final DatePicker datePicker;
	private EventHandler<KeyEvent> eh;

	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public DateEditor() {
		super();
		datePicker = new DatePicker();
	}

	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	@Override
	public void startEdit() {
		super.startEdit();
		
		attachEnterEscapeEventHandler();

		// If the GridCell is deselected, we commit.
		// Sometimes, when you you touch the scrollBar when editing, this is called way
		// too late and the GridCell is null, so we need to be careful.
		il = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {

				if (spreadsheetCell != null && spreadsheetCell.isEditing()) {
					commitEdit();
					spreadsheetCell.commitEdit(cell);
				}
				end();
			}
		};

		spreadsheetCell.selectedProperty().addListener(il);

		spreadsheetCell.setGraphic(datePicker);

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				datePicker.requestFocus();
			}
		};
		Platform.runLater(r);
	}
	
	/***************************************************************************
     *                                                                         *
     * Protected Methods                                                       *
     *                                                                         *
     **************************************************************************/
	@Override
	protected void begin(DataCell<?> cell, SpreadsheetCell bc) {
		this.cell = cell;
		this.spreadsheetCell = bc;
		final DateCell dc = (DateCell) cell;
		datePicker.setValue(dc.getCellValue());
	}

	@Override
	protected void end() {
		super.end();
		
		if(spreadsheetCell != null) {
			spreadsheetCell.selectedProperty().removeListener(il);
		}
		
		if(datePicker.isShowing()){
			datePicker.hide();
		}
		
		datePicker.removeEventFilter(KeyEvent.KEY_PRESSED, eh);
		this.cell = null;
		this.spreadsheetCell = null;
		il = null;
	}

	@Override
	protected DataCell<?> commitEdit() {
		final DateCell temp = (DateCell) this.cell;

		temp.setCellValue(datePicker.getValue());
		return cell;
	}

	@Override
	protected void cancelEdit() {
		end();
	}

	@Override
	protected Control getControl() {
		return datePicker;
	}

	@Override
	protected void attachEnterEscapeEventHandler() {
		/**
		 * We need to add an EventFilter because otherwise the DatePicker
		 * will block "escape" and "enter".
		 * But when "enter" is hit, we need to runLater the commit because
		 * the value has not yet hit the DatePicker itself.
		 */
		eh = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ENTER) {
					final Runnable r = new Runnable() {
						@Override
						public void run() {
							commitEdit();
							spreadsheetCell.commitEdit(cell);
							end();
						}
					};
					Platform.runLater(r);
				} else if (t.getCode() == KeyCode.ESCAPE) {
					spreadsheetCell.cancelEdit();
					cancelEdit();
				}
			}
		};

		datePicker.addEventFilter(KeyEvent.KEY_PRESSED,eh);
	}
}
