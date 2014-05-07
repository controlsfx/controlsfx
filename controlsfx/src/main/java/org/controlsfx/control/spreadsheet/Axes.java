/**
 * Copyright (c) 2014 ControlsFX
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

import java.util.BitSet;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;

/**
 * This class handles everything related to the Axes of the
 * {@link SpreadsheetView}.
 * <br/>
 *
 * <h3>Fixing Rows and Columns</h3>
 * <br/>
 * You can fix some rows and some columns by right-clicking on their header. A
 * context menu will appear if it's possible to fix them. The label will then be
 * in italic and the background will turn to dark grey. Keep in mind that only
 * columns without any spanning cells can be fixed.
 * <br/>
 * And that and only rows without row-spanning cells can be fixed. <br/>
 * You have also the possibility to fix them manually by adding and removing
 * items from {@link #getFixedRows()} and {@link #getFixedColumns()}. But you
 * are strongly advised to check if it's possible to do so with
 * {@link SpreadsheetColumn#isColumnFixable()} for the fixed columns and with
 * {@link #isRowFixable(int)} for the fixed rows. Calling those methods prior
 * every move will ensure that no exception will be thrown.
 *
 * <br/>
 *
 * <h3>Headers</h3>
 * <br/>
 * You can also access and toggle header's visibility by using the methods
 * provided like {@link #setShowRowHeader(boolean) } or {@link #setShowColumnHeader(boolean)
 * }.
 * 
 * <br/>
 * 
 * <h3>Pickers</h3>
 * <br/>
 * 
 * You can show some little images next to the Axes. They will appear on the 
 * left of the VerticalHeader and on top on the HorizontalHeader. They are called
 * "picker" because they were used originally for picking a row or a column to 
 * insert in the SpreadsheetView.
 * <br/>
 * But you can do anything you want with it. Simply add a row or a column index in 
 * {@link #getRowPickers() } and {@link #getColumnPickers() }. Then you can provide 
 * a custom CallBack with {@link #setRowPickerCallback(javafx.util.Callback) } and 
 * {@link #setColumnPickerCallback(javafx.util.Callback) } in order to react when 
 * the user click on the picker. The Callback gives you the index of the picker.
 * <br/>
 * 
 * You can also override the default graphic of the picker by overriding its css,
 * example:
 * <br/>
 * <pre>
 * .picker-label{
 *   -fx-graphic: url("add.png"); 
 *   -fx-background-color: transparent;
 *   -fx-padding: 0 0 0 0;
 * }
 * </pre>
 * 
 * 
 */
public class Axes {

    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
    private final ObservableList<SpreadsheetColumn> fixedColumns = FXCollections.observableArrayList();

    private final BooleanProperty fixingRowsAllowedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty fixingColumnsAllowedProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader", true);
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader", true);

    private final SpreadsheetView spreadsheetView;
    private BitSet rowFix; // Compute if we can fix the rows or not.

    private final ObservableList<Integer> rowPickers = FXCollections.observableArrayList();
    private Callback<Integer, Void> rowPickerCallback = DEFAULT_CALLBACK;

    private final ObservableList<Integer> columnPickers = FXCollections.observableArrayList();
    private Callback<Integer, Void> columnPickerCallback = DEFAULT_CALLBACK;

    public Axes(SpreadsheetView spreadsheetView) {
        this.spreadsheetView = spreadsheetView;

        // Listeners & handlers
        fixedRows.addListener(fixedRowsListener);
        fixedColumns.addListener(fixedColumnsListener);
    }

    /**
     * You can fix or unfix a row by modifying this list. Call
     * {@link #isRowFixable(int)} before trying to fix a row. See
     * {@link SpreadsheetView} description for information.
     *
     * @return an ObservableList of integer representing the fixedRows.
     */
    public ObservableList<Integer> getFixedRows() {
        return fixedRows;
    }

    /**
     * Indicate whether a row can be fixed or not. Call that method before
     * adding an item with {@link #getFixedRows()} .
     *
     * @param row
     * @return true if the row can be fixed.
     */
    public boolean isRowFixable(int row) {
        return row < rowFix.size() && isFixingRowsAllowed() ? rowFix.get(row) : false;
    }

    /**
     * Return whether change to Fixed rows are allowed.
     *
     * @return whether change to Fixed rows are allowed.
     */
    public boolean isFixingRowsAllowed() {
        return fixingRowsAllowedProperty.get();
    }

    /**
     * If set to true, user will be allowed to fix and unfix the rows.
     *
     * @param b
     */
    public void setFixingRowsAllowed(boolean b) {
        fixingRowsAllowedProperty.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of fixing or
     * unfixing some rows.
     *
     * @return the Boolean property associated with the allowance of fixing or
     * unfixing some rows.
     */
    public ReadOnlyBooleanProperty fixingRowsAllowedProperty() {
        return fixingRowsAllowedProperty;
    }

    /**
     * You can fix or unfix a column by modifying this list. Call
     * {@link SpreadsheetColumn#isColumnFixable()} on the column before adding
     * an item.
     *
     * @return an ObservableList of the fixed columns.
     */
    public ObservableList<SpreadsheetColumn> getFixedColumns() {
        return fixedColumns;
    }

    /**
     * Indicate whether this column can be fixed or not. If you have a
     * {@link SpreadsheetColumn}, call
     * {@link SpreadsheetColumn#isColumnFixable()} on it directly. Call that
     * method before adding an item with {@link #getFixedColumns()} .
     *
     * @param columnIndex
     * @return true if the column if fixable
     */
    public boolean isColumnFixable(int columnIndex) {
        return columnIndex < spreadsheetView.getColumns().size()
                ? spreadsheetView.getColumns().get(columnIndex).isColumnFixable() : null;
    }

    /**
     * Return whether change to Fixed columns are allowed.
     *
     * @return whether change to Fixed columns are allowed.
     */
    public boolean isFixingColumnsAllowed() {
        return fixingColumnsAllowedProperty.get();
    }

    /**
     * If set to true, user will be allowed to fix and unfix the columns.
     *
     * @param b
     */
    public void setFixingColumnsAllowed(boolean b) {
        fixingColumnsAllowedProperty.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of fixing or
     * unfixing some columns.
     *
     * @return the Boolean property associated with the allowance of fixing or
     * unfixing some columns.
     */
    public ReadOnlyBooleanProperty fixingColumnsAllowedProperty() {
        return fixingColumnsAllowedProperty;
    }

    /**
     * Activate and deactivate the Column Header
     *
     * @param b
     */
    public final void setShowColumnHeader(final boolean b) {
        showColumnHeader.setValue(b);
    }

    /**
     * Return if the Column Header is showing.
     *
     * @return a boolean telling whether the column Header is shown
     */
    public final boolean isShowColumnHeader() {
        return showColumnHeader.get();
    }

    /**
     * BooleanProperty associated with the column Header.
     *
     * @return the BooleanProperty associated with the column Header.
     */
    public final BooleanProperty showColumnHeaderProperty() {
        return showColumnHeader;
    }

    /**
     * Activate and deactivate the Row Header.
     *
     * @param b
     */
    public final void setShowRowHeader(final boolean b) {
        showRowHeader.setValue(b);
    }

    /**
     * Return if the row Header is showing.
     *
     * @return a boolean telling if the row Header is being shown
     */
    public final boolean isShowRowHeader() {
        return showRowHeader.get();
    }

    /**
     * BooleanProperty associated with the row Header.
     *
     * @return the BooleanProperty associated with the row Header.
     */
    public final BooleanProperty showRowHeaderProperty() {
        return showRowHeader;
    }

    /**
     * Return an ObservableList of row indexes that display a picker.
     * See {@link Axes} description.
     * @return 
     */
    public ObservableList<Integer> getRowPickers() {
        return rowPickers;
    }

    /**
     * Set a custom callback for the Row picker. Row number is given to you in 
     * the callback.
     * @param callback 
     */
    public void setRowPickerCallback(Callback<Integer, Void> callback) {
        this.rowPickerCallback = callback;
    }

    /**
     * Return the row Picker Callback.
     * @return 
     */
    public Callback<Integer, Void> getRowPickerCallback() {
        return rowPickerCallback;
    }

    /**
     * Return an ObservableList of column indexes that display a picker.
     * See {@link Axes} description.
     * @
     * @return 
     */
    public ObservableList<Integer> getColumnPickers() {
        return columnPickers;
    }

     /**
     * Set a custom callback for the Column picker. Column number is given to you in 
     * the callback.
     * @param callback 
     */
    public void setColumnPickerCallback(Callback<Integer, Void> callback) {
        this.columnPickerCallback = callback;
    }

    /**
     * Return the columnPicker Callback.
     * @return 
     */
    public Callback<Integer, Void> getColumnPickerCallback() {
        return columnPickerCallback;
    }

    void initRowFix(Grid grid) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        rowFix = new BitSet(rows.size());
        rows:
        for (int r = 0; r < rows.size(); ++r) {
            ObservableList<SpreadsheetCell> row = rows.get(r);
            for (SpreadsheetCell cell : row) {
                if (cell.getRowSpan() > 1) {
                    continue rows;
                }
            }
            rowFix.set(r);
        }
    }

    private final ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends Integer> c) {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends Integer> newRows = c.getAddedSubList();
                    for (int row : newRows) {
                        if (!isRowFixable(row)) {
                            throw new IllegalArgumentException(computeReason(row));
                        }
                    }
                    FXCollections.sort(fixedRows);
                }
            }
        }

        private String computeReason(Integer element) {
            String reason = "\n This row cannot be fixed.";
            for (SpreadsheetCell cell : spreadsheetView.getGrid().getRows().get(element)) {
                if (cell.getRowSpan() > 1) {
                    reason += "The cell situated at line " + cell.getRow() + " and column " + cell.getColumn()
                            + "\n has a rowSpan of " + cell.getRowSpan() + ", it must be 1.";
                    return reason;
                }
            }
            return reason;
        }
    };

    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends SpreadsheetColumn> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends SpreadsheetColumn> newColumns = c.getAddedSubList();
                    for (SpreadsheetColumn column : newColumns) {
                        if (!column.isColumnFixable()) {
                            throw new IllegalArgumentException(computeReason(column));
                        }
                    }
                }
            }
        }

        private String computeReason(SpreadsheetColumn element) {
            int indexColumn = spreadsheetView.getColumns().indexOf(element);

            String reason = "\n This column cannot be fixed.";
            for (ObservableList<SpreadsheetCell> row : spreadsheetView.getGrid().getRows()) {
                int columnSpan = row.get(indexColumn).getColumnSpan();
                if (columnSpan > 1 || row.get(indexColumn).getRowSpan() > 1) {
                    reason += "The cell situated at line " + row.get(indexColumn).getRow() + " and column "
                            + indexColumn + "\n has a rowSpan or a ColumnSpan superior to 1, it must be 1.";
                    return reason;
                }
            }
            return reason;
        }
    };

    private static final Callback<Integer, Void> DEFAULT_CALLBACK = new Callback<Integer, Void>() {

        @Override
        public Void call(Integer p) {
            //no-op
            return null;
        }
    };
}
