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

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
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
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.controlsfx.control.spreadsheet.Filter;
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
    final SpreadsheetHandle handle;
    /**
     * Because we don't want to recreate Tooltip each time the TableCell is
     * re-used. We save it properly here so we avoid recreating it each time
     * since it's really time-consuming.
     */
    private Tooltip tooltip;
    //Handler for drag n drop in lazy instantiation.
    private EventHandler<DragEvent> dragOverHandler;
    private EventHandler<DragEvent> dragDropHandler;

    /***************************************************************************
     * * Static Fields * *
     **************************************************************************/
    private static final String ANCHOR_PROPERTY_KEY = "table.anchor"; //$NON-NLS-1$
    private static final int TOOLTIP_MAX_WIDTH = 400;
    private static final Duration FADE_DURATION = Duration.millis(200);

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
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    @Override
    public void startEdit() {
        /**
         * If this CellView has no parent, this means that it was stacked into
         * the cellsMap of the GridRowSkin, but the weakRef was dropped. So this
         * CellView is still reacting to events, but it's not part of the
         * sceneGraph! So we must deactivate this cell and let the real Cell in
         * the sceneGraph take the edition.
         *
         * This MUST be the first action because if this cell is not part of the
         * scene, we MUST NOT consider it at all.
         */
        if(getParent() == null){
            updateTableView(null);
            updateTableRow(null);
            updateTableColumn(null);
            return;
        }
        
        if (!isEditable()) {
            getTableView().edit(-1, null);
            return;
        } 
        
        final int column = this.getTableView().getColumns().indexOf(this.getTableColumn());
        final int row = getIndex();
        // We start to edit only if the Cell is a normal Cell (aka visible).
        final SpreadsheetView spv = handle.getView();
        final SpreadsheetView.SpanType type = spv.getSpanType(row, column);
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
            }else{
                getTableView().edit(-1, null);
            }
        }
    }

    /**
     * Return the Filter associated to this cell or null otherwise.
     * @return 
     */
    Filter getFilter() {
        Filter filter = getItem() != null  && getItem().getColumn() < handle.getView().getColumns().size() ? handle.getView().getColumns().get(getItem().getColumn()).getFilter() : null;
        //If we have a span, we check if the filteredRow is contained in the row span of this cell.
        if (filter != null && getItem().getRowSpan() > 1) {
            int rowSpan = handle.getView().getRowSpan(getItem(), getIndex());
            int row = getItem().getRow();
            for (int i = row; i < row + rowSpan; ++i) {
                if (handle.getView().getFilteredRow() == handle.getView().getModelRow(i)) {
                    return filter;
                }
            }
            //If we're here, nothing has been found.
            return null;
        }
        //If not we simply compare the filtered row.
        return filter != null && handle.getView().getFilteredRow() == handle.getView().getModelRow(getIndex()) ? filter : null;
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

        //We release the editor if it has not been done.
        GridCellEditor editor = handle.getCellsViewSkin().getSpreadsheetCellEditorImpl();
        if (editor.isEditing()) {
            editor.endEdit(false);
        }
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
            Tooltip localTooltip = getAvailableTooltip();
            if (localTooltip != null) {
                if (!Objects.equals(localTooltip.getText(), trimTooltip)) {
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
                tooltip = getTooltip();
            }
            setTooltip(null);
        }
        
        setWrapText(cell.isWrapText());

        setEditable(cell.hasPopup() ? false : cell.isEditable());
        
        if (cell.hasPopup()) {
            setOnMouseClicked(weakActionhandler);
            setCursor(Cursor.HAND);
        } else {
            setOnMouseClicked(null);
            setCursor(Cursor.DEFAULT);
        }
        if (cell.getCellType().acceptDrop()) {
            setOnDragOver(getDragOverHandler());
            // Dropping over surface
            setOnDragDropped(getDragDropHandler());
        } else {
            setOnDragOver(null);
            setOnDragDropped(null);
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
        if(tooltip != null){
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
                /**
                 * If we have a Region and no text, we force it to take full
                 * space. But we want to impact the minSize in order to let the
                 * prefSize to be computed if necessary.
                 */
            } else if (graphic instanceof Region && item.getItem() == null) {
                Region region = (Region) graphic;
                region.minHeightProperty().bind(heightProperty());
                region.minWidthProperty().bind(widthProperty());
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

        if (cellEditor.isPresent()) {
            GridCellEditor editor = handle.getCellsViewSkin().getSpreadsheetCellEditorImpl();
            /**
             * Sometimes, we end up here with the editor already editing. But
             * this case should not happen. If a cell is calling startEdit,
             * this means we want to edit the cell and the editor should not be
             * editing another cell. So we just cancel the edition and give the
             * editor to the cell because we may not be able to edit anything.
             */
            if (editor.isEditing()) {
                if (editor.getModelCell() != null) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("The cell at row ").append(editor.getModelCell().getRow())
                            .append(" and column ").append(editor.getModelCell().getColumn())
                            .append(" was in edition and cell at row ").append(cell.getRow())
                            .append(" and column ").append(cell.getColumn())
                            .append(" requested edition. This situation should not happen as the previous cell should not be in edition.");
                    Logger.getLogger("root").warning(builder.toString());
                }

                editor.endEdit(false);
            }
            
            editor.updateSpreadsheetCell(this);
            editor.updateDataCell(cell);
            editor.updateSpreadsheetCellEditor(cellEditor.get());
            return editor;
        } else {
            return null;
        }
    }
    
    private EventHandler<DragEvent> getDragOverHandler() {
        if (dragOverHandler == null) {
            dragOverHandler = new EventHandler<DragEvent>() {

                @Override
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    if (db.hasFiles()) {
                        event.acceptTransferModes(TransferMode.ANY);
                    } else {
                        event.consume();
                    }
                }
            };
        }
        return dragOverHandler;
    }

    private EventHandler<DragEvent> getDragDropHandler() {
        if (dragDropHandler == null) {
            dragDropHandler = new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasFiles() && db.getFiles().size() == 1) {
                        if (getItem().getCellType().match(db.getFiles().get(0), getItem().getOptionsForEditor())) {
                            handle.getView().getGrid().setCellValue(getItem().getRow(), getItem().getColumn(),
                                    getItem().getCellType().convertValue(db.getFiles().get(0)));
                            success = true;
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                }
            };
        }
        return dragDropHandler;
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
    
    //Listeners for the styles, not initialized by default in order not to impact performance
    private ChangeListener<String> styleListener;
    private WeakChangeListener<String> weakStyleListener;
    
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
        final int rowCell = getIndex() + handle.getView().getRowSpan(cell, getIndex()) - 1;
        final int columnCell = handle.getView().getViewColumn(cell.getColumn()) + handle.getView().getColumnSpan(cell) - 1;

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
                sm.selectRange(minRow, tableView.getVisibleLeafColumn(minColumn), maxRow,
                        tableView.getVisibleLeafColumn(maxColumn));
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

                if (oldItem.styleProperty() != null) {
                    oldItem.styleProperty().removeListener(weakStyleListener);
                }
            }
            if (newItem != null) {
                getStyleClass().clear();
                getStyleClass().setAll(newItem.getStyleClass());

                newItem.getStyleClass().addListener(weakStyleClassListener);
                setCellGraphic(newItem);
                newItem.graphicProperty().addListener(weakGraphicListener);

                if (newItem.styleProperty() != null) {
                    initStyleListener();
                    newItem.styleProperty().addListener(weakStyleListener);
                    setStyle(newItem.getStyle());
                } else {
                    //We clear the previous style.
                    setStyle(null);
                }
            }
        }
    };
    
    /**
     * Event Handler when the cell is simply clicked in order to display the
     * possible actions in MenuItem.
     */
    private final EventHandler<MouseEvent> actionEventHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            /**
             * If we have some items to show and also we don't have a current
             * filter on this cell showing. If it is, we must block this Popup,
             * otherwise we will have two contextMenu overlapping each others.
             */
            if (getItem() != null && getItem().hasPopup() && MouseButton.PRIMARY.equals(event.getButton())
                    && (getFilter() == null || !getFilter().getMenuButton().isShowing())) {
                ContextMenu menu = new ContextMenu();
                menu.getScene().getStylesheets().add(SpreadsheetView.class.getResource("spreadsheet.css").toExternalForm());
                menu.getStyleClass().add("popup-button");
                menu.getItems().setAll(getItem().getPopupItems());
                menu.show(CellView.this, Side.BOTTOM, 0, 0);
            }
        }
    };
    private final WeakEventHandler weakActionhandler = new WeakEventHandler(actionEventHandler);
    
    private void initStyleListener(){
        if(styleListener == null){
            styleListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                styleProperty().set(newValue);
            };
        }
        weakStyleListener = new WeakChangeListener<>(styleListener);
    }
}
