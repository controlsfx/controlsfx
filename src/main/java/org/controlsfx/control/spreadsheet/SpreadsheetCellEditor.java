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

import impl.org.controlsfx.skin.SpreadsheetViewSkin;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import org.controlsfx.property.editor.PropertyEditor;

/**
 * 
 * SpreadsheetCellEditor are used by {@link SpreadsheetCell} in order to control how each value will be entered.
 * <br/>
 * 
 * <h3>General behavior: </h3>
 * Editors will be displayed if the user double-click or press enter in an editable cell ( see {@link SpreadsheetCell#setEditable(boolean)} ).
 * <br/>
 * If the user does anything outside the editor, the editor <b> will be forced </b> to cancel the edition and close itself. 
 * Each editor has its own policy regarding validation of the value entered. This policy is
 * define by each editor in the {link {@link #validateEdit()}} method.
 *  If the value doesn't meet the requirements when saving the cell, nothing happens and the editor keep editing.
 * <br/>
 * You can abandon a current modification by pressing "esc" key. 
 * <br/>
 * 
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
 * 
 * @see SpreadsheetView
 * @see SpreadsheetCellEditors
 * @see SpreadsheetCell
 */
public abstract class SpreadsheetCellEditor<T> implements PropertyEditor<T>  {

    /***************************************************************************
     * * Constructor * *
     **************************************************************************/

    /**
     * Construct the SpreadsheetCellEditor.
     */
    public SpreadsheetCellEditor() {
    }

    /***************************************************************************
     * * Public Final Methods * *
     **************************************************************************/
    /**
     * Return the {@link SpreadsheetCell#getItem()} associated with the editor.
     */
    @Override
    public final T getValue() {
    	SpreadsheetCell<T> cell = (SpreadsheetCell<T>) SpreadsheetViewSkin.getSkin().getSpreadsheetCellEditorImpl().getModelCell();
        return cell == null ? null : cell.getItem();
    }
    
    @Override
    public final void setValue(T value) {
        SpreadsheetCell<T> cell = (SpreadsheetCell<T>) SpreadsheetViewSkin.getSkin().getSpreadsheetCellEditorImpl().getModelCell();
        if (cell != null) {
    	   cell.setItem(value);
        }
    }

    /**
     * Return the {@link SpreadsheetCell#getProperties()} associated with
     * the string in parameter.
     * @param key The key which has a Object associated with in {@link SpreadsheetCell#getProperties()}
     * @return
     */
    public final Object getProperties(String key){
    	return SpreadsheetViewSkin.getSkin().getSpreadsheetCellEditorImpl().getModelCell().getProperties().get(key);
    }
    
    /**
     * Whenever you want to stop the edition, you call that method.<br/>
     * True means you're trying to commit the value, then {@link #validateEdit()}
     * will be called in order to verify that the value is correct.<br/>
     * False means you're trying to cancel the value and it will be follow by {@link #end()}.<br/>
     * See SpreadsheetCellEditor description
     * @param b true means commit, false means cancel
     */
    public final void endEdit(boolean b){
    	SpreadsheetViewSkin.getSkin().getSpreadsheetCellEditorImpl().endEdit(b);
    }

    
    /***************************************************************************
     * * Public Abstract Methods * *
     **************************************************************************/
    /**
     * This method will be called when edition start.<br/>
     * You will then do all the configuration of your editor.
     */
    public abstract void startEdit();
    
    /**
     * This method will be called when a commit is happening.<br/>
     * You will then compute the value of the editor in order to determine
     * if the current value is valid.
     * @return null if not valid or the correct value otherwise.
     */
    public abstract T validateEdit();
	
    /**
     * This method will be called at the end of edition.<br/>
     * You will be offered the possibility to do the configuration
     * post editing.
     */
    public abstract void end();

}
