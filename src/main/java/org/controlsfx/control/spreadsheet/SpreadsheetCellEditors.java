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
package org.controlsfx.control.spreadsheet;

import java.time.LocalDate;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * This class offers some static methods in order to create specialized 
 * {@link SpreadsheetCellEditor} object.
 * <br/><br/>
 * 
 * Since the editors are tightly linked with the {@link SpreadsheetCell}, you have
 * four pre-built editors corresponding to the four {@link CellType}.
 * 
 *
 *@see SpreadsheetCellEditor
 *@see SpreadsheetCell
 *@see SpreadsheetView
 */
public class SpreadsheetCellEditors {

	private SpreadsheetCellEditors() {
		// no-op
	}

	/**
	 * 
	 * Specialization of the {@link SpreadsheetCellEditor} Class. It displays a
	 * {@link TextField} where the user can type different values.
	 */
	public static SpreadsheetCellEditor<String> createTextEditor() {
		return new SpreadsheetCellEditor<String>() {

			/***************************************************************************
			 * * Private Fields * *
			 **************************************************************************/
			private final TextField tf;

			/***************************************************************************
			 * * Constructor * *
			 **************************************************************************/
			{
				tf = new TextField();
				tf.setPrefHeight(20);
			}

			/***************************************************************************
			 * * Public Methods * *
			 **************************************************************************/
			@Override
			public void startEdit() {
				if (getValue() != null) {
					tf.setText(getValue());
				}
				tf.setMaxHeight(20);
				attachEnterEscapeEventHandler();

				tf.requestFocus();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				tf.setOnKeyPressed(null);
			}

			@Override
			public String validateEdit() {
				return tf.getText();
			}

			@Override
			public TextField getEditor() {
				return tf;
			}

			private void attachEnterEscapeEventHandler() {
				tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						if (t.getCode() == KeyCode.ENTER) {
							endEdit(true);
						} else if (t.getCode() == KeyCode.ESCAPE) {
							endEdit(false);
						}
					}
				});
			}
		};
	}

	/**
	 * 
	 * Specialization of the {@link SpreadsheetCellEditor} Class. It displays a
	 * {@link TextField} where the user can type different numbers. Only numbers will be
	 * stored. 
	 * <br/>
	 * Moreover, the {@link TextField} will turn red if the value currently entered if incorrect.
	 * @see SpreadsheetCellEditor
	 */
	public static SpreadsheetCellEditor<Double> createDoubleEditor() {
		return new SpreadsheetCellEditor<Double>() {

			/***************************************************************************
			 * * Private Fields * *
			 **************************************************************************/
			private final TextField tf;

			/***************************************************************************
			 * * Constructor * *
			 **************************************************************************/
			{
				tf = new TextField();
				tf.setPrefHeight(20);
			}

			/***************************************************************************
			 * * Public Methods * *
			 **************************************************************************/
			@Override
			public void startEdit() {
				if (getValue() != null) {
					tf.setText(getValue().toString());
				}
				tf.getStyleClass().removeAll("error");
				tf.setMaxHeight(20);
				attachEnterEscapeEventHandler();

				tf.requestFocus();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				tf.setOnKeyPressed(null);
			}

			@Override
			public Double validateEdit() {
				try{
					Double temp = Double.parseDouble(tf.getText());
					return temp;
				}catch(Exception e){
					return null;
				}
			}

			@Override
			public TextField getEditor() {
				return tf;
			}

			private void attachEnterEscapeEventHandler() {
				tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						if (t.getCode() == KeyCode.ENTER) {
							endEdit(true);
						} else if (t.getCode() == KeyCode.ESCAPE) {
							endEdit(false);
						}
					}
				});
				tf.setOnKeyReleased(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						try{
							if(tf.getText().equals("")){
								tf.getStyleClass().removeAll("error");
							}else{
								Double.parseDouble(tf.getText());
								tf.getStyleClass().removeAll("error");
							}
						}catch(Exception e){
							tf.getStyleClass().add("error");
						}
					}
				});
			}
		};
	}

	/**
	 * 
	 * Specialization of the {@link SpreadsheetCellEditor} Class. It displays a
	 * {@link ComboBox} where the user can choose a date through a visual calendar.
	 * The user can also type the date directly in the expected format (DD/MM/YYYY).
	 */
	public static SpreadsheetCellEditor<String> createListEditor() {
		return new SpreadsheetCellEditor<String>() {
			/***************************************************************************
			 * * Private Fields * *
			 **************************************************************************/
			private final ComboBox<String> cb;
			private ChangeListener<Number> cl;

			/***************************************************************************
			 * * Constructor * *
			 **************************************************************************/
			{
				cb = new ComboBox<String>();
				cb.setVisibleRowCount(3);

			}

			/***************************************************************************
			 * * Public Methods * *
			 **************************************************************************/

			@Override
			public void startEdit() {
				//                super.startEdit();
				if (getValue() != null) {
					ObservableList<String> items = FXCollections.observableList((List<String>) getProperties("items"));
					cb.setItems(items);
					cb.setValue(getValue());
				}
				attachEnterEscapeEventHandler();

				cb.requestFocus();
			}


			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				cb.getSelectionModel().selectedIndexProperty()
				.removeListener(cl);
				cb.setOnKeyPressed(null);
				cl = null;
			}

			@Override
			public String validateEdit() {
				if (cb.getSelectionModel().getSelectedIndex() != -1) {
					return cb.getSelectionModel().getSelectedItem();
				}
				return null;
			}

			@Override
			public ComboBox<String> getEditor() {
				return cb;
			}

			private void attachEnterEscapeEventHandler() {
				cl = new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> ov,
							Number t, Number t1) {
						endEdit(true);
					}
				};
				cb.getSelectionModel().selectedIndexProperty().addListener(cl);
				cb.setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						if (t.getCode() == KeyCode.ESCAPE) {
							endEdit(false);
						}
					}
				});
			}
		};
	}

	/**
	 * 
	 * Specialization of the {@link SpreadsheetCellEditor} Class. It displays a
	 * {@link DatePicker}.
	 */
	public static SpreadsheetCellEditor<LocalDate> createDateEditor() {
		return new SpreadsheetCellEditor<LocalDate>() {

			/***************************************************************************
			 * * Private Fields * *
			 **************************************************************************/
			private final DatePicker datePicker;
			private EventHandler<KeyEvent> eh;

			/***************************************************************************
			 * * Constructor * *
			 **************************************************************************/

			{
				datePicker = new DatePicker();
			}

			/***************************************************************************
			 * * Public Methods * *
			 **************************************************************************/
			@Override
			public void startEdit() {
				datePicker.setValue(getValue());
				attachEnterEscapeEventHandler();

				datePicker.getEditor().requestFocus();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {

				if (datePicker.isShowing()) {
					datePicker.hide();
				}

				datePicker.removeEventFilter(KeyEvent.KEY_PRESSED, eh);
			}

			@Override
			public LocalDate validateEdit() {
				return datePicker.getValue();
			}

			@Override
			public DatePicker getEditor() {
				return datePicker;
			}

			private void attachEnterEscapeEventHandler() {
				/**
				 * We need to add an EventFilter because otherwise the
				 * DatePicker will block "escape" and "enter". But when "enter"
				 * is hit, we need to runLater the commit because the value has
				 * not yet hit the DatePicker itself.
				 */
				eh = new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent t) {
						if (t.getCode() == KeyCode.ENTER) {
							final Runnable r = new Runnable() {
								@Override
								public void run() {
									endEdit(true);
								}
							};
							Platform.runLater(r);
						} else if (t.getCode() == KeyCode.ESCAPE) {
							endEdit(false);
						}
					}
				};

				datePicker.addEventFilter(KeyEvent.KEY_PRESSED, eh);
			}
		};
	}
}
