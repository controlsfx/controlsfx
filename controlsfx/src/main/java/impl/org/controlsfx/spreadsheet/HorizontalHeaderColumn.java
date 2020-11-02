/**
 * Copyright (c) 2013, 2015 ControlsFX
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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumnBase;
import javafx.scene.input.MouseEvent;

import javafx.beans.Observable;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableViewSkinBase;

/**
 * A cell column header.
 */
public class HorizontalHeaderColumn extends NestedTableColumnHeader {



   public HorizontalHeaderColumn(TableColumnBase<?, ?> tc) {
        super(tc);

        /**
         * Resolve https://bitbucket.org/controlsfx/controlsfx/issue/395
         * and https://bitbucket.org/controlsfx/controlsfx/issue/434
         */
        widthProperty().addListener((Observable observable) -> {
               ((GridViewSkin)  getTableSkin()).hBarValue.clear();
               ((GridViewSkin)  getTableSkin()).rectangleSelection.updateRectangle();

       });
    }

    @Override
    protected TableColumnHeader createTableColumnHeader(final TableColumnBase col) {
//        TableViewSkinBase<?,?,?,?,?,TableColumnBase<?,?>> tableViewSkin = getTableViewSkin();
        if (col == null || col.getColumns().isEmpty()) {
            final TableColumnHeader columnHeader = new TableColumnHeader(col);
            /**
             * When the user double click on a header, we want to resize the
             * column to fit the content.
             */
            columnHeader.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getClickCount() == 2 && mouseEvent.isPrimaryButtonDown()) {
                        ((GridViewSkin) getTableSkin()).resize(col, -1);
                    }
                }
            });
            return columnHeader;
        } else {
            return new HorizontalHeaderColumn(col);
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        layoutFixedColumns();
    }

    /**
     * We want ColumnHeader to be fixed when we freeze some columns
     */
    public void layoutFixedColumns() {
        SpreadsheetHandle handle = ((GridViewSkin) getTableSkin()).handle;
        final SpreadsheetView spreadsheetView = handle.getView();
        if (handle.getCellsViewSkin() == null || getChildren().isEmpty()) {
            return;
        }
        double hbarValue = handle.getCellsViewSkin().getHBar().getValue();

        final int labelHeight = (int) getChildren().get(0).prefHeight(-1);
        double fixedColumnWidth = 0;
        double x = snappedLeftInset();
        int max = getColumnHeaders().size();
        max = max > handle.getGridView().getVisibleLeafColumns().size() ? handle.getGridView().getVisibleLeafColumns().size() : max;
        max = max > spreadsheetView.getColumns().size() ? spreadsheetView.getColumns().size() : max;
        for (int j = 0; j < max; j++) {
            final TableColumnHeader n = getColumnHeaders().get(j);
            final double prefWidth = snapSize(n.prefWidth(-1));
            n.setPrefHeight(24.0);
            //If the column is fixed
            if (spreadsheetView.getColumns().get(spreadsheetView.getModelColumn(j)).isFixed()) {
                double tableCellX = 0;
                //If the column is hidden we have to translate it
                if (hbarValue + fixedColumnWidth > x) {

                    tableCellX = Math.abs(hbarValue - x + fixedColumnWidth);

                    n.toFront();
                    fixedColumnWidth += prefWidth;
                }
                n.relocate(x + tableCellX, labelHeight + snappedTopInset());
            }

            x += prefWidth;
        }
    }
}
