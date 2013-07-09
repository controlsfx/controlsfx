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

import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.model.DataCell;


/**
 *
 * Specialization of the Editor Class.
 * It displays a comboBox (list) where the user can choose a value.
 */
public class ListEditor extends Editor {

	private final ComboBox<String> cb;
	private ChangeListener<Number> cl;
	private InvalidationListener cl2;

	public ListEditor() {
		cb = new ComboBox<String>();
		cb.setVisibleRowCount(3);

		//TODO Modify this properly
		// We don't want the list to display out of the spreadsheetView
		cb.showingProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
				if (t1) {
					if (spreadsheetView.getLayoutBounds().getMaxX() < cb.getLocalToSceneTransform().getTx() + cb.getWidth()) {
						cb.setLayoutX(cb.getParent().getLayoutX());
					}else if(cb.getLocalToSceneTransform().getTx() <0 ){
						cb.setLayoutX(cb.getLayoutX() + Math.abs(cb.getLocalToSceneTransform().getTx()));
					}
					if(spreadsheetView.getLayoutBounds().getMaxY() < cb.getLocalToSceneTransform().getTy()) {
						cb.setLayoutY(spreadsheetView.getLayoutBounds().getMaxY()-50);// Modify the "50" here
					}else if(cb.getLocalToSceneTransform().getTy() <0 ){
						cb.setLayoutY(cb.getLayoutY()+Math.abs(cb.getLocalToSceneTransform().getTy()));
					}
				}
			}
		});
	}

	@Override
	public void begin(DataCell<?> cell, SpreadsheetCell gc) {
		if (gc != null) {
			this.cell = cell;
			this.gc = gc;
			final List<String> temp = (List<String>) cell.getCellValue();
			final ObservableList<String> temp2 = FXCollections.observableList(temp);
			cb.setItems(temp2);
			cb.setValue(cell.getStr());

			cb.setPrefWidth(spreadsheetView.getCellPrefWidth());
			cb.setMinWidth(spreadsheetView.getCellPrefWidth());

			attachEnterEscapeEventHandler();
		}
	}

	@Override
	public void end() {
		cb.getSelectionModel().selectedIndexProperty().removeListener(cl);
		cb.setOnKeyPressed(null);
		gc.selectedProperty().removeListener(cl2);
		this.cell = null;
		this.gc = null;
		cl = null;
		cl2 = null;
	}

	@Override
	public DataCell<?> commitEdit() {
		if (cb.getSelectionModel().getSelectedIndex() != -1) {
			this.cell.setStr(cb.getItems().get(cb.getSelectionModel().getSelectedIndex()));
		}
		return cell;
	}

	@Override
	public void cancelEdit() {
		end();
	}

	@Override
	public Control getControl() {
		return cb;
	}

	@Override
	public void attachEnterEscapeEventHandler() {
		cl = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
				commitEdit();
				gc.commitEdit(cell);
				end();
			}
		};
		cb.getSelectionModel().selectedIndexProperty().addListener(cl);
		cb.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					gc.cancelEdit();
					cancelEdit();
				}
			}
		});
		cl2 = new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				if (gc != null && gc.isEditing()) {
					commitEdit();
					gc.commitEdit(cell);
				}
				end();
			}
		};
		gc.selectedProperty().addListener(cl2);
	}

	@Override
	public void startEdit() {
		gc.setGraphic(cb);
		if (spreadsheetView.isEdit()) {
			cb.show();
			spreadsheetView.setEdit(false);
		}
		final Runnable r = new Runnable() {
			@Override
			public void run() {
				cb.requestFocus();
			}
		};
		Platform.runLater(r);
	}

	public void show() {
		cb.show();
	}
}
