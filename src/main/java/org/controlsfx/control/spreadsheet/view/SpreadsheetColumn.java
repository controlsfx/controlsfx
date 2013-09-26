package org.controlsfx.control.spreadsheet.view;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableColumn;

import org.controlsfx.control.SpreadsheetView;
import org.controlsfx.control.spreadsheet.model.SpreadsheetCell;

/**
 *A {@link SpreadsheetView} is made up of a number of {@link SpreadsheetColumn} instances.
 * You can then modify some informations like the width of the column or whether it is fixed or not.
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
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
	/**
	 * Creates a new SpreadsheetColumn with an initial width of 100.
	 * @param column
	 * @param spreadsheetView
	 * @param indexColumn
	 */
	public SpreadsheetColumn(final TableColumn<ObservableList<SpreadsheetCell<T>>, SpreadsheetCell<T>> column, SpreadsheetView spreadsheetView, Integer indexColumn) {
		this.spreadsheetView = spreadsheetView;
		this.column = column;
		column.setMinWidth(30); 
		this.indexColumn = indexColumn;
//		this.columnSpanConstraint = 0;
//		this.columnSpanConstraint = 0;
		canFix = canFix();
		
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
				canFix();
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
	 * @return
	 */
	public boolean isFixed() {
//		return column.impl_isFixed();
		return spreadsheetView.getFixedColumns().contains(this);
	}
	
	/**
	 * Fix this column to the left (if possible)
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
	 * @return
	 */
	private boolean canFix(){
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
