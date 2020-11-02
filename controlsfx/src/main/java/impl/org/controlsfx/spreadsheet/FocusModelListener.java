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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * The FocusModel Listener adapted to the SpreadsheetView regarding Span.
 */
public class FocusModelListener implements ChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>> {

    private final TableView.TableViewFocusModel<ObservableList<SpreadsheetCell>> tfm;
    private final SpreadsheetGridView cellsView;
    private final SpreadsheetView spreadsheetView;

    /**
     * Constructor.
     *
     * @param spreadsheetView
     * @param cellsView
     */
    public FocusModelListener(SpreadsheetView spreadsheetView, SpreadsheetGridView cellsView) {
        tfm = cellsView.getFocusModel();
        this.spreadsheetView = spreadsheetView;
        this.cellsView = cellsView;
    }

    @Override
    public void changed(ObservableValue<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> ov,
            final TablePosition<ObservableList<SpreadsheetCell>, ?> oldPosition,
            final TablePosition<ObservableList<SpreadsheetCell>, ?> newPosition) {
        int columnIndex = -1;
        if (newPosition != null && newPosition.getTableColumn() != null) {
            columnIndex = cellsView.getColumns().indexOf(newPosition.getTableColumn());
        }
        final SpreadsheetView.SpanType spanType = spreadsheetView.getSpanType(newPosition.getRow(), columnIndex);
        switch (spanType) {
            case ROW_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go below.
                if (!spreadsheetView.isPressed() && oldPosition.getColumn() == newPosition.getColumn() && oldPosition.getRow() == newPosition.getRow() - 1) {
                    Platform.runLater(() -> {
                        tfm.focus(getNextRowNumber(oldPosition, cellsView, spreadsheetView), oldPosition.getTableColumn());
                    });

                } else {
                    // If the current focused cell if hidden by row span, we go
                    // above
                    Platform.runLater(() -> {
                        tfm.focus(newPosition.getRow() - 1, newPosition.getTableColumn());
                    });
                }

                break;
            case BOTH_INVISIBLE:
                // If the current focused cell if hidden by a both (row and
                // column) span, we go left-above
                Platform.runLater(() -> {
                    tfm.focus(newPosition.getRow() - 1, cellsView.getColumns().get(newPosition.getColumn() - 1));
                });
                break;
            case COLUMN_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go right.
                if (!spreadsheetView.isPressed() && oldPosition.getColumn() == newPosition.getColumn() - 1 && oldPosition.getRow() == newPosition.getRow()) {

                    Platform.runLater(() -> {
                        tfm.focus(oldPosition.getRow(), getTableColumnSpan(oldPosition, cellsView, spreadsheetView));
                    });
                } else {
                    // If the current focused cell if hidden by column span, we
                    // go left

                    Platform.runLater(() -> {
                        tfm.focus(newPosition.getRow(), cellsView.getVisibleLeafColumn(newPosition.getColumn() - 1));
                    });
                }
            default:
                break;
        }
    }

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param pos the current TablePosition
     * @return
     */
    static TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumnSpan(final TablePosition<?, ?> pos, SpreadsheetGridView cellsView, SpreadsheetView spv) {
        return cellsView.getVisibleLeafColumn(pos.getColumn()
                + spv.getColumnSpan(cellsView.getItems().get(pos.getRow()).get(cellsView.getColumns().indexOf(pos.getTableColumn()))));
    }

    /**
     * Return the Row number right after the current TablePosition (including
     * the RowSpan to be on a visible Cell)
     *
     * @param pos
     * @param cellsView
     * @param spv
     * @return
     */
    public static int getNextRowNumber(final TablePosition<?, ?> pos, TableView<ObservableList<SpreadsheetCell>> cellsView, SpreadsheetView spv) {
        return spv.getRowSpan(cellsView.getItems().get(pos.getRow()).get(cellsView.getColumns().indexOf(pos.getTableColumn())), pos.getRow())
                +pos.getRow();
    }

//    public static int getPreviousRowNumber(final TablePosition<?, ?> pos, TableView<ObservableList<SpreadsheetCell>> cellsView, GridViewSkin skin) {
//        return skin.getFirstRow(cellsView.getItems().get(pos.getRow()).get(cellsView.getColumns().indexOf(pos.getTableColumn())), pos.getRow()) - 1;
//    }
}
