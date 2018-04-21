/**
 * Copyright (c) 2013, 2017 ControlsFX
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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import org.controlsfx.control.tableview2.TableView2;

/**
 *
 * The FocusModel Listener adapted to the TableView2 regarding Span.
 * @param <S> The type of the objects contained within the {@link TableView2} items list
 */
public class FocusModelListener<S> implements ChangeListener<TablePosition<S, ?>> {

    private final TableView.TableViewFocusModel<S> tfm;
    private final TableView2<S> tableView;

    /**
     * Constructor.
     *
     * @param tableView the {@link TableView2}
     */
    public FocusModelListener(TableView2<S> tableView) {
        tfm = tableView.getFocusModel();
        this.tableView = tableView;
    }

    @Override
    public void changed(ObservableValue<? extends TablePosition<S, ?>> ov,
            final TablePosition<S, ?> oldPosition,
            final TablePosition<S, ?> newPosition) {
        int columnIndex = -1;
        if (newPosition == null) {
            return;
        }
        if (newPosition.getTableColumn() != null) {
            columnIndex = tableView.getColumns().indexOf(newPosition.getTableColumn());
        }
        final TableView2.SpanType spanType = tableView.getSpanType(newPosition.getRow(), columnIndex);
        switch (spanType) {
            case ROW_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go below.
                if (!tableView.isPressed() && oldPosition.getColumn() == newPosition.getColumn() && oldPosition.getRow() == newPosition.getRow() - 1) {
                    Platform.runLater(() -> {
                        tfm.focus(getNextRowNumber(oldPosition, tableView), oldPosition.getTableColumn());
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
                    tfm.focus(newPosition.getRow() - 1, tableView.getColumns().get(newPosition.getColumn() - 1));
                });
                break;
            case COLUMN_SPAN_INVISIBLE:
                // If we notice that the new focused cell is the previous one,
                // then it means that we were
                // already on the cell and we wanted to go right.
                if (!tableView.isPressed() && oldPosition.getColumn() == newPosition.getColumn() - 1 && oldPosition.getRow() == newPosition.getRow()) {

                    Platform.runLater(() -> {
                        tfm.focus(oldPosition.getRow(), (TableColumn<S, ?>) getTableColumnSpan(oldPosition, tableView));
                    });
                } else {
                    // If the current focused cell if hidden by column span, we
                    // go left

                    Platform.runLater(() -> {
                        tfm.focus(newPosition.getRow(), tableView.getVisibleLeafColumn(newPosition.getColumn() - 1));
                    });
                }
            default:
                break;
        }
    }

    /**
     * Returns the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param pos the current TablePosition
     * @return a {@link TableColumn}
     */
    static TableColumn<?, ?> getTableColumnSpan(final TablePosition<?, ?> pos, TableView2<?> tableView) {
        return tableView.getVisibleLeafColumn(pos.getColumn() + tableView.getColumnSpan(pos));
    }

    /**
     * Returns the Row number right after the current TablePosition (including
     * the RowSpan to be on a visible Cell)
     *
     * @param pos a {@link TablePosition}
     * @param tableView the {@link TableView2}
     * @return a number
     */
    public static int getNextRowNumber(final TablePosition<?, ?> pos, TableView2<?> tableView) {
        return tableView.getRowSpan(pos, pos.getRow()) + pos.getRow();
    }

//    public static int getPreviousRowNumber(final TablePosition<?, ?> pos, TableView<S> tableView, GridViewSkin skin) {
//        return skin.getFirstRow(tableView.getItems().get(pos.getRow()).get(tableView.getColumns().indexOf(pos.getTableColumn())), pos.getRow()) - 1;
//    }
}
