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


import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.FilteredTableView;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static impl.org.controlsfx.tableview2.SortUtils.SortEndedEvent.SORT_ENDED_EVENT;

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

    private boolean shiftDown;

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
        Bindings.bindContent(innerTableView.getFixedRows(), tableView.getFixedRows());


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
        tableView.selectionModelProperty().addListener((observable, oldValue, newValue) -> {
            TableView.TableViewSelectionModel<S> value = new ForwardSelectionModel(newValue);
            innerTableView.setSelectionModel(value);
        });
        this.innerTableView.setSelectionModel(new ForwardSelectionModel(tableView.getSelectionModel()));

        final ChangeListener<Boolean> focusListener = (obs, ov, nv) -> {
            if (! tableView.isFocused() && ! innerTableView.isFocused()) {
                tableView.setStyle("-fx-selection-bar-non-focused: lightgrey;");
                innerTableView.setStyle("-fx-selection-bar-non-focused: lightgrey;");
            } else {
                innerTableView.setStyle("-fx-selection-bar-non-focused: -fx-accent;");
                tableView.setStyle("-fx-selection-bar-non-focused: -fx-accent;");
            }
        };

        final EventHandler<KeyEvent> keyListener = keyEvent -> {
            if (keyEvent.getCode() == KeyCode.SHIFT) {
               var eventType = keyEvent.getEventType();
                if (eventType == KeyEvent.KEY_PRESSED) {
                    shiftDown = true;
                } else if (eventType == KeyEvent.KEY_RELEASED) {
                    shiftDown = false;
                }
            }
        };

        // When sorting, the external TableView fires add/remove selection events.
        // These change the innerTableView selected rows. To avoid firing new
        // events back to the TableView, we have to remove rowHeaderSelectionListener
        // while sorting.
        tableView.addEventHandler(SortEvent.ANY, e -> {
            if (e != null && SORT_ENDED_EVENT.equals(e.getEventType())) {
                if (innerSkin != null) {
                    innerSkin.getFlow().rebuildFixedCells();
                }
            }
        });

        //sync south blend
        innerTableView.southHeaderBlendedProperty().bind(tableView.southHeaderBlendedProperty());

        // keep focus on both tableViews
        tableView.focusedProperty().addListener(focusListener);
        innerTableView.focusedProperty().addListener(focusListener);

        tableView.addEventFilter(KeyEvent.ANY, keyListener);

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
            ((FilteredTableColumn<?,?>) column).setOnFilterAction(e -> {
                    if (((FilteredTableView<?>) tableView).getPredicate() != null) {
                        ((FilteredTableView<?>) tableView).resetFilter();
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

    private class ForwardSelectionModel extends TableView.TableViewSelectionModel<S> {
        private final TableView.TableViewSelectionModel<S> baseSelectionModel;

        private final TransformationList<TablePosition, TablePosition> transformationList;

        private final Set<Integer> selectedIndices;

        private int lastSelected = 0;

        public ForwardSelectionModel(TableView.TableViewSelectionModel<S> baseSelectionModel) {
            super(innerTableView);
            selectedIndices = new HashSet<>();
            baseSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                selectedIndices.clear();
                selectedIndices.addAll(baseSelectionModel.getSelectedIndices());
                updateRowHeader();
            });
            this.baseSelectionModel = baseSelectionModel;

            final Function<TablePosition, TablePosition> mappingFunction = tablePosition -> new TablePosition(innerTableView, tablePosition.getRow(), null);
            this.transformationList = new MappedList(baseSelectionModel.getSelectedCells(), mappingFunction);
        }

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return this.baseSelectionModel.getSelectedIndices();
        }

        @Override
        public ObservableList<TablePosition> getSelectedCells() {
            return this.transformationList;
        }

        @Override
        public ObservableList<S> getSelectedItems() {
            return baseSelectionModel.getSelectedItems();
        }

        @Override
        public boolean isSelected(int index) {
            return selectedIndices.contains(index);
        }

        @Override
        public boolean isSelected(int row, TableColumn<S, ?> column) {
            return isSelected(row);
        }

        @Override
        public void selectRange(int start, int end) {
            var visibleLeafColumns = tableView.getVisibleLeafColumns();
            if (!visibleLeafColumns.isEmpty()) {
                this.baseSelectionModel.clearAndSelect(start, visibleLeafColumns.get(0));
                if (visibleLeafColumns.size() > 1) {
                    baseSelectionModel.selectRange(start, tableView.getVisibleLeafColumn(0), end, tableView.getVisibleLeafColumn(visibleLeafColumns.size() - 1));
                }
            }
            updateRowHeader();
        }

        @Override
        public void selectRange(int minRow, TableColumnBase<S, ?> minColumn, int maxRow, TableColumnBase<S, ?> maxColumn) {
            selectRange(minRow, maxRow);
        }

        @Override
        public void select(int row, TableColumn<S, ?> column) {
            selectRange(row, row);
        }

        @Override
        public void clearAndSelect(int row, TableColumn<S, ?> column) {
            var columns = tableView.getColumns();
            if (!columns.isEmpty()) {
                var start = row;
                if (shiftDown) {
                    start = lastSelected;
                } else {
                    this.baseSelectionModel.clearAndSelect(row, columns.get(0));
                }
                lastSelected = row;
                if (columns.size() > 1) {
                    selectRange(start, row);
                    return;
                }
            }
            updateRowHeader();
        }

        private void updateRowHeader() {
            if (tableView.isRowHeaderVisible()) {
                innerSkin.getSelectedRows().setAll(selectedIndices);
            }
        }

        @Override
        public void clearSelection(int row, TableColumn<S, ?> column) {
            var columns = tableView.getColumns();
            if (!columns.isEmpty()) {
                selectedIndices.remove(row);
                this.baseSelectionModel.clearSelection(row);
            }
            updateRowHeader();
        }

        @Override
        public void selectLeftCell() {
            throw generateExceptionForUnexpectedCall();
        }

        @Override
        public void selectRightCell() {
            throw generateExceptionForUnexpectedCall();
        }

        @Override
        public void selectAboveCell() {
            throw generateExceptionForUnexpectedCall();
        }

        @Override
        public void selectBelowCell() {
            throw generateExceptionForUnexpectedCall();
        }

        private RuntimeException generateExceptionForUnexpectedCall() {
            return new IllegalStateException("should not be called, as it is just forwarding");
        }
    }

    private static final class MappedList<M, B> extends TransformationList<M, B> {
        private final Function<B, M> mappingFunction;

        public MappedList(ObservableList<B> baseList, Function<B, M> mappingFunction) {
            super(baseList);
            this.mappingFunction = mappingFunction;
        }

        @Override
        protected void sourceChanged(ListChangeListener.Change c) {
            ObservableList<M> mapped = FXCollections.observableList(asMappedStream(c.getList()).collect(Collectors.toList()));
            this.fireChange(new ListChangeListener.Change<>(mapped) {
                @Override
                public boolean next() {
                    return c.next();
                }

                @Override
                public void reset() {
                    c.reset();
                }

                @Override
                public int getFrom() {
                    return c.getFrom();
                }

                @Override
                public int getTo() {
                    return c.getTo();
                }

                @Override
                public List<M> getRemoved() {
                    return asMappedStream(c.getRemoved()).collect(Collectors.toList());
                }

                @Override
                public boolean wasAdded() {
                    return c.wasAdded();
                }

                @Override
                public boolean wasPermutated() {
                    return c.wasPermutated();
                }

                @Override
                public boolean wasRemoved() {
                    return c.wasRemoved();
                }

                @Override
                public boolean wasReplaced() {
                    return c.wasReplaced();
                }

                @Override
                public boolean wasUpdated() {
                    return c.wasUpdated();
                }

                @Override
                protected int[] getPermutation() {
                    throw new IllegalStateException("call error");
                }
            });
        }

        private Stream<M> asMappedStream(List<?> list) {
            return list.stream().map(a -> (B) a).map(mappingFunction);
        }

        @Override
        public int getSourceIndex(int index) {
            return index;
        }

        @Override
        public int getViewIndex(int index) {
            return index;
        }

        @Override
        public M get(int index) {
            var position = getSource().get(index);
            return mappingFunction.apply(position);
        }

        @Override
        public int size() {
            return getSource().size();
        }
    }
}
