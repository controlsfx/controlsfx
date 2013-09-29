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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;


/**
 *
 * The tableRow which will holds the SpreadsheetCell.
 */
public class SpreadsheetRowImpl extends TableRow<ObservableList<SpreadsheetCell<?>>>{

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
    private Boolean currentlyFixed = false;

    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
    public SpreadsheetRowImpl(SpreadsheetView spreadsheetView) {
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
    
    public void setIndexVirtualFlow(int i){
        indexVirtualFlow = i;
    }

    public Boolean getCurrentlyFixed() {
		return currentlyFixed;
	}

	/**
	 * Indicate that this row is bonded on the top.
	 * @param currentlyFixed
	 */
	public void setCurrentlyFixed(Boolean currentlyFixed) {
		this.currentlyFixed = currentlyFixed;
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
                if(((SpreadsheetCellImpl<?>)o1).getItem() == null || ((SpreadsheetCellImpl<?>)o2).getItem() == null){
                    return -1;
                }
                final int lhs = getTableView().getColumns().indexOf(((SpreadsheetCellImpl<?>) o1).getTableColumn());
                final int rhs = getTableView().getColumns().indexOf(((SpreadsheetCellImpl<?>) o2).getTableColumn());
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


    public void addCell(SpreadsheetCellImpl<?> cell){
        getChildren().add(cell);
    }

    public void removeCell(SpreadsheetCellImpl<?> gc) {
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
    void setHoverPublic(boolean hover) {
        this.setHover(hover);
    }

    /**
     * Return the SpreadsheetCell at the specified column.
     * We have to be careful because if we have fixedColumns
     * then the fixedColumns cells will be at the end of the Children's List
     * @param col
     * @return the corresponding SpreadsheetCell
     */
    SpreadsheetCellImpl<?> getGridCell(int col){
    	
    	// Too much complication for a minor effect, simple is better.
 	/*	final int max = getChildrenUnmodifiable().size()-1;
 		int j = max;
 		while(j>= 0 && ((SpreadsheetCellImpl<?>)getChildrenUnmodifiable().get(j)).getItem().getColumn() != max){
 			--j;
 		}
 		
    	int fixedColSize = j == -1? 0:max -j;
    	
    	//If any cells was moved to the end
        if(fixedColSize != 0){
        	//if the requested column is fixed
            if(spreadsheetView.getColumns().get(col).getCurrentlyFixed()){
            	final int indexCol = spreadsheetView.getFixedColumns().indexOf(col);
                return (SpreadsheetCellImpl<?>) getChildrenUnmodifiable().get(getChildrenUnmodifiable().size() + indexCol - fixedColSize);
            } else {
                return (SpreadsheetCellImpl<?>) getChildrenUnmodifiable().get( col- fixedColSize );
            }
        }else{
            return (SpreadsheetCellImpl<?>) getChildrenUnmodifiable().get(col);
        }*/
    	
        for(Node cell:getChildrenUnmodifiable()){
        	if(((SpreadsheetCellImpl<?>)cell).getItem().getColumn() == col){
        		return ((SpreadsheetCellImpl<?>)cell);
        	}
        }
        return null;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SpreadsheetRowSkin<>(this,spreadsheetView);
    }

}
