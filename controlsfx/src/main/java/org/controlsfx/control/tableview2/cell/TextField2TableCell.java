/**
 * Copyright (c) 2013, 2017 ControlsFX
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
package org.controlsfx.control.tableview2.cell;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link TextField} node inside the cell.
 *
 * <p>By default, the TextField2TableCell is rendered as a {@link javafx.scene.control.Label} when not
 * being edited, and as a TextField when in editing mode. The TextField will, by
 * default, stretch to fill the entire table cell.
 * 
 * This table cell supports commit on focus lost
 *
 * @param <S> The type of the objects contained within the TableView items list.
 * @param <T> The type of the elements contained within the TableColumn.
 */
public class TextField2TableCell<S, T> extends TextFieldTableCell<S, T> {
    
    /***************************************************************************
     *                                                                         *
     * Static cell factories                                                   *
     *                                                                         *
     **************************************************************************/

    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when
     * {@link TableView#edit(int, TableColumn)} is called.
     * This method will only  work on {@link TableColumn} instances which are of
     * type String.
     *
     * This table cell supports commit on focus lost.
     *
     * @param <S> The type of the objects contained within the TableView
     * @return A {@link Callback} that can be inserted into the
     *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
     *      TableColumn, that enables textual editing of the content.
     */
    public static <S> Callback<TableColumn<S,String>, TableCell<S,String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when
     * {@link TableView#edit(int, TableColumn) } is called.
     * This method will work  on any {@link TableColumn} instance, regardless of
     * its generic type. However, to enable this, a {@link StringConverter} must
     * be provided that will convert the given String (from what the user typed
     * in) into an instance of type T. This item will then be passed along to the
     * {@link TableColumn#onEditCommitProperty()} callback.
     *
     * This table cell supports commit on focus lost.
     *
     * @param <S> The type of the objects contained within the TableView
     * @param <T> The type of the elements contained within the TableColumn
     * @param converter A {@link StringConverter} that can convert the given String
     *      (from what the user typed in) into an instance of type T.
     * @return A {@link Callback} that can be inserted into the
     *      {@link TableColumn#cellFactoryProperty() cell factory property} of a
     *      TableColumn, that enables textual editing of the content.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final StringConverter<T> converter) {
        return list -> new TextField2TableCell<>(converter);
    }

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private TextField textField;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default TextField2TableCell with a null converter. Without a
     * {@link StringConverter} specified, this cell will not be able to accept
     * input from the TextField (as it will not know how to convert this back
     * to the domain object). It is therefore strongly encouraged to not use
     * this constructor unless you intend to set the converter separately.
     * 
     * This table cell supports commit on focus lost.
     *
     */
    public TextField2TableCell() {
        this(null);
    }

    /**
     * Creates a TextField2TableCell that provides a {@link TextField} when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TableColumn instance, regardless of its generic type.
     * However, to enable this, a {@link StringConverter} must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link TableColumn#onEditCommitProperty()} callback.
     *
     * This table cell supports commit on focus lost.
     *
     * @param converter A {@link StringConverter converter} that can convert
     *      the given String (from what the user typed in) into an instance of
     *      type T.
     */
    public TextField2TableCell(StringConverter<T> converter) {
        super(converter);
        
        graphicProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (getGraphic() != null && getGraphic() instanceof TextField) {
                    textField = (TextField) getGraphic();
                    
                    // commit on focus lost
                    textField.focusedProperty().addListener((obs, ov, nv) -> {
                        if (! nv) {
                            commitEdit(converter.fromString(textField.getText()));
                        }
                    });
                    
                    // cancel with Escape, on key pressed, not released
                    textField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                        final TableView.TableViewSelectionModel<S> selectionModel = getTableView().getSelectionModel();
                        if (event.getCode() == null || selectionModel == null) {
                            return;
                        }
                        switch (event.getCode()) {
                            case ESCAPE:
                                // restore old value
                                textField.setText(converter.toString(getItem()));
                                cancelEdit();
                                event.consume();
                                break;
                            case TAB: 
                                cancelEdit();
                                event.consume();
                                if (selectionModel.isCellSelectionEnabled()) {
                                    int columnIndex = getTableView().getVisibleLeafIndex(getTableColumn());
                                    if (event.isShiftDown()) {
                                        if (columnIndex > 0) {
                                            // go to previous column
                                            selectionModel.clearAndSelect(getIndex(), getTableView().getVisibleLeafColumn(columnIndex - 1));
                                        } else if (getIndex() > 0) {
                                            // wrap to end of previous row
                                            selectionModel.clearAndSelect(getIndex() - 1, getTableView().getVisibleLeafColumn(getTableView().getVisibleLeafColumns().size() - 1));
                                        }
                                    } else {
                                        if (columnIndex + 1 < getTableView().getVisibleLeafColumns().size()) {
                                            // go to next column
                                            selectionModel.clearAndSelect(getIndex(), getTableView().getVisibleLeafColumn(columnIndex + 1));
                                        } else if (getIndex() < getTableView().getItems().size() - 1) {
                                            // wrap to start of next row
                                            selectionModel.clearAndSelect(getIndex() + 1, getTableView().getVisibleLeafColumn(0));
                                        }
                                    }
                                } else {
                                    // go to prev/next row
                                    selectionModel.clearAndSelect(event.isShiftDown() ? getIndex() - 1 : getIndex() + 1);
                                }
                                break;
                            case UP:
                                cancelEdit();
                                event.consume();
                                selectionModel.clearAndSelect(getIndex() - 1, getTableColumn());
                                break;
                            case DOWN:
                                cancelEdit();
                                event.consume();
                                selectionModel.clearAndSelect(getIndex() + 1, getTableColumn());
                                break;
                            default: 
                                break;
                        }
                    });

                    graphicProperty().removeListener(this);
                }
            }
        });
    }
    
    /** {@inheritDoc} */
    @Override public void commitEdit(T item) {
        if (! isEditing() && ! item.equals(getItem())) {
            TableView<S> table = getTableView();
            if (table != null) {
                TableColumn<S, T> column = getTableColumn();
                TableColumn.CellEditEvent<S, T> event = new TableColumn.CellEditEvent<>(table,
                        new TablePosition<>(table, getIndex(), column), TableColumn.editCommitEvent(), item);
                Event.fireEvent(column, event);
            }
        }

        super.commitEdit(item);
    }
    
}
