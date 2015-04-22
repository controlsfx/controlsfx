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

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;

import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.WindowEvent;
import org.controlsfx.control.spreadsheet.Picker;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 * Display the vertical header on the left of the cells (view), the index of the
 * lines displayed on screen.
 */
public class VerticalHeader extends StackPane {

    public static final int PICKER_SIZE = 16;
    private static final int DRAG_RECT_HEIGHT = 5;
    private static final String TABLE_ROW_KEY = "TableRow"; //$NON-NLS-1$
    private static final String PICKER_INDEX = "PickerIndex"; //$NON-NLS-1$
    private static final String TABLE_LABEL_KEY = "Label"; //$NON-NLS-1$
    private static final Image pinImage = new Image(SpreadsheetView.class.getResource("pinSpreadsheetView.png").toExternalForm()); //$NON-NLS-1$

    /**
     * *************************************************************************
     * * Private Fields * *
     * ************************************************************************
     */
    private final SpreadsheetHandle handle;
    private final SpreadsheetView spreadsheetView;
    private double horizontalHeaderHeight;
    /**
     * This represents the VerticalHeader width. It's the total amount of space
     * used by the VerticalHeader. It's composed of the sum of the
     * SpreadsheetView {@link SpreadsheetView#getRowHeaderWidth() } and the size
     * of the pickers (which is fixed right now).
     *
     */
    private final DoubleProperty innerVerticalHeaderWidth = new SimpleDoubleProperty();
    private Rectangle clip; // Ensure that children do not go out of bounds
    private ContextMenu blankContextMenu;

    // used for column resizing
    private double lastY = 0.0F;
    private static double dragAnchorY = 0.0;

    // drag rectangle overlays
    private final List<Rectangle> dragRects = new ArrayList<>();

    private final List<Label> labelList = new ArrayList<>();
    private GridViewSkin skin;
    private boolean resizing = false;

    private final Stack<Label> pickerPile;
    private final Stack<Label> pickerUsed;

    /**
     * ****************************************************************
     * CONSTRUCTOR
     *
     * @param handle
     * ***************************************************************
     */
    public VerticalHeader(final SpreadsheetHandle handle) {
        this.handle = handle;
        this.spreadsheetView = handle.getView();
        pickerPile = new Stack<>();
        pickerUsed = new Stack<>();
    }

    /**
     * *************************************************************************
     * * Private/Protected Methods *
     * ***********************************************************************
     */
    /**
     * Init
     *
     * @param skin
     * @param horizontalHeader
     */
    void init(final GridViewSkin skin, HorizontalHeader horizontalHeader) {
        this.skin = skin;
        // Adjust position upon HorizontalHeader height
        horizontalHeader.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number oldHeight, Number newHeight) {
                horizontalHeaderHeight = newHeight.doubleValue();
                requestLayout();
            }
        });

        // When the Grid is changing, we need to update our information.
        handle.getView().gridProperty().addListener(layout);

        // Clip property to stay within bounds
        clip = new Rectangle(getVerticalHeaderWidth(), snapSize(skin.getSkinnable().getHeight()));
        clip.relocate(snappedTopInset(), snappedLeftInset());
        clip.setSmooth(false);
        clip.heightProperty().bind(skin.getSkinnable().heightProperty());
        clip.widthProperty().bind(innerVerticalHeaderWidth);
        VerticalHeader.this.setClip(clip);

        // We desactivate and activate the verticalHeader upon request
        spreadsheetView.showRowHeaderProperty().addListener(layout);

        // When the Column header is showing or not, we need to update the
        // position of the verticalHeader
        spreadsheetView.showColumnHeaderProperty().addListener(layout);
        spreadsheetView.getFixedRows().addListener(layout);
        spreadsheetView.fixingRowsAllowedProperty().addListener(layout);
        spreadsheetView.rowHeaderWidthProperty().addListener(layout);

        // In case we resize the view in any manners
        spreadsheetView.heightProperty().addListener(layout);

        //When rowPickers is changing
        spreadsheetView.getRowPickers().addListener(layout);

        // For layout properly the verticalHeader when there are some selected
        // items
        skin.getSelectedRows().addListener(layout);

        blankContextMenu = new ContextMenu();
    }

    public double getVerticalHeaderWidth() {
        return innerVerticalHeaderWidth.get();
    }

    public double computeHeaderWidth() {
        double width = 0;
        if (!spreadsheetView.getRowPickers().isEmpty()) {
            width += PICKER_SIZE;
        }
        if (spreadsheetView.isShowRowHeader()) {
            width += spreadsheetView.getRowHeaderWidth();
        }
        return width;
    }

    @Override
    protected void layoutChildren() {
        if (resizing) {
            return;
        }
        if ((spreadsheetView.isShowRowHeader() || !spreadsheetView.getRowPickers().isEmpty()) && skin.getCellsSize() > 0) {

            double x = snappedLeftInset();
            /**
             * Pickers
             */
            pickerPile.addAll(pickerUsed.subList(0, pickerUsed.size()));
            pickerUsed.clear();
            if (!spreadsheetView.getRowPickers().isEmpty()) {
                innerVerticalHeaderWidth.setValue(PICKER_SIZE);
                x += PICKER_SIZE;
            } else {
                innerVerticalHeaderWidth.setValue(0);
            }
            if (spreadsheetView.isShowRowHeader()) {
                innerVerticalHeaderWidth.setValue(getVerticalHeaderWidth() + spreadsheetView.getRowHeaderWidth());
            }

            getChildren().clear();

            final int cellSize = skin.getCellsSize();

            int rowCount = 0;
            Label label;

            rowCount = addVisibleRows(rowCount, x, cellSize);

//            if (spreadsheetView.isShowRowHeader()) {
                rowCount = addFixedRows(rowCount, x, cellSize);
//            }
            // First one blank and on top (z-order) of the others
            if (spreadsheetView.showColumnHeaderProperty().get()) {
                label = getLabel(rowCount++, null);
                label.setOnMousePressed((MouseEvent event) -> {
                    spreadsheetView.getSelectionModel().selectAll();
                });
                label.setText(""); //$NON-NLS-1$
                label.resize(spreadsheetView.getRowHeaderWidth(), horizontalHeaderHeight);
                label.layoutYProperty().unbind();
                label.setLayoutY(0);
                label.setLayoutX(x);
                label.getStyleClass().clear();
                label.setContextMenu(blankContextMenu);
                getChildren().add(label);
            }

            VirtualScrollBar hbar = handle.getCellsViewSkin().getHBar();
            //FIXME handle height.
            if (hbar.isVisible()) {
                // Last one blank and on top (z-order) of the others
                label = getLabel(rowCount++, null);
                label.getProperties().put(TABLE_ROW_KEY, null);
                label.setText(""); //$NON-NLS-1$
                label.resize(getVerticalHeaderWidth(), hbar.getHeight());
                label.layoutYProperty().unbind();
                label.relocate(snappedLeftInset(), getHeight() - hbar.getHeight());
                label.getStyleClass().clear();
                label.setContextMenu(blankContextMenu);
                getChildren().add(label);
            }
        } else {
            getChildren().clear();
        }
    }

    private int addFixedRows(int rowCount, double x, int cellSize) {
        double spaceUsedByFixedRows = 0;
        int rowIndex;
        Label label;
        final Set<Integer> currentlyFixedRow = handle.getCellsViewSkin().getCurrentlyFixedRow();
        // Then we iterate over the FixedRows if any
        if (!spreadsheetView.getFixedRows().isEmpty() && cellSize != 0) {
            for (int j = 0; j < spreadsheetView.getFixedRows().size(); ++j) {

                rowIndex = spreadsheetView.getFixedRows().get(j);
                if (!currentlyFixedRow.contains(rowIndex)) {
                    break;
                }
                double rowHeight = skin.getRowHeight(rowIndex);
                double y = spreadsheetView.showColumnHeaderProperty().get() ? snappedTopInset() + horizontalHeaderHeight + spaceUsedByFixedRows
                        : snappedTopInset() + spaceUsedByFixedRows;
                
                if (spreadsheetView.getRowPickers().containsKey(rowIndex)) {
                    Label picker = getPicker(spreadsheetView.getRowPickers().get(rowIndex));
                    picker.resize(PICKER_SIZE, rowHeight);
                    picker.layoutYProperty().unbind();
                    picker.setLayoutY(y);
                    getChildren().add(picker);
                }
                if (spreadsheetView.isShowRowHeader()) {
                    label = getLabel(rowCount++, rowIndex);
                    GridRow row = skin.getRowIndexed(rowIndex);
                    label.getProperties().put(TABLE_ROW_KEY, row);
                    label.setText(getRowHeader(rowIndex));
                    label.resize(spreadsheetView.getRowHeaderWidth(), rowHeight);
                    label.setContextMenu(getRowContextMenu(rowIndex));
                    if(row != null){
                        label.layoutYProperty().bind(row.layoutYProperty().add(horizontalHeaderHeight).add(row.verticalShift.get()));
                    }
                    label.setLayoutX(x);
                    final ObservableList<String> css = label.getStyleClass();
                    if (skin.getSelectedRows().contains(rowIndex)) {
                        css.addAll("selected"); //$NON-NLS-1$
                    } else {
                        css.removeAll("selected"); //$NON-NLS-1$
                    }
                    css.addAll("fixed"); //$NON-NLS-1$
                    getChildren().add(label);
                    // position drag overlay to intercept row resize requests if authorized by the grid.
                    if (spreadsheetView.getGrid().isRowResizable(rowIndex)) {
                        Rectangle dragRect = getDragRect(rowCount++);
                        dragRect.getProperties().put(TABLE_ROW_KEY, row);
                        dragRect.getProperties().put(TABLE_LABEL_KEY, label);
                        dragRect.setWidth(label.getWidth());
                        dragRect.relocate(snappedLeftInset() + x, y + rowHeight - DRAG_RECT_HEIGHT);
                        getChildren().add(dragRect);
                    }
                }
                spaceUsedByFixedRows += skin.getRowHeight(rowIndex);

                
            }
        }
        return rowCount;
    }

    private int addVisibleRows(int rowCount, double x, int cellSize) {
        int rowIndex;
        // We add horizontalHeaderHeight because we need to
        // take the other header into account.
        double y = snappedTopInset();

        if (spreadsheetView.showColumnHeaderProperty().get()) {
            y += horizontalHeaderHeight;
        }

        // The Labels must be aligned with the rows
        if (cellSize != 0) {
            y += skin.getRow(0).getLocalToParentTransform().getTy();
        }

        Label label;
        // We don't want to add Label if there are no rows associated with.
        final int modelRowCount = spreadsheetView.getGrid().getRowCount();

        int i = 0;

        GridRow row = skin.getRow(i);

        double fixedRowHeight = skin.getFixedRowHeight();
        double rowHeaderWidth = spreadsheetView.getRowHeaderWidth();
        double height;

        // We iterate over the visibleRows
        while (cellSize != 0 && row != null && row.getIndex() < modelRowCount) {
            rowIndex = row.getIndex();
            height = row.getHeight();
            /**
             * Picker
             */
            if (row.getLayoutY() >= fixedRowHeight && spreadsheetView.getRowPickers().containsKey(rowIndex)) {
                Label picker = getPicker(spreadsheetView.getRowPickers().get(rowIndex));
                picker.resize(PICKER_SIZE, height);
                picker.layoutYProperty().bind(row.layoutYProperty().add(horizontalHeaderHeight));
                getChildren().add(picker);
            }

            if (spreadsheetView.isShowRowHeader()) {
                label = getLabel(rowCount++, rowIndex);
                label.getProperties().put(TABLE_ROW_KEY, row);
                label.setText(getRowHeader(rowIndex));
                label.resize(rowHeaderWidth, height);
                label.setLayoutX(x);
                label.layoutYProperty().bind(row.layoutYProperty().add(horizontalHeaderHeight));
                label.setContextMenu(getRowContextMenu(rowIndex));

                getChildren().add(label);
                // We want to highlight selected rows
                final ObservableList<String> css = label.getStyleClass();
                if (skin.getSelectedRows().contains(rowIndex)) {
                    css.addAll("selected"); //$NON-NLS-1$
                } else {
                    css.removeAll("selected"); //$NON-NLS-1$
                }
                if (spreadsheetView.getFixedRows().contains(rowIndex)) {
                    css.addAll("fixed"); //$NON-NLS-1$
                } else {
                    css.removeAll("fixed"); //$NON-NLS-1$
                }

                y += height;

                // position drag overlay to intercept row resize requests if authorized by the grid.
                if (spreadsheetView.getGrid().isRowResizable(rowIndex)) {
                    Rectangle dragRect = getDragRect(rowCount++);
                    dragRect.getProperties().put(TABLE_ROW_KEY, row);
                    dragRect.getProperties().put(TABLE_LABEL_KEY, label);
                    dragRect.setWidth(label.getWidth());
                    dragRect.relocate(snappedLeftInset() + x, y - DRAG_RECT_HEIGHT);
                    getChildren().add(dragRect);
                }
            }
            row = skin.getRow(++i);
        }
        return rowCount;
    }

    private final EventHandler<MouseEvent> rectMousePressed = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent me) {

            if (me.getClickCount() == 2 && me.isPrimaryButtonDown()) {
            } else {
                // rather than refer to the rect variable, we just grab
                // it from the source to prevent a small memory leak.
                dragAnchorY = me.getSceneY();
                resizing = true;
            }
            me.consume();
        }
    };

    private final EventHandler<MouseEvent> rectMouseDragged = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent me) {
            Rectangle rect = (Rectangle) me.getSource();
            GridRow row = (GridRow) rect.getProperties().get(TABLE_ROW_KEY);
            Label label = (Label) rect.getProperties().get(TABLE_LABEL_KEY);
            if (row != null) {
                rowResizing(row, label, me);
            }
            me.consume();
        }
    };

    private void rowResizing(GridRow gridRow, Label label, MouseEvent me) {
        double draggedY = me.getSceneY() - dragAnchorY;
        if (gridRow.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            draggedY = -draggedY;
        }

        double delta = draggedY - lastY;

        Double newHeight = gridRow.getHeight() + delta;
        if (newHeight < 0) {
            return;
        }
        handle.getCellsViewSkin().rowHeightMap.put(gridRow.getIndex(), newHeight);
        label.resize(spreadsheetView.getRowHeaderWidth(), newHeight);
        gridRow.setPrefHeight(newHeight);
        gridRow.requestLayout();
        
        lastY = draggedY;
    }

    private final EventHandler<MouseEvent> rectMouseReleased = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent me) {
            lastY = 0.0F;
            resizing = false;
            requestLayout();
            me.consume();
        }
    };

    /**
     * Create a new label and put it in the pile or just grab one from the pile.
     *
     * @param rowNumber
     * @return
     */
    private Label getLabel(int rowNumber, Integer row) {
        Label label;
        if (labelList.isEmpty() || labelList.size() <= rowNumber) {
            label = new Label();
            labelList.add(label);
        } else {
            label = labelList.get(rowNumber);
        }
        // We want to select the whole row when clicking on a header.
        label.setOnMousePressed(row == null ? null : (MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                headerClicked(row, event);
            }
        });
        return label;
    }

    /**
     * If a header is clicked, we must select the whole row. If Control key of
     * Shift key is pressed, we must not deselect the previous selection but
     * just act like the {@link GridViewBehavior} would.
     *
     * @param row
     * @param event
     */
    private void headerClicked(int row, MouseEvent event) {
        TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = handle.getGridView().getSelectionModel();
        int focusedRow = sm.getFocusedIndex();
        int rowCount = handle.getView().getGrid().getRowCount();
        ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> columns = sm.getTableView().getColumns();
        TableColumn<ObservableList<SpreadsheetCell>, ?> firstColumn = columns.get(0);
        TableColumn<ObservableList<SpreadsheetCell>, ?> lastColumn = columns.get(columns.size() - 1);

        if (event.isShortcutDown()) {
            sm.selectRange(row, firstColumn, row, lastColumn);
        } else if (event.isShiftDown() && focusedRow >= 0 && focusedRow < rowCount) {
            sm.clearSelection();
            sm.selectRange(focusedRow, firstColumn, row, lastColumn);
            //We want to let the focus on the focused row.
            sm.getTableView().getFocusModel().focus(focusedRow, firstColumn);
        } else {
            sm.clearSelection();
            sm.selectRange(row, firstColumn, row, lastColumn);
            //And we want to have the focus on the first cell in order to be able to copy/paste between rows.
            sm.getTableView().getFocusModel().focus(row, firstColumn);
        }
    }
    
    private Label getPicker(Picker picker) {
        Label pickerLabel;
        if (pickerPile.isEmpty()) {
            pickerLabel = new Label();
            picker.getStyleClass().addListener(layout);
            pickerLabel.setOnMouseClicked(pickerMouseEvent);
        } else {
            pickerLabel = pickerPile.pop();
        }
        pickerUsed.push(pickerLabel);

        pickerLabel.getStyleClass().setAll(picker.getStyleClass());
        pickerLabel.getProperties().put(PICKER_INDEX, picker);
        return pickerLabel;
    }

    private final EventHandler<MouseEvent> pickerMouseEvent = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent mouseEvent) {
            Label picker = (Label) mouseEvent.getSource();

            ((Picker) picker.getProperties().get(PICKER_INDEX)).onClick();
        }
    };

    /**
     * Create a new Rectangle and put it in the pile or just grab one from the
     * pile.
     *
     * @param rowNumber
     * @return
     */
    private Rectangle getDragRect(int rowNumber) {
        if (dragRects.isEmpty() || dragRects.size() <= rowNumber) {
            final Rectangle rect = new Rectangle();
            rect.setWidth(getVerticalHeaderWidth());
            rect.setHeight(DRAG_RECT_HEIGHT);
            rect.setFill(Color.TRANSPARENT);
            rect.setSmooth(false);
            rect.setOnMousePressed(rectMousePressed);
            rect.setOnMouseDragged(rectMouseDragged);
            rect.setOnMouseReleased(rectMouseReleased);
            rect.setCursor(Cursor.V_RESIZE);
            dragRects.add(rect);
            return rect;
        } else {
            return dragRects.get(rowNumber);
        }
    }

    /**
     * Return a contextMenu for fixing a row if possible.
     *
     * @param row
     * @return
     */
    private ContextMenu getRowContextMenu(final Integer row) {
        if (spreadsheetView.isRowFixable(row)) {
            final ContextMenu contextMenu = new ContextMenu();

            MenuItem fixItem = new MenuItem(localize(asKey("spreadsheet.verticalheader.menu.fix"))); //$NON-NLS-1$
            contextMenu.setOnShowing(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent event) {
                    if (spreadsheetView.getFixedRows().contains(row)) {
                        fixItem.setText(localize(asKey("spreadsheet.verticalheader.menu.unfix"))); //$NON-NLS-1$
                    } else {
                        fixItem.setText(localize(asKey("spreadsheet.verticalheader.menu.fix"))); //$NON-NLS-1$
                    }
                }
            });
            fixItem.setGraphic(new ImageView(pinImage));

            fixItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                    if (spreadsheetView.getFixedRows().contains(row)) {
                        spreadsheetView.getFixedRows().remove(row);
                    } else {
                        spreadsheetView.getFixedRows().add(row);
                    }
                }
            });
            contextMenu.getItems().add(fixItem);

            return contextMenu;
        } else {
            return blankContextMenu;
        }
    }

    /**
     * Return the String header associated with this row index.
     *
     * @param index
     * @return
     */
    private String getRowHeader(int index) {
        return spreadsheetView.getGrid().getRowHeaders().size() > index ? spreadsheetView
                .getGrid().getRowHeaders().get(index) : String.valueOf(index + 1);
    }

    /**
     * *************************************************************************
     * * Listeners * *
     * ************************************************************************
     */
    private final InvalidationListener layout = (Observable arg0) -> {
        requestLayout();
    };
}
