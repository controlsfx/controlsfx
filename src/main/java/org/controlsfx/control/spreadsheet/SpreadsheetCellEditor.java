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

import impl.org.controlsfx.spreadsheet.GridViewSkin;
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

import org.controlsfx.property.editor.PropertyEditor;

/**
 * 
 * SpreadsheetCellEditor are used by {@link SpreadsheetCell} in order to control how each value will be entered.
 * <br/>
 * 
 * <h3>General behavior: </h3>
 * Editors will be displayed if the user double-click or press enter in an editable cell ( see {@link SpreadsheetCell#setEditable(boolean)} ).
 * <br/>
 * If the user does anything outside the editor, the editor <b> will be forced </b> to cancel the edition and close itself. 
 * Each editor has its own policy regarding validation of the value entered. This policy is
 * define by each editor in the {@link #validateEdit()} method.
 *  If the value doesn't meet the requirements when saving the cell, nothing happens and the editor keeps editing.
 * <br/>
 * You can abandon a current modification by pressing "esc" key. 
 * <br/>
 * 
 * <h3>Specific behavior: </h3>
 * This class offers some static methods in order to create a {@link SpreadsheetCellEditor}. Here are their properties:
 * <br/>
 * 
 * <ul>
 *   <li> {@link #createTextEditor()}: Basic {@link TextField}, can accept all data and save it as a string.</li>
 *   <li> {@link #createListEditor(List)}: Display a {@link ComboBox} with the different values.</li>
 *   <li> {@link #createDoubleEditor()}: Display a {@link TextField} which accepts only double value. If the entered value is incorrect,
 *   the background will turn red so that the user will know in advance if the data will be saved or not.</li>
 *   <li> {@link #createDateEditor()}: Display a {@link DatePicker}.</li>
 *   <li> {@link #createObjectEditor()}: Display a {@link TextField}, accept an Object.</li>
 * </ul>
 * 
 * <br/>
 * <h3>Creating your editor: </h3>
 * You can of course create your own {@link SpreadsheetCellEditor} if you want to control more closely 
 * what is happening or simply for displaying other controls.<br/>
 * 
 * You just have to override the three abstract methods. <b>Remember</b> that you will never call those
 * methods directly. They will be called by the {@link SpreadsheetView} when the time comes.
 * <ul>
 *   <li> {@link #startEdit()}: You can instantiate your own control.</li>
 *   <li> {@link #validateEdit()}: You can decide whether the value entered is valid for you.</li>
 *   <li> {@link #end()}: When editing is finished, you can properly close your own control.</li>
 * </ul>
 * <br/>
 * Keep in mind that you will interact only with {@link #endEdit(boolean)} where a <b>true</b> value
 * means you want to commit, and a <b>false</b> means you want to cancel. The {@link SpreadsheetView}
 * will handle all the rest for you and call your methods at the right moment.
 * <br/>
 * 
 * <h3>Visual: </h3>
 * <table style="border: 1px solid gray;">
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>String</strong></td>
 *     <td><center><img src="textEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>List</strong></td>
 *     <td><center><img src="listEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Double</strong></td>
 *     <td><center><img src="doubleEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Date</strong></td>
 *     <td><center><img src="dateEditor.png"></center></td>
 *   </tr>
 *  </table>
 * 
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCell
 */
public abstract class SpreadsheetCellEditor<T> implements PropertyEditor<T>  {
	SpreadsheetView view;
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Construct the SpreadsheetCellEditor.
     */
    public SpreadsheetCellEditor(SpreadsheetView view) {
    	this.view = view;
    }

    /***************************************************************************
     * * Public Final Methods * *
     **************************************************************************/
    /**
     * Return the {@link SpreadsheetCell#getItem()} associated with the editor.
     */
    @Override
    public final T getValue() {
    	SpreadsheetCell cell = (SpreadsheetCell) view.getCellsViewSkin().getSpreadsheetCellEditorImpl().getModelCell();
        return cell == null ? null : (T) cell.getItem();
    }
    
    @Override
    public final void setValue(T value) {
        SpreadsheetCell cell = (SpreadsheetCell) view.getCellsViewSkin().getSpreadsheetCellEditorImpl().getModelCell();
        if (cell != null) {
    	   cell.setItem(value);
        }
    }

    /**
     * Return the {@link SpreadsheetCell#getProperties()} associated with
     * the string in parameter.
     * @param key The key which has a Object associated with in {@link SpreadsheetCell#getProperties()}
     * @return
     */
    public final Object getProperties(String key){
    	return view.getCellsViewSkin().getSpreadsheetCellEditorImpl().getModelCell().getProperties().get(key);
    }
    
    /**
     * Whenever you want to stop the edition, you call that method.<br/>
     * True means you're trying to commit the value, then {@link #validateEdit()}
     * will be called in order to verify that the value is correct.<br/>
     * False means you're trying to cancel the value and it will be follow by {@link #end()}.<br/>
     * See SpreadsheetCellEditor description
     * @param b true means commit, false means cancel
     */
    public final void endEdit(boolean b){
    	view.getCellsViewSkin().getSpreadsheetCellEditorImpl().endEdit(b);
    }

    
    /***************************************************************************
     * * Public Abstract Methods * *
     **************************************************************************/
    /**
     * This method will be called when edition start.<br/>
     * You will then do all the configuration of your editor.
     */
    public abstract void startEdit();
    
    /**
     * This method will be called when a commit is happening.<br/>
     * You will then compute the value of the editor in order to determine
     * if the current value is valid.
     * @return null if not valid or the correct value otherwise.
     */
    public abstract T validateEdit();
	
    /**
     * This method will be called at the end of edition.<br/>
     * You will be offered the possibility to do the configuration
     * post editing.
     */
    public abstract void end();

	/**
	 * 
	 * Specialization of the {@link SpreadsheetCellEditor} Class. It displays a
	 * {@link TextField} where the user can type different values.
	 */
	public static SpreadsheetCellEditor<Object> createObjectEditor(SpreadsheetView view) {
		return new SpreadsheetCellEditor<Object>(view) {

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
				Object value = getValue();
				if (value != null) {
					tf.setText(value.toString());
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
	 * {@link TextField} where the user can type different values.
	 */
	public static SpreadsheetCellEditor<String> createTextEditor(SpreadsheetView view) {
		return new SpreadsheetCellEditor<String>(view) {

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
				String value = getValue();
				if (value != null) {
					tf.setText(value);
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
	 */
	public static SpreadsheetCellEditor<Double> createDoubleEditor(SpreadsheetView view) {
		return new SpreadsheetCellEditor<Double>(view) {

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
	public static SpreadsheetCellEditor<String> createListEditor(SpreadsheetView view, final List<String> itemList) {
		return new SpreadsheetCellEditor<String>(view) {
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
					ObservableList<String> items = FXCollections.observableList(itemList);
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
	public static SpreadsheetCellEditor<LocalDate> createDateEditor(SpreadsheetView view) {
		return new SpreadsheetCellEditor<LocalDate>(view) {

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
