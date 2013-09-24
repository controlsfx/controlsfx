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
package org.controlsfx.control;

import impl.org.controlsfx.skin.SpreadsheetCellImpl;
import impl.org.controlsfx.skin.SpreadsheetRowImpl;
import impl.org.controlsfx.skin.SpreadsheetViewSkin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

import org.controlsfx.control.spreadsheet.model.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.model.SpreadsheetCells;
import org.controlsfx.control.spreadsheet.model.Grid;
import org.controlsfx.control.spreadsheet.view.SpreadsheetColumn;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

/**
 * The SpreadsheetView is a control based on {@link TableView} with different functionalities. The aim
 * is to have a powerful grid where data can be written and retrieved.
 * 
 * <h3>Features </h3>
 * <li> Cells can span in row and in column.</li>
 * <li> Rows can be fixed to the top of the {@link SpreadsheetView} so that they are
 * always visible on screen.</li>
 * <li> Columns can be fixed to the left of the {@link SpreadsheetView} so that they are
 * always visible on screen. Only columns without any spanning cells can be fixed.</li>
 * <li> A row header can be switched on in order to display the row number.</li>
 * <li> Selection of several cells can be made with a click and drag.</li>
 * <li> A copy/paste context menu is accessible with a right-click.</li>
 * 
 * 
 * <h3>Creating a SpreadsheetView </h3>
 * Just like the {@link TableView}, you can instantiate the underlying model, a {@link Grid}.
 * You will create some {@link ObservableList<DataCell>} filled with {@link SpreadsheetCell}. 
 * 
 * {@code 
 * int rowCount = 15;
 * int columnCount = 10;
 * Grid grid = new Grid(rowCount, columnCount);
 * 
 * ArrayList<ObservableList<DataCell>> rows = new ArrayList<ObservableList<DataCell>>(grid.getRowCount());
 *		for (int row = 0; row < grid.getRowCount(); ++row) {
 *			final ObservableList<DataCell> ObservableList<DataCell> = new ObservableList<DataCell>(row, grid.getColumnCount());
 *			for (int column = 0; column < grid.getColumnCount(); ++column) {
 *				ObservableList<DataCell>.add(SpreadsheetCells.createTextCell(row, column, 1, 1,""));
 *			}
 *			rows.add(ObservableList<DataCell>);
 *		}
 * grid.setRows(rows);
 * }
 * 
 * At that moment you can span some of the cells with the convenient method provided by the grid.
 * Then you just need to instantiate the SpreadsheetView.
 *		
 * {@code
 * SpreadsheetView spreadSheetView = new SpreadsheetView(grid);
 * }
 */
public class SpreadsheetView extends Control {

    private final TableView<ObservableList<SpreadsheetCell<?>>> tableView;

    /***************************************************************************
     *                                                                         *
     * Static Fields                                                           *
     *                                                                         *
     **************************************************************************/
    /**
     * Define how this cell is spanning.
     */
    public static enum SpanType {
        NORMAL_CELL, 		// Normal Cell (visible)
        COLUMN_INVISIBLE, 	//Invisible cell spanned in column
        ROW_INVISIBLE, 		//Invisible cell spanned in row
        ROW_VISIBLE,		//Visible Cell but has invisible cell below
        BOTH_INVISIBLE;   	//Invisible cell, span in diagonal
    }


    
    /***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/

    private Grid grid;
    private DataFormat fmt;
    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();
    private final ObservableList<Integer> fixedColumns = FXCollections.observableArrayList();

    //Properties needed by the SpreadsheetView and managed by the skin (source is the VirtualFlow)
    private VirtualScrollBar hbar=null;
    private VirtualScrollBar vbar=null;
    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
    /**
     * Creates a default SpreadsheetView control with no content and a null Grid. 
     */
    public SpreadsheetView() {
        this(null);
    }

    /**
     * Creates a SpreadsheetView control with the {@link Grid} specified. 
     * @param grid The Grid that contains the items to be rendered
     */
    public SpreadsheetView(final Grid grid){
        super();

        getStyleClass().add("SpreadsheetView");

        // FIXME extract this out as a separate skin class
        setSkin(new Skin<SpreadsheetView>() {
            @Override public Node getNode() {
                return tableView;
            }

            @Override public SpreadsheetView getSkinnable() {
                return SpreadsheetView.this;
            }

            @Override public void dispose() {
                // no-op
            }
        });

        // FIXME the SpreadsheetViewSkin actually belongs to the TableView, so
        // we should probably make that private and instead expose the skin
        // for this class (that is, the code above)
        this.tableView = new TableView<ObservableList<SpreadsheetCell<?>>>() {
            /**
             * {@inheritDoc}
             */
            @Override protected String getUserAgentStylesheet() {
                return SpreadsheetView.class.getResource("spreadsheet.css").toExternalForm();
            }

            @Override protected Skin<?> createDefaultSkin() {
                return new SpreadsheetViewSkin(SpreadsheetView.this, tableView);
            }
        };
        getChildren().add(tableView);

        //Add a listener to the selection model in order to edit the spanned cells when clicked
        tableView.setSelectionModel(new SpreadsheetViewSelectionModel<>(this,tableView));
        getSelectionModel().setCellSelectionEnabled(true);
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /**
         * Set the focus model to track keyboard change and redirect focus on spanned
         * cells
         */
        // We add a listener on the focus model in order to catch when we are on a hidden cell
        tableView.getFocusModel().focusedCellProperty().addListener((ChangeListener<TablePosition>)(ChangeListener<?>) new FocusModelListener(this));

        // The contextMenu creation must be on the JFX thread
        final Runnable r = new Runnable() {
            @Override
            public void run() {
            	SpreadsheetView.this.setContextMenu(getSpreadsheetViewContextMenu());
            }
        };
        Platform.runLater(r);
        
        //Handle copy Paste action, quite naive right now..
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
			if(arg0.isShortcutDown() && arg0.getCode().compareTo(KeyCode.C) == 0)
				copyClipBoard();
			else if (arg0.isShortcutDown() && arg0.getCode().compareTo(KeyCode.V) == 0)
				pasteClipboard();
			}	
		});
        
        setGrid(grid);
    }

    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * Return a {@link TablePosition} of cell being currently edited.
     * @return
     */
    public TablePosition<ObservableList<SpreadsheetCell<?>>, ?> getEditingCell(){
        return tableView.getEditingCell();
    }
    
    /**
     * Return an observableList of the {@link SpreadsheetColumn} used.
     * @return
     */
    public ObservableList<SpreadsheetColumn> getColumns(){
		return columns;
    }

    /**
     * Return the model Grid used by the SpreadsheetView
     * @return
     */
    public final Grid getGrid(){
        return grid;
    }
    
    
    private final BooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader",true);
    
    /**
     * Activate and deactivate the Column Header
     * @param b
     */
    public final void setShowColumnHeader(final boolean b){
        //TODO Need to do that again
        //flow.recreateCells(); // Because otherwise we have at the bottom
        showColumnHeader.setValue(b);
    }
    
    public final boolean isShowColumnHeader() {
        return showColumnHeader.get();
    }

    /**
     * BooleanProperty associated with the column Header.
     * @return
     */
    public final BooleanProperty showColumnHeaderProperty() {
        return showColumnHeader;
    }

    
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader",true);
    
    /**
     * Activate and deactivate the Row Header
     * @param b
     */
    public final void setShowRowHeader(final boolean b){
        showRowHeader.setValue(b);
    }
    
    public final boolean isShowRowHeader() {
        return showRowHeader.get();
    }
    
    /**
     * BooleanProperty associated with the row Header.
     * @return
     */
    public final BooleanProperty showRowHeaderProperty() {
        return showRowHeader;
    }

    /**
     * Fix the first "numberOfFixedRows" at the top of the SpreadsheetView
     * @param numberOfFixedRows
     */
    public final void fixRows(int numberOfFixedRows){
        getFixedRowsList().clear();
        for (int j = 0; j < numberOfFixedRows; j++) {
            getFixedRowsList().add(j);
        }
    }

    /**
     * Return an ObservableList of the fixed rows. 
     * Just the number of the rows are returned.
     * @return
     */
    public ObservableList<Integer> getFixedRowsList() {
        return fixedRows;
    }
    
    /**
     * Return the number of fixed rows at the top of the SpreadsheetView
     * @return
     */
    public int getFixedRows() {
        return fixedRows.size();
    }

    /**
     * Fix the first "numberOfFixedColumns" on the left.
     * It will unfix any previously fixed column.
     * It's possible to fix columns also by right-clicking on columns header.
     * @param numberOfFixedColumns
     */
    public void fixColumns(List<Integer> fixedColumns){

        getFixedColumns().clear();
        for (SpreadsheetColumn spc : getColumns()) {
			spc.setFixed(false);
		}

        for (Integer column: fixedColumns) {
        	if(column >=0 && column < getColumns().size()){
        		getColumns().get(column).setFixed(true);
        	}
        }
    }

    /**
     * Return an ObservableList of the fixed columns. 
     * Just the number of the columns are returned.
     * @return
     */
    public ObservableList<Integer> getFixedColumns() {
        return fixedColumns;
    }

    /**
     * Return the selectionModel used by the SpreadsheetView.
     * @return {@link SpreadsheetViewSelectionModel}
     */
    public SpreadsheetViewSelectionModel<ObservableList<SpreadsheetCell<?>>> getSelectionModel() {
        return (SpreadsheetViewSelectionModel<ObservableList<SpreadsheetCell<?>>>) tableView.getSelectionModel();
    }

    /***************************************************************************
     *                                                                         *
     * Private/Protected Implementation                                        *
     *                                                                         *
     **************************************************************************/
    
    /**
     * Return the {@link SpanType} of a cell.
     * @param row
     * @param column
     * @return
     */
    private SpanType getSpanType(final int row, final int column) {
        Grid grid = getGrid();
        if (grid == null) {
            return SpanType.NORMAL_CELL;
        }
        return grid.getSpanType(this, row, column);
    }

    /**
     * Return a list of {@code ObservableList<DataCell>} used by the SpreadsheetView.
     * @return
     */
    private ObservableList<ObservableList<SpreadsheetCell<?>>> getItems() {
        return tableView.getItems();
    }
    
    /**
     * Return the {@link SpreadsheetRowImpl} at the specified index
     * @param index
     * @return
     */
    private SpreadsheetRowImpl getNonFixedRow(int index){
        SpreadsheetViewSkin skin = (SpreadsheetViewSkin) tableView.getSkin();
        return skin.getCell(fixedRows.size()+index);
    }

    /**
     * Indicate whether or not the row at the specified index is currently 
     * being displayed.
     * @param index
     * @return
     */
    private final boolean containsRow(int index){
        SpreadsheetViewSkin skin = (SpreadsheetViewSkin) tableView.getSkin();
        int size = skin.getCellsSize();
        for (int i = 0 ; i < size; ++i) {
            if(skin.getCell(i).getIndex() == index)
                return true;
        }
        return false;
    }

    /**
     * Set a grid for the SpreadsheetView.
     * @param grid
     */
    private final void setGrid(Grid grid) {
        this.grid = grid;

        // TODO move into a property
        if(grid.getRows() != null){
            final ObservableList<ObservableList<SpreadsheetCell<?>>> observableRows = FXCollections.observableArrayList(grid.getRows());
            tableView.getItems().clear();
            tableView.setItems(observableRows);

            final int columnCount = grid.getColumnCount();
            getColumns().clear();
            for (int i = 0; i < columnCount; ++i) {
                final int col = i;

                final TableColumn<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>> column = new TableColumn<>(getEquivColumn(col));

                column.setEditable(true);
                // We don't want to sort the column
                column.setSortable(false);
                
                column.impl_setReorderable(false);
                
                // We assign a DataCell for each Cell needed (MODEL).
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>>, ObservableValue<SpreadsheetCell<?>>>() {
                    @Override
                    public ObservableValue<SpreadsheetCell<?>> call(TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>> p) {
                        return new ReadOnlyObjectWrapper<SpreadsheetCell<?>>(p.getValue().get(col));
                    }
                });
                // We create a SpreadsheetCell for each DataCell in order to specify how to represent the DataCell(VIEW)
                column.setCellFactory(new Callback<TableColumn<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>>, TableCell<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>>>() {
                    @Override
                    public TableCell<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>> call(TableColumn<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<?>> p) {
                        return new SpreadsheetCellImpl();
                    }
                });
                tableView.getColumns().add(column);
                final SpreadsheetColumn spreadsheetColumns = new SpreadsheetColumn(column,this, i);
                columns.add(spreadsheetColumns);
            }
        }
    }

    /**
     * Give the column letter in excel mode with the given number
     * @param number
     * @return
     */
    private final String getEquivColumn(int number){
        String converted = "";
        // Repeatedly divide the number by 26 and convert the
        // remainder into the appropriate letter.
        while (number >= 0)
        {
            final int remainder = number % 26;
            converted = (char)(remainder + 'A') + converted;
            number = number / 26 - 1;
        }

        return converted;
    }

    /***************************************************************************
     * 						COPY / PASTE METHODS
     **************************************************************************/

    private void checkFormat(){
        if((fmt = DataFormat.lookupMimeType("shuttle"))== null){
            fmt = new DataFormat("shuttle");
        }
    }
    /**
     * Create a menu on rightClick with two options: Copy/Paste
     * @return
     */
    private ContextMenu getSpreadsheetViewContextMenu(){
        final ContextMenu contextMenu = new ContextMenu();
        
        // We don't want to open a contextMenu when editing because editors
        // have their own contextMenu
        contextMenu.setOnShowing(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent arg0) {
				if(getEditingCell() != null){
					// We're being reactive but we want to be pro-active so we may need a work-around.
					final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                        	contextMenu.hide();
                        }
                    };
                    Platform.runLater(r);
				}
			}
		});
        final MenuItem item1 = new MenuItem("Copy");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipBoard();
            }
        });
        final MenuItem item2 = new MenuItem("Paste");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        contextMenu.getItems().addAll(item1, item2);
        return contextMenu;
    }

    /**
     * Put the current selection into the ClipBoard
     */
    private void copyClipBoard(){
        checkFormat();

        //		final ArrayList<ArrayList<DataCell>> temp = new ArrayList<>();
        final ArrayList<SpreadsheetCell<?>> list = new ArrayList<SpreadsheetCell<?>>();
        @SuppressWarnings("rawtypes")
        final ObservableList<TablePosition> posList = getSelectionModel().getSelectedCells();

        for (final TablePosition<?,?> p : posList) {
            list.add(getGrid().getRows().get(p.getRow()).get(p.getColumn()));
        }

        final ClipboardContent content = new ClipboardContent();
        content.put(fmt,list);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Try to paste the clipBoard to the specified position
     * Try to paste the current selection into the Grid. If the two contents are
     * not matchable, then it's not pasted.
     */
    private void pasteClipboard(){
        checkFormat();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if(clipboard.getContent(fmt) != null){

            @SuppressWarnings("unchecked")
            final ArrayList<SpreadsheetCell<?>> list = (ArrayList<SpreadsheetCell<?>>) clipboard.getContent(fmt);
            //TODO algorithm very bad
            int minRow=grid.getRowCount();
            int minCol=grid.getColumnCount();
            int maxRow=0;
            int maxCol=0;
            for (final SpreadsheetCell<?> p : list) {
                final int tempcol = p.getColumn();
                final int temprow = p.getRow();
                if(tempcol<minCol) {
                    minCol = tempcol;
                }
                if(tempcol>maxCol) {
                    maxCol = tempcol;
                }
                if(temprow<minRow) {
                    minRow = temprow;
                }
                if(temprow>maxRow) {
                    maxRow =temprow;
                }
            }

            final TablePosition<?,?> p = tableView.getFocusModel().getFocusedCell();

            final int offsetRow = p.getRow()-minRow;
            final int offsetCol = p.getColumn()-minCol;
            int row;
            int column;


            for (final SpreadsheetCell<?> row1 : list) {
                row = row1.getRow();
                column = row1.getColumn();
                if(row+offsetRow < getGrid().getRowCount() && column+offsetCol < getGrid().getColumnCount()
                        && row+offsetRow >= 0 && column+offsetCol >=0 ){
                    final SpanType type = getSpanType(row+offsetRow, column+offsetCol);
                    if(type == SpanType.NORMAL_CELL || type== SpanType.ROW_VISIBLE) {
                        getGrid().getRows().get(row+offsetRow).get(column+offsetCol).match(row1);
                    }
                }
            }
            //For layout
            getSelectionModel().clearSelection();
            requestLayout();
        //To be improved
        }else if(clipboard.hasString()){
        	final TablePosition<?,?> p = tableView.getFocusModel().getFocusedCell();
        	
        	getGrid().getRows().get(p.getRow()).get(p.getColumn()).match(SpreadsheetCells.createTextCell(0, 0, 1, 1, clipboard.getString()));
        	
        	//For layout
        	getSelectionModel().clearSelection();
        	requestLayout();
        }
    }


    /**************************************************************************
     * 
     * 						FOCUS MODEL
     * 
     * *************************************************************************/

    class FocusModelListener implements ChangeListener<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> {

        private final TableView.TableViewFocusModel<ObservableList<SpreadsheetCell<?>>> tfm;

        public FocusModelListener(SpreadsheetView spreadsheetView) {
            tfm = tableView.getFocusModel();
        }

        @Override
        public void changed(ObservableValue<? extends TablePosition<ObservableList<SpreadsheetCell<?>>,?>> ov, final TablePosition<ObservableList<SpreadsheetCell<?>>,?> t, final TablePosition<ObservableList<SpreadsheetCell<?>>,?> t1) {
            final SpreadsheetView.SpanType spanType = getSpanType(t1.getRow(), t1.getColumn());
            switch (spanType) {
                case ROW_INVISIBLE:
                    // If we notice that the new focused cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go below.
                    if (!isPressed()
                            && t.getColumn() == t1.getColumn()
                            && t.getRow() == t1.getRow() - 1) {
                        final Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(getTableRowSpan(t), t.getTableColumn());
                            }
                        };
                        Platform.runLater(r);

                    } else {
                        // If the current focused cell if hidden by row span, we go above
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
                    // If the current focused cell if hidden by a both (row and column) span, we go left-above
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(t1.getRow() - 1, tableView.getColumns().get(t1.getColumn() - 1));
                        }
                    };
                    Platform.runLater(r);
                    break;
                case COLUMN_INVISIBLE:
                    // If we notice that the new focused cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go right.
                    if (!isPressed()
                            && t.getColumn() == t1.getColumn() - 1
                            && t.getRow() == t1.getRow()) {

                        final Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(t.getRow(), getTableColumnSpan(t));
                            }
                        };
                        Platform.runLater(r2);
                    } else {
                        // If the current focused cell if hidden by column span, we go left

                        final Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(t1.getRow(), tableView.getColumns().get(t1.getColumn() - 1));
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
     * 						SELECTION MODEL
     * 
     * *************************************************************************/

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param t the current TablePosition
     * @return
     */
    private TableColumn<ObservableList<SpreadsheetCell<?>>, ?> getTableColumnSpan(final TablePosition<?,?> t) {
        return tableView.getVisibleLeafColumn(t.getColumn() + tableView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan());
    }

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param t the current TablePosition
     * @return
     */
    private int getTableColumnSpanInt(final TablePosition<?,?> t) {
        return t.getColumn() + tableView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan();
    }

    /**
     * Return the Row number right after the current TablePosition (including
     * the RowSpan to be on a visible Cell)
     *
     * @param t
     * @param spreadsheetView
     * @return
     */
    private int getTableRowSpan(final TablePosition<?,?> t) {
        return tableView.getItems().get(t.getRow()).get(t.getColumn()).getRowSpan()
                + tableView.getItems().get(t.getRow()).get(t.getColumn()).getRow();
    }

    /**
     * For a position, return the Visible Cell associated with
     * It can be the top of the span cell if it's visible,
     * or it can be the first row visible if we have scrolled
     * @param row
     * @param column
     * @param col
     * @return
     */
    private TablePosition<ObservableList<SpreadsheetCell<?>>,?> getVisibleCell(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column, int col) {
        final SpreadsheetView.SpanType spanType = getSpanType(row, col);
        switch (spanType) {
            case NORMAL_CELL:
            case ROW_VISIBLE:
                return new TablePosition<>(tableView, row, column);
            case BOTH_INVISIBLE:
            case COLUMN_INVISIBLE:
            case ROW_INVISIBLE:
            default:
                final SpreadsheetCell<?> cellSpan = tableView.getItems().get(row).get(col);
                if (SpreadsheetViewSkin.getSkin(this).getCellsSize() != 0 && getNonFixedRow(0).getIndex() <= cellSpan.getRow()) {
                    return new TablePosition<>(tableView, cellSpan.getRow(), tableView.getColumns().get(cellSpan.getColumn()));

                } else { // If it's not, then it's the firstkey
                    return new TablePosition<>(tableView, getNonFixedRow(0).getIndex(),tableView.getColumns().get(cellSpan.getColumn()));
                }
        }
    }




    public static class SpreadsheetViewSelectionModel<S> extends TableView.TableViewSelectionModel<ObservableList<SpreadsheetCell<?>>> {

        private boolean ctrl = false;   // Register state of 'ctrl' key
        private boolean shift = false;  // Register state of 'shift' key
        private boolean key = false;    // Register if we last touch the keyboard or the mouse
        private boolean drag = false;	//register if we are dragging (no edition)
        private int itemCount = 0;
        MouseEvent mouseEvent;
        private final TableView<ObservableList<SpreadsheetCell<?>>> tableView;
        private final SpreadsheetView spreadsheetView;

        /**
         * Make the tableView move when selection operating outside bounds
         */
        private final Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                SpreadsheetViewSkin skin = SpreadsheetViewSkin.getSkin(spreadsheetView);
                
                if (mouseEvent != null && !tableView.contains(mouseEvent.getX(), mouseEvent.getY())) {
                    if(mouseEvent.getSceneX() < tableView.getLayoutX()) {
                        skin.getHBar().decrement();
                    }else if(mouseEvent.getSceneX() > tableView.getLayoutX()+tableView.getWidth()){
                        skin.getHBar().increment();
                    }
                    else if(mouseEvent.getSceneY() < tableView.getLayoutY()) {
                        skin.getVBar().decrement();
                    }else if(mouseEvent.getSceneY() > tableView.getLayoutY()+tableView.getHeight()) {
                        skin.getVBar().increment();
                    }
                }
            }
        }));

        /**
         * When the drag is over, we remove the listener and stop the timer
         */
        private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent arg0) {
                drag = false;
                timer.stop();
                spreadsheetView.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
            }
        };

        /**
         * *********************************************************************
         *                                                                     *
         * Constructors * *
         * ********************************************************************
         */
        public SpreadsheetViewSelectionModel(SpreadsheetView spreadsheetView, final TableView<ObservableList<SpreadsheetCell<?>>> tableView) {
            super(tableView);
            this.tableView = tableView;
            this.spreadsheetView = spreadsheetView;

            tableView.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    key = true;
                    ctrl = t.isControlDown();
                    shift = t.isShiftDown();
                }
            });

            tableView.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    key = false;
                    ctrl = t.isControlDown();
                    shift = t.isShiftDown();
                }
            });
            tableView.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    tableView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
                    drag = true;
                    timer.setCycleCount(Timeline.INDEFINITE);
                    timer.play();
                }
            });

            tableView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    mouseEvent = e;
                }
            });

            updateItemCount();

            selectedCells = FXCollections.<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>observableArrayList();

            /*
             * The following two listeners are used in conjunction with
             * SelectionModel.select(T obj) to allow for a developer to select
             * an item that is not actually in the data model. When this occurs,
             * we actively try to find an index that matches this object, going
             * so far as to actually watch for all changes to the items list,
             * rechecking each time.
             */

            // watching for changes to the items list
            tableView.itemsProperty().addListener(weakItemsPropertyListener);

            // watching for changes to the items list content
            if (tableView.getItems() != null) {
                tableView.getItems().addListener(weakItemsContentListener);
            }
        }

        private final ChangeListener<ObservableList<ObservableList<SpreadsheetCell<?>>>> itemsPropertyListener = new ChangeListener<ObservableList<ObservableList<SpreadsheetCell<?>>>>() {
            @Override
            public void changed(ObservableValue<? extends ObservableList<ObservableList<SpreadsheetCell<?>>>> observable,
                    ObservableList<ObservableList<SpreadsheetCell<?>>> oldList, ObservableList<ObservableList<SpreadsheetCell<?>>> newList) {
                updateItemsObserver(oldList, newList);
            }
        };
        private final WeakChangeListener<ObservableList<ObservableList<SpreadsheetCell<?>>>> weakItemsPropertyListener =
                new WeakChangeListener<ObservableList<ObservableList<SpreadsheetCell<?>>>>(itemsPropertyListener);

        final ListChangeListener<ObservableList<SpreadsheetCell<?>>> itemsContentListener = new ListChangeListener<ObservableList<SpreadsheetCell<?>>>() {
            @Override public void onChanged(Change<? extends ObservableList<SpreadsheetCell<?>>> c) {
                updateItemCount();

                while (c.next()) {
                    final ObservableList<SpreadsheetCell<?>> selectedItem = getSelectedItem();
                    final int selectedIndex = getSelectedIndex();

                    if (spreadsheetView.getItems() == null || spreadsheetView.getItems().isEmpty()) {
                        clearSelection();
                    } else if (getSelectedIndex() == -1 && getSelectedItem() != null) {
                        final int newIndex = spreadsheetView.getItems().indexOf(getSelectedItem());
                        if (newIndex != -1) {
                            setSelectedIndex(newIndex);
                        }
                    } else if (c.wasRemoved() &&
                            c.getRemovedSize() == 1 &&
                            ! c.wasAdded() &&
                            selectedItem != null &&
                            selectedItem.equals(c.getRemoved().get(0))) {
                        // Bug fix for RT-28637
                        if (getSelectedIndex() < getItemCount()) {
                            final ObservableList<SpreadsheetCell<?>> newSelectedItem = getModelItem(selectedIndex);
                            if (! selectedItem.equals(newSelectedItem)) {
                                setSelectedItem(newSelectedItem);
                            }
                        }
                    }
                }

                updateSelection(c);
            }
        };

        final WeakListChangeListener<ObservableList<SpreadsheetCell<?>>> weakItemsContentListener = new WeakListChangeListener<ObservableList<SpreadsheetCell<?>>>(itemsContentListener);

        private void updateItemsObserver(ObservableList<ObservableList<SpreadsheetCell<?>>> oldList, ObservableList<ObservableList<SpreadsheetCell<?>>> newList) {
            // the listview items list has changed, we need to observe
            // the new list, and remove any observer we had from the old list
            if (oldList != null) {
                oldList.removeListener(weakItemsContentListener);
            }
            if (newList != null) {
                newList.addListener(weakItemsContentListener);
            }

            updateItemCount();

            // when the items list totally changes, we should clear out
            // the selection
            setSelectedIndex(-1);
        }
        /**
         * *********************************************************************
         *                                                                     *
         * Observable properties (and getters/setters) * *
         * ********************************************************************
         */
        // the only 'proper' internal observableArrayList, selectedItems and selectedIndices
        // are both 'read-only and unbacked'.
        private final ObservableList<TablePosition<ObservableList<SpreadsheetCell<?>>, ?>> selectedCells;

        // NOTE: represents selected ROWS only - use selectedCells for more data
        //        private final ReadOnlyUnbackedObservableList<Integer> selectedIndices;
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return null;
        }

        // used to represent the _row_ backing data for the selectedCells
        //        private final ReadOnlyUnbackedObservableList<S> selectedItems;
        @Override
        public ObservableList<ObservableList<SpreadsheetCell<?>>> getSelectedItems() {
            return null;//selectedItems;
        }

        //        private final ReadOnlyUnbackedObservableList<TablePosition> selectedCellsSeq;
        @Override
        public ObservableList<TablePosition> getSelectedCells() {
            return (ObservableList<TablePosition>)(Object)selectedCells;
        }
        /**
         * *********************************************************************
         *                                                                     *
         * Internal properties * *
         * ********************************************************************
         */
        private int previousModelSize = 0;

        // Listen to changes in the tableview items list, such that when it
        // changes we can update the selected indices list to refer to the
        // new indices.
        private void updateSelection(ListChangeListener.Change<? extends ObservableList<SpreadsheetCell<?>>> c) {
            c.reset();
            while (c.next()) {
                if (c.wasReplaced()) {
                    if (c.getList().isEmpty()) {
                        // the entire items list was emptied - clear selection
                        clearSelection();
                    } else {
                        final int index = getSelectedIndex();

                        if (previousModelSize == c.getRemovedSize()) {
                            // all items were removed from the model
                            clearSelection();
                        } else if (index < getItemCount() && index >= 0) {
                            // Fix for RT-18969: the list had setAll called on it
                            // Use of makeAtomic is a fix for RT-20945
                            //makeAtomic = true;
                            clearSelection(index);
                            //makeAtomic = false;
                            select(index);
                        } else {
                            // Fix for RT-22079
                            clearSelection();
                        }
                    }
                } else if (c.wasAdded() || c.wasRemoved()) {
                    final int position = c.getFrom();
                    final int shift = c.wasAdded() ? c.getAddedSize() : -c.getRemovedSize();

                    if (position < 0) {
                        return;
                    }
                    if (shift == 0) {
                        return;
                    }

                    final List<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> newIndices = new ArrayList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>(selectedCells.size());

                    for (int i = 0; i < selectedCells.size(); i++) {
                        final TablePosition<ObservableList<SpreadsheetCell<?>>,?> old = selectedCells.get(i);
                        final int oldRow = old.getRow();
                        final int newRow = oldRow < position ? oldRow : oldRow + shift;

                        // Special case for RT-28637 (See unit test in TableViewTest).
                        // Essentially the selectedItem was correct, but selectedItems
                        // was empty.
                        if (oldRow == 0 && shift == -1) {
                            newIndices.add(new TablePosition<>(getTableView(), 0, old.getTableColumn()));
                            continue;
                        }

                        if (newRow < 0) {
                            continue;
                        }
                        newIndices.add(new TablePosition<>(getTableView(), newRow, old.getTableColumn()));
                    }

                    quietClearSelection();

                    // Fix for RT-22079
                    for (int i = 0; i < newIndices.size(); i++) {
                        final TablePosition<ObservableList<SpreadsheetCell<?>>,?> tp = newIndices.get(i);
                        select(tp.getRow(), tp.getTableColumn());
                    }
                } else if (c.wasPermutated()) {
                    // General approach:
                    //   -- detected a sort has happened
                    //   -- Create a permutation lookup map (1)
                    //   -- dump all the selected indices into a list (2)
                    //   -- clear the selected items / indexes (3)
                    //   -- create a list containing the new indices (4)
                    //   -- for each previously-selected index (5)
                    //     -- if index is in the permutation lookup map
                    //       -- add the new index to the new indices list
                    //   -- Perform batch selection (6)

                    // (1)
                    final int length = c.getTo() - c.getFrom();
                    final HashMap<Integer, Integer> pMap = new HashMap<Integer, Integer> (length);
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        pMap.put(i, c.getPermutation(i));
                    }

                    // (2)
                    final List<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> selectedIndices = new ArrayList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>((ObservableList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>)(Object)getSelectedCells());


                    // (3)
                    clearSelection();

                    // (4)
                    final List<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> newIndices = new ArrayList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>(getSelectedIndices().size());

                    // (5)
                    for (int i = 0; i < selectedIndices.size(); i++) {
                        final TablePosition<ObservableList<SpreadsheetCell<?>>,?> oldIndex = selectedIndices.get(i);

                        if (pMap.containsKey(oldIndex.getRow())) {
                            final Integer newIndex = pMap.get(oldIndex.getRow());
                            newIndices.add(new TablePosition<>(oldIndex.getTableView(), newIndex, oldIndex.getTableColumn()));
                        }
                    }

                    // (6)
                    quietClearSelection();
                    selectedCells.setAll(newIndices);
                    //selectedCellsSeq.callObservers(new NonIterableChange.SimpleAddChange<TablePosition<ObservableList<DataCell>,?>>(0, newIndices.size(), selectedCellsSeq));
                }
            }

            previousModelSize = getItemCount();
        }

        /**
         * *********************************************************************
         *                                                                     *
         * Public selection API * *
         * ********************************************************************
         */
        @Override
        public void clearAndSelect(int row) {
            clearAndSelect(row, null);
        }

        @Override
        public void clearAndSelect(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column) {
            quietClearSelection();
            select(row, column);
        }

        @Override
        public void select(int row) {
            select(row, null);
        }
        private TablePosition<ObservableList<SpreadsheetCell<?>>, ?> old = null;

        @Override
        public void select(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column) {

            if (row < 0 || row >= getItemCount()) {
                return;
            }

            // if I'm in cell selection mode but the column is null, I don't want
            // to select the whole row instead...
            if (isCellSelectionEnabled() && column == null) {
                return;
            }
            //Variable we need for algorithm
            TablePosition<ObservableList<SpreadsheetCell<?>>, ?> posFinal = new TablePosition<>(getTableView(), row, column);

            final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(row, posFinal.getColumn());

            /**
             * We check if we are on covered cell. If so we have the
             * algorithm of the focus model to give the selection to the right cell.
             *
             */
            switch (spanType) {
                case ROW_INVISIBLE:
                    // If we notice that the new selected cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go below.
                    // We make sure that old is not null, and that the move is initiated by keyboard.
                    //Because if it's a click, then we just want to go on the clicked cell (not below)
                    if (old != null && key && !shift
                    && old.getColumn() == posFinal.getColumn()
                    && old.getRow() == posFinal.getRow() - 1) {
                        posFinal = spreadsheetView.getVisibleCell(spreadsheetView.getTableRowSpan(old), old.getTableColumn(), old.getColumn());
                    } else {
                        // If the current selected cell if hidden by row span, we go above
                        posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
                    }
                    break;
                case BOTH_INVISIBLE:
                    // If the current selected cell if hidden by a both (row and column) span, we go left-above
                    posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
                    break;
                case COLUMN_INVISIBLE:
                    // If we notice that the new selected cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go right.
                    if (old != null && key && !shift
                    && old.getColumn() == posFinal.getColumn() - 1
                    && old.getRow() == posFinal.getRow()) {
                        posFinal = spreadsheetView.getVisibleCell(old.getRow(), spreadsheetView.getTableColumnSpan(old), spreadsheetView.getTableColumnSpanInt(old));
                    } else {
                        // If the current selected cell if hidden by column span, we go left
                        posFinal = spreadsheetView.getVisibleCell(row, column, posFinal.getColumn());
                    }
                default:
                    break;
            }

            //This is to handle edition
            if (posFinal.equals(old) && !ctrl && !shift && !drag) {
                // If we are on an Invisible row or both (in diagonal), we need to force the edition
                if (spanType == SpreadsheetView.SpanType.ROW_INVISIBLE || spanType == SpreadsheetView.SpanType.BOTH_INVISIBLE) {
                    final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> FinalPos = new TablePosition<>(tableView, posFinal.getRow(), posFinal.getTableColumn());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tableView.edit(FinalPos.getRow(), FinalPos.getTableColumn());
                        }
                    };
                    Platform.runLater(r);
                }
            }
            old = posFinal;

            if (!selectedCells.contains(posFinal)) {
                selectedCells.add(posFinal);
            }

            updateScroll(posFinal);
            addSelectedRowsAndColumns(posFinal);

            updateSelectedIndex(posFinal.getRow());
            focus(posFinal.getRow(), posFinal.getTableColumn());
        }

        private void updateScroll(TablePosition<ObservableList<SpreadsheetCell<?>>, ?> posFinal) {

            //spreadsheetView.scrollTo(posFinal.getRow());
            //		spreadsheetView.scrollToColumnIndex(posFinal.getColumn());

            //We try to make visible the rows that may be hidden by Fixed rows
            // We don't want to do any scroll behavior when dragging
            if(!drag && SpreadsheetViewSkin.getSkin(spreadsheetView).getCellsSize() != 0 && spreadsheetView.getNonFixedRow(0).getIndex()> posFinal.getRow() && !spreadsheetView.getFixedRowsList().contains(posFinal.getRow())) {
                tableView.scrollTo(posFinal.getRow());
            }

        }

        @Override
        public void select(ObservableList<SpreadsheetCell<?>> obj) {
            if (obj == null && getSelectionMode() == SelectionMode.SINGLE) {
                clearSelection();
                return;
            }

            // We have no option but to iterate through the model and select the
            // first occurrence of the given object. Once we find the first one, we
            // don't proceed to select any others.
            ObservableList<SpreadsheetCell<?>> rowObj = null;
            for (int i = 0; i < getItemCount(); i++) {
                rowObj = getModelItem(i);
                if (rowObj == null) {
                    continue;
                }

                if (rowObj.equals(obj)) {
                    if (isSelected(i)) {
                        return;
                    }

                    if (getSelectionMode() == SelectionMode.SINGLE) {
                        quietClearSelection();
                    }

                    select(i);
                    return;
                }
            }

            // if we are here, we did not find the item in the entire data model.
            // Even still, we allow for this item to be set to the give object.
            // We expect that in concrete subclasses of this class we observe the
            // data model such that we check to see if the given item exists in it,
            // whilst SelectedIndex == -1 && SelectedItem != null.
            setSelectedItem(obj);
        }

        @Override
        public void selectIndices(int row, int... rows) {
            if (rows == null) {
                select(row);
                return;
            }

            /*
             * Performance optimization - if multiple selection is disabled, only
             * process the end-most row index.
             */
            final int rowCount = getItemCount();

            if (getSelectionMode() == SelectionMode.SINGLE) {
                quietClearSelection();

                for (int i = rows.length - 1; i >= 0; i--) {
                    final int index = rows[i];
                    if (index >= 0 && index < rowCount) {
                        select(index);
                        break;
                    }
                }

                if (selectedCells.isEmpty()) {
                    if (row > 0 && row < rowCount) {
                        select(row);
                    }
                }
            } else {
                int lastIndex = -1;
                final Set<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> positions = new LinkedHashSet<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>();

                if (row >= 0 && row < rowCount) {
                    final TablePosition<ObservableList<SpreadsheetCell<?>>,Object> tp = new TablePosition<ObservableList<SpreadsheetCell<?>>,Object>(getTableView(), row, null);

                    if (! selectedCells.contains(tp)) {
                        positions.add(tp);
                        lastIndex = row;
                    }
                }

                for (int i = 0; i < rows.length; i++) {
                    final int index = rows[i];
                    if (index < 0 || index >= rowCount) {
                        continue;
                    }
                    lastIndex = index;
                    final TablePosition<ObservableList<SpreadsheetCell<?>>,Object> pos = new TablePosition<ObservableList<SpreadsheetCell<?>>,Object>(getTableView(), index, null);
                    if (! selectedCells.contains(pos)) {
                        positions.add(pos);
                    }
                }

                selectedCells.addAll(positions);

                if (lastIndex != -1) {
                    select(lastIndex);
                }
            }
        }

        @Override
        public void selectAll() {
            if (getSelectionMode() == SelectionMode.SINGLE) {
                return;
            }

            quietClearSelection();

            if (isCellSelectionEnabled()) {
                final List<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> indices = new ArrayList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>();
                TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column;
                TablePosition<ObservableList<SpreadsheetCell<?>>,?> tp = null;
                for (int col = 0; col < getTableView().getVisibleLeafColumns().size(); col++) {
                    column = getTableView().getVisibleLeafColumns().get(col);
                    for (int row = 0; row < getItemCount(); row++) {
                        tp = new TablePosition<>(getTableView(), row, column);
                        indices.add(tp);
                    }
                }
                selectedCells.setAll(indices);

                if (tp != null) {
                    select(tp.getRow(), tp.getTableColumn());
                    focus(tp.getRow(), tp.getTableColumn());
                }
            } else {
                final List<TablePosition<ObservableList<SpreadsheetCell<?>>,?>> indices = new ArrayList<TablePosition<ObservableList<SpreadsheetCell<?>>,?>>();
                for (int i = 0; i < getItemCount(); i++) {
                    indices.add(new TablePosition<>(getTableView(), i, null));
                }
                selectedCells.setAll(indices);

                final int focusedIndex = getFocusedIndex();
                if (focusedIndex == -1) {
                    select(getItemCount() - 1);
                    focus(indices.get(indices.size() - 1));
                } else {
                    select(focusedIndex);
                    focus(focusedIndex);
                }
            }
        }

        @Override
        public void clearSelection(int index) {
            clearSelection(index, null);
        }

        @Override
        public void clearSelection(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column) {

            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> tp = new TablePosition<>(getTableView(), row, column);
            if (isSelectedRange(row, column, tp.getColumn()) != null) {
                final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> tp1 = isSelectedRange(row, column, tp.getColumn());
                selectedCells.remove(tp1);
                removeSelectedRowsAndColumns(tp1);
                focus(tp1.getRow());
            } else {

                final boolean csMode = isCellSelectionEnabled();

                for (final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos : getSelectedCells()) {
                    if (!csMode && pos.getRow() == row || csMode && pos.equals(tp)) {
                        selectedCells.remove(pos);
                        removeSelectedRowsAndColumns(pos);

                        // give focus to this cell index
                        focus(row);

                        return;
                    }
                }
            }
        }

        @Override
        public void clearSelection() {
            updateSelectedIndex(-1);
            focus(-1);
            quietClearSelection();
        }

        private void quietClearSelection() {
            selectedCells.clear();
            selectedRows.clear();
            selectedColumns.clear();
        }

        @Override
        public boolean isSelected(int index) {
            return isSelected(index, null);
        }

        @Override
        public boolean isSelected(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column) {
            // When in cell selection mode, we currently do NOT support selecting
            // entire rows, so a isSelected(row, null)
            // should always return false.

            if (isCellSelectionEnabled() && column == null || row <0) {
                return false;
            }
            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> tp1 = new TablePosition<>(getTableView(), row, column);
            if (isSelectedRange(row, column, tp1.getColumn()) != null) {
                return true;
            } else {
                return false;
            }

            //    System.oufor (TablePosition tp : getSelectedCells()) {
            //            boolean columnMatch = !isCellSelectionEnabled()
            //                    || (column == null && tp.getTableColumn() == null)
            //                    || (column != null && column.equals(tp.getTableColumn()));
            //
            //            if (tp.getRow() == row && columnMatch) {
            //                return true;
            //            }
            //        }
            //        return false;t.println("Is selected"+row+"/"+tp1.getColumn());
            //
        }

        /**
         * Return the tablePosition of a selected cell inside a spanned cell if any.
         *
         * @param row
         * @param column
         * @param col
         * @return
         */
        public TablePosition<ObservableList<SpreadsheetCell<?>>, ?> isSelectedRange(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column, int col) {

            if (isCellSelectionEnabled() && column == null && row >=0) {
                return null;
            }

            final SpreadsheetCell<?> cellSpan = tableView.getItems().get(row).get(col);
            final int infRow = cellSpan.getRow();
            final int supRow = infRow + cellSpan.getRowSpan();

            final int infCol = cellSpan.getColumn();
            final int supCol = infCol + cellSpan.getColumnSpan();

            for (final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> tp : getSelectedCells()) {
                //boolean columnMatch = (column != null && column.equals(tp.getTableColumn()));

                if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol && tp.getColumn() < supCol) {
                    return tp;
                }
            }
            return null;
        }

        @Override
        public boolean isEmpty() {
            return selectedCells.isEmpty();
        }

        @Override
        public void selectPrevious() {
            if (isCellSelectionEnabled()) {
                // in cell selection mode, we have to wrap around, going from
                // right-to-left, and then wrapping to the end of the previous line
                final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();
                if (pos.getColumn() - 1 >= 0) {
                    // go to previous row
                    select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
                } else if (pos.getRow() < getItemCount() - 1) {
                    // wrap to end of previous row
                    select(pos.getRow() - 1, getTableColumn(getTableView().getVisibleLeafColumns().size() - 1));
                }
            } else {
                final int focusIndex = getFocusedIndex();
                if (focusIndex == -1) {
                    select(getItemCount() - 1);
                } else if (focusIndex > 0) {
                    select(focusIndex - 1);
                }
            }
        }

        @Override
        public void selectNext() {
            if (isCellSelectionEnabled()) {
                // in cell selection mode, we have to wrap around, going from
                // left-to-right, and then wrapping to the start of the next line
                final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();
                if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
                    // go to next column
                    select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
                } else if (pos.getRow() < getItemCount() - 1) {
                    // wrap to start of next row
                    select(pos.getRow() + 1, getTableColumn(0));
                }
            } else {
                final int focusIndex = getFocusedIndex();
                if (focusIndex == -1) {
                    select(0);
                } else if (focusIndex < getItemCount() - 1) {
                    select(focusIndex + 1);
                }
            }
        }

        @Override
        public void selectAboveCell() {
            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();
            if (pos.getRow() == -1) {
                select(getItemCount() - 1);
            } else if (pos.getRow() > 0) {
                select(pos.getRow() - 1, pos.getTableColumn());
            }
        }

        @Override
        public void selectBelowCell() {
            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();

            if (pos.getRow() == -1) {
                select(0);
            } else if (pos.getRow() < getItemCount() - 1) {
                select(pos.getRow() + 1, pos.getTableColumn());
            }
        }

        @Override
        public void selectFirst() {
            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> focusedCell = getFocusedCell();

            if (getSelectionMode() == SelectionMode.SINGLE) {
                quietClearSelection();
            }

            if (getItemCount() > 0) {
                if (isCellSelectionEnabled()) {
                    select(0, focusedCell.getTableColumn());
                } else {
                    select(0);
                }
            }
        }

        @Override
        public void selectLast() {
            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> focusedCell = getFocusedCell();

            if (getSelectionMode() == SelectionMode.SINGLE) {
                quietClearSelection();
            }

            final int numItems = getItemCount();
            if (numItems > 0 && getSelectedIndex() < numItems - 1) {
                if (isCellSelectionEnabled()) {
                    select(numItems - 1, focusedCell.getTableColumn());
                } else {
                    select(numItems - 1);
                }
            }
        }

        @Override
        public void selectLeftCell() {
            if (!isCellSelectionEnabled()) {
                return;
            }

            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();
            if (pos.getColumn() - 1 >= 0) {
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
            }
        }

        @Override
        public void selectRightCell() {
            if (!isCellSelectionEnabled()) {
                return;
            }

            final TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos = getFocusedCell();
            if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
                select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
            }
        }

        /**
         * *********************************************************************
         *                                                                     *
         * Support code * *
         * ********************************************************************
         */
        private TableColumn<ObservableList<SpreadsheetCell<?>>, ?> getTableColumn(int pos) {
            return getTableView().getVisibleLeafColumn(pos);
        }

        //        private TableColumn<S,?> getTableColumn(TableColumn<S,?> column) {
        //            return getTableColumn(column, 0);
        //        }
        // Gets a table column to the left or right of the current one, given an offset
        private TableColumn<ObservableList<SpreadsheetCell<?>>, ?> getTableColumn(TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column, int offset) {
            final int columnIndex = getTableView().getVisibleLeafIndex(column);
            final int newColumnIndex = columnIndex + offset;
            return getTableView().getVisibleLeafColumn(newColumnIndex);
        }

        private void updateSelectedIndex(int row) {
            setSelectedIndex(row);
            setSelectedItem(getModelItem(row));
        }

        @Override
        public void focus(int row) {
            focus(row, null);
        }

        private void focus(int row, TableColumn<ObservableList<SpreadsheetCell<?>>, ?> column) {
            focus(new TablePosition<>(getTableView(), row, column));
        }

        private void focus(TablePosition<ObservableList<SpreadsheetCell<?>>, ?> pos) {
            if (getTableView().getFocusModel() == null) {
                return;
            }

            getTableView().getFocusModel().focus(pos.getRow(), pos.getTableColumn());
        }

        @Override
        public int getFocusedIndex() {
            return getFocusedCell().getRow();
        }

        private TablePosition<ObservableList<SpreadsheetCell<?>>, ?> getFocusedCell() {
            if (getTableView().getFocusModel() == null) {
                return new TablePosition<>(getTableView(), -1, null);
            }
            return getTableView().getFocusModel().getFocusedCell();
        }

        @Override protected int getItemCount() {
            return itemCount;
            //        List<S> items = spreadsheetView.getItems();
            //        return items == null ? -1 : items.size();
        }

        @Override protected ObservableList<SpreadsheetCell<?>> getModelItem(int index) {
            if (index < 0 || index > getItemCount()) {
                return null;
            }
            return tableView.getItems().get(index);
        }

        private void updateItemCount() {
            if (tableView == null) {
                itemCount = -1;
            } else {
                final List<ObservableList<SpreadsheetCell<?>>> items = tableView.getItems();
                itemCount = items == null ? -1 : items.size();
            }
        }

        /**
         * A list of Integer with the current selected Rows. This is useful for columnheader and
         * RowHeader because they need to highligh when a selection is made.
         */
        private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
        public ObservableList<Integer> getSelectedRows() {
            return selectedRows;
        }

        /**
         * A list of Integer with the current selected Columns. This is useful for columnheader and
         * RowHeader because they need to highligh when a selection is made.
         */
        private final ObservableList<Integer> selectedColumns= FXCollections.observableArrayList();
        public ObservableList<Integer> getSelectedColumns() {
            return selectedColumns;
        }

        private void addSelectedRowsAndColumns(TablePosition<?, ?> t){
            final SpreadsheetCell<?> cell = tableView.getItems().get(t.getRow()).get(t.getColumn());
            for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
                selectedRows.add(i);
                for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
                    selectedColumns.add(j);
                }
            }
        }
        private void removeSelectedRowsAndColumns(TablePosition<?, ?> t){
            final SpreadsheetCell<?> cell = tableView.getItems().get(t.getRow()).get(t.getColumn());
            for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
                selectedRows.remove(Integer.valueOf(i));
                for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
                    selectedColumns.remove(Integer.valueOf(j));
                }
            }
        }

        /*****************************************************************
         * 				END OF MODIFIED BY NELLARMONIA
         *****************************************************************/
    }
}
