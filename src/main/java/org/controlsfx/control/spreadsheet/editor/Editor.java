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
import javafx.scene.control.Control;

import org.controlsfx.control.spreadsheet.control.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.control.SpreadsheetEditor;
import org.controlsfx.control.spreadsheet.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;


/**
 *
 * Mother Class for all the possible editors displayed in the {@link SpreadsheetCell}.
 * It reacts to all the possible events in order to submit or cancel the
 * displayed editor or the value entered.
 */
public abstract class Editor{
	/***************************************************************************
     *                                                                         *
     * Protected/Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	protected DataCell<?> cell;
	protected SpreadsheetCell spreadsheetCell;
	protected SpreadsheetView spreadsheetView;
	private SpreadsheetEditor spreadsheetEditor;
	private InvalidationListener editorListener;
	private boolean working = false;
	
	
	protected InvalidationListener il;
	
	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	
	public Editor(){
		this.spreadsheetEditor = new SpreadsheetEditor();
	}

	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	/**
	 * Initialization of the Editor, called by SpreadsheetView.
	 * @param cell
	 * @param bc
	 * @param t
	 */
	public void begin(DataCell<?> cell, SpreadsheetCell bc, SpreadsheetView t){
		working = true;
		spreadsheetEditor.begin(cell, bc, t);
		this.spreadsheetView = t;
		begin(cell, bc);
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
			@Override
			public void invalidated(Observable arg0) {
				commitEdit();
				spreadsheetCell.commitEdit(cell);
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
		working = false;
		spreadsheetEditor.end();
		
		spreadsheetView.getVbar().valueProperty().removeListener(editorListener);
		editorListener = null;
	}

	public boolean isWorking(){
		return working;
	}
	/***************************************************************************
     *                                                                         *
     * Protected Abstract Methods                                                          *
     *                                                                         *
     **************************************************************************/
	protected abstract void attachEnterEscapeEventHandler();

	protected abstract void begin(DataCell<?> cell, SpreadsheetCell bc);

	protected abstract void cancelEdit();

	protected abstract DataCell<?> commitEdit();

	protected abstract Control getControl();
}
