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

import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetColumn;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

/**
 * The set of horizontal (column) headers.
 */
public class HorizontalHeader extends TableHeaderRow {
	final GridViewSkin gridViewSkin;
	
    // Indicate whether the this HorizontalHeader is activated or not
    private boolean working = true;
    
    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
    public HorizontalHeader(final GridViewSkin skin) {
        super(skin);
        gridViewSkin = skin;
    }
    
    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/
    
    public void init() {
        updateHorizontalHeaderVisibility(gridViewSkin.spreadsheetView.isShowColumnHeader());
        
		final SpreadsheetView view = gridViewSkin.spreadsheetView;
		
		//Visibility of vertical Header listener
	    view.showRowHeaderProperty().addListener(verticalHeaderListener);
	    
	    //Visibility of horizontal Header listener
	    view.showColumnHeaderProperty().addListener(horizontalHeaderVisibilityListener);
	    
	    //Selection listener to highlight header
	    gridViewSkin.getSelectedColumns().addListener(selectionListener);
	    
	    //Fixed Column listener to change style of header
	    view.getFixedColumns().addListener(fixedColumnsListener);
	    
	   
		
		final Runnable r = new Runnable() {
            @Override
            public void run() {
            	 //We are doing that because some columns may be already fixed.
        		for(SpreadsheetColumn column: view.getFixedColumns()){
        			fixColumn(column);
        		}
            	requestLayout();
			    /**
			     * Clicking on header select the cell situated in that column.
			     * This may be replaced by selecting the entire Column/Row.
			     */
			    for (final TableColumnHeader i : getRootHeader().getColumnHeaders()) {
			        i.getChildrenUnmodifiable().get(0).setOnMousePressed(new EventHandler<MouseEvent>(){
						@Override
						public void handle(MouseEvent arg0) {
							if(arg0.isPrimaryButtonDown()){
								TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = gridViewSkin.handle.getView().getSelectionModel();
								TableViewFocusModel<ObservableList<SpreadsheetCell>> fm = gridViewSkin.handle.getGridView().getFocusModel();
								sm.clearAndSelect(fm.getFocusedCell().getRow(),i.getTableColumn() );
							}
						}
					});
			    }
            }
        };
        Platform.runLater(r);

    }
    
    @Override
    public HorizontalHeaderColumn getRootHeader() {
        return (HorizontalHeaderColumn) super.getRootHeader();
    }

    /***************************************************************************
     * 
     * Protected Methods 
     * 
     **************************************************************************/
    @Override
    protected void updateTableWidth() {
        super.updateTableWidth();
        // snapping added for RT-19428
        double padding = 0;

        if (working && gridViewSkin != null
                && gridViewSkin.spreadsheetView != null
                && gridViewSkin.spreadsheetView.showRowHeaderProperty().get()) {
            padding += gridViewSkin.getVerticalHeaderWidth();
        }

        Rectangle clip = ((Rectangle) getClip());
        clip.setWidth(clip.getWidth() == 0 ? 0 : clip.getWidth() - padding);
    }

    @Override
    protected void updateScrollX() {
        super.updateScrollX();

        if (working) {
            requestLayout();
            getRootHeader().layoutFixedColumns();
        }
    }

    @Override
    protected NestedTableColumnHeader createRootHeader() {
        return new HorizontalHeaderColumn(getTableSkin(), null);
    }

    /***************************************************************************
     * 
     * Private Methods 
     * 
     **************************************************************************/
    
    /**
     *Whether the Vertical Header is showing, we need to update the width because some
     *space on the left will be available/used.
     */
    private final ChangeListener<Boolean> verticalHeaderListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
			 updateTableWidth();
		}
    };
    
    /**
     * Whether the Horizontal Header is showing, we need to toggle its visibility.
     */
    private final ChangeListener<Boolean> horizontalHeaderVisibilityListener = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> arg0,Boolean arg1, Boolean arg2) {
        	updateHorizontalHeaderVisibility(arg2);
        }
    };
    
    /**
     * When we fix/unfix some columns, we change the style of the Label header text
     */
    private final ListChangeListener<SpreadsheetColumn> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn>() {

		@Override
		public void onChanged(
				javafx.collections.ListChangeListener.Change<? extends SpreadsheetColumn> arg0) {
			while(arg0.next()){
				//If we unfix a column
				for (SpreadsheetColumn remitem : arg0.getRemoved()) {
                  unfixColumn(remitem);
                }
				//If we fix one
                for (SpreadsheetColumn additem : arg0.getAddedSubList()) {
                	fixColumn(additem);
                }
			}
			 updateHighlightSelection();
		}
	}; 

	/**
	 * Fix this column regarding the style
	 * @param column
	 */
	private void fixColumn(SpreadsheetColumn column){
		addStyleHeader(gridViewSkin.spreadsheetView.getColumns().indexOf(column));
//		column.setText(column.getText().replace(".", "")+":");
	}
	
	/**
	 * Unfix this column regarding the style
	 * @param column
	 */
	private void unfixColumn(SpreadsheetColumn column){
		 removeStyleHeader(gridViewSkin.spreadsheetView.getColumns().indexOf(column));
//		 column.setText(column.getText().replace(":", "."));
	}
	/**
	 * Add the fix style of the header Label of the specified column
	 * @param i
	 */
	private void removeStyleHeader(Integer i) {
        	getRootHeader().getColumnHeaders().get(i).getStyleClass().removeAll("fixed");
    }
	/**
	 * Remove the fix style of the header Label of the specified column
	 * @param i
	 */
	private void addStyleHeader(Integer i) {
            getRootHeader().getColumnHeaders().get((Integer) i).getStyleClass()
                    .addAll("fixed");
    }
    
    /**
     * When we select some cells, we want the header to be highlighted
     */
    private final InvalidationListener selectionListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable valueModel) {
            updateHighlightSelection();
        }
    };
    
    /**
     * Highlight the header Label when selection change.
     */
    private void updateHighlightSelection() {
    	for (final TableColumnHeader i : getRootHeader().getColumnHeaders()) {
            i.getStyleClass().removeAll("selected");

        }
        final List<Integer> selectedColumns = gridViewSkin.getSelectedColumns();
        for (final Object i : selectedColumns) {
            getRootHeader().getColumnHeaders().get((Integer) i).getStyleClass()
                    .addAll("selected");
        }

    }
    
    private void updateHorizontalHeaderVisibility(boolean visible){
        working = visible;
        setManaged(working);
        if(!visible){
            getStyleClass().add("invisible");
        }else{
            getStyleClass().remove("invisible");
            requestLayout();
            getRootHeader().layoutFixedColumns();
            updateHighlightSelection();
        }
    }
}
