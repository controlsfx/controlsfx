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
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.MapChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseEvent;
import org.controlsfx.control.tableview2.TableView2;

/**
 *
 * The tableRow which will holds the TableView2 cells.
 * @param <S> The type of the objects contained within the {@link TableView2} items list.
 */
public class TableRow2<S> extends TableRow<S> {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final TableView2<S> tableView;
    private final TableView2Skin<S> skin;

    /**
     * When the row is fixed, it may have a shift from its original position
     * which we need in order to layout the cells properly and also for the
     * rectangle selection.
     */
    DoubleProperty verticalShift = new SimpleDoubleProperty();

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    TableView2<S> parentTableView = null;
    /**
     * @param tableView the {@link TableView2}
     */
    public TableRow2(TableView2<S> tableView) {
        super();
        this.tableView = tableView;


        if (tableView.getParent() != null && tableView.getParent() instanceof RowHeader) {
            parentTableView = ((RowHeader) tableView.getParent()).getParentTableView();
        }

        skin = (TableView2Skin<S>) (parentTableView != null ? parentTableView : tableView).getSkin();

        /**
         *  FIXME Bug? When re-using the row, it should re-compute the prefHeight and not
         *  keep the old value.
         */
        this.indexProperty().addListener(weakPrefHeightListener);
        this.visibleProperty().addListener(weakPrefHeightListener);

        /**
         * When the height is changing elsewhere, we need to update ourself if necessary.
         */
        skin.rowHeightMap.addListener((MapChangeListener.Change<? extends Integer, ? extends Double> change) -> {
            if (change.wasAdded() && change.getKey() == getIndex()) {
                setRowHeight(change.getValueAdded());
            } else if (change.wasRemoved() && change.getKey() == getIndex()) {
                setRowHeight(computePrefHeight(-1));
            }
        });
        skin.rowHeightMap.addListener((Observable o) -> {
            if (getIndex() > -1 && getIndex() < skin.rowHeightMap.size()) {
                setRowHeight(skin.rowHeightMap.getOrDefault(getIndex(), computePrefHeight(-1)));
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

    void addCell(TableCell<S, ?> cell) {
        getChildren().add(cell);
    }

    void removeCell(TableCell<S, ?> gc) {
        getChildren().remove(gc);
    }

    /** {@inheritDoc} */
    @Override protected double computePrefHeight(double width) {
        return skin.getRowHeight(getIndex());
    }

    /** {@inheritDoc} */
    @Override protected double computeMinHeight(double width) {
        return skin.getRowHeight(getIndex());
    }

    /** {@inheritDoc} */
    @Override protected Skin<?> createDefaultSkin() {
        return new TableRow2Skin<>(tableView, this);
    }

    private final InvalidationListener setPrefHeightListener = (Observable o) -> setRowHeight(computePrefHeight(-1));

    private final WeakInvalidationListener weakPrefHeightListener = new WeakInvalidationListener(setPrefHeightListener);

    public void setRowHeight(double height) {
        runOnFXThread(() -> setHeight(height));

        setPrefHeight(height);
    }

    private final EventHandler<MouseEvent> dragDetectedEventHandler = event -> {
        if (event.getTarget().getClass().equals(TableRow2.class) && event.getPickResult().getIntersectedNode() != null
                && event.getPickResult().getIntersectedNode().getClass().equals(TableCell.class)) {
            Event.fireEvent(event.getPickResult().getIntersectedNode(), event);
        }
    };

    private final WeakEventHandler<MouseEvent> weakDragHandler = new WeakEventHandler(dragDetectedEventHandler);

    private void runOnFXThread(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}
