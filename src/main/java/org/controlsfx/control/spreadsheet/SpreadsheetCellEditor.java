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
package org.controlsfx.control.spreadsheet;

import impl.org.controlsfx.skin.SpreadsheetCellImpl;
import impl.org.controlsfx.skin.SpreadsheetRowImpl;
import impl.org.controlsfx.skin.SpreadsheetViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import org.controlsfx.control.spreadsheet.SpreadsheetCell.CellType;
import org.controlsfx.property.editor.PropertyEditor;

/**
 * 
 * SpreadsheetCellEditor are used by {@link SpreadsheetCell} in order to control how each value will be entered.
 * <br/>
 * 
 * <h3>General behavior: </h3>
 * Editors will be displayed if the user double-click or press enter in an editable cell ( see {@link SpreadsheetCell#setEditable(boolean)} ).
 * <br/>
 * If the user does anything outside the editor, it will try to save the value and it will close itself. Each editor has its own policy regarding
 * validation of the value entered. If the value doesn't meet the requirements when saving the cell, the old value is used. 
 * <br/>
 * You can abandon a current modification by pressing "esc" key. 
 * <br/>
 * <h3>Specific behavior: </h3>
 * Each editor is linked with a specific {@link CellType}. Here are their properties:
 * <br/>
 * 
 * <ul>
 *   <li> String: Basic {@link TextField}, can accept all data and save it as a string.</li>
 *   <li> List: Display a {@link ComboBox} with the different values.</li>
 *   <li> Double: Display a {@link TextField} which accepts only double value. If the entered value is incorrect,
 *   the background will turn red so that the user will know in advance if the data will be saved or not.</li>
 *   <li> Date: Display a {@link DatePicker}.</li>
 * </ul>
 * 
 * <br/>
 * <h3>Visual: </h3>
 * <table style="border: 1px solid gray;">
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>String</strong></td>
 *     <td><center><img src="textEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>List</strong></td>
 *     <td><center><img src="listEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Double</strong></td>
 *     <td><center><img src="doubleEditor.png"></center></td>
 *   </tr>
 *   <tr>
 *     <td valign="center" style="text-align:right;"><strong>Date</strong></td>
 *     <td><center><img src="dateEditor.png"></center></td>
 *   </tr>
 *  </table>
 * 
 */
public abstract class SpreadsheetCellEditor<T> implements PropertyEditor<T> {

    /***************************************************************************
     * * Protected/Private Fields * *
     **************************************************************************/

    // transient properties - these fields will change based on the current
    // cell being edited.
    protected SpreadsheetCell<T> modelCell;
    protected SpreadsheetCellImpl<T> viewCell;
    protected SpreadsheetView spreadsheetView;

    // private internal fields
    private SpreadsheetEditor<T> spreadsheetEditor;
    private InvalidationListener editorListener;
    private boolean editing = false;

    protected InvalidationListener il;

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    public SpreadsheetCellEditor() {
        this.spreadsheetEditor = new SpreadsheetEditor<T>();
    }

    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/

    public void updateDataCell(SpreadsheetCell<T> cell) {
        this.modelCell = cell;
    }

    public void updateSpreadsheetCell(SpreadsheetCellImpl<T> cell) {
        this.viewCell = cell;
    }

    public void updateSpreadsheetView(SpreadsheetView spreadsheet) {
        this.spreadsheetView = spreadsheet;
    }

    /**
     * In case the cell is spanning in rows. We want the cell to be fully
     * accessible so we need to remove it from its tableRow and add it to the
     * last row possible. Then we translate the cell so that it's invisible for
     * the user.
     */
    public void startEdit() {
    	editing = true;
        spreadsheetEditor.startEdit();

        // In ANY case, we stop when something move in scrollBar Vertical
        editorListener = new InvalidationListener() {
            @Override
            public void invalidated(Observable arg0) {
                commitEdit();

                if (viewCell != null) {
                    viewCell.commitEdit(modelCell);
                }

                end();
            }
        };
        SpreadsheetViewSkin.getSkin().getVBar().valueProperty().addListener(editorListener);
    }

    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/
    /**
     * When we have finish editing. We put the cell back to its right TableRow.
     */
    protected void end() {
        editing = false;
        spreadsheetEditor.end();

        SpreadsheetViewSkin.getSkin().getVBar().valueProperty().removeListener(editorListener);
        editorListener = null;
    }

    public boolean isEditing() {
        return editing;
    }

    @Override
    public T getValue() {
        return modelCell == null ? null : modelCell.getCellValue();
    }

    @Override
    public void setValue(T value) {
        if (modelCell != null) {
            modelCell.setCellValue(value);
        }
    }

    /***************************************************************************
     * * Protected Abstract Methods * *
     **************************************************************************/

    protected abstract void cancelEdit();

    protected abstract SpreadsheetCell<T> commitEdit();

    private class SpreadsheetEditor<A> {

        /***********************************************************************
         * * Private Fields * *
         **********************************************************************/
        private SpreadsheetRowImpl original;
        private boolean isMoved;

        private int getCellCount() {
            return SpreadsheetViewSkin.getSkin().getCellsSize();
        }
        
        private boolean addCell(SpreadsheetCellImpl<?> cell){
            SpreadsheetRowImpl temp = SpreadsheetViewSkin.getCell(spreadsheetView, getCellCount()-1-spreadsheetView.getFixedRows().size());
            if(temp != null){
                temp.addCell(cell);
                return true;
            }
            return false;
        }
        /***********************************************************************
         * * Public Methods * *
         **********************************************************************/

        public void startEdit() {
            // Case when RowSpan if larger and we're not on the last row
            if (modelCell != null && modelCell.getRowSpan() > 1
                    && modelCell.getRow() != getCellCount() - 1) {
                original = (SpreadsheetRowImpl) viewCell.getTableRow();

                final double temp = viewCell.getLocalToSceneTransform().getTy();
                isMoved = addCell(viewCell);
                if (isMoved) {
                    viewCell.setTranslateY(temp
                            - viewCell.getLocalToSceneTransform().getTy());
                    original.putFixedColumnToBack();
                }
            }
        }

        public void end() {
            if (modelCell != null && modelCell.getRowSpan() > 1) {
                viewCell.setTranslateY(0);
                if (isMoved) {
                    original.addCell(viewCell);
                    original.putFixedColumnToBack();
                }
            }
        }
    }
}
