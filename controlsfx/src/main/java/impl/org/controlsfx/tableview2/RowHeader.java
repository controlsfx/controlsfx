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
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SortEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.FilteredTableColumn;
import org.controlsfx.control.tableview2.FilteredTableView;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;

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
                snapSize(tableView.getHeight() - tableView.snappedTopInset() - tableView.snappedBottomInset()));
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
            while (c.next()) {
                c.getRemoved().forEach(i -> {
                        if (tableView.getSelectionModel().isCellSelectionEnabled()) {
                            tableView.getVisibleLeafColumns().forEach(col -> tableView.getSelectionModel().clearSelection(i, col));
                        } else {
                            tableView.getSelectionModel().clearSelection(i);
                        }
                    });
                c.getAddedSubList().forEach(i -> tableView.getSelectionModel().select(i));
            }
            skin.getSelectedRows().addListener(tableSelectionListener);
        };
        tableSelectionListener = (ListChangeListener.Change<? extends Integer> c) -> {
            innerTableView.getSelectionModel().getSelectedIndices().removeListener(rowHeaderSelectionListener);
            while (c.next()) {
                c.getRemoved().forEach(i -> innerTableView.getSelectionModel().clearSelection(i));
                c.getAddedSubList().forEach(i -> innerTableView.getSelectionModel().select(i));
            }
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
                if (innerTableView.getItems() != null) {
                    skin.getSelectedRows().forEach(i -> innerTableView.getSelectionModel().select(i));
                }
                innerTableView.getSelectionModel().getSelectedIndices().addListener(rowHeaderSelectionListener);
            }
        });

        //sync south blend
        innerTableView.southHeaderBlendedProperty().bind(tableView.southHeaderBlendedProperty());

        // keep focus on both tableViews
        tableView.focusedProperty().addListener(focusListener);
        innerTableView.focusedProperty().addListener(focusListener);

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
                    snapSize(hBar.getHeight()) : 0;
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
