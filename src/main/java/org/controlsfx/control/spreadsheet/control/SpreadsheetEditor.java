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

package org.controlsfx.control.spreadsheet.control;

import org.controlsfx.control.spreadsheet.model.DataCell;

public class SpreadsheetEditor {
	
	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private SpreadsheetRow original;
	private DataCell<?> cell;
	private SpreadsheetCell gc;
	private boolean isMoved;
	private SpreadsheetView spreadsheetView;
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	public void begin(DataCell<?> cell, SpreadsheetCell bc, SpreadsheetView t) {
		this.cell = cell;
		this.gc = bc;
		this.spreadsheetView = t;
	}

	public void startEdit() {
		//Case when RowSpan if larger and we're not on the last row
				if(cell.getRowSpan()>1 && cell.getRow() != spreadsheetView.getVirtualFlowCellSize()-1){
					original = (SpreadsheetRow) gc.getTableRow();
					
					final double temp = gc.getLocalToSceneTransform().getTy();
					isMoved = spreadsheetView.addCell(gc);
					if(isMoved){
						gc.setTranslateY(temp - gc.getLocalToSceneTransform().getTy());
						original.putFixedColumnToBack();
					}
				}
	}

	public void end() {
		if(cell != null && cell.getRowSpan() >1){
			gc.setTranslateY(0);
			if(isMoved){
				original.addCell(gc);
				original.putFixedColumnToBack();
			}
		}
	}
}
