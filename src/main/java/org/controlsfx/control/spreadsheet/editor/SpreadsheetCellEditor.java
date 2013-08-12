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

package org.controlsfx.control.spreadsheet.editor;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

import org.controlsfx.control.SpreadsheetCell;
import org.controlsfx.control.SpreadsheetRow;
import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.property.editor.PropertyEditor;


/**
 *
 * Mother Class for all the possible editors displayed in the {@link SpreadsheetCell}.
 * It reacts to all the possible events in order to submit or cancel the
 * displayed editor or the value entered.
 */
public abstract class SpreadsheetCellEditor<T> implements PropertyEditor<T> {


    /***************************************************************************
     *                                                                         *
     * Protected/Private Fields                                                *
     *                                                                         *
     **************************************************************************/

    // transient properties - these fields will change based on the current
    // cell being edited.
    protected DataCell<T> modelCell;
    protected SpreadsheetCell<T> viewCell;
    protected SpreadsheetView spreadsheetView;

    // private internal fields
    private SpreadsheetEditor<T> spreadsheetEditor;
    private InvalidationListener editorListener;
    private boolean editing = false;

    protected InvalidationListener il;



    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/

    public SpreadsheetCellEditor(){
        this.spreadsheetEditor = new SpreadsheetEditor<T>();
    }



    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/

    public void updateDataCell(DataCell<T> cell) {
        this.modelCell = cell;
    }

    public void updateSpreadsheetCell(SpreadsheetCell<T> cell) {
        this.viewCell = cell;
    }

    public void updateSpreadsheetView(SpreadsheetView spreadsheet) {
        this.spreadsheetView = spreadsheet;
    }

    /**
     * In case the cell is spanning in rows.
     * We want the cell to be fully accessible so we need to remove it from its tableRow
     * and add it to the last row possible. Then we translate the cell so that it's invisible for
     * the user.
     */
    public void startEdit(){
        spreadsheetEditor.startEdit();

        //In ANY case, we stop when something move in scrollBar Vertical
        editorListener = new InvalidationListener() {
            @Override public void invalidated(Observable arg0) {
                commitEdit();

                if (viewCell != null) {
                    viewCell.commitEdit(modelCell);
                }

                end();
            }
        };
        spreadsheetView.getVbar().valueProperty().addListener(editorListener);
    }



    /***************************************************************************
     *                                                                         *
     * Protected Methods                                                       *
     *                                                                         *
     **************************************************************************/
    /**
     * When we have finish editing. We put the cell back to its right TableRow.
     */
    protected void end(){
        editing = false;
        spreadsheetEditor.end();

        spreadsheetView.getVbar().valueProperty().removeListener(editorListener);
        editorListener = null;
    }

    public boolean isEditing(){
        return editing;
    }

    @Override public T getValue() {
        return modelCell == null ? null : modelCell.getCellValue();
    }

    @Override
    public void setValue(T value) {
        if (modelCell != null) {
            modelCell.setCellValue(value);
        }
    }


    /***************************************************************************
     *                                                                         *
     * Protected Abstract Methods                                              *
     *                                                                         *
     **************************************************************************/

    protected abstract void cancelEdit();

    protected abstract DataCell<T> commitEdit();





    private class SpreadsheetEditor<A> {

        /***********************************************************************
         *                                                                     *
         * Private Fields                                                      *
         *                                                                     *
         **********************************************************************/
        private SpreadsheetRow original;
        private boolean isMoved;

        
        
        /***********************************************************************
         *                                                                     *
         * Public Methods                                                      *
         *                                                                     *
         **********************************************************************/

        public void startEdit() {
            //Case when RowSpan if larger and we're not on the last row
            if(modelCell.getRowSpan()>1 && modelCell.getRow() != spreadsheetView.getVirtualFlowCellSize()-1){
                original = (SpreadsheetRow) viewCell.getTableRow();

                final double temp = viewCell.getLocalToSceneTransform().getTy();
                isMoved = spreadsheetView.addCell(viewCell);
                if(isMoved){
                    viewCell.setTranslateY(temp - viewCell.getLocalToSceneTransform().getTy());
                    original.putFixedColumnToBack();
                }
            }
        }

        public void end() {
            if(modelCell != null && modelCell.getRowSpan() >1){
                viewCell.setTranslateY(0);
                if(isMoved){
                    original.addCell(viewCell);
                    original.putFixedColumnToBack();
                }
            }
        }
    }
}
