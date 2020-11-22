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
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.tableview2.TableView2;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

import static impl.org.controlsfx.tableview2.SouthTableHeaderRow.SOUTH_HEADER_STYLE;

/**
 * The set of horizontal (column) headers.
 */
public class TableHeaderRow2 extends TableHeaderRow {

    private final static String FIXED_STYLE = "fixed";  //$NON-NLS-1$
    private final static String SELECTED_STYLE = "selected";  //$NON-NLS-1$
    private final static String LEAF_STYLE = "leaf-header";  //$NON-NLS-1$
    
    final TableView2Skin<?> skin;
    final TableView2<?> control;
    
    // Indicate whether the this TableHeaderRow2 is activated or not
    private boolean working = true;
    /**
     * When the columns header are clicked, we consider the column as selected.
     * This BitSet is reset when a modification on cells is done.
     */
    protected BitSet selectedColumns = new BitSet();

    private final List<TableColumnHeader> visibleLeafHeaders;
        
    private double scrollX;

    private double tableWidth;
    
    private final NestedTableColumnHeader header;
    private NestedTableColumnHeader2 rootHeader2;
    private final Region filler;
    private final Pane cornerRegion;
    private final StackPane dragHeader;
    private TableColumnHeader reorderingRegion;

    private final SouthTableHeaderRow southHeaderRow;

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/
    /**
     * 
     * @param skin the {@link TableView2Skin}
     */
    public TableHeaderRow2(final TableView2Skin<?> skin) {
        super(skin);
        this.skin = skin;
        control = (TableView2<?>) skin.getSkinnable();
        
        visibleLeafHeaders = new ArrayList<>();
        buildVisibleLeafColumnHeaders(getRootHeader().getColumnHeaders(), visibleLeafHeaders);
        
        filler = (Region) getChildrenUnmodifiable().get(0);
        header = (NestedTableColumnHeader) getChildrenUnmodifiable().get(1);
        cornerRegion = (Pane) getChildrenUnmodifiable().get(2);
        dragHeader = (StackPane) getChildrenUnmodifiable().get(3);
        
        southHeaderRow = new SouthTableHeaderRow(skin);
        getChildren().add(0, southHeaderRow);
    }

    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    public void init() {
        updateColumnHeaderVisibility(skin.isColumnHeaderVisible());

        //Visibility of vertical Header listener
        control.rowHeaderVisibleProperty().addListener(rowHeaderListener);
        if (skin.rowHeader != null) {
            skin.rowHeader.rowHeaderWidthProperty().addListener(rowHeaderListener);
        }
        // style of column headers
        control.columnFixingEnabledProperty().addListener(columnFixingEnabledHeaderListener);
        control.itemsProperty().addListener(columnFixingEnabledHeaderListener);

        //Selection listener to highlight header
        skin.getSelectedColumns().addListener(selectionListener);

        //Fixed Column listener to change style of header
        control.getFixedColumns().addListener(fixedColumnsListener);
        
        Platform.runLater(() -> {
            //We are doing that because some columns may be already fixed.
            updateFixedColumnsStyle();
            requestLayout();
            
            updateVisibleLeafColumnHeaders();
            updateVisibleLeafStyle();
        });
        
        /**
         * When we are setting a new model on the TableView2, it
         * appears that the headers are re-created. So we need to listen to
         * those changes in order to re-apply our css style class. Otherwise
         * we'll end up with fixedColumns but no graphic confirmation.
         */
        getRootHeader().getColumnHeaders().addListener((Observable o) -> {
            updateFixedColumnsStyle();
            updateHighlightSelection();
            updateVisibleLeafStyle();
        });
        
        /**
         * Allows cancelling dragging events when ESC key is pressed
         */
        control.addEventHandler(KeyEvent.KEY_PRESSED, cancelDrag);
        
        southHeaderRow.heightProperty().addListener(o -> updateVisibleLeafStyle());
        control.southHeaderBlendedProperty().addListener(o -> updateVisibleLeafStyle());
    }

    public SouthTableHeaderRow getSouthHeaderRow() {
        return southHeaderRow;
    }
    
    /**************************************************************************
     * 
     * Protected methods
     * 
     **************************************************************************/
    /** {@inheritDoc} */
    @Override protected void layoutChildren() {
        double x = scrollX;
        double headerWidth = snapSize(header.prefWidth(-1));
        
        double southTableHeaderRowHeight = southHeaderRow.prefHeight(headerWidth);
        
        southHeaderRow.resizeRelocate(x, getHeight() - snappedBottomInset() - southTableHeaderRowHeight, headerWidth, southTableHeaderRowHeight);
            
        double prefHeight = getHeight() - snappedTopInset() - snappedBottomInset();
        double cornerWidth = snapSize(skin.getFlow().getVerticalBar().prefWidth(-1));

        // position the main nested header
        header.resizeRelocate(x, snappedTopInset(), headerWidth, prefHeight - southTableHeaderRowHeight);

        // position the filler region
        if (control == null) {
            return;
        }

        final double controlInsets = control.snappedLeftInset() + control.snappedRightInset();
        double fillerWidth = tableWidth - headerWidth + filler.getInsets().getLeft() - controlInsets;
        fillerWidth -= control.tableMenuButtonVisibleProperty().get() ? cornerWidth : 0;
        filler.setVisible(fillerWidth > 0);
        if (fillerWidth > 0) {
            filler.resizeRelocate(x + headerWidth, snappedTopInset(), fillerWidth, prefHeight);
        }

        // position the top-right rectangle (which sits above the scrollbar)
        cornerRegion.resizeRelocate(tableWidth - cornerWidth, snappedTopInset(), cornerWidth, prefHeight);
    }
    
    void clearSelectedColumns(){
        selectedColumns.clear();
    }
    
    /** {@inheritDoc} */
    @Override protected void updateTableWidth() {
        super.updateTableWidth();
        // snapping added for JDK-8127930
        double padding = 0;

        if (working && skin != null
                && control != null
                && control.rowHeaderVisibleProperty().get()
                && skin.rowHeader != null) {
            padding += skin.rowHeader.getRowHeaderWidth();
        }

        Rectangle clip = ((Rectangle) getClip());
        tableWidth = clip.getWidth();
        
        clip.setWidth(clip.getWidth() == 0 ? 0 : clip.getWidth() - padding);
    }

    /** {@inheritDoc} */
    @Override protected void updateScrollX() {
        scrollX = skin.getFlow().getHorizontalBar().isVisible() ? -skin.getFlow().getHorizontalBar().getValue() : 0.0F;
        requestLayout();
        layout();
        
        if (working) {
            requestLayout();
            if (rootHeader2 == null) {
                createRootHeader();
            }
            rootHeader2.layoutFixedColumns();
        }
        skin.getSouthHeader().updateScrollX();
    }

    /** {@inheritDoc} */
    @Override protected NestedTableColumnHeader createRootHeader() {
        rootHeader2 = new NestedTableColumnHeader2(null);
        return rootHeader2;
    }

    protected void updateVisibleLeafColumnHeaders() {
        visibleLeafHeaders.clear();
        buildVisibleLeafColumnHeaders(getRootHeader().getColumnHeaders(), visibleLeafHeaders);
        updateHighlightSelection();
    }

    /** {@inheritDoc} */
    @Override public TableColumnHeader getReorderingRegion() {
        return reorderingRegion;
    }
    
    /** {@inheritDoc} */
    @Override public void setReorderingRegion(TableColumnHeader reorderingRegion) {
        this.reorderingRegion = reorderingRegion;
        
        if (reorderingRegion != null) {
            dragHeader.resize(reorderingRegion.getWidth(), reorderingRegion.getHeight());
            
            reorderingProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    // Adjust dragHeader region based in rowHeader and southHeaderRow offsets
                    dragHeader.setLayoutX(- skin.rowHeader.getRowHeaderWidth());
                    dragHeader.setTranslateY(dragHeader.getTranslateY() - southHeaderRow.getHeight());
                    dragHeader.resize(reorderingRegion.getWidth(), dragHeader.getHeight() + southHeaderRow.getHeight());
                    reorderingProperty().removeListener(this);
                }
            });
        }
    }
    
    /**************************************************************************
     * 
     * Private methods.
     * 
     **************************************************************************/
    
    /**
     * Whether the Row Header is showing, we need to update the width
     * because some space on the left will be available/used.
     */
    private final InvalidationListener rowHeaderListener = o -> updateTableWidth();
    
    /**
     * When columnFixingEnabled property changes, we need to update the header
     * style of the fixed columns
     */
    private final InvalidationListener columnFixingEnabledHeaderListener = o -> updateFixedColumnsStyle();
    
    /**
     * When we fix/unfix some columns, we change the style of the Label header
     * text
     */
    private final ListChangeListener<TableColumn> fixedColumnsListener = (ListChangeListener.Change<? extends TableColumn> change) -> {
        while (change.next()) {
            //If we unfix a column
            change.getRemoved().forEach(this::unfixColumnStyle);
            //If we fix one
            change.getAddedSubList().forEach(this::fixColumnStyle);
        }
        updateHighlightSelection();
    };

    /**
     * Fix this column regarding the style
     *
     * @param column
     */
    private void fixColumnStyle(TableColumn column) {
        int i = skin.getViewColumn(control.getColumns().indexOf(column));
        if (getRootHeader().getColumnHeaders().size() > i) {
            if (control.isColumnFixingEnabled() && control.getItems() != null && column.isVisible()) {
                addStyleHeader(getRootHeader().getColumnHeaders().get(i), FIXED_STYLE);
                addStyleSouthHeader(column, FIXED_STYLE);
            } else {
                removeStyleHeader(getRootHeader().getColumnHeaders().get(i), FIXED_STYLE);
                removeStyleSouthHeader(column, FIXED_STYLE);
            }
        }
    }

    /**
     * Unfix this column regarding the style
     *
     * @param column
     */
    private void unfixColumnStyle(TableColumn column) {
        int i = skin.getViewColumn(control.getColumns().indexOf(column));
        if (getRootHeader().getColumnHeaders().size() > i) {
            removeStyleHeader(getRootHeader().getColumnHeaders().get(i), FIXED_STYLE);
        }
        removeStyleSouthHeader(column, FIXED_STYLE);
    }

    /**
     * Remove the fix style of the header Label of the specified column
     *
     * @param i
     */
    private void removeStyleHeader(TableColumnHeader header, String style) {
        if (header instanceof NestedTableColumnHeader) {
            ((NestedTableColumnHeader) header).getChildrenUnmodifiable().stream()
                    .filter(TableColumnHeader.class::isInstance)
                    .map(TableColumnHeader.class::cast)
                    .forEach(t -> removeStyleHeader(t, style));
        } else {
            if (header.getStyleClass().contains(style)) {
                header.getStyleClass().remove(style);
            }
        }
    }

    /**
     * Add the fix style of the header Label of the specified column
     *
     * @param i
     */
    private void addStyleHeader(TableColumnHeader header, String style) {
        if (header instanceof NestedTableColumnHeader) {
            ((NestedTableColumnHeader) header).getChildrenUnmodifiable().stream()
                    .filter(TableColumnHeader.class::isInstance)
                    .map(TableColumnHeader.class::cast)
                    .forEach(t -> addStyleHeader(t, style));
        } else {
            if (! header.getStyleClass().contains(style)) {
                header.getStyleClass().add(style);
            }
        }
    }

    private void addStyleSouthHeader(TableColumn<?, ?> column, String style) {
        SouthTableColumnHeader southHeader = southHeaderRow.getSouthColumnHeaderFor(column);
        if (southHeader != null) {
            if (! southHeader.getStyleClass().contains(style)) {
                southHeader.getStyleClass().add(style);
            }
        } else {
            column.getColumns().forEach(col -> addStyleSouthHeader(col, style));
        }
    }
    
    private void removeStyleSouthHeader(TableColumn<?, ?> column, String style) {
        SouthTableColumnHeader southHeader = southHeaderRow.getSouthColumnHeaderFor(column);
        if (southHeader != null) {
            if (southHeader.getStyleClass().contains(style)) {
                southHeader.getStyleClass().remove(style);
            }
        } else {
            column.getColumns().forEach(col -> removeStyleSouthHeader(col, style));
        }
    }

    /**
     * When we select some cells, we want the header to be highlighted
     */
    private final InvalidationListener selectionListener = o -> updateHighlightSelection();

    /**
     * Highlight the header Label when selection change.
     */
    private void updateHighlightSelection() {
        removeStyleHeader(getRootHeader(), SELECTED_STYLE);
        visibleLeafHeaders.forEach(h -> 
                removeStyleSouthHeader((TableColumn<?, ?>) h.getTableColumn(), SELECTED_STYLE));
        
        skin.getSelectedColumns().forEach(i -> {
            if (visibleLeafHeaders.size() > i && 
                 ! visibleLeafHeaders.get(i).getStyleClass().contains(SELECTED_STYLE)) {
                visibleLeafHeaders.get(i).getStyleClass().add(SELECTED_STYLE);
                addStyleSouthHeader((TableColumn<?, ?>) visibleLeafHeaders.get(i).getTableColumn(), SELECTED_STYLE);
            }
        });
        
    }
    
    private void buildVisibleLeafColumnHeaders(List<TableColumnHeader> colHeaders, List<TableColumnHeader> vlch) {
        colHeaders.stream()
                .filter(Objects::nonNull)
                .forEachOrdered(c -> {
                    if (c instanceof NestedTableColumnHeader) {
                        buildVisibleLeafColumnHeaders(((NestedTableColumnHeader) c).getColumnHeaders(), vlch);
                    } else if (c.isVisible()) {
                        vlch.add(c);
                    }
                });
    }
    
    private void updateColumnHeaderVisibility(boolean visible) {
        working = visible;
        setManaged(working);
        if (!visible) {
            getStyleClass().add("invisible"); //$NON-NLS-1$
        } else {
            getStyleClass().remove("invisible"); //$NON-NLS-1$
            requestLayout();
            if (rootHeader2 == null) {
                createRootHeader();
            }
            rootHeader2.layoutFixedColumns();
            updateHighlightSelection();
        }
    }
    
    private void updateFixedColumnsStyle() {
        Platform.runLater(() -> control.getFixedColumns().forEach(this::fixColumnStyle));
    }
    
    private void updateVisibleLeafStyle() {
        removeStyleHeader(getRootHeader(), LEAF_STYLE);
        if (southHeaderRow.getHeight() > 0) {
            visibleLeafHeaders.forEach(h -> h.getStyleClass().add(LEAF_STYLE));
        }
        
        removeStyleHeader(getRootHeader(), SOUTH_HEADER_STYLE);
        if (southHeaderRow.getHeight() > 0 && control.isSouthHeaderBlended()) {
            visibleLeafHeaders.forEach(h -> h.getStyleClass().add(SOUTH_HEADER_STYLE));
        }
    }

    /**
     * When a dragging event to reorder columns starts, reordering is set to true. 
     * If ESC is pressed, the event can be cancelled, by setting reordering to 
     * false and setting the column to not reorderable.
     */
    private final EventHandler<KeyEvent> cancelDrag = e -> {
            if (isReordering() && e.getCode() == KeyCode.ESCAPE) {
                setReordering(false);
                TableColumnHeader columnHeader = getReorderingRegion();
                if (columnHeader != null) {
                    columnHeader.getTableColumn().setReorderable(false);
                }
            }
        };

    /**
     * {@inheritDoc}
     */
    @Override
    protected double computePrefHeight(double width) {
        /**
         * We have a weird situation where the headerRow is 22.0 when no cells
         * is clicked in the Grid..
         */
        //If it's not showing, height is 0!
        if (! skin.isColumnHeaderVisible()) {
            return 0.0;
        }
        // we hardcode 24.0 here to avoid JDK-8095994, where the
        // entire header row would disappear when all columns were hidden.
        double headerPrefHeight = getRootHeader().prefHeight(width);
        headerPrefHeight = headerPrefHeight == 0.0 ? 24.0 : headerPrefHeight;
        double southTableHeaderRowHeight = southHeaderRow.prefHeight(width);
        
        return snappedTopInset() + headerPrefHeight + southTableHeaderRowHeight + snappedBottomInset();
    }
}
