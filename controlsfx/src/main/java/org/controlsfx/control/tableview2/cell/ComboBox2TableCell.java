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
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * A class containing a {@link TableCell} implementation that draws a
 * {@link ComboBox} node inside the cell.
 *
 * <p>By default, the ComboBox2TableCell is rendered as a {@link javafx.scene.control.Label} when not
 * being edited, and as a ComboBox when in editing mode. The ComboBox will, by
 * default, stretch to fill the entire table cell.
 *
 * <p>To create a ComboBox2TableCell, it is necessary to provide zero or more
 * items that will be shown to the user when the {@link ComboBox} menu is
 * showing. These items must be of the same type as the TableColumn.
 * 
 * This table cell supports commit on focus lost. By default the ComboBox will
 * be set as editable.
 *
 * @param <S> The type of the objects contained within the TableView items list.
 * @param <T> The type of the elements contained within the TableColumn.
 */
public class ComboBox2TableCell<S, T> extends ComboBoxTableCell<S, T> {
    
    /***************************************************************************
     *                                                                         *
     * Static cell factories                                                   *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
     * By default, the ComboBoxCell is rendered as a {@link javafx.scene.control.Label} when not
     * being edited, and as a ComboBox when in editing mode. The ComboBox will,
     * by default, stretch to fill the entire list cell.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param <S> The type of the objects contained within the TableView
     * @param <T> The type of the elements contained within the TableColumn.
     * @param items Zero or more items that will be shown to the user when the
     *      {@link ComboBox} menu is showing. These items must be of the same
     *      type as the TableColumn. Note that it is up to the developer to set
     *      {@link EventHandler event handlers} to listen to edit events in the
     *      TableColumn, and react accordingly. Methods of interest include
     *      {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
     *      {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
     *      and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
     * @return A {@link Callback} that will return a TableCell that is able to
     *      work on the type of element contained within the TableColumn.
     */
    @SafeVarargs
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final T... items) {
        return forTableColumn(null, items);
    }

    /**
     * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
     * By default, the ComboBoxCell is rendered as a {@link javafx.scene.control.Label} when not
     * being edited, and as a ComboBox when in editing mode. The ComboBox will,
     * by default, stretch to fill the entire list cell.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param <S> The type of the objects contained within the TableView
     * @param <T> The type of the elements contained within the TableColumn.
     * @param converter A {@link StringConverter} to convert the given item (of
     *      type T) to a String for displaying to the user.
     * @param items Zero or more items that will be shown to the user when the
     *      {@link ComboBox} menu is showing. These items must be of the same
     *      type as the TableColumn. Note that it is up to the developer to set
     *      {@link EventHandler event handlers} to listen to edit events in the
     *      TableColumn, and react accordingly. Methods of interest include
     *      {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
     *      {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
     *      and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
     * @return A {@link Callback} that will return a TableCell that is able to
     *      work on the type of element contained within the TableColumn.
     */
    @SafeVarargs
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final StringConverter<T> converter,
            final T... items) {
        return forTableColumn(converter, FXCollections.observableArrayList(items));
    }

    /**
     * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
     * By default, the ComboBoxCell is rendered as a {@link javafx.scene.control.Label} when not
     * being edited, and as a ComboBox when in editing mode. The ComboBox will,
     * by default, stretch to fill the entire list cell.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param <S> The type of the objects contained within the TableView
     * @param <T> The type of the elements contained within the TableColumn.
     * @param items Zero or more items that will be shown to the user when the
     *      {@link ComboBox} menu is showing. These items must be of the same
     *      type as the TableColumn. Note that it is up to the developer to set
     *      {@link EventHandler event handlers} to listen to edit events in the
     *      TableColumn, and react accordingly. Methods of interest include
     *      {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
     *      {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
     *      and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
     * @return A {@link Callback} that will return a TableCell that is able to
     *      work on the type of element contained within the TableColumn.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final ObservableList<T> items) {
        return forTableColumn(null, items);
    }

    /**
     * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
     * By default, the ComboBoxCell is rendered as a {@link javafx.scene.control.Label} when not
     * being edited, and as a ComboBox when in editing mode. The ComboBox will,
     * by default, stretch to fill the entire list cell.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param <S> The type of the objects contained within the TableView
     * @param <T> The type of the elements contained within the TableColumn.
     * @param converter A {@link StringConverter} to convert the given item (of
     *      type T) to a String for displaying to the user.
     * @param items Zero or more items that will be shown to the user when the
     *      {@link ComboBox} menu is showing. These items must be of the same
     *      type as the TableColumn. Note that it is up to the developer to set
     *      {@link EventHandler event handlers} to listen to edit events in the
     *      TableColumn, and react accordingly. Methods of interest include
     *      {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
     *      {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
     *      and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
     * @return A {@link Callback} that will return a TableCell that is able to
     *      work on the type of element contained within the TableColumn.
     */
    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final StringConverter<T> converter,
            final ObservableList<T> items) {
        return list -> new ComboBox2TableCell<>(converter, items);
    }

    /***************************************************************************
     *                                                                         *
     * Fields                                                                  *
     *                                                                         *
     **************************************************************************/

    private ComboBox<T> comboBox;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Creates a default ComboBox2TableCell with an empty items list.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     */
    public ComboBox2TableCell() {
        this(FXCollections.<T>observableArrayList());
    }

    /**
     * Creates a default {@link ComboBox2TableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param items The items to show in the ComboBox popup menu when selected
     *      by the user.
     */
    @SafeVarargs
    public ComboBox2TableCell(T... items) {
        this(FXCollections.observableArrayList(items));
    }

    /**
     * Creates a {@link ComboBox2TableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown, and the
     * {@link StringConverter} being used to convert the item in to a
     * user-readable form.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param converter A {@link StringConverter} that can convert an item of type T
     *      into a user-readable string so that it may then be shown in the
     *      ComboBox popup menu.
     * @param items The items to show in the ComboBox popup menu when selected
     *      by the user.
     */
    @SafeVarargs
    public ComboBox2TableCell(StringConverter<T> converter, T... items) {
        this(converter, FXCollections.observableArrayList(items));
    }

    /**
     * Creates a default {@link ComboBox2TableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param items The items to show in the ComboBox popup menu when selected
     *      by the user.
     */
    public ComboBox2TableCell(ObservableList<T> items) {
        this(null, items);
    }

    /**
     * Creates a {@link ComboBox2TableCell} instance with the given items
     * being used to populate the {@link ComboBox} when it is shown, and the
     * {@link StringConverter} being used to convert the item in to a
     * user-readable form.
     *
     * This table cell supports commit on focus lost. By default the ComboBox is
     * set as editable.
     *
     * @param converter A {@link StringConverter} that can convert an item of type T
     *      into a user-readable string so that it may then be shown in the
     *      ComboBox popup menu.
     * @param items The items to show in the ComboBox popup menu when selected
     *      by the user.
     */
    public ComboBox2TableCell(StringConverter<T> converter, ObservableList<T> items) {
        super(converter, items);
        
        // by default, set combobox editable
        setComboBoxEditable(true);
        
        graphicProperty().addListener(new InvalidationListener() {
            
            @Override
            public void invalidated(Observable observable) {
                if (getGraphic() != null && getGraphic() instanceof ComboBox) {
                    comboBox = (ComboBox<T>) getGraphic();
                    
                    comboBox.editableProperty().addListener((obs, ov, nv) -> 
                        updateListeners(nv));
                    updateListeners(comboBox.isEditable());
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
    
    private final EventHandler<KeyEvent> keyEventHandler = event -> {
            final TableView.TableViewSelectionModel<S> selectionModel = getTableView().getSelectionModel();
            if (comboBox == null || event.getCode() == null || selectionModel == null) {
                return;
            }
            switch (event.getCode()) {
                case ESCAPE:
                    // restore old value
                    comboBox.getEditor().setText(getConverter().toString(getItem()));
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
        };
    
    private final ChangeListener<Boolean> focusListener = (obs, ov, nv) -> {
            if (! nv && comboBox != null && getConverter() != null) {
                String text = comboBox.getEditor().getText();
                T t = getConverter().fromString(text);
                commitEdit(t);
            }
        };
    
    private void updateListeners(boolean editable) {
        if (editable) {
            // commit on focus lost
            comboBox.getEditor().focusedProperty().addListener(focusListener);
            // cancel with Escape, on key pressed, not released
            comboBox.addEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
        } else  {
            comboBox.getEditor().focusedProperty().removeListener(focusListener);
            comboBox.removeEventFilter(KeyEvent.KEY_PRESSED, keyEventHandler);
        }
    }
}
