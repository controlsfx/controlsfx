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
package impl.org.controlsfx.spreadsheet;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Region;
import javafx.stage.Screen;
import javafx.util.Callback;

import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ResizeFeaturesBase;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.input.MouseEvent;

/**
 * This skin is actually the skin of the SpreadsheetGridView (tableView)
 * contained within the SpreadsheetView. The skin for the SpreadsheetView itself
 * currently resides inside the SpreadsheetView constructor!
 *
 * We need to extends directly from TableViewSkinBase in order to work-around
 * https://javafx-jira.kenai.com/browse/RT-34753 if we want to set a custom
 * TableViewBehavior.
 *
 */
public class GridViewSkin extends TableViewSkinBase<ObservableList<SpreadsheetCell>,ObservableList<SpreadsheetCell>,TableView<ObservableList<SpreadsheetCell>>,TableViewBehavior<ObservableList<SpreadsheetCell>>,TableRow<ObservableList<SpreadsheetCell>>,TableColumn<ObservableList<SpreadsheetCell>,?>> {
    
    static {
        // refer to ControlsFXControl for why this is necessary
        StyleManager.getInstance().addUserAgentStylesheet(
                SpreadsheetView.class.getResource("spreadsheet.css").toExternalForm()); //$NON-NLS-1$
    }
    
    
    /***************************************************************************
     * * STATIC FIELDS * *
     **************************************************************************/

    /** Default height of a row. */
    public static final double DEFAULT_CELL_HEIGHT;

    // FIXME This should seriously be investigated ..
    private static final double DATE_CELL_MIN_WIDTH = 200 - Screen.getPrimary().getDpi();

    static {
        double cell_size = 24.0;
        try {
            Class<?> clazz = com.sun.javafx.scene.control.skin.CellSkinBase.class;
            Field f = clazz.getDeclaredField("DEFAULT_CELL_SIZE"); //$NON-NLS-1$
            f.setAccessible(true);
            cell_size = f.getDouble(null);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        DEFAULT_CELL_HEIGHT = cell_size;
    }

    /**
     * When we add some tableCell to some topRow in order for them to be on top
     * in term of z-order. We may end up with the situation where the row that
     * put the cell is not in the ViewPort anymore. For example when a fixedRow
     * has taken over the real row when scrolling down. Then, the tableCell
     * added is still hanging out in the topRow. That tableCell has no clue that
     * its "creator" has been destroyed or re-used since that tableCell was not
     * technically belonging to its "creator". Therefore, we need to track those
     * cells in order to remove them each time.
     */
    final Map<GridRow,Set<CellView>> deportedCells = new HashMap<>();
    /***************************************************************************
     * * PRIVATE FIELDS * *
     **************************************************************************/
    /**
     * When resizing, we save the height here in order to override default row
     * height. package protected.
     */
    ObservableMap<Integer, Double> rowHeightMap = FXCollections.observableHashMap();

    /** The editor. */
    private GridCellEditor gridCellEditor;

    protected final SpreadsheetHandle handle;
    protected SpreadsheetView spreadsheetView;
    protected VerticalHeader verticalHeader;
    protected HorizontalPicker horizontalPickers;
    
    /**
     * The currently fixedRow. This handles an Integer's set of rows being
     * fixed. NOT Fixable but truly fixed.
     */
    private ObservableSet<Integer> currentlyFixedRow = FXCollections.observableSet(new HashSet<Integer>());

    /**
     * A list of Integer with the current selected Rows. This is useful for
     * HorizontalHeader and VerticalHeader because they need to highlight when a
     * selection is made.
     */
    private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();

    /**
     * A list of Integer with the current selected Columns. This is useful for
     * HorizontalHeader and VerticalHeader because they need to highlight when a
     * selection is made.
     */
    private final ObservableList<Integer> selectedColumns = FXCollections.observableArrayList();

    /**
     * The total height of the currently fixedRows.
     */
    private double fixedRowHeight = 0;

    /**
     * These variable try to optimize the layout of the rows in order not to layout
     * every time every row.
     * 
     * So rowToLayout contains the rows that really needs layout(contain span or fixed).
     * 
     * And hBarValue is an indicator for the VirtualFlow. When the Hbar is touched, this BitSet
     * is set to false. And when a row is drawing, it flips its value in this BitSet. 
     * So that we know when scrolling up or down whether a row has taken into account
     * that the HBar was moved (otherwise, blank area may appear).
     */
    BitSet hBarValue;
    BitSet rowToLayout;
    
    /**
     * This rectangle will be used for drawing a border around the selection.
     */
    RectangleSelection rectangleSelection;
    
    /**
     * This is the current width used by the currently fixed column on the left. 
     */
    double fixedColumnWidth;
    
    /***************************************************************************
     * * CONSTRUCTOR * *
     **************************************************************************/
    public GridViewSkin(final SpreadsheetHandle handle) {
        super(handle.getGridView(), new GridViewBehavior(handle.getGridView()));
        super.init(handle.getGridView());
        
        this.handle = handle;
        this.spreadsheetView = handle.getView();
        gridCellEditor = new GridCellEditor(handle);
        TableView<ObservableList<SpreadsheetCell>> tableView = handle.getGridView();

        //Set a new row factory, useful when handling row height.
        tableView.setRowFactory(new Callback<TableView<ObservableList<SpreadsheetCell>>, TableRow<ObservableList<SpreadsheetCell>>>() {
            @Override
            public TableRow<ObservableList<SpreadsheetCell>> call(TableView<ObservableList<SpreadsheetCell>> p) {
                return new GridRow(handle);
            }
        });

        tableView.getStyleClass().add("cell-spreadsheet"); //$NON-NLS-1$

        getCurrentlyFixedRow().addListener(currentlyFixedRowListener);
        spreadsheetView.getFixedRows().addListener(fixedRowsListener);
        spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);

        init();
        /**
         * When we are changing the grid we re-instantiate the rowToLayout because
         * spans and fixedRow may have changed.
         */
        handle.getView().gridProperty().addListener(new ChangeListener<Grid>() {

            @Override
            public void changed(ObservableValue<? extends Grid> ov, Grid t, Grid t1) {
                 rowToLayout = initRowToLayoutBitSet();
            }
        });
        hBarValue = new BitSet(handle.getView().getGrid().getRowCount());
        rowToLayout = initRowToLayoutBitSet();
        // Because fixedRow Listener is not reacting first time.
        computeFixedRowHeight();
        
        
        EventHandler<MouseEvent> ml = (MouseEvent event) -> {
            // RT-15127: cancel editing on scroll. This is a bit extreme
            // (we are cancelling editing on touching the scrollbars).
            // This can be improved at a later date.
            if (tableView.getEditingCell() != null) {
                tableView.edit(-1, null); 
            }
            
            // This ensures that the table maintains the focus, even when the vbar
            // and hbar controls inside the flow are clicked. Without this, the
            // focus border will not be shown when the user interacts with the
            // scrollbars, and more importantly, keyboard navigation won't be
            // available to the user.
            tableView.requestFocus();
        };
        
        getFlow().getVerticalBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
        getFlow().getHorizontalBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);

        // init the behavior 'closures'
        TableViewBehavior<ObservableList<SpreadsheetCell>> behavior = getBehavior();
        behavior.setOnFocusPreviousRow(new Runnable() {
            @Override public void run() { onFocusPreviousCell(); }
        });
        behavior.setOnFocusNextRow(new Runnable() {
            @Override public void run() { onFocusNextCell(); }
        });
        behavior.setOnMoveToFirstCell(new Runnable() {
            @Override public void run() { onMoveToFirstCell(); }
        });
        behavior.setOnMoveToLastCell(new Runnable() {
            @Override public void run() { onMoveToLastCell(); }
        });
        behavior.setOnScrollPageDown(new Callback<Boolean, Integer>() {
            @Override public Integer call(Boolean isFocusDriven) { return onScrollPageDown(isFocusDriven); }
        });
        behavior.setOnScrollPageUp(new Callback<Boolean, Integer>() {
            @Override public Integer call(Boolean isFocusDriven) { return onScrollPageUp(isFocusDriven); }
        });
        behavior.setOnSelectPreviousRow(new Runnable() {
            @Override public void run() { onSelectPreviousCell(); }
        });
        behavior.setOnSelectNextRow(new Runnable() {
            @Override public void run() { onSelectNextCell(); }
        });
        behavior.setOnSelectLeftCell(new Runnable() {
            @Override public void run() { onSelectLeftCell(); }
        });
        behavior.setOnSelectRightCell(new Runnable() {
            @Override public void run() { onSelectRightCell(); }
        });

        registerChangeListener(tableView.fixedCellSizeProperty(), "FIXED_CELL_SIZE");
    }

    /**
     * Compute the height of a particular row. If the row is in
     * {@link Grid#AUTOFIT}, {@link #DEFAULT_CELL_HEIGHT} is returned.
     *
     * @param row
     * @return
     */
    public double getRowHeight(int row) {
        Double rowHeightCache = rowHeightMap.get(row);
        if (rowHeightCache == null) {
            double rowHeight = handle.getView().getGrid().getRowHeight(row);
            return rowHeight == Grid.AUTOFIT ? DEFAULT_CELL_HEIGHT : rowHeight;
        } else {
            return rowHeightCache;
        }
    }

    public double getFixedRowHeight() {
        return fixedRowHeight;
    }

    public ObservableList<Integer> getSelectedRows() {
        return selectedRows;
    }

    public ObservableList<Integer> getSelectedColumns() {
        return selectedColumns;
    }

    public GridCellEditor getSpreadsheetCellEditorImpl() {
        return gridCellEditor;
    }

    /**
     * This return the GridRow which has the specified index if found. Otherwise
     * null is returned.
     *
     * @param index
     * @return
     */
    public GridRow getRowIndexed(int index) {
        for (GridRow obj : (List<GridRow>) getFlow().getCells()) {
            if (obj.getIndex() == index) {
                return obj;
            }
        }
        return null;
    }
    
    /**
     * This return the row at the specified index in the list. The index
     * specified HAS NOTHING to do with the index of the row.
     * @see #getRowIndexed(int) for a getting a row with its real index.
     * @param index
     * @return
     */
    public GridRow getRow(int index) {
        return (GridRow) getFlow().getCells().get(index);
    }

    /**
     * Indicate whether or not the row at the specified index is currently being
     * displayed.
     * 
     * @param index
     * @return
     */
    public final boolean containsRow(int index) {
        for (Object obj : getFlow().getCells()) {
            if (((GridRow) obj).getIndex() == index)
                return true;
        }
        return false;
    }

    public int getCellsSize() {
        return getFlow().getCells().size();
    }

    public VirtualScrollBar getHBar() {
        return getFlow().getHorizontalBar();
    }

    public VirtualScrollBar getVBar() {
        return getFlow().getVerticalBar();
    }

    /**
     * Will compute for each row the necessary height and fit the line.
     * This can degrade performance a lot so need to use it wisely. 
     * But I don't see other solutions right now.
     */
    public void resizeRowsToFitContent() {
        if(getSkinnable().getColumns().isEmpty()){
            return;
        }
        
        final TableColumn<ObservableList<SpreadsheetCell>, ?> col = getSkinnable().getColumns().get(0);
        List<?> items = itemsProperty().get();
        if (items == null || items.isEmpty()) {
            return;
        }
        Callback/* <TableColumn<T, ?>, TableCell<T,?>> */ cellFactory = col.getCellFactory();
        if (cellFactory == null) {
            return;
        }

        CellView cell = (CellView) cellFactory.call(col);
        if (cell == null) {
            return;
        }

        // set this property to tell the TableCell we want to know its actual
        // preferred width, not the width of the associated TableColumnBase
        cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE); //$NON-NLS-1$
        
        // determine cell padding
        double padding = 10;
        Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
        if (n instanceof Region) {
            Region r = (Region) n;
            padding = r.snappedTopInset() + r.snappedBottomInset();
        }

        double maxHeight;
        int maxRows = handle.getView().getGrid().getRowCount();
        for (int row = 0; row < maxRows; row++) {
            maxHeight = 0;
            for (TableColumn column : getSkinnable().getColumns()) {

                cell.updateTableColumn(column);
                cell.updateTableView(handle.getGridView());
                cell.updateIndex(row);

                if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
                    getChildren().add(cell);
                    cell.setWrapText(true);

                    cell.impl_processCSS(false);
                    maxHeight = Math.max(maxHeight, cell.prefHeight(col.getWidth()));
                    getChildren().remove(cell);
                }
            }
            rowHeightMap.put(row, maxHeight + padding);
        }
        rectangleSelection.updateRectangle();
    }
    
    public void resizeRowsToMaximum() {
        //First we resize to fit.
        resizeRowsToFitContent();
        
        //Then we take the maximum and apply it everywhere.
        double maxHeight = 0;
        for(int key:rowHeightMap.keySet()){
            maxHeight = Math.max(maxHeight, rowHeightMap.get(key));
        }
        
        rowHeightMap.clear();
        int maxRows = handle.getView().getGrid().getRowCount();
        for (int row = 0; row < maxRows; row++) {
            rowHeightMap.put(row, maxHeight);
        }
        rectangleSelection.updateRectangle();
    }
    
    public void resizeRowsToDefault() {
        rowHeightMap.clear();
        
        /**
         * When resizing to default, we need to go through the visible rows in
         * order to update them directly. Because if the rowHeightMap is empty,
         * the rows will not detect that maybe the height has changed.
         */
        for (GridRow row : (List<GridRow>) getFlow().getCells()) {
            double newHeight = row.getPrefHeight();
            if(row.getPrefHeight() != newHeight){
                row.setPrefHeight(newHeight);
                row.requestLayout();
            }
        }
        
        //Fixing https://bitbucket.org/controlsfx/controlsfx/issue/358/
        getFlow().layoutChildren();
        rectangleSelection.updateRectangle();
    }
    /**
     * We want to have extra space when displaying LocalDate because they will
     * use an editor that display a little icon on the right. Thus, that icon is
     * reducing the visibility of the date string.
     */
    @Override
    public void resizeColumnToFitContent(TableColumn<ObservableList<SpreadsheetCell>, ?> tc, int maxRows) {
        final TableColumn<ObservableList<SpreadsheetCell>, ?> col = tc;
        List<?> items = itemsProperty().get();
        if (items == null || items.isEmpty()) {
            return;
        }

        Callback/* <TableColumn<T, ?>, TableCell<T,?>> */ cellFactory = col.getCellFactory();
        if (cellFactory == null) {
            return;
        }

        TableCell<ObservableList<SpreadsheetCell>, ?> cell = (TableCell<ObservableList<SpreadsheetCell>, ?>) cellFactory
                .call(col);
        if (cell == null) {
            return;
        }

        //The current index of that column
        int indexColumn = handle.getGridView().getColumns().indexOf(tc);
        
        // set this property to tell the TableCell we want to know its actual
        // preferred width, not the width of the associated TableColumnBase
        cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE); //$NON-NLS-1$

        // determine cell padding
        double padding = 10;
        Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
        if (n instanceof Region) {
            Region r = (Region) n;
            padding = r.snappedLeftInset() + r.snappedRightInset();
        }

        ObservableList<ObservableList<SpreadsheetCell>> gridRows = spreadsheetView.getGrid().getRows();//.get(row)
        
        int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows);
        double maxWidth = 0;
        boolean datePresent = false;
        for (int row = 0; row < rows; row++) {
            cell.updateTableColumn(col);
            cell.updateTableView(handle.getGridView());
            cell.updateIndex(row);
            
            if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
                getChildren().add(cell);

                if (((SpreadsheetCell) cell.getItem()).getItem() instanceof LocalDate) {
                    datePresent = true;
                }
                cell.impl_processCSS(false);
                double width = cell.prefWidth(-1);
                
                /**
                 * If the cell is spanning in column, we need to take the other
                 * columns into account in the calculation of the width. So we
                 * compute the width needed by the cell and we substract the
                 * remaining columns width in order not to have a huge width for
                 * the considered column.
                 *
                 * Also if the cell considered is not in the column, then we can
                 * directly continue because we don't want to take its width
                 * into account for the current column.
                 */
                SpreadsheetCell spc = gridRows.get(row).get(indexColumn);
                if(spc.getColumn() != indexColumn){
                    getChildren().remove(cell);
                    continue;
                }
                if (spc.getColumnSpan() > 1) {
                    for (int i = 1; i < spc.getColumnSpan(); ++i) {
                        width -= spreadsheetView.getColumns().get(indexColumn + i).getWidth();
                    }
                }
                maxWidth = Math.max(maxWidth, width);
                getChildren().remove(cell);
            }
        }

        // dispose of the cell to prevent it retaining listeners (see RT-31015)
        cell.updateIndex(-1);

        // RT-23486
        double widthMax = maxWidth + padding;
        if (handle.getGridView().getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY) {
            widthMax = Math.max(widthMax, col.getWidth());
        }

        if (datePresent && widthMax < DATE_CELL_MIN_WIDTH) {
            widthMax = DATE_CELL_MIN_WIDTH;
        }
        if(col.isResizable()){
            col.setPrefWidth(widthMax);
        }
        rectangleSelection.updateRectangle();
    }

    /***************************************************************************
     * * PRIVATE/PROTECTED METHOD * *
     **************************************************************************/
    protected final void init() {
        rectangleSelection = new RectangleSelection(this, (SpreadsheetViewSelectionModel) spreadsheetView.getSelectionModel());
        getFlow().getVerticalBar().valueProperty().addListener(vbarValueListener);
        verticalHeader = new VerticalHeader(handle);
        getChildren().add(verticalHeader);

        ((HorizontalHeader) getTableHeaderRow()).init();
        verticalHeader.init(this, (HorizontalHeader) getTableHeaderRow());
        
        horizontalPickers = new HorizontalPicker((HorizontalHeader) getTableHeaderRow(), spreadsheetView);
        getChildren().add(horizontalPickers);
        getFlow().init(spreadsheetView);
    }

    protected final ObservableSet<Integer> getCurrentlyFixedRow() {
        return currentlyFixedRow;
    }

    /**
     * Used in the HorizontalColumnHeader when we need to resize in double
     * click.
     * 
     * @param tc
     */
    void resize(TableColumnBase<?, ?> tc) {
        resizeColumnToFitContent(getColumns().get(getColumns().indexOf(tc)), -1);
    }

    @Override
    protected void layoutChildren(double x, double y, double w, final double h) {
        if (spreadsheetView == null) {
            return;
        }
        double verticalHeaderWidth = verticalHeader.computeHeaderWidth();
        double horizontalPickerHeight = spreadsheetView.getColumnPickers().isEmpty() ? 0: VerticalHeader.PICKER_SIZE;
        
        if (spreadsheetView.isShowRowHeader() || !spreadsheetView.getRowPickers().isEmpty()) {
            x += verticalHeaderWidth;
            w -=  verticalHeaderWidth;
        }

        
        y += horizontalPickerHeight;
        super.layoutChildren(x, y, w, h-horizontalPickerHeight);

        final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
        double tableHeaderRowHeight = 0;

        if(!spreadsheetView.getColumnPickers().isEmpty()){
            layoutInArea(horizontalPickers, x, y - VerticalHeader.PICKER_SIZE, w, tableHeaderRowHeight, baselineOffset, HPos.CENTER, VPos.CENTER);
        }
        
        if (spreadsheetView.showColumnHeaderProperty().get()) {
            // position the table header
            tableHeaderRowHeight = getTableHeaderRow().prefHeight(-1);
            layoutInArea(getTableHeaderRow(), x, y, w, tableHeaderRowHeight, baselineOffset, HPos.CENTER, VPos.CENTER);

            y += tableHeaderRowHeight;
        } else {
            // This is temporary handled in the HorizontalHeader with Css
            // FIXME tweak open in https://javafx-jira.kenai.com/browse/RT-32673
        }

        if (spreadsheetView.isShowRowHeader() || !spreadsheetView.getRowPickers().isEmpty()) {
            layoutInArea(verticalHeader, x - verticalHeaderWidth, y - tableHeaderRowHeight, w, h, baselineOffset,
                    HPos.CENTER, VPos.CENTER);
        }
    }

    @Override
    protected void onFocusPreviousCell() {
        final TableFocusModel<?, ?> fm = getFocusModel();
        if (fm == null) {
            return;
        }
        /*****************************************************************
         * MODIFIED
         *****************************************************************/
        final int row = fm.getFocusedIndex();
//        // We try to make visible the rows that may be hiden by Fixed rows
        if (!getFlow().getCells().isEmpty()
                && getFlow().getCells().get(spreadsheetView.getFixedRows().size()).getIndex() > row
                && !spreadsheetView.getFixedRows().contains(row)) {
            flow.scrollTo(row);
        } else {
            flow.show(row);
        }
        scrollHorizontally();
        /*****************************************************************
         * END OF MODIFIED
         *****************************************************************/
    }

    @Override
    protected void onFocusNextCell() {
        final TableFocusModel<?, ?> fm = getFocusModel();
        if (fm == null) {
            return;
        }
        /*****************************************************************
         * MODIFIED
         *****************************************************************/
        final int row = fm.getFocusedIndex();
        // We try to make visible the rows that may be hidden by Fixed rows
        if (!getFlow().getCells().isEmpty()
                && getFlow().getCells().get(spreadsheetView.getFixedRows().size()).getIndex() > row
                && !spreadsheetView.getFixedRows().contains(row)) {
            flow.scrollTo(row);
        } else {
            flow.show(row);
        }
        scrollHorizontally();
        /*****************************************************************
         * END OF MODIFIED
         *****************************************************************/
    }

    @Override
    protected void onSelectPreviousCell() {
        super.onSelectPreviousCell();
        scrollHorizontally();
    }

    @Override
    protected void onSelectNextCell() {
        super.onSelectNextCell();
        scrollHorizontally();
    }

    @Override
    protected VirtualFlow<TableRow<ObservableList<SpreadsheetCell>>> createVirtualFlow() {
        return new GridVirtualFlow<>(this);
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        return new HorizontalHeader(this);
    }

    BooleanProperty getTableMenuButtonVisibleProperty() {
        return tableMenuButtonVisibleProperty();
    }

    @Override
    public void scrollHorizontally(){
        super.scrollHorizontally();
    }
    
    @Override
    protected void scrollHorizontally(TableColumn<ObservableList<SpreadsheetCell>, ?> col) {

        if (col == null || !col.isVisible()) {
            return;
        }

        // work out where this column header is, and it's width (start -> end)
        double start = 0;// scrollX;
        for (TableColumnBase<?, ?> c : getVisibleLeafColumns()) {
            if (c.equals(col))
                break;
            start += c.getWidth();
        }

        /*****************************************************************
         * MODIFIED : We modified this function so that we ensure that any
         * selected cells will not be below a fixed column. Because when there's
         * some fixed columns, the "left border" is not the table anymore, but
         * the right side of the last fixed columns.
         *****************************************************************/

        final double end = start + col.getWidth();

        // determine the visible width of the table
        final double headerWidth = getSkinnable().getWidth() - snappedLeftInset() - snappedRightInset();

        // determine by how much we need to translate the table to ensure that
        // the start position of this column lines up with the left edge of the
        // tableview, and also that the columns don't become detached from the
        // right edge of the table
        final double pos = getFlow().getHorizontalBar().getValue();
        final double max = getFlow().getHorizontalBar().getMax();
        double newPos;

        if (start < pos + fixedColumnWidth && start >= 0 && start >= fixedColumnWidth) {
            newPos = start - fixedColumnWidth < 0 ? start : start - fixedColumnWidth;
        } else {
            final double delta = start < 0 || end > headerWidth ? start - pos - fixedColumnWidth : 0;
            newPos = pos + delta > max ? max : pos + delta;
        }

        // FIXME we should add API in VirtualFlow so we don't end up going
        // direct to the hbar.
        // actually shift the flow - this will result in the header moving
        // as well
        getFlow().getHorizontalBar().setValue(newPos);
    }

    private void verticalScroll() {
        verticalHeader.requestLayout();
    }

    GridVirtualFlow<?> getFlow() {
        return (GridVirtualFlow<?>) flow;
    }

    /**
     * Return a BitSet of the rows that needs layout all the time. This
     * includes any row containing a span, or a fixed row.
     * @return 
     */
    private BitSet initRowToLayoutBitSet(){
        Grid grid =  handle.getView().getGrid();
        BitSet bitSet = new BitSet(grid.getRowCount());
        for(int row = 0;row<grid.getRowCount();++row){
            if(spreadsheetView.getFixedRows().contains(row)){
                bitSet.set(row);
                continue;
            }
            List<SpreadsheetCell> myRow = grid.getRows().get(row);
            for(SpreadsheetCell cell:myRow){
                
                if(cell.getRowSpan()>1 /*|| cell.getColumnSpan() >1*/){
                    bitSet.set(row);
                    break;
                }
            }
        }
        return bitSet;
    }
    
    /**
     * When the vertical moves, we update the verticalHeader
     */
    private final InvalidationListener vbarValueListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable valueModel) {
            verticalScroll();
        }
    };

    /**
     * We listen on the FixedRows in order to do the modification in the
     * VirtualFlow
     */
    private final ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override
        public void onChanged(Change<? extends Integer> c) {
            hBarValue.clear();
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (Integer fixedRow : c.getList()) {
                        rowToLayout.set(fixedRow, true);
                    }
                } else {
                    for (Integer unfixedRow : c.getRemoved()) {
                        rowToLayout.set(unfixedRow, false);
                    //If the grid permits it, we check the spanning in order not
                        //to remove a row that might need layout.
                        if (spreadsheetView.getGrid().getRows().size() > unfixedRow) {
                            List<SpreadsheetCell> myRow = spreadsheetView.getGrid().getRows().get(unfixedRow);
                            for (SpreadsheetCell cell : myRow) {
                                if (cell.getRowSpan() > 1 || cell.getColumnSpan() > 1) {
                                    rowToLayout.set(unfixedRow, true);
                                    break;
                                }
                            }
                        }
                    }

                    //We check for the newly fixedRow
                    for (Integer fixedRow : c.getAddedSubList()) {
                        rowToLayout.set(fixedRow, true);
                    }
                }
            }
            // requestLayout() not responding immediately..
            getFlow().requestLayout();
        }
    };

    /**
     * We listen on the currentlyFixedRow in order to do the modification in the
     * FixedRowHeight.
     */
    private final SetChangeListener<? super Integer> currentlyFixedRowListener = new SetChangeListener<Integer>() {
        @Override
        public void onChanged(javafx.collections.SetChangeListener.Change<? extends Integer> arg0) {
            computeFixedRowHeight();
        }
    };

    /**
     * We compute the total height of the fixedRows so that the selection can
     * use it without performance regression.
     */
    private void computeFixedRowHeight() {
        fixedRowHeight = 0;
        for (int i : getCurrentlyFixedRow()) {
            fixedRowHeight += getRowHeight(i);
        }
    }

    /**
     * We listen on the FixedColumns in order to do the modification in the
     * VirtualFlow.
     */
    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {
        @Override
        public void onChanged(Change<? extends SpreadsheetColumn> c) {
            hBarValue.clear();
            getFlow().requestLayout();
            // requestLayout() not responding immediately..
//            getFlow().layoutTotal();
        }
    };

    @Override
    protected TableSelectionModel<ObservableList<SpreadsheetCell>> getSelectionModel() {
        return getSkinnable().getSelectionModel();
    }

    @Override
    protected TableFocusModel<ObservableList<SpreadsheetCell>, TableColumn<ObservableList<SpreadsheetCell>, ?>> getFocusModel() {
        return getSkinnable().getFocusModel();
    }

    @Override
    protected TablePositionBase<? extends TableColumn<ObservableList<SpreadsheetCell>, ?>> getFocusedCell() {
        return getSkinnable().getFocusModel().getFocusedCell();
    }

    @Override
    protected ObservableList<? extends TableColumn<ObservableList<SpreadsheetCell>, ?>> getVisibleLeafColumns() {
        return getSkinnable().getVisibleLeafColumns();
    }

    @Override
    protected int getVisibleLeafIndex(TableColumn<ObservableList<SpreadsheetCell>, ?> tc) {
        return getSkinnable().getVisibleLeafIndex(tc);
    }

    @Override
    protected TableColumn<ObservableList<SpreadsheetCell>, ?> getVisibleLeafColumn(int col) {
        return getSkinnable().getVisibleLeafColumn(col);
    }

    @Override
    protected ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> getColumns() {
        return getSkinnable().getColumns();
    }

    @Override
    protected ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> getSortOrder() {
        return getSkinnable().getSortOrder();
    }

    @Override
    protected ObjectProperty<ObservableList<ObservableList<SpreadsheetCell>>> itemsProperty() {
        return getSkinnable().itemsProperty();
    }

    @Override
    protected ObjectProperty<Callback<TableView<ObservableList<SpreadsheetCell>>, TableRow<ObservableList<SpreadsheetCell>>>> rowFactoryProperty() {
        return getSkinnable().rowFactoryProperty();
    }

    @Override
    protected ObjectProperty<Node> placeholderProperty() {
        return getSkinnable().placeholderProperty();
    }

    @Override
    protected BooleanProperty tableMenuButtonVisibleProperty() {
        return getSkinnable().tableMenuButtonVisibleProperty();
    }

    @Override
    protected ObjectProperty<Callback<ResizeFeaturesBase, Boolean>> columnResizePolicyProperty() {
        return (ObjectProperty<Callback<ResizeFeaturesBase, Boolean>>) (Object)getSkinnable().columnResizePolicyProperty();
    }

    @Override
    protected boolean resizeColumn(TableColumn<ObservableList<SpreadsheetCell>, ?> tc, double delta) {
         return getSkinnable().resizeColumn(tc, delta);
    }

    @Override
    protected void edit(int index, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
        getSkinnable().edit(index, column);
    }

    @Override
    public TableRow<ObservableList<SpreadsheetCell>> createCell() {
        TableRow<ObservableList<SpreadsheetCell>> cell;

        if (getSkinnable().getRowFactory() != null) {
            cell = getSkinnable().getRowFactory().call(getSkinnable());
        } else {
            cell = new TableRow<>();
        }

        cell.updateTableView(getSkinnable());
        return cell;
    }

    @Override
    public int getItemCount() {
        return getSkinnable().getItems() == null ? 0 : getSkinnable().getItems().size();
    }
}
