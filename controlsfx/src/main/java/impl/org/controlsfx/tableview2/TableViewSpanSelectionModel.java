/**
 * Copyright (c) 2013, 2018 ControlsFX
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

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.SelectedCellsMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.util.Pair;
import org.controlsfx.control.tableview2.TableView2;

/**
 *
 * The Selection Model adapted for the TableView2 regarding span.
 * 
 * NOT IN USE
 */
public class TableViewSpanSelectionModel<S> extends TableView.TableViewSelectionModel<S> {

    private boolean shift = false; // Register state of 'shift' key
    private boolean key = false; // Register if we last touch the keyboard
    // or the mouse
    private boolean drag = false; // register if we are dragging (no
    // edition)
    private MouseEvent mouseEvent;
    private boolean makeAtomic;

    private TableView2<S> tableView;
    // the only 'proper' internal data structure, selectedItems and
    // selectedIndices
    // are both 'read-only and unbacked'.
    private final SelectedCellsMap<TablePosition<S, ?>> selectedCellsMap;

    // we create a ReadOnlyUnbackedObservableList of selectedCells here so
    // that we can fire custom list change events.
    private final ReadOnlyUnbackedObservableList<TablePosition<S, ?>> selectedCellsSeq;

    /**
     * We use these variable in order to stay on the same row/column when
     * navigating with arrows. If we are going down, and we are arriving on a
     * column-spanning cell, when going down again, we don't want to go on the
     * starting column of the spanning cell but on the same column we arrived
     * previously.
     */
//    private int oldCol = -1;
    private TableColumn<S, ?> oldTableColumn = null;
    private int oldRow = -1;
    public Pair<Integer, Integer> direction;
    private int oldColSpan = -1;
    private int oldRowSpan = -1;
    /**
     * Make the tableView move when selection operating outside bounds
     */
    private final Timeline timer;

    private final EventHandler<ActionEvent> timerEventHandler = (ActionEvent event) -> {
        TableView2Skin<S> skin = (TableView2Skin<S>) getTableView2Skin();
        //If my event is not within the grid, it means I have to scroll.
        if (mouseEvent != null && !tableView.contains(mouseEvent.getX(), mouseEvent.getY())) {
            //Pick up the current mouse location.
            double sceneX = mouseEvent.getSceneX();
            double sceneY = mouseEvent.getSceneY();
            //Pick up the Grid top-left location.
            double layoutX = tableView.getLocalToSceneTransform().getTx();
            double layoutY = tableView.getLocalToSceneTransform().getTy();
            //Pick up Grid bottom-right location.
            double layoutXMax = layoutX + tableView.getWidth();
            double layoutYMax = layoutY + tableView.getHeight();

            //If I go out of bounds, simply scroll.
            if (sceneX > layoutXMax) {
                skin.getHBar().increment();
            } else if (sceneX < layoutX) {
                skin.getHBar().decrement();
            }
            //Note the separate "if" here in order 
            //to scroll in both direction if necessary
            if (sceneY > layoutYMax) {
                skin.getVBar().increment();
            } else if (sceneY < layoutY) {
                skin.getVBar().decrement();
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
            tableView.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
        }
    };

    private final EventHandler<KeyEvent> keyPressedEventHandler = (KeyEvent keyEvent) -> {
        key = true;
        shift = keyEvent.isShiftDown();
    };

    private final EventHandler<MouseEvent> mousePressedEventHandler = (MouseEvent mouseEvent1) -> {
        key = false;
        shift = mouseEvent1.isShiftDown();
    };

    private final EventHandler<MouseEvent> onDragDetectedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            tableView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
            drag = true;
            timer.setCycleCount(Timeline.INDEFINITE);
            timer.play();
        }
    };

    private final EventHandler<MouseEvent> onMouseDragEventHandler = (MouseEvent e) -> {
        mouseEvent = e;
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
     *
     * @param tableView the {@link TableView2}
     */
    public TableViewSpanSelectionModel(@NamedArg("tableView") TableView2<S> tableView) {
        super(tableView);
        this.tableView = tableView;

        timer = new Timeline(new KeyFrame(Duration.millis(100), new WeakEventHandler<>((timerEventHandler))));
        tableView.addEventHandler(KeyEvent.KEY_PRESSED, new WeakEventHandler<>(keyPressedEventHandler));

        tableView.addEventFilter(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(mousePressedEventHandler));
        tableView.setOnDragDetected(new WeakEventHandler<>(onDragDetectedEventHandler));

        tableView.setOnMouseDragged(new WeakEventHandler<>(onMouseDragEventHandler));

        selectedCellsMap = new SelectedCellsMap<TablePosition<S,?>>(c -> handleSelectedCellsListChangeEvent(c)) {
            
                @Override public boolean isCellSelectionEnabled() {
                    return tableView.getSelectionModel().isCellSelectionEnabled();
                }
            };
        
        selectedCellsSeq = new ReadOnlyUnbackedObservableList<TablePosition<S, ?>>() {
            @Override
            public TablePosition<S, ?> get(int i) {
                return selectedCellsMap.get(i);
            }

            @Override
            public int size() {
                return selectedCellsMap.size();
            }
        };
    }

    private void handleSelectedCellsListChangeEvent(ListChangeListener.Change<? extends TablePosition<S, ?>> c) {
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
    private TablePosition<S, ?> old = null;

    @Override
    public void select(int row, TableColumn<S, ?> column) {
        if (row < 0 || row >= getItemCount()) {
            return;
        }
        
        if (! isCellSelectionEnabled()) {
            select(row);
            return;
        }

        // if I'm in cell selection mode but the column is null, I don't
        // want
        // to select the whole row instead...
        if (isCellSelectionEnabled() && column == null) {
            return;
        } 
        // Variable we need for algorithm
        TablePosition<S, ?> posFinal = new TablePosition<>(getTableView(), row, column);

        int columnIndex = tableView.getColumns().indexOf(posFinal.getTableColumn());
        final TableView2.SpanType spanType = tableView.getSpanType(row, columnIndex);

        /**
         * We check if we are on covered cell. If so we have the algorithm of
         * the focus model to give the selection to the right cell.
         *
         */
        switch (spanType) {
            case ROW_SPAN_INVISIBLE:
                /**
                 * If we notice that the new selected cell is the previous one,
                 * then it means that we were already on the cell and we wanted
                 * to go below. We make sure that old is not null, and that the
                 * move is initiated by keyboard. Because if it's a click, then
                 * we just want to go on the clicked cell (not below)
                 */
                if (old != null && !shift && old.getColumn() == posFinal.getColumn()
                        && old.getRow() == posFinal.getRow() - 1) {
                    int visibleRow = FocusModelListener.getNextRowNumber(old, tableView);
                    /**
                     * If the visibleRow we're targeting is out of bounds, we do
                     * not want to get a visibleCell, so we step out. But we
                     * need to set edition to false because we will be going
                     * back to the old cell and we could go to edition.
                     */
                    if (visibleRow < getItemCount()) {
                        posFinal = getVisibleCell(visibleRow, oldColSpan > 1 ? oldTableColumn : old.getTableColumn());
                        break;
                    }
                }
                // If the current selected cell if hidden by row span, we go
                // above
                posFinal = getVisibleCell(row, oldColSpan > 1 ? oldTableColumn : column);
                break;
            case BOTH_INVISIBLE:
                // If the current selected cell if hidden by a both (row and
                // column) span, we go left-above
                posFinal = getVisibleCell(row, column);
                break;
            case COLUMN_SPAN_INVISIBLE:
                // If we notice that the new selected cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go right.
                if (old != null && !shift && old.getColumn() == posFinal.getColumn() - 1
                        && old.getRow() == posFinal.getRow()) {
                    posFinal = getVisibleCell(oldRowSpan > 1 ? oldRow : old.getRow(), (TableColumn<S, ?>) FocusModelListener.getTableColumnSpan(old, tableView));
                } else {
                    // If the current selected cell if hidden by column span, we
                    // go left
                    posFinal = getVisibleCell(row, column);
                }
            default:
                if (direction != null && key) {
                    /**
                     * If I'm going up or down, and the previous cell had a
                     * column span, then we take the column used before instead
                     * of the current column.
                     */
                    if (direction.getKey() != 0 && oldColSpan > 1) {
                        //FIXME
                        posFinal = getVisibleCell(posFinal.getRow(), oldTableColumn);
                    } else if (direction.getValue() != 0 && oldRowSpan > 1) {
                        posFinal = getVisibleCell(oldRow, posFinal.getTableColumn());
                    }
                }
                break;
        }

        old = posFinal;

        //If it's a click, we register everything.
        if (!key) {
            oldRow = old.getRow();
//            oldCol = old.getColumn();
            oldTableColumn = old.getTableColumn();
        } else //If we're going up or down, we register the row changing, not the column.
         if (direction != null && direction.getKey() != 0) {
                oldRow = old.getRow();
            } else if (direction != null && direction.getValue() != 0) {
//            oldCol = old.getColumn();
                oldTableColumn = old.getTableColumn();
            }
        if (getSelectionMode() == SelectionMode.SINGLE) {
            quietClearSelection();
        }
        oldRowSpan = tableView.getRowSpan(old, old.getRow());
        oldColSpan = tableView.getColumnSpan(old);
        for (int i = old.getRow(); i < oldRowSpan + old.getRow(); ++i) {
            for (int j = posFinal.getColumn(); j < oldColSpan + posFinal.getColumn(); ++j) {
                posFinal = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
                selectedCellsMap.add(posFinal);
            }
        }

        updateScroll(old);
        addSelectedRowsAndColumns(old);

        setSelectedIndex(old.getRow());
        setSelectedItem(getModelItem(old.getRow()));
        if (getTableView().getFocusModel() == null) {
            return;
        }

        getTableView().getFocusModel().focus(old.getRow(), old.getTableColumn());
    }

    /**
     * We try to make visible the rows that may be hidden by Fixed rows.
     *
     * @param posFinal
     */
    private void updateScroll(TablePosition<S, ?> posFinal) {

        /**
         * We don't want to do any scroll when dragging or selecting with click.
         * Only keyboard action arrow action.
         */
        if (!drag && key && getTableView2Skin().getCellsSize() != 0 && ! tableView.getFixedRows().isEmpty()) {

            int start = getTableView2Skin().getRow(0).getIndex();
            double posFinalOffset = 0;
            for (int j = start; j < posFinal.getRow(); ++j) {
                posFinalOffset += getTableView2Skin().getRowHeight(j);
            }

            if (getTableView2Skin().getFixedRowHeight() > posFinalOffset) {
                tableView.scrollTo(posFinal.getRow());
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clearSelection(int row, TableColumn<S, ?> column) {

        final TablePosition<S, ?> tp = new TablePosition<>(getTableView(), row, column);
        if (tp.getRow() < 0 || tp.getColumn() < 0) {
            return;
        }
        List<TablePosition<S, ?>> selectedCells;
        if ((selectedCells = isSelectedRange(row, column, tp.getColumn())) != null) {
            for (TablePosition<S, ?> cell : selectedCells) {
                selectedCellsMap.remove(cell);
                removeSelectedRowsAndColumns(cell);
                focus(cell.getRow());
            }
        } else {
            for (TablePosition<S, ?> pos : getSelectedCells()) {
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

    /**
     * When we set a new grid, we need to update the selected Cells because
     * otherwise we will end up with TablePosition which have "-1" as their
     * column number. So we need to verify that the old selected cells are still
     * selectable and select them.
     *
     * @param selectedCells a list of {@link Pair} of selected cells
     */
    public void verifySelectedCells(List<Pair<Integer, Integer>> selectedCells) {
        List<TablePosition<S, ?>> newList = new ArrayList<>();
        clearSelection();

        final int itemCount = getItemCount();
        final int columnSize = getTableView().getVisibleLeafColumns().size();
        final HashSet<Integer> selectedRows = new HashSet<>();
        final HashSet<Integer> selectedColumns = new HashSet<>();
        TablePosition<S, ?> pos = null;
        for (Pair<Integer, Integer> position : selectedCells) {
            if (position.getKey() < 0
                    || position.getKey() >= itemCount
                    || position.getValue() < 0
                    || position.getValue() >= columnSize) {
                continue;
            }

            final TableColumn<S, ?> column = getTableView().getVisibleLeafColumn(position.getValue());

            pos = getVisibleCell(position.getKey(), column);
            // We store all the selectedColumn and Rows, we will update
            // just once at the end
            if (pos != null) {
                final int rowSpan = tableView.getRowSpan(pos, pos.getRow());
                final int currentRow = pos.getRow();
                for (int i = pos.getRow(); i < rowSpan + currentRow; ++i) {
                    selectedColumns.add(i);
                    for (int j = pos.getColumn(); j < tableView.getColumnSpan(pos) + pos.getColumn(); ++j) {
                        selectedRows.add(j);
                        pos = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
                        newList.add(pos);
                    }
                }
            }
        }
        selectedCellsMap.setAll(newList);

        final TablePosition finalPos = pos;
        // Then we update visuals just once
        TableView2Skin skin = getTableView2Skin();
        //If the skin is null, we just wait till everything is ready..
        if (skin == null) {
            tableView.skinProperty().addListener(new InvalidationListener() {

                @Override
                public void invalidated(Observable observable) {
                    tableView.skinProperty().removeListener(this);
                    TableView2Skin skin = getTableView2Skin();
                    if (skin != null) {
                        updateSelectedVisuals(skin, finalPos, selectedRows, selectedColumns);
                    }
                }
            });
        } else {
            updateSelectedVisuals(skin, pos, selectedRows, selectedColumns);
        }
    }

    /**
     * When all the selection has been made, we just need to light up the
     * indicators that are showing which indexes are selected.
     *
     * @param skin
     * @param pos
     * @param selectedRows
     * @param selectedColumns
     */
    private void updateSelectedVisuals(TableView2Skin skin, TablePosition pos, HashSet<Integer> selectedRows, HashSet<Integer> selectedColumns) {
        if (skin != null) {
            skin.getSelectedRows().addAll(selectedColumns);
            skin.getSelectedColumns().addAll(selectedRows);
        }

        /**
         * If we made some selection, we need to force the visual selected
         * confirmation to come when the layout is starting. Doing it before
         * will result in a selected cell with no css applied to it.
         */
        if (pos != null) {
            getTableView2Skin().lastRowLayout.set(true);
            getTableView2Skin().lastRowLayout.addListener(new InvalidationListener() {

                @Override
                public void invalidated(Observable observable) {
                    handleSelectedCellsListChangeEvent(new NonIterableChange.SimpleAddChange<>(0,
                            selectedCellsMap.size(), selectedCellsSeq));
                    getTableView2Skin().lastRowLayout.removeListener(this);
                }
            });
        }
    }

    @Override
    public void select(int row) {
        super.select(row); 
        getTableView2Skin().getSelectedRows().add(row);
        tableView.refresh(); // this shouldn't be necessary, but it helps setting the proper focused css
    }

    @Override
    public void selectRange(int minRow, TableColumnBase<S, ?> minColumn, int maxRow,
            TableColumnBase<S, ?> maxColumn) {

        if (isCellSelectionEnabled() && getSelectionMode() == SelectionMode.SINGLE) {
            quietClearSelection();
            select(maxRow, maxColumn);
            return;
        }

        makeAtomic = true;

        final int itemCount = getItemCount();

        final int minColumnIndex = getTableView().getVisibleLeafIndex((TableColumn<S, ?>) minColumn);
        final int maxColumnIndex = getTableView().getVisibleLeafIndex((TableColumn<S, ?>) maxColumn);
        final int _minColumnIndex = Math.min(minColumnIndex, maxColumnIndex);
        final int _maxColumnIndex = Math.max(minColumnIndex, maxColumnIndex);

        final int _minRow = Math.min(minRow, maxRow);
        final int _maxRow = Math.max(minRow, maxRow);

        HashSet<Integer> selectedRows = new HashSet<>();
        HashSet<Integer> selectedColumns = new HashSet<>();

        for (int _row = _minRow; _row <= _maxRow; _row++) {
            for (int _col = _minColumnIndex; _col <= _maxColumnIndex; _col++) {
                // begin copy/paste of select(int, column) method (with some
                // slight modifications)
                if (_row < 0 || _row >= itemCount) {
                    continue;
                }

                final TableColumn<S, ?> column = getTableView().getVisibleLeafColumn(_col);

                // if I'm in cell selection mode but the column is null, I
                // don't want
                // to select the whole row instead...
                if (column == null) {
                    continue;
                }

                TablePosition<S, ?> pos = getVisibleCell(_row, column);

                // We store all the selectedColumn and Rows, we will update
                // just once at the end
                final int rowSpan = tableView.getRowSpan(pos, pos.getRow());
                final int currentRow = pos.getRow();
                for (int i = currentRow; i < rowSpan + currentRow; ++i) {
                    selectedRows.add(i);
                    for (int j = pos.getColumn(); j < tableView.getColumnSpan(pos) + pos.getColumn(); ++j) {
                        selectedColumns.add(j);
                        pos = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
                        selectedCellsMap.add(pos);
                    }
                }

//                makeAtomic = true;
                // end copy/paste
            }
        }
        makeAtomic = false;

        // Then we update visuals just once
        getTableView2Skin().getSelectedRows().addAll(selectedRows);
        getTableView2Skin().getSelectedColumns().addAll(selectedColumns);

        // fire off events
        setSelectedIndex(maxRow);
        setSelectedItem(getModelItem(maxRow));
        if (getTableView().getFocusModel() == null) {
            return;
        }

        //FIXME Focus is wrong, and endIndex also..
        getTableView().getFocusModel().focus(maxRow, (TableColumn<S, ?>) maxColumn);

        /**
         * If we end up on a spanned cell, there is not reliable way to
         * determine which is the last index, certainly not the maxRow and
         * maxColumn. So right now we need to take this extreme measure in order
         * to be sure that the cells will be highlighted correctly.
         */
        final int startChangeIndex = selectedCellsMap.indexOf(new TablePosition<>(getTableView(), minRow,
                (TableColumn<S, ?>) minColumn));
        final int endChangeIndex = selectedCellsMap.getSelectedCells().size() - 1;//indexOf(new TablePosition<>(getTableView(), maxRow,
//                (TableColumn<S, ?>) maxColumn));

        if (startChangeIndex > -1 && endChangeIndex > -1) {
            final int startIndex = Math.min(startChangeIndex, endChangeIndex);
            final int endIndex = Math.max(startChangeIndex, endChangeIndex);
            handleSelectedCellsListChangeEvent(new NonIterableChange.SimpleAddChange<>(startIndex,
                    endIndex + 1, selectedCellsSeq));
        }
    }

    @Override
    public void selectAll() {
        if (getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }

        quietClearSelection();

        List<TablePosition<S, ?>> indices = new ArrayList<>();
        TableColumn<S, ?> column;
        TablePosition<S, ?> tp = null;

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
        for (int col = 0; col < tableView.getColumns().size(); col++) {
            selectedColumns.add(col);
        }

        ArrayList<Integer> selectedRows = new ArrayList<>();
        for (int row = 0; row < getItemCount(); row++) {
            selectedRows.add(row);
        }
        getTableView2Skin().getSelectedRows().addAll(selectedRows);
        getTableView2Skin().getSelectedColumns().addAll(selectedColumns);

        if (tp != null) {
            select(tp.getRow(), tp.getTableColumn());
            //Just like rowHeader, the focus should be put on the 
            //first cell to ease copy/paste operation.
            getTableView().getFocusModel().focus(0, getTableView().getColumns().get(0));
        }
    }

    @Override
    public boolean isSelected(int row, TableColumn<S, ?> column) {
        // When in cell selection mode, we currently do NOT support
        // selecting
        // entire rows, so a isSelected(row, null)
        // should always return false.
        if (column == null || row < 0) {
            return false;
        }

        int columnIndex = getTableView().getVisibleLeafIndex(column);

        if (getTableView2Skin().getCellsSize() != 0) {
            TablePosition<S, ?> posFinal = getVisibleCell(row, column);
            return selectedCellsMap.isSelected(posFinal.getRow(), posFinal.getColumn());
        } else {
            return selectedCellsMap.isSelected(row, columnIndex);
        }
    }

    /**
     * Returns the tablePosition of a selected cell inside a spanned cell if any.
     *
     * @param row the number of row
     * @param column the {@link TableColumn}
     * @param col the number of column
     * @return a list of table positions
     */
    public List<TablePosition<S, ?>> isSelectedRange(int row, TableColumn<S, ?> column, int col) {

        if (col < 0 || row < 0) {
            return null;
        }

        final TablePosition<S, ?> pos = new TablePosition<>(tableView, row, column);

        final int infRow = row;
        final int supRow = infRow + tableView.getRowSpan(pos, row);

        final int infCol = ((TableView2Skin<S>) tableView.getSkin()).getViewColumn(col);
        final int supCol = infCol + tableView.getColumnSpan(pos);
        List<TablePosition<S, ?>> selectedCells = new ArrayList<>();
        for (final TablePosition<S, ?> tp : getSelectedCells()) {
            if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol && tp.getColumn() < supCol) {
                selectedCells.add(tp);
            }
        }
        return selectedCells.isEmpty() ? null : selectedCells;
    }

    /**
     * *********************************************************************
     * * Support code * *
     * ********************************************************************
     */
    private void addSelectedRowsAndColumns(TablePosition<S, ?> pos) {
        TableView2Skin<S> skin = getTableView2Skin();
        if (skin == null) {
            return;
        }
        final int rowSpan = tableView.getRowSpan(pos, pos.getRow());
//        final int filteredRow = tableView.getFilteredRow(cell.getRow());
        for (int i = pos.getRow(); i < rowSpan + pos.getRow(); ++i) {
            skin.getSelectedRows().add(i);
            for (int j = pos.getColumn();  j < tableView.getColumnSpan(pos) + pos.getColumn(); ++j) {
                skin.getSelectedColumns().add(j);
            }
        }
    }

    private void removeSelectedRowsAndColumns(TablePosition<S, ?> pos) {
        final int rowSpan = tableView.getRowSpan(pos, pos.getRow());
//        final int filteredRow = tableView.getFilteredRow(cell.getRow());
        for (int i = pos.getRow(); i < rowSpan + pos.getRow(); ++i) {
            getTableView2Skin().getSelectedRows().remove(Integer.valueOf(i));
            for (int j = pos.getColumn(); j < tableView.getColumnSpan(pos) + pos.getColumn(); ++j) {
                getTableView2Skin().getSelectedColumns().remove(Integer.valueOf(j));
            }
        }
    }

    @Override
    public void clearAndSelect(int row, TableColumn<S, ?> column) {
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
        List<TablePosition<S, ?>> previousSelection = new ArrayList<>(selectedCellsMap.getSelectedCells());

        // then clear the current selection
        clearSelection();

        // and select the new row
        select(row, column);

        makeAtomic = false;

        // fire off a single add/remove/replace notification (rather than
        // individual remove and add notifications) - see RT-33324
        if (old != null && old.getColumn() >= 0) {
            TableColumn<S, ?> columnFinal = getTableView().getVisibleLeafColumn(old.getColumn());
            int changeIndex = selectedCellsSeq.indexOf(new TablePosition<>(getTableView(), old.getRow(),
                    columnFinal));
            NonIterableChange.GenericAddRemoveChange<TablePosition<S, ?>> change = 
                    new NonIterableChange.GenericAddRemoveChange<>(changeIndex, 
                            changeIndex + 1, previousSelection, selectedCellsSeq);
            handleSelectedCellsListChangeEvent(change);
        }
    }

    /**
     * FIXME I don't understand why TablePosition is not parameterized in the
     * API..
     *
     * @return an ObservableList of table positions
     */
    @Override
    public ObservableList<TablePosition> getSelectedCells() {
        return (ObservableList<TablePosition>) (Object) selectedCellsSeq;
    }

    @Override
    public void selectAboveCell() {
        final TablePosition<S, ?> pos = getFocusedCell();
        if (pos.getRow() == -1) {
            select(getItemCount() - 1);
        } else if (pos.getRow() > 0) {
            select(pos.getRow() - 1, pos.getTableColumn());
        }
    }

    @Override
    public void selectBelowCell() {
        final TablePosition<S, ?> pos = getFocusedCell();

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

        final TablePosition<S, ?> pos = getFocusedCell();
        if (pos.getColumn() - 1 >= 0) {
            select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
        }

    }

    @Override
    public void selectRightCell() {
        if (!isCellSelectionEnabled()) {
            return;
        }

        final TablePosition<S, ?> pos = getFocusedCell();
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
        TableView2Skin skin = getTableView2Skin();
        if (skin != null) {
            skin.getSelectedRows().clear();
            skin.getSelectedColumns().clear();
        }
    }

    @SuppressWarnings("unchecked")
    private TablePosition<S, ?> getFocusedCell() {
        if (getTableView().getFocusModel() == null) {
            return new TablePosition<>(getTableView(), -1, null);
        }
        return (TablePosition<S, ?>) tableView.getFocusModel().getFocusedCell();
    }

    private TableColumn<S, ?> getTableColumn(TableColumn<S, ?> column, int offset) {
        final int columnIndex = getTableView().getVisibleLeafIndex(column);
        final int newColumnIndex = columnIndex + offset;
        return getTableView().getVisibleLeafColumn(newColumnIndex);
    }

    /**
     * For a position, return the Visible Cell associated with It can be the top
     * of the span cell if it's visible, or it can be the first row visible if
     * we have scrolled
     *
     * @param row
     * @param column
     * @param viewCol
     * @return
     */
    private TablePosition<S, ?> getVisibleCell(int row, TableColumn<S, ?> column) {
        int modelColumn = tableView.getColumns().indexOf(column);
        final TableView2.SpanType spanType = tableView.getSpanType(row, modelColumn);
        final TablePosition<S, ?> tablePosition = new TablePosition<>(tableView, row, column);
        switch (spanType) {
            case NORMAL_CELL:
            case ROW_VISIBLE:
                return tablePosition;
            case BOTH_INVISIBLE:
            case COLUMN_SPAN_INVISIBLE:
            case ROW_SPAN_INVISIBLE:
            default:
                int firstRow = getTableView2Skin() == null ? -1 : getTableView2Skin().getFirstRow(tablePosition, row);
                int viewColumn = ((TableView2Skin<S>) tableView.getSkin()).getViewColumn(modelColumn);
                if (getTableView2Skin() == null || (getTableView2Skin().getCellsSize() != 0 && getNonFixedRow(0).getIndex() <= firstRow)) {
                    return new TablePosition<>(tableView, firstRow,
                            tableView.getVisibleLeafColumn(viewColumn));
                } else { // If it's not, then it's the firstkey
                    TableRow gridRow = getNonFixedRow(0);
                    return new TablePosition<>(tableView, gridRow == null ? row : gridRow.getIndex(),
                            tableView.getVisibleLeafColumn(viewColumn));
        }
    }
    }

    /**
     * @return the inner table view skin
     */
    final TableView2Skin<S> getTableView2Skin() {
        return (TableView2Skin<S>) tableView.getSkin();
    }

    /**
     * Return the {@link TableRow2} at the specified index
     *
     * @param index
     * @return
     */
    private TableRow<S> getNonFixedRow(int index) {
        return getTableView2Skin().getRow(index);
    }
    
}
