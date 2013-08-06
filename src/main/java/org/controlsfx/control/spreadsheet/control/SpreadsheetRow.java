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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;

import org.controlsfx.control.spreadsheet.model.DataRow;
import org.controlsfx.control.spreadsheet.skin.SpreadsheetRowSkin;


/**
 *
 * The tableRow which will holds the SpreadsheetCell.
 */
public class SpreadsheetRow extends TableRow<DataRow>{

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private final SpreadsheetView spreadsheetView;
	/**
	 * This is the index used by the VirtualFlow
	 * So the row can be with indexVirtualFlow at 32
	 * But if it is situated in the header, his index will be 0 (or the row in the header)
	 */
	private Integer indexVirtualFlow = null;
	private boolean layoutFixedColumns = false;

	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	public SpreadsheetRow(SpreadsheetView spreadsheetView) {
		super();
		this.spreadsheetView = spreadsheetView;
	}

	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	
	public int getIndexVirtualFlow(){
		return indexVirtualFlow == null?getIndex():indexVirtualFlow;
	}
	@Override
	public void updateIndex(int i){
		
		for (int j = 0; j<spreadsheetView.getVirtualFlowCellSize();j++ ) {
			if(spreadsheetView.getRow(j) != null){
				if(spreadsheetView.getRow(j).getIndexVirtualFlow() == i){
					
					System.out.println("PROBLEM2");
				}
			}
		}
		super.updateIndex(i);
	}
	public void setIndexVirtualFlow(int i){
		
		indexVirtualFlow = i;
	}
	
	/**
	 * For the fixed columns in order to just re-layout the fixed columns
	 * @param b
	 */
	public void setLayoutFixedColumns(boolean b){
		layoutFixedColumns = b;
	}

	public boolean getLayoutFixedColumns(){
		return layoutFixedColumns;
	}

	/**
	 * When unfixing some Columns, we need to put the previously FixedColumns back
	 * if we want the hover to be dealt correctly
	 * @param size
	 */
	public void putFixedColumnToBack() {
		final List<Node> tset = new ArrayList<>(getChildren());
		tset.sort( new Comparator<Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				// In case it's null (some rows are initiated after rowCount)
				if(((SpreadsheetCell)o1).getItem() == null || ((SpreadsheetCell)o2).getItem() == null){
					return -1;
				}
				final int lhs = getTableView().getColumns().indexOf(((SpreadsheetCell) o1).getTableColumn());
				final int rhs = getTableView().getColumns().indexOf(((SpreadsheetCell) o2).getTableColumn());
				if (lhs < rhs) {
					return -1;
				}
				if (lhs > rhs) {
					return +1;
				}
				return 0;

			}
		});
		getChildren().setAll(tset);
	}

	
	public void addCell(SpreadsheetCell cell){
		getChildren().add(cell);
	}

	public void removeCell(SpreadsheetCell gc) {
		getChildren().remove(gc);
	}
	
	/***************************************************************************
     *                                                                         *
     * Protected Methods                                                       *
     *                                                                         *
     **************************************************************************/
	
	SpreadsheetView getSpreadsheetView() {
		return spreadsheetView;
	}
	
	/**
	 * Set this SpreadsheetRow hoverProperty
	 * @param hover
	 */
	protected void setHoverPublic(boolean hover) {
		this.setHover(hover);
	}
	
	/**
	 * Return the SpreadsheetCell at the specified column.
	 * We have to be careful because if we have fixedColumns
	 * then the fixedColumns cells will be at the end of the Children's List
	 * @param col
	 * @return the corresponding SpreadsheetCell
	 */
	SpreadsheetCell getGridCell(int col){
		int fixedColSize;
		if((fixedColSize =spreadsheetView.getFixedColumns().size() ) != 0){
			if(col < fixedColSize){
				return (SpreadsheetCell) getChildrenUnmodifiable().get(getChildrenUnmodifiable().size() - fixedColSize + col);
			} else {
				return (SpreadsheetCell) getChildrenUnmodifiable().get( col- fixedColSize );
			}
		}else{
			return (SpreadsheetCell) getChildrenUnmodifiable().get(col);
		}
	}
	
	@Override
	protected Skin<?> createDefaultSkin() {
		return new SpreadsheetRowSkin<>(this,spreadsheetView);
	}

}
