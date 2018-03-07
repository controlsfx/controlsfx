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

import com.sun.javafx.scene.control.skin.TableCellSkin;
import java.util.UUID;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import org.controlsfx.control.spreadsheet.Filter;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCell.CornerPosition;

/**
 *
 * This is the skin for the {@link CellView}.
 *
 * Its main goal is to draw an object (a triangle) on cells which have their
 * {@link SpreadsheetCell#commentedProperty()} set to true.
 *
 */
public class CellViewSkin extends TableCellSkin<ObservableList<SpreadsheetCell>, SpreadsheetCell> {

    /**
     * This EventType can be used with an {@link EventHandler} in order to catch
     * when a SpreadsheetCell filter is activated/deactivated on this column.
     */
    public static final EventType<Event> FILTER_EVENT_TYPE 
            = new EventType<>("FilterEventType" + UUID.randomUUID().toString()); //$NON-NLS-1$

    private final static String TOP_LEFT_CLASS = "top-left"; //$NON-NLS-1$
    private final static String TOP_RIGHT_CLASS = "top-right"; //$NON-NLS-1$
    private final static String BOTTOM_RIGHT_CLASS = "bottom-right"; //$NON-NLS-1$
    private final static String BOTTOM_LEFT_CLASS = "bottom-left"; //$NON-NLS-1$
    /**
     * The size of the edge of the triangle FIXME Handling of static variable
     * will be changed.
     */
    private static final int TRIANGLE_SIZE = 8;
    /**
     * The region we will add on the cell when necessary.
     */
    private Region topLeftRegion = null;
    private Region topRightRegion = null;
    private Region bottomRightRegion = null;
    private Region bottomLeftRegion = null;
    private MenuButton filterButton = null;

    public CellViewSkin(CellView tableCell) {
        super(tableCell);
        tableCell.itemProperty().addListener(weakItemChangeListener);
        tableCell.tableColumnProperty().addListener(weakColumnChangeListener);
        tableCell.getTableColumn().addEventHandler(FILTER_EVENT_TYPE, weakTriangleEventHandler);
        if (tableCell.getItem() != null) {
            tableCell.getItem().addEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, weakTriangleEventHandler);
        }
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        /**
         * If we have an Image in the Cell, its fitHeight will be affected by
         * the cell height (see CellView). But during calculation for autofit
         * option, we want to know the real prefHeight of this cell. Apparently,
         * the fitHeight option is returned by default so we must override and
         * return the Height of the image inside.
         */
        Node graphic = getSkinnable().getGraphic();
        if (graphic != null && graphic instanceof ImageView) {
            ImageView view = (ImageView) graphic;
            if (view.getImage() != null) {
                return view.getImage().getHeight();
            }
        }
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected void layoutChildren(double x, final double y, final double w, final double h) {
        double width = w;
        Filter filter = ((CellView) getSkinnable()).getFilter();
        boolean change = handleFilter(x, y, w, h, filter);

        //We don't want the filter icon to overlap the cell text.
        if (filter != null && getSkinnable().getItem() != null) {
            width = width - filter.getMenuButton().getWidth();
        }
        /**
         * If a filter change has been done, we want to set the invalidText of
         * LabeledSkinBase to true. If this is not done, the text width is not
         * recomputed.
         */
        if(change){
            handleControlPropertyChanged("WIDTH");
        }
        super.layoutChildren(x, y, width, h);
        layoutTriangle();
    }

    private void layoutTriangle() {
        SpreadsheetCell cell = getSkinnable().getItem();
        if (cell != null) {
            handleTopLeft(cell);
            handleTopRight(cell);
            handleBottomLeft(cell);
            handleBottomRight(cell);

            getSkinnable().requestLayout();
        }
    }

    /**
     * Return true if a change in the filters has been operated (new filter,
     * removed filter).
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param filter
     * @return
     */
    private boolean handleFilter(double x, final double y, final double w, final double h, Filter filter) {
        if (getSkinnable().getItem() == null) {
            return false;
        }
        if (filter != null) {
            //We first remove it.
            MenuButton previousButton = filterButton;
            removeMenuButton();
            filterButton = filter.getMenuButton();
            if (!getChildren().contains(filterButton)) {
                getChildren().add(filterButton);
            }
            layoutInArea(filterButton, x, y, w, h, 0, HPos.RIGHT, VPos.BOTTOM);
            return previousButton != filterButton;
        } else if (filterButton != null) {
            removeMenuButton();
            return true;
        }
        return false;
    }

    private void removeMenuButton() {
        if (filterButton != null && getChildren().contains(filterButton)) {
            getChildren().remove(filterButton);
            filterButton = null;
        }
    }

    private void handleTopLeft(SpreadsheetCell cell) {
        if (cell.isCornerActivated(CornerPosition.TOP_LEFT)) {
            if (topLeftRegion == null) {
                topLeftRegion = getRegion(CornerPosition.TOP_LEFT);
            }
            if (!getChildren().contains(topLeftRegion)) {
                getChildren().add(topLeftRegion);
            }
            //We do not wants snappedTopInset because it takes the padding in consideration!
            topLeftRegion.relocate(0, 0);
        } else if (topLeftRegion != null) {
            getChildren().remove(topLeftRegion);
            topLeftRegion = null;
        }
    }

    private void handleTopRight(SpreadsheetCell cell) {
        if (cell.isCornerActivated(CornerPosition.TOP_RIGHT)) {
            if (topRightRegion == null) {
                topRightRegion = getRegion(CornerPosition.TOP_RIGHT);
            }
            if (!getChildren().contains(topRightRegion)) {
                getChildren().add(topRightRegion);
            }
            //We do not wants snappedTopInset because it takes the padding in consideration!
            topRightRegion.relocate(getSkinnable().getWidth() - TRIANGLE_SIZE, 0);
        } else if (topRightRegion != null) {
            getChildren().remove(topRightRegion);
            topRightRegion = null;
        }
    }

    private void handleBottomRight(SpreadsheetCell cell) {
        if (cell.isCornerActivated(CornerPosition.BOTTOM_RIGHT)) {
            if (bottomRightRegion == null) {
                bottomRightRegion = getRegion(CornerPosition.BOTTOM_RIGHT);
            }
            if (!getChildren().contains(bottomRightRegion)) {
                getChildren().add(bottomRightRegion);
            }
            bottomRightRegion.relocate(getSkinnable().getWidth() - TRIANGLE_SIZE, getSkinnable().getHeight() - TRIANGLE_SIZE);
        } else if (bottomRightRegion != null) {
            getChildren().remove(bottomRightRegion);
            bottomRightRegion = null;
        }
    }

    private void handleBottomLeft(SpreadsheetCell cell) {
        if (cell.isCornerActivated(CornerPosition.BOTTOM_LEFT)) {
            if (bottomLeftRegion == null) {
                bottomLeftRegion = getRegion(CornerPosition.BOTTOM_LEFT);
            }
            if (!getChildren().contains(bottomLeftRegion)) {
                getChildren().add(bottomLeftRegion);
            }
            bottomLeftRegion.relocate(0, getSkinnable().getHeight() - TRIANGLE_SIZE);
        } else if (bottomLeftRegion != null) {
            getChildren().remove(bottomLeftRegion);
            bottomLeftRegion = null;
        }
    }

    private static Region getRegion(CornerPosition position) {
        Region region = new Region();
        region.resize(TRIANGLE_SIZE, TRIANGLE_SIZE);
        region.getStyleClass().add("cell-corner"); //$NON-NLS-1$
        switch (position) {
            case TOP_LEFT:
                region.getStyleClass().add(TOP_LEFT_CLASS);
                break;
            case TOP_RIGHT:
                region.getStyleClass().add(TOP_RIGHT_CLASS);
                break;
            case BOTTOM_RIGHT:
                region.getStyleClass().add(BOTTOM_RIGHT_CLASS);
                break;
            case BOTTOM_LEFT:
                region.getStyleClass().add(BOTTOM_LEFT_CLASS);
                break;

        }

        return region;
    }

    private final EventHandler<Event> triangleEventHandler = new EventHandler<Event>() {

        @Override
        public void handle(Event event) {
            getSkinnable().requestLayout();
        }
    };
    private final WeakEventHandler weakTriangleEventHandler = new WeakEventHandler(triangleEventHandler);

    private final ChangeListener<SpreadsheetCell> itemChangeListener = new ChangeListener<SpreadsheetCell>() {
        @Override
        public void changed(ObservableValue<? extends SpreadsheetCell> arg0, SpreadsheetCell oldCell,
                SpreadsheetCell newCell) {
            if (oldCell != null) {
                oldCell.removeEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, weakTriangleEventHandler);
            }
            if (newCell != null) {
                newCell.addEventHandler(SpreadsheetCell.CORNER_EVENT_TYPE, weakTriangleEventHandler);
            }
            if (getSkinnable().getItem() != null) {
                layoutTriangle();
            }
        }
    };
    private final WeakChangeListener<SpreadsheetCell> weakItemChangeListener = new WeakChangeListener<>(itemChangeListener);

    private final ChangeListener<TableColumn> columnChangeListener = new ChangeListener<TableColumn>() {
        @Override
        public void changed(ObservableValue<? extends TableColumn> arg0, TableColumn oldCell,
                TableColumn newCell) {
            if (oldCell != null) {
                oldCell.removeEventHandler(FILTER_EVENT_TYPE, weakTriangleEventHandler);
            }
            if (newCell != null) {
                newCell.addEventHandler(FILTER_EVENT_TYPE, weakTriangleEventHandler);
            }
        }
    };
    private final WeakChangeListener<TableColumn> weakColumnChangeListener = new WeakChangeListener<>(columnChangeListener);
}
