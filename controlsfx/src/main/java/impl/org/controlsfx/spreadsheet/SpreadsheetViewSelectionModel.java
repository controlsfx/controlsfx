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
package impl.org.controlsfx.spreadsheet;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.SelectedCellsMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * The Selection Model adapted for the SpreadsheetView regarding span.
 */
public class SpreadsheetViewSelectionModel extends
        TableView.TableViewSelectionModel<ObservableList<SpreadsheetCell>> {

    private boolean ctrl = false; // Register state of 'ctrl' key
    private boolean shift = false; // Register state of 'shift' key
    private boolean key = false; // Register if we last touch the keyboard
    // or the mouse
    private boolean drag = false; // register if we are dragging (no
    // edition)
    private MouseEvent mouseEvent;
    private boolean makeAtomic;
    private SpreadsheetGridView cellsView;

    private SpreadsheetView spreadsheetView;
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

                if (sceneX > layoutXMax) {
                    skin.getHBar().increment();
                } else if (sceneX < layoutX) {
                    skin.getHBar().decrement();
                }
                if (sceneY > layoutYMax) {
                    skin.getVBar().increment();
                } else if (sceneY < layoutY) {
                    skin.getVBar().decrement();
                }
            }
        }
    };
    /**
     * When the drag is over, we remove the listener and stop the timer
     */
    private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            drag = false;
            timer.stop();
            spreadsheetView.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
        }
    };

    private final EventHandler<KeyEvent> keyPressedEventHandler = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent keyEvent) {
            key = true;
            ctrl = keyEvent.isControlDown();
            shift = keyEvent.isShiftDown();
        }
    };
    private final EventHandler<MouseEvent> mousePressedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            key = false;
            ctrl = mouseEvent.isControlDown();
            shift = mouseEvent.isShiftDown();
        }
    };

    private final EventHandler<MouseEvent> onDragDetectedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
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

    /**
     * *********************************************************************
     *
     * Constructors
     *
     *********************************************************************
     */
    /**
     * Constructor
     * @param spreadsheetView
     * @param cellsView 
     */
    public SpreadsheetViewSelectionModel(SpreadsheetView spreadsheetView, SpreadsheetGridView cellsView) {
        super(cellsView);
        this.cellsView = cellsView;
        this.spreadsheetView = spreadsheetView;

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

        final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(row, posFinal.getColumn());

        /**
         * We check if we are on covered cell. If so we have the algorithm of
         * the focus model to give the selection to the right cell.
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
                    posFinal = getVisibleCell(FocusModelListener.getTableRowSpan(old, cellsView), old.getTableColumn(), old.getColumn());
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
                    posFinal = getVisibleCell(old.getRow(), FocusModelListener.getTableColumnSpan(old, cellsView), getTableColumnSpanInt(old));
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
         * We don't want to do any scroll when dragging or selecting with click.
         * Only keyboard action arrow action.
         */
        if (!drag && key && getCellsViewSkin().getCellsSize() != 0 && spreadsheetView.getAxes().getFixedRows().size() != 0) {

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
        TablePosition<ObservableList<SpreadsheetCell>, ?> position;
        if ((position = isSelectedRange(row, column, tp.getColumn())) != null) {
            selectedCellsMap.remove(position);
            removeSelectedRowsAndColumns(position);
            focus(position.getRow());
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
                if (_row < 0 || _row >= itemCount) {
                    continue;
                }

                final TableColumn<ObservableList<SpreadsheetCell>, ?> column = getTableView().getVisibleLeafColumn(
                        _col);

                // if I'm in cell selection mode but the column is null, I
                // don't want
                // to select the whole row instead...
                if (column == null) {
                    continue;
                }

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
        if (getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

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
        for (int col = 0; col < spreadsheetView.getGrid().getColumnCount(); col++) {
            selectedColumns.add(col);
        }

        ArrayList<Integer> selectedRows = new ArrayList<>();
        for (int row = 0; row < spreadsheetView.getGrid().getRowCount(); row++) {
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
     * Return the tablePosition of a selected cell inside a spanned cell if any.
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
    private void addSelectedRowsAndColumns(TablePosition<?, ?> position) {
        final SpreadsheetCell cell = cellsView.getItems().get(position.getRow()).get(position.getColumn());
        for (int i = cell.getRow(); i < cell.getRowSpan() + cell.getRow(); ++i) {
            getSpreadsheetViewSkin().getSelectedRows().add(i);
            for (int j = cell.getColumn(); j < cell.getColumnSpan() + cell.getColumn(); ++j) {
                getSpreadsheetViewSkin().getSelectedColumns().add(j);
            }
        }
    }

    private void removeSelectedRowsAndColumns(TablePosition<?, ?> position) {
        final SpreadsheetCell cell = cellsView.getItems().get(position.getRow()).get(position.getColumn());
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
            NonIterableChange.GenericAddRemoveChange<TablePosition<ObservableList<SpreadsheetCell>, ?>> change = new NonIterableChange.GenericAddRemoveChange<>(
                    changeIndex, changeIndex + 1, previousSelection, selectedCellsSeq);
            handleSelectedCellsListChangeEvent(change);
        }
    }

    /**
     * FIXME I don't understand why TablePosition is not parameterized in the
     * API..
     * @return 
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
        final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(row, col);
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
     * @return the inner table view skin
     */
    final GridViewSkin getCellsViewSkin() {
        return (GridViewSkin) (cellsView.getSkin());
    }

    /**
     * Return the {@link GridRow} at the specified index
     *
     * @param index
     * @return
     */
    private GridRow getNonFixedRow(int index) {
        return getCellsViewSkin().getRow(index);
    }

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param t the current TablePosition
     * @return
     */
    private int getTableColumnSpanInt(final TablePosition<?, ?> t) {
        return t.getColumn() + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan();
    }

}
