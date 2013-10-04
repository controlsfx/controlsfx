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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


/**
 * 
 * SpreadsheetCellEditor are used by {@link SpreadsheetCellType} and {@link SpreadsheetCell} in order to control how each value will be entered.
 * <br/>
 * 
 * <h3>General behavior: </h3>
 * Editors will be displayed if the user double-click or press enter in an editable cell ( see {@link SpreadsheetCell#setEditable(boolean)} ).
 * <br/>
 * If the user does anything outside the editor, the editor <b> will be forced </b> to cancel the edition and close itself. 
 * The editor is just here to allow communication between the user and the {@link SpreadsheetView}. It will just be
 * given a value, and it will just give back another one after. The policy regarding validation of a given value is 
 * define in {@link SpreadsheetCellType#convertValue(String)}.
 * 
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
 *   <li> {@link #createTextEditor(SpreadsheetView)}: Basic {@link TextField}, can accept all data and save it as a string.</li>
 *   <li> {@link #createListEditor(SpreadsheetView, List)}: Display a {@link ComboBox} with the different values.</li>
 *   <li> {@link #createDoubleEditor(SpreadsheetView)}: Display a {@link TextField} which accepts only double value. If the entered value is incorrect,
 *   the background will turn red so that the user will know in advance if the data will be saved or not.</li>
 *   <li> {@link #createDateEditor(SpreadsheetView)}: Display a {@link DatePicker}.</li>
 *   <li> {@link #createObjectEditor(SpreadsheetView)}: Display a {@link TextField}, accept an Object.</li>
 * </ul>
 * 
 * <br/>
 * <h3>Creating your editor: </h3>
 * You can of course create your own {@link SpreadsheetCellEditor} for displaying other controls.<br/>
 * 
 * You just have to override the four abstract methods. <b>Remember</b> that you will never call those
 * methods directly. They will be called by the {@link SpreadsheetView} when the time comes.
 * <ul>
 *   <li> {@link #startEdit(Object)}: You will configure your control with the given value which is {@link SpreadsheetCell#getItem()}
 *   converted to an object. You do not instantiate your control here, you do it in the constructor.</li>
 *   <li> {@link #getEditor()}: You will return which control you're using (for display).</li>
 *   <li> {@link #getControlValue()}: You will return the value inside your editor in order to submit it for validation.</li>
 *   <li> {@link #end()}: When editing is finished, you can properly close your own control.</li>
 * </ul>
 * <br/>
 * Keep in mind that you will interact only with {@link #endEdit(boolean)} where a <b>true</b> value
 * means you want to commit, and a <b>false</b> means you want to cancel. The {@link SpreadsheetView}
 * will handle all the rest for you and call your methods at the right moment.
 * <br/>
 * 
 * <h3>Use case : </h3>
 * <td><center><img src="editorScheme.png"></center></td>
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
public abstract class SpreadsheetCellEditor<T>{
	SpreadsheetView view;
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

	/**
	 * Construct the SpreadsheetCellEditor.
	 * @param view
	 */
    public SpreadsheetCellEditor(SpreadsheetView view) {
    	this.view = view;
    }

    /***************************************************************************
     * * Public Final Methods * *
     **************************************************************************/
    /**
     * Whenever you want to stop the edition, you call that method.<br/>
     * True means you're trying to commit the value, then {@link SpreadsheetCellType#convertValue(String)}
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
     * @param item
     */
    public abstract void startEdit(Object item);
    
    /**
     * Return the control used for controlling the input.
     * This is called at the beginning in order to display your control
     * in the cell.
     * @return the control used.
     */
    public abstract Control getEditor();
   
    /**
     * Return the value within your editor as a string.
     * This will be used by the {@link SpreadsheetCellType#convertValue(String)}
     * in order to compute whether the value is valid regarding
     * the {@link SpreadsheetCellType} policy.
     * @return the value within your editor as a string.
     */
    public abstract String getControlValue();
	
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
			public void startEdit(Object value) {
				if (value instanceof String) {
					tf.setText(value.toString());
				}
				tf.setMaxHeight(20);
				attachEnterEscapeEventHandler();

				tf.requestFocus();
				tf.end();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				tf.setOnKeyPressed(null);
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

			@Override
			public String getControlValue() {
				return tf.getText();
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
			public void startEdit(Object value) {
				
				if (value instanceof String) {
					tf.setText((String)value);
				}
				tf.setMaxHeight(20);
				attachEnterEscapeEventHandler();

				tf.requestFocus();
				tf.end();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				tf.setOnKeyPressed(null);
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

			@Override
			public String getControlValue() {
				return tf.getText();
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
			public void startEdit(Object value) {
				if(value instanceof Double){
					tf.setText(((Double) value).isNaN()?"":value.toString());
				}
				tf.getStyleClass().removeAll("error");
				tf.setMaxHeight(20);
				attachEnterEscapeEventHandler();

				tf.requestFocus();
				tf.end();
			}

			/***************************************************************************
			 * * Protected Methods * *
			 **************************************************************************/


			@Override
			public void end() {
				tf.setOnKeyPressed(null);
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

			@Override
			public String getControlValue() {
				return tf.getText();
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
			public void startEdit(Object value) {
				
				if (value instanceof String) {
					ObservableList<String> items = FXCollections.observableList(itemList);
					cb.setItems(items);
					cb.setValue(value.toString());
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


			@Override
			public String getControlValue() {
				return cb.getSelectionModel().getSelectedItem();
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
			public void startEdit(Object value) {
				if(value instanceof LocalDate){
					datePicker.setValue((LocalDate)value);
				}
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
							endEdit(true);
						} else if (t.getCode() == KeyCode.ESCAPE) {
							endEdit(false);
						}
					}
				};

				datePicker.addEventFilter(KeyEvent.KEY_PRESSED, eh);
			}

			@Override
			public String getControlValue() {
				return datePicker.getEditor().getText();
			}
		};
	}

}
