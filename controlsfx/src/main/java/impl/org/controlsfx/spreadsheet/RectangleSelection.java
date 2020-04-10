/**
 * Copyright (c) 2014, 2018 ControlsFX
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

import java.util.List;
import java.util.TreeSet;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.spreadsheet.ClipboardCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 *
 * This class extends Rectangle and will draw a rectangle with a border to the
 * selection.
 */
public class RectangleSelection extends Rectangle {

    private final GridViewSkin skin;
    private final TableViewSpanSelectionModel sm;
    private final SelectionRange selectionRange;

    public RectangleSelection(GridViewSkin skin, TableViewSpanSelectionModel sm) {
        this.skin = skin;
        this.sm = sm;
        getStyleClass().add("selection-rectangle"); //$NON-NLS-1$
        setMouseTransparent(true);

        selectionRange = new SelectionRange();
        this.selectedCellListener = (Observable observable) -> {
            skin.getHorizontalHeader().clearSelectedColumns();
            skin.verticalHeader.clearSelectedRows();
            selectionRange.fill(sm.getSelectedCells(), skin.spreadsheetView);
            updateRectangle();
        };
        skin.getVBar().valueProperty().addListener(layoutListener);

        //When draging, it's not working properly so we remove the rectangle.
        skin.getVBar().addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedListener);

        // When playing with the hidden indexes, we must recomputer the range because it is wrong.
        skin.spreadsheetView.hiddenRowsProperty().addListener(selectedCellListener);
        skin.spreadsheetView.hiddenColumnsProperty().addListener(selectedCellListener);
        skin.getHBar().valueProperty().addListener(layoutListener);
        sm.getSelectedCells().addListener(selectedCellListener);
    }

    private final InvalidationListener selectedCellListener;
    private final InvalidationListener layoutListener = (Observable observable) -> {
        updateRectangle();
    };

    private final EventHandler<MouseEvent> mouseDraggedListener = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            skin.getVBar().valueProperty().removeListener(layoutListener);
            setVisible(false);
            skin.getVBar().addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {

                @Override
                public void handle(MouseEvent event) {
                    skin.getVBar().removeEventFilter(MouseEvent.MOUSE_RELEASED, this);
                    skin.getVBar().valueProperty().addListener(layoutListener);
                    updateRectangle();
                }
            });
        }
    };

    public final void updateRectangle() {
        if (sm.getSelectedCells().isEmpty()
                || skin.getSelectedRows().isEmpty()
                || skin.getSelectedColumns().isEmpty()
                || selectionRange.range == null) {
            setVisible(false);
            return;
        }

        IndexedCell topRowCell = skin.getFlow().getTopRow();
        if (topRowCell == null) {
            return;
        }
        //We fetch the first and last row currently displayed
        int topRow = topRowCell.getIndex();
        IndexedCell bottomRowCell = skin.getFlow().getCells().get(skin.getFlow().getCells().size() - 1);
        if (bottomRowCell == null) {
            return;
        }
        int bottomRow = bottomRowCell.getIndex();

        int minRow = selectionRange.range.getTop();
        if (minRow > bottomRow) {
            setVisible(false);
            return;
        }
        minRow = Math.max(minRow, topRow);

        int maxRow = selectionRange.range.getBottom();
        if (maxRow < topRow) {
            setVisible(false);
            return;
        }

        maxRow = Math.min(maxRow, bottomRow);
        int minColumn = selectionRange.range.getLeft();
        int maxColumn = selectionRange.range.getRight();

        GridRow gridMinRow = skin.getRowIndexed(minRow);
        if (gridMinRow == null) {
            setVisible(false);
            return;
        }

        if (maxRow >= skin.getItemCount() || maxColumn >= skin.getSkinnable().getVisibleLeafColumns().size()
                || minColumn < 0) {
            setVisible(false);
            return;
        }
        SpreadsheetCell cell = skin.getSkinnable().getItems().get(maxRow).get(skin.spreadsheetView.getModelColumn(maxColumn));
        handleHorizontalPositioning(minColumn, skin.spreadsheetView.getViewColumn(cell.getColumn()) + skin.spreadsheetView.getColumnSpan(cell) - 1);

        //If we are out of sight
        if (getX() + getWidth() < 0) {
            setVisible(false);
            return;
        }

        GridRow gridMaxRow = skin.getRowIndexed(maxRow);
        if (gridMaxRow == null) {
            setVisible(false);
            return;
        }
        setVisible(true);

        handleVerticalPositioning(minRow, maxRow, gridMinRow, gridMaxRow);
    }

    /**
     * This will compute and assign the y and height properties of the
     * rectangle.
     *
     * @param minRow
     * @param maxRow
     * @param gridMinRow
     */
    private void handleVerticalPositioning(int minRow, int maxRow, GridRow gridMinRow, GridRow gridMaxRow) {
        double height = 0;
        for (int i = maxRow; i <= maxRow /*+ (rowSpan - 1)*/; ++i) {
            height += skin.getRowHeight(i);
        }

        /**
         * If we are not in fixed row, we will just take the layout Y, and if
         * it's below some of our fixed rows, we will take the fixedRowheight as
         * value.
         */
        if (!skin.getCurrentlyFixedRow().contains(minRow)) {
            yProperty().unbind();
            //If we have fixedRows, we do not want to overlap them with the rectangle.
            if (gridMinRow.getLayoutY() < skin.getFixedRowHeight()) {
                setY(skin.getFixedRowHeight());
            } else {
                yProperty().bind(gridMinRow.layoutYProperty());
            }
            /**
             * If we are in fixedRow, we cannot trust the layoutY alone. We also
             * need to rely on the verticalShift for shifting the rectangle to
             * the right starting position.\n
             *
             */
        } else {
            yProperty().bind(gridMinRow.layoutYProperty().add(gridMinRow.verticalShift));
        }

        /**
         * Finally we compute the height by subtracting our starting point to
         * the ending point.
         */
        heightProperty().bind(gridMaxRow.layoutYProperty().add(gridMaxRow.verticalShift).subtract(yProperty()).add(height));
    }

    /**
     * This will compute and assign the x and width propertis of the Rectangle.
     *
     * @param minColumn
     * @param maxColumn
     */
    private void handleHorizontalPositioning(int minColumn, int maxColumn) {
        double x = 0;

        final List<TableColumn<ObservableList<SpreadsheetCell>, ?>> visibleColumns = skin.handle.getGridView().getVisibleLeafColumns();
        final List<TableColumn<ObservableList<SpreadsheetCell>, ?>> allColumns = skin.handle.getGridView().getColumns();
        final List<SpreadsheetColumn> columns = skin.spreadsheetView.getColumns();
        if (visibleColumns.size() <= minColumn || visibleColumns.size() <= maxColumn) {
            return;
        }
        //We first compute the total space between the left edge and our first column
        for (int i = 0; i < minColumn; ++i) {
            //Here we use Ceil because we want to "snapSize" otherwise we may end up with a weird shift.
            x += snapSize(visibleColumns.get(i).getWidth());
        }

        /**
         * We then substract the value of the Hbar in order to place it properly
         * because 0 means the left edge or the SpreadsheetView and we want to
         * consider the left edge of the viewport of the virtualFlow.
         */
        x -= skin.getHBar().getValue();

        //Then we compute the width by adding the space between the min and max column
        double width = 0;
        for (int i = minColumn; i <= maxColumn /*+ (columnSpan - 1)*/; ++i) {
            width += snapSize(visibleColumns.get(i).getWidth());
        }

        //FIXED COLUMNS
        /**
         * If the selection is not on a fixed column, we may have the case where
         * the first selected cell will be hid by a fixed column. If so, we must
         * translate the starting point in because the rectangle must also be
         * hidden by the fixed column.
         */
        int col = skin.spreadsheetView.getModelColumn(minColumn);
        if (col < columns.size() && !skin.spreadsheetView.getFixedColumns().contains(columns.get(col))) {
            if (x < skin.fixedColumnWidth) {
                //Since I translate the starting point, I must reduce the width by the value I'm translating.
                width -= skin.fixedColumnWidth - x;
                x = skin.fixedColumnWidth;
            }
            /**
             * If the maxColumn is contained within the fixed column, we may
             * look at the starting point and the ending point. Because prior
             * computation are wrong since our columns are fixed on the left. So
             * there initial position are worthless and we must consider their
             * current position compared to each other.
             *
             */
        } else /**
         * If x + width is inferior, we can re-compute the width by checking our
         * fixed columns interval
         */
        {
            if (x + width < skin.fixedColumnWidth) {
                x = 0;
                width = 0;
                for (SpreadsheetColumn column : skin.spreadsheetView.getFixedColumns()) {
                    int indexColumn = skin.spreadsheetView.getViewColumn(columns.indexOf(column));
                    if (indexColumn < minColumn && indexColumn != minColumn) {
                        x += snapSize(column.getWidth());
                    }
                    if (indexColumn >= minColumn && indexColumn <= maxColumn) {
                        width += snapSize(column.getWidth());
                    }
                }
                /**
                 * If just x is inferior to fixedColumnWidth, we just adjust the
                 * width by substracting the gap between the original x and the
                 * new x.
                 */
            } else if (x < skin.fixedColumnWidth) {
                double tempX = 0;
                for (SpreadsheetColumn column : skin.spreadsheetView.getFixedColumns()) {
                    int indexColumn = skin.spreadsheetView.getViewColumn(columns.indexOf(column));
                    if (indexColumn < minColumn && indexColumn != minColumn) {
                        tempX += snapSize(column.getWidth());
                    }
                }
                width -= tempX - x;
                x = tempX;
            }
        }
        setX(x);
        setWidth(width);
    }

    /**
     * Returns a value ceiled to the nearest pixel.
     *
     * @param value the size value to be snapped
     * @return value ceiled to nearest pixel
     */
    private double snapSize(double value) {
        return Math.ceil(value);
    }

    /**
     * Utility class to transform a list of selected cells into a union of
     * ranges.
     */
    public static class SelectionRange {

        private final TreeSet<Long> set = new TreeSet<>();
        private GridRange range;

        public SelectionRange() {
        }

        public void fill(List<TablePosition> list) {
            set.clear();
            for (TablePosition pos : list) {
                long key = key(pos.getRow(), pos.getColumn());
                set.add(key);
            }
            computeRange();
        }

        /**
         * Construct a SelectionRange with a List of Pair where the value is the
         * row and the value is column.
         *
         * @param list
         * @param spv
         */
        public void fill(List<TablePosition> list, SpreadsheetView spv) {
            set.clear();
            range = null;
            for (TablePosition pos : list) {
                long key = key(pos.getRow(), pos.getColumn());
                set.add(key);
                //I just check that a selected cell is not against it.
                if (!spv.getGrid().isCellDisplaySelection(spv.getModelRow(pos.getRow()), spv.getModelColumn(pos.getColumn()))) {
                    return;
                }
            }

            computeRange();
        }

        /**
         * Fills this ClipBoardRange with a list a {@code ClipboardCell}. The
         * result can be accessed with the {@link #getRange() } method.
         *
         * @param list the list a {@code ClipboardCell}
         */
        public void fillClipboardRange(List<ClipboardCell> list) {
            set.clear();
            range = null;
            for (ClipboardCell pos : list) {
                set.add(key(pos.getRow(), pos.getColumn()));
            }
            computeRange();
        }

        public GridRange getRange() {
            return range;
        }

        public static Long key(int row, int column) {
            return (((long) row) << 32) | column;
        }

        private int getRow(Long l) {
            return (int) (l >> 32);
        }

        private int getColumn(Long l) {
            return (int) (l & 0xffFFffFF);
        }

        /**
         * return a list of WsGridRange
         *
         * @return
         */
        private void computeRange() {
            range = null;
            while (!set.isEmpty()) {
                if (range != null) {
                    range = null;
                    return;
                }

                long first = set.first();
                set.remove(first);

                int row = getRow(first);
                int column = getColumn(first);

                //Go in row
                while (set.contains(key(row, column + 1))) {
                    ++column;
                    set.remove(key(row, column));
                }

                //Go in column
                boolean flag = true;
                while (flag) {
                    ++row;
                    for (int col = getColumn(first); col <= column; ++col) {
                        if (!set.contains(key(row, col))) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        for (int col = getColumn(first); col <= column; ++col) {
                            set.remove(key(row, col));
                        }
                    } else {
                        --row;
                    }
                }
                range = new GridRange(getRow(first), row, getColumn(first), column);
            }
        }
    }

    public static class GridRange {

        private final int top;
        private final int bottom;
        private final int left;
        private final int right;

        public GridRange(int top, int bottom, int left, int right) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public int getBottom() {
            return bottom;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }
    }
}
