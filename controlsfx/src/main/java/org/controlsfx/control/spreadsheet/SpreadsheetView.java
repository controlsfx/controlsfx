/**
 * Copyright (c) 2013, 2015 ControlsFX
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
import impl.org.controlsfx.spreadsheet.RectangleSelection.GridRange;
import impl.org.controlsfx.spreadsheet.RectangleSelection.SelectionRange;
import impl.org.controlsfx.spreadsheet.SpreadsheetGridView;
import impl.org.controlsfx.spreadsheet.SpreadsheetHandle;
import impl.org.controlsfx.spreadsheet.TableViewSpanSelectionModel;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.WindowEvent;
import javafx.util.Pair;
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
 * <li>Rows can be resized just like columns with click &amp; drag.</li>
 * <li>Both row and column header can be visible or invisible.</li>
 * <li>Selection of several cells can be made with a click and drag.</li>
 * <li>A copy/paste context menu is accessible with a right-click. The usual
 * shortcuts are also working.</li>
 * </ul>
 * 
 * <br>
 * 
 * <h3>Fixing Rows and Columns</h3> 
 * <br>
 * You can fix some rows and some columns by right-clicking on their header. A
 * context menu will appear if it's possible to fix them. The label will then be
 * in italic and the background will turn to dark grey. 
 * <br>
 * You have also the possibility to fix them manually by adding and removing
 * items from {@link #getFixedRows()} and {@link #getFixedColumns()}. But you
 * are strongly advised to check if it's possible to do so with
 * {@link SpreadsheetColumn#isColumnFixable()} for the fixed columns and with
 * {@link #isRowFixable(int)} for the fixed rows. 
 * <br>
 * 
 * If you want to fix several rows or columns together, and they have a span
 * inside, you can call {@link #areRowsFixable(java.util.List) } or  {@link #areSpreadsheetColumnsFixable(java.util.List)
 * }
 * to verify if you can fix them. Be sure to add them all in once otherwise the
 * system will detect that a span is going out of bounds and will throw an
 * exception.
 *
 * Calling those methods prior
 * every move will ensure that no exception will be thrown.
 * <br>
 * You have also the possibility to deactivate these possibilities. For example,
 * you force some row/column to be fixed and then the user cannot change the 
 * settings. 
 * <br>
 * 
 * <h3>Headers</h3>
 * <br>
 * You can also access and toggle header's visibility by using the methods
 * provided like {@link #setShowRowHeader(boolean) } or {@link #setShowColumnHeader(boolean)
 * }.
 * 
 * <br>
 * 
 * <h3>Pickers</h3>
 * <br>
 * 
 * You can show some little images next to the headers. They will appear on the 
 * left of the VerticalHeader and on top on the HorizontalHeader. They are called
 * "picker" because they were used originally for picking a row or a column to 
 * insert in the SpreadsheetView.
 * <br>
 * But you can do anything you want with it. Simply put a row or a column index
 * in {@link #getRowPickers() } and {@link #getColumnPickers() } along with an
 * instance of {@link Picker}. You can override the {@link Picker#onClick() }
 * method in order to react when the user click on the picker.
 * <br>
 * The pickers will appear on the top of the column's header and on the left of 
 * the row's header.
 * <br>
 * 
 * <h3>Copy pasting</h3> You can copy any cell you want and paste it elsewhere.
 * Be aware that only the value inside will be pasted, not the style nor the
 * type. Thus the value you're trying to paste must be compatible with the
 * {@link SpreadsheetCellType} of the receiving cell. Pasting a Double into a
 * String will work but the reverse operation will not. 
 * <br>
 * See {@link SpreadsheetCellType} <i>Value Verification</i> documentation for more 
 * information.
 * <br>
 * A unique cell or a selection of several of them can be copied and pasted.
 * 
 * <br>
 * <br>
 * <h3>Code Samples</h3> Just like the {@link TableView}, you instantiate the
 * underlying model, a {@link Grid}. You will create some rows filled with {@link SpreadsheetCell}.
 * 
 * <br>
 * <br>
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
 * provided by the grid. Then you just need to instantiate the SpreadsheetView. <br>
 * <h3>Visual:</h3> <center><img src="spreadsheetView.png" alt="Screenshot of SpreadsheetView"></center>
 * 
 * @see SpreadsheetCell
 * @see SpreadsheetCellBase
 * @see SpreadsheetColumn
 * @see Grid
 * @see GridBase
 * @see Picker
 */
public class SpreadsheetView extends Control{

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/

    /**
     * The SpanType describes in which state each cell can be. When a spanning
     * is occurring, one cell is becoming larger and the others are becoming
     * invisible. Thus, that particular cell is masking the others. <br>
     * <br>
     * But the SpanType cannot be known in advance because it's evolving for
     * each cell during the lifetime of the {@link SpreadsheetView}. Suppose you
     * have a cell spanning in row, the first one is in a ROW_VISIBLE state, and
     * all the other below are in a ROW_SPAN_INVISIBLE state. But if the user is
     * scrolling down, the first will go out of sight. At that moment, the
     * second cell is switching from ROW_SPAN_INVISIBLE state to ROW_VISIBLE
     * state. <br>
     * <br>
     * 
     * <center><img src="spanType.png" alt="Screenshot of SpreadsheetView.SpanType"></center>
     * Refer to {@link SpreadsheetView} for more information.
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

    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader", true); //$NON-NLS-1$
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader", true); //$NON-NLS-1$

    private BitSet rowFix; // Compute if we can fix the rows or not.

    private final ObservableMap<Integer, Picker> rowPickers = FXCollections.observableHashMap();

    private final ObservableMap<Integer, Picker> columnPickers = FXCollections.observableHashMap();

    // Properties needed by the SpreadsheetView and managed by the skin (source
    // is the VirtualFlow)
    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
    private Map<SpreadsheetCellType<?>, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
    private final SpreadsheetViewSelectionModel selectionModel;
    
    /**
     * The vertical header width, just for the Label, not the Pickers.
     */
    private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);

    /**
     * Since the default with applied to TableColumn is 80. If a user sets a
     * width of 80, the column will be detected as having the default with and
     * therefore will be requested to be autosized. In order to prevent that, we
     * must detect which columns has been specifically set and which not. With
     * that BitSet, we are able to make the difference between a "default" 80
     * width applied by the system, and a 80 width applid by a user.
     */
    private final BitSet columnWidthSet = new BitSet();
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

        @Override
        protected boolean isColumnWidthSet(int indexColumn) {
            return columnWidthSet.get(indexColumn);
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
    
    /**
     * Used by {@link SpreadsheetColumn} internally in order to specify if a
     * column width has been set by the user.
     *
     * @param indexColumn
     */
    void columnWidthSet(int indexColumn) {
        columnWidthSet.set(indexColumn);
    }

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * This constructor will generate sample Grid with 100 rows and 15 columns.
     * All cells are typed as String (see {@link SpreadsheetCellType#STRING}).
     */
    public SpreadsheetView(){
        this(getSampleGrid());
        for(SpreadsheetColumn column: getColumns()){
            column.setPrefWidth(100);
        }
    }
    
    /**
     * Creates a SpreadsheetView control with the {@link Grid} specified.
     *
     * @param grid The Grid that contains the items to be rendered
     */
    public SpreadsheetView(final Grid grid) {
        super();
        //We want to recompute the rectangleHeight when a fixedRow is resized.
        addEventHandler(RowHeightEvent.ROW_HEIGHT_CHANGE, (RowHeightEvent event) -> {
            if(getFixedRows().contains(event.getRow()) && getCellsViewSkin() != null){
                getCellsViewSkin().computeFixedRowHeight();
            }
        });
        getStyleClass().add("SpreadsheetView"); //$NON-NLS-1$
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
        TableViewSpanSelectionModel tableViewSpanSelectionModel = new TableViewSpanSelectionModel(this,cellsView);
        cellsView.setSelectionModel(tableViewSpanSelectionModel);
        tableViewSpanSelectionModel.setCellSelectionEnabled(true);
        tableViewSpanSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        selectionModel = new SpreadsheetViewSelectionModel(this, tableViewSpanSelectionModel);

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
        cellsView.setOnKeyPressed(keyPressedHandler);

        /**
         * ContextMenu handling.
         */
        this.contextMenuProperty().addListener(new WeakChangeListener<>(contextMenuChangeListener));
        // The contextMenu creation must be on the JFX thread
        CellView.getValue(() -> {
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
        if(grid == null){
            return;
        }
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
        for (SpreadsheetColumn column : columns) {
            widthColumns.add(column.getWidth());
        }
        //We need to update the focused cell afterwards
        Pair<Integer, Integer> focusedPair = null;
        TablePosition focusedCell = cellsView.getFocusModel().getFocusedCell();
        if (focusedCell != null && focusedCell.getRow() != -1 && focusedCell.getColumn() != -1) {
            focusedPair = new Pair(focusedCell.getRow(), focusedCell.getColumn());
        }

        final Pair<Integer, Integer> finalPair = focusedPair;
        
        if (grid.getRows() != null) {
            final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections
                    .observableArrayList(grid.getRows());
            cellsView.getItems().clear();
            cellsView.setItems(observableRows);

            final int columnCount = grid.getColumnCount();
            columns.clear();
            for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
                final SpreadsheetColumn spreadsheetColumn = new SpreadsheetColumn(getTableColumn(grid, columnIndex), this, columnIndex, grid);
                if(widthColumns.size() > columnIndex){
                    spreadsheetColumn.setPrefWidth(widthColumns.get(columnIndex));
                }
                columns.add(spreadsheetColumn);
                // We verify if this column was fixed before and try to re-fix
                // it.
                if (columnsFixed.contains((Integer) columnIndex) && spreadsheetColumn.isColumnFixable()) {
                    spreadsheetColumn.setFixed(true);
                }
            }
        }
        
        List<Pair<Integer, Integer>> selectedCells = new ArrayList<>();
        for (TablePosition position : getSelectionModel().getSelectedCells()) {
            selectedCells.add(new Pair<>(position.getRow(), position.getColumn()));
        }
        
        
        /**
         * Since the TableView is added to the sceneGraph, it's not possible to
         * modify the columns in another thread. We normally should call
         * Platform.runLater() and exit. But in this particular case, we need to
         * add the tableColumn right now. So that when we exit this "setGrid"
         * method, we are sure we can manipulate all the elements.
         *
         * We also try to be smart here when we already have some columns in
         * order to re-use them and minimize the time used to add/remove
         * columns.
         */
        Runnable runnable = () -> {
            if (cellsView.getColumns().size() > grid.getColumnCount()) {
                cellsView.getColumns().remove(grid.getColumnCount(), cellsView.getColumns().size());
            } else if (cellsView.getColumns().size() < grid.getColumnCount()) {
                for (int i = cellsView.getColumns().size(); i < grid.getColumnCount(); ++i) {
                    cellsView.getColumns().add(columns.get(i).column);
                }
            }
            ((TableViewSpanSelectionModel) cellsView.getSelectionModel()).verifySelectedCells(selectedCells);
            //Just like the selected cell we update the focused cell.
            if(finalPair != null && finalPair.getKey() < getGrid().getRowCount() && finalPair.getValue() < getGrid().getColumnCount()){
                cellsView.getFocusModel().focus(finalPair.getKey(), cellsView.getColumns().get(finalPair.getValue()));
            }
        };
        
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            try {
                FutureTask future = new FutureTask(runnable, null);
                Platform.runLater(future);
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(SpreadsheetView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
     * Represents the current cell being edited, or null if there is no cell
     * being edited.
     *
     * @return the current cell being edited, or null if there is no cell being
     * edited.
     */
    public ReadOnlyObjectProperty<TablePosition<ObservableList<SpreadsheetCell>, ?>> editingCellProperty() {
        return cellsView.editingCellProperty();
    }

    /**
     * Return an ObservableList of the {@link SpreadsheetColumn} used. This list
     * is filled automatically by the SpreadsheetView. Adding and removing
     * columns should be done in the model {@link Grid}.
     *
     * @return An ObservableList of the {@link SpreadsheetColumn}
     */
    public final ObservableList<SpreadsheetColumn> getColumns() {
        return columns;
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
        return row >= 0 && row < rowFix.size() && isFixingRowsAllowed() ? rowFix.get(row) : false;
    }
    
    /**
     * Indicates whether a List of rows can be fixed or not.
     * @param list
     * @return true if the List of row can be fixed together.
     */
    public boolean areRowsFixable(List<? extends Integer> list) {
        if(list == null || list.isEmpty() || !isFixingRowsAllowed()){
            return false;
        }
        final Grid grid = getGrid();
        final int rowCount = grid.getRowCount();
        final ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        for (Integer row : list) {
            if (row == null || row < 0 || row >= rowCount) {
                return false;
            }
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {
                int maxSpan = 1;
                List<SpreadsheetCell> gridRow = rows.get(row);
                for (SpreadsheetCell cell : gridRow) {
                    //If the original row is not within this range, there is not need to look deeper.
                    if (!list.contains(cell.getRow())) {
                        return false;
                    }
                    //We only want to consider the original cell.
                    if (cell.getRowSpan() > maxSpan && cell.getRow() == row) {
                        maxSpan = cell.getRowSpan();
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
        return columnIndex >= 0 && columnIndex < getColumns().size() && isFixingColumnsAllowed()
                ? getColumns().get(columnIndex).isColumnFixable() : false;
    }

    /**
     * Indicates whether a List of {@link SpreadsheetColumn} can be fixed or
     * not.
     *
     * @param list
     * @return true if the List of columns can be fixed together.
     */
    public boolean areSpreadsheetColumnsFixable(List<? extends SpreadsheetColumn> list) {
        List<Integer> newList = new ArrayList<>();
        for (SpreadsheetColumn column : list) {
            if (column != null) {
                newList.add(columns.indexOf(column));
            }
        }
        return areColumnsFixable(newList);
    }

    /**
     * This method is the same as {@link #areSpreadsheetColumnsFixable(java.util.List)
     * } but is using a List of {@link SpreadsheetColumn} indexes.
     *
     * @param list
     * @return true if the List of columns can be fixed together.
     */
    public boolean areColumnsFixable(List<? extends Integer> list) {
        if (list == null || list.isEmpty() || !isFixingRowsAllowed()) {
            return false;
        }
        final Grid grid = getGrid();
        final int columnCount = grid.getColumnCount();
        final ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        for (Integer columnIndex : list) {
            if (columnIndex == null || columnIndex < 0 || columnIndex >= columnCount) {
                return false;
            }
            //If this column is not fixable, we need to identify the maximum span
            if (!isColumnFixable(columnIndex)) {
                int maxSpan = 1;
                SpreadsheetCell cell;
                for (List<SpreadsheetCell> row : rows) {
                    cell = row.get(columnIndex);
                    //If the original column is not within this range, there is not need to look deeper.
                    if (!list.contains(cell.getColumn())) {
                        return false;
                    }
                    //We only want to consider the original cell.
                    if (cell.getColumnSpan() > maxSpan && cell.getColumn() == columnIndex) {
                        maxSpan = cell.getColumnSpan();
                    }
                }
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
     * @return An ObservableMap with the row index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getRowPickers() {
        return rowPickers;
    }

    /**
     * @return An ObservableMap with the column index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getColumnPickers() {
        return columnPickers;
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
        //Sometime, the skin is not initialised yet..
        if (getCellsViewSkin() == null) {
            return getGrid().getRowHeight(row);
        } else {
            return getCellsViewSkin().getRowHeight(row);
        }
    }
    
    /**
     * Return the selectionModel used by the SpreadsheetView. 
     * 
     * @return {@link SpreadsheetViewSelectionModel}
     */
    public SpreadsheetViewSelectionModel getSelectionModel() {
        return selectionModel;
    }
    
    /**
     * Scrolls the SpreadsheetView so that the given row is visible.
     * @param row 
     */
    public void scrollToRow(int row){
        cellsView.scrollTo(row);
    }
    
    /**
     * Scrolls the SpreadsheetView so that the given {@link SpreadsheetColumn} is visible.
     * @param column 
     */
    public void scrollToColumn(SpreadsheetColumn column){
        cellsView.scrollToColumn(column.column);
    }
    
    /**
     *
     * Scrolls the SpreadsheetView so that the given column index is visible.
     *
     * @param columnIndex
     *
     */
    public void scrollToColumnIndex(int columnIndex) {
        cellsView.scrollToColumnIndex(columnIndex);
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
        if(cellType == null){
            return Optional.empty();
        }
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
            list.add(new GridChange(cell.getRow(), cell.getColumn(), null, cell.getItem() == null ? null : cell.getItem().toString()));
        }

        final ClipboardContent content = new ClipboardContent();
        content.put(fmt, list);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Paste one value from the clipboard over the whole selection.
     * @param change 
     */
    private void pasteOneValue(GridChange change) {
        for (TablePosition position : getSelectionModel().getSelectedCells()) {
            tryPasteCell(position.getRow(), position.getColumn(), change.getNewValue());
        }
    }

    /**
     * Try to paste the given value into the given position.
     * @param row
     * @param column
     * @param value 
     */
    private void tryPasteCell(int row, int column, Object value) {
        final SpanType type = getSpanType(row, column);
        if (type == SpanType.NORMAL_CELL || type == SpanType.ROW_VISIBLE) {
            SpreadsheetCell cell = getGrid().getRows().get(row).get(column);
            boolean succeed = cell.getCellType().match(value);
            if (succeed) {
                getGrid().setCellValue(cell.getRow(), cell.getColumn(),
                        cell.getCellType().convertValue(value));
            }
        }
    }

    /**
     * Try to paste the values given into the selection. If both selection are
     * rectangles and the number of rows of the source is equal of the numbers
     * of rows of the target AND number of columns of the target is a multiple
     * of the number of columns of the source, then we can paste.
     *
     * Same goes if we invert the rows and columns.
     * @param list
     */
    private void pasteMixedValues(ArrayList<GridChange> list) {
        SelectionRange sourceSelectionRange = new SelectionRange();
        sourceSelectionRange.fillGridRange(list);

        //It means we have a rectangle.
        if (sourceSelectionRange.getRange() != null) {
            SelectionRange targetSelectionRange = new SelectionRange();
            targetSelectionRange.fill(cellsView.getSelectionModel().getSelectedCells());
            if (targetSelectionRange.getRange() != null) {
                //If both selection are rectangle
                GridRange sourceRange = sourceSelectionRange.getRange();
                GridRange targetRange = targetSelectionRange.getRange();
                int sourceRowGap = sourceRange.getBottom() - sourceRange.getTop() + 1;
                int targetRowGap = targetRange.getBottom() - targetRange.getTop() + 1;

                int sourceColumnGap = sourceRange.getRight() - sourceRange.getLeft() + 1;
                int targetColumnGap = targetRange.getRight() - targetRange.getLeft() + 1;

                final int offsetRow = targetRange.getTop() - sourceRange.getTop();
                final int offsetCol = targetRange.getLeft() - sourceRange.getLeft();

                //If the numbers of rows are the same and the targetColumnGap is a multiple of sourceColumnGap
                if ((sourceRowGap == targetRowGap || targetRowGap == 1) && (targetColumnGap % sourceColumnGap) == 0) {
                    for (final GridChange change : list) {
                        int row = change.getRow() + offsetRow;
                        int column = change.getColumn() + offsetCol;
                        do {
                            if (row < getGrid().getRowCount() && column < getGrid().getColumnCount()
                                    && row >= 0 && column >= 0) {
                                tryPasteCell(row, column, change.getNewValue());
                            }
                        } while ((column = column + sourceColumnGap) <= targetRange.getRight());
                    }
                    //If the numbers of columns are the same and the targetRowGap is a multiple of sourceRowGap
                } else if ((sourceColumnGap == targetColumnGap || targetColumnGap == 1) && (targetRowGap % sourceRowGap) == 0) {
                    for (final GridChange change : list) {

                        int row = change.getRow() + offsetRow;
                        int column = change.getColumn() + offsetCol;
                        do {
                            if (row < getGrid().getRowCount() && column < getGrid().getColumnCount()
                                    && row >= 0 && column >= 0) {
                                tryPasteCell(row, column, change.getNewValue());
                            }
                        } while ((row = row + sourceRowGap) <= targetRange.getBottom());
                    }
                }
            }
        }
    }

    /**
     * If we have several source values to paste into one cell, we do it.
     *
     * @param list
     */
    private void pasteSeveralValues(ArrayList<GridChange> list) {
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
        final int rowCount = getGrid().getRowCount();
        final int columnCount = getGrid().getColumnCount();
        int row;
        int column;

        for (final GridChange change : list) {
            row = change.getRow() + offsetRow;
            column = change.getColumn() + offsetCol;
            if (row < rowCount && column < columnCount
                    && row >= 0 && column >= 0) {
                tryPasteCell(row, column, change.getNewValue());
            }
        }
    }
    
    /**
     * Try to paste the clipBoard to the specified position. Try to paste the
     * current selection into the Grid. If the two contents are not matchable,
     * then it's not pasted. This can be overridden by developers for custom
     * behavior.
     */
    public void pasteClipboard() {
        // FIXME Maybe move editableProperty to the model..
        List<TablePosition> selectedCells = cellsView.getSelectionModel().getSelectedCells();
        if (!isEditable() || selectedCells.isEmpty()) {
            return;
        }

        checkFormat();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.getContent(fmt) != null) {

            @SuppressWarnings("unchecked")
            final ArrayList<GridChange> list = (ArrayList<GridChange>) clipboard.getContent(fmt);
            if (list.size() == 1) {
                pasteOneValue(list.get(0));
            } else if (selectedCells.size() > 1) {
                pasteMixedValues(list);
            } else {
                pasteSeveralValues(list);
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

        final MenuItem copyItem = new MenuItem(localize(asKey("spreadsheet.view.menu.copy"))); //$NON-NLS-1$
        copyItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("copySpreadsheetView.png")))); //$NON-NLS-1$
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipboard();
            }
        });

        final MenuItem pasteItem = new MenuItem(localize(asKey("spreadsheet.view.menu.paste"))); //$NON-NLS-1$
        pasteItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("pasteSpreadsheetView.png")))); //$NON-NLS-1$
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN));
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        
        final Menu cornerMenu = new Menu(localize(asKey("spreadsheet.view.menu.comment"))); //$NON-NLS-1$
        cornerMenu.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("comment.png")))); //$NON-NLS-1$

        final MenuItem topLeftItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.top-left"))); //$NON-NLS-1$
        topLeftItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_LEFT);
            }
        });
        final MenuItem topRightItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.top-right"))); //$NON-NLS-1$
        topRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.TOP_RIGHT);
            }
        });
        final MenuItem bottomRightItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.bottom-right"))); //$NON-NLS-1$
        bottomRightItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell = getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                cell.activateCorner(SpreadsheetCell.CornerPosition.BOTTOM_RIGHT);
            }
        });
        final MenuItem bottomLeftItem = new MenuItem(localize(asKey("spreadsheet.view.menu.comment.bottom-left"))); //$NON-NLS-1$
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

    /**
     * This is called when setting a Grid. The main idea is to re-use
     * TableColumn if possible. Because we can have a great amount of time spent
     * in com.sun.javafx.css.StyleManager.forget when removing lots of columns
     * and adding new ones. So if we already have some, we can just re-use them
     * so we avoid doign all the fuss with the TableColumns.
     *
     * @param grid
     * @param columnIndex
     * @return
     */
    private TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> getTableColumn(Grid grid, int columnIndex) {

        TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;

        String columnHeader = grid.getColumnHeaders().size() > columnIndex ? grid
                .getColumnHeaders().get(columnIndex) : Utils.getExcelLetterFromNumber(columnIndex);

        if (columnIndex < cellsView.getColumns().size()) {
            column = (TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>) cellsView.getColumns().get(columnIndex);
            column.setText(columnHeader);
        } else {
            column = new TableColumn<>(columnHeader);

            column.setEditable(true);
            // We don't want to sort the column
            column.setSortable(false);

            column.impl_setReorderable(false);

            // We assign a DataCell for each Cell needed (MODEL).
            column.setCellValueFactory((TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> {
                if (columnIndex >= p.getValue().size()) {
                    return null;
                }
                return new ReadOnlyObjectWrapper<>(p.getValue().get(columnIndex));
            });
            // We create a SpreadsheetCell for each DataCell in order to
            // specify how to represent the DataCell(VIEW)
            column.setCellFactory((TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> new CellView(handle));
        }
        return column;
    }
    
    /**
     * This static method creates a sample Grid with 100 rows and 15 columns.
     * All cells are typed as String.
     *
     * @return the sample Grid
     * @see SpreadsheetCellType#STRING
     */
    private static Grid getSampleGrid() {
        GridBase gridBase = new GridBase(100, 15);
        List<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

        for (int row = 0; row < gridBase.getRowCount(); ++row) {
            ObservableList<SpreadsheetCell> currentRow = FXCollections.observableArrayList();
            for (int column = 0; column < gridBase.getColumnCount(); ++column) {
                currentRow.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "toto"));
            }
            rows.add(currentRow);
        }
        gridBase.setRows(rows);
        return gridBase;
    }
    
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
                            throw new IllegalStateException("\n At row " + i + " and column " + j //$NON-NLS-1$ //$NON-NLS-2$
                                    + ": this cell is in the range of a columnSpan but is different. \n" //$NON-NLS-1$
                                    + "Every cell in a range of a ColumnSpan must be of the same instance."); //$NON-NLS-1$
                        }
                        ++count;
                        ++j;
                    }
                } else {
                    throw new IllegalStateException("\n At row " + i + " and column " + j //$NON-NLS-1$ //$NON-NLS-2$
                            + ": this cell has a negative columnSpan"); //$NON-NLS-1$
                }
            }
            if (count != grid.getColumnCount()) {
                throw new IllegalStateException("The row" + i //$NON-NLS-1$
                        + " has a number of cells different of the columnCount declared in the grid."); //$NON-NLS-1$
            }
        }
    }

    private void checkFormat() {
        if ((fmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) { //$NON-NLS-1$
            fmt = new DataFormat("SpreadsheetView"); //$NON-NLS-1$
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
                if (c.wasAdded()) {
                    List<? extends Integer> newRows = c.getAddedSubList();
                    if(!areRowsFixable(newRows)){
                        throw new IllegalArgumentException(computeReason(newRows));
                    }
                    FXCollections.sort(fixedRows);
                }
                
                if(c.wasRemoved()){
                    //Handle this case.
                }
            }
        }
    };

        private String computeReason(List<? extends Integer> list) {
        String reason = "\n A row cannot be fixed. \n"; //$NON-NLS-1$

        for (Integer row : list) {
            //If this row is not fixable, we need to identify the maximum span
            if (!isRowFixable(row)) {

                int maxSpan = 1;
                List<SpreadsheetCell> gridRow = getGrid().getRows().get(row);
                for (SpreadsheetCell cell : gridRow) {
                    if(!list.contains(cell.getRow())){
                        reason += "The row " + row + " is inside a row span and the starting row " + cell.getRow() + " is not fixed.\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    //We only want to consider the original cell.
                    if (cell.getRowSpan() > maxSpan && cell.getRow() == row) {
                        maxSpan = cell.getRowSpan();
                    }
                }
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

    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends SpreadsheetColumn> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends SpreadsheetColumn> newColumns = c.getAddedSubList();
                    if (!areSpreadsheetColumnsFixable(newColumns)) {
                        List<Integer> newList = new ArrayList<>();
                        for (SpreadsheetColumn column : newColumns) {
                            if (column != null) {
                                newList.add(columns.indexOf(column));
                            }
                        }
                        throw new IllegalArgumentException(computeReason(newList));
                    }
                }
            }
        }

        private String computeReason(List<Integer> list) {

            String reason = "\n This column cannot be fixed."; //$NON-NLS-1$
            final ObservableList<ObservableList<SpreadsheetCell>> rows = getGrid().getRows();
            for (Integer columnIndex : list) {
                //If this row is not fixable, we need to identify the maximum span
                if (!isColumnFixable(columnIndex)) {
                    int maxSpan = 1;
                    SpreadsheetCell cell;
                    for (List<SpreadsheetCell> row : rows) {
                        cell = row.get(columnIndex);
                        //If the original column is not within this range, there is not need to look deeper.
                        if (!list.contains(cell.getColumn())) {
                            reason += "The column " + columnIndex + " is inside a column span and the starting column " + cell.getColumn() + " is not fixed.\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        }
                        //We only want to consider the original cell.
                        if (cell.getColumnSpan() > maxSpan && cell.getColumn() == columnIndex) {
                            maxSpan = cell.getColumnSpan();
                        }
                    }
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
    
    private final EventHandler<KeyEvent> keyPressedHandler = (KeyEvent keyEvent) -> {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getSelectionModel().getFocusedCell();
        // Go to the next row only if we're not editing
        if (getEditingCell() == null && KeyCode.ENTER.equals(keyEvent.getCode())) {
            if (position != null) {
                if(keyEvent.isShiftDown()){
                    getSelectionModel().clearAndSelectPreviousCell();
                }else{
                    getSelectionModel().clearAndSelectNextCell();
                }
                //We consume the event because we don't want to go in edition
                keyEvent.consume();
            }
            getCellsViewSkin().scrollHorizontally();
            // Go to next cell
        } else if (getEditingCell() == null && KeyCode.TAB.equals(keyEvent.getCode())) {
            if (position != null) {
                if (keyEvent.isShiftDown()) {
                    getSelectionModel().clearAndSelectLeftCell();
                } else {
                    getSelectionModel().clearAndSelectRightCell();
                }
            }
            //We consume the event because we don't want to loose focus
            keyEvent.consume();
            getCellsViewSkin().scrollHorizontally();
            // We want to erase values when delete key is pressed.
        } else if (KeyCode.DELETE.equals(keyEvent.getCode())) {
            deleteSelectedCells();
            /**
             * We want NOT to go in edition if we're pressing SHIFT and if we're
             * using the navigation keys. But we still want the user to go in
             * edition with SHIFT and some letters for example if he wants a
             * capital letter.
             * FIXME Add a test to prevent the Shift fail case.
             */
        }else if (keyEvent.getCode() != KeyCode.SHIFT && !keyEvent.isShortcutDown() 
                && !keyEvent.getCode().isNavigationKey() 
                && keyEvent.getCode() != KeyCode.ESCAPE) {
            getCellsView().edit(position.getRow(), position.getTableColumn());
        }
    };
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a row
     * with its mouse.
     */
    public static class RowHeightEvent extends Event {

        /**
         * This is the event used by {@link RowHeightEvent}.
         */
        public static final EventType<RowHeightEvent> ROW_HEIGHT_CHANGE = new EventType<>(Event.ANY, "RowHeightChange"); //$NON-NLS-1$

        private final int row;
        private final double height;

        public RowHeightEvent(int row, double height) {
            super(ROW_HEIGHT_CHANGE);
            this.row = row;
            this.height = height;
        }

        /**
         * Return the row index that has been resized.
         * @return the row index that has been resized.
         */
        public int getRow() {
            return row;
        }

        /**
         * Return the new height for this row.
         * @return the new height for this row.
         */
        public double getHeight() {
            return height;
        }
    }
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a column
     * with its mouse.
     */
    public static class ColumnWidthEvent extends Event {

        /**
         * This is the event used by {@link ColumnWidthEvent}.
         */
        public static final EventType<ColumnWidthEvent> COLUMN_WIDTH_CHANGE = new EventType<>(Event.ANY, "ColumnWidthChange"); //$NON-NLS-1$

        private final int column;
        private final double width;

        public ColumnWidthEvent(int column, double width) {
            super(COLUMN_WIDTH_CHANGE);
            this.column = column;
            this.width = width;
        }

        /**
         * Return the column index that has been resized.
         * @return the column index that has been resized.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Return the new width for this column.
         * @return the new width for this column.
         */
        public double getWidth() {
            return width;
        }
    }
}
