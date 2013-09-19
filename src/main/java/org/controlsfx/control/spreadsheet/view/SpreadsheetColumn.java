package org.controlsfx.control.spreadsheet.view;

import impl.org.controlsfx.skin.SpreadsheetRow;
import impl.org.controlsfx.skin.SpreadsheetRowSkin;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.DataCell;
import org.controlsfx.control.spreadsheet.model.DataRow;

import com.sun.javafx.scene.control.skin.TableColumnHeader;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;

/**
 *A {@link SpreadsheetView} is made up of a number of {@link SpreadsheetColumn} instances.
 * You can then modify some informations like the width of the column or whether it is fixed or not.
 */
public class SpreadsheetColumn{

	/***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/
	private SpreadsheetView spreadsheetView;
	private TableColumn<DataRow, DataCell<?>> column;
	private boolean canFix;
	private Integer indexColumn;
	private CheckMenuItem fixItem;
	/**
	 * if we have to fix some column together, how many columns we have to fix
	 */
//	private Integer columnSpanConstraint;
	/**
	 * if we have to fix some column together, the starting column
	 */
//	private Integer columnStart;
	
	/***************************************************************************
     *                                                                         *
     * Package protected Fields                                                *
     *                                                                         *
     **************************************************************************/
	/**
	 * Indicate whether or not this column is currently fixed on the left.
	 * The column can be fixed, but has not moved yet because we have not exceed it.
	 * It is needed for the HoverProperty. See {@link SpreadsheetRow} and {@link SpreadsheetRowSkin} implementation.
	 */
	Boolean currentlyFixed = false;

	/***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	/**
	 * Creates a new SpreadsheetColumn with an initial width of 100.
	 * @param column
	 * @param spreadsheetView
	 * @param indexColumn
	 */
	public SpreadsheetColumn(TableColumn<DataRow, DataCell<?>> column, SpreadsheetView spreadsheetView, Integer indexColumn) {
		this.spreadsheetView = spreadsheetView;
		this.column = column;
//		column.setPrefWidth(100);
		this.indexColumn = indexColumn;
//		this.columnSpanConstraint = 0;
//		this.columnSpanConstraint = 0;
		canFix = canFix();
		column.setContextMenu(getColumnContextMenu());
		
		//FIXME implement better listening after
		spreadsheetView.getGrid().getRows().addListener(new ListChangeListener<DataRow>(){

			@Override
			public void onChanged(
					javafx.collections.ListChangeListener.Change<? extends DataRow> arg0) {
				canFix();
			}});
	}
	
	/***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
	/**
	 * Return whether this column is fixed or not.
	 * @return
	 */
	public boolean isFixed() {
		return column.impl_isFixed();
	}
	
	/**
	 * Verify that you can fix this column.
	 * Right now, only a column without any cell spanning 
	 * can be fixed.
	 * 
	 * @return
	 */
	public boolean canFix(){
		for (DataRow row : spreadsheetView.getGrid().getRows()) {
			int columnSpan = row.get(indexColumn).getColumnSpan();
			if(columnSpan >1 || row.get(indexColumn).getRowSpan()>1)
				return false;
//			}else if(columnSpan>1){
//				columnSpanConstraint = columnSpanConstraint>columnSpan?columnSpanConstraint:columnSpan;
//			}
		}
		return true;
	}
	
	/**
	 * Fix this column to the left (if possible)
	 * Visual confirmation is Label in italic
	 * @param fixed
	 */
	public void setFixed(boolean fixed) {
		if(canFix){
//			if(columnSpanConstraint == 0){
				column.impl_setFixed(fixed);
				
				//Just a visual confirmation, will be removed
				if(column.impl_isFixed()){
					spreadsheetView.getFixedColumns().add(indexColumn);
				}else{
					spreadsheetView.getFixedColumns().remove(Integer.valueOf(indexColumn));
				}
				FXCollections.sort(spreadsheetView.getFixedColumns());
//			}else{
//				for(int i=indexColumn;i<columnSpanConstraint;++i){
//					
//				}
//			}
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
	 * @return
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
	
	public Boolean getCurrentlyFixed() {
		return currentlyFixed;
	}

	/**
	 * Indicate that this column is bonded on the left because the hbar has 
	 * exceed the column normal position.
	 * @param currentlyFixed
	 */
	public void setCurrentlyFixed(Boolean currentlyFixed) {
		this.currentlyFixed = currentlyFixed;
	}
	
	/***************************************************************************
     *                                                                         *
     * Private Methods                                               		   *
     *                                                                         *
     **************************************************************************/
	/**
     * Generate a context Menu in order to fix/unfix some column
     * It is shown when right-clicking on the column header
     * @return
     */
    private ContextMenu getColumnContextMenu(){
    	final ContextMenu contextMenu = new ContextMenu();

    	this.fixItem = new CheckMenuItem("Fix");
    	//FIXME Conflict between this item and fix of SpreadsheetView, not important right now
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
    }
    
}
