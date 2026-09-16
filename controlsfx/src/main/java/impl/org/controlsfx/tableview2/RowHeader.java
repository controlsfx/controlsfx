/**
 * Copyright (c) 2013, 2026, ControlsFX
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


import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.FilteredTableView;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static impl.org.controlsfx.tableview2.SortUtils.SortEndedEvent.SORT_ENDED_EVENT;
import static impl.org.controlsfx.tableview2.SortUtils.SortStartedEvent.SORT_STARTED_EVENT;

/**
 * Display the row header on the left of the cells (view), where the user can
 * display any content via {@link TableView2#getRowHeader() }.
 *
 * @param <S> The type of the objects contained within the TableView2 items list.
 */
public class RowHeader<S> extends StackPane {

    /**
     * *************************************************************************
     * * Private Fields * *
     * ************************************************************************
     */
    private final TableView2<S> tableView;
    private TableView2Skin<S> skin;
    private TableView2Skin<S> innerSkin;
    private double tableColumnHeaderHeight;

    private final TableView2<S> innerTableView;

    /**
     * This represents the RowHeader width. It's the total amount of space
     * used by the RowHeader {@link TableView2#getRowHeaderWidth() }.
     *
     */
    private final DoubleProperty innerRowHeaderWidth = new SimpleDoubleProperty();
    private Rectangle clip; // Ensure that children do not go out of bounds

    // used for column resizing

    private ListChangeListener<Integer> tableSelectionListener;
    private ListChangeListener<Integer> rowHeaderSelectionListener;

    private boolean sorting;

    /**
     * ****************************************************************
     * CONSTRUCTOR
     *
     * @param tableView
     * ***************************************************************
     */
    public RowHeader(TableView2<S> tableView) {
        this.tableView = tableView;
        getStyleClass().add("row-header"); //$NON-NLS-1$
        if (tableView instanceof FilteredTableView) {
            innerTableView = new FilteredTableView<>();
        } else {
            innerTableView = new TableView2<>();
        }
        innerTableView.setColumnFixingEnabled(false);
        innerTableView.setRowHeaderVisible(false);
        innerTableView.setEditable(false);
        innerTableView.setPlaceholder(new Label());

        // TODO: Enable sorting the RowHeader when a sorting criterium is
        // defined for the tableView
        innerTableView.setSortPolicy(t -> false);
    }

    /**
     * *************************************************************************
     * * Private/Protected Methods *
     * ***********************************************************************
     */
    /**
     * Init
     *
     * @param skin
     * @param tableColumnHeader
     */
    void init(final TableView2Skin<S> skin, TableHeaderRow2 tableColumnHeader) {
        this.skin = skin;

        // Adjust position upon TableHeaderRow2 height
        tableColumnHeader.heightProperty().addListener((obs, oldHeight, newHeight) -> {
            tableColumnHeaderHeight = newHeight.doubleValue();
            requestLayout();
        });

        // Clip property to stay within bounds
        clip = new Rectangle(getRowHeaderWidth(),
                snapSizeY(tableView.getHeight() - tableView.snappedTopInset() - tableView.snappedBottomInset()));
        clip.relocate(snappedTopInset(), snappedLeftInset());
        clip.setSmooth(false);
        clip.heightProperty().bind(Bindings.createDoubleBinding(() ->
                tableView.getHeight() - tableView.snappedTopInset() - tableView.snappedBottomInset(),
                tableView.heightProperty()));
        clip.widthProperty().bind(innerRowHeaderWidth);
        RowHeader.this.setClip(clip);

        // We desactivate and activate the rowHeader upon request
        tableView.rowHeaderVisibleProperty().addListener(layout);
        tableView.getFixedRows().addListener(layout);
        tableView.rowHeaderWidthProperty().addListener(layout);
        tableView.heightProperty().addListener(layout);
        skin.getHBar().visibleProperty().addListener(layout);

        // add refresh listeners
        tableView.fixedCellSizeProperty().addListener((Observable o) -> {
            innerTableView.refresh();
            innerTableView.requestLayout();
        });
        tableView.rowFixingEnabledProperty().addListener((Observable o) -> {
            innerTableView.setRowFixingEnabled(tableView.isRowFixingEnabled());
            innerTableView.refresh();
            innerTableView.requestLayout();
        });

        // install tableColumn
        tableView.getVisibleLeafColumns().addListener((Observable o) -> {
            if (tableView.getVisibleLeafColumns().isEmpty() != innerTableView.getColumns().isEmpty()) {
                setContent();
            }
        });
        tableView.rowHeaderProperty().addListener((Observable o) -> setContent());
        setContent();

        // sync items between tableViews
        innerTableView.itemsProperty().bind(tableView.itemsProperty());

        // sync fixed rows
        innerTableView.getFixedRows().setAll(tableView.getFixedRows().stream().collect(Collectors.toList()));
        tableView.getFixedRows().addListener((Observable o) -> {
            innerTableView.getFixedRows().setAll(tableView.getFixedRows().stream().collect(Collectors.toList()));
        });

        // sync scrolling between tableViews
        innerTableView.skinProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                innerSkin = (TableView2Skin<S>) innerTableView.getSkin();
                setScrollbars();
                innerTableView.skinProperty().removeListener(this);
            }
        });

        // sync selection between two selection models
        innerTableView.getSelectionModel().selectionModeProperty().bind(tableView.getSelectionModel().selectionModeProperty());
        rowHeaderSelectionListener = (ListChangeListener.Change<? extends Integer> c) -> {
            skin.getSelectedRows().removeListener(tableSelectionListener);
            applyListChange(tableView, c);
            skin.getSelectedRows().addListener(tableSelectionListener);
        };
        tableSelectionListener = (ListChangeListener.Change<? extends Integer> c) -> {
            innerTableView.getSelectionModel().getSelectedIndices().removeListener(rowHeaderSelectionListener);
            applyListChange(innerTableView, c);
            if (! sorting) {
                innerTableView.getSelectionModel().getSelectedIndices().addListener(rowHeaderSelectionListener);
            }
        };

        final ChangeListener<Boolean> focusListener = (obs, ov, nv) -> {
            if (! tableView.isFocused() && ! innerTableView.isFocused()) {
                tableView.setStyle("-fx-selection-bar-non-focused: lightgrey;");
                innerTableView.setStyle("-fx-selection-bar-non-focused: lightgrey;");
            } else {
                innerTableView.setStyle("-fx-selection-bar-non-focused: -fx-accent;");
                tableView.setStyle("-fx-selection-bar-non-focused: -fx-accent;");
            }
        };
        innerTableView.getSelectionModel().getSelectedIndices().addListener(rowHeaderSelectionListener);
        skin.getSelectedRows().addListener(tableSelectionListener);

        // When sorting, the external TableView fires add/remove selection events.
        // These change the innerTableView selected rows. To avoid firing new
        // events back to the TableView, we have to remove rowHeaderSelectionListener
        // while sorting.
        tableView.addEventHandler(SortEvent.ANY, e -> {
            if (e != null && SORT_STARTED_EVENT.equals(e.getEventType())) {
                sorting = true;
                innerTableView.getSelectionModel().getSelectedIndices().removeListener(rowHeaderSelectionListener);
            } else if (e != null && SORT_ENDED_EVENT.equals(e.getEventType())) {
                sorting = false;
                if (innerSkin != null) {
                    innerSkin.getFlow().rebuildFixedCells();
                }
                innerTableView.getSelectionModel().clearSelection();
                selectIndices(innerTableView.getSelectionModel(),
                        validRows(innerTableView, skin.getSelectedRows()));
                innerTableView.getSelectionModel().getSelectedIndices().addListener(rowHeaderSelectionListener);
            }
        });

        //sync south blend
        innerTableView.southHeaderBlendedProperty().bind(tableView.southHeaderBlendedProperty());

        // keep focus on both tableViews
        tableView.focusedProperty().addListener(focusListener);
        innerTableView.focusedProperty().addListener(focusListener);

    }

    /**
     * Selects all the given row indices in a single batch, so the selection model
     * fires one single change event, instead of one event per selected row/cell.
     * @param selectionModel The selection model to update.
     * @param indices The list of valid row indices to be selected.
     */
    private void selectIndices(TableView.TableViewSelectionModel<S> selectionModel, List<Integer> indices) {
        if (selectionModel == null || indices == null || indices.isEmpty()) {
            return;
        }
        final int first = indices.get(0);
        if (indices.size() == 1) {
            selectionModel.select(first);
            return;
        }
        final int[] rest = indices.subList(1, indices.size()).stream()
                .mapToInt(Integer::intValue)
                .toArray();
        selectionModel.selectIndices(first, rest);
    }

    /**
     * Applies the given list change to the given table view.
     * @param table The table view to update
     * @param c The list change to apply.
     */
    private void applyListChange(TableView2<S> table, ListChangeListener.Change<? extends Integer> c) {
        if (table == null || c == null) {
            return;
        }
        final List<Integer> removed = new ArrayList<>();
        final List<Integer> added = new ArrayList<>();
        while (c.next()) {
            if (c.wasRemoved()) {
                removed.addAll(c.getRemoved());
            }
            if (c.wasAdded()) {
                added.addAll(c.getAddedSubList());
            }
        }
        final List<Integer> rowsToSelect = validRows(table, added);
        final Set<Integer> rowsToKeep = new HashSet<>(rowsToSelect);
        final List<Integer> rowsToDeselect = validRows(table, removed).stream()
                .filter(row -> !rowsToKeep.contains(row))
                .collect(Collectors.toList());
        clearRowsSelection(table, rowsToDeselect);
        selectIndices(table.getSelectionModel(), rowsToSelect);
    }

    /**
     * Deselects all the given rows, either one by one, or rebuilding the selection in a single
     * batch after clearing the full selection, whichever fires fewer change events.
     *
     * @param table The table view whose selection model should be updated.
     * @param rows The valid row indices to be deselected.
     */
    private void clearRowsSelection(TableView2<S> table, List<Integer> rows) {
        if (table == null || rows == null || rows.isEmpty()) {
            return;
        }
        final TableView.TableViewSelectionModel<S> sm = table.getSelectionModel();
        if (sm == null) {
            return;
        }
        final Set<Integer> rowsToDeselect = new HashSet<>(rows);
        if (sm.isCellSelectionEnabled()) {
            clearCells(table, sm, rowsToDeselect);
        } else {
            clearRows(table, sm, rowsToDeselect);
        }
    }

    /**
     * Deselects the given rows when row selection is enabled.
     * <p>If only a few rows have to be deselected, they are cleared one by one. Otherwise, the selection is
     * cleared at once and the rows that have to remain selected are re-selected in a single batch, and
     * only two change events are fired.</p>
     *
     * @param table The table view whose selection model should be updated.
     * @param sm The selection model of the given table view, with cell selection disabled.
     * @param rowsToDeselect The valid row indices to be deselected.
     */
    private void clearRows(TableView2<S> table, TableView.TableViewSelectionModel<S> sm, Set<Integer> rowsToDeselect) {
        if (rowsToDeselect.size() < 3 || rowsToDeselect.size() < sm.getSelectedIndices().size() / 4) {
            // if the number of rows to deselect is small, or less than 25% of the selected rows,
            // clearing them one by one fires fewer events than rebuilding the selection
            clearRowsOneByOne(sm, rowsToDeselect);
            return;
        }

        final int selectedIndex = sm.getSelectedIndex();
        final TableView.TableViewFocusModel<S> fm = table.getFocusModel();
        final int focusedIndex = fm == null ? -1 : fm.getFocusedIndex();

        // batch the rows that have to remain selected, preventing firing a change event per selected row
        final List<Integer> rowsToKeep = sm.getSelectedIndices().stream()
                .filter(row -> row != null && !rowsToDeselect.contains(row))
                .collect(Collectors.toCollection(ArrayList::new));

        // TableViewSelectionModel::selectIndices sets the selected index, the selected item and the
        // focus from the last index of the list, so move the selected index to the end to restore it
        if (rowsToKeep.remove((Integer) selectedIndex)) {
            rowsToKeep.add(selectedIndex);
        }

        sm.clearSelection();
        selectIndices(sm, rowsToKeep);

        if (fm != null && fm.getFocusedIndex() != focusedIndex) {
            // if needed, restore focus
            fm.focus(focusedIndex);
        }
    }

    /**
     * Deselects the given rows when cell selection is enabled.
     * <p>The selection is cleared at once, and the cells that have to remain selected are restored
     * with the fewest possible change events, see {@link CellSelectionRestorePath}. As a safeguard, the
     * selection is only rebuilt if that fires fewer events than deselecting the rows one by one.</p>
     *
     * @param table The table view whose selection model should be updated.
     * @param sm The selection model of the given table view, with cell selection enabled.
     * @param rowsToDeselect The valid row indices to be deselected.
     */
    @SuppressWarnings("unchecked")
    private void clearCells(TableView2<S> table, TableView.TableViewSelectionModel<S> sm, Set<Integer> rowsToDeselect) {
        final int columnCount = table.getVisibleLeafColumns().size();
        final Map<Integer, List<TableColumn<S, ?>>> cellsToKeepPerRow = cellsToKeepPerRow(sm, rowsToDeselect, columnCount);
        final CellSelectionRestorePath restorePath = new CellSelectionRestorePath(table, cellsToKeepPerRow, columnCount);

        if (restorePath.eventCount() > rowsToDeselect.size() * columnCount) {
            clearRowsOneByOne(sm, rowsToDeselect);
            return;
        }

        final int selectedIndex = sm.getSelectedIndex();
        final TableView.TableViewFocusModel<S> fm = table.getFocusModel();
        final TablePosition<S, ?> focusedCell = fm == null ? null : (TablePosition<S, ?>) fm.getFocusedCell();

        // clear the selection, and restore the rows that are selected as a whole
        sm.clearSelection();
        selectIndices(sm, restorePath.rowsToSelect);

        // select and clear the remaining individual cells
        restorePath.cellsToSelect.forEach(cell -> sm.select(cell.getRow(), cell.getTableColumn()));
        restorePath.cellsToClear.forEach(cell -> sm.clearSelection(cell.getRow(), cell.getTableColumn()));

        // restore the selected index and item: the cell is already selected, so no event is fired
        final List<TableColumn<S, ?>> selectedRowColumns = cellsToKeepPerRow.get(selectedIndex);
        if (selectedRowColumns != null && !selectedRowColumns.isEmpty()) {
            sm.select(selectedIndex, selectedRowColumns.get(0));
        }

        // restore focus
        if (focusedCell != null && cellsToKeepPerRow.containsKey(focusedCell.getRow())) {
            fm.focus(focusedCell.getRow(), focusedCell.getTableColumn());
        }
    }

    /**
     * Groups the currently selected cells that have to remain selected by their row, keeping the
     * selection order of both the rows and their cells.
     *
     * @param sm The selection model with the current cell selection.
     * @param rowsToDeselect The valid row indices to be deselected.
     * @param columnCount The number of visible leaf columns of the table view.
     * @return The columns of the cells that have to remain selected, per row.
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, List<TableColumn<S, ?>>> cellsToKeepPerRow(TableView.TableViewSelectionModel<S> sm,
            Set<Integer> rowsToDeselect, int columnCount) {
        final Map<Integer, List<TableColumn<S, ?>>> cellsToKeepPerRow = new LinkedHashMap<>();
        for (TablePosition<S, ?> cell : (List<TablePosition<S, ?>>) (List<?>) sm.getSelectedCells()) {
            final Integer row = cell.getRow();
            if (!rowsToDeselect.contains(row)) {
                cellsToKeepPerRow.computeIfAbsent(row, r -> new ArrayList<>(columnCount))
                        .add(cell.getTableColumn());
            }
        }
        return cellsToKeepPerRow;
    }

    /**
     * Deselects the given rows one by one.
     *
     * @param sm The selection model to update.
     * @param rowsToDeselect The valid row indices to be deselected.
     */
    private void clearRowsOneByOne(TableView.TableViewSelectionModel<S> sm, Set<Integer> rowsToDeselect) {
        if (sm == null || rowsToDeselect == null || rowsToDeselect.isEmpty()) {
            return;
        }
        rowsToDeselect.forEach(sm::clearSelection);
    }

    /**
     * The set of operations needed to restore a cell selection after it has been cleared at once.
     * <p>Fully selected rows are restored in a single batch. A partially selected row can't be part
     * of that batch, so it is restored either by selecting the whole row and then clearing its
     * missing cells, or by selecting its cells one by one, whichever needs fewer events.</p>
     */
    private class CellSelectionRestorePath {

        private final List<Integer> rowsToSelect;
        private final List<TablePosition<S, ?>> cellsToSelect = new ArrayList<>();
        private final List<TablePosition<S, ?>> cellsToClear = new ArrayList<>();

        CellSelectionRestorePath(TableView2<S> table, Map<Integer, List<TableColumn<S, ?>>> cellsToKeepPerRow, int columnCount) {
            rowsToSelect = new ArrayList<>(cellsToKeepPerRow.size());
            for (Map.Entry<Integer, List<TableColumn<S, ?>>> entry : cellsToKeepPerRow.entrySet()) {
                final int row = entry.getKey();
                final List<TableColumn<S, ?>> columnsToKeep = entry.getValue();
                if (columnsToKeep.size() > columnCount / 2) {
                    // if the number of cells to keep is more than half of the row,
                    // select the whole row and clear the missing cells
                    rowsToSelect.add(row);
                    if (columnsToKeep.size() < columnCount) {
                        final Set<TableColumn<S, ?>> keep = new HashSet<>(columnsToKeep);
                        table.getVisibleLeafColumns().stream()
                                .filter(column -> !keep.contains(column))
                                .map(column -> new TablePosition<>(table, row, column))
                                .forEach(cellsToClear::add);
                    }
                } else {
                    // else, select cells one by one
                    columnsToKeep.stream()
                            .map(column -> new TablePosition<>(table, row, column))
                            .forEach(cellsToSelect::add);
                }
            }
        }

        /**
         * @return The number of change events fired by applying this plan, without counting the
         * two events fired by clearing the selection and selecting the full rows in a batch.
         */
        int eventCount() {
            return cellsToSelect.size() + cellsToClear.size();
        }
    }

    /**
     * Filters out the row indices that are duplicated or out of the range of the items list of the given table,
     * as a safeguard to prevent {@link IndexOutOfBoundsException}.
     * @param table The table view whose items list is to be checked.
     * @param rows The row indices to be filtered.
     * @return The valid row indices, keeping their original order.
     */
    private List<Integer> validRows(TableView2<S> table, List<? extends Integer> rows) {
        if (table == null || rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }
        final int itemCount = table.getItems() == null ? 0 : table.getItems().size();
        if (itemCount == 0) {
            return Collections.emptyList();
        }
        return rows.stream()
                .filter(row -> row != null && 0 <= row && row < itemCount)
                .distinct()
                .collect(Collectors.toList());
    }

    public double getRowHeaderWidth() {
        return innerRowHeaderWidth.get();
    }

    public ReadOnlyDoubleProperty rowHeaderWidthProperty() {
        return innerRowHeaderWidth;
    }

    public double computeHeaderWidth() {
        double width = 0;
        if (tableView.isRowHeaderVisible()) {
            width += tableView.getRowHeaderWidth();
        }
        return width;
    }

    public TableView2<S> getParentTableView() {
        return tableView;
    }

    /** {@inheritDoc} */
    @Override protected void layoutChildren() {
        if (tableView.isRowHeaderVisible()) {
            double x = snappedLeftInset();
            innerRowHeaderWidth.setValue(tableView.getRowHeaderWidth());
            if (getChildren().isEmpty()) {
                getChildren().setAll(innerTableView);
            }

            if (innerSkin != null) {
                TableHeaderRow2 tableHeaderRow2 = innerSkin.getTableHeaderRow2();
                tableHeaderRow2.setPrefHeight(tableColumnHeaderHeight);
            }
            final ScrollBar hBar = skin.getHBar();
            double hBarHeight = hBar.isVisible() && tableView.getItems() != null && ! tableView.getItems().isEmpty() ?
                    snapSizeY(hBar.getHeight()) : 0;
            innerTableView.resizeRelocate(x, 0, innerRowHeaderWidth.get(), tableView.getHeight() - hBarHeight -
                    tableView.snappedTopInset() - tableView.snappedBottomInset());
            if (! innerTableView.getColumns().isEmpty()) {
                innerTableView.getColumns().get(0).setPrefWidth(innerRowHeaderWidth.get());
            }

            Label label;
            if (getChildren().size() == 1) {
                label = new Label("");
                label.getStyleClass().setAll("hbar");
                getChildren().add(label);
            } else {
                label = (Label) getChildren().get(1);
            }

            label.resizeRelocate(snappedLeftInset(), getHeight() - snappedBottomInset() - hBarHeight,
                    innerRowHeaderWidth.get(), hBarHeight);
        } else {
            getChildren().clear();
            innerRowHeaderWidth.setValue(0);
        }
    }

    private void setContent() {
        innerTableView.getColumns().clear();
        if (tableView.getVisibleLeafColumns().isEmpty()) {
            return;
        }
        if (tableView.getRowHeader() != null) {
            innerTableView.getColumns().add(tableView.getRowHeader());
        } else {
            innerTableView.getColumns().add(getDefaultTableColumn());
        }
    }

    private TableColumn2<S, String> getDefaultTableColumn() {
        TableColumn2<S, String> column;
        if (tableView instanceof FilteredTableView) {
            column = new FilteredTableColumn<>();
            // default option: reset filter on main tableView
            ((FilteredTableColumn) column).setOnFilterAction(e -> {
                    if (((FilteredTableView) tableView).getPredicate() != null) {
                        ((FilteredTableView) tableView).resetFilter();
                    }
                });
        } else {
            column = new TableColumn2<>();
        }

        column.setSortable(false);
        column.setCellValueFactory(p -> new SimpleStringProperty(String.valueOf(p.getTableView().getItems().indexOf(p.getValue()) + 1)));
        return column;
    }

    private void setScrollbars() {
        ScrollBar scrollBarParent = skin.getVBar();
        ScrollBar scrollBar = innerSkin.getVBar();
        scrollBar.setMin(scrollBarParent.getMin());
        scrollBar.setMax(scrollBarParent.getMax());
        scrollBar.valueProperty().bindBidirectional(scrollBarParent.valueProperty());

        // If adjustPixels is called in one tableView, sync the other one
        innerSkin.getFlow().adjustedPixelsProperty().bindBidirectional(skin.getFlow().adjustedPixelsProperty());
    }

    /**
     * *************************************************************************
     * * Listeners * *
     * ************************************************************************
     */
    private final InvalidationListener layout = (Observable o) -> requestLayout();
}
