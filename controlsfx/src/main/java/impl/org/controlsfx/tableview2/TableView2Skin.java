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
package impl.org.controlsfx.tableview2;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.transform.Translate;
import org.controlsfx.control.tableview2.TableView2;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static impl.org.controlsfx.tableview2.SortUtils.SortEndedEvent.SORT_ENDED_EVENT;

/**
 * This skin for the TableView2 control
 *
 * We need to extend directly from TableViewSkinBase in order to work-around
 * https://bugs.openjdk.java.net/browse/JDK-8090674 if we want to set a custom
 * TableViewBehavior.
 *
 * @param <S> The type of the objects contained within the {@link TableView2} items list.
 */
public class TableView2Skin<S> extends TableViewSkinBase<S,S, TableView<S>, TableRow<S>, TableColumn<S,?>> {
        
    /***************************************************************************
     * * STATIC FIELDS * *
     **************************************************************************/

    /** Default height of a row. */
    private static final double DEFAULT_CELL_HEIGHT;

    static {
        double cell_size = 24.0;
//        try {
//            Class<?> clazz = javafx.scene.control.skin.CellSkinBase.class;
//            Field f = clazz.getDeclaredField("DEFAULT_CELL_SIZE"); //$NON-NLS-1$
//            f.setAccessible(true);
//            cell_size = f.getDouble(null);
//        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
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
    final Map<TableRow2<S>, Set<TableCell<S, ?>>> deportedCells = new HashMap<>();
    /***************************************************************************
     * * PRIVATE FIELDS * *
     **************************************************************************/
    /**
     * When resizing, we save the height here in order to override default row
     * height. package protected.
     */
    ObservableMap<Integer, Double> rowHeightMap = FXCollections.observableHashMap();

    protected TableView2<S> tableView;
    private final TableView2<S> parentTableView;
    protected RowHeader<S> rowHeader;
    
    /**
     * The currently fixedRow. This handles an Integer's set of rows being
     * fixed. NOT Fixable but truly fixed.
     */
    private final ObservableSet<Integer> currentlyFixedRow = FXCollections.observableSet(new HashSet<>());

    /**
     * A list of Integer with the current selected Rows. This is useful for
     * TableHeaderRow2 and RowHeader because they need to highlight when a
     * selection is made.
     */
    private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
    
    /**
     * A list of Integer with the current selected Columns. This is useful for
     * TableHeaderRow2 and RowHeader because they need to highlight when a
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
     * This is the current width used by the currently fixed column on the left. 
     */
    double fixedColumnWidth;
    
    /**
     * When we try to select cells after a setGrid, we end up with the cell
     * selected but no visual confirmation. In order to prevent that, we need to
     * warn the selectionModel when the layout is starting and then the
     * selectionModel will do the appropriate actions in order to force the
     * visual to come.
     */
    BooleanProperty lastRowLayout = new SimpleBooleanProperty(true);
    
    TableHeaderRow tableHeaderRow;
    
    private boolean key = false; // Register if we last touch the keyboard
    private final EventHandler<KeyEvent> keyPressedEventHandler = e -> key = true;
    private final EventHandler<MouseEvent> mousePressedEventHandler = e -> key = false;
    private final List<Integer> oldSelectedColumns = new ArrayList<>();
    private final List<Integer> oldSelectedRows = new ArrayList<>();
    
    private final VirtualFlow<TableRow<S>> flow;
    
    /***************************************************************************
     * * CONSTRUCTOR * *
     **************************************************************************/

    /**
     * 
     * @param tableView the {@link TableView2}
     */
    public TableView2Skin(TableView2<S> tableView) {
        super(tableView);
        
        this.flow = getVirtualFlow();
        flow.setCellFactory(p -> createCell());
        this.tableView = tableView;
        
        if (tableView.getParent() != null && tableView.getParent() instanceof RowHeader) {
            parentTableView = ((RowHeader) tableView.getParent()).getParentTableView();
        } else {
            parentTableView = null;
        }

        //Bind the row factory, useful to handle the row freezing and row height. 
        // It prevents from setting a different row factory, unless developer  
        // chooses to unbind it and provide on his own risk a different row 
        // factory that may or may not work with TableView2 and row freezing.
        tableView.rowFactoryProperty().bind(Bindings.createObjectBinding(
                () -> param -> new TableRow2(tableView), tableView.skinProperty()));

        getCurrentlyFixedRow().addListener(currentlyFixedRowListener);
        this.tableView.getFixedRows().addListener(fixedRowsListener);
        this.tableView.getFixedColumns().addListener(fixedColumnsListener);

        init();
        
        hBarValue = new BitSet(getItemCount());
        rowToLayout = initRowToLayoutBitSet();
        tableView.rowFixingEnabledProperty().addListener((Observable o) -> {
            rowToLayout = initRowToLayoutBitSet();
            tableView.refresh();
        });
        // Because fixedRow Listener is not reacting first time.
        computeFixedRowHeight();
        
        
        EventHandler<MouseEvent> ml = (MouseEvent event) -> {
            // JDK-8114594: cancel editing on scroll. This is a bit extreme
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

        registerChangeListener(tableView.fixedCellSizeProperty(), t -> {
            tableView.refresh();
            verticalScroll();
            computeFixedRowHeight();
        });
        registerChangeListener(tableView.columnFixingEnabledProperty(), t -> {
            tableView.refresh();
            verticalScroll();
            computeFixedRowHeight();
        });
    }

    /**
     * Compute the height of a particular row. 
     * 
     * If {@link TableView#getFixedCellSize() } is set, all the rows will take
     * this fixed size.
     * 
     * Else if there is no value set, {@link #DEFAULT_CELL_HEIGHT} is returned.
     * 
     * Else, the value is returned from the map that {@link TableRow2Skin} manages
     * to set based on the row requirements.
     *
     * @param row the number of row
     * @return the row height
     */
    public double getRowHeight(int row) {
        if (tableView.getFixedCellSize() > 0) {
            return tableView.getFixedCellSize();
        }
        
        if (row < 0 || row >= getItemCount() || rowHeightMap.get(row) == null) {
            return DEFAULT_CELL_HEIGHT;
        }
        return rowHeightMap.get(row);
    }

    public double getFixedRowHeight() {
        return fixedRowHeight;
    }

    /**
     * Contains the index of the sortedList.
     * @return an ObservableList of row indices
     */
    public ObservableList<Integer> getSelectedRows() {
        return selectedRows;
    }

    public ObservableList<Integer> getSelectedColumns() {
        return selectedColumns;
    }

    /**
     * This returns the TableRow2 which has the specified index if found. Otherwise
     * null is returned.
     *
     * @param index the index
     * @return the {@link TableRow2}
     */
    public TableRow<S> getRowIndexed(int index) {
        List<? extends IndexedCell> cells = getFlow().getCells();
        if (!cells.isEmpty()) {
            IndexedCell cell = cells.get(0);
            if (index >= cell.getIndex() && index - cell.getIndex() < cells.size()) {
                return (TableRow<S>) cells.get(index - cell.getIndex());
            }
        }
        for (IndexedCell cell : getFlow().getFixedCells()) {
            if (cell.getIndex() == index) {
                return (TableRow<S>) cell;
            }
        }
        return null;
    }
    
    /**
     * This return the first index displaying a cell in case of a rowSpan. If
     * the returned index is the same as given, it means the current cell is the
     * one showing. Otherwise, it means another cell above will be the one
     * drawn.
     *
     * @param pos the {@link TablePositionBase}
     * @param index the index
     * @return the index of the first row
     */
    public int getFirstRow(TablePositionBase<?> pos, int index) {
        // TODO: Find the first index in a rowSpan
        return index;
    }
    
    /**
     * This return the row at the specified index in the list. The index
     * specified HAS NOTHING to do with the index of the row.
     * 
     * @see #getRowIndexed(int) for a getting a row with its real index.
     * @param index the number of row
     * @return the {@link TableView2}
     */
    public TableRow<S> getRow(int index) {
        if (index < getFlow().getCells().size()) {
            return (TableRow<S>) getFlow().getCells().get(index);
        }
        return null;
    }

    /**
     * Indicates whether or not the row at the specified index is currently being
     * displayed.
     * 
     * @param index the index
     * @return whether or not the row at the specified index is currently being
     * displayed
     */
    public final boolean containsRow(int index) {
        /**
         * When scrolling with mouse wheel, some row are present but will not be
         * lay out. Thus we only consider the row with children as really
         * available.
         */
        for (Object obj : getFlow().getCells()) {
            if (((TableRow2) obj).getIndex() == index && !((TableRow2) obj).getChildrenUnmodifiable().isEmpty())
                return true;
            }
        return false;
    }

    public int getCellsSize() {
        return getFlow().getCells().size();
    }

    public ScrollBar getHBar() {
        if (getFlow() != null) {
            return getFlow().getHorizontalBar();
        }
        return null;
    }

    public ScrollBar getVBar() {
        return getFlow().getVerticalBar();
    }

////    /**
////     * 
////     * @param tc the {@link TableColumn}
////     * @param maxRows the maximum number of rows
////     */
////    @Override
////    public void resizeColumnToFitContent(TableColumn<S, ?> tc, int maxRows) {
////        if (tc == null || ! tc.isResizable() || getTableHeaderRow().getColumnHeaderFor(tc) instanceof NestedTableColumnHeader) {
////            return;
////        }
////        
////        final TableColumn<S, ?> col = tc;
////        List<?> items = itemsProperty().get();
////        if (items == null || items.isEmpty()) {
////            return;
////        }
////
////        Callback/* <TableColumn<T, ?>, TableCell<T,?>> */ cellFactory = col.getCellFactory();
////        if (cellFactory == null) {
////            return;
////        }
////
////        TableCell cell = (TableCell<S, ?>) cellFactory.call(col);
////        if (cell == null) {
////            return;
////        }
////
////        //The current index of that column
////        int indexColumn = tableView.getColumns().indexOf(tc);
////        
////        // set this property to tell the TableCell we want to know its actual
////        // preferred width, not the width of the associated TableColumnBase
////        cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE); //$NON-NLS-1$
////        
////        // determine cell padding
////        double padding = 10;
////        Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
////        if (n instanceof Region) {
////            Region r = (Region) n;
////            padding = r.snappedLeftInset() + r.snappedRightInset();
////        }
////
////        /**
////         * If maxRows is -1, we take all rows. If it's 30, it means it's coming
////         * from TableColumnHeader during initialization, so we push it to 100.
////         */
////        int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows == 30 ? 100 : maxRows);
////        double maxWidth = 0;
////        cell.updateTableColumn(col);
////        cell.updateTableView(tableView);
////        TableColumn<S, ?> column = null;
////        if (indexColumn > -1) {
////            column = tableView.getColumns().get(indexColumn);
////        }
////        
////        for (int row = 0; row < rows; row++) {
////            cell.updateIndex(row);
////            
////            if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null) {
////                getChildren().add(cell);
////
////                cell.applyCss();
////                double width = cell.prefWidth(-1);
////                /**
////                 * If the cell is spanning in column, we need to take the other
////                 * columns into account in the calculation of the width. So we
////                 * compute the width needed by the cell and we substract the
////                 * other columns width.
////                 *
////                 * Also if the cell considered is not in the column, we still
////                 * have to compute because a previous column may have based its
////                 * calculation on the current width which will be modified.
////                 */
////                if (indexColumn < getColumns().size() && column != null) {
////                    int viewColumn = getViewColumn(indexColumn);
////                    TablePosition<S, ?> pos = new TablePosition<>(tableView, row, column);
////                    if (tableView.getColumnSpan(pos) > 1) {
////                        for (int i = viewColumn; i < viewColumn + tableView.getColumnSpan(pos); ++i) {
////                            if (i != indexColumn) {
////                                width -= tableView.getColumns().get(i).getWidth();
////                            }
////                        }
////                    }
////                }
////                maxWidth = Math.max(maxWidth, width);
////                getChildren().remove(cell);
////            }
////        }
////
////        // dispose of the cell to prevent it retaining listeners (see JDK-8122968)
////        cell.updateIndex(-1);
////
////        // JDK-8096512 - take into account the column header text / graphic widths.
////        double headerWidth = computeHeaderWidth(getTableHeaderRow().getColumnHeaderFor(tc));
////        maxWidth = Math.max(maxWidth, headerWidth);
////
////        // JDK-8126253
////        double widthMax = maxWidth + padding;
////        if (tableView.getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY) {
////            widthMax = Math.max(widthMax, col.getWidth());
////        }
////        
////        widthMax = snapSize(widthMax);
////        
////        col.impl_setWidth(widthMax);
////    }

    /***************************************************************************
     * * PRIVATE/PROTECTED METHOD * *
     **************************************************************************/
    protected final void init() {
        if (parentTableView == null) {
            rowHeader = new RowHeader<>(tableView);
            getChildren().add(rowHeader);
            rowHeader.init(this, (TableHeaderRow2) getTableHeaderRow());
        }        
        
        getFlow().getVerticalBar().valueProperty().addListener(vbarValueListener);
        ((TableHeaderRow2) getTableHeaderRow()).init();
        getFlow().init();
        
        if (parentTableView != null) {
            // Don't add listeners to inner table
            return;
        }
        
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, new WeakEventHandler<>(keyPressedEventHandler));
        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(mousePressedEventHandler));

        //update headers and selection while keyboard navigation
        if (tableView.getSelectionModel() != null) {
            tableView.getSelectionModel().selectedIndexProperty().addListener((obs, ov, nv) -> {
                updateHeaders();
                scrollToVisibleCell(nv, ov);
            });
            tableView.getSelectionModel().getSelectedCells().addListener((Observable o) -> updateHeaders());
            tableView.selectionModelProperty().addListener((observable, oldModel, newModel) -> {
                if (newModel != null) {
                    newModel.selectedIndexProperty().addListener((obs, ov, nv) -> {
                        updateHeaders();
                        scrollToVisibleCell(nv, ov);
                    });
                    newModel.getSelectedCells().addListener((Observable o) -> updateHeaders());
                }
            });
        }
        tableView.getVisibleLeafColumns().addListener((Observable o) -> updateHeaders());

        /*
         * Column Header Drag
         */ 
        // Adjust column overlay based on headers offset
        getChildren().stream()
                .filter(n -> n.getStyleClass().contains("column-overlay"))
                .findFirst()
                .ifPresent(n -> {
                    n.translateXProperty().bind(rowHeader.rowHeaderWidthProperty());
                });
        
        // Adjust line based in rowHeader offset
        final Region columnReorderLine = (Region) getChildren().get(3); // getColumnReorderLine();
        columnReorderLine.translateXProperty().addListener((obs, ov, nv) -> {
            if (! tableView.isRowHeaderVisible() && ! columnReorderLine.getTransforms().isEmpty()) {
                columnReorderLine.getTransforms().clear();
            } else if (tableView.isRowHeaderVisible()) {
                columnReorderLine.getTransforms().setAll(new Translate(getRowHeaderOffset(), 0));
            }
        });
        columnReorderLine.boundsInParentProperty().addListener((obs, ov, nv) -> 
            columnReorderLine.setOpacity(nv.getMaxX() > tableView.getWidth() ? 0 : 1));
        
        // Adjust corner region based in rowHeader offset
        getTableHeaderRow().getChildren().stream()
                .filter(n -> n.getStyleClass().contains("show-hide-columns-button"))
                .findFirst()
                .ifPresent(n -> n.translateXProperty().bind(rowHeader.rowHeaderWidthProperty().multiply(-1)));

        // sorting: the fixed rows need to be rebuilt, otherwise they keep the 
        // wrong tableRow
        tableView.addEventHandler(SortEvent.ANY, e -> {
            if (e != null && SORT_ENDED_EVENT.equals(e.getEventType())) {
                getFlow().rebuildFixedCells();
            }
        });
    }

    protected final ObservableSet<Integer> getCurrentlyFixedRow() {
        return currentlyFixedRow;
    }

    /**
     * Allows notifications in the TableView2 control
     */
    private final IntegerProperty size = new SimpleIntegerProperty();
    public final IntegerProperty sizeProperty() { return size; }
    
    /** {@inheritDoc} */
    @Override protected void layoutChildren(double x, double y, double w, final double h) {
        if (tableView == null) {
            return;
        }
        size.set(getItemCount());
        
        double width = w;
        double height = h;
        
        double rowHeaderWidth = getRowHeaderOffset();
        
        if (tableView.isRowHeaderVisible()) {
            x += rowHeaderWidth;
            width -= rowHeaderWidth;
        }
        
        super.layoutChildren(x, y, width, height);

        final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
        double tableHeaderRowHeight = 0;

        if (isColumnHeaderVisible()) {
            // position the table header
            tableHeaderRowHeight = tableHeaderRow.prefHeight(-1);
            layoutInArea(getTableHeaderRow(), x, y, width, tableHeaderRowHeight, baselineOffset, HPos.CENTER, VPos.CENTER);
            
            y += tableHeaderRowHeight;
        }

        if (tableView.isRowHeaderVisible() && rowHeader != null) {
            layoutInArea(rowHeader, x - rowHeaderWidth, y - tableHeaderRowHeight, rowHeaderWidth, height, baselineOffset,
                    HPos.CENTER, VPos.CENTER);
        }
    }

    protected boolean isColumnHeaderVisible() {
        if (tableHeaderRow == null) {
            tableHeaderRow = getTableHeaderRow();
        }
        return tableHeaderRow.isVisible();
    }
    
    
    protected int getViewColumn(int modelColumn) {
       return (int) tableView.getColumns().stream()
               .limit(modelColumn > - 1 ? modelColumn : 0)
               .filter(TableColumn::isVisible)
               .count();
    }

    /** {@inheritDoc} */
    @Override protected void onFocusAboveCell() {
        focusScroll();
    }

    /** {@inheritDoc} */
    @Override protected void onFocusBelowCell() {
        focusScroll();
    }

    private int getFixedRowSize() {
        return tableView.getFixedRows().size();
    }
    
    void focusScroll() {
        final TableFocusModel<?, ?> fm = tableView.getFocusModel();
        if (fm == null) {
            return;
        }
        /**
         * ***************************************************************
         * MODIFIED
         ****************************************************************
         */
        final int row = fm.getFocusedIndex();
        // We try to make visible the rows that may be hidden by Fixed rows
        if (!getFlow().getCells().isEmpty()
                //FIXME
                && getFlow().getCells().get(getFixedRowSize()).getIndex() > row
                && !tableView.getFixedRows().contains(row)) {
            flow.scrollTo(row);
        } else {
            flow.scrollTo(row);
        }
        scrollHorizontally();
        /**
         * ***************************************************************
         * END OF MODIFIED
         ****************************************************************
         */
    }

    /** {@inheritDoc} */
    @Override protected void onSelectAboveCell() {
        super.onSelectAboveCell();
        scrollHorizontally();
    }

    /** {@inheritDoc} */
    @Override protected void onSelectBelowCell() {
        super.onSelectBelowCell();
        scrollHorizontally();
    }

    /** {@inheritDoc} */
    @Override protected VirtualFlow<TableRow<S>> createVirtualFlow() {
        return new TableView2VirtualFlow<>(this);
    }

    @Override
    protected TableHeaderRow createTableHeaderRow() {
        return new TableHeaderRow2(this);
    }
    
    protected TableHeaderRow2 getTableHeaderRow2(){
        return (TableHeaderRow2) getTableHeaderRow();
    }
    
    private double getRowHeaderOffset() {
        if (rowHeader != null) {
            return rowHeader.computeHeaderWidth();
        }
        return 0d;
    }
    
    protected SouthTableHeaderRow getSouthHeader() {
        return getTableHeaderRow2().getSouthHeaderRow();
    }
    
    BooleanProperty getTableMenuButtonVisibleProperty() {
        return tableView.tableMenuButtonVisibleProperty();
    }

    /** {@inheritDoc} */
    @Override public void scrollHorizontally(){
        super.scrollHorizontally();
    }
    
    /** {@inheritDoc} */
    @Override protected void scrollHorizontally(TableColumn<S, ?> col) {
        if (col == null || !col.isVisible()) {
            return;
        }
        /**
         * We modified this function so that we ensure that any selected cells
         * will not be below a fixed column. Because when there's some fixed
         * columns, the "left border" is not the table anymore, but the right
         * side of the last fixed columns.
         *
         * Moreover, we need to re-compute the fixedColumnWidth because the
         * layout of the rows hasn't been done yet and the value is not right.
         * So we might end up below a fixedColumns.
         */
        
        fixedColumnWidth = 0;
        final double pos = getFlow().getHorizontalBar().getValue();
        int index = tableView.getVisibleLeafColumns().indexOf(col);
        double start = 0;// scrollX;

        for (int columnIndex = 0; columnIndex < index; ++columnIndex) {
            //Do not add the width of hidden column!
            if (tableView.getVisibleLeafColumns().get(columnIndex).isVisible()) {
                TableColumn<S, ?> column = (TableColumn<S, ?>) tableView.getVisibleLeafColumns().get(columnIndex);
                while (column.getParentColumn() != null) {
                    // on nested columns, we check if the root parent is the one fixed
                    column = (TableColumn<S, ?>) column.getParentColumn();
                }
                if (tableView.isColumnFixingEnabled() && tableView.getFixedColumns().contains(column)) {
                    fixedColumnWidth += column.getWidth();
                }
                start += column.getWidth();
            }
        }

        final double end = start + col.getWidth();

        // determine the visible width of the table
        final double headerWidth = tableView.getWidth() - snappedLeftInset() - snappedRightInset() - 
                (rowHeader != null ? rowHeader.getRowHeaderWidth() : 0d);

        // determine by how much we need to translate the table to ensure that
        // the start position of this column lines up with the left edge of the
        // tableview, and also that the columns don't become detached from the
        // right edge of the table
        final double max = getFlow().getHorizontalBar().getMax();
        double newPos;

        /**
         * If the starting position of our column if lower than the left egde
         * (of tableView or fixed columns), then we need to scroll.
         */
        if (start < pos + fixedColumnWidth && start >= 0 && start >= fixedColumnWidth) {
            newPos = start - fixedColumnWidth < 0 ? start : start - fixedColumnWidth;
            getFlow().getHorizontalBar().setValue(newPos);
        //If the starting point is not visible on the right.    
        } else if (start > pos + headerWidth) {
            final double delta = start < 0 || end > headerWidth ? start - pos - fixedColumnWidth : 0;
            newPos = Math.min(pos + delta, max);
            getFlow().getHorizontalBar().setValue(newPos);
        }
        /**
         * In all other cases, it means the cell is visible so no scroll needed,
         * because otherwise we may end up with a continous scroll that always
         * place the selected cell in the center of the screen.
         */
    }

//    /** {@inheritDoc} */
//    @Override protected void horizontalScroll() {
//        super.horizontalScroll();
//        getSouthHeader().updateScrollX();
//    }

    private void verticalScroll() {
        if (rowHeader != null) {
            rowHeader.requestLayout();
        }
    }

    final TableView2VirtualFlow<?> getFlow() {
        return (TableView2VirtualFlow<?>) flow;
    }

    /**
     * Return a BitSet of the rows that needs layout all the time. This
     * includes any row containing a span, or a fixed row.
     * @return 
     */
    private BitSet initRowToLayoutBitSet() {
        int rowCount = getItemCount();
        BitSet bitSet = new BitSet(rowCount);
        for (int row = 0; row < rowCount; ++row) {
            if (tableView.isRowFixingEnabled() && tableView.getFixedRows().contains(row)) {
                bitSet.set(row);
            }
        }
        return bitSet;
    }

    /**
     * When the vertical moves, we update the rowHeader
     */
    private final InvalidationListener vbarValueListener = (Observable o) -> verticalScroll();

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
    private final SetChangeListener<? super Integer> currentlyFixedRowListener = (SetChangeListener.Change<? extends Integer> arg0) -> {
        computeFixedRowHeight();
    };

    /**
     * We compute the total height of the fixedRows so that the selection can
     * use it without performance regression.
     */
    public final void computeFixedRowHeight() {
        fixedRowHeight = getCurrentlyFixedRow().stream()
                .mapToDouble(this::getRowHeight)
                .sum();
    }

    /**
     * We listen on the FixedColumns in order to do the modification in the
     * VirtualFlow.
     */
    private final ListChangeListener<TableColumn> fixedColumnsListener = new ListChangeListener<TableColumn>() {
        @Override
        public void onChanged(Change<? extends TableColumn> c) {
            hBarValue.clear();
            getFlow().requestLayout();
        }
    };

//    /** {@inheritDoc} */
//    @Override protected boolean resizeColumn(TableColumn<S, ?> tc, double delta) {
//        getTableHeaderRow2().getRootHeader().lastColumnResized = getColumns().indexOf(tc);
//        return getSkinnable().resizeColumn(tc, delta);
//    }

    public TableRow<S> createCell() {
        TableRow<S> cell;

        if (getSkinnable().getRowFactory() != null) {
            cell = getSkinnable().getRowFactory().call(getSkinnable());
        } else {
            cell = new TableRow<>();
        }

        cell.updateTableView(getSkinnable());
        return cell;
    }
    
    /** {@inheritDoc} */
    @Override public final int getItemCount() {
        return getSkinnable().getItems() == null ? 0 : getSkinnable().getItems().size();
    }

    /**
     * If the scene is not yet instantiated, we need to wait otherwise the
     * VirtualFlow will not shift the cells properly.
     *
     * @param value the horizontal value
     */
    public void setHbarValue(double value) {
        setHbarValue(value, 0);
    }

    public void setHbarValue(double value, int count) {
        if (count > 5) {
            return;
        }
        final int newCount = count + 1;
        if (flow.getScene() == null) {
            Platform.runLater(() -> {
                setHbarValue(value, newCount);
            });
            return;
        }
        getHBar().setValue(value);
    }
    
    private void updateHeaders() {
        final ObservableList<TablePosition> selectedCells = tableView.getSelectionModel().getSelectedCells();
        final List<Integer> columns = selectedCells.stream()
                .map(TablePosition::getColumn)
                .filter(column -> (column > -1 && tableView.getSelectionModel().isCellSelectionEnabled()))
                .collect(Collectors.toList());
        if (! oldSelectedColumns.equals(columns)) {
            oldSelectedColumns.clear();
            oldSelectedColumns.addAll(columns);
            getSelectedColumns().setAll(columns);
        }
        final List<Integer> rows = selectedCells.stream()
                .map(TablePosition::getRow)
                .collect(Collectors.toList());
        if (! oldSelectedRows.equals(rows)) {
            oldSelectedRows.clear();
            oldSelectedRows.addAll(rows);
            getSelectedRows().setAll(rows);
        }
    }
    
    /**
     * We try to make visible the rows that may be hidden by Fixed rows.
     *
     * @param newIndex
     * @param oldIndex 
     */
    private void scrollToVisibleCell(Number newIndex, Number oldIndex) {
        if (key && newIndex != null && oldIndex != null) {
            double posFinalOffset = 0, heightLastRow = 0;
            for (int j = getRow(0).getIndex(); j < newIndex.intValue(); ++j) {
                heightLastRow = getRowHeight(j);
                posFinalOffset += heightLastRow;
            }
            final double fixedHeight = getFixedRowHeight();
            if (fixedHeight > posFinalOffset) {
                flow.scrollTo(newIndex.intValue());
            } else if (fixedHeight > posFinalOffset - heightLastRow) {
                flow.scrollPixels(posFinalOffset - heightLastRow - fixedHeight);
            }
        }
    }

}
