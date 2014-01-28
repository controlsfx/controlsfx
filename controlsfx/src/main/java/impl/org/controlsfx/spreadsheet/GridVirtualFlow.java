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
package impl.org.controlsfx.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Region;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.VirtualFlow;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;

final class GridVirtualFlow<T extends IndexedCell<?>> extends VirtualFlow<T> {
    
    private static final Comparator<GridRow> ROWCMP = new Comparator<GridRow>() {
        @Override
        public int compare(GridRow o1, GridRow o2) {
            final int lhs = o1.getIndex();
            final int rhs = o2.getIndex();
            return lhs < rhs ? -1 : +1;
        }
    };

    /***************************************************************************
     * * Private Fields * *
     **************************************************************************/
    private SpreadsheetView spreadSheetView;
    private GridViewSkin gridViewSkin;
    /**
     * Variable used for improvement;
     */
//    private int firstIndex = -1;
//    private double previousHbarValue = -1;
//    private double previousHBarAmount = -1;
//    private boolean copyPaste = true;//We're copy/pasting so layout is necessary
    /**
     * Store the fixedRow in order to place them at the top when necessary.
     * That is to say, when the VirtualFlow has not already placed one.
     */
    private ArrayList<T> myFixedCells = new ArrayList<>();
    private List<Node> sheetChildren;
    
    /***************************************************************************
     * * Constructor * *
     **************************************************************************/
    public GridVirtualFlow(GridViewSkin gridViewSkin) {
        super();
        this.gridViewSkin = gridViewSkin;
        final ChangeListener<Number> listenerY = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
                layoutTotal();
            }
        };
        getVbar().valueProperty().addListener(listenerY);
        

        // FIXME Until RT-31777 is resolved
        getHbar().setUnitIncrement(10);
        
        sheetChildren = findSheetChildren();
    }

    /**
     * WARNING : This is bad but no other options right now.
     * This will find the sheetChildren of the VirtualFlow, 
     * aka where the cells are kept and clipped. See layoutFixedRows() for use.
     * 
     * @return
     */
    private List<Node> findSheetChildren(){
        if(!getChildren().isEmpty()){
            if(getChildren().get(0) instanceof Region){
                Region region = (Region) getChildren().get(0);
                if(!region.getChildrenUnmodifiable().isEmpty()){
                    if(region.getChildrenUnmodifiable().get(0) instanceof Group){
                        return ((Group)region.getChildrenUnmodifiable().get(0)).getChildren();
                    }
                }
            }
        }
        return new ArrayList<>();
    }
    /***************************************************************************
     * * Public Methods * *
     **************************************************************************/
    public void init(SpreadsheetView spv) {
        this.spreadSheetView = spv;
       
        spv.getFixedRows().addListener(new ListChangeListener<Integer>(){
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Integer> arg0) {
				while(arg0.next()){
					if(arg0.wasRemoved()){
						List<? extends Integer> list = arg0.getRemoved();
						for(Integer i:list){
							for(T cell:myFixedCells){
								if(cell.getIndex() == i){
									cell.setManaged(false);
									cell.setVisible(false);
									myFixedCells.remove(cell);
									break;
								}
							}
						}
					}
				}
			}
		});
    }

    @Override
    public void show(int index) {
        super.show(index);
        layoutTotal();
        layoutFixedRows();
    }

    @Override
    public void scrollTo(int index) {
    	//If we have some fixedRows, we check if the selected row is not below them
    	if(!getCells().isEmpty() && spreadSheetView.getFixedRows().size()>0){
    		double offset = gridViewSkin.getFixedRowHeight();
    		
			while(offset >=0 && index >0){
				index--;
				offset-=spreadSheetView.getGrid().getRowHeight(index);
			}
        }
        super.scrollTo(index);

        layoutTotal();
        layoutFixedRows();
    }

    @Override
    public double adjustPixels(final double delta) {
        final double returnValue = super.adjustPixels(delta);

        layoutTotal();
        layoutFixedRows();

        return returnValue;
    }

    /***************************************************************************
     * * Protected Methods * *
     **************************************************************************/

    @Override
    protected void layoutChildren() {
        // We don't want to layout everything in case we're editing 
        // because it has no sense
        if (spreadSheetView != null
                && (spreadSheetView.getEditingCell() == null || spreadSheetView
                        .getEditingCell().getRow() == -1)) {
            sortRows();
            super.layoutChildren();
            layoutTotal();
            layoutFixedRows();
        }
    }

    /**
     * Layout all the visible rows
     */
    protected void layoutTotal() {
        sortRows();
        
        // When scrolling fast with fixed Rows, cells is empty and not recreated..
        if (getCells().isEmpty()) {
            reconfigureCells();
        }
        
        //FIXME This is put on hold until performance review.
        
        /*//We do not need to layout if nothing has changed really..
        //FIXME This will be improved for performance.
        T firstCell = getFirstVisibleCellWithinViewPort();
        int newFirstIndex = firstCell != null? firstCell.getIndex(): -2;
        if(copyPaste &&
        		newFirstIndex == firstIndex 
        		&& previousHbarValue == getHbar().getValue() 
        		&& previousHBarAmount == getHbar().getVisibleAmount()){
        	return;
        }
        
        firstIndex = newFirstIndex;
        previousHbarValue = getHbar().getValue();
        previousHBarAmount = getHbar().getVisibleAmount();*/
        
        for (Cell<?> cell : getCells()) {
            if (cell != null) {
                cell.requestLayout();
            }
        }
    }

    protected VirtualScrollBar getVerticalBar() {
        return getVbar();
    }
    protected VirtualScrollBar getHorizontalBar() {
        return getHbar();
    }

    @Override
    protected List<T> getCells() {
        return super.getCells();
    }

    /***************************************************************************
     * * Private Methods * *
     **************************************************************************/

    /**
     * Layout the fixed rows to position them correctly
     */
    private void layoutFixedRows() {

		//We must have a cell in ViewPort because otherwise
        //we short-circuit the VirtualFlow.
        if (spreadSheetView.getFixedRows().size() > 0 && getFirstVisibleCellWithinViewPort() != null) {
            sortRows();
            /**
             * What I do is just going after the VirtualFlow in order to ADD
             * (not replace like before) new rows at the top.
             *
             * If the VirtualFlow has the row, then I will hide mine and let him
             * handle. But if the row is missing, then I must show mine in order
             * to have the fixed row.
             */
            T cell = null;
            Integer fixedRowIndex;
            
            rows:
            for (int i = spreadSheetView.getFixedRows().size() - 1; i >= 0; i--) {
                fixedRowIndex = spreadSheetView.getFixedRows().get(i);
                //If the fixed row is out of bounds
                if (fixedRowIndex > getLastVisibleCellWithinViewPort().getIndex()) {
                    if (cell != null) {
                        cell.setVisible(false);
                        cell.setManaged(false);
                    }
                    continue rows;
                }

                //We see if the row is laid out by the VirtualFlow
                for (T virtualFlowCells : getCells()) {
                	if (virtualFlowCells.getIndex() > fixedRowIndex) {
                        break;
                    }else if (virtualFlowCells.getIndex() == fixedRowIndex) {
                        cell = containsRows(fixedRowIndex);
                        if (cell != null) {
                            cell.setVisible(false);
                            cell.setManaged(false);
                        }
                        virtualFlowCells.toFront();
                        continue rows;
                    }
                }
                
                cell = containsRows(fixedRowIndex);
                if (cell == null) {
                    /**
                     * getAvailableCell is not added our cell to the ViewPort in some cases.
                     * So we need to instantiate it ourselves.
                     */
                    cell = getCreateCell().call(this);
                    cell.getProperties().put("newcell", null);
//                	cell = getAvailableCell(fixedRowIndex);
                	 
                    setCellIndex(cell, fixedRowIndex);
                    resizeCellSize(cell);
                    myFixedCells.add(cell);
                }
                
                /**
                 * Sometime, when we set a new Grid on a SpreadsheetView without recreating it,
                 * we can end up with some rows not being added to the ViewPort.
                 * So we must be sure it's in and add it ourself otherwise.
                 */
                if(!sheetChildren.contains(cell)){
                    sheetChildren.add(cell);
                }
               
                cell.setManaged(true);
                cell.setVisible(true);
                cell.toFront();
                cell.requestLayout();
            }
        }
    }

    /**
     * Verify if the row has been added to myFixedCell
     * @param i
     * @return
     */
    private T containsRows(int i){
    	for(T cell:myFixedCells){
    		if(cell.getIndex() == i)
    			return cell;
    	}
    	return null;
    }
    /**
     * Sort the rows so that they stay in order for layout
     */
    private void sortRows() {
        final List<GridRow> temp = (List<GridRow>) getCells();
        final List<GridRow> tset = new ArrayList<>(temp);
        Collections.sort(tset, ROWCMP);
        for (final TableRow<ObservableList<SpreadsheetCell>> r : tset) {
            r.toFront();
        }
    }
}

