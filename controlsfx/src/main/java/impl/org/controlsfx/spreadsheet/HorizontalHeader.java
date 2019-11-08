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

import static impl.org.controlsfx.spreadsheet.GridViewSkin.DEFAULT_CELL_HEIGHT;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 * The set of horizontal (column) headers.
 */
public class HorizontalHeader extends TableHeaderRow {

    final GridViewSkin gridViewSkin;
    private int lastColumnResized = -1;

    // Indicate whether the this HorizontalHeader is activated or not
    private boolean working = true;
    /**
     * When the columns header are clicked, we consider the column as selected.
     * This BitSet is reset when a modification on cells is done.
     */
    protected BitSet selectedColumns = new BitSet();

    /***************************************************************************
     * 
     * Constructor
     * 
     **************************************************************************/
    public HorizontalHeader(final GridViewSkin skin) {
        super(skin);
        this.gridViewSkin = skin;

        /**
         * We want to resize all other selected columns when we resize one.
         *
         * I cannot really determine when a resize is finished. Apparently, when
         * this variable Layout is set to 0, it means the drag is done, so until
         * a better solution is shown, it will do the trick.
         */
        //FIXME Moving it here but this trick is no longer working
//        reorderingProperty().addListener(new ChangeListener<Boolean>() {
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
//                HorizontalHeader headerRow = (HorizontalHeader) gridViewSkin.getHorizontalHeader();
//                GridViewSkin mySkin = gridViewSkin;
//                if (!t1 && lastColumnResized >= 0) {
//                    if (headerRow.selectedColumns.get(lastColumnResized)) {
//                        double width1 = mySkin.getColumns().get(lastColumnResized).getWidth();
//                        for (int i = headerRow.selectedColumns.nextSetBit(0); i >= 0; i = headerRow.selectedColumns.nextSetBit(i + 1)) {
//                            mySkin.getColumns().get(i).setPrefWidth(width1);
//                        }
//                    }
//                }
//            }
//        });
    }

    /**************************************************************************
     * 
     * Public API
     * 
     **************************************************************************/
    public void init() {
        SpreadsheetView spv = gridViewSkin.handle.getView();
        updateHorizontalHeaderVisibility(spv.isShowColumnHeader());

        //Visibility of vertical Header listener
        spv.showRowHeaderProperty().addListener(verticalHeaderListener);
        gridViewSkin.verticalHeader.verticalHeaderWidthProperty().addListener(verticalHeaderListener);

        //Visibility of horizontal Header listener
        spv.showColumnHeaderProperty().addListener(horizontalHeaderVisibilityListener);

        //Selection listener to highlight header
        gridViewSkin.getSelectedColumns().addListener(selectionListener);

        //Fixed Column listener to change style of header
        spv.getFixedColumns().addListener(fixedColumnsListener);

        Platform.runLater(() -> {
            //We are doing that because some columns may be already fixed.
            for (SpreadsheetColumn column : spv.getFixedColumns()) {
                fixColumn(column);
            }
            requestLayout();
            /**
             * Clicking on header select the whole column.
             */
            installHeaderMouseEvent();
        });

        /**
         * When we are setting a new Grid (model) on the SpreadsheetView, it
         * appears that the headers are re-created. So we need to listen to
         * those changes in order to re-apply our css style class. Otherwise
         * we'll end up with fixedColumns but no graphic confirmation.
         */
        getRootHeader().getColumnHeaders().addListener((Observable o) -> {
            for (SpreadsheetColumn fixItem : spv.getFixedColumns()) {
                fixColumn(fixItem);
            }
            updateHighlightSelection();
            installHeaderMouseEvent();
        });
    }

    void clearSelectedColumns(){
        selectedColumns.clear();
    }
    /**************************************************************************
     * 
     * Protected methods
     * 
     **************************************************************************/
    @Override
    protected void updateTableWidth() {
        super.updateTableWidth();
        // snapping added for RT-19428
        double padding = 0;

        if (working && gridViewSkin != null
                && gridViewSkin.spreadsheetView != null
                && gridViewSkin.spreadsheetView.showRowHeaderProperty().get()
                && gridViewSkin.verticalHeader != null) {
            padding += gridViewSkin.verticalHeader.getVerticalHeaderWidth();
        }

        Rectangle clip = ((Rectangle) getClip());
        
        clip.setWidth(clip.getWidth() == 0 ? 0 : clip.getWidth() - padding);
    }

    @Override
    protected void updateScrollX() {
        super.updateScrollX();
        gridViewSkin.horizontalPickers.updateScrollX();
        
        if (working) {
            requestLayout();
            ((HorizontalHeaderColumn)getRootHeader()).layoutFixedColumns();
        }
    }

    @Override
    protected NestedTableColumnHeader createRootHeader() {
        return new HorizontalHeaderColumn(null);
    }

    /**************************************************************************
     * 
     * Private methods.
     * 
     **************************************************************************/
    
    /**
     * When we click on header, we want to select the whole column.
     */
    private void installHeaderMouseEvent() {
        for (final TableColumnHeader columnHeader : getRootHeader().getColumnHeaders()) {
            EventHandler<MouseEvent> mouseEventHandler = (MouseEvent mouseEvent) -> {
                if (mouseEvent.isPrimaryButtonDown()) {
                    headerClicked((TableColumn) columnHeader.getTableColumn(), mouseEvent);
                }
            };
            columnHeader.getChildrenUnmodifiable().get(0).setOnMousePressed(mouseEventHandler);
        }
    }
    /**
     * If a header is clicked, we must select the whole column. If Control key of
     * Shift key is pressed, we must not deselect the previous selection but
     * just act like the {@link GridViewBehavior} would.
     *
     * @param column
     * @param event
     */
    private void headerClicked(TableColumn column, MouseEvent event) {
        TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = gridViewSkin.handle.getGridView().getSelectionModel();
        int lastRow = gridViewSkin.getItemCount() - 1;
        int indexColumn = column.getTableView().getColumns().indexOf(column);
        TablePosition focusedPosition = sm.getTableView().getFocusModel().getFocusedCell();
        if (event.isShortcutDown()) {
            BitSet tempSet = (BitSet) selectedColumns.clone();
            sm.selectRange(0, column, lastRow, column);
            selectedColumns.or(tempSet);
            selectedColumns.set(indexColumn);
        } else if (event.isShiftDown() && focusedPosition != null && focusedPosition.getTableColumn() != null) {
            sm.clearSelection();
            sm.selectRange(0, column, lastRow, focusedPosition.getTableColumn());
            sm.getTableView().getFocusModel().focus(0, focusedPosition.getTableColumn());
            int min = Math.min(indexColumn, focusedPosition.getColumn());
            int max = Math.max(indexColumn, focusedPosition.getColumn());
            selectedColumns.set(min, max + 1);
        } else {
            sm.clearSelection();
            sm.selectRange(0, column, lastRow, column);
            //And we want to have the focus on the first cell in order to be able to copy/paste between columns.
            sm.getTableView().getFocusModel().focus(0, column);
            selectedColumns.set(indexColumn);
        }
    }
    /**
     * Whether the Vertical Header is showing, we need to update the width
     * because some space on the left will be available/used.
     */
    private final InvalidationListener verticalHeaderListener = new InvalidationListener() {

        @Override
        public void invalidated(Observable observable) {
            updateTableWidth();
        }
    };

    /**
     * Whether the Horizontal Header is showing, we need to toggle its
     * visibility.
     */
    private final ChangeListener<Boolean> horizontalHeaderVisibilityListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
            updateHorizontalHeaderVisibility(arg2);
        }
    };

    /**
     * When we freeze/unfreeze some columns, we change the style of the Label header
     * text
     */
    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {

        @Override
        public void onChanged(javafx.collections.ListChangeListener.Change<? extends SpreadsheetColumn> change) {
            while (change.next()) {
               //If we unfix a column
               for (SpreadsheetColumn remitem : change.getRemoved()) {
                   unfixColumn(remitem);
               }
               //If we freeze one
               for (SpreadsheetColumn additem : change.getAddedSubList()) {
                   fixColumn(additem);
               }
            }
            updateHighlightSelection();
        }
    };

    /**
     * Fix this column regarding the style
     *
     * @param column
     */
    private void fixColumn(SpreadsheetColumn column) {
        addStyleHeader(gridViewSkin.spreadsheetView.getViewColumn(gridViewSkin.spreadsheetView.getColumns().indexOf(column)));
    }

    /**
     * Unfix this column regarding the style
     *
     * @param column
     */
    private void unfixColumn(SpreadsheetColumn column) {
         try{
            removeStyleHeader(gridViewSkin.spreadsheetView.getViewColumn(gridViewSkin.spreadsheetView.getColumns().indexOf(column)));
        } catch (ConcurrentModificationException ex) {
            //It may happen...
            //FIXME
//            Logger.getLogger("root").log(Level.WARNING, "", ex);
        }
    }

    /**
     * Add the freeze style of the header Label of the specified column
     *
     * @param i
     */
    private void removeStyleHeader(Integer i) {
        if (getRootHeader().getColumnHeaders().size() > i) {
            getRootHeader().getColumnHeaders().get(i).getStyleClass().removeAll("fixed"); //$NON-NLS-1$
        }
    }

    /**
     * Remove the freeze style of the header Label of the specified column
     *
     * @param i
     */
    private void addStyleHeader(Integer i) {
        if (getRootHeader().getColumnHeaders().size() > i) {
            getRootHeader().getColumnHeaders().get(i).getStyleClass().addAll("fixed"); //$NON-NLS-1$
        }
    }

    /**
     * When we select some cells, we want the header to be highlighted
     */
    private final InvalidationListener selectionListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable valueModel) {
            updateHighlightSelection();
        }
    };

    /**
     * Highlight the header Label when selection change.
     */
    private void updateHighlightSelection() {
        for (final TableColumnHeader i : getRootHeader().getColumnHeaders()) {
            i.getStyleClass().removeAll("selected"); //$NON-NLS-1$

        }
        final List<Integer> selectedColumns = gridViewSkin.getSelectedColumns();
        for (final Integer i : selectedColumns) {
            if (getRootHeader().getColumnHeaders().size() > i) {
                getRootHeader().getColumnHeaders().get(i).getStyleClass()
                        .addAll("selected"); //$NON-NLS-1$
            }
        }

    }

    private void updateHorizontalHeaderVisibility(boolean visible) {
        working = visible;
        setManaged(working);
        if (!visible) {
            getStyleClass().add("invisible"); //$NON-NLS-1$
        } else {
            getStyleClass().remove("invisible"); //$NON-NLS-1$
            requestLayout();
            ((HorizontalHeaderColumn)getRootHeader()).layoutFixedColumns();
            updateHighlightSelection();
        }
    }
   
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
        if (!gridViewSkin.handle.getView().isShowColumnHeader()) {
            return 0.0;
        }
        // we hardcode 24.0 here to avoid RT-37616, where the
        // entire header row would disappear when all columns were hidden.
        double headerPrefHeight = getRootHeader().prefHeight(width);
        headerPrefHeight = headerPrefHeight == 0.0 ? 24.0 : headerPrefHeight;
        double height = snappedTopInset() + headerPrefHeight + snappedBottomInset();
        height = height < DEFAULT_CELL_HEIGHT ? DEFAULT_CELL_HEIGHT : height;
        return height;
    }
}
