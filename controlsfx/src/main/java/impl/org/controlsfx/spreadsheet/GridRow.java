/**
 * Copyright (c) 2013, 2016 ControlsFX
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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;


/**
 * 
 * The tableRow which will holds the SpreadsheetCell.
 */
public class GridRow extends TableRow<ObservableList<SpreadsheetCell>> {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final SpreadsheetHandle handle;
    /**
     * When the row is fixed, it may have a shift from its original position
     * which we need in order to layout the cells properly and also for the
     * rectangle selection.
     */
    DoubleProperty verticalShift = new SimpleDoubleProperty();

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public GridRow(SpreadsheetHandle handle) {
        super();
        this.handle = handle;

        /**
         *  FIXME Bug? When re-using the row, it should re-compute the prefHeight and not
         *  keep the old value.
         */
        this.indexProperty().addListener(weakPrefHeightListener);
        this.visibleProperty().addListener(weakPrefHeightListener);
        
        handle.getView().gridProperty().addListener(weakGridListener);
        handle.getView().hiddenRowsProperty().addListener(weakPrefHeightListener);
        handle.getView().hiddenColumnsProperty().addListener(weakPrefHeightListener);
        handle.getView().comparatorProperty().addListener(weakComparatorListener);
        
        /**
         * When the height is changing elsewhere, we need to update ourself if necessary.
         */
        handle.getCellsViewSkin().rowHeightMap.addListener(new MapChangeListener<Integer, Double>() {

            @Override
            public void onChanged(MapChangeListener.Change<? extends Integer, ? extends Double> change) {
                if (change.wasAdded() && change.getKey() == handle.getView().getModelRow(getIndex())) {
                    setRowHeight(change.getValueAdded());
                } else if (change.wasRemoved() && change.getKey() == handle.getView().getModelRow(getIndex())) {
                    setRowHeight(computePrefHeight(-1));
                }
            }
        });
        /**
         * When we are adding deported cells (fixed in columns) into a row via
         * addCell. The cell is not receiving the DRAG_DETECTED eventHandler
         * because it's the row that receives it first. If it's the case, we
         * must give the event to the cell underneath.
         */
        this.addEventHandler(MouseEvent.DRAG_DETECTED, weakDragHandler);
    }
    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    void addCell(CellView cell) {
        getChildren().add(cell);
    }

    void removeCell(CellView gc) {
        getChildren().remove(gc);
    }
    
    SpreadsheetView getSpreadsheetView() {
        return handle.getView();
    }

    @Override
    protected double computePrefHeight(double width) {
        return handle.getCellsViewSkin().getRowHeight(getIndex());
    }
    
    @Override
    protected double computeMinHeight(double width) {
        return handle.getCellsViewSkin().getRowHeight(getIndex());
    }
    
    @Override
    protected Skin<?> createDefaultSkin() {
        return new GridRowSkin(handle, this);
    }
    
    private final InvalidationListener setPrefHeightListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable o) {
            setRowHeight(computePrefHeight(-1));
        }
    };
    
    private final WeakInvalidationListener weakPrefHeightListener = new WeakInvalidationListener(setPrefHeightListener);
    
    public void setRowHeight(double height) {
        CellView.getValue(() -> {
            setHeight(height);
        });
        
        setPrefHeight(height);
        handle.getCellsViewSkin().rectangleSelection.updateRectangle();
    }
    
    private final EventHandler<MouseEvent> dragDetectedEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getTarget().getClass().equals(GridRow.class) && event.getPickResult().getIntersectedNode() != null 
                    && event.getPickResult().getIntersectedNode().getClass().equals(CellView.class)) {
                Event.fireEvent(event.getPickResult().getIntersectedNode(), event);
            }
        }
    };

    private final WeakEventHandler<MouseEvent> weakDragHandler = new WeakEventHandler(dragDetectedEventHandler);
   
    /**
     * When the Grid is changing, we have recreated a new SortedList, therefore
     * we must re-attach our listener to the Comparator.
     */
    private final InvalidationListener gridListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable o) {
            setRowHeight(computePrefHeight(-1));
            handle.getView().comparatorProperty().addListener(weakComparatorListener);
        }
    };

    private final WeakInvalidationListener weakGridListener = new WeakInvalidationListener(gridListener);
    /**
     * When the comparator is changing, we may have an issue with the fixedRow
     * not updating their inner cells. So we force it.
     */
    private final InvalidationListener comparatorListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable o) {
            updateIndex(getIndex());
            setRowHeight(computePrefHeight(-1));
        }
    };

    private final WeakInvalidationListener weakComparatorListener = new WeakInvalidationListener(comparatorListener);
}
