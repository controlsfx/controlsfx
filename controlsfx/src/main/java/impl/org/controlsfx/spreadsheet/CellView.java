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

import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellEditor;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

/**
 * 
 * The View cell that will be visible on screen. It holds the
 * {@link SpreadsheetCell}.
 */
public class CellView extends TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> {
    private final SpreadsheetHandle handle;

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/
    private static final String ANCHOR_PROPERTY_KEY = "table.anchor"; //$NON-NLS-1$
    private static final int TOOLTIP_MAX_WIDTH = 400;
    private static final Duration FADE_DURATION = Duration.millis(200);

    private static final Stack<Tooltip> tooltipStack = new Stack<>();
    
    static TablePositionBase<?> getAnchor(Control table, TablePositionBase<?> focusedCell) {
        return hasAnchor(table) ? (TablePositionBase<?>) table.getProperties().get(ANCHOR_PROPERTY_KEY) : focusedCell;
    }

    static boolean hasAnchor(Control table) {
        return table.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
    }

    static void setAnchor(Control table, TablePositionBase anchor) {
        if (table != null && anchor == null) {
            removeAnchor(table);
        } else {
            table.getProperties().put(ANCHOR_PROPERTY_KEY, anchor);
        }
    }
    
    static void removeAnchor(Control table) {
        table.getProperties().remove(ANCHOR_PROPERTY_KEY);
    }
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public CellView(SpreadsheetHandle handle) {
        this.handle = handle;
        // When we detect a drag, we start the Full Drag so that other event
        // will be fired
        this.addEventHandler(MouseEvent.DRAG_DETECTED, new WeakEventHandler<>(startFullDragEventHandler));
        setOnMouseDragEntered(new WeakEventHandler<>(dragMouseEventHandler));
        
        itemProperty().addListener(itemChangeListener);
        setWrapText(true);
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    @Override
    public void startEdit() {
        if (!isEditable()) {
            return;
        } else if (handle.getGridView().getEditWithEnter()) {
            handle.getGridView().setEditWithEnter(false);
            return;
        }
        final int column = this.getTableView().getColumns().indexOf(this.getTableColumn());
        final int row = getIndex();
        // We start to edit only if the Cell is a normal Cell (aka visible).
        final SpreadsheetView spv = handle.getView();
        final Grid grid = spv.getGrid();
        final SpreadsheetView.SpanType type = grid.getSpanType(spv, row, column);
        //FIXME with the reverse algorithm in virtualFlow, is this still necessary?
        if (type == SpreadsheetView.SpanType.NORMAL_CELL || type == SpreadsheetView.SpanType.ROW_VISIBLE) {

            /**
             * We may come to the situation where this method is called two
             * times. One time by the row inside the VirtualFlow. And another by
             * the row inside myFixedCells used by our GridVirtualFlow.
             * 
             * In that case, we have to give priority to the one used by the
             * VirtualFlow. So we just check if the row is managed. If not, we
             * know for sure that the our GridVirtualFlow has stepped out.
             */
            if (!getTableRow().isManaged()) {
                return;
            }

            GridCellEditor editor = getEditor(getItem(), spv);
            if (editor != null) {
                super.startEdit();
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                editor.startEdit();
            }
        }
    }

    @Override
    public void commitEdit(SpreadsheetCell newValue) {
        //When commiting, we bring the value smoothly.
        FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, this);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
        
        if (!isEditing()) {
            return;
        }
        super.commitEdit(newValue);

        setContentDisplay(ContentDisplay.LEFT);
        updateItem(newValue, false);

        if (getTableView() != null) {
            getTableView().requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        if (!isEditing()) {
            return;
        }

        super.cancelEdit();

        setContentDisplay(ContentDisplay.LEFT);
        updateItem(getItem(), false);

        if (getTableView() != null) {
            getTableView().requestFocus();
        }
    }

    @Override
    public void updateItem(final SpreadsheetCell item, boolean empty) {
        final boolean emptyRow = getTableView().getItems().size() < getIndex() + 1;
        /**
         * don't call super.updateItem() because it will trigger cancelEdit() if
         * the cell is being edited. It causes calling commitEdit() ALWAYS call
         * cancelEdit as well which is undesired.
         * 
         */
        if (!isEditing()) {
            super.updateItem(item, empty && emptyRow);
        }
        if (empty && isSelected()) {
            updateSelected(false);
        }
        if (empty && emptyRow) {
            textProperty().unbind();
            setText(null);
            // do not nullify graphic here. Let the TableRow to control cell
            // dislay
            // setGraphic(null);
            setContentDisplay(null);
        } else if (!isEditing() && item != null) {
            show(item);
            if (item.getGraphic() == null) {
                setGraphic(null);
            }
        }
    }

    /**
     * Called in the gridRowSkinBase when doing layout This allow not to
     * override opacity in the row and let the cell handle itself
     * @param cell
     */
    public void show(final SpreadsheetCell cell) {
        // We reset the settings
        textProperty().bind(cell.textProperty());
        setCellGraphic(cell);

        Optional<String> tooltipText = cell.getTooltip();
        String trimTooltip = tooltipText.isPresent() ? tooltipText.get().trim() : null;
        
        if (trimTooltip != null && !trimTooltip.isEmpty()) {
            /**
             * Here we check if the Tooltip has not been created in order NOT TO
             * re-create it for nothing as it is a really time-consuming
             * operation.
             */
            Tooltip tooltip = getAvailableTooltip();
            if (tooltip != null) {
                if (!Objects.equals(tooltip.getText(), trimTooltip)) {
                    getTooltip().setText(trimTooltip);
                }
            } else {
                /**
                 * Ensure that modification of ToolTip are set on the JFX thread
                 * because an exception can be thrown otherwise.
                 */
                getValue(() -> {
                    Tooltip newTooltip = new Tooltip(tooltipText.get());
                    newTooltip.setWrapText(true);
                    newTooltip.setMaxWidth(TOOLTIP_MAX_WIDTH);
                    setTooltip(newTooltip);
                }
                );
            }
        } else {
            //We save that tooltip
            if(getTooltip() != null){
                tooltipStack.push(getTooltip());
            }
            setTooltip(null);
        }
        // We want the text to wrap onto another line
//        setWrapText(true);
        setEditable(cell.isEditable());
    }

    public void show() {
        if (getItem() != null) {
            show(getItem());
        }
    }

    /***************************************************************************
     * * Private Methods * *
     **************************************************************************/

    /**
     * See if a tootlip is available (either on the TableCell already, or in the
     * Stack). And then set it to the TableCell.
     *
     * @return
     */
    private Tooltip getAvailableTooltip(){
        if(getTooltip() != null){
            return getTooltip();
        }
        if(!tooltipStack.isEmpty()){
            Tooltip tooltip = tooltipStack.pop();
            setTooltip(tooltip);
            return tooltip;
        }
        return null;
    }
    
    private void setCellGraphic(SpreadsheetCell item) {

        if (isEditing()) {
            return;
        }
        Node graphic = item.getGraphic();
        if (graphic != null) {
            /**
             * This workaround is added for the first row containing a graphic
             * because for an unknown reason, the graphic is translated to a
             * negative value so it's not fully visible. So we add those
             * listener that watch those changes, and try to get the previous
             * value (the right one) if the new value goes out of bounds.
             */
            if (item.getRow() == 0) {
                graphic.layoutXProperty().removeListener(firstRowLayoutXListener);
                graphic.layoutXProperty().addListener(firstRowLayoutXListener);

                graphic.layoutYProperty().removeListener(firstRowLayoutYListener);
                graphic.layoutYProperty().addListener(firstRowLayoutYListener);
            }
            
            if (graphic instanceof ImageView) {
                ImageView image = (ImageView) graphic;
                image.setCache(true);
                image.setPreserveRatio(true);
                image.setSmooth(true);
                if(image.getImage() != null){
                image.fitHeightProperty().bind(
                        new When(heightProperty().greaterThan(image.getImage().getHeight())).then(
                                image.getImage().getHeight()).otherwise(heightProperty()));
                image.fitWidthProperty().bind(
                        new When(widthProperty().greaterThan(image.getImage().getWidth())).then(
                                image.getImage().getWidth()).otherwise(widthProperty()));
                }
            //If we have a Region and no text, we force it to take full space.    
            } else if (graphic instanceof Region && item.getItem() == null) {
                Region region = (Region) graphic;
                region.prefHeightProperty().bind(heightProperty());
                region.prefWidthProperty().bind(widthProperty());
            }
            setGraphic(graphic);
            /**
             * In case of a resize of the column, we have new cells that steal
             * the image from the original TableCell. So we check here if we are
             * not in that case so that the Graphic of the SpreadsheetCell will
             * always be on the latest tableView and therefore fully visible.
             */
            if (!getChildren().contains(graphic)) {
                getChildren().add(graphic);
            }
        } else {
            setGraphic(null);
        }
    }

    private final ChangeListener<Number> firstRowLayoutXListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldLayoutX, Number newLayoutX) {
            if (getItem() != null && getItem().getGraphic() != null && newLayoutX.doubleValue() < 0 && oldLayoutX != null) {
                getItem().getGraphic().setLayoutX(oldLayoutX.doubleValue());
            }
        }
    };
    
    private final ChangeListener<Number> firstRowLayoutYListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> ov, Number oldLayoutY, Number newLayoutY) {
            if (getItem() != null && getItem().getGraphic() != null && newLayoutY.doubleValue() < 0 && oldLayoutY != null) {
                getItem().getGraphic().setLayoutY(oldLayoutY.doubleValue());
            }
        }
    };
    
    /**
     * Return an instance of Editor specific to the Cell type We are not using
     * the build-in editor-Cell because we cannot know in advance which editor
     * we will need. Furthermore, we want to control the behavior very closely
     * in regards of the spanned cell (invisible etc).
     * 
     * @param cell
     *            The SpreadsheetCell
     * @param bc
     *            The SpreadsheetCell
     * @return
     */
    private GridCellEditor getEditor(final SpreadsheetCell cell, final SpreadsheetView spv) {
        SpreadsheetCellType<?> cellType = cell.getCellType();
        Optional<SpreadsheetCellEditor> cellEditor = spv.getEditor(cellType);

        if(cellEditor.isPresent()){
            GridCellEditor editor = handle.getCellsViewSkin().getSpreadsheetCellEditorImpl();
            if (editor.isEditing()) {
                return null;
            } else {
                editor.updateSpreadsheetCell(this);
                editor.updateDataCell(cell);
                editor.updateSpreadsheetCellEditor(cellEditor.get());
                return editor;
            }
        }else{
            return null;
        }
    }

    private final ChangeListener<Node> graphicListener = new ChangeListener<Node>() {
        @Override
        public void changed(ObservableValue<? extends Node> arg0, Node arg1, Node newGraphic) {
            setCellGraphic(getItem());
        }
    };

    private final WeakChangeListener<Node> weakGraphicListener = new WeakChangeListener<>(graphicListener);
    
    private final SetChangeListener<String> styleClassListener = new SetChangeListener<String>() {
        @Override
        public void onChanged(javafx.collections.SetChangeListener.Change<? extends String> arg0) {
            if (arg0.wasAdded()) {
                getStyleClass().add(arg0.getElementAdded());
            } else if (arg0.wasRemoved()) {
                getStyleClass().remove(arg0.getElementRemoved());
            }
        }
    };
    
    private final WeakSetChangeListener<String> weakStyleClassListener = new WeakSetChangeListener<>(styleClassListener);
    
    /**
     * Method that will select all the cells between the drag place and that
     * cell.
     * 
     * @param e
     */
    private void dragSelect(MouseEvent e) {
        // If the mouse event is not contained within this tableCell, then
        // we don't want to react to it.
        if (!this.contains(e.getX(), e.getY())) {
            return;
        }

        final TableView<ObservableList<SpreadsheetCell>> tableView = getTableView();
        if (tableView == null) {
            return;
        }

        final int count = tableView.getItems().size();
        if (getIndex() >= count) {
            return;
        }

        final TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = tableView.getSelectionModel();
        if (sm == null) {
            return;
        }

        final int row = getIndex();
        final int column = tableView.getVisibleLeafIndex(getTableColumn());

        // For spanned Cells
        final SpreadsheetCell cell = (SpreadsheetCell) getItem();
        final int rowCell = cell.getRow() + cell.getRowSpan() - 1;
        final int columnCell = cell.getColumn() + cell.getColumnSpan() - 1;

        final TableViewFocusModel<?> fm = tableView.getFocusModel();
        if (fm == null) {
            return;
        }

        final TablePositionBase<?> focusedCell = fm.getFocusedCell();
        final MouseButton button = e.getButton();
        if (button == MouseButton.PRIMARY) {
            // we add all cells/rows between the current selection focus and
            // this cell/row (inclusive) to the current selection.
            final TablePositionBase<?> anchor = getAnchor(tableView, focusedCell);

            /**
             * FIXME We need to clarify how we want to select the cells. If a
             * spanned cell is in the way, which minRow/maxRow will be taken?
             * Where the mouse is exactly? Or where the "motherCell" is? This
             * needs some thinking.
             */
            // and then determine all row and columns which must be selected
            int minRow = Math.min(anchor.getRow(), row);
            minRow = Math.min(minRow, rowCell);
            int maxRow = Math.max(anchor.getRow(), row);
            maxRow = Math.max(maxRow, rowCell);
            int minColumn = Math.min(anchor.getColumn(), column);
            minColumn = Math.min(minColumn, columnCell);
            int maxColumn = Math.max(anchor.getColumn(), column);
            maxColumn = Math.max(maxColumn, columnCell);

            // clear selection, but maintain the anchor
            if (!e.isShortcutDown())
                sm.clearSelection();
            if (minColumn != -1 && maxColumn != -1)
                sm.selectRange(minRow, tableView.getColumns().get(minColumn), maxRow,
                        tableView.getColumns().get(maxColumn));
            setAnchor(tableView, anchor);
        }

    }

    /**
     * Will safely execute the request on the JFX thread by checking whether we
     * are on the JFX thread or not.
     * 
     * @param runnable
     */
    public static void getValue(final Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    @Override
    protected javafx.scene.control.Skin<?> createDefaultSkin() {
        return new CellViewSkin(this);
    };
    
    private final EventHandler<MouseEvent> startFullDragEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent arg0) {
            if (handle.getGridView().getSelectionModel().getSelectionMode().equals(SelectionMode.MULTIPLE)) {
                setAnchor(getTableView(), getTableView().getFocusModel().getFocusedCell());
                startFullDrag();
            }
        }
    };
    
    private final EventHandler<MouseEvent> dragMouseEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent arg0) {
            dragSelect(arg0);
        }
    };
    
    private final ChangeListener<SpreadsheetCell> itemChangeListener = new ChangeListener<SpreadsheetCell>() {

        @Override
        public void changed(ObservableValue<? extends SpreadsheetCell> arg0, SpreadsheetCell oldItem,
                SpreadsheetCell newItem) {
            if (oldItem != null) {
                oldItem.getStyleClass().removeListener(weakStyleClassListener);
                oldItem.graphicProperty().removeListener(weakGraphicListener);
            }
            if (newItem != null) {
                getStyleClass().clear();
                getStyleClass().setAll(newItem.getStyleClass());

                newItem.getStyleClass().addListener(weakStyleClassListener);
                setCellGraphic(newItem);
                newItem.graphicProperty().addListener(weakGraphicListener);
            }
        }
    };
}
