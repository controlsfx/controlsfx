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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;

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
        this.indexProperty().addListener(setPrefHeightListener);
        
        handle.getView().gridProperty().addListener(setPrefHeightListener);
        
        /**
         * When the height is changing elsewhere, we need to update ourself if necessary.
         */
        handle.getCellsViewSkin().rowHeightMap.addListener(new MapChangeListener<Integer, Double>() {

            @Override
            public void onChanged(MapChangeListener.Change<? extends Integer, ? extends Double> change) {
                if(change.wasAdded() && change.getKey() == getIndex()){
                    setPrefHeight(change.getValueAdded());
                    requestLayout();
                }else if(change.wasRemoved() && change.getKey() == getIndex()){
                    setPrefHeight(computePrefHeight(-1));
                    requestLayout();
                }
            }
        });
    }
    private final InvalidationListener setPrefHeightListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable o) {
            setPrefHeight(computePrefHeight(-1));
        }
    };
    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * When unfixing some Columns, we need to put the previously FixedColumns
     * back if we want the hover to be dealt correctly
     * 
     */
    public void putFixedColumnToBack() {
        final List<Node> tset = new ArrayList<>(getChildren());
        tset.sort(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                // In case it's null (some rows are initiated after rowCount)
                if (((CellView) o1).getItem() == null || ((CellView) o2).getItem() == null) {
                    return -1;
                }
                final int lhs = getTableView().getColumns().indexOf(((CellView) o1).getTableColumn());
                final int rhs = getTableView().getColumns().indexOf(((CellView) o2).getTableColumn());
                if (lhs < rhs) {
                    return -1;
                }
                if (lhs > rhs) {
                    return +1;
                }
                return 0;

            }
        });
        getChildren().setAll(tset);
    }

    public void addCell(CellView cell) {
        getChildren().add(cell);
    }

    public void removeCell(CellView gc) {
        getChildren().remove(gc);
    }

    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    SpreadsheetView getSpreadsheetView() {
        return handle.getView();
    }

    /**
     * Set this SpreadsheetRow hoverProperty
     * 
     * @param hover
     */
    void setHoverPublic(boolean hover) {
        this.setHover(hover);
    }

    /**
     * Return the SpreadsheetCell at the specified column. We have to be careful
     * because if we have fixedColumns then the fixedColumns cells will be at
     * the end of the Children's List
     * 
     * @param col
     * @return the corresponding SpreadsheetCell
     */
    CellView getGridCell(int col) {

        for (Node node : getChildrenUnmodifiable()) {
            CellView cellView = (CellView) node;
            SpreadsheetCell cell = cellView.getItem();
            if (cell.getColumn() == col) {
                return cellView;
            }
        }
        return null;
    }

    @Override
    protected double computePrefHeight(double width) {
        return handle.getCellsViewSkin().getRowHeight(getIndex());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GridRowSkin(handle, this);
    }

}
