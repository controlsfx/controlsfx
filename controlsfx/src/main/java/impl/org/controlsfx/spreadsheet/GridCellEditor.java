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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellEditor;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

public class GridCellEditor {

    /***************************************************************************
     * * Protected/Private Fields * *
     **************************************************************************/

    private final SpreadsheetHandle handle;
    // transient properties - these fields will change based on the current
    // cell being edited.
    private SpreadsheetCell modelCell;
    private CellView viewCell;
    private BooleanExpression focusProperty;

    private boolean editing = false;
    
    //The cell's editor 
    private SpreadsheetCellEditor spreadsheetCellEditor;
    
    //The last key pressed in order to select cell below if it was "enter"
    private KeyCode lastKeyPressed;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Construct the GridCellEditor.
     */
    public GridCellEditor(SpreadsheetHandle handle) {
        this.handle = handle;
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    /**
     * Update the internal {@link SpreadsheetCell}.
     * 
     * @param cell
     */
    public void updateDataCell(SpreadsheetCell cell) {
        this.modelCell = cell;
    }

    /**
     * Update the internal {@link CellView}
     * 
     * @param cell
     */
    public void updateSpreadsheetCell(CellView cell) {
        this.viewCell = cell;
    }

    /**
     * Update the SpreadsheetCellEditor
     * 
     * @param spreadsheetCellEditor
     */
    public void updateSpreadsheetCellEditor(final SpreadsheetCellEditor spreadsheetCellEditor) {
        this.spreadsheetCellEditor = spreadsheetCellEditor;
    }

    /**
     * Whenever you want to stop the edition, you call that method.<br/>
     * True means you're trying to commit the value, then 
     * {@link SpreadsheetCellType#match(java.lang.Object) } will be called 
     * in order to verify that the value is correct.<br/>
     * 
     * False means you're trying to cancel the value and it will be follow by
     * {@link #end()}.<br/>
     * See SpreadsheetCellEditor description
     * 
     * @param commitValue true means commit, false means cancel
     */
    public void endEdit(boolean commitValue) {
        if (commitValue && editing) {
            final SpreadsheetView view = handle.getView();
            boolean match = modelCell.getCellType().match(spreadsheetCellEditor.getControlValue(), modelCell.getOptionsForEditor());

            if (match && viewCell != null) {
                Object value = modelCell.getCellType().convertValue(spreadsheetCellEditor.getControlValue());

                // We update the value
                view.getGrid().setCellValue(modelCell.getRow(), modelCell.getColumn(), value);
                editing = false;
                viewCell.commitEdit(modelCell);
                end();
                spreadsheetCellEditor.end();

                //We select the cell below if "enter" was typed.
                if (KeyCode.ENTER.equals(lastKeyPressed)) {
                    ((GridViewBehavior) handle.getCellsViewSkin().getBehavior()).selectCell(1, 0);
                } else if (KeyCode.TAB.equals(lastKeyPressed)) {
                    handle.getView().getSelectionModel().clearAndSelectRightCell();
                    handle.getCellsViewSkin().scrollHorizontally();
                }
            }
        }

        if (editing) {
            editing = false;
            if(viewCell != null){
                viewCell.cancelEdit();
            }
            end();
            if(spreadsheetCellEditor != null){
                spreadsheetCellEditor.end();
            }
        }
    }

    /**
     * Return if this editor is currently being used.
     * 
     * @return if this editor is being used.
     */
    public boolean isEditing() {
        return editing;
    }

    public SpreadsheetCell getModelCell() {
        return modelCell;
    }

    /***************************************************************************
     * * Protected/Private Methods * *
     **************************************************************************/
    void startEdit() {
        //If we do not reset this, it could false the endEdit behavior in case no key was pressed.
        lastKeyPressed = null;
        editing = true;
        
        handle.getGridView().addEventFilter(KeyEvent.KEY_PRESSED, enterKeyPressed);

        handle.getCellsViewSkin().getVBar().valueProperty().addListener(endEditionListener);
        handle.getCellsViewSkin().getHBar().valueProperty().addListener(endEditionListener);
        
        Control editor = spreadsheetCellEditor.getEditor();

        // Then we call the user editor in order for it to be ready
        Object value = modelCell.getItem();
        //We don't want the editor to go beyond the cell boundaries
        Double maxHeight = Math.min(viewCell.getHeight(), spreadsheetCellEditor.getMaxHeight());
        
        if (editor != null) {
            viewCell.setGraphic(editor);
            editor.setMaxHeight(maxHeight);
            editor.setPrefWidth(viewCell.getWidth());
        }

        spreadsheetCellEditor.startEdit(value, modelCell.getFormat(), modelCell.getOptionsForEditor());
        
        if (editor != null) {
            focusProperty = getFocusProperty(editor);
            focusProperty.addListener(focusListener);
        }
    }

    private void end() {
        if(focusProperty != null){
            focusProperty.removeListener(focusListener);
            focusProperty = null;
        }
        handle.getCellsViewSkin().getVBar().valueProperty().removeListener(endEditionListener);
        handle.getCellsViewSkin().getHBar().valueProperty().removeListener(endEditionListener);
        
        handle.getGridView().removeEventFilter(KeyEvent.KEY_PRESSED, enterKeyPressed);

        this.modelCell = null;
        this.viewCell = null;
    }

    /**
     * If we have a TextArea, we need to return a custom BooleanExpression
     * because we want to let the editor in place even if the user is touching
     * the scrollBars inside the textArea.
     *
     * @param control
     * @return
     */
    private BooleanExpression getFocusProperty(Control control) {
        if (control instanceof TextArea) {
            return Bindings.createBooleanBinding(() -> {
                if(handle.getView().getScene() == null){
                    return false;
                }
                for (Node n = handle.getView().getScene().getFocusOwner(); n != null; n = n.getParent()) {
                    if (n == control) {
                        return true;
                    }
                }
                return false;
            }, handle.getView().getScene().focusOwnerProperty());
        } else {
            return control.focusedProperty();
        }
    }
     
    /**
     * When we stop editing a cell, if enter was pressed, we want to go to the next line.
     */
    private final EventHandler<KeyEvent> enterKeyPressed = new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent t) {
            lastKeyPressed = t.getCode();
        }
    };
    
    private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isFocus) {
            if (!isFocus) {
                endEdit(true);
            }
        }
    };
    
    private final InvalidationListener endEditionListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            endEdit(true);
        }
    };
}
