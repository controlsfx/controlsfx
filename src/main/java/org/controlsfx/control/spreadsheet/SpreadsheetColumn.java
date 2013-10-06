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

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;

/**
 * A {@link SpreadsheetView} is made up of a number of {@link SpreadsheetColumn} instances.
 * 
 * <h3>Configuration</h3>
 * SpreadsheetColumns are instantiated by the {@link SpreadsheetView} itself, so
 * there is no public constructor for this class.
 * <br/>
 * What you can do is modify some informations like the width of the column with {@link #setPrefWidth(double)} or
 * if you want it to be resizable with {@link #setResizable(boolean)}.
 * <br/>
 * You have the ability to fix this column at the left of the SpreadsheetView by calling {@link #setFixed(boolean)}. 
 * But you are strongly advised to check if it's possible with {@link #isColumnFixable()}.
 * Take a look at the {@link SpreadsheetView} description to understand the fixing constraints.
 * 
 * <br/>
 * If the column can be fixed, a {@link ContextMenu} will appear if the user right-click on the header. 
 * If not, nothing will appear and the user will not have the possibility to fix it.
 * <br/>
 * 
 * <h3>Visual: </h3>
 * The column <b> A </b> is fixed and is covering column <b> B </b> and partially column <b> C </b>. 
 * <br/>
 * The context menu is being shown and offer the possibility to unfix the column.
 * 
 * <br/><br/>
 * <center><img src="fixedColumn.png"></center>
 * @see SpreadsheetView
 */
public class SpreadsheetColumn<T> {

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private SpreadsheetView spreadsheetView;
	private TableColumn<ObservableList<SpreadsheetCell<T>>, SpreadsheetCell<T>> column;
	private boolean canFix;
	private Integer indexColumn;
	private CheckMenuItem fixItem;

	
	
	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	/**
	 * Creates a new SpreadsheetColumn with a minimum width of 30.
	 * @param column
	 * @param spreadsheetView
	 * @param indexColumn
	 */
	SpreadsheetColumn(final TableColumn<ObservableList<SpreadsheetCell<T>>, SpreadsheetCell<T>> column, SpreadsheetView spreadsheetView, Integer indexColumn) {
		this.spreadsheetView = spreadsheetView;
		this.column = column;
		column.setMinWidth(30); 
		this.indexColumn = indexColumn;
		canFix = initCanFix();
		
		// The contextMenu creation must be on the JFX thread
		final Runnable r = new Runnable() {
            @Override
            public void run() {
            	column.setContextMenu(getColumnContextMenu());
            }
        };
        Platform.runLater(r);
		
		//FIXME implement better listening after
		spreadsheetView.getGrid().getRows().addListener(new ListChangeListener<ObservableList<SpreadsheetCell<?>>>(){
			@Override public void onChanged(Change<? extends ObservableList<SpreadsheetCell<?>>> arg0) {
				initCanFix();
			}
		});
	}
	
	
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	
	/**
	 * Return whether this column is fixed or not.
	 * @return true if this column is fixed.
	 */
	public boolean isFixed() {
		return spreadsheetView.getFixedColumns().contains(this);
	}
	
	/**
	 * Fix this column to the left if possible.
	 * Call {@link #isColumnFixable()} before trying to fix a column.
	 * Visual confirmation is Label in italic
	 * @param fixed
	 */
	public void setFixed(boolean fixed) {
	    if (fixed) {
	        spreadsheetView.getFixedColumns().add(this);
	    } else {
	        spreadsheetView.getFixedColumns().removeAll(this);
	    }
	}
	
	/**
	 * Set the width of this column
	 * @param arg0
	 */
	public void setPrefWidth(double arg0){
		column.setPrefWidth(arg0);
	}
	/**
	 * Return the Actual width of the column
	 * @return {@link TableColumn#getWidth()}
	 */
	public double getWidth(){
		return column.getWidth();
	}
	
	/**
	 * If this column can be resized by the user
	 * @param b
	 */
	public void setResizable(boolean b){
		column.setResizable(b);
	}
	
	/**
	 * Indicate whether this column can be fixed or not.
	 * Call that method before calling {@link #setFixed(boolean)} or
	 * adding an item to {@link SpreadsheetView#getFixedColumns()}.
	 * @return true if this column is fixable.
	 */
	public boolean isColumnFixable(){
		return canFix;
	}
	
	
	
	/***************************************************************************
     *                                                                         *
     * Private Methods                                               		   *
     *                                                                         *
     **************************************************************************/
	
	/**
     * Generate a context Menu in order to fix/unfix some column
     * It is shown when right-clicking on the column header
     * @return a context menu.
     */
    private ContextMenu getColumnContextMenu(){
    	if(canFix){
	    	final ContextMenu contextMenu = new ContextMenu();
	
	    	this.fixItem = new CheckMenuItem("Fix");
	    	fixItem.selectedProperty().addListener(new ChangeListener<Boolean>(){
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0,
						Boolean arg1, Boolean arg2) {
					
						if(!isFixed()){
							setFixed(true);
						}else{
							setFixed(false);
						}
				}
	        });
	        contextMenu.getItems().addAll(fixItem);
	        
	        return contextMenu;
    	}else{
    		return new ContextMenu();
    	}
    }
    
    /**
	 * Verify that you can fix this column.
	 * Right now, only a column without any cell spanning 
	 * can be fixed.
	 * 
	 * @return if it's fixable.
	 */
	private boolean initCanFix(){
		for (ObservableList<SpreadsheetCell<?>> row : spreadsheetView.getGrid().getRows()) {
			int columnSpan = row.get(indexColumn).getColumnSpan();
			if(columnSpan >1 || row.get(indexColumn).getRowSpan()>1)
				return false;
//			}else if(columnSpan>1){
//				columnSpanConstraint = columnSpanConstraint>columnSpan?columnSpanConstraint:columnSpan;
//			}
		}
		return true;
	}
    
}
