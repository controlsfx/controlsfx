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
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
                super.startEdit();
                tf.setMaxHeight(20);
                attachEnterEscapeEventHandler();

                // If the SpreadsheetCell is deselected, we commit.
                // Sometimes, when you you touch the scrollBar when editing,
                // this is called way
                // too late and the SpreadsheetCell is null, so we need to be
                // careful.
                il = new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {

                        if (viewCell != null && viewCell.isEditing()) {
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                        }
                        end();

                    }
                };

                viewCell.selectedProperty().addListener(il);
                viewCell.setGraphic(tf);

                tf.requestFocus();
            }

            /***************************************************************************
             * * Protected Methods * *
             **************************************************************************/

            @Override
            public void updateDataCell(SpreadsheetCell<String> cell) {
                super.updateDataCell(cell);

                if (cell != null) {
                    tf.setText(cell.getText());
                }
            }

            @Override
            protected void end() {
                super.end();
                if (viewCell != null) {
                    viewCell.selectedProperty().removeListener(il);
                }

                tf.setOnKeyPressed(null);
                this.modelCell = null;
                this.viewCell = null;
                il = null;
            }

            @Override
            protected SpreadsheetCell<String> commitEdit() {
                this.modelCell.setItem(this.modelCell.getConverter().fromString(tf.getText()));
                return modelCell;
            }

            @Override
            protected void cancelEdit() {
                end();
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
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                            end();
                        } else if (t.getCode() == KeyCode.ESCAPE) {
                            viewCell.cancelEdit();
                            cancelEdit();
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
                super.startEdit();
                tf.getStyleClass().removeAll("error");
                tf.setMaxHeight(20);
                attachEnterEscapeEventHandler();

                // If the SpreadsheetCell is deselected, we commit.
                // Sometimes, when you you touch the scrollBar when editing,
                // this is called way
                // too late and the SpreadsheetCell is null, so we need to be
                // careful.
                il = new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {

                        if (viewCell != null && viewCell.isEditing()) {
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                        }
                        end();

                    }
                };

                viewCell.selectedProperty().addListener(il);
                viewCell.setGraphic(tf);

                tf.requestFocus();
            }

            /***************************************************************************
             * * Protected Methods * *
             **************************************************************************/

            @Override
            public void updateDataCell(SpreadsheetCell<Double> cell) {
                super.updateDataCell(cell);

                if (cell != null) {
                    tf.setText(cell.getText());
                }
            }

            @Override
            protected void end() {
                super.end();
                if (viewCell != null) {
                    viewCell.selectedProperty().removeListener(il);
                }

                tf.setOnKeyPressed(null);
                this.modelCell = null;
                this.viewCell = null;
                il = null;
            }

            @Override
            protected SpreadsheetCell<Double> commitEdit() {
            	try{
            		Double temp = Double.parseDouble(tf.getText());
            		 this.modelCell.setItem(temp);
            	}catch(Exception e){
            		
            	}
               
                return modelCell;
            }

            @Override
            protected void cancelEdit() {
                end();
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
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                            end();
                        } else if (t.getCode() == KeyCode.ESCAPE) {
                            viewCell.cancelEdit();
                            cancelEdit();
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

                // TODO Modify this properly
                // We don't want the list to display out of the spreadsheetView
                cb.showingProperty().addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> ov,
                            Boolean t, Boolean t1) {
                        if (t1) {
                            if (spreadsheetView.getLayoutBounds().getMaxX() < cb
                                    .getLocalToSceneTransform().getTx()
                                    + cb.getWidth()) {
                                cb.setLayoutX(cb.getParent().getLayoutX());
                            } else if (cb.getLocalToSceneTransform().getTx() < 0) {
                                cb.setLayoutX(cb.getLayoutX()
                                        + Math.abs(cb
                                                .getLocalToSceneTransform()
                                                .getTx()));
                            }
                            if (spreadsheetView.getLayoutBounds().getMaxY() < cb
                                    .getLocalToSceneTransform().getTy()) {
                                cb.setLayoutY(spreadsheetView.getLayoutBounds()
                                        .getMaxY() - 50);// Modify the "50" here
                            } else if (cb.getLocalToSceneTransform().getTy() < 0) {
                                cb.setLayoutY(cb.getLayoutY()
                                        + Math.abs(cb
                                                .getLocalToSceneTransform()
                                                .getTy()));
                            }
                        }
                    }
                });
            }

            /***************************************************************************
             * * Public Methods * *
             **************************************************************************/

            @Override
            public void startEdit() {
                super.startEdit();

                attachEnterEscapeEventHandler();
                if (viewCell != null) {
                    viewCell.setGraphic(cb);

                cb.requestFocus();
                }
            }

            /***************************************************************************
             * * Protected Methods * *
             **************************************************************************/

            @Override
            public void updateDataCell(SpreadsheetCell<String> cell) {
                super.updateDataCell(cell);

                if (cell != null) {
                    ObservableList<String> items = FXCollections.observableList((List<String>) cell.getProperties().get("items"));
                    cb.setItems(items);
                    cb.setValue(cell.getText());

//                    cb.setPrefWidth(spreadsheetView.getCellPrefWidth());
//                    cb.setMinWidth(spreadsheetView.getCellPrefWidth());
                }
            }

            @Override
            protected void end() {
                super.end();

                cb.getSelectionModel().selectedIndexProperty()
                        .removeListener(cl);
                cb.setOnKeyPressed(null);
                if (viewCell != null) viewCell.selectedProperty()
                        .removeListener(il);

                this.modelCell = null;
                this.viewCell = null;
                cl = null;
                il = null;
            }

            @Override
            protected SpreadsheetCell<String> commitEdit() {
                if (cb.getSelectionModel().getSelectedIndex() != -1) {
                    this.modelCell.setItem(this.modelCell.getConverter().fromString(cb.getSelectionModel().getSelectedItem()));
                    return modelCell;
                }
                return modelCell;
            }

            @Override
            protected void cancelEdit() {
                end();
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
                        commitEdit();
                        viewCell.commitEdit(modelCell);
                        end();
                    }
                };
                cb.getSelectionModel().selectedIndexProperty().addListener(cl);
                cb.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent t) {
                        if (t.getCode() == KeyCode.ESCAPE) {
                            viewCell.cancelEdit();
                            cancelEdit();
                        }
                    }
                });
                il = new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        if (viewCell != null && viewCell.isEditing()) {
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                        }
                        end();
                    }
                };
                if (viewCell == null) {
                    end();
                } else {
                    viewCell.selectedProperty().addListener(il);
                }
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
                super.startEdit();

                attachEnterEscapeEventHandler();

                // If the GridCell is deselected, we commit.
                // Sometimes, when you you touch the scrollBar when editing,
                // this is called way
                // too late and the GridCell is null, so we need to be careful.
                il = new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {

                        if (viewCell != null && viewCell.isEditing()) {
                            commitEdit();
                            viewCell.commitEdit(modelCell);
                        }
                        end();
                    }
                };

                viewCell.selectedProperty().addListener(il);

                viewCell.setGraphic(datePicker);

                datePicker.getEditor().requestFocus();
            }

            /***************************************************************************
             * * Protected Methods * *
             **************************************************************************/

            @Override
            public void updateDataCell(SpreadsheetCell<LocalDate> cell) {
                super.updateDataCell(cell);
                datePicker.setValue(cell.getItem());
            }

            @Override
            protected void end() {
                super.end();

                if (viewCell != null) {
                    viewCell.selectedProperty().removeListener(il);
                }

                if (datePicker.isShowing()) {
                    datePicker.hide();
                }

                datePicker.removeEventFilter(KeyEvent.KEY_PRESSED, eh);
                this.modelCell = null;
                this.viewCell = null;
                il = null;
            }

            @Override
            protected SpreadsheetCell<LocalDate> commitEdit() {
                final SpreadsheetCell<LocalDate> temp = (SpreadsheetCell<LocalDate>) this.modelCell;

                temp.setItem(datePicker.getValue());
                return modelCell;
            }

            @Override
            protected void cancelEdit() {
                end();
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
                                	if (viewCell != null && viewCell.isEditing()) {
	                                    commitEdit();
	                                    viewCell.commitEdit(modelCell);
	                                    end();
                                	}
                                }
                            };
                            Platform.runLater(r);
                        } else if (t.getCode() == KeyCode.ESCAPE) {
                            viewCell.cancelEdit();
                            cancelEdit();
                        }
                    }
                };

                datePicker.addEventFilter(KeyEvent.KEY_PRESSED, eh);
            }
        };
    }
}
