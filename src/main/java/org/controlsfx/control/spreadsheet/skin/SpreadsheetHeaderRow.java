package org.controlsfx.control.spreadsheet.skin;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.spreadsheet.control.SpreadsheetView.SpreadsheetViewSelectionModel;
import org.controlsfx.control.spreadsheet.model.DataRow;
import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;

public class SpreadsheetHeaderRow extends TableHeaderRow{

	SpreadsheetViewSkin spreadsheetViewSkin;
	public SpreadsheetHeaderRow(final SpreadsheetViewSkin skin) {
		super(skin);
		spreadsheetViewSkin = skin;
	}
	public void init(){
		final Runnable r = new Runnable() {
			@Override
			public void run() {
		
				spreadsheetViewSkin.spreadsheetView.getRowHeader().addListener(rowHeaderListener);
				selectionModel = spreadsheetViewSkin.spreadsheetView.getSelectionModel();
				selectionModel.getSelectedColumns().addListener(selectionListener);
				spreadsheetViewSkin.spreadsheetView.getFixedColumns().addListener(fixedColumnsListener);
			
		
		spreadsheetViewSkin.getTableMenuButtonVisibleProperty().addListener(new InvalidationListener() {
			@Override public void invalidated(Observable valueModel) {
				if(working) {
					requestLayout();
				}
			}
		});
		
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		// We listen to the BooleanProperty linked with the CheckBox of the columnHeader
		spreadsheetViewSkin.spreadsheetView.getColumnHeader().addListener(new ChangeListener<Boolean>(){
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean arg1, Boolean arg2) {
				working = arg2;
				requestLayout();
				getRootHeader().layoutFixedColumns();
				updateHighlighSelection();
			}});
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
			}
		};
		Platform.runLater(r);
	}
	
	
	protected void updateTableWidth() {
		super.updateTableWidth();
		// snapping added for RT-19428
		double padding = 0;
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		if(spreadsheetViewSkin != null && spreadsheetViewSkin.spreadsheetView != null && spreadsheetViewSkin.spreadsheetView.getRowHeader().get()){
			padding += spreadsheetViewSkin.getRowHeaderWidth();
		}

		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
		Rectangle clip = ((Rectangle)getClip());
		clip.setWidth(clip.getWidth() == 0? 0 :clip.getWidth() - padding );
	}
	
	protected void updateScrollX() {
		super.updateScrollX();
		/*****************************************************************
		 * 				MODIFIED BY NELLARMONIA
		 *****************************************************************/
		if(working) {
			requestLayout();
			getRootHeader().layoutFixedColumns();
		}
		/*****************************************************************
		 * 				END OF MODIFIED BY NELLARMONIA
		 *****************************************************************/
	}
	
	// Indicate wether the this TableHeaderRow is activated or not
		private   Boolean working = true;

		/**
		 * When the Rowheader is showing (or not anymore) we need to react accordingly
		 */
		private final InvalidationListener rowHeaderListener = new InvalidationListener() {
			@Override public void invalidated(Observable valueModel) {
				updateTableWidth();
			}
		};
		/**
		 * When we fix/unfix some columns, the header must react accordingly
		 * TODO maybe modify and remove that "runLater"
		 * TODO But if if have not that "runLater", we call "layoutFixedColumns" too early..
		 */
		private final InvalidationListener fixedColumnsListener = new InvalidationListener() {
			@Override public void invalidated(Observable valueModel) {
				getRootHeader().updateHeader();
				getRootHeader().layoutFixedColumns();
				final Runnable r = new Runnable() {
					@Override
					public void run() {
						updateHighlighSelection();
					}
				};
				Platform.runLater(r);
			}
		};

		private SpreadsheetViewSelectionModel<DataRow> selectionModel;
		private void updateHighlighSelection(){
			for (final TableColumnHeader i : getRootHeader().getColumnHeaders()) {
				i.getChildrenUnmodifiable().get(0).getStyleClass().clear();

			}
			final List<Integer> selectedColumns = selectionModel.getSelectedColumns();
			//TODO Ugly hack to get access to the Label
			for (final Object i : selectedColumns) {
				getRootHeader().getColumnHeaders().get((Integer)i).getChildrenUnmodifiable().get(0).getStyleClass().addAll("selected");
			}

		}
		/**
		 * When we select some cells, we want the header to be highlighted
		 */
		private final InvalidationListener selectionListener = new InvalidationListener() {
			@Override public void invalidated(Observable valueModel) {
				updateHighlighSelection();
			}
		};
		
		protected NestedTableColumnHeader createRootHeader() {
	        return new SpreadsheetNestedTableColumnHeader(getTableSkin(), null);
	    } 
		
		public SpreadsheetNestedTableColumnHeader getRootHeader() {
	        return (SpreadsheetNestedTableColumnHeader) super.getRootHeader();
	    }

}
