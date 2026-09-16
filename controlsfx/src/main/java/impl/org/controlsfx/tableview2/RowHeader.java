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
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.FilteredTableView;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

import java.util.ArrayList;
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
                selectValidIndices(innerTableView, skin.getSelectedRows());
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
     * Selects all the given row indices, filtering out any invalid indices.
     * @param table The table view whose selection model should be updated.
     * @param rows The row indices to be selected.
     */
    private void selectValidIndices(TableView2<S> table, List<? extends Integer> rows) {
        if (table == null || table.getItems() == null || rows == null || rows.isEmpty()) {
            return;
        }
        selectIndices(table.getSelectionModel(), rows.stream()
                .filter(i -> isValidIndex(table, i))
                .collect(Collectors.toList()));
    }

    /**
     * Selects all the given row indices in a single batch, so the selection model
     * fires one single change event, instead of one event per selected row/cell.
     * @param selectionModel The selection model to update.
     * @param indices The list of row indices to be selected.
     */
    private void selectIndices(TableView.TableViewSelectionModel<S> selectionModel, List<Integer> indices) {
        if (selectionModel == null || indices == null || indices.isEmpty()) {
            return;
        }
        int first = indices.get(0);
        if (indices.size() == 1) {
            selectionModel.select(first);
            return;
        }
        int[] rest = indices.subList(1, indices.size()).stream()
                .mapToInt(Integer::intValue)
                .toArray();
        selectionModel.selectIndices(first, rest);
    }

    /**
     * Applies the given list change to the given table view.
     * @param tableView The table view to update
     * @param c The list change to apply.
     */
    private void applyListChange(TableView2<S> tableView, ListChangeListener.Change<? extends Integer> c) {
        if (tableView == null || c == null) {
            return;
        }
        while (c.next()) {
            if (c.wasRemoved()) {
                clearRowsSelection(tableView, c.getRemoved());
            }
            if (c.wasAdded()) {
                selectValidIndices(tableView, c.getAddedSubList());
            }
        }
    }

    /**
     * Deselects all the given rows.
     * <p>
     * In case of cell selection, if the rows that have to remain selected are fully selected rows, the selection
     * is cleared at once and those rows are re-selected in a single batch, preventing firing a change event per
     * removed cell.</p>
     * <p>For row selection or partial cell selection, the rows are cleared one by one.</p>
     *
     * @param table The table view whose selection model should be updated.
     * @param rows The row indices to be deselected.
     */
    private void clearRowsSelection(TableView2<S> table, List<? extends Integer> rows) {
        if (table == null || rows == null || rows.isEmpty()) {
            return;
        }
        final Set<Integer> rowsToDeselect = rows.stream()
                .filter(i -> isValidIndex(table, i))
                .collect(Collectors.toSet());
        final TableView.TableViewSelectionModel<S> sm = table.getSelectionModel();
        if (!sm.isCellSelectionEnabled()) {
            rowsToDeselect.forEach(sm::clearSelection);
            return;
        }

        final Map<Integer, Integer> cellsPerRemainingRow = sm.getSelectedCells().stream()
                .filter(position -> !rowsToDeselect.contains(position.getRow()))
                .collect(Collectors.toMap(TablePositionBase::getRow, position -> 1, Integer::sum, LinkedHashMap::new));
        final int columnCount = table.getVisibleLeafColumns().size();
        final boolean remainingRowsAreFullySelected = cellsPerRemainingRow.values().stream()
                        .allMatch(count -> count == columnCount);
        if (remainingRowsAreFullySelected) {
            final TablePosition<S, ?> focusedCell = table.getFocusModel() == null ? null : table.getFocusModel().getFocusedCell();
            // clear selection, select rows at once, and restore focus if possible
            sm.clearSelection();
            selectIndices(sm, new ArrayList<>(cellsPerRemainingRow.keySet()));
            if (focusedCell != null && cellsPerRemainingRow.containsKey(focusedCell.getRow())) {
                table.getFocusModel().focus(focusedCell.getRow(), focusedCell.getTableColumn());
            }
        } else {
            // partial cell selections have to be preserved, so rows are cleared one by one
            rowsToDeselect.forEach(sm::clearSelection);
        }
    }

    /**
     * Check that the {@code index} is within range of the {@code table} items list,
     * as a safeguard to prevent {@link IndexOutOfBoundsException}.
     * @param table The table view whose items list is to be checked.
     * @param index The index to be checked.
     */
    private boolean isValidIndex(TableView2<S> table, Integer index) {
        return index != null && table != null && table.getItems() != null &&
                0 <= index && index < table.getItems().size();
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
