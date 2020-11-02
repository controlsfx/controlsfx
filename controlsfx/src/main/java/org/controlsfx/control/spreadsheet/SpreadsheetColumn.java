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
package org.controlsfx.control.spreadsheet;

import static impl.org.controlsfx.i18n.Localization.asKey;
import static impl.org.controlsfx.i18n.Localization.localize;
import impl.org.controlsfx.spreadsheet.CellView;
import impl.org.controlsfx.spreadsheet.CellViewSkin;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.WindowEvent;
import org.controlsfx.tools.Utils;

/**
 * A {@link SpreadsheetView} is made up of a number of {@link SpreadsheetColumn}
 * instances.
 * 
 * <h3>Configuration</h3> SpreadsheetColumns are instantiated by the
 * {@link SpreadsheetView} itself, so there is no public constructor for this
 * class. To access the available columns, you need to call
 * {@link SpreadsheetView#getColumns()}.
 * 
 * <p>
 * SpreadsheetColumn gives you the ability to modify some aspects of the column,
 * for example the {@link #setPrefWidth(double) width} or
 * {@link #setResizable(boolean) resizability} of the column.
 * 
 * <p>
 * You have the ability to freeze this column at the left of the SpreadsheetView by
 * calling {@link #setFixed(boolean)}. But you are strongly advised to check if
 * it is possible with {@link #isColumnFixable()} before calling
 * {@link #setFixed(boolean)}. Take a look at the {@link SpreadsheetView}
 * description to understand the freezing constraints.
 * 
 * <p>
 * If the column can be frozen, a {@link ContextMenu} will appear if the user right-clicks on it. 
 * If not, nothing will appear and the user will not have the possibility to freeze it.
 * 
 * <h3>Screenshot</h3>
 * The column <b>A</b> is frozen and is covering column <b>B</b> and partially
 * column <b>C</b>. The context menu is being shown and offers the possibility
 * to unfreeze the column.
 * 
 * <br>
 * <br>
 * <center><img src="fixedColumn.png" alt="Screenshot of SpreadsheetColumn"></center>
 * 
 * @see SpreadsheetView
 */
public final class SpreadsheetColumn {

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private final SpreadsheetView spreadsheetView;
    final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;
    private final boolean canFix;
    private final Integer indexColumn;
    private MenuItem fixItem;
    //The current filter applied on this column if any.
    private final ObjectProperty<Filter> filterProperty = new SimpleObjectProperty<>();

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    /**
     * Creates a new SpreadsheetColumn.
     * 
     * @param column
     * @param spreadsheetView
     * @param indexColumn
     */
    SpreadsheetColumn(final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column,
            final SpreadsheetView spreadsheetView, final Integer indexColumn, Grid grid) {
        this.spreadsheetView = spreadsheetView;
        this.column = column;
        column.setMinWidth(0);
        this.indexColumn = indexColumn;
        canFix = initCanFix(grid);

        // The contextMenu creation must be on the JFX thread
        CellView.getValue(() -> {
            column.setContextMenu(getColumnContextMenu());
        });

        // When changing frozen fixed columns, we need to update the ContextMenu.
        spreadsheetView.fixingColumnsAllowedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                CellView.getValue(() -> {
                    column.setContextMenu(getColumnContextMenu());
                });
            }
        });

        // When ColumnsHeaders are changing, we update the text
        grid.getColumnHeaders().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                List<String> columnsHeader = spreadsheetView.getGrid().getColumnHeaders();
                if (columnsHeader.size() <= indexColumn) {
                    setText(Utils.getExcelLetterFromNumber(indexColumn));
                } else if (!columnsHeader.get(indexColumn).equals(getText())) {
                    setText(columnsHeader.get(indexColumn));
                }
            }
        });

        // When changing rows, we re-calculate if this columns can be fixed.
        grid.getRows().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                initCanFix(grid);
            }
        });
       
        filterProperty.addListener(new ChangeListener<Filter>() {
            @Override
            public void changed(ObservableValue<? extends Filter> observable, Filter oldFilter, Filter newFilter) {
                if (newFilter != null) {
                    //We verify this cell can actually be filtered.
                    //FIXME Only one row can be filtered.
                    if (spreadsheetView.getFilteredRow() == -1) {
                        setFilter(null);
                        return;
                    }
                    SpreadsheetCell cell = spreadsheetView.getGrid().getRows().get(spreadsheetView.getFilteredRow()).get(indexColumn);
                    if (cell.getColumnSpan() > 1) {
                        setFilter(null);
                        return;
                    }
                }
                Event.fireEvent(column, new Event(CellViewSkin.FILTER_EVENT_TYPE));
            }
        });
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    /**
     * Return whether this column is frozen or not.
     * 
     * @return true if this column is frozen.
     */
    public boolean isFixed() {
        return spreadsheetView.getFixedColumns().contains(this);
    }

    /**
     * Freeze this column to the left if possible, although it is recommended that
     * you call {@link #isColumnFixable()} before trying to freeze a column.
     *
     * If you want to freeze several columns (because of a span for example), add
     * all the columns directly in {@link SpreadsheetView#getFixedColumns() }.
     * Always use {@link SpreadsheetView#areSpreadsheetColumnsFixable(java.util.List)
     * } before.
     *
     * @param fixed
     */
    public void setFixed(boolean fixed) {
        if (fixed) {
            spreadsheetView.getFixedColumns().add(this);
        } else {
            spreadsheetView.getFixedColumns().removeAll(this);
        }
    }

    /**
     * Set the width of this column.
     * 
     * @param width
     */
    public void setPrefWidth(double width) {
        width = Math.ceil(width);
        if (column.getPrefWidth() == width && column.getWidth() != width) {
            //FIXME
//            column.impl_setWidth(width);
        } else {
            column.setPrefWidth(width);
        }
        spreadsheetView.columnWidthSet(indexColumn);
    }

    /**
     * Return the actual width of the column.
     *
     * @return the actual width of the column
     */
    public double getWidth() {
        return column.getWidth();
    }

    /**
     * Return the Property related to the actual width of the column.
     *
     * @return the Property related to the actual width of the column.
     */
    public final ReadOnlyDoubleProperty widthProperty() {
        return column.widthProperty();
    }

    /**
     * Set the minimum width for this SpreadsheetColumn.
     *
     * @param value
     */
    public final void setMinWidth(double value) {
        column.setMinWidth(value);
    }

    /**
     * Return the minimum width for this SpreadsheetColumn.
     *
     * @return the minimum width for this SpreadsheetColumn.
     */
    public final double getMinWidth() {
        return column.getMinWidth();
    }

    /**
     * Return the Property related to the minimum width of this
     * SpreadsheetColumn.
     *
     * @return the Property related to the minimum width of this
     * SpreadsheetColumn.
     */
    public final DoubleProperty minWidthProperty() {
        return column.minWidthProperty();
    }

    /**
     * Return the Property related to the maximum width of this
     * SpreadsheetColumn.
     *
     * @return the Property related to the maximum width of this
     * SpreadsheetColumn.
     */
    public final DoubleProperty maxWidthProperty() {
        return column.maxWidthProperty();
    }

    /**
     * Set the maximum width for this SpreadsheetColumn.
     *
     * @param value
     */
    public final void setMaxWidth(double value) {
        column.setMaxWidth(value);
    }

    /**
     * Return the maximum width for this SpreadsheetColumn.
     *
     * @return the maximum width for this SpreadsheetColumn.
     */
    public final double getMaxWidth() {
        return column.getMaxWidth();
    }
    /**
     * If this column can be resized by the user
     * 
     * @param b
     */
    public void setResizable(boolean b) {
        column.setResizable(b);
    }

    /**
     * If the column is resizable, it will compute the optimum width for all the
     * visible cells to be visible.
     */
    public void fitColumn() {
        if (column.isResizable() && spreadsheetView.getCellsViewSkin() != null) {
            spreadsheetView.getCellsViewSkin().resize(column, 100);
        }
    }

    /**
     * Indicate whether this column can be frozen or not. Call that method before
     * calling {@link #setFixed(boolean)} or adding an item to
     * {@link SpreadsheetView#getFixedColumns()}.
     *
     * A column cannot be frozen alone if any cell inside the column has a column
     * span superior to one.
     *
     * @return true if this column is freezable.
     */
    public boolean isColumnFixable() {
        return canFix && spreadsheetView.isFixingColumnsAllowed();
    }

    public void setFilter(Filter filter){
        this.filterProperty.setValue(filter);
    }
    
    public Filter getFilter(){
        return filterProperty.get();
    }
    public ObjectProperty filterProperty(){
        return filterProperty;
    }
    
    /***************************************************************************
     * * Private Methods * *
     **************************************************************************/
    private void setText(String text) {
        column.setText(text);
    }

    private String getText() {
        return column.getText();
    }

    /**
     * Generate a context Menu in order to freeze/unfreeze some column It is shown
     * when right-clicking on the column header
     * 
     * @return a context menu.
     */
    private ContextMenu getColumnContextMenu() {
        if (isColumnFixable()) {
            final ContextMenu contextMenu = new ContextMenu();

            this.fixItem = new MenuItem(localize(asKey("spreadsheet.column.menu.fix"))); //$NON-NLS-1$
            contextMenu.setOnShowing(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent event) {
                    if (!isFixed()) {
                        fixItem.setText(localize(asKey("spreadsheet.column.menu.fix"))); //$NON-NLS-1$
                    } else {
                        fixItem.setText(localize(asKey("spreadsheet.column.menu.unfix"))); //$NON-NLS-1$
                }
                }
            });
            fixItem.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("pinSpreadsheetView.png")))); //$NON-NLS-1$
            fixItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent arg0) {
                    if (!isFixed()) {
                        setFixed(true);
                    } else {
                        setFixed(false);
                    }
                }
            });
            contextMenu.getItems().addAll(fixItem);

            return contextMenu;
        } else {
            return new ContextMenu();
        }
    }

    /**
     * Verify that you can freeze this column. 
     * 
     * @return if it's freezable.
     */
    private boolean initCanFix(Grid grid) {
        for (ObservableList<SpreadsheetCell> row : grid.getRows()) {
            int columnSpan = row.get(indexColumn).getColumnSpan();
            if (columnSpan > 1) {
                return false;
            }
        }
        return true;
    }
}
