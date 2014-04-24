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

import impl.org.controlsfx.spreadsheet.CellView;
import impl.org.controlsfx.spreadsheet.GridRow;
import impl.org.controlsfx.spreadsheet.SpreadsheetGridView;
import impl.org.controlsfx.spreadsheet.GridViewSkin;
import impl.org.controlsfx.spreadsheet.SpreadsheetHandle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.NonIterableChange.GenericAddRemoveChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.SelectedCellsMap;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
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
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

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
 * <li>A copy/paste context menu is accessible with a right-click or the usual
 * shortcuts.</li>
 * </ul>
 * 
 * <br/>
 * 
 * <h3>Fixing Rows and Columns</h3> You can fix some rows and some columns by
 * right-clicking on their header. A context menu will appear if it's possible to fix them. 
 * The label will then be in italic and the background will turn to dark grey. 
 * Keep in mind that only columns without any spanning cells can be fixed.
 * 
 * And that and only rows without row-spanning cells can be fixed. <br/>
 * You have also the possibility to fix them manually by adding and removing
 * items from {@link #getFixedRows()} and {@link #getFixedColumns()}. But you
 * are strongly advised to check if it's possible to do so with
 * {@link SpreadsheetColumn#isColumnFixable()} for the fixed columns and with
 * {@link #isRowFixable(int)} for the fixed rows. Calling those methods prior
 * every move will ensure that no exception will be thrown.
 * 
 * <br/>
 * <br/>
 * 
 * <h3>Copy pasting</h3> You can copy every cell you want to paste it elsewhere.
 * Be aware that only the value inside will be pasted, not the style nor the
 * type. Thus the value you're trying to paste must be compatible with the
 * {@link SpreadsheetCellType} of the receiving cell. Pasting a Double into a
 * String will work but the reverse operation will not. <br/>
 * A unique cell or a selection of several of them can be copied and pasted.
 * 
 * <br/>
 * <br/>
 * <h3>Code Samples</h3> Just like the {@link TableView}, you instantiate the
 * underlying model, a {@link Grid}. You will create some ObservableList<
 * {@link SpreadsheetCell}> filled with {@link SpreadsheetCell}.
 * 
 * <br/>
 * <br/>
 * 
 * <pre>
 * int rowCount = 15;
 *     int columnCount = 10;
 *     GridBase grid = new GridBase(rowCount, columnCount);
 *     
 *     ObservableList&lt;ObservableList&lt;SpreadsheetCell&lt;&lt; rows = FXCollections.observableArrayList();
 *     for (int row = 0; row &lt; grid.getRowCount(); ++row) {
 *         final ObservableList&lt;SpreadsheetCell&lt; list = FXCollections.observableArrayList();
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
 * <h3>Visual:</h3> <center><img src="spreadsheetView.png"></center>
 * 
 * @see SpreadsheetCell
 * @see SpreadsheetColumn
 * @see Grid
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

    // Properties needed by the SpreadsheetView and managed by the skin (source
    // is the VirtualFlow)
    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
    private Map<SpreadsheetCellType<?>, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
    private BitSet rowFix; // Compute if we can fix the rows or not.
    // private ObservableSet<SpreadsheetCell> modifiedCells =
    // FXCollections.observableSet();
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
        SpreadsheetViewSelectionModel selectionModel = new SpreadsheetViewSelectionModel(this);
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
                .addListener((ChangeListener<TablePosition>) (ChangeListener<?>) new FocusModelListener(this));

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
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                setContextMenu(getSpreadsheetViewContextMenu());
            }
        };
        Platform.runLater(r);

        setGrid(grid);
        setEditable(true);

        // Listeners & handlers
        fixedRows.addListener(fixedRowsListener);
        fixedColumns.addListener(fixedColumnsListener);

        // getModifiedCells().addListener(modifiedCellsListener);
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
     * @param grid
     *            the new Grid
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

                String columnHeader = ((GridBase) grid).getColumnHeaders().size() > i ? ((GridBase) grid)
                        .getColumnHeaders().get(i) : getExcelLetterFromNumber(i);
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

    public final ReadOnlyObjectProperty<Grid> gridProperty() {
        return gridProperty;
    }

    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader", true);

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

    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader", true);

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
     *         unfixing some rows.
     */
    public ReadOnlyBooleanProperty fixingRowsAllowedProperty() {
        return fixingRowsAllowedProperty;
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
     * Return the height of a particular row of the SpreadsheetView. \n
     * @param row
     * @return 
     */
    public double getRowHeight(int row) {
         return getCellsViewSkin().getRowHeight(row);
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
        return columnIndex < getColumns().size() ? getColumns().get(columnIndex).isColumnFixable() : null;
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
     *         unfixing some columns.
     */
    public ReadOnlyBooleanProperty fixingColumnsAllowedProperty() {
        return fixingColumnsAllowedProperty;
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
     * Return an ObservableSet of the modified {@link SpreadsheetCell}.
     * 
     * @return an ObservableSet of the modified {@link SpreadsheetCell}.
     */
    // public ObservableSet<SpreadsheetCell> getModifiedCells() {
    // return modifiedCells;
    // }

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

        final MenuItem copyItem = new MenuItem("Copy");
        copyItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("copySpreadsheetView.png"))));
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipboard();
            }
        });

        final MenuItem pasteItem = new MenuItem("Paste");
        pasteItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("pasteSpreadsheetView.png"))));
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        
        final MenuItem commentedItem = new MenuItem("Comment cell");
        commentedItem.setGraphic(new ImageView(new Image(SpreadsheetView.class
                .getResourceAsStream("comment.png"))));
        commentedItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> pos = cellsView.getFocusModel().getFocusedCell();
                SpreadsheetCell cell =  getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
               cell.setCommented(!cell.isCommented());
            }
        });
        
        contextMenu.getItems().addAll(copyItem, pasteItem, commentedItem);
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

    /***************************************************************************
     * * Private/Protected Implementation * *
     **************************************************************************/

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

    /**
     * Return the {@link SpanType} of a cell.
     * 
     * @param row
     * @param column
     * @return
     */
    private SpanType getSpanType(final int row, final int column) {
        if (getGrid() == null) {
            return SpanType.NORMAL_CELL;
        }
        return getGrid().getSpanType(this, row, column);
    }

    /**
     * Return the {@link GridRow} at the specified index
     * 
     * @param index
     * @return
     */
    private GridRow getNonFixedRow(int index) {
        GridViewSkin skin = (GridViewSkin) cellsView.getSkin();
        return skin.getRow(index);
    }

    private void initRowFix(Grid grid) {
        ObservableList<ObservableList<SpreadsheetCell>> rows = grid.getRows();
        rowFix = new BitSet(rows.size());
        rows: for (int r = 0; r < rows.size(); ++r) {
            ObservableList<SpreadsheetCell> row = rows.get(r);
            for (SpreadsheetCell cell : row) {
                if (cell.getRowSpan() > 1) {
                    continue rows;
                }
            }
            rowFix.set(r);
        }
    }

    /***************************************************************************
     * COPY / PASTE METHODS
     **************************************************************************/

    private void checkFormat() {
        if ((fmt = DataFormat.lookupMimeType("SpreadsheetView")) == null) {
            fmt = new DataFormat("SpreadsheetView");
        }
    }

    /**************************************************************************
     * 
     * FOCUS MODEL
     * 
     * *************************************************************************/

    class FocusModelListener implements ChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>> {

        private final TableView.TableViewFocusModel<ObservableList<SpreadsheetCell>> tfm;

        public FocusModelListener(SpreadsheetView spreadsheetView) {
            tfm = cellsView.getFocusModel();
        }

        @Override
        public void changed(ObservableValue<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> ov,
                final TablePosition<ObservableList<SpreadsheetCell>, ?> t,
                final TablePosition<ObservableList<SpreadsheetCell>, ?> t1) {
            final SpreadsheetView.SpanType spanType = getSpanType(t1.getRow(), t1.getColumn());
            switch (spanType) {
            case ROW_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go below.
                if (!isPressed() && t.getColumn() == t1.getColumn() && t.getRow() == t1.getRow() - 1) {
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(getTableRowSpan(t), t.getTableColumn());
                        }
                    };
                    Platform.runLater(r);

                } else {
                    // If the current focused cell if hidden by row span, we go
                    // above
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(t1.getRow() - 1, t1.getTableColumn());
                        }
                    };
                    Platform.runLater(r);
                }

                break;
            case BOTH_INVISIBLE:
                // If the current focused cell if hidden by a both (row and
                // column) span, we go left-above
                final Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        tfm.focus(t1.getRow() - 1, cellsView.getColumns().get(t1.getColumn() - 1));
                    }
                };
                Platform.runLater(r);
                break;
            case COLUMN_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go right.
                if (!isPressed() && t.getColumn() == t1.getColumn() - 1 && t.getRow() == t1.getRow()) {

                    final Runnable r2 = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(t.getRow(), getTableColumnSpan(t));
                        }
                    };
                    Platform.runLater(r2);
                } else {
                    // If the current focused cell if hidden by column span, we
                    // go left

                    final Runnable r2 = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(t1.getRow(), cellsView.getColumns().get(t1.getColumn() - 1));
                        }
                    };
                    Platform.runLater(r2);
                }
            default:
                break;
            }
        }
    }

    /**************************************************************************
     * 
     * SELECTION MODEL
     * 
     * *************************************************************************/

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     * 
     * @param t
     *            the current TablePosition
     * @return
     */
    private TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumnSpan(final TablePosition<?, ?> t) {
        return cellsView.getVisibleLeafColumn(t.getColumn()
                + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan());
    }

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     * 
     * @param t
     *            the current TablePosition
     * @return
     */
    private int getTableColumnSpanInt(final TablePosition<?, ?> t) {
        return t.getColumn() + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan();
    }

    /**
     * Return the Row number right after the current TablePosition (including
     * the RowSpan to be on a visible Cell)
     * 
     * @param t
     * @param spreadsheetView
     * @return
     */
    private int getTableRowSpan(final TablePosition<?, ?> t) {
        return cellsView.getItems().get(t.getRow()).get(t.getColumn()).getRowSpan()
                + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getRow();
    }

    /**
     * For a position, return the Visible Cell associated with It can be the top
     * of the span cell if it's visible, or it can be the first row visible if
     * we have scrolled
     * 
     * @param row
     * @param column
     * @param col
     * @return
     */
    private TablePosition<ObservableList<SpreadsheetCell>, ?> getVisibleCell(int row,
            TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col) {
        final SpreadsheetView.SpanType spanType = getSpanType(row, col);
        switch (spanType) {
        case NORMAL_CELL:
        case ROW_VISIBLE:
            return new TablePosition<>(cellsView, row, column);
        case BOTH_INVISIBLE:
        case COLUMN_SPAN_INVISIBLE:
        case ROW_SPAN_INVISIBLE:
        default:
            final SpreadsheetCell cellSpan = cellsView.getItems().get(row).get(col);
            if (getCellsViewSkin().getCellsSize() != 0 && getNonFixedRow(0).getIndex() <= cellSpan.getRow()) {
                return new TablePosition<>(cellsView, cellSpan.getRow(), cellsView.getColumns().get(
                        cellSpan.getColumn()));

            } else { // If it's not, then it's the firstkey
                return new TablePosition<>(cellsView, getNonFixedRow(0).getIndex(), cellsView.getColumns().get(
                        cellSpan.getColumn()));
            }
        }
    }

    /**
     * A {@link SelectionModel} implementation for the {@link SpreadsheetView}
     * control.
     * 
     * @param <S>
     */
    private class SpreadsheetViewSelectionModel extends
            TableView.TableViewSelectionModel<ObservableList<SpreadsheetCell>> {

        private boolean ctrl = false; // Register state of 'ctrl' key
        private boolean shift = false; // Register state of 'shift' key
        private boolean key = false; // Register if we last touch the keyboard
                                     // or the mouse
        private boolean drag = false; // register if we are dragging (no
                                      // edition)
        private MouseEvent mouseEvent;
        private boolean makeAtomic;
        // the only 'proper' internal data structure, selectedItems and
        // selectedIndices
        // are both 'read-only and unbacked'.
        private final SelectedCellsMap<TablePosition<ObservableList<SpreadsheetCell>, ?>> selectedCellsMap;

        // we create a ReadOnlyUnbackedObservableList of selectedCells here so
        // that we can fire custom list change events.
        private final ReadOnlyUnbackedObservableList<TablePosition<ObservableList<SpreadsheetCell>, ?>> selectedCellsSeq;

        /**
         * Make the tableView move when selection operating outside bounds
         */
        private final Timeline timer;

        EventHandler<ActionEvent> timerEventHandler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                GridViewSkin skin = (GridViewSkin) getCellsViewSkin();
                if (mouseEvent != null && !cellsView.contains(mouseEvent.getX(), mouseEvent.getY())) {
                    double sceneX = mouseEvent.getSceneX();
                    double sceneY = mouseEvent.getSceneY();
                    double layoutX = cellsView.getLayoutX();
                    double layoutY = cellsView.getLayoutY();
                    double layoutXMax = layoutX + cellsView.getWidth();
                    double layoutYMax = layoutY + cellsView.getHeight();

                    if (sceneX > layoutXMax)
                        skin.getHBar().increment();
                    else if (sceneX < layoutX)
                        skin.getHBar().decrement();
                    if (sceneY > layoutYMax)
                        skin.getVBar().increment();
                    else if (sceneY < layoutY)
                        skin.getVBar().decrement();
                }
            }
        };
        /**
         * When the drag is over, we remove the listener and stop the timer
         */
        private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                drag = false;
                timer.stop();
                removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
            }
        };

        private final EventHandler<KeyEvent> keyPressedEventHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                key = true;
                ctrl = t.isControlDown();
                shift = t.isShiftDown();
            }
        };
        private final EventHandler<MouseEvent> mousePressedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                key = false;
                ctrl = t.isControlDown();
                shift = t.isShiftDown();
            }
        };
        
        private final EventHandler<MouseEvent> onDragDetectedEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                cellsView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
                drag = true;
                timer.setCycleCount(Timeline.INDEFINITE);
                timer.play();
            }
        };
        private final EventHandler<MouseEvent> onMouseDragEventHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                mouseEvent = e;
            }
        };
        private final ListChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>> listChangeListener = new ListChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>>() {
            @Override
            public void onChanged(
                    final ListChangeListener.Change<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> c) {
                handleSelectedCellsListChangeEvent(c);
            }
        };
        /***********************************************************************
         * 
         * Constructors
         * 
         **********************************************************************/

        public SpreadsheetViewSelectionModel(SpreadsheetView spreadsheetView) {
            super(spreadsheetView.cellsView);
            final SpreadsheetGridView cellsView = spreadsheetView.cellsView;
            timer = new Timeline(new KeyFrame(Duration.millis(100), new WeakEventHandler<>((timerEventHandler))));
            cellsView.addEventHandler(KeyEvent.KEY_PRESSED, new WeakEventHandler<>(keyPressedEventHandler));

            cellsView.setOnMousePressed(new WeakEventHandler<>(mousePressedEventHandler));
            cellsView.setOnDragDetected(new WeakEventHandler<>(onDragDetectedEventHandler));

            cellsView.setOnMouseDragged(new WeakEventHandler<>(onMouseDragEventHandler));

            selectedCellsMap = new SelectedCellsMap<>(new WeakListChangeListener<>(listChangeListener));
                    

            selectedCellsSeq = new ReadOnlyUnbackedObservableList<TablePosition<ObservableList<SpreadsheetCell>, ?>>() {
                @Override
                public TablePosition<ObservableList<SpreadsheetCell>, ?> get(int i) {
                    return selectedCellsMap.get(i);
                }

                @Override
                public int size() {
                    return selectedCellsMap.size();
                }
            };
        }

        private void handleSelectedCellsListChangeEvent(
                ListChangeListener.Change<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> c) {
            if (makeAtomic) {
                return;
            }

            selectedCellsSeq.callObservers(new MappingChange<>(c, MappingChange.NOOP_MAP, selectedCellsSeq));
            c.reset();
        }

        /**
         * *********************************************************************
         * * Public selection API * *
         * ********************************************************************
         */
        private TablePosition<ObservableList<SpreadsheetCell>, ?> old = null;

        @Override
        public void select(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
            if (row < 0 || row >= getItemCount()) {
                return;
            }

            // if I'm in cell selection mode but the column is null, I don't
            // want
            // to select the whole row instead...
            if (isCellSelectionEnabled() && column == null) {
                return;
            }
            // Variable we need for algorithm
            TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal = new TablePosition<>(getTableView(), row,
                    column);

            final SpreadsheetView.SpanType spanType = getSpanType(row, posFinal.getColumn());

            /**
             * We check if we are on covered cell. If so we have the algorithm
             * of the focus model to give the selection to the right cell.
             * 
             */
            switch (spanType) {
            case ROW_SPAN_INVISIBLE:
                // If we notice that the new selected cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go below.
                // We make sure that old is not null, and that the move is
                // initiated by keyboard.
                // Because if it's a click, then we just want to go on the
                // clicked cell (not below)
                if (old != null && key && !shift && old.getColumn() == posFinal.getColumn()
                        && old.getRow() == posFinal.getRow() - 1) {
                    posFinal = getVisibleCell(getTableRowSpan(old), old.getTableColumn(), old.getColumn());
                } else {
                    // If the current selected cell if hidden by row span, we go
                    // above
                    posFinal = getVisibleCell(row, column, posFinal.getColumn());
                }
                break;
            case BOTH_INVISIBLE:
                // If the current selected cell if hidden by a both (row and
                // column) span, we go left-above
                posFinal = getVisibleCell(row, column, posFinal.getColumn());
                break;
            case COLUMN_SPAN_INVISIBLE:
                // If we notice that the new selected cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go right.
                if (old != null && key && !shift && old.getColumn() == posFinal.getColumn() - 1
                        && old.getRow() == posFinal.getRow()) {
                    posFinal = getVisibleCell(old.getRow(), getTableColumnSpan(old), getTableColumnSpanInt(old));
                } else {
                    // If the current selected cell if hidden by column span, we
                    // go left
                    posFinal = getVisibleCell(row, column, posFinal.getColumn());
                }
            default:
                break;
            }

            // This is to handle edition
            if (posFinal.equals(old) && !ctrl && !shift && !drag) {
                // If we are on an Invisible row or both (in diagonal), we need
                // to force the edition
                if (spanType == SpreadsheetView.SpanType.ROW_SPAN_INVISIBLE
                        || spanType == SpreadsheetView.SpanType.BOTH_INVISIBLE) {
                    final TablePosition<ObservableList<SpreadsheetCell>, ?> FinalPos = new TablePosition<>(cellsView,
                            posFinal.getRow(), posFinal.getTableColumn());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            cellsView.edit(FinalPos.getRow(), FinalPos.getTableColumn());
                        }
                    };
                    Platform.runLater(r);
                }
            }
            old = posFinal;

            selectedCellsMap.add(posFinal);

            updateScroll(posFinal);
            addSelectedRowsAndColumns(posFinal);

            setSelectedIndex(posFinal.getRow());
            setSelectedItem(getModelItem(posFinal.getRow()));
            if (getTableView().getFocusModel() == null) {
                return;
            }

            getTableView().getFocusModel().focus(posFinal.getRow(), posFinal.getTableColumn());
        }

        /**
         * We try to make visible the rows that may be hidden by Fixed rows.
         * 
         * @param posFinal
         */
        private void updateScroll(TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal) {

            /**
             * We don't want to do any scroll when dragging or selecting with
             * click. Only keyboard action arrow action.
             */
            if (!drag && key && getCellsViewSkin().getCellsSize() != 0 && getFixedRows().size() != 0) {

                int start = getCellsViewSkin().getRow(0).getIndex();
                double posFinalOffset = 0;
                for (int j = start; j < posFinal.getRow(); ++j) {
                    posFinalOffset += getSpreadsheetViewSkin().getRowHeight(j);
                }

                if (getCellsViewSkin().getFixedRowHeight() > posFinalOffset) {
                    cellsView.scrollTo(posFinal.getRow());
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public void clearSelection(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {

            final TablePosition<ObservableList<SpreadsheetCell>, ?> tp = new TablePosition<>(getTableView(), row,
                    column);
            TablePosition<ObservableList<SpreadsheetCell>, ?> tp1;
            if ((tp1 = isSelectedRange(row, column, tp.getColumn())) != null) {
                selectedCellsMap.remove(tp1);
                removeSelectedRowsAndColumns(tp1);
                focus(tp1.getRow());
            } else {
                for (TablePosition<ObservableList<SpreadsheetCell>, ?> pos : getSelectedCells()) {
                    if (pos.equals(tp)) {
                        selectedCellsMap.remove(pos);
                        removeSelectedRowsAndColumns(pos);
                        // give focus to this cell index
                        focus(row);
                        return;
                    }
                }
            }
        }

        @Override
        public void selectRange(int minRow, TableColumnBase<ObservableList<SpreadsheetCell>, ?> minColumn, int maxRow,
                TableColumnBase<ObservableList<SpreadsheetCell>, ?> maxColumn) {
            makeAtomic = true;

            final int itemCount = getItemCount();

            final int minColumnIndex = getTableView().getVisibleLeafIndex(
                    (TableColumn<ObservableList<SpreadsheetCell>, ?>) minColumn);
            final int maxColumnIndex = getTableView().getVisibleLeafIndex(
                    (TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn);
            final int _minColumnIndex = Math.min(minColumnIndex, maxColumnIndex);
            final int _maxColumnIndex = Math.max(minColumnIndex, maxColumnIndex);
            HashSet<Integer> selectedRows = new HashSet<>();
            HashSet<Integer> selectedColumns = new HashSet<>();

            for (int _row = minRow; _row <= maxRow; _row++) {
                for (int _col = _minColumnIndex; _col <= _maxColumnIndex; _col++) {
                    // begin copy/paste of select(int, column) method (with some
                    // slight modifications)
                    if (_row < 0 || _row >= itemCount)
                        continue;

                    final TableColumn<ObservableList<SpreadsheetCell>, ?> column = getTableView().getVisibleLeafColumn(
                            _col);

                    // if I'm in cell selection mode but the column is null, I
                    // don't want
                    // to select the whole row instead...
                    if (column == null)
                        continue;

                    TablePosition<ObservableList<SpreadsheetCell>, ?> pos = new TablePosition<>(getTableView(), _row,
                            column);

                    pos = getVisibleCell(_row, column, pos.getColumn());

                    // We store all the selectedColumn and Rows, we will update
                    // just once at the end
                    final SpreadsheetCell cell = cellsView.getItems().get(pos.getRow()).get(pos.getColumn());
                    for (int i = cell.getRow(); i < cell.getRowSpan() + cell.getRow(); ++i) {
                        selectedColumns.add(i);
                        for (int j = cell.getColumn(); j < cell.getColumnSpan() + cell.getColumn(); ++j) {
                            selectedRows.add(j);
                        }
                    }
                    selectedCellsMap.add(pos);

                    // end copy/paste
                }
            }
            makeAtomic = false;

            // Then we update visuals just once
            getSpreadsheetViewSkin().getSelectedRows().addAll(selectedColumns);
            getSpreadsheetViewSkin().getSelectedColumns().addAll(selectedRows);

            // fire off events
            setSelectedIndex(maxRow);
            setSelectedItem(getModelItem(maxRow));
            if (getTableView().getFocusModel() == null) {
                return;
            }

            getTableView().getFocusModel().focus(maxRow, (TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn);

            final int startChangeIndex = selectedCellsMap.indexOf(new TablePosition<>(getTableView(), minRow,
                    (TableColumn<ObservableList<SpreadsheetCell>, ?>) minColumn));
            final int endChangeIndex = selectedCellsMap.indexOf(new TablePosition<>(getTableView(), maxRow,
                    (TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn));
            handleSelectedCellsListChangeEvent(new NonIterableChange.SimpleAddChange<>(startChangeIndex,
                    endChangeIndex + 1, selectedCellsSeq));
        }

        @Override
        public void selectAll() {
            if (getSelectionMode() == SelectionMode.SINGLE)
                return;

            quietClearSelection();

            List<TablePosition<ObservableList<SpreadsheetCell>, ?>> indices = new ArrayList<>();
            TableColumn<ObservableList<SpreadsheetCell>, ?> column;
            TablePosition<ObservableList<SpreadsheetCell>, ?> tp = null;

            for (int col = 0; col < getTableView().getVisibleLeafColumns().size(); col++) {
                column = getTableView().getVisibleLeafColumns().get(col);
                for (int row = 0; row < getItemCount(); row++) {
                    tp = new TablePosition<>(getTableView(), row, column);
                    indices.add(tp);
                }
            }
            selectedCellsMap.setAll(indices);

            // Then we update visuals just once
            ArrayList<Integer> selectedColumns = new ArrayList<>();
            for (int col = 0; col < getGrid().getColumnCount(); col++) {
                selectedColumns.add(col);
            }

            ArrayList<Integer> selectedRows = new ArrayList<>();
            for (int row = 0; row < getGrid().getRowCount(); row++) {
                selectedRows.add(row);
            }
            getSpreadsheetViewSkin().getSelectedRows().addAll(selectedRows);
            getSpreadsheetViewSkin().getSelectedColumns().addAll(selectedColumns);

            if (tp != null) {
                select(tp.getRow(), tp.getTableColumn());
                getTableView().getFocusModel().focus(tp.getRow(), tp.getTableColumn());
            }
        }

        @Override
        public boolean isSelected(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
            // When in cell selection mode, we currently do NOT support
            // selecting
            // entire rows, so a isSelected(row, null)
            // should always return false.
            if (column == null || row < 0) {
                return false;
            }

            int columnIndex = getTableView().getVisibleLeafIndex(column);

            if (getCellsViewSkin().getCellsSize() != 0) {
                TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal = getVisibleCell(row, column, columnIndex);
                return selectedCellsMap.isSelected(posFinal.getRow(), posFinal.getColumn());
            } else {
                return selectedCellsMap.isSelected(row, columnIndex);
            }
        }

        /**
         * Return the tablePosition of a selected cell inside a spanned cell if
         * any.
         * 
         * @param row
         * @param column
         * @param col
         * @return
         */
        @SuppressWarnings("unchecked")
        public TablePosition<ObservableList<SpreadsheetCell>, ?> isSelectedRange(int row,
                TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col) {

            if (column == null && row >= 0) {
                return null;
            }

            final SpreadsheetCell cellSpan = cellsView.getItems().get(row).get(col);
            final int infRow = cellSpan.getRow();
            final int supRow = infRow + cellSpan.getRowSpan();

            final int infCol = cellSpan.getColumn();
            final int supCol = infCol + cellSpan.getColumnSpan();

            for (final TablePosition<ObservableList<SpreadsheetCell>, ?> tp : getSelectedCells()) {
                if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol
                        && tp.getColumn() < supCol) {
                    return tp;
                }
            }
            return null;
        }

        /**
         * *********************************************************************
         * * Support code * *
         * ********************************************************************
         */

        private void addSelectedRowsAndColumns(TablePosition<?, ?> t) {
            final SpreadsheetCell cell = cellsView.getItems().get(t.getRow()).get(t.getColumn());
            for (int i = cell.getRow(); i < cell.getRowSpan() + cell.getRow(); ++i) {
                getSpreadsheetViewSkin().getSelectedRows().add(i);
                for (int j = cell.getColumn(); j < cell.getColumnSpan() + cell.getColumn(); ++j) {
                    getSpreadsheetViewSkin().getSelectedColumns().add(j);
                }
            }
        }

        private void removeSelectedRowsAndColumns(TablePosition<?, ?> t) {
            final SpreadsheetCell cell = cellsView.getItems().get(t.getRow()).get(t.getColumn());
            for (int i = cell.getRow(); i < cell.getRowSpan() + cell.getRow(); ++i) {
                getSpreadsheetViewSkin().getSelectedRows().remove(Integer.valueOf(i));
                for (int j = cell.getColumn(); j < cell.getColumnSpan() + cell.getColumn(); ++j) {
                    getSpreadsheetViewSkin().getSelectedColumns().remove(Integer.valueOf(j));
                }
            }
        }

        @Override
        public void clearAndSelect(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
            // RT-33558 if this method has been called with a given row/column
            // intersection, and that row/column intersection is the only
            // selection currently, then this method becomes a no-op.

            // This is understandable but not compatible with spanning
            // selection.
            /*
             * if (getSelectedCells().size() == 1 && isSelected(row, column)) {
             * return; }
             */

            makeAtomic = true;
            // firstly we make a copy of the selection, so that we can send out
            // the correct details in the selection change event
            List<TablePosition<ObservableList<SpreadsheetCell>, ?>> previousSelection = new ArrayList<>(
                    selectedCellsMap.getSelectedCells());

            // then clear the current selection
            clearSelection();

            // and select the new row
            select(row, column);

            makeAtomic = false;

            // fire off a single add/remove/replace notification (rather than
            // individual remove and add notifications) - see RT-33324
            if (old != null && old.getColumn() >= 0) {
                TableColumn<ObservableList<SpreadsheetCell>, ?> columnFinal = getTableView().getColumns().get(
                        old.getColumn());
                int changeIndex = selectedCellsSeq.indexOf(new TablePosition<>(getTableView(), old.getRow(),
                        columnFinal));
                GenericAddRemoveChange<TablePosition<ObservableList<SpreadsheetCell>, ?>> change = new NonIterableChange.GenericAddRemoveChange<>(
                        changeIndex, changeIndex + 1, previousSelection, selectedCellsSeq);
                handleSelectedCellsListChangeEvent(change);
            }
        }

        /**
         * FIXME I don't understand why TablePosition is not parameterized in
         * the API..
         */
        @Override
        public ObservableList<TablePosition> getSelectedCells() {
            return (ObservableList<TablePosition>) (Object) selectedCellsSeq;
        }

        @Override
        public void selectAboveCell() {
            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
            if (pos.getRow() == -1) {
                select(getItemCount() - 1);
            } else if (pos.getRow() > 0) {
                select(pos.getRow() - 1, pos.getTableColumn());
            }

        }

        @Override
        public void selectBelowCell() {
            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();

            if (pos.getRow() == -1) {
                select(0);
            } else if (pos.getRow() < getItemCount() - 1) {
                select(pos.getRow() + 1, pos.getTableColumn());
            }

        }

        @Override
        public void selectLeftCell() {
            if (!isCellSelectionEnabled()) {
                return;
            }

            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
            }

        }

        @Override
        public void selectRightCell() {
            if (!isCellSelectionEnabled()) {
                return;
            }

            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
            if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
            }

        }

        @Override
        public void clearSelection() {
            if (!makeAtomic) {
                setSelectedIndex(-1);
                setSelectedItem(getModelItem(-1));
                focus(-1);
            }
            quietClearSelection();
        }

        private void quietClearSelection() {
            selectedCellsMap.clear();
            getSpreadsheetViewSkin().getSelectedRows().clear();
            getSpreadsheetViewSkin().getSelectedColumns().clear();
        }

        @SuppressWarnings("unchecked")
        private TablePosition<ObservableList<SpreadsheetCell>, ?> getFocusedCell() {
            if (getTableView().getFocusModel() == null) {
                return new TablePosition<>(getTableView(), -1, null);
            }
            return (TablePosition<ObservableList<SpreadsheetCell>, ?>) cellsView.getFocusModel().getFocusedCell();
        }

        private TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumn(
                TableColumn<ObservableList<SpreadsheetCell>, ?> column, int offset) {
            final int columnIndex = getTableView().getVisibleLeafIndex(column);
            final int newColumnIndex = columnIndex + offset;
            return getTableView().getVisibleLeafColumn(newColumnIndex);
        }

        private GridViewSkin getSpreadsheetViewSkin() {
            return (GridViewSkin) getCellsViewSkin();
        }
    }

    /**
     * ********************************************************************* *
     * private listeners
     * ********************************************************************
     */

    private ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override
        public void onChanged(Change<? extends Integer> c) {
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

    private ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {
        @Override
        public void onChanged(Change<? extends SpreadsheetColumn> c) {
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
     * Return a letter (just like Excel) associated with the number. When the
     * number is under 26, a simple letter is returned. When the number is
     * superior, concatenated letters are returned.
     * 
     * 
     * For example: 0 -> A 1 -> B 26 -> AA 32 -> AG 45 -> AT
     * 
     * 
     * @param number
     * @return a letter (like) associated with the number.
     */
    static final String getExcelLetterFromNumber(int number) {
        String letter = "";
        // Repeatedly divide the number by 26 and convert the
        // remainder into the appropriate letter.
        while (number >= 0) {
            final int remainder = number % 26;
            letter = (char) (remainder + 'A') + letter;
            number = number / 26 - 1;
        }

        return letter;
    }
    
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
