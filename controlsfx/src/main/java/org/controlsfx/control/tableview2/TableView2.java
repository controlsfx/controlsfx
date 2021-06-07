/**
 * Copyright (c) 2013, 2020 ControlsFX
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
package org.controlsfx.control.tableview2;

import impl.org.controlsfx.tableview2.SortUtils.SortEndedEvent;
import impl.org.controlsfx.tableview2.SortUtils.SortStartedEvent;
import impl.org.controlsfx.tableview2.TableView2Skin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import org.controlsfx.control.tableview2.actions.ColumnFixAction;
import org.controlsfx.control.tableview2.actions.RowFixAction;
import org.controlsfx.control.tableview2.cell.ComboBox2TableCell;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * The TableView2 is an advanced JavaFX {@link TableView} control, that can
 * be used as drop-in replacement control for the existing TableView, and provides
 * different functionalities and use cases. 
 * 
 * <h3>Features</h3>
 * <ul>
 * <li>Rows can be fixed to the top of the {@link TableView2} so that they
 * are always visible on screen.</li>
 * <li>Columns can be fixed to the left of the {@link TableView2} so that
 * they are always visible on screen.</li>
 * <li>A row header can be switched on in order to display any custom content.</li>
 * <li>The column header can be extended to provide custom content.</li>
 * <li>{@link FilteredTableView} is a subclass of TableView2 with extended
 * filtering options</li>
 * </ul>
 * 
 * <br>
 * 
 * <h3>Fixing Rows and Columns</h3> 
 * <br>
 * Rows and columns can be fixed using dedicated actions like 
 * {@link ColumnFixAction} and {@link RowFixAction}. When fixed, the header's
 * background will show a darker color. For instance, these actions can be 
 * attached to a {@link ContextMenu} that can be installed to the row header cells
 * with {@link #rowHeaderContextMenuFactory}.
 * <br>
 * You have also the possibility to fix them manually by adding and removing
 * items from {@link #getFixedRows()} and {@link #getFixedColumns()}. But you
 * are strongly advised to check if it's possible to do so with
 * {@link #isColumnFixable(int)} for the fixed columns and with
 * {@link #isRowFixable(int)} for the fixed rows.
 * <br>
 *
 * <br>
 * If you want to fix several rows or columns together, you can call 
 * {@link #areRowsFixable(List) } or
 * {@link #areTableViewColumnsFixable(List)}
 * to verify if you can fix them.
 *
 * Calling those methods prior every move will ensure that no exception will be thrown.
 * <br>
 * You have also the possibility to deactivate these features.
 * <br>
 *
 * <h3>Row Headers</h3>
 * <br>
 * You can also access and toggle row header's visibility by using the method
 * provided {@link #setRowHeaderVisible(boolean) }.
 *
 * By default the row header will show the row index, but you can set the content
 * with {@link #setRowHeader(TableColumn) }.
 *
 * <h3>Cell editing</h3>
 * <br>
 * Two specialized cell factories are available {@link TextField2TableCell} and
 * {@link ComboBox2TableCell}, providing support for commit on focus lost.
 *
 * <h3>Filtering options</h3>
 * <br>
 * While filtering can be implemented as in a regular JavaFX TableView control,
 * see {@link FilteredTableView} for extended filtering options.
 *
 * <h3>Features not supported</h3>
 * <br>
 * Cell spanning is not supported yet.
 *
 * <h2>Sample</h2>
 *
 * <p>Let's provide the underlying data model, based on a <code>Person</code> class.
 *
 * <pre>
 * {@code
 * public class Person {
 *     private StringProperty firstName;
 *     public void setFirstName(String value) { firstNameProperty().set(value); }
 *     public String getFirstName() { return firstNameProperty().get(); }
 *     public StringProperty firstNameProperty() {
 *         if (firstName == null) firstName = new SimpleStringProperty(this, "firstName");
 *         return firstName;
 *     }
 *
 *     private StringProperty lastName;
 *     public void setLastName(String value) { lastNameProperty().set(value); }
 *     public String getLastName() { return lastNameProperty().get(); }
 *     public StringProperty lastNameProperty() {
 *         if (lastName == null) lastName = new SimpleStringProperty(this, "lastName");
 *         return lastName;
 *     }
 * }}</pre>
 *
 * <p>A TableView2 can be created, and filled with an observable list of people:
 *
 * <pre>
 * {@code
 * TableView2<Person> table = new TableView2<Person>();
 * ObservableList<Person> people = getPeople();
 * table.setItems(people);
 * }</pre>
 *
 * <p>Now we add two {@link TableColumn2 columns} to the table:
 *
 * <pre>
 * {@code
 * TableColumn2<Person,String> firstNameCol = new TableColumn2<>("First Name");
 * firstNameCol.setCellValueFactory(p -> p.getValue().firstNameProperty());
 * TableColumn2<Person,String> lastNameCol = new TableColumn2<>("Last Name");
 * lastNameCol.setCellValueFactory(p -> p.getValue().lastNameProperty());
 *
 * table.getColumns().setAll(firstNameCol, lastNameCol);}</pre>
 *
 * <p>A cell factory that allows commit on focus lost can be set:
 *
 * <pre>
 * {@code
 * firstName.setCellFactory(TextField2TableCell.forTableColumn());}</pre>
 *
 * <p>We can fix some row and columns, and also show the row header:
 *
 * <pre>
 * {@code
 * table.getFixedColumns().setAll(firstNameColumn);
 * table.getFixedRows().setAll(0, 1, 2);
 *
 * table.setRowHeaderVisible(true);}</pre>
 *
 * @param <S> The type of the objects contained within the TableView2 items list.
 */
public class TableView2<S> extends TableView<S> {

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/

    /**
     * The SpanType describes in which state each cell can be. When a spanning
     * is occurring, one cell is becoming larger and the others are becoming
     * invisible. Thus, that particular cell is masking the others. <br>
     * <br>
     * But the SpanType cannot be known in advance because it's evolving for
     * each cell during the lifetime of the {@link TableView2}. Suppose you
     * have a cell spanning in row, the first one is in a ROW_VISIBLE state, and
     * all the other below are in a ROW_SPAN_INVISIBLE state. But if the user is
     * scrolling down, the first will go out of sight. At that moment, the
     * second cell is switching from ROW_SPAN_INVISIBLE state to ROW_VISIBLE
     * state. <br>
     * <br>
     *
     * <center><img src="spanType.png" alt="Screenshot of TableView2.SpanType"></center>
     * Refer to {@link TableView2} for more information.
     */
    public static enum SpanType {

        /**
         * Visible cell, can be a unique cell (no span) or the first one inside
         * a column spanning cell.
         */
        NORMAL_CELL,

        /**
         * Invisible cell because a cell in a NORMAL_CELL state on the left is
         * covering it.
         */
        COLUMN_SPAN_INVISIBLE,

        /**
         * Invisible cell because a cell in a ROW_VISIBLE state on the top is
         * covering it.
         */
        ROW_SPAN_INVISIBLE,

        /** Visible Cell but has some cells below in a ROW_SPAN_INVISIBLE state. */
        ROW_VISIBLE,

        /**
         * Invisible cell situated in diagonal of a cell in a ROW_VISIBLE state.
         */
        BOTH_INVISIBLE;
    }

    /**
     * Default width of the VerticalHeader.
     */
    private static final double DEFAULT_ROW_HEADER_WIDTH = 30.0;

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
    private final ObservableList<TableColumn> fixedColumns = FXCollections.observableArrayList();

    private final BooleanProperty rowFixingEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty columnFixingEnabled = new SimpleBooleanProperty(true);

    private final BooleanProperty rowHeaderVisible = new SimpleBooleanProperty(true, "showRowHeader", false); //$NON-NLS-1$

    private BitSet rowFix; // Compute if we can fix the rows or not.

    /**
     * The vertical header width, just for the Label.
     */
    private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);

    /*
     * cache the stylesheet as lookup takes time and the getUserAgentStylesheet is called repeatedly
     */
    private String stylesheet;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Creates a TableView2 control with no content.
     *
     */
    public TableView2() {
        this(FXCollections.<S>observableArrayList());
    }

    /**
     * Creates a TableView2 with the content provided in the items ObservableList.
     *
     * @param items The items to insert into the TableView2, and the list to watch
     *          for changes (to automatically show in the TableView2).
     */
    public TableView2(ObservableList<S> items) {
        super(items);
        getStyleClass().add("table-view2"); //$NON-NLS-1$

        // Listeners & handlers
        fixedRows.addListener(fixedRowsListener);
        fixedColumns.addListener(fixedColumnsListener);

        skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                ((TableView2Skin<S>) getSkin()).sizeProperty().addListener(o -> initRowFix());
                skinProperty().removeListener(this);
            }
        });

        getItems().addListener((Observable o) -> initRowFix());
        initRowFix();
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * Return the current row span at the given position in
     * the Table.
     *
     * If a sort is applied to the TableView2, some spanned cells may be
     * splitted.
     *
     * @param pos the {@link TablePosition}
     * @param index the index
     * @return the current row span for the given cell.
     */
    public int getRowSpan(TablePosition<?, ?> pos, int index) {
        // TODO: find cell at pos.getRow(), pos.getColumn()
        // return getRowSpan(cell, index);
        return 1;
    }

    /**
     * Return the current column span.
     *
     * @param pos The {@link TablePosition}
     * @return the current column span of a Cell.
     */
    public int getColumnSpan(TablePosition<?, ?> pos) {
        // TODO: find cell at pos.getRow(), pos.getColumn()
        return 1;
    }

    /**
     * You can fix or unfix a row by modifying this list. Call
     * {@link #isRowFixable(int)} before trying to fix a row. See
     * {@link TableView2} description for information.
     *
     * @return an ObservableList of integer representing the fixedRows.
     */
    public final ObservableList<Integer> getFixedRows() {
        return fixedRows;
    }

    /**
     * Indicate whether a row can be fixed or not. Call that method before
     * adding an item with {@link #getFixedRows()} .
     *
     * A row cannot be fixed alone if any cell inside the row has a row span
     * superior to one.
     *
     * @param row the number of row
     * @return true if the row can be fixed.
     */
    public final boolean isRowFixable(int row) {
        return row >= 0 && row < rowFix.size() && isRowFixingEnabled() ? rowFix.get(row) : false;
    }

    /**
     * Indicates whether a List of rows can be fixed or not.
     *
     * A set of rows cannot be fixed if any cell inside these rows has a row
     * span superior to the number of fixed rows.
     *
     * @param list List of rows indices
     * @return true if the List of row can be fixed together.
     */
    public final boolean areRowsFixable(List<? extends Integer> list) {
        if (list == null || list.isEmpty() || !isRowFixingEnabled() || getItems() == null) {
            return false;
        }
        for (Integer row : list) {
            if (row == null || row < 0 || row >= getItems().size()) {
                return false;
            }
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {
                int maxSpan = 1;
                for (TableColumn c : getColumns()) {
                    //If the original row is not within this range, there is not need to look deeper.
                    if (!list.contains(row)) {
                        return false;
                    }
                    //We only want to consider the original cell.
                    if (getRowSpan(new TablePosition<>(this, row, c), row) > maxSpan) {
                        maxSpan = 1; // TODO: cell.getRowSpan();
                    }
                }
                //Then we need to verify that all rows within that span are fixed.
                int count = row + maxSpan - 1;
                for (int index = row + 1; index <= count; ++index) {
                    if (!list.contains(index)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return whether changes to Fixed rows are enabled.
     *
     * @return whether changes to Fixed rows are enabled.
     */
    public final boolean isRowFixingEnabled() {
        return rowFixingEnabled.get();
    }

    /**
     * If set to true, user will be allowed to fix and unfix the rows.
     *
     * @param b If set to true, user will be allowed to fix and unfix the rows
     */
    public final void setRowFixingEnabled(boolean b) {
        rowFixingEnabled.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of fixing or
     * unfixing some rows.
     *
     * @return the Boolean property associated with the allowance of fixing or
     * unfixing some rows.
     */
    public final ReadOnlyBooleanProperty rowFixingEnabledProperty() {
        return rowFixingEnabled;
    }

    /**
     * You can fix or unfix a column by modifying this list.
     *
     * @return an ObservableList of the fixed columns.
     */
    public final ObservableList<TableColumn> getFixedColumns() {
        return fixedColumns;
    }

    /**
     * Indicate whether this column can be fixed or not.
     *
     * @param columnIndex the number of column
     * @return true if the column if fixable
     */
    public final boolean isColumnFixable(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= getColumns().size() || ! isColumnFixingEnabled()) {
            return false;
        }
        TableColumn<S, ?> column = getColumns().get(columnIndex);
        return column.getParentColumn() == null;
    }

    /**
     * Indicates whether a List of {@link TableColumn} can be fixed or
     * not.
     *
     * A set of columns cannot be fixed if any cell inside these columns has a
     * column span superior to the number of fixed columns.
     *
     * @param list list of {@link TableColumn}
     * @return true if the List of columns can be fixed together.
     */
    private boolean areTableViewColumnsFixable(List<? extends TableColumn> list) {
        return areColumnsFixable(list.stream()
            .filter(Objects::nonNull)
            .map(getColumns()::indexOf)
            .collect(Collectors.toList()));
    }

    /**
     * This method is the same as {@link #areTableViewColumnsFixable(List)
     * } but is using a List of {@link TableColumn} indices.
     *
     * A set of columns cannot be fixed if any cell inside these columns has a
     * column span superior to the number of fixed columns.
     *
     * @param list list of column indices
     * @return true if the List of columns can be fixed together.
     */
    private boolean areColumnsFixable(List<? extends Integer> list) {
        if (list == null || list.isEmpty() || !isRowFixingEnabled()) {
            return false;
        }
        for (Integer columnIndex : list) {
            if (columnIndex == null || columnIndex < 0 || columnIndex >= getColumns().size()) {
                return false;
            }
            //If this column is not fixable, we need to identify the maximum span
            if (!isColumnFixable(columnIndex)) {
                int maxSpan = 1;
                // TODO: Get maxSpan
                //Then we need to verify that all columns within that span are fixed.
                int count = columnIndex + maxSpan - 1;
                for (int index = columnIndex + 1; index <= count; ++index) {
                    if (!list.contains(index)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Return whether changes to Fixed columns are enabled.
     *
     * @return whether changes to Fixed columns are enabled.
     */
    public final boolean isColumnFixingEnabled() {
        return getItems() != null && columnFixingEnabled.get();
    }

    /**
     * If set to true, user will be allowed to fix and unfix the columns.
     *
     * @param b If set to true, user will be allowed to fix and unfix the columns
     */
    public final void setColumnFixingEnabled(boolean b) {
        columnFixingEnabled.set(b);
    }

    /**
     * Return the Boolean property associated with the allowance of fixing or
     * unfixing some columns.
     *
     * @return the Boolean property associated with the allowance of fixing or
     * unfixing some columns.
     */
    public final ReadOnlyBooleanProperty columnFixingEnabledProperty() {
        return columnFixingEnabled;
    }

    /**
     * Activate and deactivate the row header.
     *
     * @param b boolean to show or hide the header
     */
    public final void setRowHeaderVisible(final boolean b) {
        rowHeaderVisible.setValue(b);
    }

    /**
     * Return if the row header is showing.
     *
     * @return a boolean telling if the row header is being shown
     */
    public final boolean isRowHeaderVisible() {
        return rowHeaderVisible.get();
    }

    /**
     * BooleanProperty associated with the row Header.
     *
     * @return the BooleanProperty associated with the row header.
     */
    public final BooleanProperty rowHeaderVisibleProperty() {
        return rowHeaderVisible;
    }

    /**
     * This DoubleProperty represents the with of the rowHeader. This is just
     * representing the width of the Labels.
     *
     * @return A DoubleProperty.
     */
    public final DoubleProperty rowHeaderWidthProperty(){
        return rowHeaderWidth;
    }

    /**
     * Specify a new width for the row header.
     *
     * @param value the new width
     */
    public final void setRowHeaderWidth(double value){
        rowHeaderWidth.setValue(value);
    }

    /**
     *
     * @return the current width of the row header.
     */
    public final double getRowHeaderWidth(){
        return rowHeaderWidth.get();
    }

    /**
     * The row header property wraps a {@link TableColumn} that can be used to
     * render the row header.
     *
     * By default, if this property is not set, a TableColumn will be used to
     * render the number of row, starting from 1.
     */
    private final ObjectProperty<TableColumn<S, ?>> rowHeader = new SimpleObjectProperty<>(this, "rowHeader");
    public final void setRowHeader(TableColumn<S, ?> value) { rowHeader.set(value); }
    public final TableColumn<S, ?> getRowHeader() { return rowHeader.get(); }
    public final ObjectProperty<TableColumn<S, ?>> rowHeaderProperty() { return rowHeader; }

    /**
     * An object property of a {@link BiFunction} that can be used to define the
     * context menu of each row of the row header.
     *
     * See {@link RowFixAction}.
     */
    private ObjectProperty<BiFunction<Integer, S, ContextMenu>> rowHeaderContextMenuFactory;
    public final void setRowHeaderContextMenuFactory(BiFunction<Integer, S, ContextMenu> value) { rowHeaderContextMenuFactoryProperty().set(value); }
    public final BiFunction<Integer, S, ContextMenu> getRowHeaderContextMenuFactory() { return rowHeaderContextMenuFactory == null ? null : rowHeaderContextMenuFactory.get(); }
    public final ObjectProperty<BiFunction<Integer, S, ContextMenu>> rowHeaderContextMenuFactoryProperty() {
        if (rowHeaderContextMenuFactory == null) {
            rowHeaderContextMenuFactory = new SimpleObjectProperty<>(this, "rowHeaderContextMenu");
        }
        return rowHeaderContextMenuFactory;
    }

    /**
     * This property allows the developer to blend the south table header row
     * with the regular table header row, so for each column, the regular header
     * and the south table column header look like a single one.
     */
    private final BooleanProperty southHeaderBlended = new SimpleBooleanProperty(this, "southHeaderBlended", true);
    public final void setSouthHeaderBlended(boolean value) { southHeaderBlended.set(value); }
    public final boolean isSouthHeaderBlended() { return southHeaderBlended.get(); }
    public final BooleanProperty southHeaderBlendedProperty() { return southHeaderBlended; }

    /**
     * Return the {@link SpanType} of a cell. This is used internally by the
     * TableView2 but some users may find it useful.
     *
     * @param rowIndex the number of row
     * @param modelColumn the number of column
     * @return The {@link SpanType} of a cell
     */
    public SpanType getSpanType(final int rowIndex, final int modelColumn) {
        if (getSkin() == null) {
            return SpanType.NORMAL_CELL;
        }

        if (rowIndex < 0 || modelColumn < 0 || (getItems() != null && rowIndex >= getItems().size())
                || modelColumn >= getVisibleLeafColumns().size()) {
            return SpanType.NORMAL_CELL;
        }

        final TablePosition<S, ?> pos = new TablePosition<>(this, rowIndex, getVisibleLeafColumns().get(modelColumn));

        if (pos.getColumn() < 0) {
            return SpanType.COLUMN_SPAN_INVISIBLE;
        }

        final int cellColumn = modelColumn;
//        final int cellRow = spv.getViewRow(cell.getRow());
        int cellRowSpan = 1; //cell.getRowSpan();

        if (cellColumn == modelColumn /*&& cellRow == rowIndex*/ && cellRowSpan == 1) {
            return SpanType.NORMAL_CELL;
        }
//        cellRowSpan = spv.getRowSpanFilter(cell);
        final int cellColumnSpan = getColumnSpan(pos);
        /**
         * This is a consuming operation so we place it after the normal_cell
         * case since this is the most typical case.
         */
        final boolean containsRowMinusOne = getSkin() == null ? true : ((TableView2Skin) getSkin()).containsRow(rowIndex - 1);
        //If the cell above is the same.
        final boolean containsSameCellMinusOne = false; // TODO: check
        if (containsRowMinusOne && cellColumnSpan > 1 && cellColumn != modelColumn && cellRowSpan > 1
                && containsSameCellMinusOne) {
            return SpanType.BOTH_INVISIBLE;
        } else if (cellRowSpan > 1 && cellColumn == modelColumn) {
            if ((!containsSameCellMinusOne || !containsRowMinusOne)) {
                return SpanType.ROW_VISIBLE;
            } else {
                return SpanType.ROW_SPAN_INVISIBLE;
            }
        } else if (cellColumnSpan > 1 && (!containsSameCellMinusOne || !containsRowMinusOne)) {
            /**
             * If the next visible column from the starting column is my
             * viewColumn.
             */
            if (cellColumn == modelColumn) {
                return SpanType.NORMAL_CELL;
            } else {
                return SpanType.COLUMN_SPAN_INVISIBLE;
            }
        } else {
            return SpanType.NORMAL_CELL;
        }
    }

    /**
     * Overrides {@link TableView#sort() } in order to fire custom sort events
     * when sorting starts and finishes.
     *
     * See {@link TableView#sort() } for more details about calling directly this
     * method.
     */
    @Override
    public void sort() {
        SortStartedEvent<TableView<S>> sortStartedEvent = new SortStartedEvent<>(TableView2.this, TableView2.this);
        fireEvent(sortStartedEvent);

        super.sort();

        SortEndedEvent<TableView<S>> sortEndedEvent = new SortEndedEvent<>(TableView2.this, TableView2.this);
        fireEvent(sortEndedEvent);
    }

    /***************************************************************************
     * * Private/Protected Implementation * *
     **************************************************************************/

    private void initRowFix() {
        rowFix = new BitSet(getItems() != null ? getItems().size() : 0);
        for (int r = 0; r < rowFix.size(); ++r) {
            rowFix.set(r);
        }
    }

    /**
     * ********************************************************************* *
     * private listeners
     * ********************************************************************
     */

    private final ListChangeListener<Integer> fixedRowsListener = (ListChangeListener.Change<? extends Integer> c) -> {
        while (c.next()) {
            if (c.wasAdded()) {
                List<? extends Integer> newRows = c.getAddedSubList();
                if (! areRowsFixable(newRows)) {
                    throw new IllegalArgumentException(computeReason(newRows));
                }
                FXCollections.sort(fixedRows);
            }

            if(c.wasRemoved()){
                //Handle this case.
            }
        }
    };

    private String computeReason(List<? extends Integer> list) {
        String reason = "\n A row cannot be fixed. \n"; //$NON-NLS-1$

        for (Integer row : list) {
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {

                int maxSpan = 1;
                // TODO: check maxSpan
                //Then we need to verify that all rows within that span are fixed.
                int count = row + maxSpan - 1;
                for (int index = row + 1; index < count; ++index) {
                    if (!list.contains(index)) {
                        reason += "One cell on the row " + row + " has a row span of " + maxSpan + ". " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                + "But the row " + index + " contained within that span is not fixed.\n"; //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
        }
        return reason;
    }

    private final ListChangeListener<TableColumn> fixedColumnsListener = new ListChangeListener<TableColumn>() {
        @Override
        public void onChanged(Change<? extends TableColumn> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends TableColumn> newColumns = c.getAddedSubList();
                    if (!areTableViewColumnsFixable(newColumns)) {
                        List<Integer> newList = new ArrayList<>();
                        for (TableColumn column : newColumns) {
                            if (column != null) {
                                newList.add(getColumns().indexOf(column));
                            }
                        }
                        throw new IllegalArgumentException(computeReason(newList));
                    }
                }
            }
        }

        private String computeReason(List<Integer> list) {

            String reason = "\n This column cannot be fixed."; //$NON-NLS-1$
            for (Integer columnIndex : list) {
                //If this row is not fixable, we need to identify the maximum span
                if (!isColumnFixable(columnIndex)) {
                    int maxSpan = 1;
                    // TODO: check maxSpan
                    //Then we need to verify that all columns within that span are fixed.
                    int count = columnIndex + maxSpan - 1;
                    for (int index = columnIndex + 1; index < count; ++index) {
                        if (!list.contains(index)) {
                            reason += "One cell on the column " + columnIndex + " has a column span of " + maxSpan + ". " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                    + "But the column " + index + " contained within that span is not fixed.\n"; //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
            }
            return reason;
        }
    };

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new TableView2Skin(this);
    }
    
    /** {@inheritDoc} */
    @Override public String getUserAgentStylesheet() {
        /*
         * For more information please see JDK-8088253
         */
        if (stylesheet == null) {
            stylesheet = TableView2.class.getResource("tableview2.css") //$NON-NLS-1$
                    .toExternalForm();
        }

        return stylesheet;
    }

}
