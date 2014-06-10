/**
 * Copyright (c) 2013, 2014 ControlsFX
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

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import impl.org.controlsfx.spreadsheet.CellView;
import impl.org.controlsfx.spreadsheet.FocusModelListener;
import impl.org.controlsfx.spreadsheet.GridViewSkin;
import impl.org.controlsfx.spreadsheet.SpreadsheetGridView;
import impl.org.controlsfx.spreadsheet.SpreadsheetHandle;
import impl.org.controlsfx.spreadsheet.SpreadsheetViewSelectionModel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import org.controlsfx.tools.Utils;

/**
 * The SpreadsheetView is a control similar to the JavaFX {@link TableView}
 * control but with different functionalities and use cases. The aim is to have
 * a powerful grid where data can be written and retrieved.
 * 
 * <h3>Features</h3>
 * <ul>
 * <li>Cells can span in row and in column.</li>
 * <li>Rows can be fixed to the top of the {@link SpreadsheetView} so that they
 * are always visible on screen.</li>
 * <li>Columns can be fixed to the left of the {@link SpreadsheetView} so that
 * they are always visible on screen. Only columns without any spanning cells
 * can be fixed.</li>
 * <li>A row header can be switched on in order to display the row number.</li>
 * <li>Rows can be resized just like columns with click & drag.</li>
 * <li>Both row and column header can be visible or invisible.</li>
 * <li>Selection of several cells can be made with a click and drag.</li>
 * <li>A copy/paste context menu is accessible with a right-click. The usual
 * shortcuts are also working.</li>
 * </ul>
 * 
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
 * <br/>
 * You have also the possibility to deactivate these possibilities. For example,
 * you force some row/column to be fixed and then the user cannot change the 
 * settings. 
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
 * You can show some little images next to the headers. They will appear on the 
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
 * The pickers will appear on the top of the column's header and on the left of 
 * the row's header.
 * <br/>
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
 * <h3>Copy pasting</h3> You can copy any cell you want and paste it elsewhere.
 * Be aware that only the value inside will be pasted, not the style nor the
 * type. Thus the value you're trying to paste must be compatible with the
 * {@link SpreadsheetCellType} of the receiving cell. Pasting a Double into a
 * String will work but the reverse operation will not. 
 * <br/>
 * See {@link SpreadsheetCellType} <i>Value Verification</i> documentation for more 
 * information.
 * <br/>
 * A unique cell or a selection of several of them can be copied and pasted.
 * 
 * <br/>
 * <br/>
 * <h3>Code Samples</h3> Just like the {@link TableView}, you instantiate the
 * underlying model, a {@link Grid}. You will create some rows filled with {@link SpreadsheetCell}.
 * 
 * <br/>
 * <br/>
 * 
 * <pre>
 * int rowCount = 15;
 *     int columnCount = 10;
 *     GridBase grid = new GridBase(rowCount, columnCount);
 *     
 *     ObservableList&lt;ObservableList&lt;SpreadsheetCell&gt;&gt; rows = FXCollections.observableArrayList();
 *     for (int row = 0; row &lt; grid.getRowCount(); ++row) {
 *         final ObservableList&lt;SpreadsheetCell&gt; list = FXCollections.observableArrayList();
 *         for (int column = 0; column &lt; grid.getColumnCount(); ++column) {
 *             list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,"value"));
 *         }
 *         rows.add(list);
 *     }
 *     grid.setRows(rows);
 *
 *     SpreadsheetView spv = new SpreadsheetView(grid);
 *     
 * </pre>
 * 
 * At that moment you can span some of the cells with the convenient method
 * provided by the grid. Then you just need to instantiate the SpreadsheetView. <br/>
 * <h3>Visual:</h3> <center><img src="spreadsheetView.png"/></center>
 * 
 * @see SpreadsheetCell
 * @see SpreadsheetCellBase
 * @see SpreadsheetColumn
 * @see Grid
 * @see GridBase
 */
public class SpreadsheetView extends Control {

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/

    /**
     * The SpanType describes in which state each cell can be. When a spanning
     * is occurring, one cell is becoming larger and the others are becoming
     * invisible. Thus, that particular cell is masking the others. <br/>
     * <br/>
     * But the SpanType cannot be known in advance because it's evolving for
     * each cell during the lifetime of the {@link SpreadsheetView}. Suppose you
     * have a cell spanning in row, the first one is in a ROW_VISIBLE state, and
     * all the other below are in a ROW_SPAN_INVISIBLE state. But if the user is
     * scrolling down, the first will go out of sight. At that moment, the
     * second cell is switching from ROW_SPAN_INVISIBLE state to ROW_VISIBLE
     * state. <br/>
     * <br/>
     * 
     * <center><img src="spanType.png"></center> Refer to
     * {@link SpreadsheetView} for more information.
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

    private final SpreadsheetGridView cellsView;// The main cell container.
    private SimpleObjectProperty<Grid> gridProperty = new SimpleObjectProperty<>();
    private DataFormat fmt;
    
    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
    private final ObservableList<SpreadsheetColumn> fixedColumns = FXCollections.observableArrayList();

    private final BooleanProperty fixingRowsAllowedProperty = new SimpleBooleanProperty(true);
    private final BooleanProperty fixingColumnsAllowedProperty = new SimpleBooleanProperty(true);

    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader", true);
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader", true);

    private BitSet rowFix; // Compute if we can fix the rows or not.

    private final ObservableList<Integer> rowPickers = FXCollections.observableArrayList();
    private Callback<Integer, Void> rowPickerCallback = DEFAULT_CALLBACK;

    private final ObservableList<Integer> columnPickers = FXCollections.observableArrayList();
    private Callback<Integer, Void> columnPickerCallback = DEFAULT_CALLBACK;

    // Properties needed by the SpreadsheetView and managed by the skin (source
    // is the VirtualFlow)
    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
    private Map<SpreadsheetCellType<?>, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
    
    /**
     * The vertical header width, just for the Label, not the Pickers.
     */
    private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);

    // The handle that bridges with implementation.
    final SpreadsheetHandle handle = new SpreadsheetHandle() {
        @Override
        protected SpreadsheetView getView() {
            return SpreadsheetView.this;
        }

        @Override
        protected GridViewSkin getCellsViewSkin() {
            return SpreadsheetView.this.getCellsViewSkin();
        }

        @Override
        protected SpreadsheetGridView getGridView() {
            return SpreadsheetView.this.getCellsView();
        }
    };

    /**
     * @return the inner table view skin
     */
    final GridViewSkin getCellsViewSkin() {
        return (GridViewSkin) (cellsView.getSkin());
    }

    /**
     * @return the inner table view
     */
    final SpreadsheetGridView getCellsView() {
        return cellsView;
    }

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Creates a default SpreadsheetView control with no content and a Grid set
     * to null.
     */
    public SpreadsheetView() {
        this(null);
    }

    /**
     * Creates a SpreadsheetView control with the {@link Grid} specified.
     * 
     * @param grid
     *            The Grid that contains the items to be rendered
     */
    public SpreadsheetView(final Grid grid) {
        super();
        getStyleClass().add("SpreadsheetView");
        // anonymous skin
        setSkin(new Skin<SpreadsheetView>() {
            @Override
            public Node getNode() {
                return SpreadsheetView.this.getCellsView();
            }

            @Override
            public SpreadsheetView getSkinnable() {
                return SpreadsheetView.this;
            }

            @Override
            public void dispose() {
                // no-op
            }
        });

        this.cellsView = new SpreadsheetGridView(handle);
        getChildren().add(cellsView);
        
        /**
         * Add a listener to the selection model in order to edit the spanned
         * cells when clicked
         */
        SpreadsheetViewSelectionModel selectionModel = new SpreadsheetViewSelectionModel(this,cellsView);
        cellsView.setSelectionModel(selectionModel);
        selectionModel.setCellSelectionEnabled(true);
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        /**
         * Set the focus model to track keyboard change and redirect focus on
         * spanned cells
         */
        // We add a listener on the focus model in order to catch when we are on
        // a hidden cell
        cellsView.getFocusModel().focusedCellProperty()
                .addListener((ChangeListener<TablePosition>) (ChangeListener<?>) new FocusModelListener(this,cellsView));

        /**
         * Keyboard action, maybe use an accelerator
         */
        cellsView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                // Copy
                if (keyEvent.isShortcutDown() && keyEvent.getCode()==KeyCode.C)
                    copyClipboard();
                // Paste
                else if (keyEvent.isShortcutDown() && keyEvent.getCode()==KeyCode.V)
                    pasteClipboard();
                // Go to the next row
                else if (keyEvent.getCode() == KeyCode.ENTER) {
                    cellsView.setEditWithEnter(true);
                    TablePosition<ObservableList<SpreadsheetCell>, ?> position = (TablePosition<ObservableList<SpreadsheetCell>, ?>) cellsView
                            .getFocusModel().getFocusedCell();
                    if (position != null) {
                        cellsView.getSelectionModel().clearAndSelect(position.getRow() + 1, position.getTableColumn());
                    }
                   /* // Go to next cell
                } else if (keyEvent.getCode().compareTo(KeyCode.TAB) == 0) {
                    TablePosition<ObservableList<SpreadsheetCell>, ?> position = (TablePosition<ObservableList<SpreadsheetCell>, ?>) cellsView
                            .getFocusModel().getFocusedCell();
                    if (position != null) {
                        cellsView.getSelectionModel().clearSelection();
                        cellsView.getSelectionModel().selectRightCell();
                    }*/
                    // We want to erase values when delete key is pressed.
                } else if (keyEvent.getCode()==KeyCode.DELETE)
                    deleteSelectedCells();
                // We want to edit if the user is on a cell and typing
                else if ((keyEvent.getCode().isLetterKey() || keyEvent.getCode().isDigitKey() || keyEvent.getCode()
                        .isKeypadKey()) && !keyEvent.isShortcutDown() && !keyEvent.getCode().isArrowKey()) {
                    @SuppressWarnings("unchecked")
                    TablePosition<ObservableList<SpreadsheetCell>, ?> position = (TablePosition<ObservableList<SpreadsheetCell>, ?>) cellsView
                            .getFocusModel().getFocusedCell();
                    cellsView.setEditWithKey(true);
                    cellsView.edit(position.getRow(), position.getTableColumn());
                }
            }
        });

        /**
         * ContextMenu handling.
         */
        this.contextMenuProperty().addListener(new WeakChangeListener<>(contextMenuChangeListener));
        // The contextMenu creation must be on the JFX thread
        Platform.runLater(()->{
            setContextMenu(getSpreadsheetViewContextMenu());
        });

        setGrid(grid);
        setEditable(true);
        
        // Listeners & handlers
        fixedRows.addListener(fixedRowsListener);
        fixedColumns.addListener(fixedColumnsListener);
    }
    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * Set a new Grid for the SpreadsheetView. This will be called by default by
     * {@link #SpreadsheetView(Grid)}. So this is useful when you want to
     * refresh your SpreadsheetView with a new model. This will keep the state
     * of your SpreadsheetView (position of the bar, number of fixedRows etc).
     * 
     * @param grid the new Grid
     */
    public final void setGrid(Grid grid) {
        // Reactivate that after
//        verifyGrid(grid);
        gridProperty.set(grid);
        initRowFix(grid);

        /**
         * We need to verify that the previous fixedRows are still compatible
         * with our new model
         */

        List<Integer> newFixedRows = new ArrayList<>();
        for (Integer rowFixed : getFixedRows()) {
            if (isRowFixable(rowFixed)) {
                newFixedRows.add(rowFixed);
            }
        }
        getFixedRows().setAll(newFixedRows);

        /**
         * We need to store the index of the fixedColumns and clear then because
         * we will keep reference to SpreadsheetColumn that no longer exist.
         */
        List<Integer> columnsFixed = new ArrayList<>();
        for (SpreadsheetColumn column : getFixedColumns()) {
            columnsFixed.add(getColumns().indexOf(column));
        }
        getFixedColumns().clear();

        /**
         * We try to save the width of the column as we save the height of our rows so that we preserve the state.
         */
        List<Double> widthColumns = new ArrayList<>();
        for(SpreadsheetColumn column:columns){
            widthColumns.add(column.getWidth());
        }
        
        // TODO move into a property
        if (grid.getRows() != null) {
            final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections
                    .observableArrayList(grid.getRows());
            cellsView.getItems().clear();
            cellsView.setItems(observableRows);

            final int columnCount = grid.getColumnCount();
            columns.clear();
            for (int i = 0; i < columnCount; ++i) {
                final int col = i;

                String columnHeader = grid.getColumnHeaders().size() > i ? grid
                        .getColumnHeaders().get(i) : Utils.getExcelLetterFromNumber(i);
                final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column = new TableColumn<>(
                        columnHeader);

                column.setEditable(true);
                // We don't want to sort the column
                column.setSortable(false);

                column.impl_setReorderable(false);

                // We assign a DataCell for each Cell needed (MODEL).
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell>, ObservableValue<SpreadsheetCell>>() {
                    @Override
                    public ObservableValue<SpreadsheetCell> call(
                            TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) {
                        return new ReadOnlyObjectWrapper<>(p.getValue().get(col));
                    }
                });
                // We create a SpreadsheetCell for each DataCell in order to
                // specify how to represent the DataCell(VIEW)
                column.setCellFactory(new Callback<TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>, TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell>>() {
                    @Override
                    public TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> call(
                            TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) {
                        return new CellView(handle);
                    }
                });
                final SpreadsheetColumn spreadsheetColumn = new SpreadsheetColumn(column, this, i);
                if(widthColumns.size() > i){
                    spreadsheetColumn.setPrefWidth(widthColumns.get(i));
                }
                columns.add(spreadsheetColumn);
                // We verify if this column was fixed before and try to re-fix
                // it.
                if (columnsFixed.contains((Integer) i) && spreadsheetColumn.isColumnFixable()) {
                    spreadsheetColumn.setFixed(true);
                }
            }
        }
        /**
         * We need to set the columns of the TableView in the JFX thread. Since
         * this method can be called from another thread, we execute the code here.
         */
        Platform.runLater(()->{
            cellsView.getColumns().clear();
            for(SpreadsheetColumn spreadsheetColumn:columns){
                cellsView.getColumns().add(spreadsheetColumn.column);
            }
        });
    }

    /**
     * Return a {@link TablePosition} of cell being currently edited.
     * 
     * @return a {@link TablePosition} of cell being currently edited.
     */
    public TablePosition<ObservableList<SpreadsheetCell>, ?> getEditingCell() {
        return cellsView.getEditingCell();
    }

    /**
     * Return an unmodifiable observableList of the {@link SpreadsheetColumn}
     * used.
     * 
     * @return An unmodifiable observableList.
     */
    public ObservableList<SpreadsheetColumn> getColumns() {
        return FXCollections.unmodifiableObservableList(columns);
    }

    /**
     * Return the model Grid used by the SpreadsheetView
     * 
     * @return the model Grid used by the SpreadsheetView
     */
    public final Grid getGrid() {
        return gridProperty.get();
    }

    /**
     * Return a {@link ReadOnlyObjectProperty} containing the current Grid
     * used in the SpreadsheetView.
     * @return a {@link ReadOnlyObjectProperty}.
     */
    public final ReadOnlyObjectProperty<Grid> gridProperty() {
        return gridProperty;
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
        return columnIndex < getColumns().size()
                ? getColumns().get(columnIndex).isColumnFixable() : null;
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
     * This DoubleProperty represents the with of the rowHeader. This is just
     * representing the width of the Labels, not the pickers.
     *
     * @return A DoubleProperty.
     */
    public final DoubleProperty rowHeaderWidthProperty(){
        return rowHeaderWidth;
    }
    
    /**
     * Specify a new width for the row header.
     *
     * @param value
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
     * @return An ObservableList of row indexes that display a picker.
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
     * @return the row Picker Callback.
     */
    public Callback<Integer, Void> getRowPickerCallback() {
        return rowPickerCallback;
    }

    /**
     * @return An ObservableList of column indexes that display a picker.
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
     * @return the columnPicker Callback.
     */
    public Callback<Integer, Void> getColumnPickerCallback() {
        return columnPickerCallback;
    }
    
    /**
     * This method will compute the best height for each line. That is to say
     * a height where each content of each cell could be fully visible.\n
     * Use this method wisely because it can degrade performance on great grid.
     */
    public void resizeRowsToFitContent(){
        getCellsViewSkin().resizeRowsToFitContent();
    }
    
    /**
     * This method will first apply {@link #resizeRowsToFitContent() } and then
     * take the highest height and apply it to every row.\n
     * Just as {@link #resizeRowsToFitContent() }, this method can be degrading
     * your performance on great grid.
     */
    public void resizeRowsToMaximum(){
        getCellsViewSkin().resizeRowsToMaximum();
    }
    
    /**
     * This method will wipe all changes made to the row's height and set all row's
     * height back to their default height defined in the model Grid.
     */
    public void resizeRowsToDefault(){
        getCellsViewSkin().resizeRowsToDefault();
    }
    
    /**
     * @param row
     * @return the height of a particular row of the SpreadsheetView.
     */
    public double getRowHeight(int row) {
         return getCellsViewSkin().getRowHeight(row);
    }
    
    /**
     * Return the selectionModel used by the SpreadsheetView.
     * 
     * @return {@link TableViewSelectionModel}
     */
    public TableViewSelectionModel<ObservableList<SpreadsheetCell>> getSelectionModel() {
        return cellsView.getSelectionModel();
    }

    /**
     * Return the editor associated with the CellType. (defined in
     * {@link SpreadsheetCellType#createEditor(SpreadsheetView)}. FIXME Maybe
     * keep the editor references inside the SpreadsheetCellType
     * 
     * @param cellType
     * @return the editor associated with the CellType.
     */
    public final Optional<SpreadsheetCellEditor> getEditor(SpreadsheetCellType<?> cellType) {
        SpreadsheetCellEditor cellEditor = editors.get(cellType);
        if (cellEditor == null) {
            cellEditor = cellType.createEditor(this);
            if(cellEditor == null){
                return Optional.empty();
            }
            editors.put(cellType, cellEditor);
        }
        return Optional.of(cellEditor);
    }

    /**
     * Sets the value of the property editable.
     * 
     * @param b
     */
    public final void setEditable(final boolean b) {
        cellsView.setEditable(b);
    }

    /**
     * Gets the value of the property editable.
     * 
     * @return a boolean telling if the SpreadsheetView is editable.
     */
    public final boolean isEditable() {
        return cellsView.isEditable();
    }

    /**
     * Specifies whether this SpreadsheetView is editable - only if the
     * SpreadsheetView, and the {@link SpreadsheetCell} within it are both
     * editable will a {@link SpreadsheetCell} be able to go into its editing
     * state.
     * 
     * @return the BooleanProperty associated with the editableProperty.
     */
    public final BooleanProperty editableProperty() {
        return cellsView.editableProperty();
    }

    
    /***************************************************************************
     * COPY / PASTE METHODS
     **************************************************************************/
    
    /**
     * Put the current selection into the ClipBoard. This can be overridden by
     * developers for custom behavior.
     */
    public void copyClipboard() {
        checkFormat();

        final ArrayList<GridChange> list = new ArrayList<>();
        @SuppressWarnings("rawtypes")
        final ObservableList<TablePosition> posList = getSelectionModel().getSelectedCells();

        for (final TablePosition<?, ?> p : posList) {
            SpreadsheetCell cell = getGrid().getRows().get(p.getRow()).get(p.getColumn());
            // Using SpreadsheetCell change to stock the information
            // FIXME a dedicated class should be used
            list.add(new GridChange(cell.getRow(), cell.getColumn(), null, cell.getItem()));
        }

        final ClipboardContent content = new ClipboardContent();
        content.put(fmt, list);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Try to paste the clipBoard to the specified position. Try to paste the
     * current selection into the Grid. If the two contents are not matchable,
     * then it's not pasted. This can be overridden by developers for custom
     * behavior.
     */
    public void pasteClipboard() {
        // FIXME Maybe move editableProperty to the model..
        if (!isEditable())
            return;

        checkFormat();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.getContent(fmt) != null) {

            @SuppressWarnings("unchecked")
            final ArrayList<GridChange> list = (ArrayList<GridChange>) clipboard.getContent(fmt);
            if(list.size() == 1){
                GridChange change = list.get(0);
                for(TablePosition position:getSelectionModel().getSelectedCells()){
                    final SpanType type = getSpanType(position.getRow(), position.getColumn());
                    if (type == SpanType.NORMAL_CELL || type == SpanType.ROW_VISIBLE) {
                        SpreadsheetCell cell = getGrid().getRows().get(position.getRow()).get(position.getColumn());
                        boolean succeed = cell.getCellType().match(change.getNewValue());
                        if (succeed) {
                            getGrid().setCellValue(cell.getRow(), cell.getColumn(),
                                    cell.getCellType().convertValue(change.getNewValue()));
                        }
                    }
                }
            }else{
                // TODO algorithm very bad
                int minRow = getGrid().getRowCount();
                int minCol = getGrid().getColumnCount();
                int maxRow = 0;
                int maxCol = 0;
                for (final GridChange p : list) {
                    final int tempcol = p.getColumn();
                    final int temprow = p.getRow();
                    if (tempcol < minCol) {
                        minCol = tempcol;
                    }
                    if (tempcol > maxCol) {
                        maxCol = tempcol;
                    }
                    if (temprow < minRow) {
                        minRow = temprow;
                    }
                    if (temprow > maxRow) {
                        maxRow = temprow;
                    }
                }

                final TablePosition<?, ?> p = cellsView.getFocusModel().getFocusedCell();

                final int offsetRow = p.getRow() - minRow;
                final int offsetCol = p.getColumn() - minCol;
                int row;
                int column;

                for (final GridChange change : list) {
                    row = change.getRow();
                    column = change.getColumn();
                    if (row + offsetRow < getGrid().getRowCount() && column + offsetCol < getGrid().getColumnCount()
                            && row + offsetRow >= 0 && column + offsetCol >= 0) {
                        final SpanType type = getSpanType(row + offsetRow, column + offsetCol);
                        if (type == SpanType.NORMAL_CELL || type == SpanType.ROW_VISIBLE) {
                            SpreadsheetCell cell = getGrid().getRows().get(row + offsetRow).get(column + offsetCol);
                            boolean succeed = cell.getCellType().match(change.getNewValue());
                            if (succeed) {
                                getGrid().setCellValue(cell.getRow(), cell.getColumn(),
                                        cell.getCellType().convertValue(change.getNewValue()));
                            }
                        }
                    }
                }
            }
            // To be improved
        } else if (clipboard.hasString()) {
            // final TablePosition<?,?> p =
            // cellsView.getFocusModel().getFocusedCell();
            //
            // SpreadsheetCell stringCell =
            // SpreadsheetCellType.STRING.createCell(0, 0, 1, 1,
            // clipboard.getString());
            // getGrid().getRows().get(p.getRow()).get(p.getColumn()).match(stringCell);

        }
    }

    /**
     * Create a menu on rightClick with two options: Copy/Paste This can be
     * overridden by developers for custom behavior.
     * 
     * @return the ContextMenu to use.
     */
    public ContextMenu getSpreadsheetViewContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();

        final MenuItem copyItem = new MenuItem(localize(asKey("spreadsheet.view.menu.copy")));
        copyItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("copySpreadsheetView.png"))));
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipboard();
            }
        });

        final MenuItem pasteItem = new MenuItem(localize(asKey("spreadsheet.view.menu.paste")));
        pasteItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("pasteSpreadsheetView.png"))));
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        
        final Menu cornerMenu = new Menu(localize(asKey("spreadsheet.view.menu.comment")));
        cornerMenu.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("comment.png"))));

        final MenuItem topLeftItem = new MenuItem("top-left");
        topLeftItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_LEFT);
            }
        });
        final MenuItem topRightItem = new MenuItem("top-right");
        topRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_RIGHT);
            }
        });
        final MenuItem bottomRightItem = new MenuItem("bottom-right");
        bottomRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.BOTTOM_RIGHT);
            }
        });
        final MenuItem bottomLeftItem = new MenuItem("bottom-left");
        bottomLeftItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.BOTTOM_LEFT);
            }
        });

        cornerMenu.getItems().addAll(topLeftItem, topRightItem, bottomRightItem, bottomLeftItem);
        
        contextMenu.getItems().addAll(copyItem, pasteItem, cornerMenu);
        return contextMenu;
    }

    /**
     * This method is called when pressing the "delete" key on the
     * SpreadsheetView. This will erase the values of selected cells. This can
     * be overridden by developers for custom behavior.
     */
    public void deleteSelectedCells() {
        for (TablePosition<ObservableList<SpreadsheetCell>, ?> position : getSelectionModel().getSelectedCells()) {
            getGrid().setCellValue(position.getRow(), position.getColumn(), null);
        }
    }
    
     /**
     * Return the {@link SpanType} of a cell, this is a shorcut for 
     * {@link Grid#getSpanType(org.controlsfx.control.spreadsheet.SpreadsheetView, int, int) }.
     * 
     * @param row
     * @param column
     * @return The {@link SpanType} of a cell
     */
    public SpanType getSpanType(final int row, final int column) {
        if (getGrid() == null) {
            return SpanType.NORMAL_CELL;
        }
        return getGrid().getSpanType(this, row, column);
    }

    /***************************************************************************
     * * Private/Protected Implementation * *
     **************************************************************************/

    private void initRowFix(Grid grid) {
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
    
    /**
     * Verify that the grid is well-formed. Can be quite time-consuming I guess
     * so I would like it not to be compulsory..
     * 
     * @param grid
     */
    private void verifyGrid(Grid grid) {
        verifyColumnSpan(grid);
    }

    private void verifyColumnSpan(Grid grid) {
        for (int i = 0; i < grid.getRows().size(); ++i) {
            ObservableList<SpreadsheetCell> row = grid.getRows().get(i);
            int count = 0;
            for (int j = 0; j < row.size(); ++j) {
                if (row.get(j).getColumnSpan() == 1) {
                    ++count;
                } else if (row.get(j).getColumnSpan() > 1) {
                    ++count;
                    SpreadsheetCell currentCell = row.get(j);
                    for (int k = j + 1; k < currentCell.getColumn() + currentCell.getColumnSpan(); ++k) {
                        if (!row.get(k).equals(currentCell)) {
                            throw new IllegalStateException("\n At row " + i + " and column " + j
                                    + ": this cell is in the range of a columnSpan but is different. \n"
                                    + "Every cell in a range of a ColumnSpan must be of the same instance.");
                        }
                        ++count;
                        ++j;
                    }
                } else {
                    throw new IllegalStateException("\n At row " + i + " and column " + j
                            + ": this cell has a negative columnSpan");
                }
            }
            if (count != grid.getColumnCount()) {
                throw new IllegalStateException("The row" + i
                        + " has a number of cells different of the columnCount declared in the grid.");
            }
        }
    }

    private void checkFormat() {
        if ((fmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) {
            fmt = new DataFormat("SpreadsheetView");
        }
    }

    /**
     * ********************************************************************* *
     * private listeners
     * ********************************************************************
     */

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
            for (SpreadsheetCell cell : getGrid().getRows().get(element)) {
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
            int indexColumn = getColumns().indexOf(element);

            String reason = "\n This column cannot be fixed.";
            for (ObservableList<SpreadsheetCell> row : getGrid().getRows()) {
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

    /**
     * Default Callback for the Row and column picker. It does nothing.
     */
    private static final Callback<Integer, Void> DEFAULT_CALLBACK = new Callback<Integer, Void>() {

        @Override
        public Void call(Integer p) {
            //no-op
            return null;
        }
    };
    private final ChangeListener<ContextMenu> contextMenuChangeListener = new ChangeListener<ContextMenu>() {
        
        @Override
        public void changed(ObservableValue<? extends ContextMenu> arg0, ContextMenu oldContextMenu, final ContextMenu newContextMenu) {
            if(oldContextMenu !=null){
                oldContextMenu.setOnShowing(null);
            }
            if(newContextMenu != null){
                newContextMenu.setOnShowing(new WeakEventHandler<>(hideContextMenuEventHandler));
            }
        }
    };
    
    private final EventHandler<WindowEvent> hideContextMenuEventHandler = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent arg0) {
            // We don't want to open a contextMenu when editing
            // because editors
            // have their own contextMenu
            if (getEditingCell() != null) {
                // We're being reactive but we want to be pro-active
                // so we may need a work-around.
                Platform.runLater(()->{
                    getContextMenu().hide();
                });
            }
        }
    };
}
