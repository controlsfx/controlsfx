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

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import org.controlsfx.control.spreadsheet.control.SpreadsheetView.SpreadsheetViewSelectionModel;
import org.controlsfx.control.spreadsheet.sponge.TableViewSkinBase;


/**
 * Display a rowHeader, aka, the number of the lines displayed on screen.
 */
public class RowHeader  extends StackPane {


	private final TableViewSkinBase<?, ?, ?, ?> spreadsheetViewSkin;
	private final SpreadsheetView spreadsheetView;
	private double prefHeight;
	private double prefWidth;
	private   Boolean working = true; // Whether or not we are showing the RowHeader
	private SpreadsheetViewSelectionModel<?> selectionModel;

	private final InvalidationListener layout =  new InvalidationListener() {
		@Override
		public void invalidated(Observable arg0) {
			final Runnable r = new Runnable() {
				@Override
				public void run() {
					requestLayout();
				}
			};
			Platform.runLater(r);
		}
	};

	Rectangle clip; // Ensure that children do not go out of bounds

	/******************************************************************
	 * 								CONSTRUCTOR
	 * @param skin
	 * @param spreadsheetView
	 * @param rowHeaderWidth
	 ******************************************************************/
	public RowHeader(final TableViewSkinBase<?, ?, ?, ?> skin, final SpreadsheetView spreadsheetView, final double rowHeaderWidth) {
		this.spreadsheetViewSkin = skin;
		this.spreadsheetView = spreadsheetView;

		final Runnable r = new Runnable() {
			@Override
			public void run() {
				prefHeight = spreadsheetView.getDefaultCellSize();
				prefWidth = rowHeaderWidth;
				selectionModel = spreadsheetView.getSelectionModel();

				//Clip property to stay within bounds
				clip = new Rectangle(prefWidth, snapSize(skin.getSkinnable().getHeight()));
				clip.relocate(snappedTopInset(), snappedLeftInset());
				clip.setSmooth(false);
				clip.heightProperty().bind(skin.getSkinnable().heightProperty());
				RowHeader.this.setClip(clip);

				// We desactivate and activate the rowheader upon request
				spreadsheetView.getRowHeader().addListener(new ChangeListener<Boolean>(){
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0,
							Boolean arg1, Boolean arg2) {
						working = arg2;
						requestLayout();
					}});

				// When the Column header is showing or not, we need to update the position of the rowHeader
				spreadsheetView.getColumnHeader().addListener(layout);

				spreadsheetView.getFixedRows().addListener(layout);
				//In case we resize the view in any manners
				spreadsheetView.heightProperty().addListener(layout);

				// For layout properly the rowHeader when there are some selected items
				selectionModel.getSelectedRows().addListener(layout);
				requestLayout();
			}
		};
		Platform.runLater(r);
	}

	@Override protected void layoutChildren() {
		if(working) {

			final double x =snappedLeftInset();
			//We add prefHeight because we need to take the other header into account
			// And also the fixedRows if any
			double y = snappedTopInset() ;//+prefHeight*flow.getFixedRows().size();
			if(spreadsheetView.getColumnHeader().get()){
				y+=prefHeight;
			}

			//The Labels must be aligned with the rows
			if(!spreadsheetView.isEmptyCells()){
				y += spreadsheetView.getRow(0).getLocalToParentTransform().getTy();
			}

			int rowCount = 0;
			Label label;
			int i=0;

			// We iterate over the visibleRows
			while(!spreadsheetView.isEmptyCells() && spreadsheetView.getRow(i) != null){
				label = getLabel(rowCount++);
				label.setText(String.valueOf(spreadsheetView.getRow(i).getIndexVirtualFlow()));
				label.resize(prefWidth,prefHeight);
				label.relocate(x, y);
				//We want to highlight selected rows
				final ObservableList<String> css = label.getStyleClass();
				if(selectionModel.getSelectedRows().contains(spreadsheetView.getRow(i).getIndex())){
					css.setAll("selected");
				}else{
					css.clear();
				}
				y+=prefHeight;
				++i;
			}

			// Then we iterate over the FixedRows if any
			if(!spreadsheetView.getFixedRows().isEmpty() && !spreadsheetView.getVisibleRows().isEmpty() && !spreadsheetView.isEmptyCells()){
				for(i = 0;i<spreadsheetView.getFixedRows().size();++i){
					label = getLabel(rowCount++);
					label.setText(String.valueOf(i));
					label.resize(prefWidth,prefHeight);

					//If the columnHeader is here, we need to translate a bit
					if(spreadsheetView.getColumnHeader().get()){
						label.relocate(x, snappedTopInset()+prefHeight*(i+1));
					}else{
						label.relocate(x, snappedTopInset()+prefHeight*i);
					}
					final ObservableList<String> css = label.getStyleClass();
					if(selectionModel.getSelectedRows().contains(spreadsheetView.getRow(i).getIndex())){
						css.setAll("selected");
					}else{
						css.clear();
					}
					y+=prefHeight;
				}
			}

			//First one blank and on top (z-order) of the others
			if(spreadsheetView.getColumnHeader().get()){
				label = getLabel(rowCount++);
				label.setText("");
				label.resize(prefWidth,prefHeight);
				label.relocate(x, 0);
				label.getStyleClass().clear();
			}

			//Last one blank and on top (z-order) of the others
			label = getLabel(rowCount++);
			label.setText("");
			label.resize(prefWidth,spreadsheetView.getHbar().getHeight());
			label.relocate(snappedLeftInset(), getHeight()-spreadsheetView.getHbar().getHeight());
			label.getStyleClass().clear();

			//Flush the rest of the children if any
			while(getChildren().size() > rowCount){
				getChildren().remove(rowCount);
			}
		}else{
			getChildren().clear();
		}
	}

	/**
	 * Called when value of vertical scrollbar change
	 */
	public void updateScrollY() {
		if(working) {
			requestLayout();
		}
	}

	/**
	 * Create a new label and put it in the pile or just
	 * grab one from the pile.
	 * @param rowNumber
	 * @return
	 */
	private Label getLabel(int rowNumber){
		if(getChildren().isEmpty() || getChildren().size()<=rowNumber){//pile.isEmpty() || pile.size() <= rowNumber){
			final Label label = new Label();
			label.resize(prefWidth,prefHeight);
			//pile.add(label);
			getChildren().add(label);
			return label;
		}else{
			return (Label) getChildren().get(rowNumber);
			//return pile.get(rowNumber);
		}
	}

}
