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
package impl.org.controlsfx.skin;

import java.util.Map;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.SpreadsheetView.SpanType;
import org.controlsfx.control.spreadsheet.model.Grid;
import org.controlsfx.control.spreadsheet.model.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.view.SpreadsheetCellEditor;
import org.controlsfx.control.spreadsheet.view.SpreadsheetCellEditors;

import com.sun.javafx.scene.control.skin.TableCellSkin;


/**
 *
 * The View cell that will be visible on screen.
 * It holds the {@link DataRow} and the {@link SpreadsheetCell}.
 */
public class SpreadsheetCellImpl<T> extends TableCell<ObservableList<SpreadsheetCell<?>>, SpreadsheetCell<T>> {

    /***************************************************************************
     *                                                                         *
     * Static Fields                                                           *
     *                                                                         *
     **************************************************************************/
    private static final String ANCHOR_PROPERTY_KEY = "table.anchor";

    static TablePositionBase<?> getAnchor(Control table, TablePositionBase<?> focusedCell) {
        return hasAnchor(table) ?
                (TablePositionBase<?>) table.getProperties().get(ANCHOR_PROPERTY_KEY) :
                    focusedCell;
    }
    static boolean hasAnchor(Control table) {
        return table.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
    }
    
    private static final Map<SpreadsheetCell.CellType, SpreadsheetCellEditor<?>> editors = FXCollections.observableHashMap();
    private static SpreadsheetCellImpl<?> lastHover = null;
    
    
    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
    public SpreadsheetCellImpl() {

    	hoverProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
                final int row = getIndex();
//                final SpreadsheetView spv = ((SpreadsheetRow)getTableRow()).getSpreadsheetView();

                if (getItem() == null) {
                    getTableRow().requestLayout();
                // We only need to re-route if the rowSpan is large because
                // it's the only case where it's not handled correctly.
                }else if(getItem().getRowSpan() >1){
                	 // If we are not at the top of the Spanned Cell
                	if (t1 && row != getItem().getRow()) {
                        hoverGridCell(getItem());
                    } else if (!t1 && row != getItem().getRow()) {
                        unHoverGridCell();
                    }
                }
            }
        });
        //When we detect a drag, we start the Full Drag so that other event will be fired
        this.addEventHandler(MouseEvent.DRAG_DETECTED,new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent arg0) {
                startFullDrag();
            }});


        setOnMouseDragEntered(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent arg0) {
                dragSelect(arg0);
            }});

    }

    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
    @Override
    public void startEdit() {
        if(!isEditable()) {
            return;
        }

        final int column = this.getTableView().getColumns().indexOf(this.getTableColumn());
        final int row = getIndex();
        //We start to edit only if the Cell is a normal Cell (aka visible).
        final SpreadsheetView spv = getSpreadsheetView();
        Grid grid = spv.getGrid();
        final SpreadsheetView.SpanType type = grid.getSpanType(spv, row, column);
        if ( type == SpreadsheetView.SpanType.NORMAL_CELL || type == SpreadsheetView.SpanType.ROW_VISIBLE) {
        	
        	/* FIXME Currently we're adding a row two times if it's located in the fixedRows.
        	 * So we have a problem because some events, especially when editing
        	 * are received in double.
        	 * I don't know right now why this is happening but I have found a work-around.
        	 * I check if the current SpreadsheetRow is referenced in the SpreadsheetView,
        	 * if not, then I know I can throw it away (setManaged(false) ?)
        	 */
        	if(row <= spv.getFixedRows()){
	        	boolean flag = false;
	        	for (int j = 0; j<SpreadsheetViewSkin.getSkin(spv).getCellsSize();j++ ) {
	                    if(SpreadsheetViewSkin.getCell(spv, j) == getTableRow()){
	                    	flag = true;
	                    }
	            }
	        	if(!flag){
	        		getTableRow().setManaged(false);
	        		return;
	        	}
        	}
        	
            SpreadsheetCellEditor<?> editor = getEditor(getItem(), spv);
            if(editor != null){
                super.startEdit();
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                editor.startEdit();
            }
        }
    }
    
    
    /**
     * Return an instance of Editor specific to the Cell type
     * We are not using the build-in editor-Cell because we cannot know in advance
     * which editor we will need. Furthermore, we want to control the behavior very closely
     * in regards of the spanned cell (invisible etc).
     * @param cell The SpreadsheetCell
     * @param bc The SpreadsheetCell
     * @return
     */
    @SuppressWarnings("unchecked")
    private SpreadsheetCellEditor<?> getEditor(final SpreadsheetCell<T> cell, final SpreadsheetView spv) {
        SpreadsheetCellEditor<T> editor = (SpreadsheetCellEditor<T>) editors.get(cell.getCellType());
        if (editor == null) {
            switch (cell.getCellType()) {
                case STRING:
                    editor = (SpreadsheetCellEditor<T>) SpreadsheetCellEditors.createTextEditor();
                    editors.put(cell.getCellType(), editor);
                    break;
                case ENUM:
                    editor = (SpreadsheetCellEditor<T>) SpreadsheetCellEditors.createListEditor();
                    editors.put(cell.getCellType(), editor);
                    break;
                case DATE:
                    editor = (SpreadsheetCellEditor<T>) SpreadsheetCellEditors.createDateEditor();
                    editors.put(cell.getCellType(), editor);
                    break;
                default:
                    return null;
            }
        }

        if (editor.isEditing()){
            return null;
        } else {
            editor.updateSpreadsheetView(spv);
            editor.updateSpreadsheetCell(this);
            editor.updateDataCell(cell);
            return editor;
        }
    }
    
    

    @Override
    public void commitEdit(SpreadsheetCell<T> newValue) {
        if (! isEditing()) {
            return;
        }
        super.commitEdit(newValue);

        setContentDisplay(ContentDisplay.TEXT_ONLY);

        updateItem(newValue, false);


    }

    @Override
    public void cancelEdit() {
        if (! isEditing()) {
            return;
        }

        super.cancelEdit();

        setContentDisplay(ContentDisplay.TEXT_ONLY);
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                getTableView().requestFocus();
            }
        };
        Platform.runLater(r);
    }

    @Override
    public void updateItem(final SpreadsheetCell<T> item, boolean empty) {
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
            setText(null);
            //do not nullify graphic here. Let the TableRow to control cell dislay
            //setGraphic(null);
            setContentDisplay(null);
        } else if (!isEditing() && item != null) {
            show(item);
            //Sometimes the hoverProperty is not called on exit. So the cell is affected to a new Item but
            // the hover is still activated. So we fix it now.
            if(isHover()){
            	setHoverPublic(false);
            }

        }
    }
    
    /**
     * Set this SpreadsheetCell hoverProperty
     *
     * @param hover
     */
    void setHoverPublic(boolean hover) {
        this.setHover(hover);
        // We need to tell the SpreadsheetRow where this SpreadsheetCell is in to be in Hover
        //Otherwise it's will not be visible
        ((SpreadsheetRowImpl) this.getTableRow()).setHoverPublic(hover);
    }

    @Override
    public String toString(){
        return getItem().getRow()+"/"+getItem().getColumn();

    }
    /**
     * Called in the gridRowSkinBase when doing layout
     * This allow not to override opacity in the row and let the
     * cell handle itself
     */
    public void show(final SpreadsheetCell<?> item){
        //We reset the settings
        setText(item.getStr());
        this.setEditable(item.getEditable());

        // Style
        final ObservableList<String> css = getStyleClass();
        if (css.size() == 1) {
            css.set(0, item.getStyleCss());
        }else{
            css.clear();
            css.add(item.getStyleCss());
        }

    }

    public void show(){
        if (getItem() != null){
            show(getItem());
        }
    }

    /***************************************************************************
     *                                                                         *
     * Protected Methods                                                          *
     *                                                                         *
     **************************************************************************/
    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableCellSkin<>(this);
    }

    /***************************************************************************
     *                                                                         *
     * Private Methods                                                          *
     *                                                                         *
     **************************************************************************/
    
    private SpreadsheetView getSpreadsheetView() {
        return ((SpreadsheetRowImpl)getTableRow()).getSpreadsheetView();
    }
    
    /**
     * A SpreadsheetCell is being hovered and we need to re-route the signal.
     *
     * @param cell The DataCell needed to be hovered.
     * @param hover
     */
    private void hoverGridCell(SpreadsheetCell<?> cell) {
        SpreadsheetCellImpl<?> gridCell;
        
        final SpreadsheetView spv = getSpreadsheetView();
        final SpreadsheetRowImpl row = SpreadsheetViewSkin.getCell(spv, spv.getFixedRowsList().size());
        
        if (SpreadsheetViewSkin.getSkin(spv).getCellsSize() !=0  && row.getIndex() <= cell.getRow()) {
        	final SpreadsheetRowImpl rightRow = SpreadsheetViewSkin.getCell(spv, spv.getFixedRowsList().size()+cell.getRow() - row.getIndex());
            // We want to get the top of the spanned cell, so we need
            // to access the fixedRows.size plus the difference between where we want to go and the first visibleRow (header excluded)
            if( rightRow != null) {// Sometime when scrolling fast it's null so..
                gridCell = rightRow.getGridCell(cell.getColumn());
            } else {
                gridCell = row.getGridCell(cell.getColumn());
            }
        } else { // If it's not, then it's the firstkey
            gridCell = row.getGridCell(cell.getColumn());
        }
        gridCell.setHoverPublic(true);
        lastHover = gridCell;
    }

    /**
     * Set Hover to false to the previous Cell we force to be hovered
     */
    private void unHoverGridCell() {
        //If the top of the spanned cell is visible, then no problem
        if(lastHover != null){
        	lastHover.setHoverPublic(false);
        }
    }
    
    
    /**
     * Method that will select all the cells between the drag place and that cell.
     * @param e
     */
    private void dragSelect(MouseEvent e) {

        // If the mouse event is not contained within this tableCell, then
        // we don't want to react to it.
        if (! this.contains(e.getX(), e.getY())) {
            return;
        }

        final TableView<ObservableList<SpreadsheetCell<?>>> tableView = getTableView();
        if (tableView == null) {
            return;
        }

        final int count = tableView.getItems().size();
        if (getIndex() >= count) {
            return;
        }

        final TableSelectionModel<ObservableList<SpreadsheetCell<?>>> sm = tableView.getSelectionModel();
        if (sm == null) {
            return;
        }

        final int row = getIndex();
        final int column = tableView.getVisibleLeafIndex(getTableColumn());
        // For spanned Cells
        final SpreadsheetCell<?> cell = (SpreadsheetCell<?>) getItem();
        final int rowCell = cell.getRow()+cell.getRowSpan()-1;
        final int columnCell = cell.getColumn()+cell.getColumnSpan()-1;

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
            sm.clearSelection();

            // and then perform the selection
            for (int _row = minRow; _row <= maxRow; _row++) {
                for (int _col = minColumn; _col <= maxColumn; _col++) {
                    sm.select(_row, tableView.getVisibleLeafColumn(_col));
                }
            }
        }

    }
}
