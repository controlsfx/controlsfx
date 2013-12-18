/**
 * Copyright (c) 2013, ControlsFX
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
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.VirtualScrollBar;

/**
 * Display the vertical header on the left of the cells (view), the index of the
 * lines displayed on screen.
 */
public class VerticalHeader extends StackPane {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final SpreadsheetHandle handle;
    private final SpreadsheetView spreadsheetView;
    private double horizontalHeaderHeight;
    private DoubleProperty verticalHeaderWidth;
    private Double savedWidth;
    private boolean working = true; // Whether or not we are showing the
                                    // verticalHeader
    private Rectangle clip; // Ensure that children do not go out of bounds
    private ContextMenu blankContextMenu;

    /***************************************************************************
     * * Listeners * *
     **************************************************************************/
    private final InvalidationListener layout = new InvalidationListener() {
        @Override
        public void invalidated(Observable arg0) {
            if (working) {
                requestLayout();
            }
        }
    };

    /******************************************************************
     * CONSTRUCTOR
     * 
     * @param handle
     * @param verticalHeaderWidth
     ******************************************************************/
    public VerticalHeader(final SpreadsheetHandle handle, DoubleProperty verticalHeaderWidth) {
        this.handle = handle;
        this.spreadsheetView = handle.getView();
        this.verticalHeaderWidth = verticalHeaderWidth;
        working = spreadsheetView.showRowHeaderProperty().get();
    }

    /***************************************************************************
     * * Private/Protected Methods *
     * 
     * @param horizontalHeader
     *            *
     **************************************************************************/

    void init(final GridViewSkin skin, HorizontalHeader horizontalHeader) {
        // Adjust position upon HorizontalHeader height
        horizontalHeader.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> arg0, Number oldHeight, Number newHeight) {
                horizontalHeaderHeight = newHeight.doubleValue();
                requestLayout();
            }
        });

        // Clip property to stay within bounds
        clip = new Rectangle(verticalHeaderWidth.get(), snapSize(skin.getSkinnable().getHeight()));
        clip.relocate(snappedTopInset(), snappedLeftInset());
        clip.setSmooth(false);
        clip.heightProperty().bind(skin.getSkinnable().heightProperty());
        clip.widthProperty().bind(verticalHeaderWidth);
        VerticalHeader.this.setClip(clip);

        // We desactivate and activate the verticalHeader upon request
        spreadsheetView.showRowHeaderProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldValue, Boolean newValue) {
                working = newValue;
                if (!working) {
                    savedWidth = verticalHeaderWidth.get();
                    verticalHeaderWidth.set(0);
                } else {
                    verticalHeaderWidth.set(savedWidth == null ? skin.DEFAULT_VERTICALHEADER_WIDTH : savedWidth);
                }
                requestLayout();
            }
        });

        // When the Column header is showing or not, we need to update the
        // position of the verticalHeader
        spreadsheetView.showColumnHeaderProperty().addListener(layout);
        spreadsheetView.getFixedRows().addListener(layout);

        // In case we resize the view in any manners
        spreadsheetView.heightProperty().addListener(layout);

        // For layout properly the verticalHeader when there are some selected
        // items
        skin.getSelectedRows().addListener(layout);

        blankContextMenu = new ContextMenu();
    }

    @Override
    protected void layoutChildren() {
        if (working) {

            final GridViewSkin skin = handle.getCellsViewSkin();

            final double x = snappedLeftInset();
            final int cellSize = skin.getCellsSize();

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

            int rowCount = 0;
            Label label;
            int i = 0;
            // We don't want to add Label if there are no rows associated with.
            final int modelRowCount = spreadsheetView.getGrid().getRowCount();
            GridRow row;
            // We iterate over the visibleRows
            while (cellSize != 0 && skin.getRow(i) != null && i < modelRowCount) {
                row = skin.getRow(i);
                label = getLabel(rowCount++);
                if (spreadsheetView.getFixedRows().contains(row.getIndex())) {
                    label.setText(String.valueOf(row.getIndex() + 1) + ":");
                } else if (spreadsheetView.isRowFixable(row.getIndex())) {
                    label.setText(String.valueOf(row.getIndex() + 1) + ".");
                } else {
                    label.setText(String.valueOf(row.getIndex() + 1) + " ");
                }
                label.resize(verticalHeaderWidth.get(), spreadsheetView.getGrid().getRowHeight(row.getIndex()));
                label.relocate(x, y);
                label.setContextMenu(getRowContextMenu(row.getIndex()));

                // We want to highlight selected rows
                final ObservableList<String> css = label.getStyleClass();
                if (skin.getSelectedRows().contains(row.getIndex())) {
                    css.addAll("selected");
                } else {
                    css.removeAll("selected");
                }
                if (spreadsheetView.getFixedRows().contains(row.getIndex())) {
                    css.addAll("fixed");
                } else {
                    css.removeAll("fixed");
                }
                y += spreadsheetView.getGrid().getRowHeight(row.getIndex());
                ++i;
            }
            double spaceUsedByFixedRows = 0;
            // Then we iterate over the FixedRows if any
            if (!spreadsheetView.getFixedRows().isEmpty() && cellSize != 0) {
                for (int j = 0; j < spreadsheetView.getFixedRows().size(); ++j) {
                    if (!handle.getCellsViewSkin().getCurrentlyFixedRow()
                            .contains(spreadsheetView.getFixedRows().get(j)))
                        break;
                    label = getLabel(rowCount++);
                    label.setText(String.valueOf(spreadsheetView.getFixedRows().get(j) + 1) + ":");
                    label.resize(verticalHeaderWidth.get(),
                            spreadsheetView.getGrid().getRowHeight(spreadsheetView.getFixedRows().get(j)));
                    label.setContextMenu(getRowContextMenu(spreadsheetView.getFixedRows().get(j)));
                    // If the columnHeader is here, we need to translate a bit
                    if (spreadsheetView.showColumnHeaderProperty().get()) {
                        label.relocate(x, snappedTopInset() + horizontalHeaderHeight + spaceUsedByFixedRows);
                    } else {
                        label.relocate(x, snappedTopInset() + spaceUsedByFixedRows);
                    }
                    final ObservableList<String> css = label.getStyleClass();
                    if (skin.getSelectedRows().contains(spreadsheetView.getFixedRows().get(j))) {
                        css.addAll("selected");
                    } else {
                        css.removeAll("selected");
                    }
                    css.addAll("fixed");
                    spaceUsedByFixedRows += spreadsheetView.getGrid().getRowHeight(
                            spreadsheetView.getFixedRows().get(j));
                    y += spreadsheetView.getGrid().getRowHeight(spreadsheetView.getFixedRows().get(j));
                }
            }

            // First one blank and on top (z-order) of the others
            if (spreadsheetView.showColumnHeaderProperty().get()) {
                label = getLabel(rowCount++);
                label.setText("");
                label.resize(verticalHeaderWidth.get(), horizontalHeaderHeight);
                label.relocate(x, 0);
                label.getStyleClass().clear();
                label.setContextMenu(blankContextMenu);
            }

            VirtualScrollBar hbar = handle.getCellsViewSkin().getHBar();
            if (handle.getCellsViewSkin().getVBar().isVisible()) {
                // Last one blank and on top (z-order) of the others
                label = getLabel(rowCount++);
                label.setText("");
                label.resize(verticalHeaderWidth.get(), hbar.getHeight());
                label.relocate(snappedLeftInset(), skin.getSkinnable().getHeight() - hbar.getHeight());
                label.getStyleClass().clear();
                label.setContextMenu(blankContextMenu);
            }
            // Flush the rest of the children if any
            while (getChildren().size() > rowCount) {
                getChildren().remove(rowCount);
            }
        } else {
            getChildren().clear();
        }
    }

    /**
     * Called when value of vertical scrollbar change
     */
    void updateScrollY() {
        if (working) {
            requestLayout();
        }
    }

    /**
     * Create a new label and put it in the pile or just grab one from the pile.
     * 
     * @param rowNumber
     * @return
     */
    private Label getLabel(int rowNumber) {
        if (getChildren().isEmpty() || getChildren().size() <= rowNumber) {
            final Label label = new Label();
            getChildren().add(label);

            // We want to select when clicking on header
            label.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent arg0) {
                    if (arg0.isPrimaryButtonDown()) {
                        try {
                            int row = Integer.parseInt(label.getText().substring(0, label.getText().length() - 1));
                            TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = spreadsheetView
                                    .getSelectionModel();
                            TableViewFocusModel<ObservableList<SpreadsheetCell>> fm = handle.getGridView()
                                    .getFocusModel();
                            sm.clearAndSelect(row - 1, fm.getFocusedCell().getTableColumn());
                        } catch (NumberFormatException | StringIndexOutOfBoundsException ex) {

                        }
                    }
                }
            });
            return label;
        } else {
            return (Label) getChildren().get(rowNumber);
        }
    }

    /**
     * Return a contextMenu for fixing a row if possible.
     * 
     * @param i
     * @return
     */
    private ContextMenu getRowContextMenu(final Integer i) {
        if (spreadsheetView.isRowFixable(i)) {
            final ContextMenu contextMenu = new ContextMenu();

            MenuItem fixItem = new MenuItem("Fix");

            // fixItem.setGraphic(new ImageView(new
            // Image(SpreadsheetView.class.getResourceAsStream("pinSpreadsheetView.png"))));
            fixItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                    if (spreadsheetView.getFixedRows().contains(i)) {
                        spreadsheetView.getFixedRows().remove(i);
                    } else {
                        spreadsheetView.getFixedRows().add(i);
                    }
                }
            });
            contextMenu.getItems().add(fixItem);

            return contextMenu;
        } else {
            return blankContextMenu;
        }
    }
}
